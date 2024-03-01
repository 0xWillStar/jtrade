package com.crypto.jtrade.common.constants;

import java.util.Formatter;

/**
 * define constants
 *
 * @author 0xWill
 */
public final class Constants {

    public static final String NEW_LINE;

    static {
        String newLine;
        try {
            newLine = new Formatter().format("%n").toString();
        } catch (final Exception e) {
            newLine = "\n";
        }
        NEW_LINE = newLine;
    }

    public static final String UNDER_LINE = "_";

    public static final String COLON_STR = ":";

    public static final String DOT_STR = ".";

    public static final int MAX_DECIMAL = 10;

    public static final int MAX_SYMBOL_COUNT = 64;

    public static final int FUNDING_RATE_INTERVAL_HOURS = 8;

    public static final int FUNDING_RATE_INTERVAL_SECONDS = FUNDING_RATE_INTERVAL_HOURS * 3600;

    public static final String DEFAULT = "default";

    public static final String NULL = "null";

    public static final String EMPTY = "";

    public static final char COMMA = ',';

    public static final String RABBIT_BATCH_ID = "my_batchId";

    /**
     * Redis key config
     */
    public static final String REDIS_KEY_SYSTEM_PARAMETER = "jtrade:systemParameter";

    public static final String REDIS_KEY_SYMBOL = "jtrade:symbol";

    public static final String REDIS_KEY_ASSET = "jtrade:asset";

    public static final String[] REDIS_KEY_BALANCE = {"jtrade:clientId:", null, ":balance"};

    public static final String[] REDIS_KEY_POSITION = {"jtrade:clientId:", null, ":position"};

    public static final String[] REDIS_KEY_ORDER = {"jtrade:clientId:", null, ":order"};

    public static final String[] REDIS_KEY_CLIENT_SETTING = {"jtrade:clientId:", null, ":setting"};

    public static final String[] REDIS_KEY_CLIENT_FEE_RATE = {"jtrade:clientId:", null, ":feeRate"};

    public static final String[] REDIS_KEY_CLIENT_AUTHORITY = {"jtrade:clientId:", null, ":authority"};

    public static final String REDIS_KEY_ORDER_CLIENTS = "jtrade:orderClients";

    public static final String REDIS_KEY_POSITION_CLIENTS = "jtrade:positionClients";

    public static final String REDIS_KEY_DEBT_CLIENTS = "jtrade:debtClients";

    public static final String[] REDIS_KEY_PREMIUM = {"jtrade:symbol:", null, ":premium"};

    public static final String[] REDIS_KEY_MARK_PRICE_MA = {"jtrade:symbol:", null, ":markPriceForMA"};

    public static final String[] REDIS_KEY_FRONT_API_KEY = {"jtrade:front:apiKey:", null};

    public static final String REDIS_KEY_COMMAND_LOG = "jtrade:command:log";

    /**
     * Product Type
     */
    public static final long USE_SPOT = 1 << ProductType.SPOT.ordinal();

    public static final long USE_DELIVERY = 1 << ProductType.DELIVERY.ordinal();

    public static final long USE_PERPETUAL = 1 << ProductType.PERPETUAL.ordinal();

    public static final long USE_OPTIONS = 1 << ProductType.OPTIONS.ordinal();

    public static final long USE_ALL_PRODUCT_TYPE = USE_SPOT | USE_DELIVERY | USE_PERPETUAL | USE_OPTIONS;

    /**
     * Rule action
     */
    public static final long USE_SET_FUNDING_RATE = 1 << CommandIdentity.SET_FUNDING_RATE.ordinal();

    public static final long USE_PLACE_ORDER = 1 << CommandIdentity.PLACE_ORDER.ordinal();

    public static final long USE_CANCEL_ORDER = 1 << CommandIdentity.CANCEL_ORDER.ordinal();

    public static final long USE_LIQUIDATION_CANCEL_ORDER = 1 << CommandIdentity.LIQUIDATION_CANCEL_ORDER.ordinal();

    public static final long USE_ORDER_MATCHED = 1 << CommandIdentity.ORDER_MATCHED.ordinal();

    public static final long USE_ORDER_CANCELED = 1 << CommandIdentity.ORDER_CANCELED.ordinal();

    public static final long USE_OTC = 1 << CommandIdentity.OTC_TRADE.ordinal();

    public static final long USE_DEPOSIT = 1 << CommandIdentity.DEPOSIT.ordinal();

    public static final long USE_WITHDRAW = 1 << CommandIdentity.WITHDRAW.ordinal();

    public static final long USE_SET_CLIENT_SETTING = 1 << CommandIdentity.SET_CLIENT_SETTING.ordinal();

    public static final long USE_ADJUST_POSITION_MARGIN = 1 << CommandIdentity.ADJUST_POSITION_MARGIN.ordinal();

    public static final long USE_TRIGGER_SECONDARY_ORDER = 1 << CommandIdentity.TRIGGER_SECONDARY_ORDER.ordinal();

    public static final long USE_DEDUCT_COLLATERAL = 1 << CommandIdentity.DEDUCT_COLLATERAL.ordinal();

    /**
     * OrderSide
     */
    public static final int USE_BUY = 1 << OrderSide.BUY.ordinal();

    public static final int USE_SELL = 1 << OrderSide.SELL.ordinal();

    /**
     * Trade authority
     */
    public static final int AUTH_OPEN_POSITION = 1 << TradeAuthority.OPEN_POSITION.ordinal();

    public static final int AUTH_CLOSE_POSITION = 1 << TradeAuthority.CLOSE_POSITION.ordinal();

    public static final int AUTH_CANCEL_ORDER = 1 << TradeAuthority.CANCEL_ORDER.ordinal();

    public static final int DEFAULT_TRADE_AUTHORITY = AUTH_OPEN_POSITION | AUTH_CLOSE_POSITION | AUTH_CANCEL_ORDER;

    public static final int NO_TRADE_AUTHORITY = 0;

    /**
     * kline table name
     */
    public static final String[] KLINE_TABLE_NAME = {null, ".t_kline_", null};

    /**
     * RabbitMQ config
     */
    public static final String MQ_QUEUE_MYSQL = "jtrade.queue.mysql";

    public static final String MQ_EXCHANGE_MYSQL = "jtrade.exchange.mysql";

    public static final String MQ_ROUTING_MYSQL = "jtrade.routing.mysql";

    public static final String MQ_QUEUE_STREAM_PUBLIC = "jtrade.queue.stream.public";

    public static final String MQ_EXCHANGE_STREAM_PUBLIC = "jtrade.exchange.stream.public";

    public static final String MQ_QUEUE_STREAM_PRIVATE = "jtrade.queue.stream.private";

    public static final String MQ_EXCHANGE_STREAM_PRIVATE = "jtrade.exchange.stream.private";

}
