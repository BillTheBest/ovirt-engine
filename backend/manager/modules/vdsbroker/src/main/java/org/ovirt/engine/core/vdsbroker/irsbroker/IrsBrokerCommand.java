package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.SpmStatus;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.GetStoragePoolInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStartVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStopVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.BrokerCommandBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcRunTimeException;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcUtils;

@Logged(errorLevel = LogLevel.ERROR)
public abstract class IrsBrokerCommand<P extends IrsBaseVDSCommandParameters> extends BrokerCommandBase<P> {
    public static final long BYTES_TO_GB = 1024 * 1024 * 1024;
    public static final int DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    private static Map<Guid, IrsProxyData> _irsProxyData = new ConcurrentHashMap<Guid, IrsProxyData>();

    public static void UpdateVdsDomainsData(Guid vdsId, String vdsName, Guid storagePoolId,
            java.util.ArrayList<VDSDomainsData> vdsDomainData) {
        IrsProxyData proxy = _irsProxyData.get(storagePoolId);
        if (proxy != null) {
            proxy.UpdateVdsDomainsData(vdsId, vdsName, vdsDomainData);
        }
    }

    public static boolean isDomainReportedInProblem(Guid storagePoolId, Guid domainId) {
        IrsProxyData proxy = _irsProxyData.get(storagePoolId);
        if (proxy != null) {
            return proxy.isDomainReportedInProblem(domainId);
        }
        return false;
    }

    public static void lockDbSave(Guid storagePoolId) {
        IrsProxyData proxy = _irsProxyData.get(storagePoolId);
        if (proxy != null) {
            proxy.lockDbSave();
        }
    }

    public static void unlockDbSave(Guid storagePoolId) {
        IrsProxyData proxy = _irsProxyData.get(storagePoolId);
        if (proxy != null) {
            proxy.unlockDbSave();
        }
    }

    public static void Init() {

        TransactionSupport.executeInScope(TransactionScopeOption.Suppress, new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                for (storage_pool sp : DbFacade.getInstance().getStoragePoolDAO().getAll()) {
                    if (!_irsProxyData.containsKey(sp.getId())) {
                        _irsProxyData.put(sp.getId(), new IrsProxyData(sp.getId()));
                    }
                }
                return null;
            }
        });
    }

    public void RemoveIrsProxy() {
        _irsProxyData.get(getParameters().getStoragePoolId()).Dispose();
        _irsProxyData.remove(getParameters().getStoragePoolId());
    }

    protected static class IrsProxyData {
        // TODO the syncObj initial purposs was to lock the IrsBroker creation
        // but eventually because the IRS is singlethreaded and suppose to have
        // quite a load of requests,
        // In order to avoid unexpected behaviour we have used the syncObj to
        // lock each request to the IRS
        // and by that we caused a searialization of requests to the IRS.
        // This lock should be removed as soon as the IrsBroker is turned
        // multithreaded
        public Object syncObj = new Object();

        public static final String UpVdssInStoragePoolQuery = "STORAGE: status = UP and DATACENTER = {0}";
        private final String storagePoolRefreshJobId;
        private final java.util.HashSet<Guid> mTriedVdssList = new java.util.HashSet<Guid>();
        private Guid mCurrentVdsId;

        public Guid getCurrentVdsId() {
            return getIrsProxy() != null ? mCurrentVdsId : Guid.Empty;
        }

        public void setCurrentVdsId(Guid value) {
            mCurrentVdsId = (value == Guid.Empty) ? null : (Guid) value;
        }

        private String privatemCurrentIrsHost;

        public String getmCurrentIrsHost() {
            return privatemCurrentIrsHost;
        }

        public void setmCurrentIrsHost(String value) {
            privatemCurrentIrsHost = value;
        }

        private IIrsServer privatemIrsProxy;

        private IIrsServer getmIrsProxy() {
            return privatemIrsProxy;
        }

        private void setmIrsProxy(IIrsServer value) {
            privatemIrsProxy = value;
        }

        private int privatemIrsPort;

        private int getmIrsPort() {
            return privatemIrsPort;
        }

        private void setmIrsPort(int value) {
            privatemIrsPort = value;
        }

        private Guid _storagePoolId = new Guid();
        private String mIsoPrefix = "";

        public IrsProxyData(Guid storagePoolId) {
            _storagePoolId = storagePoolId;
            int storagePoolRefreshTime = Config.<Integer> GetValue(ConfigValues.StoragePoolRefreshTimeInSeconds);
            storagePoolRefreshJobId = SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this,
                    "_updatingTimer_Elapsed", new Class[0], new Object[0], storagePoolRefreshTime,
                    storagePoolRefreshTime, TimeUnit.SECONDS);
        }

        @OnTimerMethodAnnotation("_updatingTimer_Elapsed")
        public void _updatingTimer_Elapsed() {
            try {

                synchronized (syncObj) {
                    if (!_disposed) {
                        TransactionSupport.executeInScope(TransactionScopeOption.Suppress,
                                new TransactionMethod<Object>() {
                                    @Override
                                    public Object runInTransaction() {
                                        storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO()
                                                .get(_storagePoolId);
                                        if (storagePool != null
                                                && (storagePool.getstatus() == StoragePoolStatus.Up
                                                        || storagePool.getstatus() == StoragePoolStatus.Problematic || storagePool
                                                        .getstatus() == StoragePoolStatus.Contend)) {
                                            ProceedStoragePoolStats();
                                        }
                                        return null;
                                    }
                                });
                    }
                }
            } catch (java.lang.Exception ex) {
            }

        }

        private int _errorAttempts;

        @SuppressWarnings("unchecked")
        private void ProceedStoragePoolStats() {
            // ugly patch because vdsm doesnt check if host is spm on spm
            // operations
            VDSReturnValue result = null;
            if (mCurrentVdsId != null) {
                result = ResourceManager.getInstance().runVdsCommand(VDSCommandType.SpmStatus,
                        new SpmStatusVDSCommandParameters(mCurrentVdsId, _storagePoolId));
            }

            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(_storagePoolId);
            if (storagePool != null) {
                if (result == null
                        || !result.getSucceeded()
                        || (result.getSucceeded() && ((SpmStatusResult) result.getReturnValue()).getSpmStatus() != SpmStatus.SPM)) {
                    // update pool status to problematic until fence will happen
                    if (storagePool.getstatus() != StoragePoolStatus.Problematic
                            && storagePool.getstatus() != StoragePoolStatus.NotOperational) {
                        if (result != null && result.getVdsError() != null) {
                            ResourceManager
                                    .getInstance()
                                    .getEventListener()
                                    .StoragePoolStatusChange(_storagePoolId, StoragePoolStatus.Problematic,
                                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_WITH_ERROR,
                                            result.getVdsError().getCode());
                        } else {
                            ResourceManager
                                    .getInstance()
                                    .getEventListener()
                                    .StoragePoolStatusChange(_storagePoolId, StoragePoolStatus.Problematic,
                                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC,
                                            VdcBllErrors.ENGINE);
                        }
                    }

                    // if spm status didnt work or not spm and NOT NETWORK
                    // PROBLEM
                    // then cause failover with attempts
                    if (result != null && !(result.getExceptionObject() instanceof VDSNetworkException)) {
                        java.util.HashMap<Guid, AsyncTaskStatus> tasksList =
                                (java.util.HashMap<Guid, AsyncTaskStatus>) ResourceManager
                                        .getInstance()
                                        .runVdsCommand(VDSCommandType.HSMGetAllTasksStatuses,
                                                new VdsIdVDSCommandParametersBase(mCurrentVdsId)).getReturnValue();
                        // LINQ 29456
                        // if (tasksList == null || !(tasksList.Where(a =>
                        // a.Value.Status !=
                        // AsyncTaskStatusEnum.finished).Count() > 0))
                        boolean allTasksFinished = true;
                        if (tasksList != null) {
                            for (AsyncTaskStatus taskStatus : tasksList.values()) {
                                if (AsyncTaskStatusEnum.finished != taskStatus.getStatus()) {
                                    allTasksFinished = false;
                                    break;
                                }
                            }
                        }
                        if ((tasksList == null) || allTasksFinished) {
                            setmIrsProxy(null);
                            setmCurrentIrsHost(null);
                        } else {
                            if (_errorAttempts < Config.<Integer> GetValue(ConfigValues.SPMFailOverAttempts)) {
                                _errorAttempts++;
                                log.warnFormat("failed getting spm status for pool {0}:{1}, attempt number {2}",
                                        _storagePoolId, storagePool.getname(), _errorAttempts);
                            } else {
                                setmIrsProxy(null);
                                setmCurrentIrsHost(null);
                                _errorAttempts = 0;
                            }
                        }
                    }
                } else if (result != null
                        && result.getSucceeded()
                        && ((SpmStatusResult) result.getReturnValue()).getSpmStatus() == SpmStatus.SPM
                        && (storagePool.getstatus() == StoragePoolStatus.Problematic || storagePool.getstatus() == StoragePoolStatus.Contend)) {
                    // if recovered from network exception set back to up
                    DbFacade.getInstance().getStoragePoolDAO().updateStatus(storagePool.getId(),StoragePoolStatus.Up);
                    storagePool.setstatus(StoragePoolStatus.Up);
                    ResourceManager.getInstance()
                            .StoragePoolStatusChanged(storagePool.getId(), storagePool.getstatus());
                }
                GetStoragePoolInfoVDSCommandParameters tempVar = new GetStoragePoolInfoVDSCommandParameters(
                        _storagePoolId);
                tempVar.setIgnoreFailoverLimit(true);
                VDSReturnValue storagePoolInfoResult = ResourceManager.getInstance().runVdsCommand(
                        VDSCommandType.GetStoragePoolInfo, tempVar);
                if (storagePoolInfoResult.getSucceeded()) {
                    KeyValuePairCompat<storage_pool, java.util.List<storage_domains>> data =
                            (KeyValuePairCompat<storage_pool, java.util.List<storage_domains>>) storagePoolInfoResult
                                    .getReturnValue();
                    int masterVersion = data.getKey().getmaster_domain_version();
                    java.util.HashSet<Guid> domainsInVds = new java.util.HashSet<Guid>();
                    for (storage_domains domainData : data.getValue()) {
                        domainsInVds.add(domainData.getid());
                        ProceedStorageDomain(domainData, masterVersion, storagePool);
                    }
                    List<storage_domains> domainsInDb = DbFacade.getInstance().getStorageDomainDAO()
                            .getAllForStoragePool(_storagePoolId);

                    for (final storage_domains domainInDb : domainsInDb) {
                        if (domainInDb.getstorage_domain_type() != StorageDomainType.Master
                                && domainInDb.getstatus() != StorageDomainStatus.Locked
                                && !domainsInVds.contains(domainInDb.getid())) {
                            // domain not attached to pool anymore
                            TransactionSupport.executeInScope(TransactionScopeOption.Suppress,
                                    new TransactionMethod<Object>() {
                                        @Override
                                        public Object runInTransaction() {
                                            DbFacade.getInstance()
                                                    .getStoragePoolIsoMapDAO()
                                                    .remove(new StoragePoolIsoMapId(domainInDb.getid(),
                                                            _storagePoolId));
                                            return null;
                                        }
                                    });
                        }
                    }
                }

            }
        }

        private void ProceedStorageDomain(storage_domains data, int dataMasterVersion, storage_pool storagePool) {
            storage_domains storage_domain = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(data.getid(), _storagePoolId);
            storage_domain_static domainFromDb = null;
            storage_pool_iso_map domainPoolMap = null;

            if (storage_domain != null) {
                domainFromDb = storage_domain.getStorageStaticData();
                domainPoolMap = storage_domain.getStoragePoolIsoMapData();
                // If the domain is master in the DB
                if (domainFromDb.getstorage_domain_type() == StorageDomainType.Master && domainPoolMap != null
                        && domainPoolMap.getstatus() != StorageDomainStatus.Locked) {
                    // and the domain is not master in the VDSM
                    if (!((data.getstorage_domain_type() == StorageDomainType.Master) || (data.getstorage_domain_type() == StorageDomainType.Unknown))) {
                        reconstructMasterDomainNotInSync(data.getstorage_pool_id().getValue(),
                                domainFromDb.getId(),
                                "Mismatch between master in DB and VDSM",
                                MessageFormat.format("Master domain is not in sync between DB and VDSM. "
                                        + "Domain {0} marked as master in DB and not in the storage",
                                        domainFromDb.getstorage_name()));
                    } // or master in DB and VDSM but there is a version
                      // mismatch
                    else if (dataMasterVersion != storagePool.getmaster_domain_version()) {
                        reconstructMasterDomainNotInSync(data.getstorage_pool_id().getValue(),
                                domainFromDb.getId(),
                                "Mismatch between master version in DB and VDSM",
                                MessageFormat.format("Master domain version is not in sync between DB and VDSM. "
                                        + "Domain {0} marked as master, but the version in DB: {1} and in VDSM: {2}",
                                        domainFromDb.getstorage_name(),
                                        storagePool.getmaster_domain_version(),
                                        dataMasterVersion));
                    }
                }
                boolean statusChanged = false;
                if (domainPoolMap == null) {
                    data.setstorage_pool_id(_storagePoolId);
                    DbFacade.getInstance().getStoragePoolIsoMapDAO().save(data.getStoragePoolIsoMapData());
                    statusChanged = true;
                } else if (domainPoolMap.getstatus() != StorageDomainStatus.Locked
                        && domainPoolMap.getstatus() != data.getstatus()) {
                    DbFacade.getInstance().getStoragePoolIsoMapDAO().update(data.getStoragePoolIsoMapData());
                    if (data.getstatus() != null && data.getstatus() == StorageDomainStatus.InActive
                            && domainFromDb.getstorage_domain_type() == StorageDomainType.Master) {
                        storage_pool pool = DbFacade.getInstance().getStoragePoolDAO()
                                .get(domainPoolMap.getstorage_pool_id().getValue());
                        if (pool != null) {
                            DbFacade.getInstance().getStoragePoolDAO().updateStatus(pool.getId(),StoragePoolStatus.Maintanance);
                            pool.setstatus(StoragePoolStatus.Maintanance);
                            ResourceManager.getInstance().getEventListener()
                                    .StoragePoolStatusChanged(pool.getId(), StoragePoolStatus.Maintanance);
                        }
                    }
                    statusChanged = true;
                }
                // if status didn't change and still not active no need to
                // update dynamic data
                if (statusChanged || data.getstatus() == StorageDomainStatus.Active) {
                    DbFacade.getInstance().getStorageDomainDynamicDAO().update(data.getStorageDynamicData());
                    if (data.getavailable_disk_size() != null && data.getused_disk_size() != null) {
                        int freePercent = data.getStorageDynamicData().getfreeDiskPercent();
                        int freeDiskInGB = data.getStorageDynamicData().getfreeDiskInGB();
                        AuditLogType type = AuditLogType.UNASSIGNED;
                        boolean spaceThresholdMet =
                                freeDiskInGB <= Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB);
                        boolean percentThresholdMet =
                                freePercent <= Config.<Integer> GetValue(ConfigValues.FreeSpaceLow);
                        if (spaceThresholdMet && percentThresholdMet) {
                             type = AuditLogType.IRS_DISK_SPACE_LOW_ERROR;
                        } else {
                            if (spaceThresholdMet || percentThresholdMet) {
                                type = AuditLogType.IRS_DISK_SPACE_LOW;
                            }
                         }
                        if (type != AuditLogType.UNASSIGNED) {
                            AuditLogableBase logable = new AuditLogableBase();
                            logable.setStorageDomain(data);
                            logable.setStoragePoolId(_storagePoolId);
                            logable.AddCustomValue("DiskSpace", (data.getavailable_disk_size()).toString());
                            data.setstorage_name(domainFromDb.getstorage_name());
                            AuditLogDirector.log(logable, type);

                        }
                    }

                    Set<VdcBllErrors> alerts = data.getAlerts();
                    if (alerts != null && !alerts.isEmpty()) {

                        AuditLogableBase logable = new AuditLogableBase();
                        logable.setStorageDomain(data);
                        data.setstorage_name(domainFromDb.getstorage_name());
                        logable.setStoragePoolId(_storagePoolId);

                        for (VdcBllErrors alert : alerts) {
                            switch (alert) {
                            case VG_METADATA_CRITICALLY_FULL:
                                AuditLogDirector.log(logable, AuditLogType.STORAGE_ALERT_VG_METADATA_CRITICALLY_FULL);
                                break;
                            case SMALL_VG_METADATA :
                                AuditLogDirector.log(logable, AuditLogType.STORAGE_ALERT_SMALL_VG_METADATA);
                                break;
                            default:
                                log.errorFormat("Unrecognized alert for domain {0}(id = {1}): {2}",
                                        data.getstorage_name(),
                                        data.getid(),
                                        alert);
                                break;
                            }
                        }
                    }
                }

            } else {
                log.debugFormat("The domain with id {0} was not found in DB", data.getid());
            }
        }

        /**
         * Reconstructs the master domain when the old domain is not in sync.
         *
         * @param storagePoolId
         *            The storage pool id.
         * @param masterDomainId
         *            The master domain id.
         * @param exceptionMessage
         *            The message of the exception to throw.
         * @param logMessage
         *            The log message to write in the log.
         */
        private void reconstructMasterDomainNotInSync(Guid storagePoolId,
                Guid masterDomainId,
                String exceptionMessage,
                String logMessage) {
            if (duringReconstructMaster.compareAndSet(false, true)) {
                try {
                    log.warnFormat(logMessage);

                    AuditLogableBase logable = new AuditLogableBase(mCurrentVdsId);
                    logable.setStorageDomainId(masterDomainId);
                    AuditLogDirector.log(logable, AuditLogType.SYSTEM_MASTER_DOMAIN_NOT_IN_SYNC);

                    ResourceManager.getInstance()
                            .getEventListener()
                            .MasterDomainNotOperational(masterDomainId, storagePoolId);
                } finally {
                    duringReconstructMaster.set(false);
                }
                throw new IRSNoMasterDomainException(exceptionMessage);
            } else {
                log.debug("The pool is during reconstruct master, skipping handling problematic domains "
                        + _storagePoolId);
            }
        }

        public java.util.HashSet<Guid> getTriedVdssList() {
            return mTriedVdssList;
        }

        public void Init(VDS vds) {
            mCurrentVdsId = vds.getvds_id();
            setmIrsPort(vds.getport());
            setmCurrentIrsHost(vds.gethost_name());
        }

        public boolean Failover() {
            setmIrsProxy(null);
            setmCurrentIrsHost(null);
            boolean performFailover = false;
            if (mCurrentVdsId != null) {
                try {
                    VDSReturnValue statusResult = ResourceManager.getInstance().runVdsCommand(VDSCommandType.SpmStatus,
                            new SpmStatusVDSCommandParameters(mCurrentVdsId, _storagePoolId));
                    if (statusResult != null
                            && statusResult.getSucceeded()
                            && (((SpmStatusResult) statusResult.getReturnValue()).getSpmStatus() == SpmStatus.SPM || ((SpmStatusResult) statusResult
                                    .getReturnValue()).getSpmStatus() == SpmStatus.Contend)) {
                        performFailover = ResourceManager
                                .getInstance()
                                .runVdsCommand(VDSCommandType.SpmStop,
                                        new SpmStopVDSCommandParameters(mCurrentVdsId, _storagePoolId)).getSucceeded();
                    } else {
                        performFailover = true;
                    }
                } catch (java.lang.Exception ex) {
                    // try to failover to another host if failed to get spm
                    // status or stop spm
                    // (in case mCurrentVdsId has wrong id for some reason)
                    log.errorFormat("Could not get spm status on host {0} for spmStop.", getmCurrentIrsHost());
                    performFailover = true;
                }
            }

            if (performFailover) {
                log.infoFormat("Irs placed on server {0} failed. Proceed Failover", getmCurrentIrsHost());
                mTriedVdssList.add(mCurrentVdsId);
                return true;
            } else {
                log.errorFormat("IRS failover failed - cant allocate vds server");
                return false;
            }
        }

        public IIrsServer getIrsProxy() {
            if (getmIrsProxy() == null) {
                storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(_storagePoolId);
                // dont try to start spm on uninitialized pool
                if (storagePool.getstatus() != StoragePoolStatus.Uninitialized) {
                    Object result = TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            return gethostFromVds();
                        }
                    });

                    if (result != null) {
                        String host = result.toString();

                        // log.InfoFormat("Created IrsProxy , Call stack:{0}",
                        // b.toString());

                        int clientTimeOut = Config.<Integer> GetValue(ConfigValues.vdsTimeout) * 1000;
                        KeyValuePairCompat<IrsServerConnector, HttpClient> returnValue;
                        if (!Config.<Boolean> GetValue(ConfigValues.UseSecureConnectionWithServers)) {
                            returnValue = XmlRpcUtils.getHttpConnection(host, getmIrsPort(), clientTimeOut,
                                    IrsServerConnector.class);
                        } else {
                            returnValue = XmlRpcUtils.getHttpsConnection(host, getmIrsPort(), clientTimeOut,
                                    IrsServerConnector.class);
                            // setmIrsProxy(XmlRpcProxyGen.<IIrsServer>Create());
                            // String certFileName =
                            // Config.resolveCertificatePath();
                            // getmIrsProxy().setUrl("https://" + host + ":" +
                            // getmIrsPort());
                            // X509Certificate cert = new
                            // X509Certificate(certFileName,
                            // Config.<String>GetValue(ConfigValues.CertificatePassword));
                            // getmIrsProxy().getClientCertificates().add(cert);
                        }
                        setmIrsProxy(new IrsServerWrapper(returnValue.getKey(), returnValue.getValue()));
                        Class[] inputTypes = new Class[] { storage_pool.class, boolean.class };
                        Object[] inputParams = new Object[] { storagePool, _isSpmStartCalled };
                        // TODO use thread pool
                        SchedulerUtilQuartzImpl.getInstance().scheduleAOneTimeJob(this, "StorageEventOnTimer",
                                inputTypes, inputParams, 0, TimeUnit.MILLISECONDS);
                    }
                }
            }
            return getmIrsProxy();
        }

        @OnTimerMethodAnnotation("StorageEventOnTimer")
        public void StorageEventOnTimer(storage_pool storagePool, boolean _isSpmStartCalled) {
            try {
                if (DbFacade.getInstance().IsStoragePoolMasterUp(_storagePoolId)) {
                    ResourceManager.getInstance().getEventListener().StoragePoolUpEvent(storagePool, _isSpmStartCalled);
                }
            } catch (RuntimeException exp) {
                log.error("Error in StoragePoolUpEvent - ", exp);
            }
        }

        private List<VDS> GetVdssInPool() {
            Guid curVdsId = (mCurrentVdsId != null) ? mCurrentVdsId : Guid.Empty;
            // LINQ 31899
            // DbFacade.Instance.GetAllFrom_vds().Where( s => s.status ==
            // VDSStatus.Up && s.storage_pool_id == _storagePoolId &&
            // !TriedVdssList.Contains(s.vds_id) && s.vds_id != curVdsId);
            List<VDS> allVds = DbFacade.getInstance().getVdsDAO().getAll();
            List<VDS> vdsInPool = new ArrayList<VDS>();
            for (VDS vds : allVds) {
                if (VDSStatus.Up == vds.getstatus() && _storagePoolId.equals(vds.getstorage_pool_id())
                        && !mTriedVdssList.contains(vds.getvds_id()) && !vds.getvds_id().equals(curVdsId)) {
                    vdsInPool.add(vds);
                }
            }
            return vdsInPool;
        }

        private List<VDS> GetNonOperationalVdssInPool() {
            List<VDS> allVds = DbFacade.getInstance().getVdsDAO().getAll();
            List<VDS> nonOperationalvdsInPool = new ArrayList<VDS>();
            for (VDS vds : allVds) {
                if (VDSStatus.NonOperational == vds.getstatus() && _storagePoolId.equals(vds.getstorage_pool_id())) {
                    nonOperationalvdsInPool.add(vds);
                }
            }
            return nonOperationalvdsInPool;
        }

        /**
         * Returns True if there are other vdss in pool
         */
        public boolean getHasVdssForSpmSelection() {
            // LINQ 31899
            // GetVdssInPool().Count() > 0;
            return (GetVdssInPool().size() > 0);
        }

        private String gethostFromVds() {
            _isSpmStartCalled = false;
            String returnValue = null;
            Guid curVdsId = (mCurrentVdsId != null) ? mCurrentVdsId : Guid.Empty;
            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(_storagePoolId);

            if (storagePool == null) {
                log.infoFormat("hostFromVds::Finished elect spm, storage pool {0} was removed", _storagePoolId);
                return null;
            }

            List<VDS> vdsByPool = GetVdssInPool();

            // If VDS is in initialize status, wait for it to be up (or until
            // configurable timeout is reached)
            WaitForVdsIfIsInitializing(curVdsId);
            // update pool status to problematic while selecting spm
            StoragePoolStatus prevStatus = storagePool.getstatus();
            if (prevStatus != StoragePoolStatus.Problematic) {
                try {
                    ResourceManager
                            .getInstance()
                            .getEventListener()
                            .StoragePoolStatusChange(_storagePoolId, StoragePoolStatus.Problematic,
                                    AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_SEARCHING_NEW_SPM,
                                    VdcBllErrors.ENGINE, TransactionScopeOption.RequiresNew);
                } catch (RuntimeException ex) {
                    throw new IRSStoragePoolStatusException(ex);
                }
            }
            VDS selectedVds = null;
            SpmStatusResult spmStatus = null;
            // LINQ 29456
            // if ((vdsByPool.Count != 0))
            if (vdsByPool.size() != 0) {
                // get a random index within [0,size()-1]
                int randomIndex = (int) (Math.random() * (vdsByPool.size()));
                // LINQ 29456
                // vdsByPool.ToList()[rnd.Next(vdsByPool.Count())];
                selectedVds = vdsByPool.get(randomIndex);

            } else if (!curVdsId.equals(Guid.Empty) && !getTriedVdssList().contains(curVdsId)) {
                selectedVds = DbFacade.getInstance().getVdsDAO().get(curVdsId);
                if (selectedVds.getstatus() != VDSStatus.Up) {
                    selectedVds = null;
                }
            }
            if (selectedVds != null) {
                mCurrentVdsId = selectedVds.getvds_id();
                // Stores origin host id in case and will be needed to disconnect from storage pool
                Guid selectedVdsId = selectedVds.getvds_id();
                Integer selectedVdsSpmId = selectedVds.getvds_spm_id();

                VDSReturnValue returnValueFromVds = ResourceManager.getInstance().runVdsCommand(
                        VDSCommandType.SpmStatus,
                        new SpmStatusVDSCommandParameters(selectedVds.getvds_id(), _storagePoolId));
                spmStatus = (SpmStatusResult) returnValueFromVds.getReturnValue();
                if (spmStatus != null) {
                    boolean performedPoolConnect = false;
                    log.infoFormat("hostFromVds::selectedVds - {0}, spmStatus {1}, storage pool {2}",
                            selectedVds.getvds_name(), spmStatus.getSpmStatus().toString(), storagePool.getname());
                    if (spmStatus.getSpmStatus() == SpmStatus.Unknown_Pool) {
                        Guid masterId = DbFacade.getInstance().getStorageDomainDAO()
                                .getMasterStorageDomainIdForPool(_storagePoolId);
                        VDSReturnValue connectResult = ResourceManager.getInstance().runVdsCommand(
                                VDSCommandType.ConnectStoragePool,
                                new ConnectStoragePoolVDSCommandParameters(selectedVds.getvds_id(), _storagePoolId,
                                        selectedVds.getvds_spm_id(), masterId, storagePool.getmaster_domain_version()));
                        if (!connectResult.getSucceeded()
                                && connectResult.getExceptionObject() instanceof IRSNoMasterDomainException) {
                            throw connectResult.getExceptionObject();
                        } else if (!connectResult.getSucceeded()) {
                            // if connect to pool fails throw exception for
                            // failover
                            throw new IRSNonOperationalException("Could not connect host to Data Center(Storage issue)");
                        }
                        performedPoolConnect = true;
                        // refresh spmStatus result
                        spmStatus = (SpmStatusResult) ResourceManager
                                .getInstance()
                                .runVdsCommand(VDSCommandType.SpmStatus,
                                        new SpmStatusVDSCommandParameters(selectedVds.getvds_id(), _storagePoolId))
                                .getReturnValue();
                        log.infoFormat(
                                "hostFromVds::Connected host to pool - selectedVds - {0}, spmStatus {1}, storage pool {2}",
                                selectedVds.getvds_name(),
                                spmStatus.getSpmStatus().toString(),
                                storagePool.getname());
                    }
                    RefObject<VDS> tempRefObject = new RefObject<VDS>(selectedVds);
                    spmStatus = HandleSpmStatusResult(curVdsId, vdsByPool, storagePool, tempRefObject, spmStatus);
                    selectedVds = tempRefObject.argvalue;

                    if (selectedVds != null) {
                        RefObject<VDS> tempRefObject2 = new RefObject<VDS>(selectedVds);
                        RefObject<SpmStatusResult> tempRefObject3 = new RefObject<SpmStatusResult>(spmStatus);
                        returnValue = HandleSelectedVdsForSPM(storagePool, tempRefObject2, tempRefObject3, prevStatus);
                        selectedVds = tempRefObject2.argvalue;
                        spmStatus = tempRefObject3.argvalue;
                    } else {
                        mCurrentVdsId = null;
                    }
                    if (performedPoolConnect && selectedVds == null) {
                        // if could not start spm on this host and connected to
                        // pool here
                        // then disconnect
                        ResourceManager.getInstance().runVdsCommand(
                                VDSCommandType.DisconnectStoragePool,
                                new DisconnectStoragePoolVDSCommandParameters(selectedVdsId, _storagePoolId,
                                        selectedVdsSpmId));
                    }
                } else {

                    log.infoFormat("hostFromVds::selectedVds - {0}, spmStatus returned null!",
                            selectedVds.getvds_name());
                    if (returnValueFromVds.getExceptionObject() instanceof IRSNoMasterDomainException) {
                        throw returnValueFromVds.getExceptionObject();
                    }
                }
            }
            return returnValue;
        }

        private boolean _isSpmStartCalled;

        private String HandleSelectedVdsForSPM(storage_pool storagePool, RefObject<VDS> selectedVds,
                RefObject<SpmStatusResult> spmStatus, StoragePoolStatus prevStatus) {
            String returnValue = null;
            if (spmStatus.argvalue == null || spmStatus.argvalue.getSpmStatus() != SpmStatus.SPM) {
                MovePoolToProblematicInDB(storagePool, true);

                selectedVds.argvalue = null;
                log.infoFormat(
                        "spm start treatment ended and status is not SPM!!! status: {0} - setting selectedVds to null!",
                        spmStatus.argvalue.getSpmStatus().toString());
            } else {
                Init(selectedVds.argvalue);
                storagePool.setLVER(spmStatus.argvalue.getSpmLVER());
                storagePool.setspm_vds_id(selectedVds.argvalue.getvds_id());
                // if were problemtaic or not operational and succeeded to find
                // host move pool to up
                if (prevStatus != StoragePoolStatus.NotOperational && prevStatus != StoragePoolStatus.Problematic) {
                    storagePool.setstatus(prevStatus);
                } else {
                    storagePool.setstatus(StoragePoolStatus.Up);
                }
                DbFacade.getInstance().getStoragePoolDAO().update(storagePool);
                ResourceManager.getInstance().StoragePoolStatusChanged(storagePool.getId(), storagePool.getstatus());

                returnValue = selectedVds.argvalue.gethost_name();
                log.infoFormat("Initialize Irs proxy from vds: {0}", returnValue);
                AuditLogableBase logable = new AuditLogableBase(selectedVds.argvalue.getvds_id());
                logable.AddCustomValue("ServerIp", returnValue);
                AuditLogDirector.log(logable, AuditLogType.IRS_HOSTED_ON_VDS);
            }
            return returnValue;
        }

        /**
         * Waits for VDS if is initializing.
         *
         * @param curVdsId
         */
        private void WaitForVdsIfIsInitializing(Guid curVdsId) {
            if (curVdsId != Guid.Empty
                    && DbFacade.getInstance().getVdsDAO().get(curVdsId).getstatus() == VDSStatus.Initializing) {
                final int DELAY = 5;// 5 Sec
                int total = 0;
                Integer maxSecToWait = Config.GetValue(ConfigValues.WaitForVdsInitInSec);
                String vdsName = DbFacade.getInstance().getVdsDAO().get(curVdsId).getvds_name();
                while (total <= maxSecToWait
                        && DbFacade.getInstance().getVdsDAO().get(curVdsId).getstatus() == VDSStatus.Initializing) {
                    try {
                        Thread.sleep(DELAY * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // exit the while block
                        break;
                    }
                    total += DELAY;
                    log.infoFormat("Waiting to Host {0} to finish initialization for {1} Sec.", vdsName, total);
                }
            }
        }

        private static void MovePoolToProblematicInDB(storage_pool storagePool, boolean resetSpmInDB) {
            ResourceManager
                    .getInstance()
                    .getEventListener()
                    .StoragePoolStatusChange(storagePool.getId(), StoragePoolStatus.Problematic,
                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC, VdcBllErrors.ENGINE);

            if (resetSpmInDB) {
                storagePool.setspm_vds_id(null);
                DbFacade.getInstance().getStoragePoolDAO().update(storagePool);
            }
        }

        private SpmStatusResult HandleSpmStatusResult(Guid curVdsId,
                List<VDS> vdsByPool,
                final storage_pool storagePool,
                RefObject<VDS> selectedVds,
                SpmStatusResult spmStatus) {
            if (spmStatus.getSpmStatus() == SpmStatus.Free) {
                int vdsSpmIdToFence = -1;
                boolean startSpm = true;
                if (spmStatus.getSpmId() != -1) {
                    int spmId = spmStatus.getSpmId();
                    Guid spmVdsId = Guid.Empty;
                    VDS spmVds = null;
                    if (selectedVds.argvalue.getvds_spm_id() == spmId) {
                        spmVdsId = selectedVds.argvalue.getvds_id();
                    } else {
                        // LINQ 29456
                        // spmVds = vdsByPool.Where(vds => vds.vds_spm_id ==
                        // spmId).FirstOrDefault();
                        for (VDS tempVds : vdsByPool) {
                            if (tempVds.getvds_spm_id() == spmId) {
                                spmVds = tempVds;
                                break;
                            }
                        }
                        // if the host which is marked as SPM by the storage is
                        // non operational we want to find it as well
                        if (spmVds == null) {
                            List<VDS> nonOperationalVds = GetNonOperationalVdssInPool();
                            for (VDS tempVds : nonOperationalVds) {
                                if (tempVds.getvds_spm_id() == spmId) {
                                    spmVds = tempVds;
                                    break;
                                }
                            }
                        }

                        if (spmVds != null) {
                            spmVdsId = spmVds.getvds_id();
                        } else if (!curVdsId.equals(Guid.Empty)) {
                            VDS currentVds = DbFacade.getInstance().getVdsDAO().get(curVdsId);
                            if (currentVds != null && currentVds.getstatus() == VDSStatus.Up
                                    && currentVds.getvds_spm_id() != null && currentVds.getvds_spm_id().equals(spmId)) {
                                spmVdsId = curVdsId;
                                spmVds = currentVds;
                            }
                        }
                    }
                    try {
                        if (!spmVdsId.equals(Guid.Empty)) {
                            SpmStatusResult destSpmStatus = (SpmStatusResult) ResourceManager
                                    .getInstance()
                                    .runVdsCommand(VDSCommandType.SpmStatus,
                                            new SpmStatusVDSCommandParameters(spmVdsId, _storagePoolId))
                                    .getReturnValue();
                            log.infoFormat("SpmStatus on vds {0}: {1}", spmVdsId, destSpmStatus == null ? "NULL"
                                    : destSpmStatus.getSpmStatus().toString());
                            /**
                             * intentional unreachable code
                             */
                            if (destSpmStatus != null && destSpmStatus.getSpmStatus() == SpmStatus.SPM) {
                                if (spmVdsId != selectedVds.argvalue.getvds_id() && spmVds != null
                                        && spmVds.getstatus() == VDSStatus.Up) {
                                    selectedVds.argvalue = spmVds;
                                    startSpm = false;
                                    log.infoFormat("Using old spm server: {0}, no start needed", spmVds.getvds_name());
                                    return destSpmStatus;
                                }
                                // VDS is non-operational and SPM
                                else {
                                    log.warn("Host reports to be SPM but is not up. " + spmVdsId);
                                    vdsSpmIdToFence = spmStatus.getSpmId();
                                }
                            }
                            // if the host which is marked as SPM in the storage
                            // replies
                            // to SPMStatus with Unknown_Pool we don't need to
                            // fence it simply assume
                            // it is not SPM and continue.
                            else if (destSpmStatus == null
                                    || (destSpmStatus.getSpmStatus() != SpmStatus.Free && destSpmStatus.getSpmStatus() != SpmStatus.Unknown_Pool)) {
                                vdsSpmIdToFence = spmStatus.getSpmId();
                            }
                        } else {
                            log.errorFormat(
                                    "SPM Init: could not find reported vds or not up - pool:{0} vds_spm_id: {1}",
                                    storagePool.getname(), spmStatus.getSpmId());
                            vdsSpmIdToFence = spmStatus.getSpmId();
                        }
                    } catch (java.lang.Exception ex) {
                        vdsSpmIdToFence = spmStatus.getSpmId();
                    }
                }
                if (startSpm) {
                    vds_spm_id_map map = DbFacade.getInstance().getVdsSpmIdMapDAO().get(
                            _storagePoolId, vdsSpmIdToFence);
                    if (map != null) {
                        VDS vdsToFenceObject = DbFacade.getInstance().getVdsDAO().get(map.getId());
                        if (vdsToFenceObject != null) {
                            log.infoFormat("SPM selection - vds seems as spm {0}", vdsToFenceObject.getvds_name());
                            if (vdsToFenceObject.getstatus() == VDSStatus.NonResponsive) {
                                log.warn("spm vds is non responsive, stopping spm selection.");
                                selectedVds.argvalue = null;
                                return spmStatus;
                            } else {
                                // try to stop spm
                                VDSReturnValue spmStopReturnValue = ResourceManager.getInstance().runVdsCommand(
                                        VDSCommandType.SpmStop,
                                        new SpmStopVDSCommandParameters(vdsToFenceObject.getvds_id(), _storagePoolId));
                                // if spm stop succeeded no need to fence,
                                // continue with spm selection
                                if (spmStopReturnValue != null && spmStopReturnValue.getSucceeded()) {
                                    log.info("spm stop succeeded, continuing with spm selection");
                                }
                                // if spm stop failed for any reason we stop spm
                                // selection
                                else {
                                    log.warn("spm stop on spm failed, stopping spm selection!");
                                    selectedVds.argvalue = null;
                                    return spmStatus;
                                }
                            }
                        }
                    }
                    storagePool.setstatus(StoragePoolStatus.Contend);
                    storagePool.setspm_vds_id(selectedVds.argvalue.getvds_id());

                    TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            DbFacade.getInstance().getStoragePoolDAO().update(storagePool);
                            return null;
                        }
                    });

                    log.infoFormat("starting spm on vds {0}, storage pool {1}, prevId {2}, LVER {3}",
                            selectedVds.argvalue.getvds_name(), storagePool.getname(), spmStatus.getSpmId(),
                            spmStatus.getSpmLVER());
                    spmStatus = (SpmStatusResult) ResourceManager
                            .getInstance()
                            .runVdsCommand(
                                    VDSCommandType.SpmStart,
                                    new SpmStartVDSCommandParameters(selectedVds.argvalue.getvds_id(), _storagePoolId,
                                            spmStatus.getSpmId(), spmStatus.getSpmLVER(), storagePool
                                                    .getrecovery_mode(), vdsSpmIdToFence != -1, storagePool.getStoragePoolFormatType())).getReturnValue();
                    if (spmStatus != null && spmStatus.getSpmStatus() == SpmStatus.SPM) {
                        _isSpmStartCalled = true;
                    } else {
                        ResourceManager
                                .getInstance()
                                .getEventListener()
                                .StoragePoolStatusChange(storagePool.getId(),
                                        StoragePoolStatus.Problematic,
                                        AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC,
                                        VdcBllErrors.ENGINE,
                                        TransactionScopeOption.RequiresNew);
                        if (spmStatus != null) {
                            TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                                @Override
                                public Object runInTransaction() {
                                    storage_pool pool =
                                            DbFacade.getInstance().getStoragePoolDAO().get(storagePool.getId());
                                    pool.setspm_vds_id(null);
                                    DbFacade.getInstance().getStoragePoolDAO().update(pool);
                                    return null;
                                }
                            });
                        }
                        throw new IrsSpmStartFailedException();
                    }
                }
            }
            return spmStatus;
        }

        public String getIsoPrefix() {
            synchronized (syncObj) {
                if (mIsoPrefix == null || mIsoPrefix.length() == 0) {
                    try {
                        StoragePoolInfoReturnForXmlRpc returnValue = getIrsProxy().getStoragePoolInfo(
                                _storagePoolId.toString());
                        if (returnValue.mStoragePoolInfo.contains(IrsProperties.isoPrefix)) {
                            mIsoPrefix = returnValue.mStoragePoolInfo.getItem(IrsProperties.isoPrefix).toString();
                        }
                    } catch (java.lang.Exception ex) {
                        log.errorFormat("IrsBroker::IsoPrefix Failed to get IRS statistics.");
                    }
                }
                return mIsoPrefix;
            }
        }

        public void setIsoPrefix(String value) {
            mIsoPrefix = value;
        }

        public String getIsoDirectory() {
            String tempVar = getmCurrentIrsHost();
            return String.format("\\\\%1$s\\CD", ((tempVar != null) ? tempVar : gethostFromVds()));
        }

        public boolean getIsValid() {
            return getIrsProxy() != null;
        }

        public boolean getIsValidWithoutSpmStart() {
            return getmIrsProxy() != null;
        }

        public Guid getCurrentVdsIdWithoutSpm() {
            return mCurrentVdsId;
        }

        public void ResetIrs() {
            setmCurrentIrsHost(null);
            setmIrsProxy(null);
            mCurrentVdsId = null;
            storage_pool storagePool = DbFacade.getInstance().getStoragePoolDAO().get(_storagePoolId);
            if (storagePool != null) {
                storagePool.setspm_vds_id(null);
                DbFacade.getInstance().getStoragePoolDAO().update(storagePool);
            }
        }

        private final Map<Guid, HashSet<Guid>> _vdssInProblem = new HashMap<Guid, HashSet<Guid>>();
        private final Map<Guid, HashSet<Guid>> _domainsInProblem = new ConcurrentHashMap<Guid, HashSet<Guid>>();
        private final Map<Guid, String> _timers = new HashMap<Guid, String>();
        private AtomicBoolean duringReconstructMaster = new AtomicBoolean(false);
        private final Object _lockObject = new Object();
        private final ReentrantLock _lockObjForDbSave = new ReentrantLock();

        public void lockDbSave() {
            _lockObjForDbSave.lock();
        }

        public void unlockDbSave() {
            if (_lockObjForDbSave.isHeldByCurrentThread()) {
                _lockObjForDbSave.unlock();
            }
        }

        public boolean isDomainReportedInProblem(Guid domainId) {
            if (_domainsInProblem.containsKey(domainId)) {
                return true;
            }
            return false;
        }

        public void UpdateVdsDomainsData(final Guid vdsId, final String vdsName,
                final java.util.ArrayList<VDSDomainsData> data) {

            Set<Guid> domainsInProblems =
                    (Set<Guid>) TransactionSupport.executeInScope(TransactionScopeOption.Suppress,
                            new TransactionMethod<Set<Guid>>() {
                                @Override
                                public Set<Guid> runInTransaction() {

                                    Set<Guid> domainsInProblems = null;
                                    storage_pool storagePool =
                                            DbFacade.getInstance().getStoragePoolDAO().get(_storagePoolId);
                                    if (storagePool != null
                                            && (storagePool.getstatus() == StoragePoolStatus.Up || storagePool.getstatus() == StoragePoolStatus.Problematic)) {

                                        try {

                                            // build a list of all domains in pool
                                            // which are in status Active or Unknown
                                            Set<Guid> domainsInPool = new HashSet<Guid>(
                                                    DbFacade.getInstance().getStorageDomainStaticDAO().getAllIds(
                                                            _storagePoolId, StorageDomainStatus.Active));
                                            domainsInPool.addAll(DbFacade.getInstance().getStorageDomainStaticDAO().getAllIds(
                                                    _storagePoolId, StorageDomainStatus.Unknown));

                                            // build a list of all the domains in
                                            // pool (domainsInPool) that are not
                                            // visible by the host.
                                            List<Guid> domainsInPoolThatNonVisibleByVds = new ArrayList<Guid>();
                                            Set<Guid> dataDomainIds = new HashSet<Guid>();
                                            for (VDSDomainsData tempData : data) {
                                                dataDomainIds.add(tempData.getDomainId());
                                            }
                                            for (Guid tempDomainId : domainsInPool) {
                                                if (!dataDomainIds.contains(tempDomainId)) {
                                                    domainsInPoolThatNonVisibleByVds.add(tempDomainId);
                                                }
                                            }

                                            // build a list of domains that the host
                                            // reports as in problem (code!=0) or (code==0
                                            // && lastChecl >
                                            // ConfigValues.MaxStorageVdsTimeoutCheckSec)
                                            // and are contained in the Active or
                                            // Unknown domains in pool
                                            List<Guid> domainsSeenByVdsInProblem = new ArrayList<Guid>();
                                            for (VDSDomainsData tempData : data) {
                                                if (domainsInPool.contains(tempData.getDomainId())) {
                                                    if (tempData.getCode() != 0) {
                                                        domainsSeenByVdsInProblem.add(tempData.getDomainId());
                                                    } else if (tempData.getLastCheck() > Config
                                                            .<Double> GetValue(ConfigValues.MaxStorageVdsTimeoutCheckSec)) {
                                                        domainsSeenByVdsInProblem.add(tempData.getDomainId());
                                                    } else if (tempData.getDelay() > Config.<Double> GetValue(ConfigValues.MaxStorageVdsDelayCheckSec)) {
                                                        AuditLogableBase logable = new AuditLogableBase();
                                                        logable.setVdsId(vdsId);
                                                        logable.setStorageDomainId(tempData.getDomainId());
                                                        logable.AddCustomValue("Delay",
                                                                Double.toString(tempData.getDelay()));
                                                        AuditLogDirector.log(logable,
                                                                AuditLogType.VDS_DOMAIN_DELAY_INTERVAL);
                                                    }
                                                }
                                            }

                                            // build a list of all potential domains
                                            // in problem
                                            domainsInProblems = new HashSet<Guid>();
                                            domainsInProblems.addAll(domainsInPoolThatNonVisibleByVds);
                                            domainsInProblems.addAll(domainsSeenByVdsInProblem);

                                        } catch (RuntimeException ex) {
                                            log.error("error in UpdateVdsDomainsData", ex);
                                        }

                                    }
                                    return domainsInProblems;
                                }
                            });
            if (domainsInProblems != null) {
                synchronized (_lockObject) {
                    // during reconstruct master we do not want to update
                    // domains status
                    if (duringReconstructMaster.get()) {
                        log.debug("The pool is during reconstruct master, skipping handling problematic domains "
                                + _storagePoolId);
                        return;
                    }

                    updateProblematicVdsData(vdsId, vdsName, domainsInProblems);
                }
            }
        }

        private void updateProblematicVdsData(final Guid vdsId, final String vdsName, Set<Guid> domainsInProblems) {
            // for all recovered domains, update the
            // cache of _domainsInProblem and
            // _vdssInProblem
            if (_vdssInProblem.containsKey(vdsId)) {
                // DO NOT unify the next 2 for
                // statements because of
                // java.util.ConcurrentModificationException
                List<Guid> domainsRecoveredFromProblem = new ArrayList<Guid>();
                for (Guid domainId : _vdssInProblem.get(vdsId)) {
                    if (!domainsInProblems.contains(domainId)) {
                        domainsRecoveredFromProblem.add(domainId);
                    }
                }
                for (Guid domainRecovered : domainsRecoveredFromProblem) {
                    DomainRecoveredFromProblem(domainRecovered, vdsId, vdsName);
                }
            }

            // for all problematic domains
            // update cache of _domainsInProblem
            // and _vdssInProblem and add a new
            // timer for new domains in problem
            Set<Guid> domainsInProblemKeySet = _domainsInProblem.keySet();
            for (Guid domainId : domainsInProblems) {
                if (domainsInProblemKeySet.contains(domainId)) {
                    // existing domains in problem
                    UpdateDomainInProblemData(domainId, vdsId, vdsName);
                } else {
                    // new domains in problems
                    AddDomainInProblemData(domainId, vdsId, vdsName);
                }
            }
        }

        private void DomainRecoveredFromProblem(Guid domainId, Guid vdsId, String vdsName) {
            log.infoFormat("Domain {0} recovered from problem. vds: {1}", domainId, vdsName);
            ClearVds(vdsId, domainId);
            _domainsInProblem.get(domainId).remove(vdsId);
            if (_domainsInProblem.get(domainId).size() == 0) {
                log.infoFormat("Domain {0} has recovered from problem. No active host in the DC is reporting it as" +
                        " problematic, so clearing the domain recovery timer.", domainId);
                _domainsInProblem.remove(domainId);
                ClearTimer(domainId);
            }
        }

        private void AddDomainInProblemData(Guid domainId, Guid vdsId, String vdsName) {
            UpdateVdsInProblem(domainId, vdsId);
            _domainsInProblem.put(domainId, new java.util.HashSet<Guid>(java.util.Arrays.asList(vdsId)));
            log.warnFormat("domain {0} in problem. vds: {1}", domainId, vdsName);
            Class[] inputType = new Class[] { Guid.class };
            Object[] inputParams = new Object[] { domainId };
            String jobId = SchedulerUtilQuartzImpl.getInstance().scheduleAOneTimeJob(this, "OnTimer", inputType,
                    inputParams, Config.<Integer> GetValue(ConfigValues.StorageDomainFalureTimeoutInMinutes),
                    TimeUnit.MINUTES);
            ClearTimer(domainId);
            _timers.put(domainId, jobId);
        }

        @OnTimerMethodAnnotation("OnTimer")
        public void OnTimer(Guid domainId) {
            synchronized (_lockObject) {
                if (_domainsInProblem.containsKey(domainId)) {
                    log.info("starting ProcessDomainRecovery for domain " + domainId);
                    ProcessDomainRecovery(domainId);
                }
                _timers.remove(domainId);
            }
        }

        private void UpdateDomainInProblemData(Guid domainId, Guid vdsId, String vdsName) {
            log.debugFormat("domain {0} still in problem. vds: {1}", domainId, vdsName);
            _domainsInProblem.get(domainId).add(vdsId);
            UpdateVdsInProblem(domainId, vdsId);
        }

        private void UpdateVdsInProblem(Guid domainId, Guid vdsId) {
            if (!_vdssInProblem.containsKey(vdsId)) {
                _vdssInProblem.put(vdsId, new java.util.HashSet<Guid>());
            }
            _vdssInProblem.get(vdsId).add(domainId);
        }

        private void ProcessDomainRecovery(final Guid domainId) {
            TransactionSupport.executeInScope(TransactionScopeOption.Suppress, new TransactionMethod<Object>() {
                @Override
                public Object runInTransaction() {

                    // build a list of all the hosts in status UP in
                    // Pool.
                    List<Guid> vdssInPool = new ArrayList<Guid>();
                    List<VDS> allVds = DbFacade.getInstance().getVdsDAO().getAll();
                    for (VDS tempVDS : allVds) {
                        if (tempVDS.getstatus() == VDSStatus.Up && tempVDS.getstorage_pool_id().equals(_storagePoolId)) {
                            vdssInPool.add(tempVDS.getvds_id());
                        }
                    }

                    // build a list of all the hosts that did not report
                    // on this domain as in problem.
                    // Mark the above list as hosts we suspect are in
                    // problem.
                    java.util.Set<Guid> hostsThatReportedDomainAsInProblem = _domainsInProblem.get(domainId);
                    List<Guid> vdssInProblem = new ArrayList<Guid>();
                    for (Guid tempVDSId : vdssInPool) {
                        if (!hostsThatReportedDomainAsInProblem.contains(tempVDSId)) {
                            vdssInProblem.add(tempVDSId);
                        }
                    }

                    // If not All the hosts in status UP reported on
                    // this domain as in problem. We assume the problem
                    // is with the hosts
                    // that did report on a problem with this domain.
                    // (and not a problem with the domain itself).
                    storage_domain_static storageDomain = DbFacade.getInstance().getStorageDomainStaticDAO().get(domainId);
                    if (vdssInProblem.size() > 0) {
                        if (storageDomain.getstorage_domain_type() != StorageDomainType.ImportExport
                                && storageDomain.getstorage_domain_type() != StorageDomainType.ISO) {
                            // The domain is of type DATA and was
                            // reported as in problem.
                            // Moving all the hosts which reported on
                            // this domain as in problem to non
                            // operational.
                            for (Guid vdsId : _domainsInProblem.get(domainId)) {
                                VDS vds = DbFacade.getInstance().getVdsDAO().get(vdsId);
                                if (vds == null) {
                                    log.warnFormat(
                                            "vds {0} reported domain {1}:{2} - as in problem but cannot find vds in db!!",
                                            vdsId,
                                            domainId,
                                            storageDomain.getstorage_name());
                                } else if (vds.getstatus() != VDSStatus.Maintenance
                                        && vds.getstatus() != VDSStatus.NonOperational) {
                                    log.warnFormat(
                                            "vds {0} reported domain {1}:{2} as in problem, moving the vds to status NonOperational",
                                            vds.getvds_name(),
                                            domainId,
                                            storageDomain.getstorage_name());
                                    ResourceManager
                                            .getInstance()
                                            .getEventListener()
                                            .VdsNonOperational(vdsId, NonOperationalReason.STORAGE_DOMAIN_UNREACHABLE,
                                                    true, true, domainId);
                                } else {
                                    log.warnFormat(
                                            "vds {0} reported domain {1}:{2} as in problem, vds is in status {3}, no need to move to nonoperational",
                                            vds.getvds_name(),
                                            domainId,
                                            storageDomain.getstorage_name(),
                                            vds.getstatus());
                                }
                            }
                        } else {
                            log.warnFormat(
                                    "Storage domain {0} is not visible to one or more hosts. " +
                                    "Since the domain's type is {1}, hosts status will not be changed to non-operational",
                                    storageDomain.getstorage_name(),
                                    storageDomain.getstorage_domain_type());
                        }

                    } else { // Because all the hosts in status UP
                             // reported on this domain as in problem
                             // we assume the problem is with the
                             // Domain.
                        if (storageDomain.getstorage_domain_type() != StorageDomainType.Master) {
                            log.warnFormat("domain {0} was reported by all hosts in status UP as problematic. Moving the Domain to NonOperational.",
                                            domainId);
                            ResourceManager.getInstance()
                                    .getEventListener().StorageDomainNotOperational(domainId, _storagePoolId);
                        } else if (duringReconstructMaster.compareAndSet(false, true)) {
                            try {
                                ResourceManager.getInstance()
                                        .getEventListener().StorageDomainNotOperational(domainId, _storagePoolId);
                            } finally {
                                duringReconstructMaster.set(false);
                            }
                        } else {
                            log.warnFormat("domain {0} was reported by all hosts in status UP as problematic. But not moving the Domain to NonOperational. Because of is reconstract now",
                                                               domainId);
                            return null;
                        }
                    }

                    // clear from cache of _vdssInProblem and
                    // _domainsInProblem
                    clearDomainFromCache(domainId);
                    ClearTimer(domainId);
                    return null;
                }
            });
        }

        /**
         * clears the time for the given domain
         *
         * @param domainId
         *            - the domain to clean the timer for
         * @param clearDomain
         *            - if set to true we remove this domain from the problematic domains list
         */
        private void ClearTimer(Guid domainId) {
            String jobId = _timers.remove(domainId);
            if (jobId != null) {
                SchedulerUtilQuartzImpl.getInstance().deleteJob(jobId);
            }
        }

        /**
         * clears the problematic domain from the vdss that reported on this domain as problematic and from the domains
         * in problem
         *
         * @param domainId
         *            - the domain to clear cache for.
         */
        private void clearDomainFromCache(Guid domainId) {
            Set<Guid> vdsIds = _domainsInProblem.remove(domainId);
            if (vdsIds != null) {
                for (Guid vdsId : vdsIds) {
                    ClearVds(vdsId, domainId);
                }
            }
        }

        /**
         * deletes all the jobs for the domains in the pool and clears the problematic entities caches.
         */
        public void clearCache() {
            synchronized (_lockObject) {
                log.info("clearing cache for problematic entities in pool " + _storagePoolId);
                // clear lists
                _timers.clear();
                _domainsInProblem.clear();
                _vdssInProblem.clear();
                duringReconstructMaster.set(false);
            }
        }

        public void clearPoolTimers() {
            synchronized (_lockObject) {
                log.info("clear domain error-timers for pool " + _storagePoolId);
                duringReconstructMaster.set(true);
                for (String jobId : _timers.values()) {
                    try {
                        SchedulerUtilQuartzImpl.getInstance().deleteJob(jobId);
                    } catch (Exception e) {
                        log.warn("failed deleting job " + jobId);
                    }
                }
            }
        }

        /**
         * Remove a VDS entry from the cache, clearing the problematic domains for this VDS and their times if they
         * need to be cleaned. This is for a case when the VDS is switched to maintenance by the user, since we
         * need to clear it's cache data and timers, or else the cache will contain stale data (since the VDS is not
         * active anymore, it won't be connected to any of the domains).<br>
         * <br>
         * <b>Warning:</b> To avoid deadlocks, don't call this method from anywhere which is called somehow from within
         * <b>this</b> class, for instance when setting the VDS to Non-Operational.
         *
         * @param vdsId The ID of the VDS to remove from the cache.
         * @param vdsName The name of the VDS (for logging).
         */
        public void clearVdsFromCache(Guid vdsId, String vdsName) {
            log.infoFormat("Clearing cache of pool: {0} for problematic entities of VDS: {1}.",
                    _storagePoolId, vdsName);

            synchronized (_lockObject) {
                if (_vdssInProblem.containsKey(vdsId)) {
                    for (Guid domainId : new HashSet<Guid>(_vdssInProblem.get(vdsId))) {
                        DomainRecoveredFromProblem(domainId, vdsId, vdsName);
                    }
                }
            }
        }

        private void ClearVds(Guid vdsId, Guid domainId) {
            _vdssInProblem.get(vdsId).remove(domainId);
            if (_vdssInProblem.get(vdsId).isEmpty()) {
                _vdssInProblem.remove(vdsId);
            }
        }

        private boolean _disposed;

        public void Dispose() {
            synchronized (syncObj) {
                log.info("IrsProxyData::disposing");
                ResetIrs();
                SchedulerUtilQuartzImpl.getInstance().deleteJob(storagePoolRefreshJobId);
                if (privatemIrsProxy != null) {
                    XmlRpcUtils.shutDownConnection(((IrsServerWrapper) privatemIrsProxy).getHttpClient());
                }
                _disposed = true;
            }
        }

    }

    /**
     * Remove a VDS entry from the pool's IRS Proxy cache, clearing the problematic domains for this VDS and their
     * timers if they need to be cleaned. This is for a case when the VDS is switched to maintenance by the user, since
     * we need to clear it's cache data and timers, or else the cache will contain stale data (since the VDS is not
     * active anymore, it won't be connected to any of the domains).
     *
     * @param storagePoolId
     *            The ID of the storage pool to clean the IRS Proxy's cache for.
     * @param vdsId
     *            The ID of the VDS to remove from the cache.
     * @param vdsName
     *            The name of the VDS (for logging).
     */
    public static void clearVdsFromCache(Guid storagePoolId, Guid vdsId, String vdsName) {
        IrsProxyData irsProxyData = getIrsProxyData(storagePoolId);
        if (irsProxyData != null) {
            irsProxyData.clearVdsFromCache(vdsId, vdsName);
        }
    }

    /**
     * Return the IRS Proxy object for the given pool id. If there's no proxy data available, since there's no SPM
     * for the pool, then returns <code>null</code>.
     * @param storagePoolId The ID of the storage pool to get the IRS proxy for.
     * @return The IRS Proxy object, on <code>null</code> if no proxy data is available.
     */
    protected static IrsProxyData getIrsProxyData(Guid storagePoolId) {
        return _irsProxyData.get(storagePoolId);
    }

    protected IrsProxyData getCurrentIrsProxyData() {
        IrsProxyData returnValue;
        if (_irsProxyData.containsKey(getParameters().getStoragePoolId())) {
            returnValue = _irsProxyData.get(getParameters().getStoragePoolId());
        } else {
            _irsProxyData.put(getParameters().getStoragePoolId(), new IrsProxyData(getParameters().getStoragePoolId()));
            returnValue = _irsProxyData.get(getParameters().getStoragePoolId());
        }
        return returnValue;
    }

    private int _failoverCounter;

    private void failover() {
        if ((getParameters().getIgnoreFailoverLimit() || _failoverCounter < Config
                .<Integer> GetValue(ConfigValues.SpmCommandFailOverRetries) - 1)
                && getCurrentIrsProxyData().getHasVdssForSpmSelection() && getCurrentIrsProxyData().Failover()) {
            _failoverCounter++;
            ExecuteCommand();
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }

    public IrsBrokerCommand(P parameters) {
        super(parameters);

    }

    protected IIrsServer getIrsProxy() {
        return getCurrentIrsProxyData().getIrsProxy();
    }

    @Override
    protected void ExecuteVDSCommand() {
        synchronized (getCurrentIrsProxyData().syncObj) {
            try {

                if (getIrsProxy() != null) {
                    ExecuteIrsBrokerCommand();
                } else {
                    if (getVDSReturnValue().getVdsError() == null) {
                        getVDSReturnValue().setExceptionString("Cannot allocate IRS server");
                        VDSError tempVar = new VDSError();
                        tempVar.setCode(VdcBllErrors.IRS_REPOSITORY_NOT_FOUND);
                        tempVar.setMessage("Cannot allocate IRS server");
                        getVDSReturnValue().setVdsError(tempVar);
                    }
                    getVDSReturnValue().setSucceeded(false);
                }
            }
            catch (UndeclaredThrowableException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(new VDSError(VdcBllErrors.VDS_NETWORK_ERROR, ex.getMessage()));
                if (ExceptionUtils.getRootCause(ex) != null) {
                    logException(ExceptionUtils.getRootCause(ex));
                } else {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                failover();
            }
            catch (XmlRpcRunTimeException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                if (ex.isNetworkError()) {
                    log.errorFormat("IrsBroker::Failed::{0} - network exception.", getCommandName());
                    getVDSReturnValue().setSucceeded(false);
                } else {
                    log.errorFormat("IrsBroker::Failed::{0}", getCommandName());
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                    throw new IRSProtocolException(ex);
                }
            }
            catch (IrsErrorIrsDbMissing ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                log.errorFormat("IrsBroker::Failed::{0}", getCommandName());
                LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                failover();
            }
            catch (IRSNoMasterDomainException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                log.errorFormat("IrsBroker::Failed::{0}", getCommandName());
                log.errorFormat("Exception: {0}", ex.getMessage());

                if (getCurrentIrsProxyData().getHasVdssForSpmSelection()) {
                    failover();
                } else {
                    // LINQ 31899
                    // DbFacade.Instance.GetStorageDomainsByStoragePool(IrsBaseVDSCommandParametersValue.StoragePoolId).Where(a
                    // => a.storage_domain_type ==
                    // StorageDomainType.Master).FirstOrDefault();
                    storage_domain_static masterDomain = null;
                    List<storage_domain_static> storageDomainStaticList = DbFacade.getInstance()
                            .getStorageDomainStaticDAO().getAllForStoragePool(getParameters().getStoragePoolId());
                    for (storage_domain_static storageDomainStatic : storageDomainStaticList) {
                        if (storageDomainStatic.getstorage_domain_type() == StorageDomainType.Master) {
                            masterDomain = storageDomainStatic;
                            break;
                        }
                    }

                    if (masterDomain != null) {
                        if (getCurrentIrsProxyData().duringReconstructMaster.compareAndSet(false, true)) {
                            try {
                                ResourceManager.getInstance()
                                         .getEventListener()
                                         .MasterDomainNotOperational(masterDomain.getId(),
                                                 getParameters().getStoragePoolId());
                            } finally {
                                getCurrentIrsProxyData().duringReconstructMaster.set(false);
                            }
                        } else {
                            log.debug("The pool is during reconstruct master, skipping handling problematic domains "
                                    + getParameters().getStoragePoolId());
                        }
                    } else {
                        log.errorFormat(
                                "IrsBroker::IRSNoMasterDomainException:: Could not find master domain for pool {0} !!!",
                                getParameters().getStoragePoolId());
                    }
                }
            } catch (IRSNetworkException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                throw new IRSGenericException("Storage Domain server not found", ex);
            } catch (IRSUnicodeArgumentException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                throw new IRSGenericException("UNICODE characters are not supported.", ex);
            } catch (IRSStoragePoolStatusException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                throw ex;
            } catch (IRSNonOperationalException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                logException(ex);
                failover();
            } catch (IrsOperationFailedNoFailoverException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                logException(ex);
                getVDSReturnValue().setSucceeded(false);
            } catch (IRSErrorException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                getVDSReturnValue().setVdsError(ex.getVdsError());
                logException(ex);
                if (log.isDebugEnabled()) {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                failover();
            } catch (RuntimeException ex) {
                getVDSReturnValue().setExceptionString(ex.toString());
                getVDSReturnValue().setExceptionObject(ex);
                if (ex instanceof VDSExceptionBase) {
                    getVDSReturnValue().setVdsError(((VDSExceptionBase)ex).getVdsError());
                }
                if (ExceptionUtils.getRootCause(ex) != null &&
                        ExceptionUtils.getRootCause(ex) instanceof SocketException) {
                    logException(ExceptionUtils.getRootCause(ex));
                } else {
                    LoggedUtils.logError(log, LoggedUtils.getObjectId(this), this, ex);
                }
                // always failover because of changes in vdsm error, until we
                // realize what to do in each case:
                failover();
            } finally {
                if (_irsProxyData.containsKey(getParameters().getStoragePoolId())
                        && getCurrentIrsProxyData().getTriedVdssList().size() != 0) {
                    getCurrentIrsProxyData().getTriedVdssList().clear();
                }
                if (!getCurrentIrsProxyData().getIsValidWithoutSpmStart()) {
                    getCurrentIrsProxyData().setCurrentVdsId(Guid.Empty);
                }
            }
        }
    }

    protected void ExecuteIrsBrokerCommand() {
    }

    /**
     * Write the exception to the system log.
     *
     * @param ex
     *            Exception to log.
     */
    private void logException(Throwable ex) {
        log.errorFormat("IrsBroker::Failed::{0} due to: {1}", getCommandName(), ExceptionUtils.getMessage(ex));
    }

    protected static Guid[] convertStringGuidArray(String[] idArray) {
        Guid[] guidArray = new Guid[idArray.length];
        for (int idx = 0; idx < idArray.length; idx++) {
            guidArray[idx] = new Guid(idArray[idx]);
        }
        return guidArray;
    }

    public static Long AssignLongValue(XmlRpcStruct input, String name) {
        Long returnValue = null;
        if (input.containsKey(name)) {
            String stringValue = null;
            try {
                if (input.getItem(name) instanceof String) {
                    stringValue = (String) input.getItem(name);
                    returnValue = Long.parseLong(stringValue);
                }
            } catch (NumberFormatException nfe) {
                log.errorFormat("Failed to parse {0} value {1} to long", name, stringValue);
                returnValue = null;
            }
        }
        return returnValue;
    }

    private static LogCompat log = LogFactoryCompat.getLog(IrsBrokerCommand.class);
}
