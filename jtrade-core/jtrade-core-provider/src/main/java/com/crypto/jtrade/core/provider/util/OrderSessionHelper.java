package com.crypto.jtrade.core.provider.util;

import com.crypto.jtrade.core.provider.model.session.OrderSession;

/**
 * session helper, a ThreadLocal session
 *
 * @author 0xWill
 **/
public class OrderSessionHelper {

    private static final ThreadLocal<OrderSession> holderThreadLocal = ThreadLocal.withInitial(OrderSession::new);

    public static OrderSession get() {
        return holderThreadLocal.get();
    }

}
