package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;

/**
 * An interface to be implemented by all Condition fields auto completers
 */
public interface IConditionFieldAutoCompleter extends IAutoCompleter {
    boolean validateFieldValue(String fieldName, String fieldValue);

    String getDbFieldName(String fieldName);

    java.lang.Class getDbFieldType(String fieldName);

    IAutoCompleter getFieldRelationshipAutoCompleter(String fieldName);

    IConditionValueAutoCompleter getFieldValueAutoCompleter(String fieldName);

    String buildFreeTextConditionSql(String tableName, String relations, String value, boolean caseSensitive);

    String buildConditionSql(String fieldName, String customizedValue, String customizedRelation, String tableName, boolean caseSensitive);

    void formatValue(String fieldName, RefObject<String> relations, RefObject<String> value, boolean caseSensitive);
}
