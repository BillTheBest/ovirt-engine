package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.StringBuilderCompat;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;

public class ADSyntaxChecker implements ISyntaxChecker {
    private AdSearchObjecAutoCompleter mSearchObjectAC;
    private BaseAutoCompleter mColonAC;
    private BaseAutoCompleter mPluralAC;
    private java.util.HashMap<SyntaxObjectType, SyntaxObjectType[]> mStateMap;
    protected final static String USER_ACCOUNT_TYPE = "$USER_ACCOUNT_TYPE";
    private final static String LDAP_GROUP_CATEGORY = "$LDAP_GROUP_CATEGORY";

    private Regex mFirstDQRegexp;
    private Regex mNonSpaceRegexp;

    public ADSyntaxChecker() {
        mSearchObjectAC = new AdSearchObjecAutoCompleter();
        mColonAC = new BaseAutoCompleter(":");
        mPluralAC = new BaseAutoCompleter("S");

        mFirstDQRegexp = new Regex("^\\s*\"$");
        mNonSpaceRegexp = new Regex("^\\S+$");

        mStateMap = new java.util.HashMap<SyntaxObjectType, SyntaxObjectType[]>();
        SyntaxObjectType[] beginArray = { SyntaxObjectType.SEARCH_OBJECT };
        mStateMap.put(SyntaxObjectType.BEGIN, beginArray);
        SyntaxObjectType[] searchObjectArray = { SyntaxObjectType.COLON };
        mStateMap.put(SyntaxObjectType.SEARCH_OBJECT, searchObjectArray);
        SyntaxObjectType[] colonArray = { SyntaxObjectType.CONDITION_FIELD, SyntaxObjectType.END };
        mStateMap.put(SyntaxObjectType.COLON, colonArray);
        SyntaxObjectType[] conditionFieldArray = { SyntaxObjectType.CONDITION_RELATION };
        mStateMap.put(SyntaxObjectType.CONDITION_FIELD, conditionFieldArray);
        SyntaxObjectType[] conditionRelationArray = { SyntaxObjectType.CONDITION_VALUE };
        mStateMap.put(SyntaxObjectType.CONDITION_RELATION, conditionRelationArray);
        SyntaxObjectType[] conditionValueArray = { SyntaxObjectType.CONDITION_FIELD };
        mStateMap.put(SyntaxObjectType.CONDITION_VALUE, conditionValueArray);
    }

    public SyntaxContainer analyzeSyntaxState(String searchText, boolean final2) {
        SyntaxContainer retval = new SyntaxContainer(searchText);
        IConditionFieldAutoCompleter AdConditionFieldAC;
        if (searchText.toUpperCase().contains("ADUSER")) {
            AdConditionFieldAC = new AdUserConditionFieldAutoCompleter();
        } else {
            AdConditionFieldAC = new AdGroupConditionFieldAutoCompleter();
        }
        IAutoCompleter conditionRelationAC;
        char[] searchCharArr = searchText.toCharArray();
        boolean betweenDoubleQuotes = false;
        int curStartPos = 0;
        String curConditionField = "";
        for (int idx = 0; idx < searchCharArr.length; idx++) {
            SyntaxObjectType curState = retval.getState();
            char curChar = searchCharArr[idx];
            if ((curChar == ' ') && (curState != SyntaxObjectType.CONDITION_RELATION)) {
                curStartPos += 1;
                continue;
            }
            String strRealObj = searchText.substring(curStartPos, idx + 1);
            String nextObject = strRealObj.toUpperCase();
            switch (curState) {
            case BEGIN:
                // we have found a search-object
                if (!mSearchObjectAC.validate(nextObject)) {
                    if (!mSearchObjectAC.validateCompletion(nextObject)) {
                        // ERROR INVALID-SEARCH OBJECT
                        retval.setErr(SyntaxError.INVALID_SEARCH_OBJECT, curStartPos, idx - curStartPos + 1);
                        return retval;
                    }
                } else {
                    if (searchCharArr.length >= idx + 2) // Check that this
                                                         // maybe a plural
                    {
                        // Validate that the next character is an 's'
                        if (mPluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
                            // Then just move things along.
                            idx++;
                            StringBuilderCompat sb = new StringBuilderCompat(nextObject);
                            sb.append('S');
                            nextObject = sb.toString();
                        }
                    }
                    retval.addSyntaxObject(SyntaxObjectType.SEARCH_OBJECT, nextObject, curStartPos, idx + 1);
                    retval.setvalid(true);
                    curStartPos = idx + 1;
                }
                break;

            case SEARCH_OBJECT:

                if (!mColonAC.validate(nextObject)) {
                    if (!mColonAC.validateCompletion(nextObject)) {
                        retval.setErr(SyntaxError.COLON_NOT_NEXT_TO_SEARCH_OBJECT, curStartPos, idx + 1);
                        return retval;
                    }
                } else {
                    retval.addSyntaxObject(SyntaxObjectType.COLON, nextObject, idx, idx + 1);
                    curStartPos = idx + 1;
                    retval.setvalid(true);
                }
                break;

            case COLON:
            case CONDITION_VALUE:
                if (AdConditionFieldAC.validate(nextObject)) {
                    retval.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD, nextObject, curStartPos, idx + 1);
                    curConditionField = nextObject;
                    curStartPos = idx + 1;

                } else if (!AdConditionFieldAC.validateCompletion(nextObject)) // &&
                                                                               // (!mSortbyAC.validateCompletion(nextObject)))
                {
                    retval.setErr(SyntaxError.INVALID_CONDITION_FILED, curStartPos, idx + 1);
                    return retval;
                }
                retval.setvalid(false);
                break;

            case CONDITION_FIELD:
                conditionRelationAC = AdConditionFieldAC.getFieldRelationshipAutoCompleter(curConditionField);
                if (conditionRelationAC == null) {
                    retval.setErr(SyntaxError.CONDITION_CANT_CREATE_RRELATIONS_AC, curStartPos, idx + 1);
                    return retval;
                }
                if (idx + 1 < searchCharArr.length) {
                    String tryNextObj = searchText.substring(curStartPos, idx + 2).toUpperCase();
                    if (conditionRelationAC.validate(tryNextObj)) {
                        break;
                    }
                }
                if (!conditionRelationAC.validate(nextObject)) {
                    if (!conditionRelationAC.validateCompletion(nextObject)) {
                        retval.setErr(SyntaxError.INVALID_CONDITION_RELATION, curStartPos, idx + 1);
                        return retval;
                    }
                } else {
                    retval.addSyntaxObject(SyntaxObjectType.CONDITION_RELATION, nextObject, curStartPos, idx + 1);
                }
                curStartPos = idx + 1;
                retval.setvalid(false);

                break;
            case CONDITION_RELATION:
                boolean addObjFlag = false;
                if (curChar == '"') {
                    betweenDoubleQuotes = (!betweenDoubleQuotes);
                    if (betweenDoubleQuotes) {
                        if (!mFirstDQRegexp.IsMatch(strRealObj)) {
                            retval.setErr(SyntaxError.INVALID_CONDITION_VALUE, curStartPos, idx + 1);
                            return retval;
                        }
                    } else {
                        strRealObj = StringHelper.trim(strRealObj, new char[] { '\"' });
                        addObjFlag = true;
                    }
                }
                // Doing this condition to identify whether this is the last
                // searchObject and no space is predicted !!
                if (final2) {
                    if (((curChar == ' ') || (idx + 1 == searchCharArr.length)) && (betweenDoubleQuotes == false)
                            && (addObjFlag == false)) {
                        strRealObj = strRealObj.trim();
                        if (mNonSpaceRegexp.IsMatch(strRealObj)) {
                            addObjFlag = true;
                        } else {
                            curStartPos = idx + 1;
                        }
                    }
                } else {
                    if ((curChar == ' ') && (betweenDoubleQuotes == false) && (addObjFlag == false)) {
                        strRealObj = strRealObj.trim();
                        if (mNonSpaceRegexp.IsMatch(strRealObj)) {
                            addObjFlag = true;
                        } else {
                            curStartPos = idx + 1;
                        }
                    }
                }
                if (addObjFlag) {
                    if (!AdConditionFieldAC.validateFieldValue(curConditionField, strRealObj)) {
                        retval.setErr(SyntaxError.INVALID_CONDITION_VALUE, curStartPos, idx);
                        return retval;
                    } else {
                        retval.addSyntaxObject(SyntaxObjectType.CONDITION_VALUE, strRealObj, curStartPos, idx + 1);
                        curConditionField = "";
                    }
                    curStartPos = idx + 1;
                    retval.setvalid(true);
                }
                break;
            default:
                retval.setErr(SyntaxError.UNIDENTIFIED_STATE, curStartPos, idx);
                return retval;
            }
        }
        return retval;

    }

    public SyntaxContainer getCompletion(String searchText) {
        SyntaxContainer retval = analyzeSyntaxState(searchText, false);
        IConditionFieldAutoCompleter AdConditionFieldAC;
        if (retval.getError() == SyntaxError.NO_ERROR) {
            if (searchText.toUpperCase().contains("ADUSER")) {
                AdConditionFieldAC = new AdUserConditionFieldAutoCompleter();
            } else {
                AdConditionFieldAC = new AdGroupConditionFieldAutoCompleter();
            }
            IAutoCompleter conditionRelationAC;
            IConditionValueAutoCompleter conditionValueAC;
            int lastIdx = retval.getLastHandledIndex();
            String curPartialWord = "";
            if (lastIdx < searchText.length()) {
                curPartialWord = searchText.substring(lastIdx, searchText.length());
                curPartialWord = curPartialWord.trim();
            }
            SyntaxObjectType curState = retval.getState();
            for (int idx = 0; idx < mStateMap.get(curState).length; idx++) {
                switch (mStateMap.get(curState)[idx]) {
                case SEARCH_OBJECT:
                    retval.addToACList(mSearchObjectAC.getCompletion(curPartialWord));
                    break;
                case COLON:
                    retval.addToACList(mColonAC.getCompletion(curPartialWord));
                    break;
                case CONDITION_FIELD:
                    String[] tmpCompletions = AdConditionFieldAC.getCompletion(curPartialWord);
                    java.util.ArrayList<String> nonDuplicates = new java.util.ArrayList<String>();
                    for (int itr = 0; itr < tmpCompletions.length; itr++) {
                        if (!retval.contains(SyntaxObjectType.CONDITION_FIELD, tmpCompletions[itr])) {
                            nonDuplicates.add(tmpCompletions[itr]);
                        }
                    }
                    retval.addToACList(nonDuplicates.toArray(new String[] {}));
                    break;
                case CONDITION_RELATION:
                    conditionRelationAC = AdConditionFieldAC.getFieldRelationshipAutoCompleter(retval
                            .getPreviousSyntaxObject(1, SyntaxObjectType.CONDITION_FIELD));
                    if (conditionRelationAC != null) {
                        retval.addToACList(conditionRelationAC.getCompletion(curPartialWord));
                    }
                    break;
                case CONDITION_VALUE:
                    conditionValueAC = AdConditionFieldAC.getFieldValueAutoCompleter(retval.getPreviousSyntaxObject(2,
                            SyntaxObjectType.CONDITION_FIELD));
                    if (conditionValueAC != null) {
                        retval.addToACList(conditionValueAC.getCompletion(curPartialWord));
                    }
                    break;
                }
            }
        }
        return retval;
    }

    public String generateQueryFromSyntaxContainer(SyntaxContainer syntax, boolean isSafe) {
        String retval = "";
        if (syntax.getvalid()) {
            retval = generateAdQueryFromSyntaxContainer(syntax);
        }
        return retval;
    }

    private static String generateAdQueryFromSyntaxContainer(SyntaxContainer syntax) {
        StringBuilderCompat retval = new StringBuilderCompat();
        if (syntax.getvalid()) {
            IConditionFieldAutoCompleter conditionFieldAC;
            if (syntax.getSearchObjectStr().toUpperCase().contains("ADUSER")) {
                retval.append("(&");
                retval.append("(" + USER_ACCOUNT_TYPE + ")");
                conditionFieldAC = new AdUserConditionFieldAutoCompleter();
            } else {
                retval.append("(&(" + LDAP_GROUP_CATEGORY + ")");
                conditionFieldAC = new AdGroupConditionFieldAutoCompleter();
            }
            StringBuilderCompat phrase = new StringBuilderCompat();
            boolean nonEqual = false;
            boolean findAll = false;
            for (SyntaxObject so : syntax) {
                switch (so.getType()) {
                case CONDITION_FIELD:
                    if (StringHelper.EqOp(so.getBody(), "ALLNAMES")) {
                        phrase.append(" (|($GIVENNAME={value})(sn={value})($USER_ACCOUNT_NAME={value})($PRINCIPAL_NAME={value}))");
                        /**
                         * mark this search as findAll for later use
                         */
                        findAll = true;
                    } else {
                        phrase.append(StringFormat.format(" (%1$s", conditionFieldAC.getDbFieldName(so.getBody())));
                    }
                    break;
                case CONDITION_RELATION:
                    /**
                     * append '=' only if not finding all
                     */
                    if (!findAll) {
                        phrase.append("=");
                    }
                    if (StringHelper.EqOp(so.getBody(), "!=")) {
                        nonEqual = true;
                    }
                    break;
                case CONDITION_VALUE:
                    if (findAll) {
                        /**
                         * replace all {value} occurences with the value searched. We escape the $ here for regex match,
                         * as it is used in replace.
                         */
                        phrase.replace("{value}", so.getBody().replace("$", "\\$"));
                    } else {
                        phrase.append(StringFormat.format("%1$s)", so.getBody()));
                    }
                    if (nonEqual) {
                        retval.append(StringFormat.format("(!%1$s)", phrase));
                    } else {
                        retval.append(phrase.toString());
                    }
                    nonEqual = false;
                    findAll = false;
                    phrase.delete(0, phrase.length());
                    break;
                default:
                    break;
                }

            }

        }
        retval.append(")");
        return retval.toString();

    }
}
