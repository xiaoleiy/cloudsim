/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.distributions.UniformDistr;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * NetDatacentreBroker represents a broker acting on behalf of Datacenter provider. It hides VM
 * management, as vm creation, submission of cloudlets to this VMs and destruction of VMs. NOTE- It
 * is an example only. It work on behalf of a provider not for users. One has to implement
 * interaction with user broker to this broker.
 *
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 3.0
 */
public class NetDatacenterBroker extends SimEntity {

    /**
     * Updated by xiaoleiy: The logger to print logs to file
     */
    private static final Logger LOGGER = Logger.getLogger(NetDatacenterBroker.class);

    /**
     * The vm list.
     */
    private List<? extends Vm> vmList;

    /**
     * The vms created list.
     */
    private List<? extends Vm> vmsCreatedList;

    /**
     * The cloudlet list.
     */
    private List<? extends NetworkCloudlet> cloudletList;

    private List<? extends AppCloudlet> appCloudletList;

    /**
     * Big data generation task:
     *
     * 1. replace the unused cloudletReceivedList (list of cloudlet) with countCloudletReceived (size of clouets)
     * 2. remove the unused global variable cloudletSubmittedList
     * 3. remove the unused global variable appCloudletRecieved
     */
    private int countCloudletReceived;

    /**
     * The cloudlets submitted.
     */
    private int cloudletsSubmitted;

    /**
     * The vms requested.
     */
    private int vmsRequested;

    /**
     * The vms acks.
     */
    private int vmsAcks;

    /**
     * The vms destroyed.
     */
    private int vmsDestroyed;

    /**
     * The datacenter ids list.
     */
    private List<Integer> datacenterIdsList;

    /**
     * The datacenter requested ids list.
     */
    private List<Integer> datacenterRequestedIdsList;

    /**
     * The vms to datacenters map.
     */
    private Map<Integer, Integer> vmsToDatacentersMap;

    /**
     * The datacenter characteristics list.
     */
    private Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

    /**
     * Updated by xiaoleiy:
     * 1. The global variable linkDC must be declared as non-static,
     * otherwise only 1 datacenter could be bound to the broker no matter how many brokers you have.
     *
     * 2. TODO
     *
     * In this way, for every broker, a separate data center could be bound for cloudlets being created and executed.
     */
    private List<NetworkDatacenter> linkDCs = new ArrayList<NetworkDatacenter>();

    public boolean createvmflag = true;

    /**
     * Updated by xiaoleiy:
     * redundant global variable cachedcloudlet, should be deleted.
     *
     * NOTE: NEVER declare static global variables when you want to create multiple data center brokers.
     * Because the static variables are class-level in Java, and only 1 instance of them exist in the JVM.
     */
    public static int cachedcloudlet = 0;

    /**
     * Created a new DatacenterBroker object.
     *
     * @param name name to be associated with this entity (as required by Sim_entity class from
     *             simjava package)
     * @throws Exception the exception
     * @pre name != null
     * @post $none
     */
    public NetDatacenterBroker(String name) throws Exception {
        super(name);

        setVmList(new ArrayList<NetworkVm>());
        setVmsCreatedList(new ArrayList<NetworkVm>());
        setCloudletList(new ArrayList<NetworkCloudlet>());
        setAppCloudletList(new ArrayList<AppCloudlet>());

        cloudletsSubmitted = 0;
        setVmsRequested(0);
        setVmsAcks(0);
        setVmsDestroyed(0);

        setDatacenterIdsList(new LinkedList<Integer>());
        setDatacenterRequestedIdsList(new ArrayList<Integer>());
        setVmsToDatacentersMap(new HashMap<Integer, Integer>());
        setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
    }

    /**
     * This method is used to send to the broker the list with virtual machines that must be
     * created.
     *
     * @param list the list
     * @pre list !=null
     * @post $none
     */
    public void submitVmList(List<? extends Vm> list) {
        getVmList().addAll(list);
    }

    /**
     * This method is used to send to the broker the list of cloudlets.
     *
     * @param list the list
     * @pre list !=null
     * @post $none
     */
    public void submitCloudletList(List<? extends NetworkCloudlet> list) {
        getCloudletList().addAll(list);
    }

    /**
     * Processes events available for this Broker.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    @Override
    public void processEvent(SimEvent ev) {
        switch (ev.getTag()) {
            // Resource characteristics request
            case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
                processResourceCharacteristicsRequest(ev);
                break;
            // Resource characteristics answer
            case CloudSimTags.RESOURCE_CHARACTERISTICS:
                processResourceCharacteristics(ev);
                break;
            // VM Creation answer

            // A finished cloudlet returned
            case CloudSimTags.CLOUDLET_RETURN:
                processCloudletReturn(ev);
                break;
            // if the simulation finishes
            case CloudSimTags.END_OF_SIMULATION:
                shutdownEntity();
                break;
            case CloudSimTags.NextCycle:
                if (NetworkConstants.BASE) {

                    /**
                     * Updated by xiaoleiy: updated to iterately create vms in all datacenters bound to this broker.
                     */
                    for (NetworkDatacenter dc : this.getLinkDCs()) {
                        createVmsInDatacenterBase(dc);
                    }
                }

                break;
            // other unknown tags are processed by this method
            default:
                processOtherEvent(ev);
                break;
        }
    }

    /**
     * Process the return of a request for the characteristics of a PowerDatacenter.
     *
     * @param ev a SimEvent object
     * @pre ev != $null
     * @post $none
     */
    protected void processResourceCharacteristics(SimEvent ev) {
        DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
        getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

        List<Integer> boundDCIds = getDatacenterIdsList();
        if (getDatacenterCharacteristicsList().size() == boundDCIds.size()) {
            setDatacenterRequestedIdsList(new ArrayList<Integer>());

            for (NetworkDatacenter datacenter : this.getLinkDCs()) {
                if (boundDCIds.contains(datacenter.getId())) {
                    createVmsInDatacenterBase(datacenter);
                }
            }
        }
    }

    /**
     * Process a request for the characteristics of a PowerDatacenter.
     *
     * @param ev a SimEvent object
     * @pre ev != $null
     * @post $none
     */
    protected void processResourceCharacteristicsRequest(SimEvent ev) {
        setDatacenterIdsList(CloudSim.getCloudResourceList());
        setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

        LOGGER.info(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with " +
                getDatacenterIdsList().size() + " resource(s)");

        for (Integer datacenterId : getDatacenterIdsList()) {
            sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
        }
    }

    /**
     * Process the ack received due to a request for VM creation.
     *
     * @param ev a SimEvent object
     *
     * @pre ev != null
     * @post $none
     */

    /**
     * Process a cloudlet return event.
     *
     * @param ev a SimEvent object
     * @pre ev != $null
     * @post $none
     */
    protected void processCloudletReturn(SimEvent ev) {
        Cloudlet cloudlet = (Cloudlet) ev.getData();
        countCloudletReceived++;
        cloudletsSubmitted--;

        /**
         * Big data generation task: free memory of the task stages explictly and cloudlet.
         */
        if (cloudlet instanceof NetworkCloudlet) {
            NetworkCloudlet netCloulet = (NetworkCloudlet)cloudlet;
            if (netCloulet.stages != null) {
                for (TaskStage stage : netCloulet.stages) {
                    stage.currPkt = null;
                }
                netCloulet.stages.clear();
                netCloulet.stages = null;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("--------- cleared stages for cloud #" + netCloulet.getCloudletId());
            }
            cloudlet = null;
        }

        // all cloudlets executed
        if (getCloudletList().size() == 0 && cloudletsSubmitted == 0 && NetworkConstants.iteration > 10) {
            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": All Cloudlets executed. Finishing...");
            clearDatacenters();
            finishExecution();
        } else { // some cloudlets haven't finished yet
            if (getAppCloudletList().size() > 0 && cloudletsSubmitted == 0) {
                // all the cloudlets sent finished. It means that some bount
                // cloudlet is waiting its VM be created
                clearDatacenters();

                /**
                 * Updated by xiaoleiy: updated to iterately create vms in all datacenters bound to this broker.
                 */
                for (NetworkDatacenter dc : this.getLinkDCs()) {
                    createVmsInDatacenterBase(dc);
                }
            }
        }
    }

    /**
     * Overrides this method when making a new and different type of Broker. This method is called
     * by {@link #()} for incoming unknown tags.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    protected void processOtherEvent(SimEvent ev) {
        if (ev == null) {
            Log.printConcatLine(getName(), ".processOtherEvent(): Error - an event is null.");
            return;
        }

        Log.printConcatLine(getName(), ".processOtherEvent(): ",
                "Error - event unknown by this DatacenterBroker.");
    }

    /**
     * Create the virtual machines in a datacenter and submit/schedule cloudlets to them.
     *
     * @param datacenter Id of the chosen PowerDatacenter
     * @pre $none
     * @post $none
     */
    protected void createVmsInDatacenterBase(NetworkDatacenter datacenter) {
        // send as much vms as possible for this datacenter before trying the
        // next one
        int requestedVms = 0;

        // All host will have two VMs (assumption) VM is the minimum unit
        if (createvmflag) {
            CreateVMs(datacenter);

            /**
             * Updated by xiaoleiy:
             * comment the following line to allow creating VMs for multiple data centers bound to this broker.
             */
//            createvmflag = false;
        }

        // generate Application execution Requests
        for (int i = 0; i < NetworkConstants.MAX_NUM_APPS; i++) {
            this.getAppCloudletList().add(new WorkflowApp(AppCloudlet.APP_Workflow, NetworkConstants.currentAppId, 0, 0, getId()));

            // Big data generation task: added logs for better information printing out.
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[" + datacenter.getName() + "] created workflow app #" + NetworkConstants.currentAppId +
                        " with datacenter #" + datacenter.getId() + " user #" + getId());
            }

            NetworkConstants.currentAppId++;
        }
        int k = 0;

        // schedule the application on VMs
        for (AppCloudlet app : this.getAppCloudletList()) {
            List<Integer> vmids = new ArrayList<Integer>();
            int numVms = datacenter.getVmList().size();
            UniformDistr ufrnd = new UniformDistr(0, numVms, 5);
            for (int i = 0; i < app.numbervm; i++) {
                int vmid = (int) ufrnd.sample();
                vmids.add(vmid);
            }

            if (!vmids.isEmpty()) {
                app.createCloudletList(vmids);
                for (int i = 0; i < app.numbervm; i++) {
                    NetworkCloudlet cloudlet = app.clist.get(i);
                    cloudlet.setUserId(getId());
                    cloudletsSubmitted++;

                    // Big data generation task: print out logs only when the debug level logging is enabled
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[" + datacenter.getName() + "] created cloudlet #" + cloudlet.getCloudletId()
                                + " with datacenter # " + datacenter.getId() + " user #" + cloudlet.getUserId());
                    }

                    // Sending cloudlet
                    sendNow(getVmsToDatacentersMap().get(this.getVmList().get(0).getId()),
                            CloudSimTags.CLOUDLET_SUBMIT,
                            cloudlet);
                }
            }
        }
 //
        this.appCloudletList.clear();
        this.appCloudletList = new ArrayList<AppCloudlet>();
        if (NetworkConstants.iteration < 10) {
            NetworkConstants.iteration++;
            this.schedule(getId(), NetworkConstants.nexttime, CloudSimTags.NextCycle);
        }

        setVmsRequested(requestedVms);
        setVmsAcks(0);
    }

    /**
     * Updated by xiaoleiy: updasted the parameter from datacentger id to the object datacenter;
     *
     * @param datacenter the datacenter
     */
    private void CreateVMs(NetworkDatacenter datacenter) {
        // two VMs per host
        int numVM = datacenter.getHostList().size() * NetworkConstants.maxhostVM;
        for (int i = 0; i < numVM; i++) {
            int vmid = i;
            int mips = 1;
            long size = 10000; // image size (MB)
            int ram = 512; // vm memory (MB)
            long bw = 1000;
            int pesNumber = NetworkConstants.HOST_PEs / NetworkConstants.maxhostVM;
            String vmm = "Xen"; // VMM name

            // create VM
            NetworkVm vm = new NetworkVm(
                    vmid,
                    getId(),
                    mips,
                    pesNumber,
                    ram,
                    bw,
                    size,
                    vmm,
                    new NetworkCloudletSpaceSharedScheduler(this));
            datacenter.processVmCreateNetwork(vm);
            // add the VM to the vmList
            getVmList().add(vm);
            getVmsToDatacentersMap().put(vmid, datacenter.getId());
            getVmsCreatedList().add(VmList.getById(getVmList(), vmid));
        }
    }

    /**
     * Submit cloudlets to the created VMs.
     *
     * @pre $none
     * @post $none /** Destroy the virtual machines running in datacenters.
     * @pre $none
     * @post $none
     */
    protected void clearDatacenters() {
        for (Vm vm : getVmsCreatedList()) {
            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Destroying VM #", vm.getId());
            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
        }

        getVmsCreatedList().clear();
    }

    /**
     * Send an internal event communicating the end of the simulation.
     *
     * @pre $none
     * @post $none
     */
    private void finishExecution() {
        sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.core.SimEntity#shutdownEntity()
     */
    @Override
    public void shutdownEntity() {
        LOGGER.info(getName() + " is shutting down...");
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.core.SimEntity#startEntity()
     */
    @Override
    public void startEntity() {
        LOGGER.info(getName() + " is starting...");
        schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
    }

    /**
     * Gets the vm list.
     *
     * @param <T> the generic type
     * @return the vm list
     */
    @SuppressWarnings("unchecked")
    public <T extends Vm> List<T> getVmList() {
        return (List<T>) vmList;
    }

    /**
     * Sets the vm list.
     *
     * @param <T>    the generic type
     * @param vmList the new vm list
     */
    protected <T extends Vm> void setVmList(List<T> vmList) {
        this.vmList = vmList;
    }

    /**
     * Gets the cloudlet list.
     *
     * @param <T> the generic type
     * @return the cloudlet list
     */
    @SuppressWarnings("unchecked")
    public <T extends NetworkCloudlet> List<T> getCloudletList() {
        return (List<T>) cloudletList;
    }

    /**
     * Sets the cloudlet list.
     *
     * @param <T>          the generic type
     * @param cloudletList the new cloudlet list
     */
    protected <T extends NetworkCloudlet> void setCloudletList(List<T> cloudletList) {
        this.cloudletList = cloudletList;
    }

    @SuppressWarnings("unchecked")
    public <T extends AppCloudlet> List<T> getAppCloudletList() {
        return (List<T>) appCloudletList;
    }

    public <T extends AppCloudlet> void setAppCloudletList(List<T> appCloudletList) {
        this.appCloudletList = appCloudletList;
    }

    /**
     * Gets the vm list.
     *
     * @param <T> the generic type
     * @return the vm list
     */
    @SuppressWarnings("unchecked")
    public <T extends Vm> List<T> getVmsCreatedList() {
        return (List<T>) vmsCreatedList;
    }

    /**
     * Sets the vm list.
     *
     * @param <T>            the generic type
     * @param vmsCreatedList the vms created list
     */
    protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
        this.vmsCreatedList = vmsCreatedList;
    }

    /**
     * Gets the vms requested.
     *
     * @return the vms requested
     */
    protected int getVmsRequested() {
        return vmsRequested;
    }

    /**
     * Sets the vms requested.
     *
     * @param vmsRequested the new vms requested
     */
    protected void setVmsRequested(int vmsRequested) {
        this.vmsRequested = vmsRequested;
    }

    /**
     * Gets the vms acks.
     *
     * @return the vms acks
     */
    protected int getVmsAcks() {
        return vmsAcks;
    }

    /**
     * Sets the vms acks.
     *
     * @param vmsAcks the new vms acks
     */
    protected void setVmsAcks(int vmsAcks) {
        this.vmsAcks = vmsAcks;
    }

    /**
     * Increment vms acks.
     */
    protected void incrementVmsAcks() {
        vmsAcks++;
    }

    /**
     * Gets the vms destroyed.
     *
     * @return the vms destroyed
     */
    protected int getVmsDestroyed() {
        return vmsDestroyed;
    }

    /**
     * Sets the vms destroyed.
     *
     * @param vmsDestroyed the new vms destroyed
     */
    protected void setVmsDestroyed(int vmsDestroyed) {
        this.vmsDestroyed = vmsDestroyed;
    }

    /**
     * Gets the datacenter ids list.
     *
     * @return the datacenter ids list
     */
    protected List<Integer> getDatacenterIdsList() {
        return datacenterIdsList;
    }

    /**
     * Sets the datacenter ids list.
     *
     * @param datacenterIdsList the new datacenter ids list
     */
    protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
        this.datacenterIdsList = datacenterIdsList;
    }

    /**
     * Gets the vms to datacenters map.
     *
     * @return the vms to datacenters map
     */
    protected Map<Integer, Integer> getVmsToDatacentersMap() {
        return vmsToDatacentersMap;
    }

    /**
     * Sets the vms to datacenters map.
     *
     * @param vmsToDatacentersMap the vms to datacenters map
     */
    protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
        this.vmsToDatacentersMap = vmsToDatacentersMap;
    }

    /**
     * Gets the datacenter characteristics list.
     *
     * @return the datacenter characteristics list
     */
    protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
        return datacenterCharacteristicsList;
    }

    /**
     * Sets the datacenter characteristics list.
     *
     * @param datacenterCharacteristicsList the datacenter characteristics list
     */
    protected void setDatacenterCharacteristicsList(
            Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
        this.datacenterCharacteristicsList = datacenterCharacteristicsList;
    }

    /**
     * Gets the datacenter requested ids list.
     *
     * @return the datacenter requested ids list
     */
    protected List<Integer> getDatacenterRequestedIdsList() {
        return datacenterRequestedIdsList;
    }

    /**
     * Sets the datacenter requested ids list.
     *
     * @param datacenterRequestedIdsList the new datacenter requested ids list
     */
    protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
        this.datacenterRequestedIdsList = datacenterRequestedIdsList;
    }

    public List<NetworkDatacenter> getLinkDCs() {
        return linkDCs;
    }

    public void setLinkDCs(List<NetworkDatacenter> linkDCs) {
        this.linkDCs = linkDCs;
    }

    public int getCountCloudletReceived() {
        return countCloudletReceived;
    }

    public void setCountCloudletReceived(int countCloudletReceived) {
        this.countCloudletReceived = countCloudletReceived;
    }
}
