package com.crypto.jtrade.core.provider.service.rule.impl.perpetual;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.BillType;
import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolIndicator;
import com.crypto.jtrade.common.util.BigDecimalUtil;
import com.crypto.jtrade.core.api.model.FundingRateRequest;
import com.crypto.jtrade.core.provider.model.landing.FundingFeeLanding;
import com.crypto.jtrade.core.provider.service.cache.ClientEntity;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.MySqlLanding;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.publish.PrivatePublish;
import com.crypto.jtrade.core.provider.service.rule.impl.AbstractTradeRule;
import com.crypto.jtrade.core.provider.util.ClientLockHelper;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * set funding rate
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class FundingRateTradeRule extends AbstractTradeRule {

    @Getter
    private int sequence = 1;

    @Getter
    private long usedProductType = Constants.USE_PERPETUAL;

    @Getter
    private long usedCommand = Constants.USE_SET_FUNDING_RATE;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private RedisLanding redisLanding;

    @Autowired
    private MySqlLanding mySqlLanding;

    @Autowired
    private PrivatePublish privatePublish;

    /**
     * set funding rate
     */
    @Override
    public void setFundingRate(Long requestId, List<FundingRateRequest> request) {
        /**
         * calculate funding fee for any client with open positions
         */
        int size = localCache.getPositionClientIds().size();
        Map<String, AssetBalance> updatedBalances = Maps.newHashMapWithExpectedSize(size);
        List<Bill> bills = new ArrayList<>(size << 1);
        FundingFeeLanding landing = null;

        Iterator<String> iterator = localCache.getPositionClientIds().iterator();
        ClientLockHelper.lockAll();
        try {
            while (iterator.hasNext()) {
                String clientId = iterator.next();
                ClientEntity clientEntity = localCache.getClientEntity(clientId);
                for (Position position : clientEntity.getPositions().values()) {
                    if (position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
                        AssetBalance balance = clientEntity.getBalance(position.getAsset());
                        // fundingFee = (−1) * fundingRate * (T / 8 hours) * positionAmt * markPrice
                        SymbolIndicator indicator = localCache.getSymbolIndicator(position.getSymbol());
                        BigDecimal fundingRate = indicator.getFundingRate();
                        BigDecimal markPrice = indicator.getMarkPrice();
                        BigDecimal fundingFee =
                            position.getPositionAmt().negate().multiply(fundingRate).multiply(markPrice);
                        fundingFee = BigDecimalUtil.getVal(fundingFee,
                            localCache.getSymbolInfo(position.getSymbol()).getClearAssetScale());

                        // update AssetBalance
                        balance.setBalance(balance.getBalance().add(fundingFee));
                        balance.setMoneyChange(balance.getMoneyChange().add(fundingFee));
                        balance.setUpdateTime(System.currentTimeMillis());
                        // add to the to-be-updated list
                        updatedBalances.put(balance.getRowKey(), balance);

                        // FUNDING_FEE bill
                        Bill bill = Bill.createBill(position.getClientId(), position.getSymbol(), BillType.FUNDING_FEE,
                            balance.getAsset(), fundingFee, null);
                        bills.add(bill);
                    }
                }
            }
            // save to redis
            Collection<AssetBalance> balances = updatedBalances.values();
            if (!CollectionUtils.isEmpty(balances)) {
                landing = new FundingFeeLanding(requestId, balances, bills);
                redisLanding.setFundingFee(landing);
            }
        } finally {
            ClientLockHelper.unlockAll();
        }

        if (landing != null) {
            /**
             * write to mysql
             */
            mySqlLanding.setFundingFee(landing);
            /**
             * private publish
             */
            privatePublish.setFundingFee(landing);
        }
    }

}
