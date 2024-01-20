package com.crypto.jtrade.common.util;

import java.math.BigDecimal;

import lombok.experimental.UtilityClass;

/**
 * BigDecimal methods for jtrade.
 *
 * @author 0xWill
 */
@UtilityClass
public class BigDecimalUtil {

    public BigDecimal divide(BigDecimal bd1, BigDecimal bd2) {
        return bd1.divide(bd2, 12, BigDecimal.ROUND_DOWN);
    }

    public BigDecimal divide(BigDecimal bd1, BigDecimal bd2, int scale) {
        return bd1.divide(bd2, scale, BigDecimal.ROUND_DOWN);
    }

    public BigDecimal divideEx(BigDecimal bd1, BigDecimal bd2) {
        if (bd2.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else {
            return bd1.divide(bd2, 12, BigDecimal.ROUND_DOWN);
        }
    }

    public BigDecimal getVal(BigDecimal val, int scale) {
        return val.setScale(scale, BigDecimal.ROUND_DOWN);
    }

    public BigDecimal getValEx(BigDecimal val, int scale, int roundingMode) {
        return val.setScale(scale, roundingMode);
    }

    public BigDecimal getValEx(BigDecimal val, int scale) {
        return val == null || val.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
            : val.setScale(scale, BigDecimal.ROUND_DOWN);
    }

    public BigDecimal max(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2) > 0 ? bd1 : bd2;
    }

    public BigDecimal min(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2) < 0 ? bd1 : bd2;
    }

    public BigDecimal max(BigDecimal bd1, BigDecimal bd2, BigDecimal bd3) {
        BigDecimal value = bd1;
        if (bd2.compareTo(value) > 0) {
            value = bd2;
        }
        if (bd3.compareTo(value) > 0) {
            value = bd3;
        }
        return value;
    }

    public BigDecimal min(BigDecimal bd1, BigDecimal bd2, BigDecimal bd3) {
        BigDecimal value = bd1;
        if (bd2.compareTo(value) < 0) {
            value = bd2;
        }
        if (bd3.compareTo(value) < 0) {
            value = bd3;
        }
        return value;
    }

    /**
     * get clamp
     */
    public static BigDecimal getClamp(BigDecimal value, BigDecimal boundary) {
        BigDecimal min = boundary.negate();
        if (value.compareTo(min) < 0) {
            return min;
        } else if (value.compareTo(boundary) > 0) {
            return boundary;
        } else {
            return value;
        }
    }

}
