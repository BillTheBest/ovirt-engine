package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class StartSpiceVDSCommand<P extends StartSpiceVDSCommandParameters> extends VdsBrokerCommand<P> {
    private String _ip;
    private int _port;
    private String _ticket;

    public StartSpiceVDSCommand(P parameters) {
        super(parameters);
        _ip = parameters.getVdsIp();
        _port = parameters.getGuestPort();
        _ticket = parameters.getTicket();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().startSpice(_ip, _port, _ticket);
        ProceedProxyReturnValue();
    }
}
