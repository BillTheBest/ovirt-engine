#!/bin/sh
#
# This script is designed to run the oVirt Event Notification service.
# The script assumes all RPM dependencies were installed, so jar
# files can be found under /usr/share/java. The service's configuration
# should be under the /etc directory by default.
#
die () {
    printf >&2 "$1"
    exit $2
}

usage () {
    printf "engine-notifier: oVirt Event Notification Service\n"
    printf "USAGE:\n"
    printf "\tengine-notifier [configuration file]\n"
    return 0
}

die_no_propset() {
    # exit when property defined but not set then exit
    die "Error: $1 if defined can not be empty, please check for this in configuration file $CONF_FILE\n" 6
}

NOTIFIER_HOME=/usr/share/engine/notifier/
if [ ! -d "$NOTIFIER_HOME" ]; then
    die "Error: daemon home directory is missing or not accessible: $NOTIFIER_HOME.\n" 5
fi

cd $NOTIFIER_HOME

if [ "$1" == "--help" -o "$1" == "-h" ]; then
    usage
    exit 0
fi

if [ "$#" -gt 1 ]; then
    usage
    die "Error: wrong argument number: $#.\n" 2
fi

if [ "$#" -eq 1 ]; then
    if [ ! -r "$1" ]; then
        die "Error: configuration file does not exist or has no read permission: $1.\n" 6
    fi
    CONF_FILE="$1"
else
    CONF_FILE=/etc/engine/notifier/notifier.conf
fi

# Import configurations
. $CONF_FILE

# Do basic checking of properties in configuration file to ensure
# a) properties are defined
# b) when properties are defined and reference a file system resource, that the resource exists.
if [ -z "$engineLib" ]; then
    die "Error: \$engineLib is not defined, please check for this in configuration file $CONF_FILE\n"
fi
if [ ! -d $engineLib ]; then
    die "Error: the oVirt Engine library is missing or not accessible.\n" 5
fi

# MAIL_SERVER is required!
if [ -z "$MAIL_SERVER" ]; then
    die "Error: \$MAIL_SERVER is not defined, please check for this in configuration file $CONF_FILE\n" 6
fi

# Now check for properties that if defined, can not be empty
# INTERVAL_IN_SECONDS if defined can not be empty
if [ "${INTERVAL_IN_SECONDS+x}" ]; then
    if [ -z "$INTERVAL_IN_SECONDS" ]; then
        die_no_propset \$INTERVAL_IN_SECONDS
    fi
fi

# MAIL_PORT if defined can not be empty
if [ "${MAIL_PORT+x}" ]; then
    if [ -z "$MAIL_PORT" ]; then
        die_no_propset \$MAIL_PORT
    fi
fi

# MAIL_USER if defined can not be empty
if [ "${MAIL_USER+x}" ]; then
    if [ -z "$MAIL_USER" ]; then
        die_no_propset \$MAIL_USER
    fi
fi

# MAIL_PASSWORD if defined can not be empty
if [ "${MAIL_PASSWORD+x}" ]; then
    if [ -z "$MAIL_PASSWORD" ]; then
        die_no_propset \$MAIL_PASSWORD
    fi
fi

# MAIL_ENABLE_SSL if defined can not be empty
if [ "${MAIL_ENABLE_SSL+x}" ]; then
    if [ -z "$MAIL_ENABLE_SSL" ]; then
        die_no_propset \$MAIL_ENABLE_SSL
    else
        # MAIL_USER if can not be empty for SSL
        if [ -z "${MAIL_USER}" ]; then
            die "Error: \$MAIL_USER is not defined for SSL MAIL, please check for this in configuration file $CONF_FILE\n" 6
        fi
        # MAIL_PASSWORD can not be empty for SSL
        if [ -z "${MAIL_PASSWORD}" ]; then
            die "Error: \$MAIL_PASSWORD is not defined for SSL MAIL, please check for this in configuration file $CONF_FILE\n" 6
        fi
    fi
fi

# HTML_MESSAGE_FORMAT if defined can not be empty
if [ "${HTML_MESSAGE_FORMAT+x}" ]; then
    if [ -z "$HTML_MESSAGE_FORMAT" ]; then
        die_no_propset \$HTML_MESSAGE_FORMAT
    fi
fi

# MAIL_FROM if defined can not be empty
if [ "${MAIL_FROM+x}" ]; then
    if [ -z "$MAIL_FROM" ]; then
        die_no_propset \$MAIL_FROM
    fi
fi

# MAIL_REPLY_TO if defined can not be empty
if [ "${MAIL_REPLY_TO+x}" ]; then
    if [ -z "$MAIL_REPLY_TO" ]; then
        die_no_propset \$MAIL_REPLY_TO
    fi
fi

# DAYS_TO_KEEP_HISTORY if defined can not be empty
if [ "${DAYS_TO_KEEP_HISTORY+x}" ]; then
    if [ -z "$DAYS_TO_KEEP_HISTORY" ]; then
        die_no_propset \$DAYS_TO_KEEP_HISTORY
    fi
fi

# ENGINE_INTERVAL_IN_SECONDS if defined can not be empty
if [ "${ENGINE_INTERVAL_IN_SECONDS+x}" ]; then
    if [ -z "$ENGINE_INTERVAL_IN_SECONDS" ]; then
        die_no_propset \$ENGINE_INTERVAL_IN_SECONDS
    fi
fi

# ENGINE_MONITOR_RETRIES if defined can not be empty
if [ "${ENGINE_MONITOR_RETRIES+x}" ]; then
    if [ -z "$ENGINE_MONITOR_RETRIES" ]; then
        die_no_propset \$ENGINE_MONITOR_RETRIES
    fi
fi

# ENGINE_TIMEOUT_IN_SECONDS if defined can not be empty
if [ "${ENGINE_TIMEOUT_IN_SECONDS+x}" ]; then
    if [ -z "$ENGINE_TIMEOUT_IN_SECONDS" ]; then
        die_no_propset \$ENGINE_TIMEOUT_IN_SECONDS
    fi
fi

# ENGINE_ADDRESS if defined can not be empty
if [ "${ENGINE_ADDRESS+x}" ]; then
    if [ -z "$ENGINE_ADDRESS" ]; then
        die_no_propset \$ENGINE_ADDRESS
    fi
fi

# IS_HTTPS_PROTOCOL if defined can not be empty
if [ "${IS_HTTPS_PROTOCOL+x}" ]; then
    if [ -z "$IS_HTTPS_PROTOCOL" ]; then
        die_no_propset \$IS_HTTPS_PROTOCOL
    fi
fi

# IS_NONREPEATED_NOTIFICATION if defined can not be empty
if [ "${IS_NONREPEATED_NOTIFICATION+x}" ]; then
    if [ -z "$IS_NONREPEATED_NOTIFICATION" ]; then
        die_no_propset \$IS_NONREPEATED_NOTIFICATION
    fi
fi

# DB_CONNECTION_URL if defined can not be empty
if [ "${DB_CONNECTION_URL+x}" ]; then
    if [ -z "$DB_CONNECTION_URL" ]; then
        die_no_propset \$DB_CONNECTION_URL
    fi
    # DB_USER_NAME if defined can not be empty
    if [ "${DB_USER_NAME+x}" ]; then
        if [ -z "$DB_USER_NAME" ]; then
            die_no_propset \$DB_USER_NAME
        fi
    else
        die_no_propset \$DB_USER_NAME
    fi
    # DB_PASSWORD if defined can not be empty
    if [ "${DB_PASSWORD+x}" ]; then
        if [ -z "$DB_PASSWORD" ]; then
            die_no_propset \$DB_PASSWORD
        fi
    else
        die_no_propset \$DB_PASSWORD
    fi
    # DB_JDBC_DRIVER_CLASS if defined can not be empty
    if [ "${DB_JDBC_DRIVER_CLASS+x}" ]; then
        if [ -z "$DB_JDBC_DRIVER_CLASS" ]; then
            die_no_propset \$DB_JDBC_DRIVER_CLASS
        fi
    else
        die_no_propset \$DB_JDBC_DRIVER_CLASS
    fi
else
    # AS_DATA_SOURCE is required!
    if [ -z "$AS_DATA_SOURCE" ]; then
        die_no_propset \$AS_DATA_SOURCE
    fi
    if [ ! -s $AS_DATA_SOURCE ]; then
        die "Error: Missing or inaccessible configuration file $AS_DATA_SOURCE ( See \$AS_DATA_SOURCE in $CONF_FILE ).\n" 6
    fi
fi

# Configure classpath for engine-notifier
JAVA_LIB_HOME=/usr/share/java
#JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8787"

# Add the configuration directory to the classpath so that configuration
# files can be loaded as resources:
CP=/etc/engine/notifier/.

# Add the required jar files from the system wide jars directory:
jar_names='
    commons-logging
    commons-collections
    commons-lang
    log4j
    commons-codec
    commons-configuration
    commons-jxpath
    postgresql-jdbc
    javamail
    activation
    engine-tools-common
'
for jar_name in ${jar_names}
do
    jar_file=${JAVA_LIB_HOME}/${jar_name}.jar
    if [ ! -s "${jar_file}" ]
    then
        die "Error: can't run without missing JAR file: ${jar_file}\n" 5
    fi
    CP=${CP}:${jar_file}
done

# Add all the needed jar files from the oVirt Engine EAR to the classpath, but
# try to locate them using the name and not the version. This is important
# in order to make the script less dependent on the version of oVirt Engine
# installed:
jar_names='
    stax-api
    jaxb-api
    slf4j-api
    validation-api
    hibernate-validator
    hibernate-commons-annotations
    hibernate-core
    antlr
    dom4j
    xml-apis
    ejb3-persistence
    hibernate-annotations
    jaxb-impl
    engine-common
    engine-compat
    engineencryptutils
'
for jar_name in ${jar_names}
do
    jar_file=$(find ${engineLib} -regex ".*/${jar_name}.*\.jar")
    if [ -z "${jar_file}" -o ! -s "${jar_file}" ]
    then
        die "Error: can't run without missing JAR file: ${engineLib}/${jar_name}*.jar\n" 5
    fi
    CP=${CP}:${jar_file}
done

# Add the jar files specific to the notifier:
jar_names='
    engine-notifier
'
for jar_name in ${jar_names}
do
    jar_file=${NOTIFIER_HOME}/${jar_name}.jar
    if [ ! -s "${jar_file}" ]
    then
        die "Error: can't run without missing JAR file: ${jar_file}\n" 5
    fi
    CP=${CP}:${jar_file}
done

if [ -z "$NOTIFIER_PID" ]
then
    NOTIFIER_PID=/dev/null
fi

exec java -cp .:$CP $JAVA_OPTS org.ovirt.engine.core.notifier.Notifier $@ 2>/dev/null &

echo $! >$NOTIFIER_PID
