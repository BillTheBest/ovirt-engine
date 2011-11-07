/**
 *
 */
package org.ovirt.engine.core.ldap;

import javax.naming.directory.SearchControls;

/**
 * RootDSEQueryInfo is a helper class to provide necessary information to perform RootDSE ldap queries
 */
public class RootDSEQueryInfo {

    public static final String ROOT_DSE_LDAP_QUERY = "(objectclass=*)";
    public static final String DEFAULT_NAMING_CONTEXT_RESULT_ATTRIBUTE = "defaultNamingContext";
    public static final String NAMING_CONTEXTS_RESULT_ATTRIBUTE = "NamingContexts";

    /**
     * Creates search controls object for the purpose of ROOT DSE query
     * @return
     */
    public static SearchControls createSearchControls() {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
        return searchControls;
    }

}
