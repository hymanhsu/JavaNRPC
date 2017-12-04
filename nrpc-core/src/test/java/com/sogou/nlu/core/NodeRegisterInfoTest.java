package com.sogou.nlu.core;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

/**
 * Created by xuhuahai on 2017/12/1.
 */
public class NodeRegisterInfoTest {

    @Test
    public void testEncode(){
        NodeRegisterInfo nodeRegisterInfo = new NodeRegisterInfo();
        nodeRegisterInfo.getNode().setIp("1.1.1.1");
        nodeRegisterInfo.getNode().setPort(6000);
        //nodeRegisterInfo.getTags().put("version","1.1");
        nodeRegisterInfo.getServices().add("com.sogou.NLU");
        String jsonString = JSON.toJSONString(nodeRegisterInfo);
        System.out.println(jsonString);

        NodeRegisterInfo nodeRegisterInfo2 = JSON.parseObject(jsonString, NodeRegisterInfo.class);
        System.out.println(nodeRegisterInfo2);
    }

}
