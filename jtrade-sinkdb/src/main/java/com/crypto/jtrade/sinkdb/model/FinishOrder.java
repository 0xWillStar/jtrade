package com.crypto.jtrade.sinkdb.model;

import com.crypto.jtrade.common.annotation.MyType;
import com.crypto.jtrade.common.model.Order;

import lombok.Data;

/**
 * When order is canceled or filled, save order to finish_order.
 *
 * @author 0xWill
 **/
@Data
@MyType(table = "t_finish_order")
public class FinishOrder extends Order {

}
