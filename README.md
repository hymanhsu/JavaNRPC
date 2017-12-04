# 介绍

Java aNother Remote Process Call Framwork

基于Protobuf3.2版本搭建的RPC框架，自带服务注册与发现功能，具备基本的服务框架的能力。


协议兼容：brpc的nrpc协议方式，python版本的aNotherRpc，Java版本的JavaNRPC


# 用法

## 服务器端
```java
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

```

## 客户端
```java
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
        com.sogou.nlu.demo.Echo.EchoResponse response = service.echo(controller,request);
        if(controller.failed()){
            //DO SOMETHING
        }


```

