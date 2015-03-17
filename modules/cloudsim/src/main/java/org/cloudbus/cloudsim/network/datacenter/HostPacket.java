/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.text.DecimalFormat;

/**
 * HostPacket represents the packet that travels through the virtual network with a Host. It
 * contains information about cloudlets which are communicating
 * 
 * Please refer to following publication for more details:
 * 
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 */
public class HostPacket {

	/**
	 *  Big data generation task: Instead of locally created formatter, use the global constant variable for less objects and CPU time.
	 */
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.###");

	public HostPacket(
			int sender,
			int reciever,
			double data,
			double sendtime,
			double recievetime,
			int vsnd,
			int vrvd) {
		super();
		this.sender = sender;
		this.reciever = reciever;
		this.data = data;
		this.sendtime = sendtime;
		this.recievetime = recievetime;
		virtualrecvid = vrvd;
		virtualsendid = vsnd;
	}

	int sender;

	int virtualrecvid;

	int virtualsendid;

	int reciever;

	double data;

	double sendtime;

	double recievetime;

	/**
	 * Big data generation task: Replace string concat with StringBuffer to apply cache for better performance.
	 *
	 * @return the logs for packet
	 */
	public String toString() {
		double latency = recievetime - sendtime;
		double throughput = data;
		if (latency != 0) throughput = (data/latency) * 1024;

		StringBuilder strBuffer = new StringBuilder(250);
		strBuffer.append("NetworkPacketStatistics -- ")
				.append(" Sender   [").append(virtualsendid).append(",").append(sender).append("]\t\t")
				.append(" Receiver [").append(virtualrecvid).append(",").append(reciever).append("]\t\t")
				.append(" latency:").append(DECIMAL_FORMAT.format(latency)).append("\t")
				.append(" throughput:").append(DECIMAL_FORMAT.format(throughput)).append("\t");

		return strBuffer.toString();
	}
}
