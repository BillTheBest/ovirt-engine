package org.ovirt.engine.core.dao;

import java.text.MessageFormat;

public class DaoFactoryException extends RuntimeException {
    private static final long serialVersionUID = 6895936691846540402L;

    private static final String DAO_FACTORY_ERROR = "Failed to load instance of {0} via properties file {1}";

    private Class<? extends DAO> daoType;
    private String propsFile;

    public DaoFactoryException(Class<? extends DAO> daoType, String propsFile, Throwable cause) {
        super(MessageFormat.format(DAO_FACTORY_ERROR, daoType.getSimpleName(), propsFile), cause);
        this.daoType = daoType;
        this.propsFile = propsFile;
    }

    public DaoFactoryException(Class<? extends DAO> daoType, String propsFile) {
        this(daoType, propsFile, null);
    }

    public Class<? extends DAO> getDaoType() {
        return daoType;
    }

    public String getPropsFile() {
        return propsFile;
    }
}
