package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkStatus;

import org.ovirt.engine.core.common.businessentities.network;

public class NetworkMapperTest extends AbstractInvertibleMappingTest<Network, network, network> {

    protected NetworkMapperTest() {
        super(Network.class, network.class, network.class);
    }

    @Override
    protected void verify(Network model, Network transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getStatus().getState(), transform.getStatus().getState());
        assertNotNull(transform.getDataCenter());
        assertEquals(model.getDataCenter().getId(), transform.getDataCenter().getId());
        assertNotNull(transform.getIp());
        assertEquals(model.getIp().getAddress(), transform.getIp().getAddress());
        assertEquals(model.getIp().getNetmask(), transform.getIp().getNetmask());
        assertEquals(model.getIp().getGateway(), transform.getIp().getGateway());
        assertNotNull(transform.getVlan());
        assertEquals(model.getVlan().getId(), transform.getVlan().getId());
        assertEquals(model.isStp(), transform.isStp());
    }

    @Override
    protected Network postPopulate(Network model) {
        model.setStatus(StatusUtils.create(MappingTestHelper.shuffle(NetworkStatus.class)));
        return super.postPopulate(model);
    }
}
