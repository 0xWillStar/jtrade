package com.crypto.jtrade.front.provider.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.util.ResponseHelper;
import com.crypto.jtrade.front.api.PublicApi;
import com.crypto.jtrade.front.api.model.IndexMarkFundingVO;
import com.crypto.jtrade.front.provider.service.PublicService;

/**
 * public api controller
 *
 * @author 0xWill
 **/
@RestController
public class PublicController implements PublicApi {

    @Autowired
    private PublicService publicService;

    @Override
    public BaseResponse<List<SymbolInfo>> getInstruments(String symbol) {
        return ResponseHelper.success(publicService.getInstruments(symbol));
    }

    @Override
    public BaseResponse<IndexMarkFundingVO> getIndexMarkFunding(String symbol) {
        return ResponseHelper.success(publicService.getIndexMarkFunding(symbol));
    }

    @Override
    public BaseResponse<Long> getTimestamp() {
        return ResponseHelper.success(System.currentTimeMillis());
    }
}
