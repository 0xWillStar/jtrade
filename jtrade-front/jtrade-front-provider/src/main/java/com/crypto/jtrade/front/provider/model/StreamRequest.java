package com.crypto.jtrade.front.provider.model;

import java.util.List;

import com.crypto.jtrade.front.provider.constants.StreamOp;

import lombok.Data;

/**
 * stream request
 *
 * @author 0xWill
 **/
@Data
public class StreamRequest {

    private StreamOp op;

    private List<StreamRequestArg> args;

}
