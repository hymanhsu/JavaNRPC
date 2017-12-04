package com.sogou.nlu.demo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import com.google.protobuf.RpcController;
import com.sogou.nlu.client.NrpcChannel;
import com.sogou.nlu.core.NrpcCallback;
import com.sogou.nlu.server.NrpcServer;

/**
 * Created by xuhuahai on 2017/12/1.
 */
public class Client {

    public void start(String etcdIp,int etcdPort){
        //服务需要满足的tag
        String serviceTags = "stage=beta;version=1.0";
        //服务名
        String serviceFullName = "sogou.nlu.rpc.example.EchoService";
        //初始化一个Channel
        NrpcChannel nrpcChannel = new NrpcChannel(serviceFullName,serviceTags,etcdIp,etcdPort);
        RpcController controller = nrpcChannel.newController();
        //获取服务Stub
        Echo.EchoService.BlockingInterface service = Echo.EchoService.newBlockingStub(nrpcChannel);

        //构造请求消息
        Echo.EchoRequest.Builder requestBuilder = Echo.EchoRequest.newBuilder();
        requestBuilder.setMessageBytes(ByteString.copyFromUtf8("你好"));
        Echo.EchoRequest request = requestBuilder.build();

        int count = 50;
        while(count-->0){
            try{
                com.sogou.nlu.demo.Echo.EchoResponse response = service.echo(controller,request);
                if(controller.failed()){
                    continue;
                }
                System.out.println(response.getMessage());
            }catch(Exception ex){
                ex.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            }catch(Exception e) {
            }
        }

    }

}
