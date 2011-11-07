package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
public class ImagesComparerByName implements java.util.Comparator<DiskImage>, Serializable {
    private static final long serialVersionUID = -6854346772820338515L;

    @Override
    public int compare(DiskImage x, DiskImage y) {
        return Compare(x, y);
    }

    public int Compare(DiskImage x, DiskImage y) {
        return x.getinternal_drive_mapping().compareTo(y.getinternal_drive_mapping());
    }

    public ImagesComparerByName() {
    }
}
