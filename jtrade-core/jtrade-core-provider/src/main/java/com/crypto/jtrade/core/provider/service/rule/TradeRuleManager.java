package com.crypto.jtrade.core.provider.service.rule;

import java.util.List;

import com.crypto.jtrade.common.constants.CommandIdentity;

/**
 * trade rule manager
 *
 * @author 0xWill
 **/
public interface TradeRuleManager {

    /**
     * get rule list by command identity
     */
    List<TradeRule> get(CommandIdentity identity);

}
