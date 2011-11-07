package org.ovirt.engine.core.searchbackend;

import org.ovirt.engine.core.compat.*;

public class SyntaxContainer implements Iterable<SyntaxObject> {
    private String mOrigText = null;
    private boolean mValid = false;
    private SyntaxError mError = SyntaxError.NO_ERROR;
    private int[] mErrorPos = new int[2];
    private java.util.LinkedList<SyntaxObject> mObjList = null;
    private java.util.ArrayList<String> mCurrentCompletions = null;
    private int privateMaxCount;
    private long searchFrom = 0;
    private boolean caseSensitive=true;

    public int getMaxCount() {
        return privateMaxCount;
    }

    public void setMaxCount(int value) {
        privateMaxCount = value;
    }

    public long getSearchFrom() {
        return searchFrom;
    }

    public void setSearchFrom(long value) {
        searchFrom = value;
    }

    public boolean getvalid() {
        return mValid;
    }

    public void setvalid(boolean value) {
        mValid = value;
    }

    public SyntaxError getError() {
        return mError;
    }

    public boolean getCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean value) {
        caseSensitive = value;
    }
    public int getErrorStartPos() {
        return mErrorPos[0];
    }

    public int getErrorEndPos() {
        return mErrorPos[1];
    }

    public SyntaxObject getFirst() {
        return mObjList.getFirst();
    }

    public String getSearchObjectStr() {
        String retval = null;
        if (mObjList.getFirst() != null) {
            retval = getObjSingularName(mObjList.getFirst().getBody());
        }
        return retval;
    }

    public SyntaxContainer(String origText) {
        mOrigText = origText;
        mValid = false;
        mObjList = new java.util.LinkedList<SyntaxObject>();
        mCurrentCompletions = new java.util.ArrayList<String>();

    }

    public void setErr(SyntaxError errCode, int startPos, int endPos) {
        mErrorPos[0] = startPos;
        mErrorPos[1] = endPos;
        mError = errCode;
        mValid = false;
    }

    public void addSyntaxObject(SyntaxObjectType type, String body, int startPos, int endPos) {
        SyntaxObject newObj = new SyntaxObject(type, body, startPos, endPos);
        mObjList.addLast(newObj);
    }

    public SyntaxObjectType getState() {
        SyntaxObjectType retval = SyntaxObjectType.BEGIN;
        if (mObjList.size() > 0) {
            retval = mObjList.getLast().getType();
        }
        return retval;
    }

    public int getLastHandledIndex() {
        int retval = 0;
        if (mObjList.size() > 0) {
            retval = mObjList.getLast().getPos()[1];
        }
        return retval;
    }

    public String getPreviousSyntaxObject(int steps, SyntaxObjectType type) {
        String retval = "";
        if (mObjList.size() > steps) {
            SyntaxObject obj = mObjList.get(mObjList.size() - 1 - steps);
            if (obj.getType() == type) {
                retval = obj.getBody();
            }
        }
        if ((StringHelper.EqOp(retval, ""))
                && ((type == SyntaxObjectType.CROSS_REF_OBJ) || (type == SyntaxObjectType.SEARCH_OBJECT))) {
            retval = mObjList.getFirst().getBody();
        }
        return retval;
    }

    public SyntaxObjectType getPreviousSyntaxObjectType(int steps) {
        SyntaxObjectType retval = SyntaxObjectType.END;
        if (mObjList.size() > steps) {
            SyntaxObject obj = mObjList.get(mObjList.size() - 1 - steps);
            retval = obj.getType();
        }
        return retval;
    }

    public void addToACList(String[] acArr) {
        for (int idx = 0; idx < acArr.length; idx++) {
            mCurrentCompletions.add(acArr[idx]);
        }
    }

    public String[] getCompletionArray() {
        String[] retval = new String[mCurrentCompletions.size()];
        for (int idx = 0; idx < mCurrentCompletions.size(); idx++) {
            retval[idx] = mCurrentCompletions.get(idx);
        }
        return retval;
    }

    public java.util.ArrayList<String> getCrossRefObjList() {
        java.util.ArrayList<String> retval = new java.util.ArrayList<String>();
        String searchObj = getObjSingularName(getSearchObjectStr());
        for (SyntaxObject obj : mObjList) {
            if (obj.getType() == SyntaxObjectType.CROSS_REF_OBJ) {
                String objSingularName = getObjSingularName(obj.getBody());
                if ((!retval.contains(objSingularName)) && (!StringHelper.EqOp(searchObj, objSingularName))) {
                    retval.add(objSingularName);
                }
            }
        }
        return retval;
    }

    public String getObjSingularName(String obj) {
        String retval = obj;

        if (obj == null) {
            return null;
        }
        // VB & C# TO JAVA CONVERTER NOTE: The following 'switch' operated on a
        // string member and was converted to Java 'if-else' logic:
        // switch (obj)
        // ORIGINAL LINE: case SearchObjects.AD_USER_OBJ_NAME:
        if (StringHelper.EqOp(obj, SearchObjects.AD_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AD_USER_PLU_OBJ_NAME)) {
            retval = SearchObjects.AD_USER_OBJ_NAME;
        }
        // ORIGINAL LINE: case SearchObjects.AUDIT_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.AUDIT_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.AUDIT_PLU_OBJ_NAME)) {
            retval = SearchObjects.AUDIT_OBJ_NAME;
        }
        // ORIGINAL LINE: case SearchObjects.TEMPLATE_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.TEMPLATE_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.TEMPLATE_PLU_OBJ_NAME)) {
            retval = SearchObjects.TEMPLATE_OBJ_NAME;
        }
        // ORIGINAL LINE: case SearchObjects.VDC_USER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_USER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_USER_PLU_OBJ_NAME)) {
            retval = SearchObjects.VDC_USER_OBJ_NAME;
        }
        // ORIGINAL LINE: case SearchObjects.VDS_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDS_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDS_PLU_OBJ_NAME)) {
            retval = SearchObjects.VDS_OBJ_NAME;
        }
        // ORIGINAL LINE: case SearchObjects.VM_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VM_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VM_PLU_OBJ_NAME)) {
            retval = SearchObjects.VM_OBJ_NAME;
        }
        // ORIGINAL LINE: case SearchObjects.VDC_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_POOL_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_POOL_PLU_OBJ_NAME)) {
            retval = SearchObjects.VDC_POOL_OBJ_NAME;
        }
        // ORIGINAL LINE: case SearchObjects.VDC_CLUSTER_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_CLUSTER_PLU_OBJ_NAME)) {
            retval = SearchObjects.VDC_CLUSTER_OBJ_NAME;
        }
        // ORIGINAL LINE: case SearchObjects.VDC_STORAGE_POOL_OBJ_NAME:
        else if (StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_POOL_OBJ_NAME)
                || StringHelper.EqOp(obj, SearchObjects.VDC_STORAGE_DOMAIN_OBJ_NAME)) {
            retval = obj;
        } else {
            retval = obj;

        }
        return retval;
    }

    @Override
    public String toString() {
        StringBuilderCompat sb = new StringBuilderCompat("---------------- SyntaxContainer ---------------------");
        sb.append("\n");
        sb.append("mOrigText       = ");
        sb.AppendLine(mOrigText);
        sb.append("Valid           = ");
        sb.AppendLine((new Boolean(mValid)).toString());
        sb.append("Error           = ");
        sb.AppendLine(mError.toString());
        sb.append("CrossRefObjlist = ");
        for (String cro : getCrossRefObjList()) {
            sb.append(StringFormat.format("%1$s, ", cro));
        }
        sb.append("Syntax object list:");

        for (SyntaxObject obj : mObjList) {
            sb.AppendLine("    ");
            sb.append(obj.toString());
        }
        return sb.toString();
    }

    public String ToStringBr() {
        StringBuilderCompat sb = new StringBuilderCompat("---------------- SyntaxContainer ---------------------");
        sb.append("<BR>mOrigText       = ");
        sb.append(mOrigText);
        sb.append("<BR>Valid           = ");
        sb.append(mValid);
        sb.append("<BR>Error           = ");
        sb.append(mError);
        sb.append("<BR>Syntax object list:");
        sb.append("<BR>CrossRefObjlist = ");
        for (String cro : getCrossRefObjList()) {
            sb.append(StringFormat.format("%1$s, ", cro));
        }
        for (SyntaxObject obj : mObjList) {
            sb.append("<BR>    ");
            sb.append(obj.toString());
        }
        return sb.toString();
    }

    public boolean contains(SyntaxObjectType type, String val) {
        boolean retval = false;
        for (SyntaxObject obj : mObjList) {
            if ((obj.getType() == type) && (StringHelper.EqOp(obj.getBody().toUpperCase(), val.toUpperCase()))) {
                retval = true;
                break;
            }
        }
        return retval;
    }

    public java.util.ListIterator<SyntaxObject> listIterator(int index) {
        return mObjList.listIterator(index);
    }

    public java.util.Iterator<SyntaxObject> iterator() {
        return mObjList.iterator();
    }
}
