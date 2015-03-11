package org.cloudbus.cloudsim.examples.network.datacenter;

import org.apache.log4j.Logger;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * The entry class for simulating the cloud network.
 */
public class NetworkSimulation extends AppCloudlet {

    /**
     * The class for printing out the logs
     */
    private static final Logger LOGGER = Logger.getLogger(NetworkSimulation.class);

    /**
     * The number of VMs
     */
    private static final int NUM_VMS = 5;

    /**
     * The number of hosts
     */
    private static final int NUM_HOSTS = 100;

    /**
     * The number of applications
     */
    private static final int NUM_APPS = 1500;

    /**
     * The number of users
     */
    private static final int NUM_USERS = 1;

    /**
     * indicate whether to trace event
     */
    private static final boolean FLAG_TRACE = false;

    /**
     * The vmlist.
     */
    private static List<NetworkVm> vmlist;

    /**
     * The constructor with given parameters
     */
    public NetworkSimulation(int type, int appID, double deadline, int userId) {
        super(type, appID, deadline, NUM_VMS, userId);
        this.exeTime = getExecTime() / this.numbervm;
    }

    /**
     * Creates main() to run this example.
     *
     * @param args the args
     */
    public static void main(String[] args) {
        NetworkConstants.MAX_NUM_APPS = NUM_APPS;
        NetworkConstants.EdgeSwitchPort = NUM_HOSTS;

        LOGGER.info("Starting NetworkSimulation...");
        LOGGER.info("Number of hosts: " + NetworkConstants.EdgeSwitchPort + ", vms: " + NUM_VMS + ", apps:" + NetworkConstants.MAX_NUM_APPS
                    + ", users: " + NUM_USERS);

        long startTimestamp = System.currentTimeMillis();
        try {
            // Initialize the CloudSim library
            CloudSim.init(NUM_USERS, Calendar.getInstance(), FLAG_TRACE);

            // Second step: Create Datacenters
            // Datacenters are the resource providers in CloudSim. We need at
            // list one of them to run a CloudSim simulation
            NetworkDatacenter datacenter = createDatacenter("Datacenter-1");

            // Third step: Create Broker
            NetDatacenterBroker broker = createBroker();

            // Updated by xiaoleiy: add data center to the list in broker, not binding to the variable linkDC of broker;
            broker.getLinkDCs().add(datacenter);

            //NetworkSimulation networkSimulation = new NetworkSimulation(0,24,1000.00,NUM_VMS);
            // Fifth step: Create one Cloudlet
            //networkSimulation.createCloudletList(VM_IDS);
            //vmlist = new ArrayList<NetworkVm>();
            // submit vm list to the broker
            //broker.submitVmList(vmlist);


            // Sixth step: Starts the simulation
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            //printCloudletList(newList);
            //printCloudletNWStats(newList);

            LOGGER.info("Number of cloudlets: " + newList.size() +
                    ", Cached " + NetDatacenterBroker.cachedcloudlet +
                    ", Data transfered " + NetworkConstants.totaldatatransfer);

        } catch (Exception e) {
            LOGGER.error("Failed to simulate the network with following exception: " + e);
        }

        long endTimestamp = System.currentTimeMillis();
        LOGGER.info("Finished the network simulation within " + (endTimestamp - startTimestamp) / 1000 + "s.");
    }

    /**
     * Creates the datacenter.
     *
     * @param name the name
     * @return the datacenter
     */
    private static NetworkDatacenter createDatacenter(String name) {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        // our machine

        List<NetworkHost> hostList = new ArrayList<NetworkHost>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        // List<Pe> peList = new ArrayList<Pe>();

        int mips = 1;

        // 3. Create PEs and add these into a list.
        // peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to
        // store Pe id and MIPS Rating

        // 4. Create Host with its id and list of PEs and add them to the list
        // of machines
        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;
        for (int i = 0; i < NetworkConstants.EdgeSwitchPort * NetworkConstants.AggSwitchPort
                * NetworkConstants.RootSwitchPort; i++) {
            // 2. A Machine contains one or more PEs or CPUs/Cores.
            // In this example, it will have only one core.
            // 3. Create PEs and add these into an object of PowerPeList.
            List<Pe> peList = new ArrayList<Pe>();
            peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to
            // store
            // PowerPe
            // id and
            // MIPS
            // Rating
            peList.add(new Pe(1, new PeProvisionerSimple(mips))); // need to
            // store
            // PowerPe
            // id and
            // MIPS
            // Rating
            peList.add(new Pe(2, new PeProvisionerSimple(mips))); // need to
            // store
            // PowerPe
            // id and
            // MIPS
            // Rating
            peList.add(new Pe(3, new PeProvisionerSimple(mips))); // need to
            // store
            // PowerPe
            // id and
            // MIPS
            // Rating
            peList.add(new Pe(4, new PeProvisionerSimple(mips))); // need to
            // store
            // PowerPe
            // id and
            // MIPS
            // Rating
            peList.add(new Pe(5, new PeProvisionerSimple(mips))); // need to
            // store
            // PowerPe
            // id and
            // MIPS
            // Rating
            peList.add(new Pe(6, new PeProvisionerSimple(mips))); // need to
            // store
            // PowerPe
            // id and
            // MIPS
            // Rating
            peList.add(new Pe(7, new PeProvisionerSimple(mips))); // need to
            // store
            // PowerPe
            // id and
            // MIPS
            // Rating

            // 4. Create PowerHost with its id and list of PEs and add them to
            // the list of machines
            hostList.add(new NetworkHost(
                    i,
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
                    peList,
                    new VmSchedulerTimeShared(peList))); // This is our machine
        }

        // 5. Create a DatacenterCharacteristics object that stores the
        // properties of a data center: architecture, OS, list of
        // Machines, allocation policy: time- or space-shared, time zone
        // and its price (G$/Pe time unit).
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
        // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
        // not
        // adding
        // SAN
        // devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch,
                os,
                vmm,
                hostList,
                time_zone,
                cost,
                costPerMem,
                costPerStorage,
                costPerBw);

        // 6. Finally, we need to create a NetworkDatacenter object.
        NetworkDatacenter datacenter = null;
        try {
            datacenter = new NetworkDatacenter(
                    name,
                    characteristics,
                    new NetworkVmAllocationPolicy(hostList),
                    storageList,
                    0);
            LOGGER.info("Created data center: " + name + ", with " + hostList.size() + " hosts, 8 PEs");
        } catch (Exception e) {
            LOGGER.error("Failed to create data center due to exception: ", e);
        }
        // Create Internal Datacenter network
        CreateNetwork(2, datacenter);
        return datacenter;
    }

    // We strongly encourage users to develop their own broker policies, to
    // submit vms and cloudlets according
    // to the specific rules of the simulated scenario

    /**
     * Creates the broker.
     *
     * @return the datacenter broker
     */
    private static NetDatacenterBroker createBroker() {
        NetDatacenterBroker broker = null;
        try {
            broker = new NetDatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects.
     *
     * @param list list of Cloudlets
     * @throws java.io.IOException
     */
    private static void printCloudletList(List<Cloudlet> list) throws IOException {
        int size = list.size();
        Cloudlet cloudlet;
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
                + indent + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + indent + "FileSize");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(cloudlet.getResourceId() + indent
                                + cloudlet.getVmId() + indent + dft.format(cloudlet.getActualCPUTime())
                                + indent + dft.format(cloudlet.getExecStartTime())
                                + indent + dft.format(cloudlet.getFinishTime())
                                + indent + dft.format(cloudlet.getCloudletFileSize())
                                + indent + cloudlet.getUtilizationOfBw(3)

                );

            }
        }

    }
/*
    private static void printCloudletNWStats(List<Cloudlet> list) throws IOException {
        int size = list.size();
        Cloudlet cloudlet;
        String indent = "    ";
        Log.printLine();
        Log.printLine("========== N/W Statistics ==========");

        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Iterator<PacketStats> itr = ((NetworkCloudlet) cloudlet).pktStats.iterator();
            while (itr.hasNext()) {
                PacketStats curr = itr.next();
                Log.printLine(curr.statString());
            }
        }
    }
*/

    @Override
    public void createCloudletList(List<Integer> vmIdList) {
        //On each VM create one cloudlet that sends some data and
        //waits to receive some data from its peer.
        int pesNumber = NetworkConstants.PES_NUMBER;
        long outputSize = NetworkConstants.OUTPUT_SIZE;
        long memory = 1000;
        int executionTime = getExecTime();
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int idx = 0; idx < NUM_VMS; idx++) {
            long dataSize = randInt(1, 1000) * 1024;
            NetworkCloudlet cl = new NetworkCloudlet(NetworkConstants.currentCloudletId, executionTime / vmIdList.size(),
                    pesNumber, dataSize, outputSize, memory,
                    utilizationModel, utilizationModel, utilizationModel);
            NetworkConstants.currentCloudletId++;
            //TODO: Need a decent explanation for this value
            cl.setUserId(0);

            cl.submittime = CloudSim.clock();
            cl.currStagenum = -1;
            cl.setVmId(vmIdList.get(idx));
            for (int i = 0; i < NUM_VMS; i++) {
                //Adding one TASKSTAGE to send data and another to receive
                int stgId = 0;
                for (int jIdx = 0; jIdx < NUM_VMS; jIdx++) {
                    if (idx != jIdx) {
                        cl.stages.add(new TaskStage(NetworkConstants.WAIT_SEND, dataSize, 0, stgId++,
                                memory, vmIdList.get(jIdx), NetworkConstants.currentCloudletId));
                    }
                }
                for (int jIdx = 0; jIdx < NUM_VMS; jIdx++) {
                    if (idx != jIdx) {
                        cl.stages.add(new TaskStage(NetworkConstants.WAIT_RECV, NetworkConstants.COMMUNICATION_LENGTH, 0,
                                stgId++, memory, vmIdList.get(jIdx), cl.getCloudletId() + jIdx));
                    }
                }

            }
            clist.add(cl);
        }
    }

    static void CreateNetwork(int numhost, NetworkDatacenter dc) {

        // Edge Switch
        EdgeSwitch edgeswitch[] = new EdgeSwitch[1];

        for (int i = 0; i < 1; i++) {
            edgeswitch[i] = new EdgeSwitch("Edge" + i, NetworkConstants.EDGE_LEVEL, dc);
            // edgeswitch[i].uplinkswitches.add(null);
            dc.Switchlist.put(edgeswitch[i].getId(), edgeswitch[i]);
            // aggswitch[(int)
            // (i/Constants.AggSwitchPort)].downlinkswitches.add(edgeswitch[i]);
        }

        for (Host hs : dc.getHostList()) {
            NetworkHost netHost = (NetworkHost) hs;
            int switchnum = (int) (hs.getId() / NetworkConstants.EdgeSwitchPort);
            edgeswitch[switchnum].hostlist.put(hs.getId(), netHost);
            dc.HostToSwitchid.put(hs.getId(), edgeswitch[switchnum].getId());

            netHost.bandwidth = NetworkConstants.BandWidthEdgeHost;
            netHost.sw = edgeswitch[switchnum];
            List<NetworkHost> hslist = netHost.sw.fintimelistHost.get(0D);

            if (hslist == null) {
                hslist = new ArrayList<NetworkHost>();
                netHost.sw.fintimelistHost.put(0D, hslist);
            }
            hslist.add(netHost);
        }
    }

    private int getExecTime() {
        //use exec constraints
        return 100;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
