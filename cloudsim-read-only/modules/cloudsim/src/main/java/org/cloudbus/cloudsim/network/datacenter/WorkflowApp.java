/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * WorkflowApp is an example of AppCloudlet having three communicating tasks. Task A and B sends the
 * data (packet) while Task C receives them
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
public class WorkflowApp extends AppCloudlet {

    // NOTE: Usually this should be a field rather than a method
    // variable so that it is not re-seeded every call.
    private static Random rand = new Random();

	public WorkflowApp(int type, int appID, double deadline, int numbervm, int userId) {
		super(type, appID, deadline, numbervm, userId);
		exeTime = 100;
		this.numbervm = 2;
	}


    @Override
    public void createCloudletList(List<Integer> vmIdList) {

        long fileSize = NetworkConstants.FILE_SIZE;
        long outputSize = NetworkConstants.OUTPUT_SIZE;
        int memory = 100;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        int i = 0;
        // SendCloudlet will only have send packets tasks. It sends packets to all RecvCloudlets
        NetworkCloudlet sendCloudlet = new NetworkCloudlet(
                NetworkConstants.currentCloudletId,
                0,
                1,
                fileSize,
                outputSize,
                memory,
                utilizationModel,
                utilizationModel,
                utilizationModel);

        sendCloudlet.numStage = 0;
        NetworkConstants.currentCloudletId++;
        sendCloudlet.setUserId(userId);
        sendCloudlet.submittime = CloudSim.clock();
        sendCloudlet.currStagenum = -1;
        sendCloudlet.setVmId(vmIdList.get(i));

        int cloudletCount = NetworkConstants.MAX_NUM_APPS * 2;

        for(int idx = 0;idx < cloudletCount;idx += 2){

            //sendCloudlet.stages.add(new TaskStage(NetworkConstants.WAIT_SEND, getPktSize(), 0, 1, memory,
            //        vmIdList.get(1), idx + 1));
            sendCloudlet.stages.add(new TaskStage(NetworkConstants.WAIT_SEND, 1024, 0, 1, memory,
                    vmIdList.get(1), idx + 1));
            sendCloudlet.numStage++;
        }

        long seed = System.nanoTime();
        Collections.shuffle(sendCloudlet.stages, new Random(seed));
        // first stage: big computation
        sendCloudlet.stages.add(0,new TaskStage(NetworkConstants.EXECUTION, 0, 1000 * 0.8, 0, memory,
                vmIdList.get(0), sendCloudlet.getCloudletId()));
        sendCloudlet.numStage++;

        clist.add(sendCloudlet);

        i++;
        // RecvCloudlet is setup to receive packets from all cloudlets
        NetworkCloudlet recvCloudlet = new NetworkCloudlet(
                NetworkConstants.currentCloudletId,
                0,
                1,
                fileSize,
                outputSize,
                memory,
                utilizationModel,
                utilizationModel,
                utilizationModel);

        NetworkConstants.currentCloudletId++;
        recvCloudlet.setUserId(userId);
        recvCloudlet.submittime = CloudSim.clock();
        recvCloudlet.currStagenum = -1;
        recvCloudlet.setVmId(vmIdList.get(i));

        for(int idx = 0;idx < cloudletCount;idx += 2){
            recvCloudlet.stages.add(new TaskStage(NetworkConstants.WAIT_RECV, 1024, 0, 0, memory,
                        vmIdList.get(0), idx));
            recvCloudlet.numStage++;
        }

        Collections.shuffle(recvCloudlet.stages, new Random(seed));
/*
        recvCloudlet.stages.add(new TaskStage(
                NetworkConstants.EXECUTION,
                0,
                1000 * 0.8,
                1,
                memory,
                vmIdList.get(0),
                recvCloudlet.getCloudletId()));
        recvCloudlet.numStage++;
*/


        clist.add(recvCloudlet);

    }

}
