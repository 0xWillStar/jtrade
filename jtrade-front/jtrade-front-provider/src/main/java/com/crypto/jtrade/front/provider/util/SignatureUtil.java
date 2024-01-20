package com.crypto.jtrade.front.provider.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import com.crypto.jtrade.common.exception.TradeException;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * signature util
 *
 * @author 0xWill
 **/
@Slf4j
@UtilityClass
public class SignatureUtil {

    public String PARAM_KEY_SIGNATURE = "Signature";

    public String HASH_ALGORITHM_SHA256 = "HmacSHA256";

    public String PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    /**
     * check login
     */
    public boolean checkLogin(String signature, String address, String timestamp, String host, String signCode) {
        String message =
            "action:\n" + signCode + " Authentication\n" + "onlySignOn:\n" + host + "\n" + "timestamp:\n" + timestamp;

        String prefix = PERSONAL_MESSAGE_PREFIX + message.length();
        byte[] msgHash = Hash.sha3((prefix + message).getBytes());

        byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
        byte v = signatureBytes[64];

        if (v < 27) {
            v += 27;
        }

        Sign.SignatureData sd = new Sign.SignatureData(v, Arrays.copyOfRange(signatureBytes, 0, 32),
            Arrays.copyOfRange(signatureBytes, 32, 64));

        String addressRecovered = null;
        boolean match = false;

        // Iterate for each possible key to recover
        for (int i = 0; i < 4; i++) {
            BigInteger publicKey = Sign.recoverFromSignature((byte)i,
                new ECDSASignature(new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())), msgHash);

            if (publicKey != null) {
                addressRecovered = "0x" + Keys.getAddress(publicKey);
                if (addressRecovered.equalsIgnoreCase(address)) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

    /**
     * sign
     */
    public String sign(String apiKey, String apiSecret, String host, String uri, String method, String timestamp,
        Map<String, String> paramMap) throws NoSuchAlgorithmException, InvalidKeyException {
        assertParam(apiKey, "Illegal apiKey");
        assertParam(apiSecret, "Illegal apiSecret");
        assertParam(host, "Illegal host");
        assertParam(uri, "Illegal uri");

        final StringBuilder sb = new StringBuilder();
        sb.append(method.toUpperCase()).append('\n').append(host.toLowerCase()).append('\n').append(uri).append('\n')
            .append(timestamp).append("\n").append(apiKey).append("\n");
        paramMap.remove(PARAM_KEY_SIGNATURE);
        SortedMap<String, String> map = new TreeMap<>(paramMap);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append('=').append(urlEncode(value)).append('&');
        }
        sb.deleteCharAt(sb.length() - 1);

        Mac hmacSha256 = Mac.getInstance(HASH_ALGORITHM_SHA256);
        SecretKeySpec secKey = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), HASH_ALGORITHM_SHA256);
        hmacSha256.init(secKey);

        String plainStr = sb.toString();
        byte[] signedHash = hmacSha256.doFinal(plainStr.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(signedHash);
    }

    /**
     * Check parameter
     */
    private void assertParam(String param, String message) {
        if (StringUtils.isBlank(param)) {
            throw new TradeException(message);
        }
    }

    /**
     * Use standard URL Encode encoding. Note that spaces are encoded as %20 instead of +, unlike the JDK default.
     */
    private String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 encoding not supported");
        }
    }

}
