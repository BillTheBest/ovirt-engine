package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;

public class SearchObjectAutoCompleter extends SearchObjectsBaseAutoCompleter {
    private final java.util.HashMap<String, String[]> mJoinDictionary = new java.util.HashMap<String, String[]>();

    public SearchObjectAutoCompleter(boolean isDesktopsAllowed) {

        mVerbs.put(SearchObjects.VM_PLU_OBJ_NAME, SearchObjects.VM_PLU_OBJ_NAME);
        if (isDesktopsAllowed) {
            mVerbs.put(SearchObjects.VDC_POOL_PLU_OBJ_NAME, SearchObjects.VDC_POOL_PLU_OBJ_NAME);
        }
        mVerbs.put(SearchObjects.VDS_PLU_OBJ_NAME, SearchObjects.VDS_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.TEMPLATE_PLU_OBJ_NAME, SearchObjects.TEMPLATE_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.AUDIT_PLU_OBJ_NAME, SearchObjects.AUDIT_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_USER_PLU_OBJ_NAME, SearchObjects.VDC_USER_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME);

        buildCompletions();
        mVerbs.put(SearchObjects.VM_OBJ_NAME, SearchObjects.VM_OBJ_NAME);
        if (isDesktopsAllowed) {
            mVerbs.put(SearchObjects.VDC_POOL_OBJ_NAME, SearchObjects.VDC_POOL_OBJ_NAME);
        }
        mVerbs.put(SearchObjects.VDS_OBJ_NAME, SearchObjects.VDS_OBJ_NAME);
        mVerbs.put(SearchObjects.TEMPLATE_OBJ_NAME, SearchObjects.TEMPLATE_OBJ_NAME);
        mVerbs.put(SearchObjects.AUDIT_OBJ_NAME, SearchObjects.AUDIT_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_USER_OBJ_NAME, SearchObjects.VDC_USER_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.VDC_CLUSTER_OBJ_NAME);

        // vms - vds
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s", SearchObjects.VM_OBJ_NAME, SearchObjects.VDS_OBJ_NAME),
                new String[] { "run_on_vds", "vds_id" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s", SearchObjects.VM_OBJ_NAME, SearchObjects.VDS_OBJ_NAME),
                new String[] { "vds_id", "run_on_vds" });

        // vms - vmt
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s", SearchObjects.VM_OBJ_NAME, SearchObjects.TEMPLATE_OBJ_NAME),
                new String[] { "vmt_guid", "vmt_guid" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s", SearchObjects.VM_OBJ_NAME, SearchObjects.TEMPLATE_OBJ_NAME),
                new String[] { "vmt_guid", "vmt_guid" });

        // vms - users
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s", SearchObjects.VM_OBJ_NAME, SearchObjects.VDC_USER_OBJ_NAME),
                new String[] { "vm_guid", "vm_guid" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s", SearchObjects.VM_OBJ_NAME, SearchObjects.VDC_USER_OBJ_NAME),
                new String[] { "vm_guid", "vm_guid" });

        // vms - audit
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s", SearchObjects.VM_OBJ_NAME, SearchObjects.AUDIT_OBJ_NAME),
                new String[] { "vm_guid", "vm_id" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s", SearchObjects.VM_OBJ_NAME, SearchObjects.AUDIT_OBJ_NAME),
                new String[] { "vm_id", "vm_guid" });

        // vms - storage domain
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s", SearchObjects.VM_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME),
                new String[] { "storage_id", "id" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s", SearchObjects.VM_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME),
                new String[] { "id", "storage_id" });

        // templates - storage domain
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s", SearchObjects.TEMPLATE_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME),
                new String[] { "storage_id", "id" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s", SearchObjects.TEMPLATE_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME),
                new String[] { "id", "storage_id" });

        // vds - storage domain
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s", SearchObjects.VDS_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME),
                new String[] { "storage_id", "id" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s", SearchObjects.VDS_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME),
                new String[] { "id", "storage_id" });

        // cluster - storage domain
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s", SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME),
                new String[] { "storage_id", "id" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s", SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME),
                new String[] { "id", "storage_id" });

        // vds - audit
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s", SearchObjects.VDS_OBJ_NAME, SearchObjects.AUDIT_OBJ_NAME),
                new String[] { "vds_id", "vds_id" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s", SearchObjects.VDS_OBJ_NAME, SearchObjects.AUDIT_OBJ_NAME),
                new String[] { "vds_id", "vds_id" });

        // users - audit
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s",
                SearchObjects.VDC_USER_OBJ_NAME,
                SearchObjects.AUDIT_OBJ_NAME),
                new String[] { "user_id", "user_id" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s",
                SearchObjects.VDC_USER_OBJ_NAME,
                SearchObjects.AUDIT_OBJ_NAME),
                new String[] { "user_id", "user_id" });

        // Datacenter(Storage_pool) - Cluster(vds group)
        mJoinDictionary
                .put(StringFormat.format("%1$s.%2$s", SearchObjects.VDC_STORAGE_POOL_OBJ_NAME,
                        SearchObjects.VDC_CLUSTER_OBJ_NAME), new String[] { "id", "storage_pool_id" });
        mJoinDictionary
                .put(StringFormat.format("%2$s.%1$s", SearchObjects.VDC_STORAGE_POOL_OBJ_NAME,
                        SearchObjects.VDC_CLUSTER_OBJ_NAME), new String[] { "storage_pool_id", "id" });

        // Datacenter(Storage_pool) - Storage Domain
        mJoinDictionary
                .put(StringFormat.format("%1$s.%2$s", SearchObjects.VDC_STORAGE_POOL_OBJ_NAME,
                        SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME), new String[] { "id", "storage_pool_id" });
        mJoinDictionary
                .put(StringFormat.format("%2$s.%1$s", SearchObjects.VDC_STORAGE_POOL_OBJ_NAME,
                        SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME), new String[] { "storage_pool_id", "id" });

        // audit - cluster
        mJoinDictionary.put(StringFormat.format("%1$s.%2$s", SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.AUDIT_OBJ_NAME),
                new String[] { "vds_group_id", "vds_group_id" });
        mJoinDictionary.put(StringFormat.format("%2$s.%1$s", SearchObjects.VDC_CLUSTER_OBJ_NAME, SearchObjects.AUDIT_OBJ_NAME),
                new String[] { "vds_group_id", "vds_group_id" });

    }

    public IAutoCompleter getCrossRefAutoCompleter(String obj) {
        if (obj == null) {
            return null;
        }
        // VB & C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
        // string member and was converted to Java 'if-else' logic:
        // switch (obj)
        // ORIGINAL LINE: case SearchObjects.AUDIT_OBJ_NAME:
        if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            return new AuditCrossRefAutoCompleter();

        }
        // ORIGINAL LINE: case SearchObjects.TEMPLATE_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            return new TemplateCrossRefAutoCompleter();

        }
        // ORIGINAL LINE: case SearchObjects.VDC_USER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            return new UserCrossRefAutoCompleter();

        }
        // ORIGINAL LINE: case SearchObjects.VDS_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            return new VdsCrossRefAutoCompleter();

        }
        // ORIGINAL LINE: case SearchObjects.VM_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            return new VmCrossRefAutoCompleter();

        }
        // ORIGINAL LINE: case SearchObjects.VDC_CLUSTER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            return new ClusterCrossRefAutoCompleter();

        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            return new StoragePoolCrossRefAutoCompleter();
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            return new StorageDomainCrossRefAutoCompleter();

            // no need for empty case before default: case
            // SearchObjects.VDC_POOL_OBJ_NAME:
            // no need for empty case before default: case
            // SearchObjects.VDC_POOL_PLU_OBJ_NAME:
        } else {
            return null;
        }

    }

    public boolean isCrossReferece(String text, String obj) {
        IAutoCompleter completer = getCrossRefAutoCompleter(obj);
        boolean retval = false;
        if (completer != null) {
            retval = completer.validate(text);
        }
        return retval;
    }

    public String getInnerJoin(String searchObj, String crossRefObj) {
        String[] JoinKey = mJoinDictionary.get(StringFormat.format("%1$s.%2$s", searchObj, crossRefObj));
        String crossRefTable = getRelatedTableName(crossRefObj);
        String searchObjTable = getRelatedTableName(searchObj);

        return StringFormat.format(" LEFT OUTER JOIN %3$s ON %1$s.%2$s=%3$s.%4$s ", searchObjTable, JoinKey[0],
                crossRefTable, JoinKey[1]);
    }

    public IConditionFieldAutoCompleter getFieldAutoCompleter(String obj) {
        IConditionFieldAutoCompleter retval = null;
        if (obj == null) {
            return null;
        }
        // VB & C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
        // string member and was converted to Java 'if-else' logic:
        // switch (obj)
        // ORIGINAL LINE: case SearchObjects.VDS_OBJ_NAME:
        if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = new VdsConditionFieldAutoCompleter();
        }
        // ORIGINAL LINE: case SearchObjects.VM_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = new VmConditionFieldAutoCompleter();
        }
        // ORIGINAL LINE: case SearchObjects.TEMPLATE_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = new VmTemplateConditionFieldAutoCompleter();
        }
        // ORIGINAL LINE: case SearchObjects.AUDIT_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            retval = new AuditLogConditionFieldAutoCompleter();
        }
        // ORIGINAL LINE: case SearchObjects.VDC_USER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = new VdcUserConditionFieldAutoCompleter();
        }
        // ORIGINAL LINE: case SearchObjects.VDC_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_POOL_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_POOL_PLU_OBJ_NAME)) {
            retval = new PoolConditionFieldAutoCompleter();
        }
        // ORIGINAL LINE: case SearchObjects.VDC_CLUSTER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = new ClusterConditionFieldAutoCompleter();
        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            retval = new StoragePoolFieldAutoCompleter();
        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = new StorageDomainFieldAutoCompleter();

        } else {
        }
        return retval;
    }

    public String getRelatedTableNameWithOutTags(String obj) {
        String retval;
        if (obj == null) {
            return null;
        }
        // VB & C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
        // string member and was converted to Java 'if-else' logic:
        // switch (obj)
        // ORIGINAL LINE: case SearchObjects.VDC_USER_OBJ_NAME:
        if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = "vdc_users";

        }
        // ORIGINAL LINE: case SearchObjects.VM_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = "vms";

        }
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = "vm_templates_view";
        }
        // ORIGINAL LINE: case SearchObjects.VDS_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = "vds";

        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = "vds_groups";
        }
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            retval = "storage_pool";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = "storage_domains_without_storage_pools";

        } else {
            retval = getRelatedTableName(obj);
        }
        return retval;
    }

    public String getRelatedTableName(String obj) {
        String retval = null;
        if (obj == null) {
            return retval;
        }
        // VB & C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
        // string member and was converted to Java 'if-else' logic:
        // switch (obj)
        // ORIGINAL LINE: case SearchObjects.VDS_OBJ_NAME:
        if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = "vds_with_tags";
        }
        // ORIGINAL LINE: case SearchObjects.VM_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = "vms_with_tags";
        }
        // ORIGINAL LINE: case SearchObjects.TEMPLATE_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = "vm_templates_storage_domain";
        }
        // ORIGINAL LINE: case SearchObjects.AUDIT_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            retval = "audit_log";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_USER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = "vdc_users_with_tags";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_POOL_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_POOL_PLU_OBJ_NAME)) {
            retval = "vm_pools_full_view";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_CLUSTER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = "vds_groups_storage_domain";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            retval = "storage_pool_with_storage_domain";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = "storage_domains_with_hosts_view";

        } else {
        }
        return retval;
    }

    public String getPrimeryKeyName(String obj) {
        String retval = null;
        // VB & C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
        // string member and was converted to Java 'if-else' logic:
        // switch (obj)
        // ORIGINAL LINE: case SearchObjects.VDS_OBJ_NAME:
        if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = "vds_id";
        }
        // ORIGINAL LINE: case SearchObjects.VM_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = "vm_guid";
        }
        // ORIGINAL LINE: case SearchObjects.TEMPLATE_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = "vmt_guid";
        }
        // ORIGINAL LINE: case SearchObjects.AUDIT_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            retval = "audit_log_id";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_USER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = "user_id";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_POOL_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_POOL_PLU_OBJ_NAME)) {
            retval = "vm_pool_id";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_CLUSTER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = "vds_group_id";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            retval = "id";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = "id";

        } else {
        }
        return retval;
    }

    public IAutoCompleter getFieldRelationshipAutoCompleter(String obj, String fieldName) {
        IAutoCompleter retval = null;
        IConditionFieldAutoCompleter curConditionFieldAC = getFieldAutoCompleter(obj);
        if (curConditionFieldAC != null) {
            retval = curConditionFieldAC.getFieldRelationshipAutoCompleter(fieldName);
        }
        return retval;
    }

    public IAutoCompleter getObjectRelationshipAutoCompleter(String obj) {
        IAutoCompleter retval;
        retval = new StringConditionRelationAutoCompleter();
        return retval;
    }

    public IConditionValueAutoCompleter getFieldValueAutoCompleter(String obj, String fieldName) {
        IConditionValueAutoCompleter retval = null;
        IConditionFieldAutoCompleter curConditionFieldAC = getFieldAutoCompleter(obj);
        if (curConditionFieldAC != null) {
            retval = curConditionFieldAC.getFieldValueAutoCompleter(fieldName);
        }
        return retval;
    }

    public String getDefaultSort(String obj) {
        String retval = "";
        if (obj == null) {
            return retval;
        }
        // VB & C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
        // string member and was converted to Java 'if-else' logic:
        // switch (obj)
        // ORIGINAL LINE: case SearchObjects.VDS_OBJ_NAME:
        if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = "vds_name ASC ";
        }
        // ORIGINAL LINE: case SearchObjects.VM_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = "vm_name ASC ";
        }
        // ORIGINAL LINE: case SearchObjects.AUDIT_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            retval = "audit_log_id DESC ";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_USER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = "name ASC ";
        }
        // ORIGINAL LINE: case SearchObjects.TEMPLATE_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = "name ASC ";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_POOL_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_POOL_PLU_OBJ_NAME)) {
            retval = "vm_pool_name ASC ";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)) {
            retval = "name ASC ";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = "storage_name ASC ";
        }
        // ORIGINAL LINE: case SearchObjects.VDC_CLUSTER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = "name ASC ";
        } else {
        }
        return retval;
    }
}
