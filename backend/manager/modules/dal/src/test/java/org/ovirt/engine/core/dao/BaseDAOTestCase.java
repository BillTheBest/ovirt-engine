package org.ovirt.engine.core.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.DiskImageTemplate;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.RoleGroupMap;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdcOption;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.action_version_map;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.bookmarks;
import org.ovirt.engine.core.common.businessentities.event_map;
import org.ovirt.engine.core.common.businessentities.event_notification_hist;
import org.ovirt.engine.core.common.businessentities.event_notification_methods;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.businessentities.image_group_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.businessentities.image_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.businessentities.stateless_vm_image_map;
import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.businessentities.tags_user_group_map;
import org.ovirt.engine.core.common.businessentities.tags_user_map;
import org.ovirt.engine.core.common.businessentities.tags_vds_map;
import org.ovirt.engine.core.common.businessentities.tags_vm_map;
import org.ovirt.engine.core.common.businessentities.tags_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pool_map;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.businessentities.vm_template_image_map;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dal.dbbroker.user_sessions;
import org.ovirt.engine.core.dal.dbbroker.generic.DBConfigUtils;


/**
 * <code>BaseDAOTestCase</code> provides a foundation for creating unit tests for the persistence layer.
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({TransactionalTestExecutionListener.class})
@ContextConfiguration(loader=CustomizedContextLoader.class)
@Transactional
public abstract class BaseDAOTestCase {
    private static SessionFactory sessionFactory;
    protected static DbFacade dbFacade;
    private static Object dataFactory;
    protected static boolean needInitializationSql = false;
    protected static String initSql;
    protected static DataSource dataSource;
    private static IDataSet dataset;

    @NotTransactional
    @BeforeClass
    public static void initTestCase() throws Exception {
       dataSource = createDataSource();
       dataset = initDataSet();
       dbFacade = new DbFacade();
       dbFacade.setDbEngineDialect(DbFacadeLocator.loadDbEngineDialect());
       dbFacade.setTemplate(dbFacade.getDbEngineDialect().createJdbcTemplate(dataSource));

       //load data from fixtures to DB
       DatabaseOperation.CLEAN_INSERT.execute(getConnection(), dataset);
    }

    @NotTransactional
    @AfterClass
    public static void tearDownTestCase() throws Exception {
        ((DisposableBean) getDataSource()).destroy();
    }

    @Before
    public void setUp() throws Exception {
    }

    protected <T> T prepareDAO(T dao) {
        if (dao instanceof BaseDAOHibernateImpl || dao instanceof BaseDAOWrapperImpl) {
            if (sessionFactory == null) {
                sessionFactory = getSessionFactory();
            }
        }

        if (dao instanceof BaseDAOHibernateImpl) {
            ((BaseDAOHibernateImpl<?, ?>) dao).setSession(getSession());
        } else if (dao instanceof BaseDAOWrapperImpl) {
            ((BaseDAOWrapperImpl) dao).setSession(getSession());
        }
        return dao;
    }

    private static IDataSet initDataSet() throws Exception {
        return new XmlDataSet(BaseDAOTestCase.class.getResourceAsStream(
                "/fixtures.xml"));
    }


    protected void setUpDatabaseConfig(DatabaseConfig config) {
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dataFactory);
    }

    protected static IDatabaseConnection getConnection() throws Exception {
        // get connection and setup it's meta data
        Connection con = dataSource.getConnection();
        IDatabaseConnection connection = new DatabaseConnection(con);
        connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, dataFactory);
        if (needInitializationSql) {
            Statement stmt = con.createStatement();
            stmt.executeUpdate(initSql);
        }
        return connection;
    }

    private static DataSource createDataSource() {
        DataSource result = null;
        Properties properties = new Properties();

        Config.setConfigUtils(new DBConfigUtils(false));

        try {
            String job = System.getProperty("JOB_NAME");
            if(job == null) job = "";
            String number = System.getProperty("BUILD_NUMBER");
            if(number == null) number = "";
            String schemaNamePostfix = job + number;
            properties.load(BaseDAOTestCase.class.getResourceAsStream(
                    "/test-database.properties"));
            ClassLoader.getSystemClassLoader().loadClass(
                    properties.getProperty("database.driver"));
            String dbUrl = properties.getProperty("database.url") + schemaNamePostfix;
            result = new SingleConnectionDataSource(
                    dbUrl,
                    properties.getProperty("database.username"),
                    properties.getProperty("database.password"), true);

            initSql = properties.getProperty("database.initsql");

            loadDataFactory(properties.getProperty("database.testing.datafactory"));

            if (initSql != null && !initSql.isEmpty())
            {
                needInitializationSql = true;
            }
        } catch (Exception error) {
            error.printStackTrace();
            throw new RuntimeException("Cannot create data source", error);
        }

        return result;
    }

    protected SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory =
                    new AnnotationConfiguration()
                            .addAnnotatedClass(action_version_map.class)
                            .addAnnotatedClass(ad_groups.class)
                            .addAnnotatedClass(async_tasks.class)
                            .addAnnotatedClass(AuditLog.class)
                            .addAnnotatedClass(bookmarks.class)
                            .addAnnotatedClass(DbUser.class)
                            .addAnnotatedClass(DiskImage.class)
                            .addAnnotatedClass(DiskImageDynamic.class)
                            .addAnnotatedClass(DiskImageTemplate.class)
                            .addAnnotatedClass(event_map.class)
                            .addAnnotatedClass(event_notification_hist.class)
                            .addAnnotatedClass(event_notification_methods.class)
                            .addAnnotatedClass(event_subscriber.class)
                            .addAnnotatedClass(image_group_storage_domain_map.class)
                            .addAnnotatedClass(image_vm_map.class)
                            .addAnnotatedClass(image_vm_pool_map.class)
                            .addAnnotatedClass(LUN_storage_server_connection_map.class)
                            .addAnnotatedClass(LUNs.class)
                            .addAnnotatedClass(network_cluster.class)
                            .addAnnotatedClass(network.class)
                            .addAnnotatedClass(permissions.class)
                            .addAnnotatedClass(RoleGroupMap.class)
                            .addAnnotatedClass(roles.class)
                            .addAnnotatedClass(RoleGroupMap.class)
                            .addAnnotatedClass(stateless_vm_image_map.class)
                            .addAnnotatedClass(storage_domain_dynamic.class)
                            .addAnnotatedClass(storage_domain_static.class)
                            .addAnnotatedClass(storage_pool.class)
                            .addAnnotatedClass(storage_pool_iso_map.class)
                            .addAnnotatedClass(storage_server_connections.class)
                            .addAnnotatedClass(tags.class)
                            .addAnnotatedClass(tags_user_group_map.class)
                            .addAnnotatedClass(tags_user_map.class)
                            .addAnnotatedClass(tags_vds_map.class)
                            .addAnnotatedClass(tags_vm_map.class)
                            .addAnnotatedClass(tags_vm_pool_map.class)
                            .addAnnotatedClass(time_lease_vm_pool_map.class)
                            .addAnnotatedClass(user_sessions.class)
                            .addAnnotatedClass(VdcOption.class)
                            .addAnnotatedClass(VdsDynamic.class)
                            .addAnnotatedClass(VDSGroup.class)
                            .addAnnotatedClass(VdsStatic.class)
                            .addAnnotatedClass(VdsStatistics.class)
                            .addAnnotatedClass(vm_pool_map.class)
                            .addAnnotatedClass(vm_pools.class)
                            .addAnnotatedClass(vm_template_image_map.class)
                            .addAnnotatedClass(VmDynamic.class)
                            .addAnnotatedClass(VmStatic.class)
                            .addAnnotatedClass(storage_pool_iso_map.class)
                            .addAnnotatedClass(VmTemplate.class)
                            .configure().buildSessionFactory();
        }

        return sessionFactory;
    }

    protected Session getSession() {
        return sessionFactory.openSession();
    }

    private static void loadDataFactory(String dataFactoryClassname) throws Exception {
        Class<?> clazz = Class.forName(dataFactoryClassname);
        dataFactory = clazz.newInstance();
    }

    public static DbFacade getDbFacade() {
        return dbFacade;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
