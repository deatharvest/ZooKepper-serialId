package com.jn.zookeeper;

/**
 * Created by death on 2016/7/12.
 */
public class test{

    public static void main(String[] args){



        SerialIdGeneator th1 = new SerialIdGeneator();
        th1.setThreadId(1);
        th1.start();

        SerialIdGeneator th2 = new SerialIdGeneator();
        th2.setThreadId(2);
        th2.start();

        SerialIdGeneator th3 = new SerialIdGeneator();
        th3.setThreadId(3);
        th3.start();

        SerialIdGeneator th4 = new SerialIdGeneator();
        th4.setThreadId(4);
        th4.start();

        SerialIdGeneator th5 = new SerialIdGeneator();
        th5.setThreadId(5);
        th5.start();
    }


}
