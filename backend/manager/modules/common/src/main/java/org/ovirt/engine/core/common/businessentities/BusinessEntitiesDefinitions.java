package org.ovirt.engine.core.common.businessentities;

public final class BusinessEntitiesDefinitions {

    // Data Center (storage_pool)
    public static final int DATACENTER_NAME_SIZE = 40;

    // CLUSTER (vds_groups)
    public static final int CLUSTER_NAME_SIZE = 40;
    public static final int CLUSTER_CPU_NAME_SIZE = 255;

    // VM (vm_statis)
    public static final int VM_NAME_SIZE = 255;

    // VM Pools (vm_pools)
    public static final int VM_POOL_NAME_SIZE = 255;
    public static final int VM_POOL_PARAMS = 200;

    // Templates (vm_templates)
    public static final int VM_TEMPLATE_NAME_SIZE = 40;

    // HOST (vds_static)
    public static final int HOST_NAME_SIZE = 255;
    public static final int HOST_HOSTNAME_SIZE = 255;
    public static final int HOST_IP_SIZE = 255;
    public static final int HOST_UNIQUE_ID_SIZE = 128;
    public static final int HOST_PM_USER_SIZE = 50;
    public static final int HOST_PM_PASSWD_SIZE = 50;
    public static final int HOST_PM_TYPE_SIZE = 20;

    // Network Interface (vm_interface)
    public static final int NETWORK_NAME_SIZE = 50;

    // Bookmark (bookmarks)
    public static final int BOOKMARK_NAME_SIZE = 40;
    public static final int BOOKMARK_VALUE_SIZE = 300;

    // Storage (storage_domain_static)
    public static final int STORAGE_SIZE = 250;
    public static final int STORAGE_NAME_SIZE = 250;

    // LUNS (luns)
    public static final int LUN_PHISICAL_VOLUME_ID = 50;
    public static final int LUN_ID = 50;
    public static final int LUN_VOLUME_GROUP_ID = 50;
    public static final int LUN_VENDOR_ID = 50;
    public static final int LUN_PRODUCT_ID = 50;

    // Roles (roles)
    public static final int ROLE_NAME_SIZE = 126;

    // Tags (tags)
    public static final int TAG_NAME_SIZE = 50;

    // Users (users)
    public static final int USER_GROUP_IDS_SIZE = 2048;
    public static final int USER_DEPARTMENT_SIZE = 255;
    public static final int USER_DESKTOP_DEVICE_SIZE = 255;
    public static final int USER_DOMAIN_SIZE = 255;
    public static final int USER_EMAIL_SIZE = 255;
    public static final int USER_NAME_SIZE = 255;
    public static final int USER_NOTE_SIZE = 255;
    public static final int USER_ROLE_SIZE = 255;
    public static final int USER_SURENAME_SIZE = 255;
    public static final int USER_ICON_PATH_SIZE = 255;
    public static final int USER_USER_NAME_SIZE = 255;

    // General descriptions
    public static final int GENERAL_NETWORK_ADDR_SIZE = 50;
    public static final int GENERAL_SUBNET_SIZE = 20;
    public static final int GENERAL_GATEWAY_SIZE = 20;
    public static final int GENERAL_MAC_ADDR_SIZE = 20;
    public static final int GENERAL_TIME_ZONE_SIZE = 40;
    public static final int GENERAL_DOMAIN_SIZE = 40;
    public static final int GENERAL_VERSION_SIZE = 40;
    public static final int GENERAL_MAX_SIZE = 4000;

}
