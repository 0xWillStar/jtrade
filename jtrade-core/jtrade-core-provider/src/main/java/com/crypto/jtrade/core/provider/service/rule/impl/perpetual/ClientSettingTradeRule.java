package com.crypto.jtrade.core.provider.service.rule.impl.perpetual;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.MarginType;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.ClientSetting;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.core.api.model.ClientSettingRequest;
import com.crypto.jtrade.core.provider.model.landing.ClientSettingLanding;
import com.crypto.jtrade.core.provider.service.cache.ClientEntity;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.MySqlLanding;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.rule.impl.AbstractTradeRule;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * set client setting
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class ClientSettingTradeRule extends AbstractTradeRule {

    @Getter
    private int sequence = 1;

    @Getter
    private long usedProductType = Constants.USE_PERPETUAL;

    @Getter
    private long usedCommand = Constants.USE_SET_CLIENT_SETTING;

    @Autowired
    private RedisLanding redisLanding;

    @Autowired
    private MySqlLanding mySqlLanding;

    @Autowired
    private LocalCacheService localCache;

    /**
     * set client setting
     */
    @Override
    public void setClientSetting(Long requestId, ClientSettingRequest request) {
        ClientEntity clientEntity = localCache.getClientEntity(request.getClientId());
        String insuranceClientId = localCache.getSystemParameter(SystemParameter.INSURANCE_CLIENT_ID);
        if (!request.getClientId().equals(insuranceClientId)) {
            SymbolInfo symbolInfo = localCache.getSymbolInfo(request.getSymbol());
            if (symbolInfo == null) {
                throw new TradeException(TradeError.SYMBOL_NOT_EXIST);
            }

            ClientSetting oldClientSetting = clientEntity.getSetting(request.getSymbol());
            /**
             * check leverage
             */
            if (request.getLeverage() != null) {
                if (request.getLeverage().scale() > 0 || request.getLeverage().compareTo(BigDecimal.ONE) < 0) {
                    throw new TradeException(TradeError.LEVERAGE_INVALID);
                }
                if (request.getLeverage().compareTo(symbolInfo.getMaxLeverage()) > 0) {
                    throw new TradeException(TradeError.EXCEEDED_MAX_LEVERAGE);
                }
            } else {
                request.setLeverage(
                    oldClientSetting == null ? symbolInfo.getDefaultLeverage() : oldClientSetting.getLeverage());
            }
            /**
             * check margin type
             */
            MarginType oldMarginType = oldClientSetting == null ? MarginType.CROSSED : oldClientSetting.getMarginType();
            if (request.getMarginType() != null) {
                if (request.getMarginType() != oldMarginType) {
                    Position position = clientEntity.getPosition(request.getSymbol());
                    if (position != null) {
                        /**
                         * If there is an order or position, it is not allowed to change the margin type.
                         */
                        throw new TradeException(TradeError.FORBID_CHANGE_MARGIN_TYPE);
                    }
                }
            } else {
                request.setMarginType(oldMarginType);
            }
        }

        /**
         * update local cache
         */
        clientEntity.addSetting(request);
        /**
         * write to redis
         */
        ClientSettingLanding landing = new ClientSettingLanding(requestId, request);
        redisLanding.setClientSetting(landing);
        /**
         * write to mysql
         */
        mySqlLanding.setClientSetting(landing);
    }

}
