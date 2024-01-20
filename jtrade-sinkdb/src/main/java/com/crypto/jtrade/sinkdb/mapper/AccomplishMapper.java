package com.crypto.jtrade.sinkdb.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.crypto.jtrade.sinkdb.model.Accomplish;

/**
 * accomplish mapper
 *
 * @author 0xWill
 **/
@Repository
public interface AccomplishMapper {

    List<Accomplish> getAccomplishes();

    void addAccomplish(Accomplish accomplish);

}
