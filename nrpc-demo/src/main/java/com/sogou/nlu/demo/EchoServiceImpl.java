package com.sogou.nlu.demo;


import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import com.sogou.nlu.server.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * Created by xuhuahai on 2017/12/1.
 */
public class EchoServiceImpl implements Echo.EchoService.BlockingInterface, com.sogou.nlu.server.BaseService {

    private static final Logger logger = LoggerFactory.getLogger(EchoServiceImpl.class);

    /**
     *
     * @param controller
     * @param request
     * @return
     * @throws com.google.protobuf.ServiceException
     */
    public com.sogou.nlu.demo.Echo.EchoResponse echo(
            com.google.protobuf.RpcController controller,
            com.sogou.nlu.demo.Echo.EchoRequest request)
            throws com.google.protobuf.ServiceException{

        logger.debug("received : "+request.getMessage());

        com.sogou.nlu.demo.Echo.EchoResponse.Builder echoResponseBuilder = com.sogou.nlu.demo.Echo.EchoResponse.newBuilder();
        echoResponseBuilder.setMessage(request.getMessage());
        com.sogou.nlu.demo.Echo.EchoResponse result =  echoResponseBuilder.build();
        if(!result.isInitialized()){
            throw new ServiceException("response is not initialized");
        }
        return result;
    }


    @Override
    public boolean checkValid() {
        return true;
    }

}
