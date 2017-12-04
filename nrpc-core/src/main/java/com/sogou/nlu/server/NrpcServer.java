package com.sogou.nlu.server;

import com.google.protobuf.BlockingService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * NRPC服务器主类
 *
 * @author  Kevin.XU (xuhuahai@sogou-inc.com)
 * @version 1.0.0
 */
public class NrpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NrpcServer.class);

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    private int workerNum = 6;

    private int port = 8000;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private NrpcServiceRegistry nrpcServiceRegistry;

    private Thread entryThread;

    public NrpcServer(String nodeIp, int port, String nodeTags, String etcdIp, int etcdPort, int workerNum) {
        this(nodeIp,port,nodeTags,etcdIp,etcdPort,5,workerNum);
    }

    public NrpcServer(String nodeIp, int port, String nodeTags, String etcdIp, int etcdPort, int checkIntervalSecs, int workerNum) {
        this.port = port;
        this.workerNum = workerNum;
        this.nrpcServiceRegistry = new NrpcServiceRegistry(nodeIp,port,nodeTags,etcdIp,etcdPort,checkIntervalSecs);
    }

    public NrpcServiceRegistry getNrpcServiceRegistry(){
        return nrpcServiceRegistry;
    }

    /**
     * 注册Block服务
     * @param serviceImpl       接口的实现实例
     * @param blockingService   接口实例的BlockingService封装
     */
    public void registerBlockingService(Object serviceImpl, BlockingService blockingService) {
        nrpcServiceRegistry.registerBlockingService(serviceImpl,blockingService);
    }

    /**
     * 启动
     *
     * @throws Exception
     */
    public void start() throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(workerNum);
        NrpcServer nrpcServer = this;
        entryThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ServerBootstrap bootstrap = new ServerBootstrap();
                            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                                    .childHandler(new ChannelInitializer<SocketChannel>() {
                                        @Override
                                        public void initChannel(SocketChannel ch)
                                                throws Exception {
                                            // 注册handler
                                            ch.pipeline().addLast(new NrpcDecoder(), new NrpcClientHandler(nrpcServer));
                                        }
                                    })
                                    .option(ChannelOption.SO_BACKLOG, 128)
                                    .option(ChannelOption.SO_REUSEADDR, true)
                                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                                    .childOption(ChannelOption.TCP_NODELAY, true)
                                    .childOption(ChannelOption.SO_RCVBUF, 1048576)
                                    .childOption(ChannelOption.SO_SNDBUF, 1048576);
                            // Bind and start to accept incoming connections.
                            ChannelFuture future = bootstrap.bind("0.0.0.0",port).sync();
                            // begin to check service
                            nrpcServiceRegistry.startCheckService();
                            // Wait until the server socket is closed.
                            // In this example, this does not happen, but you can do that to gracefully
                            // shut down your server.
                            future.channel().closeFuture().sync();
                        } catch (Exception ex){
                            logger.error(ex.getMessage(),ex);
                        } finally {
                            workerGroup.shutdownGracefully();
                            bossGroup.shutdownGracefully();
                        }
                    }
                },"NrpcServer-"+atomicInteger.addAndGet(1)
        );
        entryThread.start();
    }

    /**
     * 停止
     */
    public void stop(){
        nrpcServiceRegistry.stopCheckService();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
