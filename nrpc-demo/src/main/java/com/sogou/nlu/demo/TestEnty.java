package com.sogou.nlu.demo;

import com.beust.jcommander.JCommander;
import com.sogou.nlu.server.NrpcServer;

/**
 * 服务器：
 * java -jar demo-jar-with-dependencies.jar --mode 1 --nodeIp 127.0.0.1 --nodePort 9000 --etcdIp 10.136.41.49 --etcdPort 2379
 *
 * 客户端：
 * java -jar demo-jar-with-dependencies.jar --mode 0 --etcdIp 10.136.41.49 --etcdPort 2379
 *
 * Created by xuhuahai on 2017/12/1.
 */
public class TestEnty {



    public static void main(String[] args){
        //构造一个参数解析对象
        Args argsObj = new Args();
        JCommander.newBuilder()
                .addObject(argsObj)
                .build()
                .parse(args);

        //区分服务器或客户端两种模式运行
        if(argsObj.mode == 1){
            //server
            Server server = new Server();
            server.start(argsObj.nodeIp,argsObj.nodePort,argsObj.etcdIp,argsObj.etcdPort);
        }else{
            //client
            Client client = new Client();
            client.start(argsObj.etcdIp,argsObj.etcdPort);
        }

    }


}
