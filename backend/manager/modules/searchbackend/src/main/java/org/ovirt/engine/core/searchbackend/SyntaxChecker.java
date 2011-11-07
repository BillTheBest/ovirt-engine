package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.SqlInjectionException;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.EnumCompat;
import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.core.compat.StringHelper;


public class SyntaxChecker implements ISyntaxChecker {
    private SearchObjectAutoCompleter mSearchObjectAC;
    private BaseAutoCompleter mColonAC;
    private BaseAutoCompleter mPluralAC;
    private BaseAutoCompleter mSortbyAC;
    private BaseAutoCompleter mPageAC;
    private BaseAutoCompleter mAndAC;
    private BaseAutoCompleter mOrAC;
    private BaseAutoCompleter mDotAC;
    private BaseAutoCompleter mSortDirectionAC;
    private java.util.HashMap<SyntaxObjectType, SyntaxObjectType[]> mStateMap;

    private Regex mFirstDQRegexp;
    private Regex mNonSpaceRegexp;
    private java.util.ArrayList<Character> mDisAllowedChars;
    private static SqlInjectionChecker sqlInjectionChecker;

    public SyntaxChecker(int searchReasultsLimit, boolean hasDesktop) {

        mSearchObjectAC = new SearchObjectAutoCompleter(hasDesktop);
        mColonAC = new BaseAutoCompleter(":");
        mPluralAC = new BaseAutoCompleter("S");
        mSortbyAC = new BaseAutoCompleter("SORTBY");
        mPageAC = new BaseAutoCompleter("PAGE");
        mSortDirectionAC = new BaseAutoCompleter(new String[] { "ASC", "DESC" });
        mAndAC = new BaseAutoCompleter("AND");
        mOrAC = new BaseAutoCompleter("OR");
        mDotAC = new BaseAutoCompleter(".");
        mDisAllowedChars = new java.util.ArrayList<Character>(java.util.Arrays.asList(new Character[] { '\'', ';' }));

        mFirstDQRegexp = new Regex("^\\s*\"$");
        mNonSpaceRegexp = new Regex("^\\S+$");

        mStateMap = new java.util.HashMap<SyntaxObjectType, SyntaxObjectType[]>();
        mStateMap.put(SyntaxObjectType.BEGIN, new SyntaxObjectType[] { SyntaxObjectType.SEARCH_OBJECT });
        mStateMap.put(SyntaxObjectType.SEARCH_OBJECT, new SyntaxObjectType[] { SyntaxObjectType.COLON });
        SyntaxObjectType[] afterColon =
                { SyntaxObjectType.CROSS_REF_OBJ, SyntaxObjectType.CONDITION_FIELD,
                        SyntaxObjectType.SORTBY, SyntaxObjectType.PAGE, SyntaxObjectType.CONDITION_VALUE,
                        SyntaxObjectType.END };
        mStateMap.put(SyntaxObjectType.COLON, afterColon);

        SyntaxObjectType[] afterCrossRefObj = { SyntaxObjectType.DOT, SyntaxObjectType.CONDITION_RELATION };
        mStateMap.put(SyntaxObjectType.CROSS_REF_OBJ, afterCrossRefObj);
        mStateMap.put(SyntaxObjectType.DOT, new SyntaxObjectType[] { SyntaxObjectType.CONDITION_FIELD });

        mStateMap.put(SyntaxObjectType.CONDITION_FIELD, new SyntaxObjectType[] { SyntaxObjectType.CONDITION_RELATION });
        mStateMap.put(SyntaxObjectType.CONDITION_RELATION, new SyntaxObjectType[] { SyntaxObjectType.CONDITION_VALUE });
        SyntaxObjectType[] afterConditionValue = { SyntaxObjectType.OR, SyntaxObjectType.AND,
                SyntaxObjectType.CROSS_REF_OBJ, SyntaxObjectType.CONDITION_FIELD, SyntaxObjectType.SORTBY,
                SyntaxObjectType.PAGE, SyntaxObjectType.CONDITION_VALUE };
        mStateMap.put(SyntaxObjectType.CONDITION_VALUE, afterConditionValue);

        SyntaxObjectType[] AndOrArray = { SyntaxObjectType.CROSS_REF_OBJ, SyntaxObjectType.CONDITION_FIELD,
                SyntaxObjectType.CONDITION_VALUE };
        mStateMap.put(SyntaxObjectType.AND, AndOrArray);
        mStateMap.put(SyntaxObjectType.OR, AndOrArray);

        mStateMap.put(SyntaxObjectType.SORTBY, new SyntaxObjectType[] { SyntaxObjectType.SORT_FIELD });
        mStateMap.put(SyntaxObjectType.SORT_FIELD, new SyntaxObjectType[] { SyntaxObjectType.SORT_DIRECTION });
        mStateMap.put(SyntaxObjectType.SORT_DIRECTION, new SyntaxObjectType[] { SyntaxObjectType.PAGE });

        mStateMap.put(SyntaxObjectType.PAGE, new SyntaxObjectType[] { SyntaxObjectType.PAGE_VALUE });
        mStateMap.put(SyntaxObjectType.PAGE_VALUE, new SyntaxObjectType[] { SyntaxObjectType.END });
        // get sql injection checker for active database engine.
        try {
            sqlInjectionChecker = getSqlInjectionChecker();
        } catch (Exception e) {
            log.errorFormat("Failed to load Sql Injection Checker. {0}", e.getMessage());
        }
    }

    private enum ValueParseResult {
        Err,
        Normal,
        FreeText;

        public int getValue() {
            return this.ordinal();
        }

        public static ValueParseResult forValue(int value) {
            return values()[value];
        }
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: final is a keyword in Java. Change
    // the name:
    private ValueParseResult handleValuePhrase(boolean final2, String searchText, int idx, RefObject<Integer> startPos,
            SyntaxContainer container) {
        boolean addObjFlag = false;
        ValueParseResult retval = ValueParseResult.Normal;
        IConditionFieldAutoCompleter curConditionFieldAC;
        char curChar = searchText.charAt(idx);
        String strRealObj = searchText.substring(startPos.argvalue, idx + 1);

        boolean betweenDoubleQuotes = searchText.substring(startPos.argvalue, idx).contains("\"");
        if (curChar == '"') {
            betweenDoubleQuotes = (!betweenDoubleQuotes);
            if (betweenDoubleQuotes) {
                if (!mFirstDQRegexp.IsMatch(strRealObj)) {
                    container.setErr(SyntaxError.INVALID_CONDITION_VALUE, startPos.argvalue, idx + 1);
                    return ValueParseResult.Err;
                }
            } else {
                strRealObj = StringHelper.trim(strRealObj, new char[] { '\"' });
                addObjFlag = true;
            }
        }
        // Doing this condition to identify whether this is the last
        // searchObject and no space is predicted !!
        if (final2) {
            if (((curChar == ' ') || (idx + 1 == searchText.length())) && (betweenDoubleQuotes == false)
                    && (addObjFlag == false)) {
                strRealObj = strRealObj.trim();
                if (mNonSpaceRegexp.IsMatch(strRealObj)) {
                    addObjFlag = true;
                } else {
                    startPos.argvalue = idx + 1;
                }
            }
        } else {
            if ((curChar == ' ') && (betweenDoubleQuotes == false) && (addObjFlag == false)) {
                strRealObj = strRealObj.trim();
                if (mNonSpaceRegexp.IsMatch(strRealObj)) {
                    addObjFlag = true;
                } else {
                    startPos.argvalue = idx + 1;
                }
            }
        }
        if (addObjFlag) {
            String curRefObj = container.getPreviousSyntaxObject(3, SyntaxObjectType.CROSS_REF_OBJ);
            String curConditionField = container.getPreviousSyntaxObject(1, SyntaxObjectType.CONDITION_FIELD);
            curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(curRefObj);
            if (curConditionFieldAC == null) {
                container.setErr(SyntaxError.CANT_GET_CONDITION_FIELD_AC, startPos.argvalue, idx);
                return ValueParseResult.Err;
            }
            if ((!StringHelper.EqOp(curConditionField, ""))
                    && (!curConditionFieldAC.validateFieldValue(curConditionField, strRealObj))) {
                container.setErr(SyntaxError.INVALID_CONDITION_VALUE, startPos.argvalue, idx);
                return ValueParseResult.Err;
            }
            container.addSyntaxObject(SyntaxObjectType.CONDITION_VALUE, strRealObj, startPos.argvalue, idx + 1);
            retval = ValueParseResult.FreeText;
            startPos.argvalue = idx + 1;
            container.setvalid(true);
        }
        return retval;
    }

    /**
     * gets the sql injection checker class for current db vendor.
     * @return SqlInjectionChecker
     * @throws Exception
     */
    private SqlInjectionChecker getSqlInjectionChecker() throws Exception {
        // This can not be done with reflection like:
        //    return (SqlInjectionChecker) Class.forName(props.getProperty(SQL_INJECTION)).newInstance();
        // GWT lacks support of reflection.
        if  (((String)Config.GetValue(ConfigValues.DBEngine)).equalsIgnoreCase("postgres")){
                return new PostgresSqlInjectionChecker();
        }
        else {
            throw new IllegalStateException("Failed to get correct sql injection checker instance name :" + SqlInjectionChecker.class);
        }

    }
    // VB & C# TO JAVA CONVERTER TODO TASK: final is a keyword in Java. Change
    // the name:
    public SyntaxContainer analyzeSyntaxState(String searchText, boolean final2) {
        SyntaxContainer retval = new SyntaxContainer(searchText);
        IConditionFieldAutoCompleter curConditionFieldAC = null;
        IAutoCompleter curConditionRelationAC = null;
        java.util.ArrayList<String> freeTextObjSearched = new java.util.ArrayList<String>();
        char[] searchCharArr = searchText.toCharArray();
        int curStartPos = 0;

        String tryNextObj = "";
        boolean keepValid;
        for (int idx = 0; idx < searchCharArr.length; idx++) {
            SyntaxObjectType curState = retval.getState();
            char curChar = searchCharArr[idx];
            if (mDisAllowedChars.contains(curChar)) {
                retval.setErr(SyntaxError.INVALID_CHARECTER, curStartPos, idx + 1);
                return retval;
            }
            if ((curChar == ' ') && (curState != SyntaxObjectType.CONDITION_RELATION)
                    && (curState != SyntaxObjectType.COLON) && (curState != SyntaxObjectType.CONDITION_VALUE)
                    && (curState != SyntaxObjectType.OR) && (curState != SyntaxObjectType.AND)) {
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
                            StringBuilder sb = new StringBuilder(nextObject);
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
            case CROSS_REF_OBJ:
                String curRefObj = retval.getPreviousSyntaxObject(0, SyntaxObjectType.CROSS_REF_OBJ);
                curConditionRelationAC = mSearchObjectAC.getObjectRelationshipAutoCompleter(curRefObj);
                if (idx + 1 < searchCharArr.length) {
                    tryNextObj = searchText.substring(curStartPos, idx + 2).toUpperCase();
                }
                if (curConditionRelationAC == null) {
                    retval.setErr(SyntaxError.CONDITION_CANT_CREATE_RRELATIONS_AC, curStartPos, idx + 1);
                    return retval;
                }
                if (mDotAC.validate(nextObject)) {
                    retval.addSyntaxObject(SyntaxObjectType.DOT, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else if ((!StringHelper.EqOp(tryNextObj, "")) && (curConditionRelationAC.validate(tryNextObj))) {
                    break; // i.e. the relation object has another charecter
                } else if (curConditionRelationAC.validate(nextObject)) {
                    retval.addSyntaxObject(SyntaxObjectType.CONDITION_RELATION, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;

                } else if ((!curConditionRelationAC.validateCompletion(nextObject))
                        && (!mDotAC.validateCompletion(nextObject))) {
                    retval.setErr(SyntaxError.INVALID_POST_CROSS_REF_OBJ, curStartPos, idx + 1);
                    return retval;
                }
                tryNextObj = "";
                break;
            case DOT:
                curRefObj = retval.getPreviousSyntaxObject(1, SyntaxObjectType.CROSS_REF_OBJ);
                curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(curRefObj);
                if (curConditionFieldAC == null) {

                    retval.setErr(SyntaxError.CANT_GET_CONDITION_FIELD_AC, curStartPos, idx);
                    return retval;
                }
                if (!curConditionFieldAC.validate(nextObject)) {
                    if (!curConditionFieldAC.validateCompletion(nextObject)) {
                        retval.setErr(SyntaxError.INVALID_CONDITION_FILED, curStartPos, idx + 1);
                        return retval;
                    }
                } else {
                    retval.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                }
                break;
            case AND:
            case OR:
                keepValid = false;
                curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(retval.getSearchObjectStr());
                if (curConditionFieldAC.validate(nextObject)) {
                    retval.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;

                } else if (mSearchObjectAC.isCrossReferece(nextObject, retval.getFirst().getBody())) {
                    if (searchCharArr.length >= idx + 2) // Check that this
                                                         // maybe a plural
                    {
                        // Validate that the next character is an 's'
                        if (mPluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
                            // Then just move things along.
                            idx++;
                            StringBuilder sb = new StringBuilder(nextObject);
                            sb.append('S');
                            nextObject = sb.toString();
                        }
                    }
                    retval.addSyntaxObject(SyntaxObjectType.CROSS_REF_OBJ, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else {
                    RefObject<Integer> tempRefObject = new RefObject<Integer>(curStartPos);
                    ValueParseResult ans = handleValuePhrase(final2, searchText, idx, tempRefObject, retval);
                    curStartPos = tempRefObject.argvalue;
                    if (ans != ValueParseResult.Err) {
                        if (ans == ValueParseResult.FreeText) {
                            curRefObj = retval.getSearchObjectStr();
                            if (freeTextObjSearched.contains(curRefObj)) {
                                retval.setErr(SyntaxError.FREE_TEXT_ALLOWED_ONCE_PER_OBJ, curStartPos, idx + 1);
                                return retval;
                            }
                            freeTextObjSearched.add(curRefObj);
                            retval.setvalid(true);
                            keepValid = true;
                        }
                    } else if ((!curConditionFieldAC.validateCompletion(nextObject))
                            && (!mSearchObjectAC.validateCompletion(nextObject))) {
                        retval.setErr(SyntaxError.INVALID_POST_OR_AND_PHRASE, curStartPos, idx + 1);
                        return retval;
                    }
                }
                if (keepValid == false) {
                    retval.setvalid(false);
                }
                break;
            case COLON:
                keepValid = false;
                curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(retval.getSearchObjectStr());
                if (curConditionFieldAC.validate(nextObject)) {
                    retval.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;

                } else if (mSortbyAC.validate(nextObject)) {
                    retval.addSyntaxObject(SyntaxObjectType.SORTBY, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else if (mPageAC.validate(nextObject)) {
                    retval.addSyntaxObject(SyntaxObjectType.PAGE, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else if (mSearchObjectAC.isCrossReferece(nextObject, retval.getFirst().getBody())) {
                    if (searchCharArr.length >= idx + 2) // Check that this
                                                         // maybe a plural
                    {
                        // Validate that the next character is an 's'
                        if (mPluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
                            // Then just move things along.
                            idx++;
                            StringBuilder sb = new StringBuilder(nextObject);
                            sb.append('S');
                            nextObject = sb.toString();
                        }
                    }
                    retval.addSyntaxObject(SyntaxObjectType.CROSS_REF_OBJ, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                } else {
                    RefObject<Integer> tempRefObject2 = new RefObject<Integer>(curStartPos);
                    ValueParseResult ans = handleValuePhrase(final2, searchText, idx, tempRefObject2, retval);
                    curStartPos = tempRefObject2.argvalue;
                    if (ans != ValueParseResult.Err) {
                        if (ans == ValueParseResult.FreeText) {
                            freeTextObjSearched.add(retval.getSearchObjectStr());
                        }
                        keepValid = true;
                    } else if ((!curConditionFieldAC.validateCompletion(nextObject))
                            && (!mSortbyAC.validateCompletion(nextObject))
                            && (!mSearchObjectAC.validateCompletion(nextObject))) {
                        retval.setErr(SyntaxError.INVALID_POST_COLON_PHRASE, curStartPos, idx + 1);
                        return retval;
                    }
                }
                if (keepValid == false) {
                    retval.setvalid(false);
                }
                break;
            case CONDITION_VALUE:
                nextObject = nextObject.trim();
                if (nextObject.length() > 0) {
                    keepValid = false;
                    curRefObj = retval.getSearchObjectStr();
                    curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(curRefObj);
                    if (curConditionFieldAC.validate(nextObject)) {
                        retval.addSyntaxObject(SyntaxObjectType.CONDITION_FIELD, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;

                    } else if (mSortbyAC.validate(nextObject)) {
                        retval.addSyntaxObject(SyntaxObjectType.SORTBY, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (mPageAC.validate(nextObject)) {
                        retval.addSyntaxObject(SyntaxObjectType.PAGE, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (mSearchObjectAC.isCrossReferece(nextObject, retval.getFirst().getBody())) {
                        if (searchCharArr.length >= idx + 2) // Check that this
                                                             // maybe a
                                                             // plural
                        {
                            // Validate that the next character is an 's'
                            if (mPluralAC.validate(searchText.substring(idx + 1, idx + 1 + 1))) {
                                // Then just move things along.
                                idx++;
                                StringBuilder sb = new StringBuilder(nextObject);
                                sb.append('S');
                                nextObject = sb.toString();
                            }
                        }
                        retval.addSyntaxObject(SyntaxObjectType.CROSS_REF_OBJ, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (mAndAC.validate(nextObject)) {
                        retval.addSyntaxObject(SyntaxObjectType.AND, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    } else if (mOrAC.validate(nextObject)) {
                        retval.addSyntaxObject(SyntaxObjectType.OR, nextObject, curStartPos, idx + 1);
                        curStartPos = idx + 1;
                    }

                    else if ((!curConditionFieldAC.validateCompletion(nextObject))
                            && (!mSortbyAC.validateCompletion(nextObject))
                            && (!mSearchObjectAC.validateCompletion(nextObject))
                            && (!mAndAC.validateCompletion(nextObject)) && (!mOrAC.validateCompletion(nextObject))) {
                        RefObject<Integer> tempRefObject3 = new RefObject<Integer>(curStartPos);
                        ValueParseResult ans = handleValuePhrase(final2, searchText, idx, tempRefObject3, retval);
                        curStartPos = tempRefObject3.argvalue;
                        if (ans != ValueParseResult.Err) {
                            if (ans == ValueParseResult.FreeText) {
                                if (freeTextObjSearched.contains(curRefObj)) {
                                    retval.setErr(SyntaxError.FREE_TEXT_ALLOWED_ONCE_PER_OBJ, curStartPos, idx + 1);
                                    return retval;
                                }
                                freeTextObjSearched.add(curRefObj);
                                retval.setvalid(true);
                                keepValid = true;
                            }
                        } else {
                            retval.setErr(SyntaxError.INVALID_POST_CONDITION_VALUE_PHRASE, curStartPos, idx + 1);
                            return retval;
                        }
                    }
                    if (keepValid == false) {
                        retval.setvalid(false);
                    }
                }
                break;
            case CONDITION_FIELD:
                curRefObj = retval.getPreviousSyntaxObject(2, SyntaxObjectType.CROSS_REF_OBJ);
                String curConditionField = retval.getPreviousSyntaxObject(0, SyntaxObjectType.CONDITION_FIELD);
                curConditionRelationAC = mSearchObjectAC
                        .getFieldRelationshipAutoCompleter(curRefObj, curConditionField);
                if (curConditionRelationAC == null) {
                    retval.setErr(SyntaxError.CONDITION_CANT_CREATE_RRELATIONS_AC, curStartPos, idx + 1);
                    return retval;
                }
                if (idx + 1 < searchCharArr.length) {
                    tryNextObj = searchText.substring(curStartPos, idx + 2).toUpperCase();
                    if (curConditionRelationAC.validate(tryNextObj)) {
                        break;
                    }
                }
                if (!curConditionRelationAC.validate(nextObject)) {
                    if (!curConditionRelationAC.validateCompletion(nextObject)) {
                        retval.setErr(SyntaxError.INVALID_CONDITION_RELATION, curStartPos, idx + 1);
                        return retval;
                    }
                } else {
                    retval.addSyntaxObject(SyntaxObjectType.CONDITION_RELATION, nextObject, curStartPos, idx + 1);
                }
                curStartPos = idx + 1;
                retval.setvalid(false);
                tryNextObj = "";

                break;
            case CONDITION_RELATION: {
                RefObject<Integer> tempRefObject4 = new RefObject<Integer>(curStartPos);
                ValueParseResult ans = handleValuePhrase(final2, searchText, idx, tempRefObject4, retval);
                curStartPos = tempRefObject4.argvalue;
                if (ans == ValueParseResult.Err) {
                    return retval;
                }
                if (ans == ValueParseResult.FreeText) {
                    if (retval.getPreviousSyntaxObjectType(2) == SyntaxObjectType.CROSS_REF_OBJ) {
                        curRefObj = retval.getObjSingularName(retval.getPreviousSyntaxObject(2,
                                SyntaxObjectType.CROSS_REF_OBJ));
                        if (freeTextObjSearched.contains(curRefObj)) {
                            retval.setErr(SyntaxError.FREE_TEXT_ALLOWED_ONCE_PER_OBJ, curStartPos, idx + 1);
                            return retval;
                        }
                        freeTextObjSearched.add(curRefObj);
                    }
                }
            }
                break;
            case SORTBY:
                curConditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(retval.getSearchObjectStr());
                if (!curConditionFieldAC.validate(nextObject)) {
                    if (!curConditionFieldAC.validateCompletion(nextObject)) {
                        retval.setErr(SyntaxError.INVALID_SORT_FIELD, curStartPos, idx + 1);
                        return retval;
                    }
                } else {
                    retval.addSyntaxObject(SyntaxObjectType.SORT_FIELD, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                    retval.setvalid(true);
                }
                break;
            case PAGE:
                Integer pageNumber = new Integer(0);
                RefObject<Integer> tempRefObject5 = new RefObject<Integer>(pageNumber);
                boolean tempVar = !IntegerCompat.TryParse(nextObject, tempRefObject5);
                pageNumber = tempRefObject5.argvalue;
                if (tempVar) {
                    retval.setErr(SyntaxError.INVALID_CHARECTER, curStartPos, idx + 1);
                    return retval;
                } else {
                    String s = "";
                    int pos = idx;
                    // parsing the whole page number (can be more than one char)
                    while (pos < searchText.length() - 1 && Character.isDigit(nextObject.charAt(0))) {
                        s += nextObject;
                        pos++;
                        strRealObj = searchText.substring(pos, pos + 1);
                        nextObject = strRealObj.toUpperCase();
                    }
                    s += nextObject;
                    retval.addSyntaxObject(SyntaxObjectType.PAGE_VALUE, s, curStartPos, idx + s.length());
                    // update index position
                    idx = pos + 1;
                    retval.setvalid(true);
                }
                break;
            case SORT_FIELD:
                if (!mSortDirectionAC.validate(nextObject)) {
                    if (!mSortDirectionAC.validateCompletion(nextObject)) {
                        retval.setErr(SyntaxError.INVALID_SORT_DIRECTION, curStartPos, idx + 1);
                        return retval;
                    }
                } else {
                    retval.addSyntaxObject(SyntaxObjectType.SORT_DIRECTION, nextObject, curStartPos, idx + 1);
                    curStartPos = idx + 1;
                    retval.setvalid(true);
                }
                break;
            case PAGE_VALUE:
                if (curChar != ' ') {
                    retval.setErr(SyntaxError.NOTHING_COMES_AFTER_PAGE_VALUE, curStartPos, idx + 1);
                    return retval;
                }
                break;
            case SORT_DIRECTION:
                if (!mPageAC.validate(nextObject)) {
                    if (!mPageAC.validateCompletion(nextObject)) {
                        retval.setErr(SyntaxError.INVALID_PAGE_FEILD, curStartPos, idx);
                        return retval;
                    }
                } else {
                    retval.addSyntaxObject(SyntaxObjectType.PAGE, nextObject, curStartPos, idx + 1);
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
        if (retval.getError() == SyntaxError.NO_ERROR) {
            IConditionFieldAutoCompleter conditionFieldAC;
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
                case CROSS_REF_OBJ:
                    IAutoCompleter crossRefAC = mSearchObjectAC.getCrossRefAutoCompleter(retval.getFirst().getBody());
                    if (crossRefAC != null) {
                        retval.addToACList(crossRefAC.getCompletion(curPartialWord));
                    }
                    break;
                case DOT:
                    retval.addToACList(mDotAC.getCompletion(curPartialWord));
                    break;
                case COLON:
                    retval.addToACList(mColonAC.getCompletion(curPartialWord));
                    break;
                case AND:
                    retval.addToACList(mAndAC.getCompletion(curPartialWord));
                    break;
                case OR:
                    retval.addToACList(mOrAC.getCompletion(curPartialWord));
                    break;
                case CONDITION_FIELD:
                    String relObj = retval.getPreviousSyntaxObject(1, SyntaxObjectType.CROSS_REF_OBJ);
                    conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(relObj);
                    if (conditionFieldAC != null) {
                        retval.addToACList(conditionFieldAC.getCompletion(curPartialWord));
                    }
                    break;
                case CONDITION_RELATION: {
                    if (curState == SyntaxObjectType.CONDITION_FIELD) {
                        relObj = retval.getPreviousSyntaxObject(2, SyntaxObjectType.CROSS_REF_OBJ);
                        String fldName = retval.getPreviousSyntaxObject(0, SyntaxObjectType.CONDITION_FIELD);
                        conditionRelationAC = mSearchObjectAC.getFieldRelationshipAutoCompleter(relObj, fldName);
                    } else // curState == SyntaxObjectType.CROSS_REF_OBJ
                    {
                        relObj = retval.getPreviousSyntaxObject(0, SyntaxObjectType.CROSS_REF_OBJ);
                        conditionRelationAC = mSearchObjectAC.getObjectRelationshipAutoCompleter(relObj);

                    }
                    if (conditionRelationAC != null) {
                        retval.addToACList(conditionRelationAC.getCompletion(curPartialWord));
                    }
                }
                    break;
                case CONDITION_VALUE: {
                    relObj = retval.getPreviousSyntaxObject(3, SyntaxObjectType.CROSS_REF_OBJ);
                    String fldName = retval.getPreviousSyntaxObject(1, SyntaxObjectType.CONDITION_FIELD);
                    conditionValueAC = mSearchObjectAC.getFieldValueAutoCompleter(relObj, fldName);
                    if (conditionValueAC != null) {
                        retval.addToACList(conditionValueAC.getCompletion(curPartialWord));
                    }
                }
                    break;
                case SORTBY:
                    retval.addToACList(mSortbyAC.getCompletion(curPartialWord));
                    break;
                case PAGE:
                    retval.addToACList(mPageAC.getCompletion(curPartialWord));
                    break;
                case SORT_FIELD:
                    conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(retval.getSearchObjectStr());
                    if (conditionFieldAC != null) {
                        retval.addToACList(conditionFieldAC.getCompletion(curPartialWord));
                    }
                    break;
                case SORT_DIRECTION:
                    retval.addToACList(mSortDirectionAC.getCompletion(curPartialWord));
                    break;
                }
            }
        }
        return retval;
    }

    public String generateQueryFromSyntaxContainer(SyntaxContainer syntax, boolean isSafe) {
        String retval = "";
        if (syntax.getvalid()) {
            retval = generateSqlFromSyntaxContainer(syntax, isSafe);
        }
        return retval;
    }

    private String generateFromStatement(SyntaxContainer syntax) {
        java.util.LinkedList<String> innerJoins = new java.util.LinkedList<String>();
        java.util.ArrayList<String> refObjList = syntax.getCrossRefObjList();
        String searchObjStr = syntax.getSearchObjectStr();
        if (refObjList.size() > 0) {
            // VB & C# TO JAVA CONVERTER NOTE: The following 'switch' operated
            // on a string member and was converted to Java 'if-else' logic:
            // switch (searchObjStr)
            // ORIGINAL LINE: case SearchObjects.TEMPLATE_OBJ_NAME:
            if (StringHelper.EqOp(searchObjStr, SearchObjects.TEMPLATE_OBJ_NAME)) {
                innerJoins.addFirst(mSearchObjectAC.getInnerJoin(SearchObjects.TEMPLATE_OBJ_NAME,
                        SearchObjects.VM_OBJ_NAME));
                if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                    refObjList.remove(SearchObjects.VM_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.VDC_USER_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.VDC_USER_OBJ_NAME));
                    refObjList.remove(SearchObjects.VDC_USER_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.VDS_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.VDS_OBJ_NAME));
                    refObjList.remove(SearchObjects.VDS_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.AUDIT_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.AUDIT_OBJ_NAME));
                    refObjList.remove(SearchObjects.AUDIT_OBJ_NAME);
                }
            }
            // ORIGINAL LINE: case SearchObjects.VDS_OBJ_NAME:
            else if (StringHelper.EqOp(searchObjStr, SearchObjects.VDS_OBJ_NAME)) {
                if ((refObjList.contains(SearchObjects.VDC_USER_OBJ_NAME))
                        || (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME))) {
                    innerJoins.addFirst(mSearchObjectAC.getInnerJoin(SearchObjects.VDS_OBJ_NAME,
                            SearchObjects.VM_OBJ_NAME));
                    if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                        refObjList.remove(SearchObjects.VM_OBJ_NAME);
                    }
                }
                if (refObjList.contains(SearchObjects.VDC_USER_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.VDC_USER_OBJ_NAME));
                    refObjList.remove(SearchObjects.VDC_USER_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.TEMPLATE_OBJ_NAME));
                    refObjList.remove(SearchObjects.TEMPLATE_OBJ_NAME);
                }
            }
            // ORIGINAL LINE: case SearchObjects.VDC_USER_OBJ_NAME:
            else if (StringHelper.EqOp(searchObjStr, SearchObjects.VDC_USER_OBJ_NAME)) {
                if ((refObjList.contains(SearchObjects.VDS_OBJ_NAME))
                        || (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME))) {
                    innerJoins.addFirst(mSearchObjectAC.getInnerJoin(SearchObjects.VDC_USER_OBJ_NAME,
                            SearchObjects.VM_OBJ_NAME));
                    if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                        refObjList.remove(SearchObjects.VM_OBJ_NAME);
                    }
                }
                if (refObjList.contains(SearchObjects.VDS_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.VDS_OBJ_NAME));
                    refObjList.remove(SearchObjects.VDS_OBJ_NAME);
                }
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.TEMPLATE_OBJ_NAME));
                    refObjList.remove(SearchObjects.TEMPLATE_OBJ_NAME);
                }
            }
            // ORIGINAL LINE: case SearchObjects.AUDIT_OBJ_NAME:
            else if (StringHelper.EqOp(searchObjStr, SearchObjects.AUDIT_OBJ_NAME)) {
                if (refObjList.contains(SearchObjects.TEMPLATE_OBJ_NAME)) {
                    innerJoins.addFirst(mSearchObjectAC.getInnerJoin(SearchObjects.AUDIT_OBJ_NAME,
                            SearchObjects.VM_OBJ_NAME));
                    innerJoins.addLast(mSearchObjectAC.getInnerJoin(SearchObjects.VM_OBJ_NAME,
                            SearchObjects.TEMPLATE_OBJ_NAME));
                    refObjList.remove(SearchObjects.TEMPLATE_OBJ_NAME);
                    if (refObjList.contains(SearchObjects.VM_OBJ_NAME)) {
                        refObjList.remove(SearchObjects.VM_OBJ_NAME);
                    }
                }

            }
        }
        for (String cro : refObjList) {
            innerJoins.addLast(mSearchObjectAC.getInnerJoin(searchObjStr, cro));
        }
        innerJoins.addFirst(mSearchObjectAC.getRelatedTableName(searchObjStr));
        StringBuilder sb = new StringBuilder();
        for (String part : innerJoins) {
            sb.append(" ");
            sb.append(part);
            sb.append(" ");
        }
        return sb.toString();

    }

    private String generateSqlFromSyntaxContainer(SyntaxContainer syntax, boolean isSafe) {
        String retval = "";
        if (syntax.getvalid()) {
            java.util.ListIterator<SyntaxObject> objIter = syntax.listIterator(0);
            IConditionFieldAutoCompleter conditionFieldAC;
            java.util.LinkedList<String> whereBuilder = new java.util.LinkedList<String>();
            String searchObjStr = syntax.getSearchObjectStr();
            String sortByPhrase = "";
            String fromStatement = "";
            String pageNumber = "";

            while (objIter.hasNext()) {
                SyntaxObject obj = objIter.next();
                switch (obj.getType()) {
                case SEARCH_OBJECT:
                    fromStatement = generateFromStatement(syntax);
                    break;
                case OR:
                case AND:
                    whereBuilder.addLast(obj.getBody());
                    break;
                case CONDITION_VALUE:
                    whereBuilder.addLast(generateConditionStatment(obj, syntax.listIterator(objIter.previousIndex()),
                            searchObjStr, syntax.getCaseSensitive(),isSafe));
                    break;
                case SORTBY:
                    break;
                case PAGE_VALUE:
                    pageNumber = obj.getBody();
                    break;
                case SORT_FIELD:
                    conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(searchObjStr);
                    sortByPhrase =
                            StringFormat.format(" ORDER BY %1$s", conditionFieldAC.getDbFieldName(obj.getBody()));
                    break;
                case SORT_DIRECTION:
                    sortByPhrase = StringFormat.format("%1$s %2$s", sortByPhrase, obj.getBody());
                    break;
                default:
                    break;
                }
            }

            // implying precedence rules
            String[] lookFor = { "AND", "OR" };
            for (int idx = 0; idx < lookFor.length; idx++) {
                boolean found = true;
                while (found) {
                    found = false;
                    java.util.ListIterator<String> iter = whereBuilder.listIterator(0);
                    while (iter.hasNext()) {
                        String queryPart = iter.next();
                        if (StringHelper.EqOp(queryPart, lookFor[idx])) {
                            iter.remove();
                            String nextPart = iter.next();
                            iter.remove();
                            String prevPart = iter.previous();
                            iter.set(StringFormat.format("( %1$s %2$s %3$s )", prevPart, queryPart, nextPart));
                            found = true;
                            break;
                        }
                    }
                }
            }
            // adding WHERE if required and All implicit AND
            StringBuilder wherePhrase = new StringBuilder();
            if (whereBuilder.size() > 0) {
                wherePhrase.append(" WHERE ");
                java.util.ListIterator<String> iter = whereBuilder.listIterator(0);
                while (iter.hasNext()) {
                    String queryPart = iter.next();
                    wherePhrase.append(queryPart);
                    if (iter.hasNext()) {
                        wherePhrase.append(" AND ");
                    }
                }
            }

            // adding the sorting part if required
            if (StringHelper.EqOp(sortByPhrase, "")) {
                sortByPhrase = StringFormat.format(" ORDER BY %1$s", mSearchObjectAC.getDefaultSort(searchObjStr));
            }
            // adding the paging phrase
            String pagePhrase = getPagePhrase(syntax, pageNumber);

            String primeryKey = mSearchObjectAC.getPrimeryKeyName(searchObjStr);
            String tableName = mSearchObjectAC.getRelatedTableName(searchObjStr);
            String tableNameWithOutTags = mSearchObjectAC.getRelatedTableNameWithOutTags(searchObjStr);

            String innerQuery =
                    StringFormat.format("SELECT %1$s.%2$s FROM %3$s %4$s", tableName, primeryKey, fromStatement,
                            wherePhrase);
            // only audit log search supports the SearchFrom which enables getting records starting from a certain
            // audit_log_id, this is done to make search queries from the client more efficient and eliminate the client
            // from registering to such queries and comparing last data with previous.

            String inQuery =
                    (primeryKey.equals("audit_log_id")
                            ?
                            StringFormat.format("SELECT * FROM %1$s WHERE ( %2$s > %3$s and %2$s IN (%4$s)",
                                    tableNameWithOutTags,
                                    primeryKey,
                                    syntax.getSearchFrom(),
                                    innerQuery)
                            :
                            StringFormat.format("SELECT * FROM %1$s WHERE ( %2$s IN (%3$s)", tableNameWithOutTags,
                                    primeryKey, innerQuery));
            retval =
                    StringFormat.format(Config.<String> GetValue(ConfigValues.DBSearchTemplate), sortByPhrase, inQuery,
                            pagePhrase);
            // Check for sql injection if query is not safe
            if (! isSafe) {
                if (sqlInjectionChecker.hasSqlInjection(retval)) {
                    throw new SqlInjectionException();
                }
            }
            log.traceFormat("Search: {0}", retval);
        }
        return retval;
    }

    private String getPagePhrase(SyntaxContainer syntax, String pageNumber) {
        String result = "";
        Integer page = new Integer(0);
        RefObject<Integer> tempRefObject = new RefObject<Integer>(page);
        boolean tempVar = !IntegerCompat.TryParse(pageNumber, tempRefObject);
        page = tempRefObject.argvalue;
        if (tempVar) {
            page = 1;
        }
        String pagingTypeStr = Config.<String> GetValue(ConfigValues.DBPagingType);
        if (EnumCompat.IsDefined(PagingType.class, pagingTypeStr)) {
            PagingType pagingType = EnumUtils.valueOf(PagingType.class, pagingTypeStr, true);
            String pagingSyntax = Config.<String> GetValue(ConfigValues.DBPagingSyntax);
            switch (pagingType) {
            case Range:
                result = StringFormat
                        .format(pagingSyntax, (page - 1) * syntax.getMaxCount() + 1, page * syntax.getMaxCount());
                break;
            case Offset:
                result = StringFormat.format(pagingSyntax, (page - 1) * syntax.getMaxCount() + 1, syntax.getMaxCount());
                break;
            }
        } else {
            log.error(StringFormat.format("Unknown paging type %1$s", pagingTypeStr));
        }

        return result;

    }

    private enum ConditionType {
        None,
        FreeText,
        FreeTextSpecificObj,
        ConditionWithDefaultObj,
        ConditionwithSpesificObj;

        public int getValue() {
            return this.ordinal();
        }

        public static ConditionType forValue(int value) {
            return values()[value];
        }
    }

    private String generateConditionStatment(SyntaxObject obj, java.util.ListIterator<SyntaxObject> objIter,
            String searchObjStr, boolean caseSensitive, boolean issafe) {
        IConditionFieldAutoCompleter conditionFieldAC;
        IConditionValueAutoCompleter conditionValueAC = null;
        // check for sql injection
        String originalValue = obj.getBody();
        String customizedValue = originalValue;
        if (!issafe) {
            // Enforce escape characters before special characters
            customizedValue = SqlInjectionChecker.enforceEscapeCharacters(originalValue);
        }
        String customizedRelation;
        String fieldName = "";
        String objName;
        ConditionType conditionType;
        SyntaxObject prev = objIter.previous();
        if (prev.getType() != SyntaxObjectType.CONDITION_RELATION) {
            // free text of default search object
            customizedRelation = "=";
            objName = searchObjStr;
            conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(searchObjStr);
            conditionType = ConditionType.FreeText;
        } else {
            customizedRelation = prev.getBody();
            prev = objIter.previous();
            if (prev.getType() == SyntaxObjectType.CROSS_REF_OBJ) { // free text
                                                                    // search
                                                                    // for some
                                                                    // object
                objName = prev.getBody();
                conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(objName);
                conditionType = ConditionType.FreeTextSpecificObj;
            } else // if (prev.getType() == SyntaxObjectType.CONDITION_FIELD)
            {
                fieldName = prev.getBody();
                prev = objIter.previous();
                if (prev.getType() != SyntaxObjectType.DOT) {
                    // standard condition with default AC (search obj)
                    objName = searchObjStr;
                    conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(searchObjStr);
                    conditionType = ConditionType.ConditionWithDefaultObj;
                } else {
                    // standard condition with specific AC
                    prev = objIter.previous();
                    objName = prev.getBody();
                    conditionFieldAC = mSearchObjectAC.getFieldAutoCompleter(objName);
                    conditionType = ConditionType.ConditionwithSpesificObj;
                }
            }
            conditionValueAC = conditionFieldAC.getFieldValueAutoCompleter(fieldName);
        }

        BaseConditionFieldAutoCompleter conditionAsBase =
                (BaseConditionFieldAutoCompleter) ((conditionFieldAC instanceof BaseConditionFieldAutoCompleter) ? conditionFieldAC
                        : null);
        java.lang.Class curType = null;
        if (conditionAsBase != null && (curType = conditionAsBase.getTypeDictionary().get(fieldName)) != null
                && curType == String.class && !StringHelper.isNullOrEmpty(customizedValue)
                && !StringHelper.EqOp(customizedValue, "''") && !StringHelper.EqOp(customizedValue, "'*'")) {
            customizedValue =
                    StringFormat.format(BaseConditionFieldAutoCompleter.getI18NPrefix() + "%1$s", customizedValue);
        }

        if (conditionValueAC != null) {
            customizedValue = StringFormat.format("'%1$s'",
                    conditionValueAC.convertFieldEnumValueToActualValue(obj.getBody()));
        } else if (fieldName.equals("") /* search on all relevant fields */ ||
                  (conditionFieldAC.getDbFieldType(fieldName).equals(String.class))) {
            customizedValue = customizedValue.replace('*', '%');
            /* enable case-insensitive search by changing operation to I/LIKE*/
            if (StringHelper.EqOp(customizedRelation, "=")) {
                customizedRelation = BaseConditionFieldAutoCompleter.getLikeSyntax(caseSensitive);
            } else if (StringHelper.EqOp(customizedRelation, "!=")) {
                customizedRelation = "NOT " + BaseConditionFieldAutoCompleter.getLikeSyntax(caseSensitive);
            }
        }
        String condition = "";
        String tableName = mSearchObjectAC.getRelatedTableName(objName);
        switch (conditionType) {
        case FreeText:
        case FreeTextSpecificObj:
            condition = conditionFieldAC.buildFreeTextConditionSql(tableName, customizedRelation, customizedValue, caseSensitive);
            break;
        case ConditionWithDefaultObj:
        case ConditionwithSpesificObj:
            condition = conditionFieldAC.buildConditionSql(fieldName, customizedValue, customizedRelation, tableName, caseSensitive);
            break;
        }
        return condition;
    }
    private static LogCompat log = LogFactoryCompat.getLog(SyntaxChecker.class);
}
