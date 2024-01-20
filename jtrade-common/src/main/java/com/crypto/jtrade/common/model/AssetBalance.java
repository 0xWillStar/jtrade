package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;
import com.crypto.jtrade.common.constants.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * an asset balance
 *
 * @author 0xWill
 **/
@Data
@MyType(table = "t_asset_balance")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetBalance {

    @MyField(json = false)
    @JsonIgnore
    private String exchangeId;

    @MyField(json = false)
    @JsonIgnore
    private String memberId;

    @MyField(key = true)
    @JsonIgnore
    private String clientId;

    @MyField(key = true)
    private String asset;

    /**
     * Only used when there is a settlement.
     */
    private BigDecimal preBalance = BigDecimal.ZERO;

    /**
     * balance = preBalance + deposit - withdraw + closeProfit - fee + moneyChange - isolatedBalance
     */
    private BigDecimal balance = BigDecimal.ZERO;

    private BigDecimal deposit = BigDecimal.ZERO;

    private BigDecimal withdraw = BigDecimal.ZERO;

    /**
     * If dynamic margin, positionMargin = 0
     */
    private BigDecimal positionMargin = BigDecimal.ZERO;

    private BigDecimal closeProfit = BigDecimal.ZERO;

    private BigDecimal fee = BigDecimal.ZERO;

    /**
     * If perpetual, moneyChange is funding fee or deduct collateral.
     */
    private BigDecimal moneyChange = BigDecimal.ZERO;

    /**
     * If position is NET, frozenMargin = 0
     */
    private BigDecimal frozenMargin = BigDecimal.ZERO;

    /**
     * If withdrawing, money can be frozen.
     */
    private BigDecimal frozenMoney = BigDecimal.ZERO;

    /**
     * frozen fee for all open orders
     */
    private BigDecimal frozenFee = BigDecimal.ZERO;

    /**
     * If dynamic margin, available = 0
     */
    private BigDecimal available = BigDecimal.ZERO;

    /**
     * If dynamic margin, withdrawable = 0
     */
    private BigDecimal withdrawable = BigDecimal.ZERO;

    /**
     * Sum of abs(positionAmt) in all symbols
     */
    @MyField(json = false)
    @JsonIgnore
    private BigDecimal positionAmt = BigDecimal.ZERO;

    private Long updateTime;

    /**
     * Key of the AssetBalance: clientId_asset
     */
    @MyField(json = false)
    @JsonIgnore
    private String rowKey;

    /**
     * used when margin type is ISOLATED
     */
    private BigDecimal isolatedBalance = BigDecimal.ZERO;

    /**
     * Create an asset balance instance when depositing for the first time
     */
    public static AssetBalance createAssetBalance(String clientId, String asset, BigDecimal deposit) {
        AssetBalance assetBalance = new AssetBalance();
        assetBalance.setClientId(clientId);
        assetBalance.setAsset(asset);
        assetBalance.setBalance(deposit);
        assetBalance.setDeposit(deposit);
        assetBalance.setUpdateTime(System.currentTimeMillis());
        assetBalance
            .setRowKey(new StringBuilder().append(clientId).append(Constants.UNDER_LINE).append(asset).toString());
        return assetBalance;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        if (getExchangeId() != null) {
            sb.append(getExchangeId());
        }
        sb.append(",");
        if (getMemberId() != null) {
            sb.append(getMemberId());
        }
        sb.append(",");
        if (getClientId() != null) {
            sb.append(getClientId());
        }
        sb.append(",");
        if (getAsset() != null) {
            sb.append(getAsset());
        }
        sb.append(",");
        if (getPreBalance() != null) {
            sb.append(getPreBalance());
        }
        sb.append(",");
        if (getBalance() != null) {
            sb.append(getBalance());
        }
        sb.append(",");
        if (getDeposit() != null) {
            sb.append(getDeposit());
        }
        sb.append(",");
        if (getWithdraw() != null) {
            sb.append(getWithdraw());
        }
        sb.append(",");
        if (getPositionMargin() != null) {
            sb.append(getPositionMargin());
        }
        sb.append(",");
        if (getCloseProfit() != null) {
            sb.append(getCloseProfit());
        }
        sb.append(",");
        if (getFee() != null) {
            sb.append(getFee());
        }
        sb.append(",");
        if (getMoneyChange() != null) {
            sb.append(getMoneyChange());
        }
        sb.append(",");
        if (getFrozenMargin() != null) {
            sb.append(getFrozenMargin());
        }
        sb.append(",");
        if (getFrozenMoney() != null) {
            sb.append(getFrozenMoney());
        }
        sb.append(",");
        if (getFrozenFee() != null) {
            sb.append(getFrozenFee());
        }
        sb.append(",");
        if (getAvailable() != null) {
            sb.append(getAvailable());
        }
        sb.append(",");
        if (getWithdrawable() != null) {
            sb.append(getWithdrawable());
        }
        sb.append(",");
        if (getPositionAmt() != null) {
            sb.append(getPositionAmt());
        }
        sb.append(",");
        if (getUpdateTime() != null) {
            sb.append(getUpdateTime());
        }
        sb.append(",");
        if (getRowKey() != null) {
            sb.append(getRowKey());
        }
        sb.append(",");
        if (getIsolatedBalance() != null) {
            sb.append(getIsolatedBalance());
        }
        return sb.toString();
    }

    public static AssetBalance toObject(String str) {
        AssetBalance obj = new AssetBalance();
        String[] values = StringUtils.splitPreserveAllTokens(str, ',');
        if (!values[0].equals("")) {
            obj.setExchangeId(values[0]);
        }

        if (!values[1].equals("")) {
            obj.setMemberId(values[1]);
        }

        if (!values[2].equals("")) {
            obj.setClientId(values[2]);
        }

        if (!values[3].equals("")) {
            obj.setAsset(values[3]);
        }

        if (!values[4].equals("")) {
            obj.setPreBalance(new BigDecimal(values[4]));
        }

        if (!values[5].equals("")) {
            obj.setBalance(new BigDecimal(values[5]));
        }

        if (!values[6].equals("")) {
            obj.setDeposit(new BigDecimal(values[6]));
        }

        if (!values[7].equals("")) {
            obj.setWithdraw(new BigDecimal(values[7]));
        }

        if (!values[8].equals("")) {
            obj.setPositionMargin(new BigDecimal(values[8]));
        }

        if (!values[9].equals("")) {
            obj.setCloseProfit(new BigDecimal(values[9]));
        }

        if (!values[10].equals("")) {
            obj.setFee(new BigDecimal(values[10]));
        }

        if (!values[11].equals("")) {
            obj.setMoneyChange(new BigDecimal(values[11]));
        }

        if (!values[12].equals("")) {
            obj.setFrozenMargin(new BigDecimal(values[12]));
        }

        if (!values[13].equals("")) {
            obj.setFrozenMoney(new BigDecimal(values[13]));
        }

        if (!values[14].equals("")) {
            obj.setFrozenFee(new BigDecimal(values[14]));
        }

        if (!values[15].equals("")) {
            obj.setAvailable(new BigDecimal(values[15]));
        }

        if (!values[16].equals("")) {
            obj.setWithdrawable(new BigDecimal(values[16]));
        }

        if (!values[17].equals("")) {
            obj.setPositionAmt(new BigDecimal(values[17]));
        }

        if (!values[18].equals("")) {
            obj.setUpdateTime(Long.parseLong(values[18]));
        }

        if (!values[19].equals("")) {
            obj.setRowKey(values[19]);
        }

        if (!values[20].equals("")) {
            obj.setIsolatedBalance(new BigDecimal(values[20]));
        }

        return obj;
    }

    public String toJSONString() {
        StringBuilder sb = new StringBuilder(1024);
        boolean first = true;
        sb.append("{");
        if (getClientId() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"clientId\":\"").append(getClientId()).append("\"");
        }

        if (getAsset() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"asset\":\"").append(getAsset()).append("\"");
        }

        if (getPreBalance() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"preBalance\":").append(getPreBalance());
        }

        if (getBalance() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"balance\":").append(getBalance());
        }

        if (getDeposit() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"deposit\":").append(getDeposit());
        }

        if (getWithdraw() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"withdraw\":").append(getWithdraw());
        }

        if (getPositionMargin() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"positionMargin\":").append(getPositionMargin());
        }

        if (getCloseProfit() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"closeProfit\":").append(getCloseProfit());
        }

        if (getFee() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"fee\":").append(getFee());
        }

        if (getMoneyChange() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"moneyChange\":").append(getMoneyChange());
        }

        if (getFrozenMargin() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"frozenMargin\":").append(getFrozenMargin());
        }

        if (getFrozenMoney() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"frozenMoney\":").append(getFrozenMoney());
        }

        if (getFrozenFee() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"frozenFee\":").append(getFrozenFee());
        }

        if (getAvailable() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"available\":").append(getAvailable());
        }

        if (getWithdrawable() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"withdrawable\":").append(getWithdrawable());
        }

        if (getUpdateTime() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"updateTime\":").append(getUpdateTime());
        }

        if (getIsolatedBalance() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"isolatedBalance\":").append(getIsolatedBalance());
        }

        sb.append("}");
        return sb.toString();
    }

}
