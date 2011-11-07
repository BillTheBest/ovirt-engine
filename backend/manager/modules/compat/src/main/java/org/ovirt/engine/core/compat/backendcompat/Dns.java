package org.ovirt.engine.core.compat.backendcompat;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Dns {

    public static IPAddress[] GetHostAddresses(String gethost_name) {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            byte[] ipAddr = addr.getAddress();
            IPAddress[] ipAddresses = new IPAddress[1];
            ipAddresses[0] = new IPAddress(ipAddr);
            return ipAddresses;
        } catch (UnknownHostException ex) {
            RuntimeException newEx = new RuntimeException(ex.getMessage());
            newEx.setStackTrace(ex.getStackTrace());
            throw newEx;
        }
    }
}
