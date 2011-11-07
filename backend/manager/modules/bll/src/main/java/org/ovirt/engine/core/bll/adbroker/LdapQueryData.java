package org.ovirt.engine.core.bll.adbroker;

public interface LdapQueryData {

    public LdapQueryType getLdapQueryType();

    public void setLdapQueryType(LdapQueryType ldapQueryType);

    public void setFilterParameters(Object[] filterParameters);

    public Object[] getFilterParameters();

    public void setBaseDNParameters(Object[] baseDNParameters);

    public Object[] getBaseDNParameters();

    public String getDomain();

    public void setDomain(String domain);

}
