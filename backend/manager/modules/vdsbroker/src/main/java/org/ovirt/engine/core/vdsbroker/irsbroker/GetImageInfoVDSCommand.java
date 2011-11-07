package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.GetImageInfoVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class GetImageInfoVDSCommand<P extends GetImageInfoVDSCommandParameters> extends IrsBrokerCommand<P> {
    protected OneImageInfoReturnForXmlRpc imageInfoReturn;

    @Override
    protected Object getReturnValueFromBroker() {
        return imageInfoReturn;
    }

    public GetImageInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        imageInfoReturn = getIrsProxy().getVolumeInfo(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(), getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString());
        DiskImage di = null;
        try {
            ProceedProxyReturnValue();
            di = buildImageEntity(imageInfoReturn.mInfo);
        } catch (java.lang.Exception e) {
            PrintReturnValue();
            // nothing to do - logging inside upper functions
        } finally {
            // if couldn't parse image then succeeded should be false
            getVDSReturnValue().setSucceeded((di != null));
            setReturnValue(di);
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return imageInfoReturn.mStatus;
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        if (returnStatus != VdcBllErrors.Done) {
            log.errorFormat(
                    "IrsBroker::getImageInfo::Failed getting image info imageId = {0} does not exist on domainName = {1} , domainId = {2},  error code: {3}, message: {4}",
                    getParameters().getImageId().toString(),
                    DbFacade.getInstance().getStorageDomainStaticDAO()
                            .get(getParameters().getStorageDomainId())
                            .getstorage_name(),
                    getParameters()
                            .getStorageDomainId().toString(),
                    returnStatus
                            .toString(),
                    imageInfoReturn.mStatus.mMessage);
            throw new IRSErrorException(returnStatus.toString());
        }
    }

    /**
     * <exception>VdcDAL.IrsBrokerIRSErrorException.
     */
    public DiskImage buildImageEntity(XmlRpcStruct xmlRpcStruct) {
        DiskImage newImage = new DiskImage();
        try {
            newImage.setId(new Guid((String) xmlRpcStruct.getItem(IrsProperties.uuid)));
            if (xmlRpcStruct.getItem(IrsProperties.children).getClass().getName().equals("String[]")) {
                String[] childrenIdList = (String[]) xmlRpcStruct.getItem(IrsProperties.children);
                newImage.setchildrenId(convertStringGuidArray(childrenIdList));
            }

            newImage.setParentId(new Guid((String) xmlRpcStruct.getItem(IrsProperties.parent)));
            newImage.setdescription((String) xmlRpcStruct.getItem(IrsProperties.description));
            newImage.setimageStatus(EnumUtils.valueOf(ImageStatus.class,
                    (String) xmlRpcStruct.getItem(IrsProperties.ImageStatus), true));
            if (xmlRpcStruct.contains(IrsProperties.size)) {
                newImage.setsize(Long.parseLong(xmlRpcStruct.getItem(IrsProperties.size).toString()) * 512);
            }
            if (xmlRpcStruct.contains("apparentsize")) {
                newImage.setactual_size(Long.parseLong(xmlRpcStruct.getItem("apparentsize").toString()) * 512);
            }
            if (xmlRpcStruct.contains("capacity")) {
                newImage.setsize(Long.parseLong(xmlRpcStruct.getItem("capacity").toString()));
            }
            if (xmlRpcStruct.contains("truesize")) {
                newImage.setactual_size(Long.parseLong(xmlRpcStruct.getItem("truesize").toString()));
            }
            if (xmlRpcStruct.contains("ctime")) {
                long secsSinceEpoch = Long.parseLong(xmlRpcStruct.getItem("ctime").toString());
                newImage.setcreation_date(MakeDTFromCTime(secsSinceEpoch));
            }
            if (xmlRpcStruct.contains("mtime")) {
                long secsSinceEpoch = Long.parseLong(xmlRpcStruct.getItem("mtime").toString());
                newImage.setlast_modified_date(MakeDTFromCTime(secsSinceEpoch));
            }
            if (xmlRpcStruct.contains("domain")) {
                newImage.setstorage_id(new Guid(xmlRpcStruct.getItem("domain").toString()));
            }
            if (xmlRpcStruct.contains("image")) {
                newImage.setimage_group_id(new Guid(xmlRpcStruct.getItem("image").toString()));
            }
        } catch (RuntimeException ex) {
            log.errorFormat("irsBroker::buildImageEntity::Failed building DIskImage");
            PrintReturnValue();
            log.error(ex.getMessage(), ex);
            newImage = null;
        }

        return newImage;
    }

    private static java.util.Date MakeDTFromCTime(long ctime) {
        return new Date(ctime * 1000L);
    }

    private static LogCompat log = LogFactoryCompat.getLog(GetImageInfoVDSCommand.class);
}
