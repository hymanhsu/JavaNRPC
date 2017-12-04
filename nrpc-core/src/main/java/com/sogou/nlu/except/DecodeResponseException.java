package com.sogou.nlu.except;

import com.sogou.nlu.rpc.BaiduRpcErrno;

/**
 * Created by xuhuahai on 2017/11/29.
 */
public class DecodeResponseException extends NrpcException {

    public DecodeResponseException(String errorMessage){
        super(BaiduRpcErrno.Errno.ERESPONSE,errorMessage);
    }

}
