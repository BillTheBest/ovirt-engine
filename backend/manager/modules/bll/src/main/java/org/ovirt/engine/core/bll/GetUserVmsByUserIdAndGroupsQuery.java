package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImagesComparerByName;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetUserVmsByUserIdAndGroupsQuery<P extends GetUserVmsByUserIdAndGroupsParameters>
        extends QueriesCommandBase<P> {
    public GetUserVmsByUserIdAndGroupsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vmList =
                DbFacade.getInstance().getVmDAO().getAllForUserWithGroupsAndUserRoles(getParameters().getId());
        for (VM vm : vmList) {
            VmHandler.UpdateVmGuestAgentVersion(vm);
            if (getParameters().getIncludeDiskData()) {
                VmHandler.updateDisksFromDb(vm);
                java.util.Collections.sort(vm.getDiskList(), new ImagesComparerByName());
                for (DiskImage diskImage : vm.getDiskMap().values()) {
                    diskImage.getSnapshots().addAll(
                            ImagesHandler.getAllImageSnapshots(diskImage.getId(), diskImage.getit_guid()));
                }
            }
        }
        getQueryReturnValue().setReturnValue(vmList);
    }
}
