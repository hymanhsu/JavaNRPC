package com.sogou.nlu.except;

import com.sogou.nlu.rpc.BaiduRpcErrno;

/**
 * Created by xuhuahai on 2017/11/29.
 */
public class ServiceNotFoundException extends NrpcException {

    public ServiceNotFoundException(String errorMessage){
        super(BaiduRpcErrno.Errno.ENOSERVICE,errorMessage);
    }

}
