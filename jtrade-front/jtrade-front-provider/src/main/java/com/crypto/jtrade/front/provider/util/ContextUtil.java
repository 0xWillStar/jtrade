package com.crypto.jtrade.front.provider.util;

import javax.servlet.http.HttpServletRequest;

import com.crypto.jtrade.front.provider.constants.Constants;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * http context util
 *
 * @author 0xWill
 */
@UtilityClass
@Slf4j
public class ContextUtil {

    /**
     * get account from http context
     * 
     * @return account
     */
    public String getAccountByContext() {
        HttpServletRequest request = getCurrentRequestByContext();
        return (String)request.getAttribute(Constants.ATTRIBUTE_ACCOUNT);
    }

    /**
     * get current request
     * 
     * @return HttpServletRequest
     */
    public HttpServletRequest getCurrentRequestByContext() {
        return ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
    }

}
