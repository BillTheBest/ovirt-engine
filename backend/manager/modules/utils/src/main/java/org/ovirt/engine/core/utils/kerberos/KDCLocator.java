package org.ovirt.engine.core.utils.kerberos;

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dns.DnsSRVLocator;

/**
 * KDCs locator helper class. This class is used in order to locate KDCs in the DNS (based on a given realm). For each
 * KDC there are SRV records in DNS , providing information on the UDP and TCP ports it is using RFC 2782 defines an
 * algorithm that is used to order the KDCs for a given realm.
 **/

public class KDCLocator extends DnsSRVLocator {

    public DnsSRVResult getKdc(String protocol, String realmName) throws Exception {
        return getService("_kerberos", protocol, realmName);

    }

    public DnsSRVResult getKdc(String[] records) {
        return getSRVResult(records);

    }

    private static LogCompat log = LogFactoryCompat.getLog(KDCLocator.class);
}
