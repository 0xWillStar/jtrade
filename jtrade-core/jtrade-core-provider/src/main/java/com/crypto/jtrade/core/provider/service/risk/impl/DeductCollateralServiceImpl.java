package com.crypto.jtrade.core.provider.service.risk.impl;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.provider.service.cache.ClientEntity;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.risk.DeductCollateralService;
import com.crypto.jtrade.core.provider.service.trade.TradeCommand;

import lombok.extern.slf4j.Slf4j;

/**
 * deduct collateral service, client which no positions and have debts
 *
 * @author 0xWill
 **/
@Service
@Slf4j
@ConditionalOnProperty(value = "jtrade.deduct.enabled")
public class DeductCollateralServiceImpl implements DeductCollateralService {

    @Value("${jtrade.deduct.calculate-interval-seconds:7}")
    private int calculateIntervalSeconds;

    @Value("${jtrade.deduct.delay-intervals:1}")
    private int delayIntervals;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private TradeCommand tradeCommand;

    @PostConstruct
    public void init() {
        initCalculateTimer();
    }

    /**
     * init calculate timer
     */
    private long initCalculateTimer() {
        long currTimeSeconds = Utils.currentSecondTime();
        long delay = (currTimeSeconds / calculateIntervalSeconds + 1 + delayIntervals) * calculateIntervalSeconds
            - currTimeSeconds;
        TimerManager.scheduleAtFixedRate(() -> onTimeCalculate(), delay, calculateIntervalSeconds, TimeUnit.SECONDS);
        return currTimeSeconds;
    }

    /**
     * time on liquidation
     */
    private void onTimeCalculate() {
        try {
            for (String clientId : localCache.getDebtClientIds()) {
                if (checkDeduct(clientId)) {
                    tradeCommand.deductCollateralAssets(clientId);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * check deduct
     */
    private boolean checkDeduct(String clientId) {
        boolean deduct = false;
        ClientEntity clientEntity = localCache.getClientEntity(clientId);
        String clearAsset = localCache.getSystemParameter(SystemParameter.CLEAR_ASSET);
        AssetBalance clearAssetBalance = clientEntity.getBalance(clearAsset);
        if (clearAssetBalance != null && clearAssetBalance.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal debtThreshold = new BigDecimal(localCache.getSystemParameter(SystemParameter.DEBT_THRESHOLD));
            if (clearAssetBalance.getBalance().negate().compareTo(debtThreshold) >= 0
                || clearAssetBalance.getBalance().negate().compareTo(localCache.getClientCollateral(clientId)) >= 0) {
                deduct = true;
            }
        }
        return deduct;
    }

}
