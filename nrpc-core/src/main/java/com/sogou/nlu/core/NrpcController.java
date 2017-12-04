package com.sogou.nlu.core;

import com.sogou.nlu.rpc.BaiduRpcErrno;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

public class NrpcController implements RpcController {

    private boolean failed = false;
    private BaiduRpcErrno.Errno errorCode;
    private String errorMessage;

    @Override
    public void reset() {
        failed = false;
        errorMessage = null;
        errorCode = null;
    }

    @Override
    public boolean failed() {
        return failed;
    }

    @Override
    public String errorText() {
        return errorMessage;
    }

    public BaiduRpcErrno.Errno errorCode() {
        return errorCode;
    }

    @Override
    public void startCancel() {
        // Not yet supported
        throw new UnsupportedOperationException(
                "Cannot cancel request in Socket RPC");
    }

    @Override
    public void setFailed(String reason) {
        failed = true;
        errorMessage = reason;
    }

    public void setFailed(String error, BaiduRpcErrno.Errno errorReason) {
        setFailed(error);
        errorCode = errorReason;
    }

    @Override
    public boolean isCanceled() {
        // Not yet supported
        throw new UnsupportedOperationException(
                "Cannot cancel request in Socket RPC");
    }

    @Override
    public void notifyOnCancel(RpcCallback<Object> callback) {
        // Not yet supported
        throw new UnsupportedOperationException(
                "Cannot cancel request in Socket RPC");
    }

    @Override
    public String toString() {
        return new StringBuffer("NrpcController:")
                .append("\nFailed: " + failed)
                .append("\nError: " + errorMessage)
                .append("\nReason: " + errorCode).toString();
    }

}
