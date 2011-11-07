package org.ovirt.engine.core.utils.kerberos;

import static org.ovirt.engine.core.utils.kerberos.InstallerConstants.ERROR_PREFIX;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.ovirt.engine.core.utils.CLIParser;

/**
 * Utility to verify Kerberos installation
 *
 */
public class KerberosConfigCheck {
    private LoginContext lc;
    private final static Logger log = Logger.getLogger(KerberosConfigCheck.class);

    public enum Arguments {
        domains,
        user,
        password,
        jaas_file,
        jboss_dir,
        krb5_conf_path;
    }

    // This function gets the username and adjusts it doing the following:
    // 1. If the username contains @, for example:
    // user@domain, it returns user@DOMAIN
    // 2. If the username doesn't contain @ it returns the input user name
    // 3. For inputs like "@", "user@" and @domain it just returns the input
    private static String adjustUserName(String userName) {
        String returnUserName = userName;

        if (userName.contains("@")) {
            String[] parts = userName.split("@");
            int numberOfParts = parts.length;

            switch (numberOfParts) {
            case 1:
                returnUserName = parts[0];
                break;
            case 2:
                returnUserName = parts[0] + '@' + parts[1].toUpperCase();
                break;
            default:
                returnUserName = userName;
                break;
            }
        }

        return returnUserName;
    }
    /**
     * JAAS callback handler. JAAS uses this class during login - it provides an array of callbacks (including the
     * NameCallback and PasswordCallback) It is the responsibility of the implementor of CallbackHandler to set the user
     * name and the password on the relevant call backs.
     */
    private static class KerberosUtilCallbackHandler implements CallbackHandler {
        private String username;
        private String password;

        public KerberosUtilCallbackHandler(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof NameCallback) {
                    NameCallback cb = (NameCallback) callbacks[i];
                    cb.setName(username);

                } else if (callbacks[i] instanceof PasswordCallback) {
                    PasswordCallback cb = (PasswordCallback) callbacks[i];
                    cb.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callbacks[i]);
                }
            }
        }
    }

    private void printUsage() {
        System.out.println("Usage:");
        System.out
                .println("KerberosConfigCheck: -domains=<domains> -user=<user> -password=<password> -jaas_conf=<jaas conf path> krb5_conf_path=<krb5 conf path>");
    }

    private boolean validate(CLIParser parser) {
        Arguments[] argsToValidate =
                { Arguments.domains, Arguments.user, Arguments.password, Arguments.jaas_file, Arguments.krb5_conf_path };
        for (Arguments argument : argsToValidate) {
            if (!parser.hasArg(argument.name())) {
                System.out.println(argument.name() + " is required");
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        KerberosConfigCheck util = new KerberosConfigCheck();
        CLIParser parser = new CLIParser(args);
        if (!util.validate(parser)) {
            util.printUsage();
            System.exit(1);
        }
        String username = adjustUserName(parser.getArg(Arguments.user.name()));
        String password = parser.getArg(Arguments.password.name());
        String jaasFile = parser.getArg(Arguments.jaas_file.name());
        String krb5ConfFile = parser.getArg(Arguments.krb5_conf_path.name());
        String domains = parser.getArg(Arguments.domains.name());
        StringBuffer userGuid = new StringBuffer();
        try {
            util.checkInstallation(domains, username, password, jaasFile, krb5ConfFile, userGuid);
        } catch (AuthenticationException e) {
            System.err.println(ERROR_PREFIX + e.getMessage());
            System.exit(e.getAuthResult().getExitCode());
        }
    }

    public void checkInstallation(String domains,
            String username,
            String password,
            String jaasFile,
            String krb5ConfFile,
            StringBuffer userGuid)
            throws AuthenticationException {
        String[] domainsList = domains.split(",", -1);
        String domain = domainsList[0].trim();
        String realm = domain.toUpperCase();
        validateKerberosInstallation(realm, username, password, jaasFile, krb5ConfFile, userGuid);
    }

    public void validateKerberosInstallation(String realm,
            String username,
            String password,
            String pathToJAASFile,
            String pathToKrb5ConfFile,
            StringBuffer userGuid) throws AuthenticationException {

        AuthenticationResult authResult = authenticate(realm, username, password, pathToJAASFile, pathToKrb5ConfFile);
        if (authResult == AuthenticationResult.OK) {
            // Successful authentication was acehived, no point in searching for
            // KDcs that use UDP
            AuthenticationResult actionResult = promptSuccessfulAuthentication(realm, username, userGuid);

            if (actionResult != AuthenticationResult.OK) {
                throw new AuthenticationException(actionResult);
            }

            return;
        } else {
            throw new AuthenticationException(authResult);
        }
    }

    private AuthenticationResult promptSuccessfulAuthentication(String realm, String username, StringBuffer userGuid) {

        AuthenticationResult authResult = AuthenticationResult.OTHER;

        try {
            // Executing the code that will perform the LDAP query to get the
            // user and print its GUID.
            // A Windows domain is lowercase string of Keberos realm.
            authResult =
                    (AuthenticationResult) Subject.doAs(lc.getSubject(), new JndiAction(username,
                            realm.toLowerCase(),
                            userGuid));

        } finally {
            if (lc != null) {
                try {
                    lc.logout();
                } catch (LoginException e) {
                    System.out.println(ERROR_PREFIX + " logout failed " + e.getMessage());
                }
            }
        }

        return authResult;
    }

    private AuthenticationResult authenticate(String realm, String username, String password,
            String pathToJAASFile, String pathToKrb5File) {

        // Set the realm to authenticate to and the path to the JAAS file that
        // will define
        // that JAAS is using kerberos login module

        System.setProperty("java.security.krb5.conf", pathToKrb5File);
        // Get kdcs for the relevant protocol (tcp or udp) and for the given
        // realm

        System.setProperty("java.security.auth.login.config", pathToJAASFile);

        return checkAuthentication(username, password);
    }

    private AuthenticationResult checkAuthentication(String username, String password) {

        AuthenticationResult result = AuthenticationResult.OK;
        try {

            lc = new LoginContext("KerberosUtil", new KerberosUtilCallbackHandler(username, password));
            lc.login();
            log.debug("Check authentication finished successfully ");
        } catch (LoginException ex) {
            String resultMessage = ex.getMessage();

            KerberosReturnCodeParser parser = new KerberosReturnCodeParser();
            result = parser.parse(resultMessage);
            if (result != AuthenticationResult.OTHER) {
                return result;
            } else {
                System.out.println(ERROR_PREFIX + " exception message: " + ex.getMessage());
            }
        }
        return result;
    }
}
