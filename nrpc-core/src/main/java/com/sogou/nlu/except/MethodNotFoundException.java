package com.sogou.nlu.except;

import com.sogou.nlu.rpc.BaiduRpcErrno;

/**
 * Created by xuhuahai on 2017/11/29.
 */
public class MethodNotFoundException extends NrpcException {

    public MethodNotFoundException(String errorMessage){
        super(BaiduRpcErrno.Errno.ENOMETHOD,errorMessage);
    }

}
