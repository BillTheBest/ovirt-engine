package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.IP;
import org.ovirt.engine.api.model.MAC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NicStatus;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.core.common.businessentities.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class HostNicMapper {
    private static final String OPTIONS_DELIMITER = "\\ ";
    private static final String OPTIONS_EQ = "\\=";
    private static final String[] BONDING_MODS = new String[]{"Active-Backup",
                                                              "Load balance (balance-xor)",
                                                              null,
                                                              "Dynamic link aggregation (802.3ad)",
                                                              "Adaptive transmit load balancing (balance-tlb)"};

    @Mapping(from = HostNIC.class, to = VdsNetworkInterface.class)
    public static VdsNetworkInterface map(HostNIC model, VdsNetworkInterface template) {
        VdsNetworkInterface entity = template != null ? template : new VdsNetworkInterface();
        if (model.isSetId()) {
            entity.setId(new Guid(model.getId()));
        }
        if (model.isSetNetwork() && model.getNetwork().isSetName()) {
            entity.setNetworkName(model.getNetwork().getName());
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetIp()) {
            if (model.getIp().isSetAddress()) {
                entity.setAddress(model.getIp().getAddress());
            }
            if (model.getIp().isSetGateway()) {
                entity.setGateway(model.getIp().getGateway());
            }
            if (model.getIp().isSetNetmask()) {
                entity.setSubnet(model.getIp().getNetmask());
            }
        }
        if (model.isSetMac() && model.getMac().isSetAddress()) {
            entity.setMacAddress(model.getMac().getAddress());
        }
        if(model.isSetBonding() && model.getBonding().isSetOptions()){
                StringBuffer buf = new StringBuffer();
                for(Option opt : model.getBonding().getOptions().getOptions()){
                    buf.append(opt.getName() + "=" + opt.getValue() + " ");
                }
                entity.setBondOptions(buf.toString().substring(0, buf.length() - 1));
        }
        if(model.isSetBootProtocol()){
            NetworkBootProtocol networkBootProtocol = map(BootProtocol.fromValue(model.getBootProtocol()), null);
            if(networkBootProtocol != null){
                entity.setBootProtocol(networkBootProtocol);
            }
        }
        return entity;
    }

    @Mapping(from = VdsNetworkInterface.class, to = HostNIC.class)
    public static HostNIC map(VdsNetworkInterface entity, HostNIC template) {
        HostNIC model = template != null ? template : new HostNIC();
        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        if (entity.getNetworkName() != null) {
            model.setNetwork(new Network());
            model.getNetwork().setName(entity.getNetworkName());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getAddress() != null || entity.getGateway() != null || entity.getSubnet() != null) {
            model.setIp(new IP());
            if (entity.getAddress() != null) {
                model.getIp().setAddress(entity.getAddress());
            }
            if (entity.getGateway() != null) {
                model.getIp().setGateway(entity.getGateway());
            }
            if (entity.getSubnet() != null) {
                model.getIp().setNetmask(entity.getSubnet());
            }
        }
        if (entity.getMacAddress() != null) {
            model.setMac(new MAC());
            model.getMac().setAddress(entity.getMacAddress());
        }
        if(entity.getStatistics().getStatus()!=InterfaceStatus.None){
            NicStatus nicStatus = map(entity.getStatistics().getStatus(),null);
            if(nicStatus!=null){
                model.setStatus(StatusUtils.create(nicStatus));
            }
        }
        if(entity.getSpeed()!=null && entity.getSpeed()>0){
            model.setSpeed(entity.getSpeed() * 1000L * 1000);
        }
        if(!StringHelper.isNullOrEmpty(entity.getBondOptions())){
            if(model.getBonding() == null) model.setBonding(new Bonding());
            model.getBonding().setOptions(new Options());
            for(String opt : entity.getBondOptions().split(OPTIONS_DELIMITER)){
                String[] option_pair = opt.split(OPTIONS_EQ);
                if(option_pair.length == 2){
                    Option option = new Option();
                    option.setName(option_pair[0]);
                    option.setValue(option_pair[1]);
                    option.setType(getType(option_pair));
                    model.getBonding().getOptions().getOptions().add(option);
                }
            }
        }

        BootProtocol bootProtocol = map(entity.getBootProtocol(), null);
        if(bootProtocol!=null){
            model.setBootProtocol(bootProtocol.value());
        }
        return model;
    }

    private static String getType(final String[] optionPair) {
        if(!StringHelper.isNullOrEmpty(optionPair[0]) && optionPair[0].equals("mode") && !StringHelper.isNullOrEmpty(optionPair[1])){
            Integer mode = tryParse(optionPair[1]);
            if(mode != null && mode > 0 && mode < 6){
                return BONDING_MODS[mode - 1];
            }
        }
        return null;
    }

    public static Integer tryParse(String text) {
        try {
          return new Integer(text);
        } catch (NumberFormatException e) {
          return null;
        }
      }

    @Mapping(from = InterfaceStatus.class, to = NicStatus.class)
    public static NicStatus map(InterfaceStatus interfaceStatus, NicStatus template) {
        if(interfaceStatus!=null){
            switch (interfaceStatus) {
            case Up:
                return NicStatus.UP;
            case Down:
                return NicStatus.DOWN;
            default:
                return null;
            }
        }
        return null;
    }

    @Mapping(from = NetworkBootProtocol.class, to = BootProtocol.class)
    public static BootProtocol map(NetworkBootProtocol networkBootProtocol, BootProtocol template) {
        if(networkBootProtocol!=null){
            switch (networkBootProtocol) {
            case Dhcp:
                return BootProtocol.DHCP;
            case StaticIp:
                return BootProtocol.STATIC;
            default:
                return null;
            }
        }
        return null;
    }

    @Mapping(from = BootProtocol.class, to = NetworkBootProtocol.class)
    public static NetworkBootProtocol map(BootProtocol bootProtocol, NetworkBootProtocol template) {
        if(bootProtocol!=null){
            switch (bootProtocol) {
            case DHCP:
                return NetworkBootProtocol.Dhcp;
            case STATIC:
                return NetworkBootProtocol.StaticIp;
            default:
                return null;
            }
        }
        return null;
    }
}
