package com.crypto.jtrade.front.provider.cache.impl;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.ClientSetting;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.front.provider.cache.RedisService;
import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.model.ApiKeyInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * redis service
 *
 * @author 0xWill
 */
@Service
@Slf4j
public class RedisServiceImpl implements RedisService {

    @Autowired
    private FrontConfig frontConfig;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void setApiKeyInfo(ApiKeyInfo apiKeyInfo) {
        String redisKey = Utils.format(Constants.REDIS_KEY_FRONT_API_KEY, apiKeyInfo.getApiKey());
        redisTemplate.opsForValue().set(redisKey, JSON.toJSONString(apiKeyInfo),
            Duration.ofMinutes(frontConfig.getApiKeyTimeoutMinutes()));
    }

    @Override
    public ApiKeyInfo getApiKeyInfo(String apiKey) {
        String redisKey = Utils.format(Constants.REDIS_KEY_FRONT_API_KEY, apiKey);
        String value = redisTemplate.opsForValue().get(redisKey);
        return value == null ? null : JSON.parseObject(value, ApiKeyInfo.class);
    }

    @Override
    public void refreshApiKey(String apiKey) {
        String redisKey = Utils.format(Constants.REDIS_KEY_FRONT_API_KEY, apiKey);
        redisTemplate.expire(redisKey, frontConfig.getApiKeyTimeoutMinutes(), TimeUnit.MINUTES);
    }

    @Override
    public Map<SystemParameter, String> getSystemParameters() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(Constants.REDIS_KEY_SYSTEM_PARAMETER);
        return entries.entrySet().stream().collect(Collectors
            .toMap(entry -> SystemParameter.valueOf((String)entry.getKey()), entry -> (String)entry.getValue()));
    }

    @Override
    public ClientSetting getSettingsByClientId(String clientId, String symbol) {
        String redisKey = Utils.format(Constants.REDIS_KEY_CLIENT_SETTING, clientId);
        Object value = redisTemplate.opsForHash().get(redisKey, symbol);
        return value == null ? null : ClientSetting.toObject((String)value);
    }

    @Override
    public FeeRate getFeeRateByClientId(String clientId) {
        String key = Utils.format(Constants.REDIS_KEY_CLIENT_FEE_RATE, clientId);
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? null : FeeRate.toObject(value);
    }
}
