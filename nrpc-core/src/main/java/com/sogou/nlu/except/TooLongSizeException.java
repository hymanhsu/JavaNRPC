package com.sogou.nlu.except;

import com.sogou.nlu.rpc.BaiduRpcErrno.Errno;

/**
 * Created by xuhuahai on 2017/11/29.
 */
public class TooLongSizeException extends NrpcException {

    public TooLongSizeException(String errorMessage){
        super(Errno.SYS_EMSGSIZE,errorMessage);
    }

}
