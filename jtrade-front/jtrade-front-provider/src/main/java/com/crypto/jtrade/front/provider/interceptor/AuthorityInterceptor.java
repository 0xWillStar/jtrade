package com.crypto.jtrade.front.provider.interceptor;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.alibaba.fastjson.JSON;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.util.ResponseHelper;
import com.crypto.jtrade.front.provider.cache.RedisService;
import com.crypto.jtrade.front.provider.constants.Constants;
import com.crypto.jtrade.front.provider.model.ApiKeyInfo;
import com.crypto.jtrade.front.provider.util.SignatureUtil;

import cn.hutool.core.map.MapUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * authority interceptor
 *
 * @author 0xWill
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {

    @Setter
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String apiKey = request.getHeader(Constants.HEADER_API_KEY);
            String timestamp = request.getHeader(Constants.HEADER_TIMESTAMP);
            String signature = request.getHeader(Constants.HEADER_SIGNATURE);
            if (StringUtils.isBlank(apiKey)) {
                throw new TradeException(TradeError.API_KEY_INVALID);
            }
            try {
                if (Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp)) > 60_000L) {
                    throw new TradeException(TradeError.TIMESTAMP_INVALID);
                }
            } catch (Exception e) {
                throw new TradeException(TradeError.TIMESTAMP_INVALID);
            }
            ApiKeyInfo apiKeyInfo = redisService.getApiKeyInfo(apiKey);
            if (apiKeyInfo == null) {
                throw new TradeException(TradeError.API_KEY_INVALID);
            }

            // get parameters for signature
            final String uri = request.getRequestURI().trim();
            final String host = request.getServerName().trim();
            final int index = StringUtils.lastIndexOf(uri, "/v1");
            final String relativeUri = StringUtils.substring(uri, index);
            if (StringUtils.isAnyBlank(host, relativeUri)) {
                throw new TradeException(TradeError.SIGNATURE_INVALID);
            }
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, String> paraMap = MapUtil.newHashMap();
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                if (entry.getValue() != null && entry.getValue().length > 0) {
                    paraMap.put(entry.getKey(), entry.getValue()[0]);
                }
            }
            final String method = request.getMethod();

            // verify signature
            try {
                String signStr = SignatureUtil.sign(apiKey, apiKeyInfo.getApiSecret(), host, relativeUri, method,
                    timestamp, paraMap);
                if (!StringUtils.equalsIgnoreCase(signStr, signature)) {
                    throw new TradeException(TradeError.SIGNATURE_INVALID);
                }
            } catch (Exception e) {
                throw new TradeException(TradeError.SIGNATURE_INVALID);
            }

            // verify success
            request.setAttribute(Constants.ATTRIBUTE_ACCOUNT, apiKeyInfo.getClientId());
            redisService.refreshApiKey(apiKey);
            return true;

        } catch (TradeException e) {
            try {
                response.setStatus(401);
                response.setHeader("Content-Type", "application/json;charset=utf-8");
                response.getWriter().write(JSON.toJSONString(ResponseHelper.error(e.getCode(), e.getMessage())));
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        } catch (Exception e) {
            try {
                response.setStatus(401);
                response.setHeader("Content-Type", "application/json;charset=utf-8");
                response.getWriter().write(JSON.toJSONString(ResponseHelper.error(400, e.getMessage())));
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return false;
    }

}
