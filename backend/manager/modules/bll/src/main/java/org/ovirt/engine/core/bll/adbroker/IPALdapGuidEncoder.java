package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.Guid;

public class IPALdapGuidEncoder implements LdapGuidEncoder {

    @Override
    public String encodeGuid(Guid guid) {
        return guid.toString();
    }

}
