package com.crypto.jtrade.front.provider.service;

import java.util.Map;

/**
 * api key service
 *
 * @author 0xWill
 **/
public interface ApiKeyService {

    /**
     * check signature
     */
    String checkSignature(String apiKey, String host, String uri, String method, String timestamp,
        Map<String, String> paramMap, String signature);

}
