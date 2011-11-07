package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public final class IQNListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String TARGETS = "targets";
    private static final String FULL_TARGETS = "fullTargets";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("targets")]
    private List<String> iqnList = Collections.emptyList();
    private boolean fullTargets;

    public IQNListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        if (innerMap.containsKey(FULL_TARGETS)) {
            fullTargets = true;
            iqnList = toList((Object[]) innerMap.get(FULL_TARGETS));
        } else {
            if (innerMap.containsKey(TARGETS)) {
                iqnList = toList((Object[]) innerMap.get(TARGETS));
            }
        }
    }

    private List<String> toList(Object[] objects) {
        List<String> iqns = new ArrayList<String>(objects.length);
        for (Object o : objects) {
            iqns.add((String) o);
        }
        return iqns;
    }

    public List<String> getIqnList() {
        return iqnList;
    }

    public boolean isFullTargets() {
        return fullTargets;
    }

}
