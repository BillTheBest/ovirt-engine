package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Helper class for AD issues
 *
 */
public class LdapBrokerUtils {

    private static LogCompat log = LogFactoryCompat.getLog(LdapBrokerUtils.class);

    /**
     * getDomainsList as stored in DB - trims the domains if needed
     *
     * @return
     */
    public static List<String> getDomainsList(boolean filterInternalDomain) {
        String[] domains = Config.<String> GetValue(ConfigValues.DomainName).split("[,]", -1);
        List<String> domainsList = Arrays.asList(domains);
        List<String> results = new ArrayList<String>();
        for (String domain : domains) {
            String trimmedDomain = domain.trim();
            if (!trimmedDomain.isEmpty()) {
                results.add(domain.trim());
            }
        }
        if (!filterInternalDomain) {
            results.add(Config.<String> GetValue(ConfigValues.AdminDomain).trim());
        }
        return results;
    }

    /**
     * returns the full domain list
     * @return
     */
    public static List<String> getDomainsList() {
        return  getDomainsList(false);
    }
    /**
     * This method should parse a string in the following format: CN=groupname,OU=ouSub,OU=ouMain,DC=qumranet,DC=com to
     * the following format qumranet.com/ouMain/ouSub/groupname it should also handle '\,' and '\=' as ',' and '='.
     *
     * @param ldapname
     * @return
     */
    public static String generateGroupDisplayValue(String ldapname) {
        if (ldapname == null) {
            return "";
        }
        LdapName name;
        try {
            name = new LdapName(ldapname);
        } catch (InvalidNameException e) {
            // fail to generate a nice display value. Retuning the String we got.
            return ldapname;
        }

        StringBuilder sb = new StringBuilder();

        List<Rdn> rdns = name.getRdns();
        for (Rdn rdn : rdns) {
            String type = rdn.getType();
            String val = (String) rdn.getValue();
            if (type.equalsIgnoreCase("dc")) {
                sb.insert(0, "." + val);
                continue;
            }
            sb.append("/" + val);
        }
        // remove the first "." character.
        sb.delete(0, 1);
        return sb.toString();
    }

    /**
     * This method performs group population for the given list of users. It will not execute LDAP queries for groups
     * that were already populated (passed by the updatedGroups parameters)
     *
     * @param users
     *            users to populate their groups for
     * @param loginName
     *            user to perform the LDAP queries for group population with
     * @param password
     *            password to perform the LDAP queries for group population with
     * @param domainName
     *            domain to perform the LDAP queries for group population with
     * @param updatedGroups
     *            list of already populated groups that should not be repopulated.
     */
    public static void performGroupPopulationForUsers(List<AdUser> users,
            String loginName,
            String password,
            String domainName,
            List<ad_groups> updatedGroups) {
        // A list that holds the results of the LDAP queries for groups - both from this method + from previous LDAP
        // queries for groups that populated groups
        // that are now in updatedGroups list
        List<GroupSearchResult> results = new ArrayList<GroupSearchResult>();

        HashMap<String, java.util.HashMap<Guid, AdUser>> groupsAdUsersMap =
                new java.util.HashMap<String, java.util.HashMap<Guid, AdUser>>();
        Set<String> currentGroupsForSearch = new HashSet<String>();
        // Constructs a map that holds the groups that were already previously queried (for example, by
        // DbUserCacheManager.updateDbGroups
        Map<Guid, ad_groups> alreadyQueriedGroups = new HashMap<Guid, ad_groups>();
        if (updatedGroups != null) {
            for (ad_groups adGroup : updatedGroups) {
                alreadyQueriedGroups.put(adGroup.getid(), adGroup);
            }
        }
        // Passes on all the users
        for (AdUser user : users) {
            // Passes on all known groups of a given user.
            for (Map.Entry<String, ad_groups> groupEntry : user.getGroups().entrySet()) {

                java.util.HashMap<Guid, AdUser> map;

                String groupName = groupEntry.getKey();
                Guid groupId = groupEntry.getValue().getid();
                String groupDN = groupEntry.getValue().getDistinguishedName();

                // Checks the following for all groups of user
                // 1. If the group was already marked as candidate for population - dont mark it again , so
                // redundant population will not be carried out
                // 2. For a group that is marked as candidate for population - check if it was already populated
                // if so - add it to the search results list, and not to the groups to be queried
                if (!groupsAdUsersMap.containsKey(groupName)) {
                    map = new java.util.HashMap<Guid, AdUser>();
                    groupsAdUsersMap.put(groupName, map);
                    ad_groups alreadyUpdatedGroup = alreadyQueriedGroups.get(groupId);
                    // If the group was already populated, transform it to LDAP query result object and add it to result
                    // list
                    if (alreadyUpdatedGroup != null) {
                        results.add(new GroupSearchResult(alreadyUpdatedGroup));
                    } else { // the group was not already queried - make sure it will be queried.
                        currentGroupsForSearch.add(groupDN);
                    }
                } else {
                    map = groupsAdUsersMap.get(groupName);
                }
                if (!map.containsKey(user.getUserId())) {
                    map.put(user.getUserId(), user);
                }
            }
        }
        // Generate the LDAP query and pass the results (both the results from previous population and from
        // this population) to further processing
        GroupsDNQueryGenerator generator = new GroupsDNQueryGenerator(currentGroupsForSearch);
        List<LdapQueryData> partialQueries = generator.getLdapQueriesData();

        for (LdapQueryData queryData : partialQueries) {
            List<GroupSearchResult> searchResults =
                    performGroupQuery(loginName, password, domainName, queryData);
            if (searchResults != null) {
                // Add all LDAP results to the results list - it now contains objects retreived from ldap, and objects
                // that
                // were previously queried.
                results.addAll(searchResults);
            }

        }
        for (GroupSearchResult groupSearchResult : results) {
            ProceedGroupsSearchResult(groupSearchResult, groupsAdUsersMap, currentGroupsForSearch);
        }
    }

    /**
     * Performs a query on a group by using its DN as baseDN to perform an object-scope search (in order to optimize the
     * search
     *
     * @param loginName
     *            login of AD user to perform the query with
     * @param password
     *            password of AD user to perform the query with
     * @param domainName
     *            domain of LDAP server to perform the query against
     * @param ldapSecurityAuth
     *            security authentication type (either SIMPLE or GSSAPI - in case of SIMPLE no optimization occurs)
     * @param queryInfo
     *            object that contain query information (query filter + base DN)
     * @return list of results
     */
    public static List<GroupSearchResult> performGroupQuery(String loginName,
            String password,
            String domainName,
            LdapQueryData queryData) {

        LdapCredentials ldapCredentials =
                new LdapCredentials(LdapBrokerUtils.modifyLoginNameForKerberos(loginName, domainName), password);
        DirectorySearcher directorySearcher = new DirectorySearcher(ldapCredentials);

        try {
            List<GroupSearchResult> searchResults = directorySearcher.FindAll(queryData);

            return searchResults;
        } catch (DomainNotConfiguredException ex) {
            log.errorFormat("User {0} from domain {1} is a member of a group from {2} which is not configured. Please use the manage domains utility if you wish to add this domain.",
                    loginName,
                    domainName,
                    queryData.getDomain());
            return null;
        }

    }

    /**
     * Add Group reference to User
     *
     * @param user
     * @param groupName
     */
    private static void AddGroupToUser(AdUser user, String groupName) {
        if (!user.getGroups().containsKey(groupName)) {
            ad_groups group = DbFacade.getInstance().getAdGroupDAO().getByName(groupName);
            if (group != null) {
                user.getGroups().put(groupName, group);
            } else {
                user.getGroups().put(groupName, new ad_groups());
            }
        }
    }

    /**
     * Update all groups from single search result
     *
     * @param searchResult
     */
    private static void ProceedGroupsSearchResult(GroupSearchResult searchResult,
            Map<String, java.util.HashMap<Guid, AdUser>> _groupsAdUsersMap,
            Set<String> currentGroupsForSearch) {
        List<String> memberOf = searchResult.getMemberOf();
        String groupName = searchResult.getDistinguishedName();
        groupName = generateGroupDisplayValue(groupName);
        java.util.HashMap<Guid, AdUser> groupUsers = _groupsAdUsersMap.get(groupName);
        if (memberOf == null) {
            return;
        }
        // The group may be a member of other groups - check for all the groups it is member in (all parent groups)
        for (String groupVal : memberOf) {

            String parentGroupName = generateGroupDisplayValue(groupVal);
            if (!_groupsAdUsersMap.containsKey(parentGroupName)) {

                currentGroupsForSearch.add(parentGroupName);
                java.util.HashMap<Guid, AdUser> map = new java.util.HashMap<Guid, AdUser>();
                if (groupUsers != null) {
                    for (AdUser user : groupUsers.values()) {
                        map.put(user.getUserId(), user);
                        AddGroupToUser(user, parentGroupName);
                    }
                }
                _groupsAdUsersMap.put(parentGroupName, map);
            } else {
                java.util.HashMap<Guid, AdUser> parentGroupUser = _groupsAdUsersMap.get(parentGroupName);
                if (parentGroupUser != null && groupUsers != null) {
                    for (Guid userId : groupUsers.keySet()) {
                        if (!parentGroupUser.containsKey(userId)) {
                            parentGroupUser.put(userId, groupUsers.get(userId));
                            AddGroupToUser(groupUsers.get(userId), parentGroupName);
                        }
                    }
                }
            }
        }
    }

    public static void performGroupPopulationForUsers(ArrayList<AdUser> adUsers,
            String domain,
            List<ad_groups> updatedGroups) {
        Domain domainObject = UsersDomainsCacheManagerService.getInstance().getDomain(domain.toLowerCase());
        String user = domainObject.getUserName();
        String password = domainObject.getPassword();
        performGroupPopulationForUsers(adUsers, user, password, domain, updatedGroups);

    }

    public static String hadleNameEscaping(String name) {
        return StringUtils.countMatches(name, "\\") == 1 ? name.replace("\\", "\\\\\\") : name;
    }

    public static String modifyLoginNameForKerberos(String loginName, String domain) {
        String[] parts = loginName.split("[@]");

        Domain requestedDomain = UsersDomainsCacheManagerService.getInstance().getDomain(domain);

        if (requestedDomain == null) {
            throw new DomainNotConfiguredException(domain);
        }

        LDAPSecurityAuthentication securityAuthentication = requestedDomain.getLdapSecurityAuthentication();
        boolean isKerberosAuth = securityAuthentication.equals(LDAPSecurityAuthentication.GSSAPI);

        // if loginName is not in format of user@domain
        if (parts.length != 2) {

            // when Kerberos is the auth mechanism we must use UPN to otherwise
            // the default REALM, as confugured in krb5.conf will be picked
            return isKerberosAuth ? loginName + "@" + domain.toUpperCase() : loginName;
        }

        // In case the login name is in format of user@domain, it should be
        // transformed to user@realm - realm is a capitalized version of fully
        // qualified domain name

        StringBuilder result = new StringBuilder();
        result.append(parts[0]);
        if (isKerberosAuth) {
            String realm = parts[1].toUpperCase();
            result.append("@").append(realm);
        }
        return result.toString();
    }

    public static String getGroupDomain(String ldapname) {
        if (ldapname == null) {
            return "";
        }
        LdapName name;
        try {
            name = new LdapName(ldapname);
        } catch (InvalidNameException e) {
            // fail to generate a nice display value. Retuning the String we got.
            return ldapname;
        }

        StringBuilder sb = new StringBuilder();

        List<Rdn> rdns = name.getRdns();
        for (Rdn rdn : rdns) {
            String type = rdn.getType();
            String val = (String) rdn.getValue();
            if (type.equalsIgnoreCase("dc")) {
                sb.insert(0, "." + val);
                continue;
            }
        }
        // remove the first "." character.
        sb.delete(0, 1);
        return sb.toString();
    }
}
