package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.asynctasks.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class CopyImageVDSCommand<P extends CopyImageVDSCommandParameters> extends IrsCreateCommand<P> {
    public CopyImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        /**
         * TODO: EINAV: - Consider 'RunAsync' parameter (pass it to IRS too). - Add 'description' parameter. - we should
         * provide 'volumeUUID'. - TaskID should be provided by IRS.
         */
        setReturnValue(Guid.Empty);

        log.info("-- CopyImageVDSCommand::ExecuteIrsBrokerCommand: calling 'copyImage' with two new parameters: description and UUID");
        log.infoFormat("-- copyImage parameters:" + "\r\n" + "                sdUUID={0}" + "\r\n"
                + "                spUUID={1}" + "\r\n" + "                vmGUID={2}" + "\r\n"
                + "                srcImageGUID={3}" + "\r\n" + "                srcVolUUID={4}" + "\r\n"
                + "                dstImageGUID={5}" + "\r\n" + "                dstVolUUID={6}" + "\r\n"
                + "                descr={7}" + "\r\n" + "                ", getParameters().getStorageDomainId()
                .toString(), getParameters().getStoragePoolId().toString(), getParameters().getVmId()
                .toString(), getParameters().getImageGroupId().toString(), getParameters().getImageId()
                .toString(), getParameters().getdstImageGroupId().toString(), getParameters().getDstImageId()
                .toString(), getParameters().getImageDescription());

        // NOTE: The 'uuidReturn' variable will contain the taskID and not the
        // created image id!
        uuidReturn = getIrsProxy().copyImage(getParameters().getStorageDomainId().toString(),
                                             getParameters().getStoragePoolId().toString(),
                                             getParameters().getVmId().toString(),
                                             getParameters().getImageGroupId().toString(),
                                             getParameters().getImageId().toString(),
                                             getParameters().getdstImageGroupId().toString(),
                                             getParameters().getDstImageId().toString(),
                                             getParameters().getImageDescription(),
                                             getParameters().getDstStorageDomainId().toString(),
                                             getParameters().getCopyVolumeType().getValue(),
                                             getParameters().getVolumeFormat().getValue(),
                                             getParameters().getPreallocate().getValue(),
                                             (new Boolean(getParameters().getPostZero())).toString().toLowerCase(),
                                             (new Boolean(getParameters().getForce())).toString().toLowerCase());
        ProceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.mUuid);

        mCreatedImageId = getParameters().getDstImageId().toString();
        // ReturnValue = new Guid(mCreatedImageId);

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.copyImage, getParameters().getStoragePoolId()));
    }

    private static LogCompat log = LogFactoryCompat.getLog(CopyImageVDSCommand.class);
}
