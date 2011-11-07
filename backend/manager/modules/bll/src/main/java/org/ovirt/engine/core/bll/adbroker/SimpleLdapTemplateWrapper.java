/**
 *
 */
package org.ovirt.engine.core.bll.adbroker;

import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.core.support.DirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SimpleDirContextAuthenticationStrategy;

/**
 *
 */
public class SimpleLdapTemplateWrapper extends LDAPTemplateWrapper {

    /**
     * @param contextSource
     */
    public SimpleLdapTemplateWrapper(LdapContextSource contextSource, String userName, String password, String path) {
        super(contextSource, userName, password, path);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void search(String baseDN, String filter, String displayFilter, SearchControls searchControls, NameClassPairCallbackHandler handler) {
        searchControls.setCountLimit(1000); //SIMPLE mode will limit have a limitation of 1000 results - as this is used
        //only by development, this is a reasonable limitation
        pagedSearch(baseDN,filter, displayFilter, searchControls, handler);

    }

    @Override
    protected DirContextAuthenticationStrategy buildContextAuthenticationStategy() {
        return new SimpleDirContextAuthenticationStrategy();
    }

    @Override
    public void adjustUserName(LdapProviderType ldapProviderType) {
        StringBuilder userDNSb = new StringBuilder();

        if ( ldapProviderType.equals(LdapProviderType.activeDirectory) ) {
            userDNSb.append("CN=").append(userName).append(",CN=Users");
        } else {
            userDNSb.append("uid=").append(userName).append(",cn=Users").append(",cn=Accounts");
        }

        if (!baseDN.isEmpty()) {
            String dcDN = getDcDN(baseDN);
            if (!dcDN.isEmpty()) {
                userDNSb.append(",").append(dcDN);
            }
        }

        userName = userDNSb.toString();
    }

    @Override
    protected void setCredentialsOnContext() {
        contextSource.setUserDn(userName);
        contextSource.setPassword(password);

    }

    private String getDcDN(String baseDN) {
        if (!baseDN.isEmpty()) {
            int dcIndex = baseDN.toLowerCase().indexOf("dc=");

            if ( dcIndex == -1 ) {
                dcIndex = 0;
            }

            return baseDN.substring(dcIndex);
        } else {
            return "";
        }
    }

}
