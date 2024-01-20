package com.crypto.jtrade.common.model;

import com.crypto.jtrade.common.util.myserialize.MySerializeUtils;
import com.crypto.jtrade.common.util.myserialize.MySqlUtils;
import org.junit.Test;

public class AssetInfoTest {

    @Test
    public void toStringCode() {
        System.out.println(MySerializeUtils.toStringCode(AssetInfo.class));
    }

    @Test
    public void toObjectCode() {
        System.out.println(MySerializeUtils.toObjectCode(AssetInfo.class));
    }

    @Test
    public void toJSONCode() {
        System.out.println(MySerializeUtils.toJSONCode(AssetInfo.class));
    }

    @Test
    public void getInsertSql() {
        System.out.println(MySqlUtils.getInsertSql(AssetInfo.class));
    }

    @Test
    public void getUpdateSql() {
        System.out.println(MySqlUtils.getUpdateSql(AssetInfo.class));
    }

    @Test
    public void getDeleteSql() {
        System.out.println(MySqlUtils.getDeleteSql(AssetInfo.class));
    }

}
