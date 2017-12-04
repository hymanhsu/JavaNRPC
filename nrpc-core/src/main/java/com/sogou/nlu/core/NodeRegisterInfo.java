package com.sogou.nlu.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by xuhuahai on 2017/12/1.
 */
public class NodeRegisterInfo {

    // node
    private Node node = new Node();

    // tags
    private Map<String,String> tags = new HashMap<String,String>();

    // services
    private List<String> services = new ArrayList<>();

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "NodeRegisterInfo{" +
                "node=" + node +
                ", tags=" + tags +
                ", services=" + services +
                '}';
    }

}
