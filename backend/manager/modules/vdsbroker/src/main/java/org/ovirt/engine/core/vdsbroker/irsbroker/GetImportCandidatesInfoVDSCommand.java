package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.vdsbroker.vdsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;
import org.ovirt.engine.core.compat.backendcompat.UTF8EncodingCompat;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

import java.util.List;

public class GetImportCandidatesInfoVDSCommand<P extends GetImportCandidatesVDSCommandParameters>
        extends GetImportCandidateBase<P> {
    private ImportCandidatesInfoReturnForXmlRpc _candidatesInfoRetVal;

    public GetImportCandidatesInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        _candidatesInfoRetVal =
                getIrsProxy().getImportCandidatesInfo(StringHelper.trimEnd(getParameters().getPath(), '/'),
                        ImportEnumsManager.CandidateSourceString(getParameters().getCandidateSource()),
                        ImportEnumsManager.CandidateTypeString(getParameters().getCandidateType()));
        ProceedProxyReturnValue();

        java.util.HashMap<String, ImportCandidateInfoBase> ret =
                new java.util.HashMap<String, ImportCandidateInfoBase>();
        if (_candidatesInfoRetVal != null && _candidatesInfoRetVal.mInfoList.getCount() > 0) {
            for (String candidateID : _candidatesInfoRetVal.mInfoList.getKeys()) {
                ImportCandidateInfoBase candidateInfo = null;
                try {

                    java.util.Map irsInfoListAsMap = (java.util.Map) ((_candidatesInfoRetVal.mInfoList
                            .getItem(candidateID) instanceof java.util.Map) ? _candidatesInfoRetVal.mInfoList
                            .getItem(candidateID) : null);
                    XmlRpcStruct irsInfoList = (irsInfoListAsMap != null) ? new XmlRpcStruct(irsInfoListAsMap) : null;
                    if (irsInfoList != null) {
                        candidateInfo = GetCandidateInfoByIrsInfoList(irsInfoList);
                    }
                }

                catch (RuntimeException e) {
                    log.error(
                            String.format(
                                    "IrsBrokerCommand::GetImportCandidatesInfoVDSCommand::ExecuteIrsBrokerCommand: Error while trying to build info candidate for %1$s",
                                    candidateID),
                            e);
                }

                if (candidateInfo != null) {
                    if (!ret.containsKey(candidateID)) {
                        ret.put(candidateID, candidateInfo);
                    }

                    else {
                        log.warnFormat(
                                "IrsBrokerCommand::GetImportCandidatesInfoVDSCommand::ExecuteIrsBrokerCommand: There are several candidates that has '{0}' as ID",
                                candidateID);
                    }
                }
            }
        }

        setReturnValue(ret);
    }

    protected ImportCandidateInfoBase GetCandidateInfoByIrsInfoList(XmlRpcStruct irsInfoList) {
        ImportCandidateInfoBase retValue = null;

        if (getParameters().getCandidateSource() == ImportCandidateSourceEnum.VMWARE) {
            VmStatic tmpRetVal = new VmStatic();

            try {
                tmpRetVal.setDiskSize(((Integer) irsInfoList.getItem("diskSize")));
                tmpRetVal.setmem_size_mb(((Integer) irsInfoList.getItem("memSize")));
                GuessOsType(irsInfoList, tmpRetVal);
                tmpRetVal.setvm_name(((String) irsInfoList.getItem("displayName")));
                String imageName = ((String) irsInfoList.getItem("imageName"));
                String imageDescr = ((String) irsInfoList.getItem("imageDescr"));
                retValue = new VmCandidateInfo(tmpRetVal, ImportCandidateSourceEnum.VMWARE, null);
            } catch (RuntimeException e) {
                log.errorFormat(
                        "IrsBrokerCommand::GetImportCandidatesInfoVDSCommand::Failed recieving info, xmlRpcStruct = {0},\n Exception is : {1}",
                        irsInfoList.toString(),
                        e.toString());
                IRSErrorException outEx = new IRSErrorException(e);
                log.error(outEx);
                throw outEx;
            }
        }

        else // KVM
        {
            try {
                byte[] byteArrayOvfData = (byte[]) (irsInfoList.getItem("metadata"));
                UTF8EncodingCompat utf8enc = new UTF8EncodingCompat();
                String ovfData = utf8enc.GetString(byteArrayOvfData);

                java.util.ArrayList<DiskImage> candidateImagesData = null;
                OvfManager ovfm = new OvfManager();
                if (ovfm.IsOvfTemplate(ovfData)) {
                    VmTemplate candidateData = null;
                    RefObject<VmTemplate> tempRefObject = new RefObject<VmTemplate>(candidateData);
                    RefObject<java.util.ArrayList<DiskImage>> tempRefObject2 =
                            new RefObject<java.util.ArrayList<DiskImage>>(
                                    candidateImagesData);
                    try {
                        ovfm.ImportTemplate(ovfData, tempRefObject, tempRefObject2);
                    } catch (OvfReaderException ex) {
                        AuditLogableBase logable = new AuditLogableBase();
                        logable.AddCustomValue("Template", ex.getName());
                        AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_TEMPLATE);
                    }
                    candidateData = tempRefObject.argvalue;
                    candidateImagesData = tempRefObject2.argvalue;
                    retValue = new TemplateCandidateInfo(candidateData, ImportCandidateSourceEnum.KVM,
                            GetListOfImageListsByDrive(candidateImagesData));
                }

                else // VM
                {
                    VM candidateData = null;
                    RefObject<VM> tempRefObject3 = new RefObject<VM>(candidateData);
                    RefObject<java.util.ArrayList<DiskImage>> tempRefObject4 =
                            new RefObject<java.util.ArrayList<DiskImage>>(
                                    candidateImagesData);
                    try {
                        ovfm.ImportVm(ovfData, tempRefObject3, tempRefObject4);
                    } catch (OvfReaderException ex) {
                        AuditLogableBase logable = new AuditLogableBase();
                        logable.AddCustomValue("VmName", ex.getName());
                        AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_VM);
                    }
                    candidateData = tempRefObject3.argvalue;
                    candidateImagesData = tempRefObject4.argvalue;
                    retValue = new VmCandidateInfo(candidateData.getStaticData(), ImportCandidateSourceEnum.KVM,
                            GetListOfImageListsByDrive(candidateImagesData));
                }
            } catch (RuntimeException e) {
                log.errorFormat(
                        "IrsBrokerCommand::GetImportCandidatesInfoVDSCommand::Failed recieving 'metadata' from info or error analyzing info, xmlRpcStruct = {0},\n Exception is : {1}",
                        irsInfoList.toString(),
                        e.toString());
                IRSErrorException outEx = new IRSErrorException(e);
                log.error(outEx);
                throw outEx;
            }
        }

        return retValue;
    }

    private void GuessOsType(XmlRpcStruct irsInfoList, VmStatic tmpRetVal) {
        String strOSName = ((String) irsInfoList.getItem("guestOS"));
        if (strOSName.contains("xp")) {
            tmpRetVal.setos(VmOsType.WindowsXP);
        } else if (strOSName.contains("2003")) // TODO: Can we distinct 2003 and
                                               // 2003 x64?
        {
            tmpRetVal.setos(VmOsType.Windows2003);
        } else if (strOSName.contains("inux")) {
            tmpRetVal.setos(VmOsType.OtherLinux);
        } else if (strOSName.contains("2008")) {
            tmpRetVal.setos(VmOsType.Windows2008);
        } else {
            tmpRetVal.setos(VmOsType.Unassigned);
        }
    }

    protected java.util.HashMap<String, List<DiskImage>> GetListOfImageListsByDrive(
            java.util.ArrayList<DiskImage> allDrivesImages) {
        java.util.HashMap<String, List<DiskImage>> ret = new java.util.HashMap<String, List<DiskImage>>();
        if (allDrivesImages != null && allDrivesImages.size() > 0) {
            for (DiskImage image : allDrivesImages) {
                if (!ret.containsKey(image.getinternal_drive_mapping())) {
                    ret.put(image.getinternal_drive_mapping(), new java.util.ArrayList<DiskImage>());
                }

                ret.get(image.getinternal_drive_mapping()).add(image);
            }
        }

        return ret;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _candidatesInfoRetVal.mStatus;
    }

    private static LogCompat log = LogFactoryCompat.getLog(GetImportCandidatesInfoVDSCommand.class);
}
