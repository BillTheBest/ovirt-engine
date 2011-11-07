package org.ovirt.engine.core.searchbackend;

public class ClusterCrossRefAutoCompleter extends SearchObjectsBaseAutoCompleter {
    public ClusterCrossRefAutoCompleter() {
        mVerbs.put(SearchObjects.VDC_STORAGE_POOL_OBJ_NAME, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME);
        mVerbs.put(SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME);
        buildCompletions();
    }
}
