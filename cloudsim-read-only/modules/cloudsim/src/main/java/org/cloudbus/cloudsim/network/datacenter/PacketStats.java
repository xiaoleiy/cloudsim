package org.cloudbus.cloudsim.network.datacenter;

import java.text.DecimalFormat;

/**
 * Project: cloudsim-package
 * Package: org.cloudbus.cloudsim.network.datacenter
 * Created by gurram on 2/19/15.
 */
public class PacketStats {
    public int sendvm;
    public int rcvvm;
    public int sendcloudlet;
    public int rcvcloudlet;
    public double pktSize;

    public double latency;
    public double throughput;

    public PacketStats(HostPacket pkt){
        sendvm       = pkt.sender;
        rcvvm        = pkt.reciever;
        sendcloudlet = pkt.virtualsendid;
        rcvcloudlet  = pkt.virtualrecvid;
        pktSize      = pkt.data;

        latency = (pkt.recievetime - pkt.sendtime);
        throughput = pkt.data;
        if (latency != 0) throughput = (pkt.data/latency) * 1024;
        //System.out.println("Sendtime: " + pkt.sendtime + " Receivetime:" + pkt.recievetime);
    }

    public String statString(){
        DecimalFormat ddft = new DecimalFormat("###.###");
        String statStr = "NetworkPacketStatistics -- ";
        statStr += " Sender   [" + sendcloudlet + "," + sendvm + "]\t\t";
        statStr += " Receiver [" + rcvcloudlet  + "," + rcvvm  + "]\t\t";
        statStr += " latency:" + ddft.format(latency) + "\t";
        statStr += " throughput:" + ddft.format(throughput) +"\t";
        //statStr += " pkt.data:" + pktSize;
        //statStr += " [size:"       + ddft.format(pktSize/(1024 * 1024)) + "MB]";
        return statStr;
    }
}
