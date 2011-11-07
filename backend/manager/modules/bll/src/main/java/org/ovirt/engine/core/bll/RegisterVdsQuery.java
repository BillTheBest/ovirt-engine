package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.RegisterVdsParameters;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public class RegisterVdsQuery<P extends RegisterVdsParameters> extends QueriesCommandBase<P> {
    private final AuditLogableBase _logable;

    public RegisterVdsQuery(P parameters) {
        super(parameters);
        _logable = new AuditLogableBase(parameters.getVdsId());
    }

    private AuditLogType _error = AuditLogType.forValue(0);
    private static Object _doubleRegistrationLock = new Object();

    private static final String validChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-.:_";
    private String _strippedVdsUniqueId;

    private String getStrippedVdsUniqueId() {
        if (_strippedVdsUniqueId == null) {
            // since we use the management IP field, makes sense to remove the
            // illegal characters in advance
            StringBuilder builder = new StringBuilder();
            for (char ch : getParameters().getVdsUniqueId().toCharArray()) {
                if (validChars.indexOf(ch) != -1) {
                    builder.append(ch);
                }
            }
            _strippedVdsUniqueId = builder.toString();
        }
        return _strippedVdsUniqueId;
    }

    private List<VDS> _vdssByUniqueId;

    private List<VDS> getVdssByUniqueId() {
        if (_vdssByUniqueId == null) {
            VdsInstaller.UpdateUniqueId(getStrippedVdsUniqueId());
            _vdssByUniqueId = DbFacade.getInstance().getVdsDAO().getAllWithUniqueId(getStrippedVdsUniqueId());
        }
        return _vdssByUniqueId;
    }

    protected boolean CanDoAction(RefObject<String> errorMessage) {
        boolean returnValue = true;
        errorMessage.argvalue = "";
        Long otp = getParameters().getOtp();
        try {
            String hostName = getParameters().getVdsHostName();
            if (StringHelper.isNullOrEmpty(hostName)) {
                errorMessage.argvalue = "Cannot register Host - no Hostname address specified.";
                returnValue = false;
            } else {
                List<VDS> vdssByUniqueId = getVdssByUniqueId();
                if (vdssByUniqueId.size() > 1) {
                    errorMessage.argvalue = "Cannot register Host - unique id is ambigious.";
                    returnValue = false;
                } else if (vdssByUniqueId.size() == 1) {
                    VDS vds = vdssByUniqueId.get(0);
                    if (!VdsHandler.isPendingOvirt(vds)) {
                        errorMessage.argvalue =
                                String.format("Illegal Host status %s and/or type %s for host %s, expected %s type with %s status.",
                                        vds.getstatus().name(),
                                        vds.getvds_type().name(),
                                        vds.getvds_name(),
                                        VDSType.oVirtNode.name(),
                                        VDSStatus.PendingApproval.name());
                        errorMessage.argvalue = VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE.name();
                        returnValue = false;
                    } else if (otp != null && !isValidOtp(vds, otp)) {
                        errorMessage.argvalue = "Invalid OTP for host " + hostName;
                        returnValue = false;
                    }
                }
            }
        } catch (RuntimeException ex) {
            log.error("RegisterVdsQuery::CanDoAction: An exception has been thrown.", ex);
            errorMessage.argvalue = String.format("Cannot register Host - An exception has been thrown: %1$s",
                    ex.getMessage());
            returnValue = false;
        }

        return returnValue;
    }

    private boolean isValidOtp(VDS vds, Long otp) {
        if (otp != null && otp.longValue() == vds.getOtpValidity()) {
            Integer otpExpiration = Config.<Integer> GetValue(ConfigValues.OtpExpirationInSeconds);
            DateTime otpValidity = new DateTime(otp);
            otpValidity.AddSeconds(otpExpiration);
            if (otpValidity.before(DateTime.getUtcNow())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void executeQueryCommand() {

        // CanDoAction:
        String errorMessage = null;
        RefObject<String> tempRefObject = new RefObject<String>(errorMessage);
        boolean tempVar = !CanDoAction(tempRefObject);
        errorMessage = tempRefObject.argvalue;
        if (tempVar) {
            log.errorFormat("RegisterVdsQuery::ExecuteQueryCommand: CanDoAction failed: {0}", errorMessage);
            throw new RuntimeException(errorMessage);
        }

        // ExecuteAction:
        TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                ExecuteWithoutTransaction();
                return null;
            }
        });
    }

    protected boolean ExecuteWithoutTransaction() {
        boolean succeeded = false;

        try {
            log.info("Running Command: RegisterVds");
            ExecuteRegisterVdsCommand();
            succeeded = getQueryReturnValue().getSucceeded();
        }

        catch (RuntimeException ex) {
            log.error("RegisterVdsQuery::ExecuteWithoutTransaction: An exception has been thrown.", ex);
        }

        finally {
            WriteToAuditLog();
        }

        return succeeded;
    }

    protected void ExecuteRegisterVdsCommand() {
        synchronized (_doubleRegistrationLock) {
            // force to reload vdss by unique ID used later on
            _vdssByUniqueId = null;
            VDS vdsByUniqueId = getVdssByUniqueId().size() != 0 ? getVdssByUniqueId().get(0) : null;

            // in case oVirt host was added for the second time - perform approval
            if (vdsByUniqueId != null && vdsByUniqueId.getstatus() == VDSStatus.PendingApproval
                    && getParameters().getVdsType() == VDSType.oVirtNode
                    && getParameters().getOtp() != null) {

                getQueryReturnValue().setSucceeded(dispatchOvirtApprovalCommand(vdsByUniqueId.getvds_id()));
                return;
            }

            if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
                log.info("RegisterVdsQuery::ExecuteCommand - Entering");
            }

            if (StringHelper.isNullOrEmpty(getParameters().getVdsName())) {
                getParameters().setVdsName(getParameters().getVdsUniqueId());
                if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
                    log.info("RegisterVdsQuery::ExecuteCommand - VdsName empty, using VdsUnique ID as name");
                }
            }

            _logable.AddCustomValue("VdsName1", getParameters().getVdsName());

            Guid vdsGroupId;
            if (getParameters().getVdsGroupId().equals(Guid.Empty)) {
                vdsGroupId = new Guid(
                        Config.<String> GetValue(ConfigValues.PowerClientAutoRegistrationDefaultVdsGroupID));
                if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
                    log.infoFormat(
                            "RegisterVdsQuery::ExecuteCommand - VdsGroupId recieved as -1, using PowerClientAutoRegistrationDefaultVdsGroupID: {0}",
                            vdsGroupId);
                }
            } else {
                vdsGroupId = getParameters().getVdsGroupId();
            }

            if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration) && vdsByUniqueId != null) {
                log.infoFormat(
                        "RegisterVdsQuery::ExecuteCommand - found vds {0} with existing Unique Id {1}.  Will try to update existing vds",
                        vdsByUniqueId.getvds_id(),
                        vdsByUniqueId.getUniqueId());
            }

            boolean isPending = false;
            // TODO: always add in pending state, and if auto approve call
            // approve command action after registration
            RefObject<Boolean> tempRefObject = new RefObject<Boolean>(isPending);
            getQueryReturnValue().setSucceeded(
                    HandleOldVdssWithSameHostName(vdsByUniqueId) && HandleOldVdssWithSameName(vdsByUniqueId)
                    && CheckAutoApprovalDefinitions(tempRefObject) && Register(vdsByUniqueId, vdsGroupId, tempRefObject.argvalue));
            isPending = tempRefObject.argvalue;
            if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
                log.infoFormat("RegisterVdsQuery::ExecuteCommand - Leaving Succeded value is {0}",
                        getQueryReturnValue().getSucceeded());
            }
        }
    }

    private boolean dispatchOvirtApprovalCommand(Guid oVirtId) {
        boolean isApprovalDispatched = true;
        final ApproveVdsParameters params = new ApproveVdsParameters();
        params.setVdsId(oVirtId);
        params.setApprovedByRegister(true);

        try {
            ThreadPoolUtil.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        VdcReturnValueBase ret =
                            Backend.getInstance().runInternalAction(VdcActionType.ApproveVds, params);
                        if (ret == null || !ret.getSucceeded()) {
                            log.errorFormat("Approval of oVirt {0} failed. ", params.getVdsId());
                        } else if (ret.getSucceeded()) {
                            log.infoFormat("Approval of oVirt {0} ended successefully. ", params.getVdsId());
                        }
                    } catch (RuntimeException ex) {
                        log.error("Failed to Approve host", ex);
                    }
                }
            });
        } catch (Exception e) {
            isApprovalDispatched = false;
        }
        return isApprovalDispatched;
    }

    private VdcReturnValueBase RunActionWithinThreadCompat(VdcActionType actionType,
                                                           VdcActionParametersBase actionParameters) {
        try {
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

            WaitCallback cb = new WaitCallback(latch, actionType, actionParameters);

            ThreadPoolUtil.execute(cb);

            // Wait for background thread to end.
            try {
                latch.await();
            } catch (InterruptedException e) {
            }

            return cb.getReturnValue();
        }

        catch (RuntimeException ex) {
            log.error("RegisterVdsQuery::RunCommandWithinThread: An exception has been thrown.", ex);
            return null;
        }
    }

    private class WaitCallback implements Runnable {
        private final java.util.concurrent.CountDownLatch latch;
        private final VdcActionType actionType;
        private final VdcActionParametersBase actionParameters;
        private VdcReturnValueBase returnValue;

        public WaitCallback(java.util.concurrent.CountDownLatch latch, VdcActionType actionType,
                            VdcActionParametersBase actionParameters) {
            this.latch = latch;
            this.actionType = actionType;
            this.actionParameters = actionParameters;
        }

        public VdcReturnValueBase getReturnValue() {
            return returnValue;
        }

        @Override
        public void run() {
            try {
                returnValue = Backend.getInstance().runInternalAction(actionType, actionParameters);
            } catch (RuntimeException ex) {
                log.error("RegisterVdsQuery::WorkMethod [within thread]: An exception has been thrown.", ex);
            }

            finally {
                // Signal that this thread is finished.
                latch.countDown();
            }
        }
    }

    private boolean Register(VDS vdsByUniqueId, Guid vdsGroupId, boolean IsPending) {
        if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
            log.infoFormat("RegisterVdsQuery::Register - Entering");
        }

        boolean returnValue = true;
        if (vdsByUniqueId == null) {
            returnValue = registerNewHost(vdsGroupId, IsPending);
        } else {
            returnValue = updateExistingHost(vdsByUniqueId, IsPending);
        }

        if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
            log.infoFormat("RegisterVdsQuery::Register - Leaving with value {0}", returnValue);
        }

        return returnValue;
    }

    private boolean updateExistingHost(VDS vdsByUniqueId, boolean IsPending) {
        boolean returnValue = true;
        vdsByUniqueId.sethost_name(getParameters().getVdsHostName());
            vdsByUniqueId.setport(getParameters().getPort());
            // vdsByUniqueId.vds_group_id = vdsGroupId;
            if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
                log.infoFormat(
                        "RegisterVdsQuery::Register - Will try now to update VDS with existing unique id; Name: {0}, Hostname: {1}, Unique: {2}, Port: {3}, IsPending: {4} with force synchronize",
                        getParameters().getVdsHostName(),
                        getStrippedVdsUniqueId(),
                        getStrippedVdsUniqueId(),
                        getParameters().getPort(),
                        IsPending);
            }

            UpdateVdsActionParameters p = new UpdateVdsActionParameters(vdsByUniqueId.getStaticData(), "", false);
            VdcReturnValueBase rc = RunActionWithinThreadCompat(VdcActionType.UpdateVds, p);

            if (rc == null || !rc.getSucceeded()) {
                _error = AuditLogType.VDS_REGISTER_EXISTING_VDS_UPDATE_FAILED;
                if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
                    log.infoFormat(
                            "RegisterVdsQuery::Register - Failed to update existing VDS Name: {0}, Hostname: {1}, Unique: {2}, Port: {3}, IsPending: {4}",
                            getParameters().getVdsHostName(),
                            getStrippedVdsUniqueId(),
                            getStrippedVdsUniqueId(),
                            getParameters().getPort(),
                            IsPending);
                }

                CaptureCommandErrorsToLogger(rc, "RegisterVdsQuery::Register");
            returnValue = false;
            } else {
                log.infoFormat(
                        "RegisterVdsQuery::Register -Updated a {3} registered VDS - Name: {0}, Hostname: {1}, UniqueID: {2}",
                        getParameters().getVdsName(),
                        getParameters().getVdsHostName(),
                        getStrippedVdsUniqueId(),
                        vdsByUniqueId.getstatus() == VDSStatus.PendingApproval ? "Pending " : "");
            }
        return returnValue;
    }

    private boolean registerNewHost(Guid vdsGroupId, boolean IsPending) {
        boolean returnValue = true;
        VdsStatic vds = new VdsStatic(getParameters().getVdsHostName(), "",
                    getStrippedVdsUniqueId(), getParameters().getPort(), vdsGroupId, Guid.Empty,
                    getParameters().getVdsName(), Config.<Boolean> GetValue(ConfigValues.SSLEnabled),
                    getParameters().getVdsType()); // management
                                                                                 // ip
                                                                                 // not
                                                                                 // currently
                                                                                 // passed
                                                                                 // by
                                                                                 // registration

            if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
                log.infoFormat(
                        "RegisterVdsQuery::Register - Will try now to add VDS from scratch; Name: {0}, Hostname: {1}, Unique: {2}, Port: {3},Subnet mask: {4}, IsPending: {5} with force synchronize",
                        getParameters().getVdsName(),
                        getParameters().getVdsHostName(),
                        getStrippedVdsUniqueId(),
                        getParameters().getPort(),
                        IsPending);
            }

            AddVdsActionParameters p = new AddVdsActionParameters(vds, "");
            p.setAddPending(IsPending);

            VdcReturnValueBase ret = RunActionWithinThreadCompat(VdcActionType.AddVds, p);

            if (ret == null || !ret.getSucceeded()) {
                log.errorFormat(
                        "RegisterVdsQuery::Register - Registration failed for VDS - Name: {0}, Hostname: {1}, UniqueID: {2}, Subnet mask: {3}",
                        getParameters().getVdsName(),
                        getParameters().getVdsHostName(),
                        getStrippedVdsUniqueId());
                CaptureCommandErrorsToLogger(ret, "RegisterVdsQuery::Register");
                _error = AuditLogType.VDS_REGISTER_FAILED;
            returnValue = false;
            } else {
                log.infoFormat(
                        "RegisterVdsQuery::Register - Registered a new VDS {3} - Name: {0}, Hostname: {1}, UniqueID: {2}",
                        getParameters().getVdsName(),
                        getParameters().getVdsHostName(),
                        getStrippedVdsUniqueId(),
                        IsPending ? "pending approval" : "automatically approved");
            }
        return returnValue;
    }

    private boolean HandleOldVdssWithSameHostName(VDS vdsByUniqueId) {
        // handle old VDSs with same host_name (IP)
        if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
            log.infoFormat("RegisterVdsQuery::HandleOldVdssWithSameHostName - Entering");
        }

        boolean returnValue = true;
        List<VDS> vdss_byHostName = DbFacade.getInstance().getVdsDAO().getAllForHostname(
                getParameters().getVdsHostName());
        int lastIteratedIndex = 1;
        if (vdss_byHostName.size() > 0) {
            if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
                log.infoFormat(
                        "RegisterVdsQuery::HandleOldVdssWithSameHostName - found {0} VDS(s) with the same host name {1}.  Will try to change their hostname to a different value",
                        vdss_byHostName.size(),
                        getParameters().getVdsHostName());
            }

            for (VDS vds_byHostName : vdss_byHostName) {
                /**
                 * looping foreach VDS found with similar hostnames and change
                 * to each one to available hostname
                 */
                if (vdsByUniqueId == null
                        || (vdsByUniqueId != null && !vds_byHostName.getvds_id().equals(vdsByUniqueId.getvds_id()))) {
                        boolean unique = false;
                        String try_host_name = "";
                        for (int i = lastIteratedIndex; i <= 100; i++, lastIteratedIndex = i) {
                            try_host_name = String.format("hostname-was-%1$s-%2$s", getParameters()
                                    .getVdsHostName(), i);
                            if (DbFacade.getInstance().getVdsDAO().getAllForHostname(try_host_name).size() == 0) {
                                unique = true;
                                break;
                            }
                        }
                        if (unique) {
                            String old_host_name = vds_byHostName.gethost_name();
                            vds_byHostName.sethost_name(try_host_name);
                            UpdateVdsActionParameters tempVar = new UpdateVdsActionParameters(
                                    vds_byHostName.getStaticData(), "", false);
                            UpdateVdsActionParameters parameters = tempVar;
                            parameters.setShouldBeLogged(false);
                            VdcReturnValueBase ret = RunActionWithinThreadCompat(VdcActionType.UpdateVds, parameters);

                            if (ret == null || !ret.getSucceeded()) {
                                _error = AuditLogType.VDS_REGISTER_ERROR_UPDATING_HOST;
                                _logable.AddCustomValue("VdsName2", vds_byHostName.getStaticData().getvds_name());
                                log.errorFormat(
                                        "RegisterVdsQuery::HandleOldVdssWithSameHostName - could not update VDS {0}",
                                        vds_byHostName.getStaticData().getvds_name());
                                CaptureCommandErrorsToLogger(ret,
                                        "RegisterVdsQuery::HandleOldVdssWithSameHostName");
                                return false;
                            } else {
                                log.infoFormat(
                                        "RegisterVdsQuery::HandleOldVdssWithSameHostName - Another VDS was using this IP {0}. Changed to {1}",
                                        old_host_name,
                                        try_host_name);
                            }
                        } else {
                            log.errorFormat(
                                    "VdcBLL::HandleOldVdssWithSameHostName - Could not change the IP for an existing VDS. All available hostnames are taken (ID = {0}, name = {1}, management IP = {2} , host name = {3})",
                                    vds_byHostName.getvds_id(),
                                    vds_byHostName.getvds_name(),
                                    vds_byHostName.getManagmentIp(),
                                    vds_byHostName.gethost_name());
                            _error = AuditLogType.VDS_REGISTER_ERROR_UPDATING_HOST_ALL_TAKEN;
                            returnValue = false;
                        }
                }
                if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
                    log.infoFormat(
                            "RegisterVdsQuery::HandleOldVdssWithSameHostName - No Change required for VDS {0}. Since it has the same unique Id",
                            vds_byHostName.getvds_id());
                }
            }
        }
        if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
            log.infoFormat("RegisterVdsQuery::HandleOldVdssWithSameHostName - Leaving with value {0}", returnValue);
        }

        return returnValue;
    }

    /**
     * Check if another host has the same name as hostToRegister and if yes append a number to it. Eventually if the
     * host is in the db, persist the changes.
     * @param hostToRegister
     * @return
     */
    private boolean HandleOldVdssWithSameName(VDS hostToRegister) {
        Boolean logRegistration = Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration);
        if (logRegistration) {
            log.infoFormat("Entering");
        }
        boolean returnValue = true;
        VdsDAO vdsDAO = DbFacade.getInstance().getVdsDAO();
        List<VDS> hosts = vdsDAO.getAllWithName(getParameters().getVdsName());
        List<String> allHostNames = getAllHostNames(vdsDAO.getAll());
        boolean hostExistInDB = hostToRegister != null;

        if (hosts.size() > 0) {
            if (logRegistration) {
                log.infoFormat(
                        "found {0} VDS(s) with the same name {1}.  Will try to register with a new name",
                        hosts.size(),
                        getParameters().getVdsName());
            }

            String nameToRegister = getParameters().getVdsName();
            String uniqueIdToRegister = getParameters().getVdsUniqueId();
            String newName;

            for (VDS storedHost : hosts) {
                // check different uniqueIds but same name
                if (!uniqueIdToRegister.equals(storedHost.getUniqueId())
                        && nameToRegister.equals(storedHost.getvds_name())) {
                    if (hostExistInDB) {
                        // update the registered host if exist in db
                        allHostNames.remove(hostToRegister.getvds_name());
                        newName = generateUniqueName(nameToRegister, allHostNames);
                        hostToRegister.setvds_name(newName);
                        UpdateVdsActionParameters parameters =
                                new UpdateVdsActionParameters(hostToRegister.getStaticData(), "", false);
                        VdcReturnValueBase ret = RunActionWithinThreadCompat(VdcActionType.UpdateVds, parameters);
                        if (ret == null || !ret.getSucceeded()) {
                            _error = AuditLogType.VDS_REGISTER_ERROR_UPDATING_NAME;
                            _logable.AddCustomValue("VdsName2", newName);
                            log.errorFormat("could not update VDS {0}", nameToRegister);
                            CaptureCommandErrorsToLogger(ret, "RegisterVdsQuery::HandleOldVdssWithSameName");
                            return false;
                        } else {
                            log.infoFormat(
                                    "Another VDS was using this name with IP {0}. Changed to {1}",
                                    nameToRegister,
                                    newName);
                        }
                    } else {
                        // host doesn't exist in db yet. not persisting changes just object values.
                        newName = generateUniqueName(nameToRegister, allHostNames);
                        getParameters().setVdsName(newName);
                    }
                    break;
                }
            }
        } else {
            if (logRegistration) {
                log.infoFormat(
                        "No Change required for VDS {0}. Since it has the same unique Id",
                        hostToRegister.getvds_id());
            }
        }
        if (logRegistration) {
            log.infoFormat("Leaving with value {0}", returnValue);
        }
        return returnValue;
    }

    private List<String> getAllHostNames(List<VDS> allHosts) {
        List<String> allHostNames = new ArrayList<String>(allHosts.size());
        for (VDS vds : allHosts) {
            allHostNames.add(vds.getvds_name());
        }
        return allHostNames;
    }

    private String generateUniqueName(String val, List<String> allHostNames) {
        int i = 2;
        boolean postfixed = false;
        StringBuilder sb = new StringBuilder(val);
        while (allHostNames.contains(val)) {
            if (!postfixed) {
                val = sb.append("-").append(i).toString();
                postfixed = true;
            } else {
                val = sb.replace(sb.lastIndexOf("-"), sb.length(), "-").append(i).toString();
            }
            i++;
        }
        return val;
    }

    private boolean CheckAutoApprovalDefinitions(RefObject<Boolean> isPending) {
        // check auto approval definitions
        if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
            log.infoFormat("RegisterVdsQuery::CheckAutoApprovalDefinitions - Entering");
        }

        isPending.argvalue = true;
        if (!Config.<String> GetValue(ConfigValues.PowerClientAutoApprovePatterns).equals("")) {
            for (String pattern : Config.<String> GetValue(ConfigValues.PowerClientAutoApprovePatterns)
                    .split("[,]", -1)) {
                try {
                    String pattern_helper = pattern.toLowerCase();
                    Regex pattern_regex = new Regex(pattern_helper);
                    String vds_hostname_helper = getParameters().getVdsHostName().toLowerCase();
                    String vds_unique_id_helper = getParameters().getVdsUniqueId().toLowerCase()
                            .replace(":", "-");
                    if (vds_hostname_helper.startsWith(pattern) || vds_unique_id_helper.startsWith(pattern)
                            || pattern_regex.IsMatch(vds_hostname_helper)
                            || pattern_regex.IsMatch(vds_unique_id_helper)) {
                        isPending.argvalue = false;
                        break;
                    }
                } catch (RuntimeException ex) {
                    _error = AuditLogType.VDS_REGISTER_AUTO_APPROVE_PATTERN;
                    log.errorFormat(
                            "RegisterVdsQuery ::CheckAutoApprovalDefinitions(out bool) -  Error in auto approve pattern: {0}-{1}",
                            pattern,
                            ex.getMessage());
                    return false;
                }
            }
        }
        if (Config.<Boolean> GetValue(ConfigValues.LogVdsRegistration)) {
            log.infoFormat("RegisterVdsQuery::CheckAutoApprovalDefinitions - Leaving - return value {0}",
                    isPending.argvalue);
        }
        return true;
    }

    private void CaptureCommandErrorsToLogger(VdcReturnValueBase retValue, String prefixToMessage) {
        if (retValue.getFault() != null) {
            log.errorFormat("{0} - Fault - {1}", prefixToMessage, retValue.getFault().getMessage());
        }
        if (retValue.getCanDoActionMessages().size() > 0) {
            // List<string> msgs =
            // ErrorTranslator.TranslateErrorText(retValue.CanDoActionMessages);
            java.util.ArrayList<String> msgs = retValue.getCanDoActionMessages();
            for (String s : msgs) {
                log.errorFormat("{0} - CanDoAction Fault - {1}", prefixToMessage, s);
            }
        }
        if (retValue.getExecuteFailedMessages().size() > 0) {
            // List<string> msgs =
            // ErrorTranslator.TranslateErrorText(retValue.ExecuteFailedMessages);
            for (String s : retValue.getExecuteFailedMessages()) {
                log.errorFormat("{0} - Ececution Fault - {1}", prefixToMessage, s);
            }
        }
    }

    private void WriteToAuditLog() {
        try {
            AuditLogDirector.log(_logable, getAuditLogTypeValue());
        } catch (RuntimeException ex) {
            log.error("RegisterVdsQuery::WriteToAuditLog: An exception has been thrown.", ex);
        }
    }

    protected AuditLogType getAuditLogTypeValue() {
        return getQueryReturnValue().getSucceeded() ? AuditLogType.VDS_REGISTER_SUCCEEDED : _error;
    }

    private static LogCompat log = LogFactoryCompat.getLog(RegisterVdsQuery.class);

}
