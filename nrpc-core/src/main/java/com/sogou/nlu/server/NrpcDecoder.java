package com.sogou.nlu.server;

import com.sogou.nlu.except.DecodeException;
import com.sogou.nlu.except.WrongMagicNumException;
import com.sogou.nlu.rpc.NRpc.NrpcMeta;
import com.sogou.nlu.util.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Created by xuhuahai on 2017/11/29.
 */
public class NrpcDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(NrpcDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readable = in.readableBytes();
        if (readable > Constants.MAX_FRAME_SIZE) {
            logger.error("Frame too big : {}, max is {}",new Object[]{readable,Constants.MAX_FRAME_SIZE});
            in.skipBytes(readable);
            ctx.close();
            throw new TooLongFrameException("Frame too big!");
        }
        int pos = 0;
        if(readable >= Constants.HEADER_SIZE){
            // 检查Magic number
            if(in.getByte(pos) == 'N' && in.getByte(pos+1) == 'R' && in.getByte(pos+2) == 'P' && in.getByte(pos+3) == 'C'){
            }else{
                logger.error("Wrong magic number!");
                in.skipBytes(readable);
                ctx.close();
                throw new WrongMagicNumException("Wrong magic number!");
            }
            int totalSize = (int)in.getUnsignedInt(pos+4);
            byte type = in.getByte(pos+8);
            if(type == 0){
                logger.debug("one request");
            }else if(type == 1){
                logger.debug("one response");
            }
            if(readable < totalSize ){
                return;
            }
            // 开始解码报文
            try{
                in.skipBytes(Constants.HEADER_SIZE);
                byte[] dest = new byte[totalSize-Constants.HEADER_SIZE];
                in.readBytes(dest);
                ByteArrayInputStream destByteInputStream = new ByteArrayInputStream(dest);
                NrpcMeta nrpcMeta = NrpcMeta.parseFrom(destByteInputStream);
                out.add(nrpcMeta);
            }catch(Exception ex) {
                logger.error(ex.getMessage(), ex);
                ctx.close();
                throw new DecodeException("NrpcMeta decode failed!");
            }
        }
    }

}
