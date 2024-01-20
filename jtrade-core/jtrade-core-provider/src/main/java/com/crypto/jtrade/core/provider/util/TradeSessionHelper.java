package com.crypto.jtrade.core.provider.util;

import com.crypto.jtrade.core.provider.model.session.TradeSession;

/**
 * session helper, a ThreadLocal session
 *
 * @author 0xWill
 **/
public class TradeSessionHelper {

    private static final ThreadLocal<TradeSession> holderThreadLocal = ThreadLocal.withInitial(TradeSession::new);

    public static TradeSession get() {
        return holderThreadLocal.get();
    }

}
