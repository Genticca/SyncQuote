/*
================================================================================
 SyncQuote — MySQL Schema
--------------------------------------------------------------------------------
 Go 수집기(common/persist.go, common/mysql.go)가 실제로 INSERT 하는 테이블만
 정의한다. 테이블/컬럼명은 코드(mysql.go의 INSERT 문)를 기준으로 맞춘다.

 [Redis → MySQL 매핑]
 ──────────────────────────────────────────────────────────────────────────────
  Redis Key                       자료구조  →  MySQL Table       방식
 ──────────────────────────────────────────────────────────────────────────────
  markets:{exchange}              Hash      →  market_info       UPSERT
  candles:1s:{exchange}:{code}    List      →  candles (1m 집계) UPSERT
  ticker:{exchange}:{code}        Stream    →  ticker_latest     UPSERT(최신1건)
 ──────────────────────────────────────────────────────────────────────────────

 [MySQL에 저장하지 않는 데이터 — 설계상 의도]
  - 52주 메타(ticker:rest:*)  : Redis Hash 로만 서빙. REST 1분 폴링으로 항상
                                재취득 가능 → 영구 보관 불필요. (테이블 미생성)
  - 현재가 이력               : 과거 가격 흐름은 candles(1분봉)가 담당.
                                현재가는 ticker_latest(최신 1행)만 유지. (이력 테이블 미생성)
  - 1초봉                     : Redis 전용(실시간). MySQL엔 1분봉으로 압축 저장.

 [구현 사실 — 코드 근거]
  1) 아카이빙 저장 순서: market_info → candles → ticker_latest.
     자식(candles, ticker_latest)이 market_info 를 FK로 참조하므로 부모(페어)를
     먼저 저장해야 FK 위반이 없다. (persist.go archiveOnce)
  2) candles 에 들어오는 interval_type 은 현재 '1m' 하나뿐. 1초봉 List 를 읽어
     "완성된 분"만 1분봉으로 집계해 저장한다. (1d/1w/1mo 등은 향후 집계 추가 시)
  3) 모든 저장은 UPSERT. 60초 배치가 같은 PK 를 반복 저장해도 최신값으로
     덮어써져 중복 행이 생기지 않는다.
  4) DECIMAL 정밀도: 가격류 (30,12) — 소액 알트코인 다수 소수점 수용,
                     누적금액류 (36,12) — 원화 대단위 거래대금 수용.
================================================================================
*/

CREATE DATABASE IF NOT EXISTS syncquote
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE syncquote;


/* ============================================================================
 * 1) market_info — 거래소별 마켓/페어 기준 테이블
 * ----------------------------------------------------------------------------
 *   - Redis markets:{exchange} Hash(JSON) → archiveMarkets() 가 채운다.
 *   - candles / ticker_latest 가 FK로 참조하는 부모 테이블.
 *   - PK (exchange, market): 거래소가 PK에 포함돼 upbit/binance 가 섞이지 않음.
 * ==========================================================================*/
CREATE TABLE IF NOT EXISTS market_info (
  exchange       VARCHAR(20)  NOT NULL                COMMENT '거래소: upbit, binance',
  market         VARCHAR(30)  NOT NULL                COMMENT '페어 코드: KRW-BTC, BTCUSDT',
  korean_name    VARCHAR(100) NULL                    COMMENT '업비트 한글명, 바이낸스는 NULL',
  english_name   VARCHAR(100) NULL                    COMMENT '영문명 또는 base asset(BTC 등)',
  market_warning VARCHAR(30)  NULL                    COMMENT '업비트 유의종목 표기: NONE/CAUTION 등',
  event_warning  BOOLEAN      NOT NULL DEFAULT FALSE  COMMENT '거래소 경보(warning) 플래그',
  event_caution  BOOLEAN      NOT NULL DEFAULT FALSE  COMMENT '주의 플래그(가격변동/거래량급등 등 OR 합성)',
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                            COMMENT '최초 적재 시각',
  updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최근 갱신 시각',
  PRIMARY KEY (exchange, market),
  INDEX idx_market_exchange     (exchange),       -- 거래소별 페어 목록 조회
  INDEX idx_market_korean_name  (korean_name),    -- 한글명 검색(자동완성 등)
  INDEX idx_market_english_name (english_name)    -- 영문명/심볼 검색
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='거래소별 마켓(페어) 기준 정보. 모든 시세 테이블의 FK 부모.';


/* ============================================================================
 * 2) candles — 차트용 봉 데이터 (OHLCV)
 * ----------------------------------------------------------------------------
 *   - Redis candles:1s:{exchange}:{code} List → archiveCandles() 가
 *     "완성된 분"만 1분봉으로 집계해 interval_type='1m' 으로 저장한다.
 *   - 현재 적재되는 interval_type = '1m' 뿐. (1s 는 Redis 전용)
 *   - candle_time : 봉 시작 시각, Unix seconds (BIGINT). 코드의 c.Time 그대로.
 * ==========================================================================*/
CREATE TABLE IF NOT EXISTS candles (
  exchange      VARCHAR(20)    NOT NULL  COMMENT '거래소: upbit, binance',
  market        VARCHAR(30)    NOT NULL  COMMENT '페어 코드: KRW-BTC, BTCUSDT',
  interval_type VARCHAR(10)    NOT NULL  COMMENT '봉 단위. 현재 적재값=1m (향후 1d/1w/1mo 등)',
  candle_time   BIGINT         NOT NULL  COMMENT '봉 시작 시각, Unix seconds',
  open_price    DECIMAL(30,12) NOT NULL  COMMENT '시가',
  high_price    DECIMAL(30,12) NOT NULL  COMMENT '고가',
  low_price     DECIMAL(30,12) NOT NULL  COMMENT '저가',
  close_price   DECIMAL(30,12) NOT NULL  COMMENT '종가',
  volume        DECIMAL(30,12) NOT NULL  COMMENT '해당 봉 거래량 합',
  created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '적재 시각',
  PRIMARY KEY (exchange, market, interval_type, candle_time),
  -- 차트 조회는 거의 항상 "특정 페어 + 봉단위 + 시간범위" 패턴.
  INDEX idx_candles_lookup (exchange, market, interval_type, candle_time),
  INDEX idx_candles_time   (interval_type, candle_time),   -- 봉단위 전체 시간 스캔
  CONSTRAINT fk_candles_market
    FOREIGN KEY (exchange, market)
    REFERENCES market_info (exchange, market)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='차트용 OHLCV 봉. 현재 1분봉만 적재(1초봉은 Redis 전용).';

/* 조회 예시
   SELECT * FROM candles
   WHERE exchange='upbit' AND market='KRW-BTC' AND interval_type='1m'
     AND candle_time BETWEEN ? AND ?
   ORDER BY candle_time;                                                       */


/* ============================================================================
 * 3) ticker_latest — 현재가 최신 스냅샷 (종목당 1행)
 * ----------------------------------------------------------------------------
 *   - Redis ticker:{exchange}:{code} Stream 최신 1건 → archiveTickers() 가
 *     SaveTickerToMySQL() 로 UPSERT. 화면 "현재가 목록"의 직접 소스.
 *   - PK (exchange, market): 종목당 1행만 유지, 항상 최신값으로 덮어씀.
 *   - change_status NOT NULL: 코드가 빈 값이면 'EVEN' 으로 보정해 넣는다.
 *   - event_timestamp : Unix milliseconds (BIGINT).
 * ==========================================================================*/
CREATE TABLE IF NOT EXISTS ticker_latest (
  exchange             VARCHAR(20)    NOT NULL  COMMENT '거래소: upbit, binance',
  market               VARCHAR(30)    NOT NULL  COMMENT '페어 코드',
  trade_price          DECIMAL(30,12) NOT NULL  COMMENT '현재가',
  change_status        VARCHAR(10)    NOT NULL  COMMENT 'RISE/EVEN/FALL (빈값은 EVEN 보정)',
  change_rate          DECIMAL(20,12) NOT NULL  COMMENT '변화율 절댓값(부호 없음)',
  signed_change_rate   DECIMAL(20,12) NOT NULL  COMMENT '부호 있는 변화율(소수). 화면 +2.34% 표시용',
  signed_change_price  DECIMAL(30,12) NOT NULL  COMMENT '부호 있는 변화액',
  acc_trade_price_24h  DECIMAL(36,12) NOT NULL  COMMENT '24h 누적 거래대금(원화 대단위 수용)',
  acc_trade_volume_24h DECIMAL(36,12) NOT NULL  COMMENT '24h 누적 거래량',
  high_price           DECIMAL(30,12) NOT NULL  COMMENT '당일 고가',
  low_price            DECIMAL(30,12) NOT NULL  COMMENT '당일 저가',
  event_timestamp      BIGINT         NOT NULL  COMMENT '거래소 이벤트 시각, Unix milliseconds',
  updated_at           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                                                COMMENT '최근 갱신 시각',
  PRIMARY KEY (exchange, market),
  -- 화면 "거래대금 상위" / "급등락 상위" 정렬에 직접 쓰인다.
  INDEX idx_latest_trade_amount (exchange, acc_trade_price_24h DESC),
  INDEX idx_latest_change_rate  (exchange, signed_change_rate DESC),
  INDEX idx_latest_updated_at   (updated_at),    -- 갱신 지연 모니터링
  CONSTRAINT fk_ticker_latest_market
    FOREIGN KEY (exchange, market)
    REFERENCES market_info (exchange, market)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='종목당 1행 현재가 최신 스냅샷. 화면 현재가 목록의 직접 소스.';
