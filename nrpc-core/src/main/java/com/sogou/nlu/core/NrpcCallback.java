package com.sogou.nlu.core;

import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;

/**
 * Created by xuhuahai on 2017/11/30.
 */
public class NrpcCallback<T extends Message> implements RpcCallback<T> {

    private T response = null;
    private boolean invoked = false;

    @Override
    public void run(T response) {
        this.response = response;
        invoked = true;
    }

    public T getResponse() {
        return response;
    }

    public boolean isInvoked() {
        return invoked;
    }
}
