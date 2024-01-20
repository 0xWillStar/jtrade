package com.crypto.jtrade.core.provider.service.rule.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.CommandIdentity;
import com.crypto.jtrade.core.provider.config.CoreConfig;
import com.crypto.jtrade.core.provider.service.rule.TradeRule;
import com.crypto.jtrade.core.provider.service.rule.TradeRuleManager;

import lombok.extern.slf4j.Slf4j;

/**
 * trade rule manager
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class TradeRuleManagerImpl implements TradeRuleManager {

    @Autowired
    private CoreConfig coreConfig;

    @Autowired
    private List<TradeRule> tradeRuleList;

    private Map<CommandIdentity, List<TradeRule>> commandTradeRules = new HashMap<>();

    @PostConstruct
    public void init() {
        if (tradeRuleList != null) {
            tradeRuleList.forEach(tradeRule -> {
                Stream.of(CommandIdentity.values()).forEach(commandIdentity -> {
                    if ((tradeRule.getUsedProductType() & (1 << coreConfig.getProductType().ordinal())) > 0
                        && (tradeRule.getUsedCommand() & (1L << commandIdentity.ordinal())) > 0) {
                        commandTradeRules.computeIfAbsent(commandIdentity, key -> new ArrayList<>()).add(tradeRule);
                    }
                });
            });
            // sort by sequence
            commandTradeRules.values()
                .forEach(tradeRules -> tradeRules.sort(Comparator.comparing(TradeRule::getSequence)));
        }
    }

    /**
     * get rule list by command identity
     */
    @Override
    public List<TradeRule> get(CommandIdentity identity) {
        return commandTradeRules.get(identity);
    }
}
