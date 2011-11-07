package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.MAC;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class NicMapper {
    @Mapping(from = NIC.class, to = VmNetworkInterface.class)
    public static VmNetworkInterface map(NIC model, VmNetworkInterface template) {
        VmNetworkInterface entity = template != null ? template : new VmNetworkInterface();
        if (model.isSetVm() && model.getVm().isSetId()) {
            entity.setVmId(new Guid(model.getVm().getId()));
        }
        if (model.isSetId()) {
            entity.setId(new Guid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetMac() && model.getMac().isSetAddress()) {
            entity.setMacAddress(model.getMac().getAddress());
        }
        if (model.isSetNetwork() && model.getNetwork().isSetName()) {
            entity.setNetworkName(model.getNetwork().getName());
        }
        if (model.isSetInterface()) {
            NicInterface nicType = NicInterface.fromValue(model.getInterface());
            if (nicType != null) {
                entity.setType(map(nicType));
            }
        }
        return entity;
    }

    @Mapping(from = VmNetworkInterface.class, to = NIC.class)
    public static NIC map(VmNetworkInterface entity, NIC template) {
        NIC model = template != null ? template : new NIC();

        if (entity.getVmId() != null) {
            model.setVm(new VM());
            model.getVm().setId(entity.getVmId().toString());
        }
        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getMacAddress() != null) {
            model.setMac(new MAC());
            model.getMac().setAddress(entity.getMacAddress());
        }
        if (entity.getNetworkName() != null) {
            model.setNetwork(new Network());
            model.getNetwork().setName(entity.getNetworkName());
        }
        model.setInterface(map(entity.getType()));
        return model;
    }

    @Mapping(from = NicInterface.class, to = Integer.class)
    public static Integer map(NicInterface type) {
        switch (type) {
        case RTL8139_VIRTIO:
            return 0;
        case RTL8139:
            return 1;
        case E1000:
            return 2;
        case VIRTIO:
            return 3;
        default:
            return -1;
        }
    }

    @Mapping(from = Integer.class, to = String.class)
    public static String map(Integer type) {
        switch (type) {
        case 0:
            return NicInterface.RTL8139_VIRTIO.value();
        case 1:
            return NicInterface.RTL8139.value();
        case 2:
            return NicInterface.E1000.value();
        case 3:
            return NicInterface.VIRTIO.value();
        default:
            return null;
        }
    }
}
