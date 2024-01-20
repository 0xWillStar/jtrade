
CREATE TABLE jtrade_trade.t_asset_info (
  `asset` varchar(32) DEFAULT NULL,
  `index_price_symbol` varchar(64) DEFAULT NULL,
  `discount` decimal(6,4) DEFAULT NULL,
  `deduct_order` int DEFAULT NULL,
  `scale` int DEFAULT NULL,
  KEY `idx_asset_info` (`asset`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_asset_balance (
  `exchange_id` varchar(64) DEFAULT NULL,
  `member_id` varchar(64) DEFAULT NULL,
  `client_id` varchar(64) DEFAULT NULL,
  `asset` varchar(32) DEFAULT NULL,
  `pre_balance` decimal(28,12) DEFAULT NULL,
  `balance` decimal(28,12) DEFAULT NULL,
  `deposit` decimal(28,12) DEFAULT NULL,
  `withdraw` decimal(28,12) DEFAULT NULL,
  `position_margin` decimal(28,12) DEFAULT NULL,
  `close_profit` decimal(28,12) DEFAULT NULL,
  `fee` decimal(28,12) DEFAULT NULL,
  `money_change` decimal(28,12) DEFAULT NULL,
  `frozen_margin` decimal(28,12) DEFAULT NULL,
  `frozen_money` decimal(28,12) DEFAULT NULL,
  `frozen_fee` decimal(28,12) DEFAULT NULL,
  `available` decimal(28,12) DEFAULT NULL,
  `withdrawable` decimal(28,12) DEFAULT NULL,
  `position_amt` decimal(28,12) DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  `row_key` varchar(64) DEFAULT NULL,
  `isolated_balance` decimal(28,12) DEFAULT NULL,
  KEY `idx_client_asset` (`client_id`,`asset`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_bill (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `exchange_id` varchar(64) DEFAULT NULL,
  `member_id` varchar(64) DEFAULT NULL,
  `client_id` varchar(64) DEFAULT NULL,
  `symbol` varchar(64) DEFAULT NULL,
  `bill_type` varchar(32) DEFAULT NULL,
  `asset` varchar(32) DEFAULT NULL,
  `amount` decimal(28,12) DEFAULT NULL,
  `info` varchar(512) DEFAULT NULL,
  `correlation_id` varchar(64) DEFAULT NULL,
  `insert_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_client` (`client_id`) USING BTREE
) ENGINE=InnoDB;


CREATE TABLE jtrade_trade.t_client_authority (
  `client_id` varchar(64) DEFAULT NULL,
  `trade_authority` int DEFAULT NULL,
  KEY `idx_client` (`client_id`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_client_fee_rate (
  `client_id` varchar(64) DEFAULT NULL,
  `maker` decimal(28,12) DEFAULT NULL,
  `taker` decimal(28,12) DEFAULT NULL,
  KEY `idx_client` (`client_id`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_client_setting (
  `client_id` varchar(64) DEFAULT NULL,
  `symbol` varchar(64) DEFAULT NULL,
  `leverage` decimal(28,12) DEFAULT NULL,
  `margin_type` varchar(32) DEFAULT NULL,
  KEY `idx_client_symbol` (`client_id`,`symbol`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_order (
  `exchange_id` varchar(64) DEFAULT NULL,
  `member_id` varchar(64) DEFAULT NULL,
  `client_id` varchar(64) DEFAULT NULL,
  `symbol` varchar(64) DEFAULT NULL,
  `side` varchar(32) DEFAULT NULL,
  `position_side` varchar(32) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `price` decimal(28,12) DEFAULT NULL,
  `quantity` decimal(28,12) DEFAULT NULL,
  `orig_type` varchar(32) DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL,
  `time_in_force` varchar(32) DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `client_order_id` varchar(64) DEFAULT NULL,
  `reduce_only` varchar(5) DEFAULT NULL,
  `working_type` varchar(32) DEFAULT NULL,
  `stop_price` decimal(28,12) DEFAULT NULL,
  `close_position` varchar(5) DEFAULT NULL,
  `activation_price` decimal(28,12) DEFAULT NULL,
  `callback_rate` decimal(28,12) DEFAULT NULL,
  `price_protect` varchar(5) DEFAULT NULL,
  `order_time` bigint DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  `frozen_fee` decimal(28,12) DEFAULT NULL,
  `frozen_margin` decimal(28,12) DEFAULT NULL,
  `cum_quote` decimal(28,12) DEFAULT NULL,
  `executed_qty` decimal(28,12) DEFAULT NULL,
  `avg_price` decimal(28,12) DEFAULT NULL,
  `fee` decimal(28,12) DEFAULT NULL,
  `fee_asset` varchar(32) DEFAULT NULL,
  `close_profit` decimal(28,12) DEFAULT NULL,
  `leverage` decimal(28,12) DEFAULT NULL,
  `left_qty` decimal(28,12) DEFAULT NULL,
  `margin_type` varchar(32) DEFAULT NULL,
  `first_isolated_order` varchar(5) DEFAULT NULL,
  `oto_order_type` varchar(32) DEFAULT NULL,
  `sub_order_id1` bigint DEFAULT NULL,
  `sub_order_id2` bigint DEFAULT NULL,
  KEY `idx_client_symbol_orderid` (`client_id`,`symbol`,`order_id`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_position (
  `exchange_id` varchar(64) DEFAULT NULL,
  `member_id` varchar(64) DEFAULT NULL,
  `client_id` varchar(64) DEFAULT NULL,
  `symbol` varchar(64) DEFAULT NULL,
  `position_side` varchar(32) DEFAULT NULL,
  `margin_type` varchar(32) DEFAULT NULL,
  `position_amt` decimal(28,12) DEFAULT NULL,
  `long_frozen_amt` decimal(28,12) DEFAULT NULL,
  `short_frozen_amt` decimal(28,12) DEFAULT NULL,
  `open_price` decimal(28,12) DEFAULT NULL,
  `position_margin` decimal(28,12) DEFAULT NULL,
  `long_frozen_margin` decimal(28,12) DEFAULT NULL,
  `short_frozen_margin` decimal(28,12) DEFAULT NULL,
  `leverage` decimal(28,12) DEFAULT NULL,
  `asset` varchar(32) DEFAULT NULL,
  `auto_add_margin` varchar(5) DEFAULT NULL,
  `isolated_balance` decimal(28,12) DEFAULT NULL,
  `isolated_frozen_fee` decimal(28,12) DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  `reduce_only_order_count` int DEFAULT NULL,
  KEY `idx_client_symbol_side` (`client_id`,`symbol`,`position_side`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_symbol_info (
  `exchange_id` varchar(64) DEFAULT NULL,
  `product_type` varchar(32) DEFAULT NULL,
  `symbol` varchar(64) DEFAULT NULL,
  `underlying` varchar(64) DEFAULT NULL,
  `position_type` varchar(32) DEFAULT NULL,
  `strike_price` decimal(28,12) DEFAULT NULL,
  `options_type` varchar(32) DEFAULT NULL,
  `volume_multiple` decimal(28,12) DEFAULT NULL,
  `price_asset` varchar(32) DEFAULT NULL,
  `clear_asset` varchar(32) DEFAULT NULL,
  `base_asset` varchar(32) DEFAULT NULL,
  `margin_price_type` varchar(32) DEFAULT NULL,
  `trade_price_mode` varchar(32) DEFAULT NULL,
  `basis_price` decimal(28,12) DEFAULT NULL,
  `min_order_quantity` decimal(28,12) DEFAULT NULL,
  `max_order_quantity` decimal(28,12) DEFAULT NULL,
  `price_tick` decimal(28,12) DEFAULT NULL,
  `quantity_tick` decimal(28,12) DEFAULT NULL,
  `inverse` varchar(5) DEFAULT NULL,
  `create_time` bigint DEFAULT NULL,
  `ordering_time` bigint DEFAULT NULL,
  `trading_time` bigint DEFAULT NULL,
  `expire_time` bigint DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `max_leverage` decimal(28,12) DEFAULT NULL,
  `default_leverage` decimal(28,12) DEFAULT NULL,
  `maintenance_margin_rate` decimal(28,12) DEFAULT NULL,
  `independent_match_thread` varchar(5) DEFAULT NULL,
  `clear_asset_scale` int DEFAULT NULL,
  `price_asset_scale` int DEFAULT NULL,
  `quantity_scale` int DEFAULT NULL,
  `impact_value` decimal(28,12) DEFAULT NULL,
  KEY `idx_symbol` (`symbol`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_system_parameter (
  `parameter` varchar(64) DEFAULT NULL,
  `value` varchar(64) DEFAULT NULL,
  KEY `idx_parameter` (`parameter`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_trade (
  `exchange_id` varchar(64) DEFAULT NULL,
  `member_id` varchar(64) DEFAULT NULL,
  `client_id` varchar(64) DEFAULT NULL,
  `symbol` varchar(64) DEFAULT NULL,
  `trade_id` bigint DEFAULT NULL,
  `side` varchar(32) DEFAULT NULL,
  `position_side` varchar(32) DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `client_order_id` varchar(64) DEFAULT NULL,
  `price` decimal(28,12) DEFAULT NULL,
  `qty` decimal(28,12) DEFAULT NULL,
  `quote_qty` decimal(28,12) DEFAULT NULL,
  `close_profit` decimal(28,12) DEFAULT NULL,
  `fee` decimal(28,12) DEFAULT NULL,
  `fee_asset` varchar(32) DEFAULT NULL,
  `match_role` varchar(32) DEFAULT NULL,
  `trade_time` bigint DEFAULT NULL,
  `trade_type` varchar(32) DEFAULT NULL,
  `liquidation_price` decimal(28,12) DEFAULT NULL,
  KEY `idx_client_symbol_tradeid_side` (`client_id`,`symbol`,`trade_id`,`side`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_finish_order (
  `exchange_id` varchar(64) DEFAULT NULL,
  `member_id` varchar(64) DEFAULT NULL,
  `client_id` varchar(64) DEFAULT NULL,
  `symbol` varchar(64) DEFAULT NULL,
  `side` varchar(32) DEFAULT NULL,
  `position_side` varchar(32) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `price` decimal(28,12) DEFAULT NULL,
  `quantity` decimal(28,12) DEFAULT NULL,
  `orig_type` varchar(32) DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL,
  `time_in_force` varchar(32) DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `client_order_id` varchar(64) DEFAULT NULL,
  `reduce_only` varchar(5) DEFAULT NULL,
  `working_type` varchar(32) DEFAULT NULL,
  `stop_price` decimal(28,12) DEFAULT NULL,
  `close_position` varchar(5) DEFAULT NULL,
  `activation_price` decimal(28,12) DEFAULT NULL,
  `callback_rate` decimal(28,12) DEFAULT NULL,
  `price_protect` varchar(5) DEFAULT NULL,
  `order_time` bigint DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  `frozen_fee` decimal(28,12) DEFAULT NULL,
  `frozen_margin` decimal(28,12) DEFAULT NULL,
  `cum_quote` decimal(28,12) DEFAULT NULL,
  `executed_qty` decimal(28,12) DEFAULT NULL,
  `avg_price` decimal(28,12) DEFAULT NULL,
  `fee` decimal(28,12) DEFAULT NULL,
  `fee_asset` varchar(32) DEFAULT NULL,
  `close_profit` decimal(28,12) DEFAULT NULL,
  `leverage` decimal(28,12) DEFAULT NULL,
  `left_qty` decimal(28,12) DEFAULT NULL,
  `margin_type` varchar(32) DEFAULT NULL,
  `first_isolated_order` varchar(5) DEFAULT NULL,
  `oto_order_type` varchar(32) DEFAULT NULL,
  `sub_order_id1` bigint DEFAULT NULL,
  `sub_order_id2` bigint DEFAULT NULL,
  KEY `idx_client_symbol_orderid` (`client_id`,`symbol`,`order_id`) USING BTREE
) ENGINE=InnoDB;

CREATE TABLE jtrade_trade.t_accomplish (
  `worker_id` int DEFAULT NULL,
  `batch_id` bigint DEFAULT NULL,
  KEY `idx_workerid` (`worker_id`) USING BTREE
) ENGINE=InnoDB;
