package com.sogou.nlu.etcd;

import java.util.List;

public class EtcdNode {

    public String key;
    public long createdIndex;
    public long modifiedIndex;
    public String value;

    // For TTL keys
    public String expiration;
    public int ttl;

    // For listings
    public boolean dir;
    public List<EtcdNode> nodes;

    @Override
    public String toString() {
        return "EtcdNode{" +
                "key='" + key + '\'' +
                ", createdIndex=" + createdIndex +
                ", modifiedIndex=" + modifiedIndex +
                ", value='" + value + '\'' +
                ", expiration='" + expiration + '\'' +
                ", ttl=" + ttl +
                ", dir=" + dir +
                ", nodes=" + nodes +
                '}';
    }

}
