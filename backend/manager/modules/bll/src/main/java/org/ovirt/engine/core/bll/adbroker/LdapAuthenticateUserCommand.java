package org.ovirt.engine.core.bll.adbroker;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.kerberos.AuthenticationResult;

public class LdapAuthenticateUserCommand extends LdapBrokerCommandBase {
    public LdapAuthenticateUserCommand(LdapUserPasswordBaseParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        log.debug("Executing LdapAuthenticateUserCommand");

        directorySearcher.setExplicitAuth(true);
        AdUser user = null;
        UserAuthenticationResult authResult = null;
        LdapQueryData queryData = new LdapQueryDataImpl();

        if (getLoginName().contains("@")) { // the user name is UPN use 'User
                                            // Principal Name' search
            queryData.setLdapQueryType(LdapQueryType.getUserByPrincipalName);
            // The domain in the UPN must overwrite the domain field. Discrepancies between the UPN domain and
            // the domain may lead failure in Kerberos queries
            String[] loginNameParts = getLoginName().split("@");
            String principalName = constructPrincipalName(loginNameParts[0], loginNameParts[1]);
            String domain = loginNameParts[1].toLowerCase();
            queryData.setFilterParameters(new Object[] { principalName });
            queryData.setDomain(domain);
            setDomain(domain);
            setAuthenticationDomain(domain);
        } else {
            // the user name is NT format use 'SAM Account Name' search
            setAuthenticationDomain(getDomain());
            queryData.setDomain(getDomain());
            queryData.setLdapQueryType(LdapQueryType.getUserByName);
            queryData.setFilterParameters(new Object[] { getLoginName() });
        }
        Object searchResult = directorySearcher.FindOne(queryData);

        if (searchResult == null) {
            log.errorFormat("Failed authenticating user: {0} to domain {1}. Ldap Query Type is {2}",
                    getLoginName(),
                    getAuthenticationDomain(),
                    queryData.getLdapQueryType().name());
            setSucceeded(false);
            Exception ex = directorySearcher.getException();
            authResult = handleDirectorySearcherException(ex);
        } else {
            user = populateUserData((AdUser) searchResult, getAuthenticationDomain());
            if (user != null) {
                user.setPassword(getPassword());
                user.setUserName(getLoginName());
                GroupsDNQueryGenerator generator = createGroupsGeneratorForUser(user);
                if (generator.getHasValues()) {
                    List<LdapQueryData> partialQueries = generator.getLdapQueriesData();
                    for (LdapQueryData currQueryData : partialQueries) {
                        PopulateGroup(currQueryData,
                                getAuthenticationDomain(),
                                user.getGroups(),
                                getLoginName(),
                                getPassword());
                    }
                }
                authResult = new UserAuthenticationResult(user);
                setSucceeded(true);
            } else {
                log.errorFormat("Failed authenticating. Domain is {0}. User is {1}. The user doesn't have a UPN",
                        getAuthenticationDomain(),
                        getLoginName());
                setSucceeded(false);
            }
        }

        if (!getSucceeded()) {
            if (authResult == null) {
                authResult = new UserAuthenticationResult(user, VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            } else if (authResult.getErrorMessages().isEmpty()) {
                authResult.getErrorMessages().add(VdcBllMessages.USER_FAILED_TO_AUTHENTICATE);
            }
        }
        setReturnValue(authResult);
    }

    private UserAuthenticationResult handleDirectorySearcherException(Exception ex) {
        UserAuthenticationResult authResult = null;
        VdcBllMessages errorMsg = VdcBllMessages.USER_FAILED_TO_AUTHENTICATE;
        if (ex != null && ex instanceof EngineDirectoryServiceException) {
            EngineDirectoryServiceException dsException = (EngineDirectoryServiceException) ex;
            AuthenticationResult result = dsException.getResult();
            if (result == null) {
                result = AuthenticationResult.OTHER;
            }
            errorMsg = VdcBllMessages.valueOf(result.getVdcBllMessage());
        }
        authResult = new UserAuthenticationResult(errorMsg);
        return authResult;
    }

    private String constructPrincipalName(String username, String domain) {
        return username + '@' + domain.toUpperCase();
    }

    @Override
    protected void handleRootDSEFailure(DirectorySearcher directorySearcher) {
        Exception ex = directorySearcher.getException();
        UserAuthenticationResult authResult = handleDirectorySearcherException(ex);
        setReturnValue(authResult);
    }

    private static LogCompat log = LogFactoryCompat.getLog(LdapAuthenticateUserCommand.class);
}
