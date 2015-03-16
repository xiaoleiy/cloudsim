package org.cloudbus.cloudsim.examples.network.datacenter;

import org.apache.log4j.LogManager;
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
    private static final int NUM_HOSTS = 10;

    /**
     * The number of applications
     */
    private static final int NUM_APPS = 100;

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

        /**
         * Big data generation task: read user input as numbers of datacenters, hosts, applications
         */
        int countDCs = 1;
        try {
            countDCs = Integer.parseInt(args[0]);                           // The number of data centers
            NetworkConstants.EdgeSwitchPort = Integer.parseInt(args[1]);    // The number of hosts
            NetworkConstants.MAX_NUM_APPS = Integer.parseInt(args[2]);      // The number of applications
        }
        catch (NumberFormatException e) {
            System.out.println("You provided invalid parameters, please provide as following: ");
            System.out.println("java -jar cloudsim.jar <num_datacenters> <num_hosts> <num_applications> ");
            return;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("You provided invalid parameters, please provide as following: ");
            System.out.println("java -jar cloudsim.jar <num_datacenters> <num_hosts> <num_applications> ");
            return;
        }

        LOGGER.info("Starting NetworkSimulation...");
        LOGGER.info("Number of datacenters: " + countDCs + ", hosts: " + NetworkConstants.EdgeSwitchPort +
                ", vms: " + NUM_VMS +
                ", apps:" + NetworkConstants.MAX_NUM_APPS +
                ", users: " + NUM_USERS);

        long startTimestamp = System.currentTimeMillis();
        try {
            // Initialize the CloudSim library
            CloudSim.init(NUM_USERS, Calendar.getInstance(), FLAG_TRACE);

            // Create multiple brokers, each of which attached a data center.
            List<NetDatacenterBroker> brokers = new ArrayList<NetDatacenterBroker>();
            for (int idxDCs = 0; idxDCs < countDCs; idxDCs++) {

                // Second step: Create Datacenters
                // Datacenters are the resource providers in CloudSim.
                NetworkDatacenter datacenter = createDatacenter("Datacenter-" + idxDCs);

                // Third step: Create Brokers for separte datacenters
                NetDatacenterBroker broker = createBroker("Broker-" + idxDCs);
                broker.getLinkDCs().add(datacenter);
                brokers.add(broker);
            }

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

            // finished simulation, will print out the stat data for reference
            StringBuffer statLogs = new StringBuffer("Data transfered: " + NetworkConstants.totaldatatransfer +
                    ". Number of cloudlets: \n");
            for (int idxDCs = 0; idxDCs < countDCs; idxDCs++) {
                NetDatacenterBroker broker = brokers.get(idxDCs);
                List<Cloudlet> receivedList = broker.getCloudletReceivedList();
                statLogs.append(broker.getName() + ": " + receivedList.size() + "\n");
            }

            LOGGER.info(statLogs);
        } catch (Exception e) {
            LOGGER.error("Failed to simulate the network with following exception: " + e);
        }

        long endTimestamp = System.currentTimeMillis();
        LOGGER.info("Finished the network simulation within " + (endTimestamp - startTimestamp) / 1000 + "s.");
        LogManager.shutdown();
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
        int bw = 1000000;
        for (int i = 0; i < NetworkConstants.EdgeSwitchPort * NetworkConstants.AggSwitchPort
                * NetworkConstants.RootSwitchPort; i++) {
            // 2. A Machine contains one or more PEs or CPUs/Cores.
            // In this example, it will have only one core.
            // 3. Create PEs and add these into an object of PowerPeList.
            // Updated by xiaoleiy: increse size of PEs from 8 to 32;
            List<Pe> peList = new ArrayList<Pe>();
            for (int peIdx = 0; peIdx < 32; peIdx++)
            {
                peList.add(new Pe(peIdx, new PeProvisionerSimple(mips))); // need to
            }

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
            LOGGER.info("Created data center: " + name + ", with " + hostList.size() + " hosts, 32 PEs");
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
     * @param brokerName
     */
    private static NetDatacenterBroker createBroker(String brokerName) {
        NetDatacenterBroker broker = null;
        try {
            broker = new NetDatacenterBroker(brokerName);
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
