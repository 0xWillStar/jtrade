
CREATE TABLE jtrade_market.t_kline_eth_usdc (
  `symbol` varchar(64) NOT NULL,
  `period` varchar(16) NOT NULL,
  `begin_time` bigint NOT NULL,
  `end_time` bigint NOT NULL,
  `open_price` decimal(28,12),
  `high_price` decimal(28,12),
  `low_price` decimal(28,12),
  `close_price` decimal(28,12),
  `volume` decimal(28,12),
  `quote_volume` decimal(28,12),
  `count` bigint DEFAULT NULL,
  PRIMARY KEY (`symbol`,`period`,`begin_time`)
) ENGINE=InnoDB;

