package com.sogou.nlu.client;


import com.google.protobuf.*;
import com.sogou.nlu.core.Node;
import com.sogou.nlu.except.DecodeException;
import com.sogou.nlu.except.WrongMagicNumException;
import com.sogou.nlu.rpc.BaiduRpcErrno;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.google.protobuf.Descriptors.MethodDescriptor;

import com.sogou.nlu.rpc.NRpc.NrpcMeta;
import com.sogou.nlu.rpc.NRpc.NrpcResponseMeta;
import com.sogou.nlu.rpc.NRpc.NrpcRequestMeta;
import com.sogou.nlu.core.NrpcController;
import com.sogou.nlu.except.ServiceNotFoundException;
import com.sogou.nlu.except.DecodeResponseException;
import com.sogou.nlu.util.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.*;
import java.util.List;
import java.util.Random;
import java.net.UnknownHostException;
import java.net.Socket;
import java.util.concurrent.Executor;


/**
 * NRPC的Protobuf Channel实现
 *
 * @author  Kevin.XU (xuhuahai@sogou-inc.com)
 * @version 1.0.0
 */
public class NrpcChannel implements RpcChannel, BlockingRpcChannel {

    private static final Logger logger = LoggerFactory.getLogger(NrpcChannel.class);

    private NrpcServiceFinder nrpcServiceFinder;
    private Random random = new Random(System.currentTimeMillis());
    private int retryMax = 2;
    private String serviceFullName;
    private SocketFactory socketFactory = SocketFactory.getDefault();

    private Socket socket = null;
    private ByteArrayOutputStream readBuffer = new ByteArrayOutputStream();

    private Executor executor;

    public NrpcChannel(String serviceFullName, String nodeTags, String etcdIp, int etcdPort) {
        this(serviceFullName,nodeTags,etcdIp,etcdPort,1,null);
    }

    public NrpcChannel(String serviceFullName, String nodeTags, String etcdIp, int etcdPort, Executor executor) {
        this(serviceFullName,nodeTags,etcdIp,etcdPort,1,executor);
    }

    public NrpcChannel(String serviceFullName, String nodeTags, String etcdIp, int etcdPort, int retryMax, Executor executor) {
        this.nrpcServiceFinder = new NrpcServiceFinder(serviceFullName, nodeTags, etcdIp, etcdPort);
        this.serviceFullName = serviceFullName;
        this.retryMax = retryMax;
        this.executor = executor;
    }

    public RpcController newController() {
        return new NrpcController();
    }

    public void closeSocket() {
        try {
            this.socket.close();
        } catch (Exception ex) {
        }
        this.socket = null;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public Socket findSocket() throws ServiceNotFoundException, UnknownHostException, IOException {
        if (this.socket != null) {
            return this.socket;
        }
        List<Node> endpoints = nrpcServiceFinder.getEndpoints();
        if (endpoints.size() == 0) {
            throw new ServiceNotFoundException("Not found service from naming : " + serviceFullName);
        }
        int pos = random.nextInt(endpoints.size());
        Node selectedNode = endpoints.get(pos);
        logger.debug("Found endpoint {}:{} of {}", new Object[]{selectedNode.getIp(), selectedNode.getPort(), serviceFullName});
        try {
            this.socket = socketFactory.createSocket(selectedNode.getIp(), selectedNode.getPort());
            //this.socket.setSoTimeout(Constants.READ_TIMEOUT_MILIS);
        } catch (UnknownHostException ex) {
            logger.error(ex.getMessage(), ex);
            closeSocket();
            throw ex;
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            closeSocket();
            throw ex;
        }
        return this.socket;
    }

    public NrpcMeta createRpcRequest(MethodDescriptor method, Message request) {
        NrpcRequestMeta.Builder requestBuilder = NrpcRequestMeta.newBuilder();
        requestBuilder.setServiceName(method.getService().getFullName());
        requestBuilder.setMethodName(method.getName());
        requestBuilder.setLogId(0);
        requestBuilder.setTraceId(0);
        requestBuilder.setSpanId(0);
        requestBuilder.setParentSpanId(0);
        requestBuilder.setRequestBody(request.toByteString());
        NrpcMeta.Builder builder = NrpcMeta.newBuilder();
        builder.setCorrelationId(0);
        builder.setRequest(requestBuilder);
        return builder.build();
    }

    public void sendRpcMessage(Socket socket, NrpcMeta requestMessage) throws IOException {
        readBuffer.reset();  //清空应答的读缓冲
        byte[] bytes = requestMessage.toByteArray();
        int totalSize = Constants.HEADER_SIZE + bytes.length;
        ByteBuf buffer = Unpooled.buffer(totalSize);
        buffer.writeBytes(new byte[]{'N', 'R', 'P', 'C'});
        buffer.writeInt(totalSize);
        buffer.writeBytes(new byte[]{0, 0, 0, 0});
        buffer.writeBytes(bytes);
        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
        dOut.write(buffer.array());
        dOut.flush();
    }

    public byte[] recvRpcMessage(Socket socket) throws IOException, java.net.SocketTimeoutException {
        BufferedInputStream dataInput = new BufferedInputStream(socket.getInputStream());
        do {
            byte[] temp = new byte[Constants.BUFFER_SIZE];
            int n = dataInput.read(temp);
            if(n>0){
                readBuffer.write(temp,0,n);
            }
            if (n < 0 || n < Constants.BUFFER_SIZE) {
                break;
            }
        } while (true);
        byte[] readContent = readBuffer.toByteArray();
        ByteBuf in = Unpooled.buffer(readContent.length);
        in.writeBytes(readContent);
        int readable = in.readableBytes();
        int pos = 0;
        if (readable >= Constants.HEADER_SIZE) {
            // 检查Magic number
            if (in.getByte(pos) == 'N' && in.getByte(pos + 1) == 'R' && in.getByte(pos + 2) == 'P' && in.getByte(pos + 3) == 'C') {
            } else {
                logger.error("Wrong magic number!");
                closeSocket();
                throw new IOException("Wrong magic number!");
            }
            int totalSize = (int)in.getUnsignedInt(pos + 4);
            byte type = in.getByte(pos + 8);
            if (type == 0) {
                logger.debug("one request");
            } else if (type == 1) {
                logger.debug("one response");
            }
            if (readable < totalSize) {
                logger.warn("Not enough data");
                closeSocket();
                throw new IOException("Not enough data!");
            }
            // 开始解码报文
            in.skipBytes(Constants.HEADER_SIZE);
            byte[] dest = new byte[totalSize - Constants.HEADER_SIZE];
            in.readBytes(dest);
            return dest;
        } else {
            logger.warn("Not enough data header");
            closeSocket();
            throw new IOException("Not enough data header!");
        }
    }

    public Message parseResponse(byte[] bytes, Message.Builder builder) throws InvalidProtocolBufferException, DecodeResponseException {
        builder.mergeFrom(bytes);
        Message result = builder.build();
        if (!result.isInitialized()) {
            throw new DecodeResponseException("Decode response error");
        }
        return result;
    }


    @Override
    public Message callBlockingMethod(MethodDescriptor method,
                                      RpcController controller, Message request, Message responsePrototype)
            throws ServiceException {
        NrpcController nrpcController = (NrpcController) controller;

        int retry = retryMax + 1;
        while (retry > 0) {
            retry--;
            controller.reset();
            try {
                Socket sock = findSocket();
                NrpcMeta rpcRequest = createRpcRequest(method, request);
                sendRpcMessage(sock, rpcRequest);
                byte[] reply = recvRpcMessage(sock);
                NrpcMeta.Builder outerBuilder = NrpcMeta.newBuilder();
                parseResponse(reply, outerBuilder);
                NrpcResponseMeta innerMsg = outerBuilder.getResponse();
                if (innerMsg.getErrorCode() != 0) {
                    nrpcController.setFailed(innerMsg.getErrorText(), BaiduRpcErrno.Errno.forNumber(innerMsg.getErrorCode()));
                    return null;
                }
                Message.Builder builder = responsePrototype.newBuilderForType();
                return parseResponse(innerMsg.getResponseBody().toByteArray(), builder);
            } catch (java.net.SocketTimeoutException ex) {
                logger.error(ex.getMessage(), ex);
                closeSocket();
                throw new ServiceException(ex.getMessage());
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                closeSocket();
                throw new ServiceException(ex.getMessage());
            }
        }
        return null;
    }


    @Override
    public void callMethod(MethodDescriptor method, RpcController controller,
                           Message request, final Message responsePrototype,
                           final RpcCallback<Message> done) {

        NrpcController nrpcController = (NrpcController) controller;
        controller.reset();

        try {
            final Socket sock = findSocket();
            NrpcMeta rpcRequest = createRpcRequest(method, request);
            sendRpcMessage(sock, rpcRequest);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            callbackWithNull(done);
            return;
        }

        // Listen for the response using the executor
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] reply = recvRpcMessage(getSocket());
                    NrpcMeta.Builder outerBuilder = NrpcMeta.newBuilder();
                    parseResponse(reply, outerBuilder);
                    NrpcResponseMeta innerMsg = outerBuilder.getResponse();
                    if (innerMsg.getErrorCode() != 0) {
                        nrpcController.setFailed(innerMsg.getErrorText(), BaiduRpcErrno.Errno.forNumber(innerMsg.getErrorCode()));
                        callbackWithNull(done);
                        return;
                    }
                    Message.Builder builder = responsePrototype.newBuilderForType().mergeFrom(innerMsg.getResponseBody());
                    if (!builder.isInitialized()) {
                        nrpcController.setFailed("Response builder initialized failed", BaiduRpcErrno.Errno.ERESPONSE);
                        callbackWithNull(done);
                        return;
                    }
                    Message rpcResponse = builder.build();

                    // Callback if failed or server invoked callback
                    if (nrpcController.failed()) {
                        if (done != null) {
                            done.run(rpcResponse);
                        }
                    }
                } catch (Exception e) {
                    closeSocket();
                    // Call done with null, controller has the error information
                    callbackWithNull(done);
                }
            }
        });

    }

    private static void callbackWithNull(RpcCallback<Message> done) {
        if (done != null) {
            done.run(null);
        }
    }


}
