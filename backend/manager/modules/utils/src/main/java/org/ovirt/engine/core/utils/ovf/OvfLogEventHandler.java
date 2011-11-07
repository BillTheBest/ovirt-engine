package org.ovirt.engine.core.utils.ovf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import org.ovirt.engine.core.common.businessentities.OvfExportOnlyField;
import org.ovirt.engine.core.common.businessentities.OvfExportOnlyField.ExportOption;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

/**
 * This class assists in OVF log events handling
 *
 *
 */
public abstract class OvfLogEventHandler<T> {

    // entity to import to OVF or to export from OVF
    private T entity;

    // map containing entries of entity's class field names to aliases - their
    // name
    private HashMap<String, String> fieldsToAliasesMap;

    // Reversed map to the above
    private HashMap<String, String> aliasesToFieldsMap;

    // list containing the names of the entity's fields that are annotated with
    // OvfExportOnlyField
    private List<String> fieldNames = new ArrayList<String>();

    // list containing the aliases of the entit's fields that are annotated with
    // OvfExportOnlyField
    private List<String> aliases = new ArrayList<String>();

    // default converter for basic types
    private static TypeConverter defaultConverter = new DefaultConverter();

    public OvfLogEventHandler(T entity) {
        this.entity = entity;
        initMaps();
    }

    /**
     * Returns a type convert maps (the key is a field name, the value is a type converter)
     *
     * @return map
     */
    protected abstract Map<String, TypeConverter> getTypeConvertersMap();

    private void initMaps() {
        try {

            // Constructs all the helper data structures - mainly by getting all
            // the entity's class fields
            // that contain the annotation OvfExportOnlyField
            Field[] fields = entity.getClass().getDeclaredFields();

            fieldsToAliasesMap = new HashMap<String, String>();
            aliasesToFieldsMap = new HashMap<String, String>();

            for (Field f : fields) {
                String fieldName = f.getName();
                String alias = fieldName;
                if (f.isAnnotationPresent(OvfExportOnlyField.class)) {

                    fieldNames.add(fieldName);

                    OvfExportOnlyField ovfLogEventField = f.getAnnotation(OvfExportOnlyField.class);
                    if (!ovfLogEventField.name().equals("")) {
                        alias = ovfLogEventField.name();
                    }

                    fieldsToAliasesMap.put(fieldName, alias);
                    aliases.add(alias);
                    aliasesToFieldsMap.put(alias, fieldName);
                }

            }

        } catch (Exception ex) {
            log.error("Error in initializing the OvfLogHandler", ex);
        }

    }

    /**
     * Gets a map of aliases to values. Aliases replace the field names (if exist) when the field data is written to
     * OVF. Values hold the data of the fields.
     *
     * @return
     */
    public Map<String, String> getAliasesValuesMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        try {

            Map<String, String> fieldsMap = fieldsToAliasesMap;

            for (java.util.Map.Entry<String, String> entry : fieldsMap.entrySet()) {

                String name = entry.getKey();
                String alias = entry.getValue();

                Field f = entity.getClass().getDeclaredField(name);
                OvfExportOnlyField ovfLogEventField = f.getAnnotation(OvfExportOnlyField.class);

                // Get the value of the field to be exported
                String value = BeanUtils.getProperty(entity, name);

                // Export the field if it should be exported
                ExportOption exportOption = ovfLogEventField.exportOption();
                if (exportOption != ExportOption.DONT_EXPORT) {
                    if (value != null && (!value.equals(ovfLogEventField.valueToIgnore())
                            || exportOption == ExportOption.ALWAYS_EXPORT)) {
                        map.put(alias, value);
                    }
                }
            }
            return map;

        } catch (Exception ex) {
            log.error("Error in getting aliases values map", ex);
            return null;
        }

    }

    /**
     * Adds an alias and a value to the entity that is managed by the event handler values
     *
     * @param alias
     *            new alias to add
     * @param value
     *            new value to add
     * @param accumulatingMap
     *            map that accumulates the new aliases/values
     */
    public void addValueForAlias(String alias, String value) {
        String fieldName = aliasesToFieldsMap.get(alias);
        try {

            Field declaredField = entity.getClass().getDeclaredField(fieldName);
            OvfExportOnlyField ovfLogEventField = declaredField.getAnnotation(OvfExportOnlyField.class);
            TypeConverter typeConverter = getTypeConverter(fieldName);
            Object objValue = typeConverter.convert(value, declaredField.getType());
            BeanUtils.setProperty(entity, fieldName, objValue);
        } catch (Exception ex) {

            log.error("Error filling the entity with values", ex);
        }
    }

    private TypeConverter getTypeConverter(String fieldName) {
        TypeConverter typeConverter = getTypeConvertersMap().get(fieldName);
        if (typeConverter == null) {
            return defaultConverter;
        }
        return typeConverter;
    }

    /**
     * Resets the export only fields back to default values. This is needed as import VM command fills the vm static
     * with read values from OVF but loggable fields should just be sent to audit log
     *
     * @param defaultEntity
     *            an entity that is provided in order to get the default values from
     * @param useDefaultFromDefaultEntity
     *            flag that determines if to get default values from the default entity or from the annotation
     */
    public void resetDefaults(T defaultEntity) {
        try {
            for (String fieldName : fieldNames) {
                Field declaredField = entity.getClass().getDeclaredField(fieldName);
                OvfExportOnlyField ovfLogEventField = declaredField.getAnnotation(OvfExportOnlyField.class);

                String defaultValue = "";

                // If the field was exported, set it during the defaults
                // restoration
                if (ovfLogEventField.exportOption() != ExportOption.DONT_EXPORT) {
                    defaultValue = BeanUtils.getProperty(defaultEntity, fieldName);
                    TypeConverter typeConverter = getTypeConverter(fieldName);
                    Object objValue = typeConverter.convert(defaultValue, declaredField.getType());
                    BeanUtils.setProperty(entity, fieldName, objValue);
                }
            }

        } catch (Exception ex) {

            log.error("Error resseting the log event fields to default values ", ex);
        }
    }

    public java.util.List<String> getAliases() {
        return aliases;
    }

    private static LogCompat log = LogFactoryCompat.getLog(OvfLogEventHandler.class);

}
