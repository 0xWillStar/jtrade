package com.crypto.jtrade.front.provider.service.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.front.provider.cache.RedisService;
import com.crypto.jtrade.front.provider.model.ApiKeyInfo;
import com.crypto.jtrade.front.provider.service.ApiKeyService;
import com.crypto.jtrade.front.provider.util.SignatureUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * api key service
 *
 * @author 0xWill
 */
@Service
@Slf4j
public class ApiKeyServiceImpl implements ApiKeyService {

    @Autowired
    private RedisService redisService;

    @Override
    public String checkSignature(String apiKey, String host, String uri, String method, String timestamp,
        Map<String, String> paramMap, String signature) {
        if (StringUtils.isAnyBlank(apiKey, timestamp, signature)) {
            throw new TradeException(TradeError.ARGUMENT_INVALID);
        }
        if (Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp)) < 60_000L) {
            throw new TradeException(TradeError.ARGUMENT_INVALID);
        }
        ApiKeyInfo apiKeyInfo = redisService.getApiKeyInfo(apiKey);

        try {
            String signStr =
                SignatureUtil.sign(apiKey, apiKeyInfo.getApiSecret(), host, uri, method, timestamp, paramMap);
            if (!StringUtils.equalsIgnoreCase(signStr, signature)) {
                throw new TradeException(TradeError.ARGUMENT_INVALID);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new TradeException(TradeError.ARGUMENT_INVALID);
        }
        return apiKeyInfo.getClientId();
    }

}
