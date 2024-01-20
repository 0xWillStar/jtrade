package com.crypto.jtrade.core.provider.model.convert;

import org.mapstruct.control.MappingControl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@MappingControl( MappingControl.Use.DIRECT )
public @interface DirectControl {

}
