package com.sogou.nlu.except;

import com.sogou.nlu.rpc.BaiduRpcErrno;

/**
 * Created by xuhuahai on 2017/11/29.
 */
public class DecodeRequestException extends NrpcException {

    public DecodeRequestException(String errorMessage){
        super(BaiduRpcErrno.Errno.EREQUEST,errorMessage);
    }

}
