package com.crypto.jtrade.front.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.front.api.model.IndexMarkFundingVO;

/**
 * define public api
 *
 * @author 0xWill
 **/
public interface PublicApi {

    @GetMapping("/v1/public/instruments")
    BaseResponse<List<SymbolInfo>> getInstruments(@RequestParam(value = "symbol", required = false) String symbol);

    @GetMapping("/v1/public/indexMarkFunding")
    BaseResponse<IndexMarkFundingVO> getIndexMarkFunding(@RequestParam String symbol);

    @GetMapping("/v1/public/timestamp")
    BaseResponse<Long> getTimestamp();

}
