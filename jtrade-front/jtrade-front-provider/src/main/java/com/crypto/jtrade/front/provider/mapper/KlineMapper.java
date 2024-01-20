package com.crypto.jtrade.front.provider.mapper;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.util.Utils;

/**
 * kline mapper
 *
 * @author 0xWill
 **/
@Repository
public interface KlineMapper {

    List<Kline> getKlineList(String symbol, String period, Long startTime, Long endTime, String tableName,
        Integer limit);

    default String getKlineTableName(String symbol, String marketDbSchema, String symbolDelimiter) {
        return Utils.format(Constants.KLINE_TABLE_NAME, marketDbSchema,
            StringUtils.replace(symbol, symbolDelimiter, Constants.UNDER_LINE));
    }

}
