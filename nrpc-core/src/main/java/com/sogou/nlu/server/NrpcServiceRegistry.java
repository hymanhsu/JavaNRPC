package com.sogou.nlu.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.Service;
import com.google.protobuf.BlockingService;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import com.sogou.nlu.core.NodeRegisterInfo;
import com.sogou.nlu.etcd.EtcdClient;
import com.sogou.nlu.etcd.EtcdClientException;
import com.sogou.nlu.except.ServiceNotFoundException;
import com.sogou.nlu.except.MethodNotFoundException;
import com.sogou.nlu.except.DecodeRequestException;
import com.sogou.nlu.rpc.BaiduRpcErrno;
import com.sogou.nlu.util.Base64Helper;
import com.sogou.nlu.util.Constants;
import com.sogou.nlu.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NrpcServiceRegistry implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NrpcServiceRegistry.class);

    private final Map<String, Service> serviceMap = new HashMap<String, Service>();

    private final Map<String, BlockingService> blockingServiceMap = new HashMap<String, BlockingService>();
    private final Map<String, Object>          blockingServiceImplMap = new HashMap<String,Object>();

    private boolean isRunning = false;


    private String nodeIp;
    private int nodePort;
    private String nodeTags;

    private String etcdIp;
    private int etcdPort = 2379;

    private int checkIntervalSecs = 5;

    public NrpcServiceRegistry(String nodeIp, int nodePort, String nodeTags, String etcdIp, int etcdPort, int checkIntervalSecs) {
        this.nodeIp = nodeIp;
        this.nodePort = nodePort;
        this.nodeTags = nodeTags;
        this.etcdIp = etcdIp;
        this.etcdPort = etcdPort;
        this.checkIntervalSecs = checkIntervalSecs;
    }

    @Override
    public void run() {
        logger.info("Start checking service !");
        boolean firstTime = true;
        while (isRunning) {
            NodeRegisterInfo nodeRegisterInfo = new NodeRegisterInfo();
            nodeRegisterInfo.getNode().setIp(nodeIp);
            nodeRegisterInfo.getNode().setPort(nodePort);
            if (nodeTags != null && !nodeTags.equals("")) {
                nodeRegisterInfo.getTags().putAll(TagUtils.parseTag(nodeTags));
            }
            Iterator<Map.Entry<String, Object>> entries = blockingServiceImplMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Object> entry = entries.next();
                String serviceFullName = entry.getKey();
                Object serviceImpl = entry.getValue();
                if (serviceImpl instanceof com.sogou.nlu.server.BaseService) {
                    BaseService base = (BaseService) serviceImpl;
                    if (base.checkValid()) {
                        nodeRegisterInfo.getServices().add(serviceFullName);
                    }
                }
            }

            if (nodeRegisterInfo.getServices().size() > 0) {
                String jsonString = JSON.toJSONString(nodeRegisterInfo);
                logger.debug(jsonString);

                String key = null;
                String value = "";
                try {
                    String encoded = Base64Helper.encode(jsonString, "UTF-8");
                    key = Constants.PROVIDER_PATH_PREFIX + encoded;
                } catch (UnsupportedEncodingException ex) {
                    logger.error(ex.getMessage(), ex);
                }

                if (key != null) {
                    int ttl = checkIntervalSecs + 2;
                    EtcdClient client = new EtcdClient(etcdIp, etcdPort);
                    try {
                        if (firstTime) {
                            client.set(key, value, ttl);
                            firstTime = false;
                        } else {
                            client.set(key, value, ttl, true);
                        }
                    } catch (EtcdClientException ex) {
                        logger.error(ex.getMessage(), ex);
                    }finally {
                        try{
                            client.close();
                        }catch(IOException ex){
                        }
                    }
                }
            }

            try {
                Thread.sleep(checkIntervalSecs * 1000);
            } catch (InterruptedException ex) {
            }
        }
        logger.info("Stop checking service !");
    }

    /**
     * 启动服务检查
     */
    public void startCheckService() {
        isRunning = true;
        Thread checker = new Thread(this,"ServiceChecker");
        checker.start();
    }

    public void stopCheckService() {
        isRunning = false;
    }

    /**
     * Register an RPC service implementation
     */
    public void registerService(Service service) {
        serviceMap.put(service.getDescriptorForType().getFullName(), service);
    }

    /**
     * Register an RPC blocking service implementation
     */
    public void registerBlockingService(Object serviceImpl, BlockingService service) {
        logger.info("registerBlockingService {}",service.getDescriptorForType().getFullName());
        blockingServiceMap.put(service.getDescriptorForType().getFullName(), service);
        blockingServiceImplMap.put(service.getDescriptorForType().getFullName(), serviceImpl);
    }

    public Service queryService(String serviceFullName) throws ServiceNotFoundException {
        Service service = serviceMap.get(serviceFullName);
        if (service == null) {
            throw new ServiceNotFoundException("Not foud service : " + serviceFullName);
        }
        return service;
    }

    public BlockingService queryBlockingService(String serviceFullName) throws ServiceNotFoundException {
        BlockingService service = blockingServiceMap.get(serviceFullName);
        if (service == null) {
            throw new ServiceNotFoundException("Not foud service : " + serviceFullName);
        }
        return service;
    }

    /**
     * Get matching method.
     */
    public MethodDescriptor getMethod(String methodName, ServiceDescriptor descriptor) throws MethodNotFoundException {
        MethodDescriptor method = descriptor.findMethodByName(methodName);
        if (method == null) {
            throw new MethodNotFoundException(
                    String.format("Could not find method %s in service %s", methodName, descriptor.getFullName())
            );
        }
        return method;
    }

    /**
     * Get request protobuf for the RPC method.
     */
    public Message getRequestProto(com.google.protobuf.ByteString byteString,
                                   Message requestPrototype) throws DecodeRequestException {
        Message.Builder builder;
        try {
            builder = requestPrototype.newBuilderForType().mergeFrom(byteString);
            if (!builder.isInitialized()) {
                throw new DecodeRequestException("Invalid request proto");
            }
        } catch (InvalidProtocolBufferException e) {
            throw new DecodeRequestException("Invalid request proto");
        }
        return builder.build();
    }


}
