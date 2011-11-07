package org.ovirt.engine.core.bll.command.utils;

import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class StorageDomainSpaceChecker {

    public static boolean isBelowThresholds(final storage_domains domain) {
        return isBelowSpaceThreshold(domain) && isBelowPctThreshold(domain);
    }

    private static boolean isBelowSpaceThreshold(final storage_domains domain) {
        Integer availableDiskSize = domain.getavailable_disk_size() == null ? 0 : domain.getavailable_disk_size();
        Integer usedDiskSize = domain.getused_disk_size() == null ? 0 : domain.getused_disk_size();
        double totalSize = availableDiskSize + usedDiskSize;
        return totalSize != 0
                && (availableDiskSize / totalSize) * (double) 100 > getLowDiskPercentThreshold();
    }

    private static boolean isBelowPctThreshold(final storage_domains domain) {
        storage_domain_dynamic dynamic = domain.getStorageDynamicData();
        return (dynamic != null
                               && dynamic.getfreeDiskInGB() > Config
                                       .<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB) && dynamic.getfreeDiskPercent() > Config
                                       .<Integer> GetValue(ConfigValues.FreeSpaceLow));
    }

    public static boolean hasSpaceForRequest(final storage_domains domain, final long requestedSize) {
        return hasSpace(domain, requestedSize) && hasSpacePct(domain, requestedSize);
    }

    private static boolean hasSpace(final storage_domains storageDomain, final long requestedSize) {
        return storageDomain.getavailable_disk_size() != null
                && storageDomain.getavailable_disk_size() - requestedSize >= getLowDiskSpaceThreshold();
    }

    private static boolean hasSpacePct(final storage_domains storageDomain, final long requestedSize) {
        Integer availableDiskSize =
                storageDomain.getavailable_disk_size() == null ? 0 : storageDomain.getavailable_disk_size();
        Integer usedDiskSize = storageDomain.getused_disk_size() == null ? 0 : storageDomain.getused_disk_size();
        double totalSize = availableDiskSize + usedDiskSize;
        return totalSize != 0
                && ((availableDiskSize - requestedSize) / totalSize) * 100 > getLowDiskPercentThreshold();
    }

    private static Integer getLowDiskPercentThreshold() {
        return Config.<Integer> GetValue(ConfigValues.FreeSpaceLow);
    }

    private static Integer getLowDiskSpaceThreshold() {
        return Config.<Integer> GetValue(ConfigValues.FreeSpaceCriticalLowInGB);
    }
}
