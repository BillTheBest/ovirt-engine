package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class GetVmsInfoReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String VM_LIST = "vmlist";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("vmlist")]
    public XmlRpcStruct vmlist;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(vmlist, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public GetVmsInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(VM_LIST);
        if (temp != null) {
            vmlist = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }
}
