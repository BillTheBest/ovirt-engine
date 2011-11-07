package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.LongCompat;
import org.ovirt.engine.core.compat.NumberStyles;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class MacPoolManager {

    private static final MacPoolManager _instance = new MacPoolManager();

    static {
    }

    public static MacPoolManager getInstance() {
        return _instance;
    }

    private final java.util.ArrayList<String> mAvailableMacs = new java.util.ArrayList<String>();
    private final java.util.ArrayList<String> mAllocatedMacs = new java.util.ArrayList<String>();
    private final java.util.ArrayList<String> mAllocatedCustomMacs = new java.util.ArrayList<String>();
    private final Object mLocObj = new Object();
    private boolean mInitialized = false;

    public void initialize() {
        synchronized (mLocObj) {

            List<VM> vms = DbFacade.getInstance().getVmDAO().getAll();
            String ranges = Config.<String> GetValue(ConfigValues.MacPoolRanges);
            if (!StringHelper.EqOp(ranges, "")) {
                try {
                    initRanges(ranges);
                } catch (MacPoolExceededMaxException e) {
                    log.errorFormat("MAC Pool range exceeded maximum number of mac pool addressed. Please check Mac Pool configuration.");
                }
            }
            for (VM vm : vms) {
                List<VmNetworkInterface> interfaces = DbFacade.getInstance()
                        .getVmNetworkInterfaceDAO().getAllForVm(vm.getvm_guid());
                for (VmNetworkInterface iface : interfaces) {
                    AddMac(iface.getMacAddress());
                }
            }
            mInitialized = true;
        }
    }

    private void initRanges(String ranges) {
        String[] rangesArray = ranges.split("[,]", -1);
        for (String range : rangesArray) {
            String[] startendArray = range.split("[-]", -1);
            if (startendArray.length == 2) {
                if (!initRange(startendArray[0], startendArray[1])) {
                    log.errorFormat("Failed to initialize Mac Pool range. Please fix Mac Pool range: {0}", range);
                }
            } else {
                log.errorFormat("Failed to initialize Mac Pool range. Please fix Mac Pool range: {0}", range);

            }
        }
        if (mAvailableMacs.isEmpty()) {
            throw new VdcBLLException(VdcBllErrors.MAC_POOL_INITIALIZATION_FAILED);
        }
    }

    private String ParseRangePart(String start) {
        StringBuilder builder = new StringBuilder();
        for (String part : start.split("[:]", -1)) {
            String tempPart = part.trim();
            if (tempPart.length() == 1) {
                builder.append('0');
            } else if (tempPart.length() > 2) {
                return null;
            }
            builder.append(tempPart);
        }
        return builder.toString();
    }

    private boolean initRange(String start, String end) {
        String parsedRangeStart = ParseRangePart(start);
        String parsedRangeEnd = ParseRangePart(end);
        if (parsedRangeEnd == null || parsedRangeStart == null) {
            return false;
        }
        long startNum = LongCompat.parseLong(ParseRangePart(start), NumberStyles.HexNumber);
        long endNum = LongCompat.parseLong(ParseRangePart(end), NumberStyles.HexNumber);
        if (startNum > endNum) {
            // throw new
            // VdcBLLException(VdcBllErrors.MAC_POOL_INITIALIZATION_FAILED);
            return false;
        }
        for (long i = startNum; i <= endNum; i++) {
            String value = String.format("%x", i);
            if (value.length() > 12) {
                // throw new
                // VdcBLLException(VdcBllErrors.MAC_POOL_INITIALIZATION_FAILED);
                return false;
            } else if (value.length() < 12) {
                value = StringHelper.padLeft(value, 12, '0');
            }
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < value.length(); j += 2) {
                builder.append(value.substring(j, j + 2));
                builder.append(":");
            }
            value = builder.toString();
            value = value.substring(0, value.length() - 1);
            if (!mAvailableMacs.contains(value)) {
                mAvailableMacs.add(value);
            }
            if (mAvailableMacs.size() > Config.<Integer> GetValue(ConfigValues.MaxMacsCountInPool)) {
                throw new MacPoolExceededMaxException();
            }
        }
        return true;
    }

    public void allocateNewMac(RefObject<String> mac) {
        log.info("MacPoolManager::allocateNewMac entered");
        synchronized (mLocObj) {
            if (!mInitialized) {
                throw new VdcBLLException(VdcBllErrors.MAC_POOL_NOT_INITIALIZED);
            }
            if (mAvailableMacs.isEmpty()) {
                throw new VdcBLLException(VdcBllErrors.MAC_POOL_NO_MACS_LEFT);
            }
            java.util.Iterator<String> my = mAvailableMacs.iterator();
            my.hasNext();
            mac.argvalue = my.next();
            CommitNewMac(mac.argvalue);
        }
        log.infoFormat("MacPoolManager::allocateNewMac allocated mac = '{0}", mac.argvalue);
    }

    private boolean CommitNewMac(String mac) {
        mAvailableMacs.remove(mac);
        mAllocatedMacs.add(mac);
        if (mAvailableMacs.isEmpty()) {
            AuditLogableBase logable = new AuditLogableBase();
            AuditLogDirector.log(logable, AuditLogType.MAC_POOL_EMPTY);
            return false;
        }
        return true;
    }

    public int getavailableMacsCount() {
        return mAvailableMacs.size();
    }

    public void freeMac(String mac) {
        log.infoFormat("MacPoolManager::freeMac(mac = '{0}') - entered", mac);
        synchronized (mLocObj) {
            if (!mInitialized) {
                throw new VdcBLLException(VdcBllErrors.MAC_POOL_NOT_INITIALIZED);
            }
            if (mAllocatedCustomMacs.contains(mac)) {
                mAllocatedCustomMacs.remove(mac);
            } else if (mAllocatedMacs.contains(mac)) {
                mAllocatedMacs.remove(mac);
                mAvailableMacs.add(mac);
            }
        }
    }

    /**
     * Add user define mac address Function return false if the mac is in use
     *
     * @param mac
     * @return
     */
    public boolean AddMac(String mac) {
        boolean retVal = true;
        synchronized (mLocObj) {
            if (mAllocatedMacs.contains(mac)) {
                retVal = false;
            } else {
                if (mAvailableMacs.contains(mac)) {
                    retVal = CommitNewMac(mac);
                } else if (mAllocatedCustomMacs.contains(mac)) {
                    retVal = false;
                } else {
                    mAllocatedCustomMacs.add(mac);
                }
            }
        }
        return retVal;
    }

    public boolean IsMacInUse(String mac) {
        return mAllocatedMacs.contains(mac) || mAllocatedCustomMacs.contains(mac);
    }

    private static LogCompat log = LogFactoryCompat.getLog(MacPoolManager.class);
}
