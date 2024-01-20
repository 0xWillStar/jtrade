package com.crypto.jtrade.common.util;

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.annotation.*;

/**
 * The type/method/field etc. to which this annotation is applied is only for unit test. It means that user should not
 * use them in business code except test code.
 *
 * @see NotThreadSafe
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface OnlyForTest {

}
