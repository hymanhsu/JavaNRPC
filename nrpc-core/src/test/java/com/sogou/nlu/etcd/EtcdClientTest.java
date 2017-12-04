package com.sogou.nlu.etcd;

import org.apache.commons.codec.binary.Base64;
import java.net.URI;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by xuhuahai on 2017/12/1.
 */
public class EtcdClientTest {

    private EtcdClient client = new EtcdClient("10.136.41.49",2379);

    @Test
    public void testListDirectory(){
        try{
            List<EtcdNode> list = client.listDir("/providers",true);
            if(list!=null){
                for(EtcdNode node : list){
                    System.out.println(node);
                }
            }
        }catch(EtcdClientException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void testEncode(){
        String message = "abcd";
        byte[] b=message.getBytes();
        Base64 base64=new Base64();
        byte[] b2 = base64.encode(b);
        String s=new String(b2);
        System.out.println(s);

        byte[] b3 = s.getBytes();
        byte[] b4 = base64.decode(b3);
        String s2=new String(b4);
        System.out.println(s2);

        Assert.assertTrue( message.equals(s2) );
    }




}
