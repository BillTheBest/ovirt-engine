package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

//-----------------------------------------------------
//
//-----------------------------------------------------

public final class OneImageInfoReturnForXmlRpc extends StatusReturnForXmlRpc {

    private static final String INFO = "info";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("info")]
    public XmlRpcStruct mInfo;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(mInfo, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public OneImageInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(INFO);
        if (temp != null) {
            mInfo = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }

}
