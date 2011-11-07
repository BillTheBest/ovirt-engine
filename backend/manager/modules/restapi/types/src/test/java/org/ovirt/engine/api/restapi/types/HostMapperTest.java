package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;

import org.junit.Test;

import org.ovirt.engine.api.model.Host;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;

public class HostMapperTest extends AbstractInvertibleMappingTest<Host, VdsStatic, VDS> {

    protected HostMapperTest() {
        super(Host.class, VdsStatic.class, VDS.class);
    }

    @Override
    protected Host postPopulate(Host from) {
        while (from.getPort() == 0) {
            from.setPort(MappingTestHelper.rand(65535));
        }
        return from;
    }

    @Override
    protected VDS getInverse(VdsStatic to) {
        VDS inverse = new VDS();
        inverse.setvds_id(to.getId());
        inverse.setvds_name(to.getvds_name());
        inverse.sethost_name(to.gethost_name());
        inverse.setvds_group_id(to.getvds_group_id());
        inverse.setport(to.getport());
        return inverse;
    }

    @Override
    protected void verify(Host model, Host transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertNotNull(transform.getCluster());
        assertEquals(model.getCluster().getId(), transform.getCluster().getId());
        assertEquals(model.getAddress(), transform.getAddress());
        assertEquals(model.getPort(), transform.getPort());
    }

    @Test
    public void testCpuMapping() {
        VDS vds = new VDS();
        vds.setvds_id(Guid.Empty);
        vds.setcpu_cores(2);
        vds.setcpu_model("some cpu model");
        vds.setcpu_speed_mh(5.5);
        Host host = HostMapper.map(vds, (Host)null);
        assertNotNull(host.getCpu());
        assertEquals(new Integer(host.getCpu().getTopology().getCores()), new Integer(2));
        assertEquals(host.getCpu().getName(), "some cpu model");
        assertEquals(host.getCpu().getSpeed(), new BigDecimal(5.5));
    }

    @Test
    public void testVmSummaryMapping() {
        VDS vds = new VDS();
        vds.setvds_id(Guid.Empty);
        vds.setvm_count(2);
        vds.setvm_active(1);
        vds.setvm_migrating(1);
        Host host = HostMapper.map(vds, (Host)null);
        assertEquals(host.getSummary().getTotal(), new Integer(2));
        assertEquals(host.getSummary().getActive(), new Integer(1));
        assertEquals(host.getSummary().getMigrating(), new Integer(1));
    }

}
