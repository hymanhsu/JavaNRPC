package com.sogou.nlu.demo;

import com.sogou.nlu.server.NrpcServer;

/**
 * Created by xuhuahai on 2017/12/1.
 */
public class Server {

    public void start(String nodeIp,int nodePort,String etcdIp,int etcdPort){
        //线程数量
        int workerNum = 3;
        //节点的tag
        String nodeTags = "stage=beta;version=1.0";
        //服务器对象
        NrpcServer nrpcServer = new NrpcServer(nodeIp,nodePort,nodeTags,etcdIp,etcdPort,workerNum);
        //注册服务实例
        EchoServiceImpl echoService = new EchoServiceImpl();
        nrpcServer.registerBlockingService( echoService, Echo.EchoService.newReflectiveBlockingService(echoService) );
        try{
            //启动服务器
            nrpcServer.start();
            //Thread.sleep(10*1000);
            //nrpcServer.stop();
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
