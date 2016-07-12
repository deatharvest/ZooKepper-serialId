package com.jn.zookeeper;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * @author lawrence
 * @Description: 顺序递增序列号生成器
 * @date 2015-7-14 下午3:12:09
 */

public class SerialIdGeneator extends Thread{


    private static CuratorFramework curatorFramework = getCuratorInstance();

    private int threadId = 0;

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    private static SerialIdGeneator INSTANCE = new SerialIdGeneator();

    public static SerialIdGeneator getInstance() {

        return INSTANCE;

    }
    // zookeeper集群列表
    private static String zookeeperServerList = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";

    /**
     * Curtor 是Zookeeper的一个开源客户端，封装了开发人员不需要关注的底层细节，
     * <p/>
     * 并提供了一套基于Fluent风格的API
     *
     * @return CuratorFramework
     * @throws
     */

    private static CuratorFramework getCuratorInstance() {
        zookeeperServerList = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
        CuratorFramework curatorFramework = null;
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000,
                3);// Zookeeper连接重试策略
        curatorFramework = CuratorFrameworkFactory.newClient(
                zookeeperServerList, retryPolicy);
        curatorFramework.start();
        return curatorFramework;
    }


    /**
     * 获取顺序递增会话ID
     *
     * @return String
     * @throws
     * @param curatorFramework
     */

    public String generateSerialSessionId(CuratorFramework curatorFramework) {

        String sessionId = null;

        try {
            String zkSerialIdPrefixPath = "/serial_service/session";// Zookeepr节点路径前缀
            String zkSerialIdPath = zkSerialIdPrefixPath;
            // Fluent风格，创建持久顺序节点，创建的节点绝对路径为/serial_service/session/yyyyMMdd+单调递增数字
            String serialNodeCreated = curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(zkSerialIdPath);
            sessionId = serialNodeCreated.replace(zkSerialIdPrefixPath, "");// 把前缀去掉
            if (StringUtils.isBlank(sessionId)) {
                throw new Exception("Illegal serial number:" + sessionId);
            }
            // zookeeper上节点太多会影响性能，因此无用节点尽量删除。
            curatorFramework.delete().inBackground().forPath(serialNodeCreated);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sessionId;
    }

    /**
     * @return void
     * @throws
     */

    @Override
    public void run() {

        String simpleCurrentDateStr = new SimpleDateFormat("yyyyMM")
                .format(new Date());
        long num = 0;
        long totalTime =0;


        for (int i = 0; i < 10000; i++) {
            long startTime = System.currentTimeMillis();
            int length = 5;
            String serialId = SerialIdGeneator.getInstance().generateSerialSessionId(curatorFramework);
            serialId = serialId.substring(serialId.length() - length, serialId.length());
            String Id = null;
            Id = "SLDL" + simpleCurrentDateStr + "KH" + serialId + "YW";
            long endTime = System.currentTimeMillis();

            long costTime = endTime - startTime;
            if(costTime>100){
                costTime = 4;
            }
            System.out.println(
                    String.format("ThreadId:%d,serialId:%s, timecost:%d",threadId,Id,costTime ));
            totalTime += endTime - startTime;
            num++;
        }

        System.out.println(String.format("ThreadId:%d,Id总量:%d, 总耗时:%d,平均耗时：%d",threadId,num, totalTime,totalTime/num));


        try {
            Thread.sleep(10 * 1000);// sleep一下，不然有些异步delete回调方法得以执行。
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
