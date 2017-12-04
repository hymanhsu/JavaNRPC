package com.sogou.nlu.except;

import com.sogou.nlu.rpc.BaiduRpcErrno.Errno;

/**
 * Created by xuhuahai on 2017/11/29.
 */
public class NrpcException extends Exception {

    private Errno errorCode;

    private String errorMessage;

    public NrpcException(Errno errorCode, String errorMessage){
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
