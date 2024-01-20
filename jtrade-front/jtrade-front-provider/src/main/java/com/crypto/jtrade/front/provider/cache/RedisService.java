package com.crypto.jtrade.front.provider.cache;

import java.util.Map;

import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.ClientSetting;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.front.provider.model.ApiKeyInfo;

/**
 * redis service
 *
 * @author 0xWill
 */
public interface RedisService {

    /**
     * set api key information
     * 
     * @param apiKeyInfo ApiKeyInfo
     */
    void setApiKeyInfo(ApiKeyInfo apiKeyInfo);

    /**
     * get api key information
     * 
     * @param apiKey api key
     * @return ApiKeyInfo
     */
    ApiKeyInfo getApiKeyInfo(String apiKey);

    /**
     * refresh api key, extending expiration time
     * 
     * @param apiKey api key
     */
    void refreshApiKey(String apiKey);

    /**
     * get all system parameter from redis
     */
    Map<SystemParameter, String> getSystemParameters();

    /**
     * get setting of the clientId from redis
     */
    ClientSetting getSettingsByClientId(String clientId, String symbol);

    /**
     * get fee rate of the clientId from redis
     */
    FeeRate getFeeRateByClientId(String clientId);

}
