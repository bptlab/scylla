package de.hpi.bpt.scylla.plugin.dmn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.ProcessInstance;

public class DmnPluginUtils {
	
    static final String PLUGIN_NAME = "dmn";
    private static DmnPluginUtils singleton;
    private Map<String, Map<Integer, List<DmnNodeInfo>>> dmnNodeInfos = new TreeMap<String, Map<Integer, List<DmnNodeInfo>>>();

    static DmnPluginUtils getInstance() {
        if (singleton == null) {
            singleton = new DmnPluginUtils();
        }
        return singleton;
    }
    
    public Map<String, Map<Integer, List<DmnNodeInfo>>> getDmnNodeInfos() {
        return dmnNodeInfos;
    }
    
    public boolean addDmnNodeInfo(ProcessModel processModel, ProcessInstance processInstance, DmnNodeInfo dmnNodeInfo) {
        String processId = processModel.getId();
        ProcessModel parent = processModel.getParent();
        while (parent != null) {
            processId = parent.getId();
            parent = parent.getParent();
        }
        int processInstanceId = processInstance.getId();

        if (!dmnNodeInfos.containsKey(processId)) {
        	dmnNodeInfos.put(processId, new TreeMap<Integer, List<DmnNodeInfo>>());
        }
        Map<Integer, List<DmnNodeInfo>> nodeInfosOfProcess = dmnNodeInfos.get(processId);
        if (!nodeInfosOfProcess.containsKey(processInstanceId)) {
            nodeInfosOfProcess.put(processInstanceId, new ArrayList<DmnNodeInfo>());
        }
        return nodeInfosOfProcess.get(processInstanceId).add(dmnNodeInfo);
    }
}
