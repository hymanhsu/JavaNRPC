package com.sogou.nlu.server;

/**
 * NRPC服务需要实现的接口
 *
 * @author  Kevin.XU (xuhuahai@sogou-inc.com)
 * @version 1.0.0
 */
public interface BaseService {

    /**
     * 返回服务的可用状态
     * @return
     */
    boolean checkValid();

}
