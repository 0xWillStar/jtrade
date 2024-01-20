package com.crypto.jtrade.common.util;

import java.nio.charset.StandardCharsets;

import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Trade;

/**
 * Helper methods for jtrade.
 *
 * @author 0xWill
 */
public final class Utils {

    /**
     * Get string bytes in UTF-8 charset.
     */
    public static byte[] getBytes(final String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * get current microsecond
     */
    public static long currentMicroTime() {
        return System.nanoTime() / 1000;
    }

    /**
     * get current second
     */
    public static long currentSecondTime() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * Get the beginning time of the period in which the current time is located
     */
    public static long getPeriodBeginTime(long currTimeSeconds, long interval) {
        return currTimeSeconds / interval * interval;
    }

    /**
     * Is this the beginning of a new period
     */
    public static boolean isNewPeriod(long currTimeSeconds, long interval) {
        return (currTimeSeconds / interval * interval) == currTimeSeconds;
    }

    /**
     * format string
     */
    public static String format(String[] format, String... args) {
        StringBuilder sb = new StringBuilder(128);
        int i = 0;
        for (String str : format) {
            if (str == null) {
                sb.append(args[i++]);
            } else {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    /**
     * format kline object after fetching from the database
     */
    public static void formatKline(Kline kline, SymbolInfo symbolInfo) {
        kline.setOpenPrice(BigDecimalUtil.getVal(kline.getOpenPrice(), symbolInfo.getPriceAssetScale()));
        kline.setHighPrice(BigDecimalUtil.getVal(kline.getHighPrice(), symbolInfo.getPriceAssetScale()));
        kline.setLowPrice(BigDecimalUtil.getVal(kline.getLowPrice(), symbolInfo.getPriceAssetScale()));
        kline.setClosePrice(BigDecimalUtil.getVal(kline.getClosePrice(), symbolInfo.getPriceAssetScale()));
        kline.setVolume(BigDecimalUtil.getVal(kline.getVolume(), symbolInfo.getQuantityScale()));
        kline.setQuoteVolume(BigDecimalUtil.getVal(kline.getQuoteVolume(), symbolInfo.getPriceAssetScale()));
    }

    /**
     * format trade object after fetching from the database
     */
    public static void formatTrade(Trade trade, SymbolInfo symbolInfo) {
        trade.setPrice(BigDecimalUtil.getVal(trade.getPrice(), symbolInfo.getPriceAssetScale()));
        trade.setQty(BigDecimalUtil.getVal(trade.getQty(), symbolInfo.getQuantityScale()));
        trade.setQuoteQty(BigDecimalUtil.getVal(trade.getQuoteQty(), symbolInfo.getPriceAssetScale()));
        trade.setCloseProfit(BigDecimalUtil.getValEx(trade.getCloseProfit(), symbolInfo.getClearAssetScale()));
        trade.setFee(BigDecimalUtil.getValEx(trade.getFee(), symbolInfo.getClearAssetScale()));
        trade
            .setLiquidationPrice(BigDecimalUtil.getValEx(trade.getLiquidationPrice(), symbolInfo.getPriceAssetScale()));
    }

    /**
     * format order object after fetching from the database
     */
    public static void formatOrder(Order order, SymbolInfo symbolInfo) {
        order.setPrice(BigDecimalUtil.getValEx(order.getPrice(), symbolInfo.getPriceAssetScale()));
        order.setQuantity(BigDecimalUtil.getVal(order.getQuantity(), symbolInfo.getQuantityScale()));
        order.setStopPrice(BigDecimalUtil.getValEx(order.getStopPrice(), symbolInfo.getPriceAssetScale()));
        order.setActivationPrice(BigDecimalUtil.getValEx(order.getActivationPrice(), symbolInfo.getPriceAssetScale()));
        order.setCallbackRate(BigDecimalUtil.getValEx(order.getCallbackRate(), 2));
        order.setFrozenFee(BigDecimalUtil.getVal(order.getFrozenFee(), symbolInfo.getClearAssetScale()));
        order.setFrozenMargin(BigDecimalUtil.getVal(order.getFrozenMargin(), symbolInfo.getClearAssetScale()));
        order.setCumQuote(BigDecimalUtil.getVal(order.getCumQuote(), symbolInfo.getPriceAssetScale()));
        order.setExecutedQty(BigDecimalUtil.getVal(order.getExecutedQty(), symbolInfo.getQuantityScale()));
        order.setAvgPrice(BigDecimalUtil.getVal(order.getAvgPrice(), symbolInfo.getPriceAssetScale()));

        // FIXME: The fee asset may not be the same as the clear asset, so the scale should be determined according to
        // the asset of the fee.
        order.setFee(BigDecimalUtil.getVal(order.getFee(), symbolInfo.getClearAssetScale()));
        order.setCloseProfit(BigDecimalUtil.getVal(order.getCloseProfit(), symbolInfo.getClearAssetScale()));
        order.setLeverage(BigDecimalUtil.getValEx(order.getLeverage(), 0));
        order.setLeftQty(BigDecimalUtil.getVal(order.getLeftQty(), symbolInfo.getQuantityScale()));
    }

    /**
     * format bill object after fetching from the database
     */
    public static void formatBill(Bill bill) {
        // FIXME: The scale should be determined according to the asset.
        bill.setAmount(BigDecimalUtil.getVal(bill.getAmount(), 2));
    }

}
