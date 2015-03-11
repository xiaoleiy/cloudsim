package org.cloudbus.cloudsim.examples.network.datacenter;

import org.apache.log4j.Logger;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.network.datacenter.NetworkPacket;

/**
 * Created by xiaoleiyu on 15-3-12.
 */
public class Util {

    private static final Logger LOGGER = Logger.getLogger(Util.class);

    public static void printPacketEvent(SimEvent event) {

        NetworkPacket packet = (NetworkPacket)event.getData();
        LOGGER.info("event from " + event.getSource() + " to " + event.getDestination() + " with data: " +
                packet.toString());
    }
}
