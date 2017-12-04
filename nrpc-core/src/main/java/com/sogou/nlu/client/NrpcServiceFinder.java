package com.sogou.nlu.client;


import com.alibaba.fastjson.JSON;
import com.google.protobuf.BlockingService;
import com.sogou.nlu.core.Node;
import com.sogou.nlu.core.NodeRegisterInfo;
import com.sogou.nlu.etcd.EtcdClient;
import com.sogou.nlu.etcd.EtcdClientException;
import com.sogou.nlu.etcd.EtcdNode;
import com.sogou.nlu.server.BaseService;
import com.sogou.nlu.util.Base64Helper;
import com.sogou.nlu.util.Constants;
import com.sogou.nlu.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by xuhuahai on 2017/11/30.
 */
public class NrpcServiceFinder {

    private static final Logger logger = LoggerFactory.getLogger(NrpcServiceFinder.class);

    private String serviceFullName;

    private String etcdIp;
    private int etcdPort = 2379;

    private Map<String,String> serviceTags;

    public NrpcServiceFinder(String serviceFullName, String nodeTags, String etcdIp, int etcdPort){
        this.serviceFullName = serviceFullName;
        this.etcdIp = etcdIp;
        this.etcdPort = etcdPort;
        this.serviceTags = TagUtils.parseTag(nodeTags);
    }

    private boolean matchTags(Map<String,String> nodeTags){
        if(serviceTags.isEmpty() && nodeTags.isEmpty()){
            return true;
        }
        if(!serviceTags.isEmpty() && nodeTags.isEmpty()){
            return false;
        }
        if(serviceTags.isEmpty() && !nodeTags.isEmpty()){
            return true;
        }
        Iterator<Map.Entry<String, String>> entries = serviceTags.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            String key = entry.getKey();
            String value = entry.getValue();
            String value2 = nodeTags.get(key);
            if(value2==null || !value2.equals(value)){
                return false;
            }
        }
        return true;
    }

    private boolean matchServiceName(List<String> servicesList){
        if(servicesList == null || servicesList.isEmpty()){
            return false;
        }
        for(String item : servicesList){
            if(item.equals(serviceFullName)){
                return true;
            }
        }
        return false;
    }

    public void parseEtcdResult(List<EtcdNode> list, List<Node> result) throws UnsupportedEncodingException{
        if(list!=null){
            for(EtcdNode node : list){
                if(node.dir){
                    parseEtcdResult(node.nodes, result);
                }else{
                    String key = node.key;
                    if(!key.startsWith(Constants.PROVIDER_PATH_PREFIX)){
                        continue;
                    }
                    String content = key.substring(Constants.PROVIDER_PATH_PREFIX.length());
                    String decoded = Base64Helper.decode(content,"UTF-8");
                    NodeRegisterInfo nodeRegisterInfo = JSON.parseObject(decoded, NodeRegisterInfo.class);
                    if(nodeRegisterInfo == null){
                        continue;
                    }
                    if(!matchTags(nodeRegisterInfo.getTags())){
                        continue;
                    }
                    if(!matchServiceName(nodeRegisterInfo.getServices())){
                        continue;
                    }
                    result.add(nodeRegisterInfo.getNode());
                }
            }
        }
    }

    /**
     * 返回可用的节点
     * @return
     */
    public List<Node> getEndpoints(){
        List<Node> result = new ArrayList<Node>();
        EtcdClient client = new EtcdClient(etcdIp, etcdPort);
        try{
            List<EtcdNode> list = client.listDir("/",true);
            parseEtcdResult(list,result);
        } catch (EtcdClientException ex) {
            logger.error(ex.getMessage(), ex);
        }catch (UnsupportedEncodingException ex){
            logger.error(ex.getMessage(), ex);
        } finally {
            try{
                client.close();
            }catch(IOException ex){
            }
        }
        return result;
    }

}
