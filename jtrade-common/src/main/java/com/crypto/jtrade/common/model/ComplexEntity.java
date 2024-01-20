package com.crypto.jtrade.common.model;

import java.util.List;

import com.crypto.jtrade.common.annotation.MyField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * composite entity for streaming
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplexEntity {

    @MyField(reference = true)
    private Order order;

    @MyField(reference = true)
    private List<AssetBalance> balanceList;

    @MyField(reference = true)
    private Position position;

    @MyField(reference = true)
    private Trade trade;

    @MyField(reference = true)
    private List<Bill> billList;

    public String toJSONString() {
        StringBuilder sb = new StringBuilder(2048);
        boolean first = true;
        sb.append("{");
        if (getOrder() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"order\":");
            boolean first1 = true;
            sb.append("{");
            if (order.getClientId() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"clientId\":\"").append(order.getClientId()).append("\"");
            }

            if (order.getSymbol() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"symbol\":\"").append(order.getSymbol()).append("\"");
            }

            if (order.getSide() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"side\":\"").append(order.getSide()).append("\"");
            }

            if (order.getPositionSide() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"positionSide\":\"").append(order.getPositionSide()).append("\"");
            }

            if (order.getStatus() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"status\":\"").append(order.getStatus()).append("\"");
            }

            if (order.getPrice() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"price\":").append(order.getPrice());
            }

            if (order.getQuantity() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"quantity\":").append(order.getQuantity());
            }

            if (order.getOrigType() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"origType\":\"").append(order.getOrigType()).append("\"");
            }

            if (order.getType() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"type\":\"").append(order.getType()).append("\"");
            }

            if (order.getTimeInForce() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"timeInForce\":\"").append(order.getTimeInForce()).append("\"");
            }

            if (order.getOrderId() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"orderId\":").append(order.getOrderId());
            }

            if (order.getClientOrderId() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"clientOrderId\":\"").append(order.getClientOrderId()).append("\"");
            }

            if (order.getReduceOnly() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"reduceOnly\":").append(order.getReduceOnly());
            }

            if (order.getWorkingType() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"workingType\":\"").append(order.getWorkingType()).append("\"");
            }

            if (order.getStopPrice() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"stopPrice\":").append(order.getStopPrice());
            }

            if (order.getClosePosition() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"closePosition\":").append(order.getClosePosition());
            }

            if (order.getActivationPrice() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"activationPrice\":").append(order.getActivationPrice());
            }

            if (order.getCallbackRate() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"callbackRate\":").append(order.getCallbackRate());
            }

            if (order.getPriceProtect() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"priceProtect\":").append(order.getPriceProtect());
            }

            if (order.getOrderTime() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"orderTime\":").append(order.getOrderTime());
            }

            if (order.getUpdateTime() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"updateTime\":").append(order.getUpdateTime());
            }

            if (order.getFrozenFee() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"frozenFee\":").append(order.getFrozenFee());
            }

            if (order.getFrozenMargin() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"frozenMargin\":").append(order.getFrozenMargin());
            }

            if (order.getCumQuote() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"cumQuote\":").append(order.getCumQuote());
            }

            if (order.getExecutedQty() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"executedQty\":").append(order.getExecutedQty());
            }

            if (order.getAvgPrice() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"avgPrice\":").append(order.getAvgPrice());
            }

            if (order.getFee() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"fee\":").append(order.getFee());
            }

            if (order.getFeeAsset() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"feeAsset\":\"").append(order.getFeeAsset()).append("\"");
            }

            if (order.getCloseProfit() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"closeProfit\":").append(order.getCloseProfit());
            }

            if (order.getLeverage() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"leverage\":").append(order.getLeverage());
            }

            if (order.getLeftQty() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"leftQty\":").append(order.getLeftQty());
            }

            if (order.getOtoOrderType() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"otoOrderType\":\"").append(order.getOtoOrderType()).append("\"");
            }

            if (order.getSubOrderId1() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"subOrderId1\":").append(order.getSubOrderId1());
            }

            if (order.getSubOrderId2() != null) {
                if (first1) {
                    first1 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"subOrderId2\":").append(order.getSubOrderId2());
            }

            sb.append("}");
        }

        if (getBalanceList() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"balanceList\":[");
            for (int i = 0; i < getBalanceList().size(); i++) {
                AssetBalance item = getBalanceList().get(i);
                boolean first2 = true;
                sb.append("{");
                if (item.getClientId() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"clientId\":\"").append(item.getClientId()).append("\"");
                }

                if (item.getAsset() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"asset\":\"").append(item.getAsset()).append("\"");
                }

                if (item.getPreBalance() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"preBalance\":").append(item.getPreBalance());
                }

                if (item.getBalance() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"balance\":").append(item.getBalance());
                }

                if (item.getDeposit() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"deposit\":").append(item.getDeposit());
                }

                if (item.getWithdraw() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"withdraw\":").append(item.getWithdraw());
                }

                if (item.getPositionMargin() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"positionMargin\":").append(item.getPositionMargin());
                }

                if (item.getCloseProfit() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"closeProfit\":").append(item.getCloseProfit());
                }

                if (item.getFee() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"fee\":").append(item.getFee());
                }

                if (item.getMoneyChange() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"moneyChange\":").append(item.getMoneyChange());
                }

                if (item.getFrozenMargin() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"frozenMargin\":").append(item.getFrozenMargin());
                }

                if (item.getFrozenMoney() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"frozenMoney\":").append(item.getFrozenMoney());
                }

                if (item.getFrozenFee() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"frozenFee\":").append(item.getFrozenFee());
                }

                if (item.getAvailable() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"available\":").append(item.getAvailable());
                }

                if (item.getWithdrawable() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"withdrawable\":").append(item.getWithdrawable());
                }

                if (item.getUpdateTime() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"updateTime\":").append(item.getUpdateTime());
                }

                if (item.getIsolatedBalance() != null) {
                    if (first2) {
                        first2 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"isolatedBalance\":").append(item.getIsolatedBalance());
                }

                sb.append("}");
                if (i < getBalanceList().size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }

        if (getPosition() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"position\":");
            boolean first3 = true;
            sb.append("{");
            if (position.getClientId() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"clientId\":\"").append(position.getClientId()).append("\"");
            }

            if (position.getSymbol() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"symbol\":\"").append(position.getSymbol()).append("\"");
            }

            if (position.getPositionSide() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"positionSide\":\"").append(position.getPositionSide()).append("\"");
            }

            if (position.getMarginType() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"marginType\":\"").append(position.getMarginType()).append("\"");
            }

            if (position.getPositionAmt() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"positionAmt\":").append(position.getPositionAmt());
            }

            if (position.getLongFrozenAmt() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"longFrozenAmt\":").append(position.getLongFrozenAmt());
            }

            if (position.getShortFrozenAmt() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"shortFrozenAmt\":").append(position.getShortFrozenAmt());
            }

            if (position.getOpenPrice() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"openPrice\":").append(position.getOpenPrice());
            }

            if (position.getPositionMargin() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"positionMargin\":").append(position.getPositionMargin());
            }

            if (position.getLongFrozenMargin() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"longFrozenMargin\":").append(position.getLongFrozenMargin());
            }

            if (position.getShortFrozenMargin() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"shortFrozenMargin\":").append(position.getShortFrozenMargin());
            }

            if (position.getLeverage() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"leverage\":").append(position.getLeverage());
            }

            if (position.getAsset() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"asset\":\"").append(position.getAsset()).append("\"");
            }

            if (position.getAutoAddMargin() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"autoAddMargin\":").append(position.getAutoAddMargin());
            }

            if (position.getIsolatedBalance() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"isolatedBalance\":").append(position.getIsolatedBalance());
            }

            if (position.getIsolatedFrozenFee() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"isolatedFrozenFee\":").append(position.getIsolatedFrozenFee());
            }

            if (position.getUpdateTime() != null) {
                if (first3) {
                    first3 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"updateTime\":").append(position.getUpdateTime());
            }

            sb.append("}");
        }

        if (getTrade() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"trade\":");
            boolean first4 = true;
            sb.append("{");
            if (trade.getClientId() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"clientId\":\"").append(trade.getClientId()).append("\"");
            }

            if (trade.getSymbol() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"symbol\":\"").append(trade.getSymbol()).append("\"");
            }

            if (trade.getTradeId() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"tradeId\":").append(trade.getTradeId());
            }

            if (trade.getSide() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"side\":\"").append(trade.getSide()).append("\"");
            }

            if (trade.getPositionSide() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"positionSide\":\"").append(trade.getPositionSide()).append("\"");
            }

            if (trade.getOrderId() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"orderId\":").append(trade.getOrderId());
            }

            if (trade.getClientOrderId() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"clientOrderId\":\"").append(trade.getClientOrderId()).append("\"");
            }

            if (trade.getPrice() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"price\":").append(trade.getPrice());
            }

            if (trade.getQty() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"qty\":").append(trade.getQty());
            }

            if (trade.getQuoteQty() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"quoteQty\":").append(trade.getQuoteQty());
            }

            if (trade.getCloseProfit() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"closeProfit\":").append(trade.getCloseProfit());
            }

            if (trade.getFee() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"fee\":").append(trade.getFee());
            }

            if (trade.getFeeAsset() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"feeAsset\":\"").append(trade.getFeeAsset()).append("\"");
            }

            if (trade.getMatchRole() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"matchRole\":\"").append(trade.getMatchRole()).append("\"");
            }

            if (trade.getTradeTime() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"tradeTime\":").append(trade.getTradeTime());
            }

            if (trade.getTradeType() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"tradeType\":\"").append(trade.getTradeType()).append("\"");
            }

            if (trade.getLiquidationPrice() != null) {
                if (first4) {
                    first4 = false;
                } else {
                    sb.append(",");
                }

                sb.append("\"liquidationPrice\":").append(trade.getLiquidationPrice());
            }

            sb.append("}");
        }

        if (getBillList() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"billList\":[");
            for (int i = 0; i < getBillList().size(); i++) {
                Bill item1 = getBillList().get(i);
                boolean first5 = true;
                sb.append("{");
                if (item1.getClientId() != null) {
                    if (first5) {
                        first5 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"clientId\":\"").append(item1.getClientId()).append("\"");
                }

                if (item1.getSymbol() != null) {
                    if (first5) {
                        first5 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"symbol\":\"").append(item1.getSymbol()).append("\"");
                }

                if (item1.getBillType() != null) {
                    if (first5) {
                        first5 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"billType\":\"").append(item1.getBillType()).append("\"");
                }

                if (item1.getAsset() != null) {
                    if (first5) {
                        first5 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"asset\":\"").append(item1.getAsset()).append("\"");
                }

                if (item1.getAmount() != null) {
                    if (first5) {
                        first5 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"amount\":").append(item1.getAmount());
                }

                if (item1.getInfo() != null) {
                    if (first5) {
                        first5 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"info\":\"").append(item1.getInfo()).append("\"");
                }

                if (item1.getCorrelationId() != null) {
                    if (first5) {
                        first5 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"correlationId\":\"").append(item1.getCorrelationId()).append("\"");
                }

                if (item1.getInsertTime() != null) {
                    if (first5) {
                        first5 = false;
                    } else {
                        sb.append(",");
                    }

                    sb.append("\"insertTime\":").append(item1.getInsertTime());
                }

                sb.append("}");
                if (i < getBillList().size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }

        sb.append("}");
        return sb.toString();
    }

}
