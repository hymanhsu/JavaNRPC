package com.sogou.nlu.demo;

import com.beust.jcommander.Parameter;

/**
 * Created by xuhuahai on 2017/12/1.
 */
public class Args {

    @Parameter(names = {"--nodeIp"}, description = "Service Server IP")
    public String nodeIp = "10.130.14.125";

    @Parameter(names = {"--nodePort"}, description = "Service Server Port")
    public int nodePort = 8000;

    @Parameter(names = {"--etcdIp"}, description = "Etcd Server IP")
    public String etcdIp = "10.136.41.49";

    @Parameter(names = {"--etcdPort"}, description = "Etcd Server Port")
    public int etcdPort = 2379;

    @Parameter(names = {"--mode"}, description = "Execute Mode, 1 means server, 0 means client")
    public int mode = 1;

}
