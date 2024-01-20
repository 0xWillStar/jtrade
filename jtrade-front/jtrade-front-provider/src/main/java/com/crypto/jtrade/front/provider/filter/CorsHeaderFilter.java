package com.crypto.jtrade.front.provider.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * cors header filter
 *
 * @author 0xWill
 **/
@Order(1)
@Component
@WebFilter(filterName = "corsFilter", urlPatterns = "/*")
@Slf4j
public class CorsHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        String origin = request.getHeader("origin");
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers",
            "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
