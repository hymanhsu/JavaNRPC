package com.sogou.nlu.server;

import com.google.protobuf.Message;
import com.sogou.nlu.core.NrpcController;
import com.sogou.nlu.except.DecodeRequestException;
import com.sogou.nlu.except.MethodNotFoundException;
import com.sogou.nlu.except.ServiceNotFoundException;
import com.sogou.nlu.rpc.NRpc.NrpcMeta;
import com.sogou.nlu.rpc.NRpc.NrpcResponseMeta;
import com.sogou.nlu.rpc.BaiduRpcErrno.Errno;
import com.sogou.nlu.util.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.google.protobuf.BlockingService;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.ServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuhuahai on 2017/11/29.
 */
public class NrpcClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NrpcClientHandler.class);

    private NrpcServer nrpcServer;

    public NrpcClientHandler(NrpcServer nrpcServer){
        this.nrpcServer = nrpcServer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        NrpcMeta nrpcMeta = (NrpcMeta) msg;
        // 处理请求报文
        long correlationId = nrpcMeta.getCorrelationId();
        String serviceFullName = nrpcMeta.getRequest().getServiceName();
        String methodName = nrpcMeta.getRequest().getMethodName();
        NrpcController nrpcController = new NrpcController();
        Message responseProto = null;
        try{
            BlockingService blockingService = nrpcServer.getNrpcServiceRegistry().queryBlockingService(serviceFullName);
            MethodDescriptor methodDescriptor = nrpcServer.getNrpcServiceRegistry().getMethod(methodName,blockingService.getDescriptorForType());
            Message requestProtoType = blockingService.getRequestPrototype(methodDescriptor);
            Message requestProto =  nrpcServer.getNrpcServiceRegistry().getRequestProto(nrpcMeta.getRequest().getRequestBody(),requestProtoType);
            responseProto = blockingService.callBlockingMethod(methodDescriptor,nrpcController, requestProto);
        }catch(ServiceNotFoundException ex){
            logger.warn(ex.getMessage(),ex);
        }catch(MethodNotFoundException ex){
            logger.warn(ex.getMessage(),ex);
        }catch(DecodeRequestException ex){
            logger.error(ex.getMessage(),ex);
            ctx.close();
        }catch (ServiceException ex) {
            logger.error(ex.getMessage(),ex);
            ctx.close();
        } catch (RuntimeException ex) {
            logger.error(ex.getMessage(),ex);
            ctx.close();
        }
        //构造应答
        NrpcMeta.Builder nrpcMetaBuilder = NrpcMeta.newBuilder();
        NrpcResponseMeta.Builder responseBuilder = NrpcResponseMeta.newBuilder();
        if(responseProto != null && !nrpcController.failed()){
            responseBuilder.setErrorCode(0);
            responseBuilder.setErrorText("");
            responseBuilder.setResponseBody(responseProto.toByteString());
        }else{
            if(nrpcController.failed()){
                responseBuilder.setErrorCode(nrpcController.errorCode().getNumber());
                responseBuilder.setErrorText(nrpcController.errorText());
            }else{
                responseBuilder.setErrorCode(Errno.EINTERNAL.getNumber());
                responseBuilder.setErrorText("Unknown internal error");
            }
        }
        nrpcMetaBuilder.setCorrelationId(correlationId);
        nrpcMetaBuilder.setResponse(responseBuilder);
        NrpcMeta nrpcMetaResp = nrpcMetaBuilder.build();
        //封装应答报文(包括报文头)
        byte[] respBytes = nrpcMetaResp.toByteArray();
        int totalSize = Constants.HEADER_SIZE + respBytes.length;
        ByteBuf buffer = ctx.alloc().buffer(totalSize);
        buffer.writeBytes(new byte[]{'N','R','P','C'});
        buffer.writeInt(totalSize);
        buffer.writeBytes(new byte[]{1,0,0,0});
        buffer.writeBytes(respBytes);
        ctx.writeAndFlush(buffer);
    }

}
