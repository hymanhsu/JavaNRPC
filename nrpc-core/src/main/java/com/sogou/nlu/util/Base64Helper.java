package com.sogou.nlu.util;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Created by xuhuahai on 2017/12/1.
 */
public class Base64Helper {

    /**
     * 编码
     * @param content
     * @param encoding
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encode(String content,String encoding) throws UnsupportedEncodingException{
        byte[] b = content.getBytes(encoding);
        Base64 base64 = new Base64();
        byte[] b2 = base64.encode(b);
        return new String(b2,"UTF-8");
    }

    /**
     * 解码
     * @param content
     * @param encoding
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String decode(String content,String encoding) throws UnsupportedEncodingException{
        byte[] b3 = content.getBytes("UTF-8");
        Base64 base64 = new Base64();
        byte[] b4 = base64.decode(b3);
        return new String(b4,encoding);
    }

}
