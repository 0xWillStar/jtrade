package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;
import com.crypto.jtrade.common.constants.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Symbol information
 *
 * @author 0xWill
 **/
@Data
@MyType(table = "t_symbol_info")
public class SymbolInfo {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String exchangeId;

    private ProductType productType;

    @MyField(key = true)
    private String symbol;

    private String underlying;

    private PositionType positionType;

    private BigDecimal strikePrice;

    private OptionsType optionsType;

    private BigDecimal volumeMultiple;

    private String priceAsset;

    private String clearAsset;

    private String baseAsset;

    private MarginPriceType marginPriceType;

    private TradePriceMode tradePriceMode;

    private BigDecimal basisPrice;

    private BigDecimal minOrderQuantity;

    private BigDecimal maxOrderQuantity;

    private BigDecimal priceTick;

    private BigDecimal quantityTick;

    private Boolean inverse = false;

    private Long createTime;

    /**
     * greater or equal to orderingTime, status will change to AUCTION_ORDERING, then market makers can place orders
     * which can't match, and ordinary customers can't place orders. The unit is seconds.
     */
    private Long orderingTime;

    /**
     * greater or equal to tradingTime, status will change to CONTINUOUS, then anyone can place orders which can match.
     * The unit is seconds.
     */
    private Long tradingTime;

    /**
     * greater or equal to expireTime, status will change to NOT_ACTIVE, then anyone can't place orders and all
     * positions will be forced to close. The unit is seconds.
     */
    private Long expireTime;

    private SymbolStatus status;

    private BigDecimal maxLeverage;

    private BigDecimal defaultLeverage;

    private BigDecimal maintenanceMarginRate;

    private Boolean independentMatchThread = false;

    private Integer clearAssetScale;

    private Integer priceAssetScale;

    private Integer quantityScale;

    /**
     * impactValue used for calculating fundingRate
     */
    private BigDecimal impactValue;

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        if (getExchangeId() != null) {
            sb.append(getExchangeId());
        }
        sb.append(",");
        if (getProductType() != null) {
            sb.append(getProductType());
        }
        sb.append(",");
        if (getSymbol() != null) {
            sb.append(getSymbol());
        }
        sb.append(",");
        if (getUnderlying() != null) {
            sb.append(getUnderlying());
        }
        sb.append(",");
        if (getPositionType() != null) {
            sb.append(getPositionType());
        }
        sb.append(",");
        if (getStrikePrice() != null) {
            sb.append(getStrikePrice());
        }
        sb.append(",");
        if (getOptionsType() != null) {
            sb.append(getOptionsType());
        }
        sb.append(",");
        if (getVolumeMultiple() != null) {
            sb.append(getVolumeMultiple());
        }
        sb.append(",");
        if (getPriceAsset() != null) {
            sb.append(getPriceAsset());
        }
        sb.append(",");
        if (getClearAsset() != null) {
            sb.append(getClearAsset());
        }
        sb.append(",");
        if (getBaseAsset() != null) {
            sb.append(getBaseAsset());
        }
        sb.append(",");
        if (getMarginPriceType() != null) {
            sb.append(getMarginPriceType());
        }
        sb.append(",");
        if (getTradePriceMode() != null) {
            sb.append(getTradePriceMode());
        }
        sb.append(",");
        if (getBasisPrice() != null) {
            sb.append(getBasisPrice());
        }
        sb.append(",");
        if (getMinOrderQuantity() != null) {
            sb.append(getMinOrderQuantity());
        }
        sb.append(",");
        if (getMaxOrderQuantity() != null) {
            sb.append(getMaxOrderQuantity());
        }
        sb.append(",");
        if (getPriceTick() != null) {
            sb.append(getPriceTick());
        }
        sb.append(",");
        if (getQuantityTick() != null) {
            sb.append(getQuantityTick());
        }
        sb.append(",");
        if (getInverse() != null) {
            sb.append(getInverse());
        }
        sb.append(",");
        if (getCreateTime() != null) {
            sb.append(getCreateTime());
        }
        sb.append(",");
        if (getOrderingTime() != null) {
            sb.append(getOrderingTime());
        }
        sb.append(",");
        if (getTradingTime() != null) {
            sb.append(getTradingTime());
        }
        sb.append(",");
        if (getExpireTime() != null) {
            sb.append(getExpireTime());
        }
        sb.append(",");
        if (getStatus() != null) {
            sb.append(getStatus());
        }
        sb.append(",");
        if (getMaxLeverage() != null) {
            sb.append(getMaxLeverage());
        }
        sb.append(",");
        if (getDefaultLeverage() != null) {
            sb.append(getDefaultLeverage());
        }
        sb.append(",");
        if (getMaintenanceMarginRate() != null) {
            sb.append(getMaintenanceMarginRate());
        }
        sb.append(",");
        if (getIndependentMatchThread() != null) {
            sb.append(getIndependentMatchThread());
        }
        sb.append(",");
        if (getClearAssetScale() != null) {
            sb.append(getClearAssetScale());
        }
        sb.append(",");
        if (getPriceAssetScale() != null) {
            sb.append(getPriceAssetScale());
        }
        sb.append(",");
        if (getQuantityScale() != null) {
            sb.append(getQuantityScale());
        }
        sb.append(",");
        if (getImpactValue() != null) {
            sb.append(getImpactValue());
        }
        return sb.toString();
    }

    public static SymbolInfo toObject(String str) {
        SymbolInfo obj = new SymbolInfo();
        String[] values = StringUtils.splitPreserveAllTokens(str, ',');
        if (!values[0].equals("")) {
            obj.setExchangeId(values[0]);
        }

        if (!values[1].equals("")) {
            obj.setProductType(ProductType.valueOf(values[1]));
        }

        if (!values[2].equals("")) {
            obj.setSymbol(values[2]);
        }

        if (!values[3].equals("")) {
            obj.setUnderlying(values[3]);
        }

        if (!values[4].equals("")) {
            obj.setPositionType(PositionType.valueOf(values[4]));
        }

        if (!values[5].equals("")) {
            obj.setStrikePrice(new BigDecimal(values[5]));
        }

        if (!values[6].equals("")) {
            obj.setOptionsType(OptionsType.valueOf(values[6]));
        }

        if (!values[7].equals("")) {
            obj.setVolumeMultiple(new BigDecimal(values[7]));
        }

        if (!values[8].equals("")) {
            obj.setPriceAsset(values[8]);
        }

        if (!values[9].equals("")) {
            obj.setClearAsset(values[9]);
        }

        if (!values[10].equals("")) {
            obj.setBaseAsset(values[10]);
        }

        if (!values[11].equals("")) {
            obj.setMarginPriceType(MarginPriceType.valueOf(values[11]));
        }

        if (!values[12].equals("")) {
            obj.setTradePriceMode(TradePriceMode.valueOf(values[12]));
        }

        if (!values[13].equals("")) {
            obj.setBasisPrice(new BigDecimal(values[13]));
        }

        if (!values[14].equals("")) {
            obj.setMinOrderQuantity(new BigDecimal(values[14]));
        }

        if (!values[15].equals("")) {
            obj.setMaxOrderQuantity(new BigDecimal(values[15]));
        }

        if (!values[16].equals("")) {
            obj.setPriceTick(new BigDecimal(values[16]));
        }

        if (!values[17].equals("")) {
            obj.setQuantityTick(new BigDecimal(values[17]));
        }

        if (!values[18].equals("")) {
            obj.setInverse(Boolean.valueOf(values[18]));
        }

        if (!values[19].equals("")) {
            obj.setCreateTime(Long.parseLong(values[19]));
        }

        if (!values[20].equals("")) {
            obj.setOrderingTime(Long.parseLong(values[20]));
        }

        if (!values[21].equals("")) {
            obj.setTradingTime(Long.parseLong(values[21]));
        }

        if (!values[22].equals("")) {
            obj.setExpireTime(Long.parseLong(values[22]));
        }

        if (!values[23].equals("")) {
            obj.setStatus(SymbolStatus.valueOf(values[23]));
        }

        if (!values[24].equals("")) {
            obj.setMaxLeverage(new BigDecimal(values[24]));
        }

        if (!values[25].equals("")) {
            obj.setDefaultLeverage(new BigDecimal(values[25]));
        }

        if (!values[26].equals("")) {
            obj.setMaintenanceMarginRate(new BigDecimal(values[26]));
        }

        if (!values[27].equals("")) {
            obj.setIndependentMatchThread(Boolean.valueOf(values[27]));
        }

        if (!values[28].equals("")) {
            obj.setClearAssetScale(Integer.parseInt(values[28]));
        }

        if (!values[29].equals("")) {
            obj.setPriceAssetScale(Integer.parseInt(values[29]));
        }

        if (!values[30].equals("")) {
            obj.setQuantityScale(Integer.parseInt(values[30]));
        }

        if (!values[31].equals("")) {
            obj.setImpactValue(new BigDecimal(values[31]));
        }

        return obj;
    }

}
