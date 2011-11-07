package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;

public class HostNicMapperTest extends AbstractInvertibleMappingTest<HostNIC, VdsNetworkInterface, VdsNetworkInterface> {

    protected HostNicMapperTest() {
        super(HostNIC.class, VdsNetworkInterface.class, VdsNetworkInterface.class);
    }

    @Override
    protected void verify(HostNIC model, HostNIC transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertNotNull(transform.getNetwork());
        assertEquals(model.getNetwork().getName(), transform.getNetwork().getName());
        assertNotNull(transform.getIp());
        assertEquals(model.getIp().getAddress(), transform.getIp().getAddress());
        assertEquals(model.getIp().getNetmask(), transform.getIp().getNetmask());
        assertEquals(model.getIp().getGateway(), transform.getIp().getGateway());
        assertNotNull(transform.getMac());
        assertEquals(model.getMac().getAddress(), transform.getMac().getAddress());
        assertNotNull(model.getBonding());
        assertEquals(model.getBonding().getOptions().getOptions().size(), transform.getBonding().getOptions().getOptions().size());
        for(int i = 0; i < model.getBonding().getOptions().getOptions().size(); i++){
            assertEquals(model.getBonding().getOptions().getOptions().get(i).getName(), transform.getBonding().getOptions().getOptions().get(i).getName());
            assertEquals(model.getBonding().getOptions().getOptions().get(i).getValue(), transform.getBonding().getOptions().getOptions().get(i).getValue());
        }

    }
}
