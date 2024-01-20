package com.crypto.jtrade.core.provider.service.oto;

import com.crypto.jtrade.common.model.ComplexEntity;

/**
 * OTO service
 *
 * @author 0xWill
 **/
public interface OTOService {

    /**
     * receive OTO event
     */
    void receiveOTOEvent(ComplexEntity complexEntity);

}
