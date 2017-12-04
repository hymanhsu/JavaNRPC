package com.sogou.nlu.etcd;

public class EtcdResult {

    // General values
    public String action;
    public EtcdNode node;
    public EtcdNode prevNode;

    // For errors
    public int errorCode;
    public String message;
    public String cause;
    public int index;

    public boolean isError() {
        return errorCode != 0;
    }

}
