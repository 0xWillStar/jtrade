package com.crypto.jtrade.core.provider.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.provider.util.StatisticsHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * request time filter
 *
 * @author 0xWill
 **/
@Order(1)
@Component
@WebFilter(filterName = "requestTimeFilter", urlPatterns = "/*")
@Slf4j
public class RequestTimeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        long startTime = Utils.currentMicroTime();
        filterChain.doFilter(request, servletResponse);
        if (StatisticsHelper.enabled()) {
            log.info("request: {} execute_time: {}us", request.getRequestURI(), Utils.currentMicroTime() - startTime);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
