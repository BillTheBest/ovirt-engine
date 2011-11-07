package org.ovirt.engine.core.common.interfaces;

import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//[ServiceContract]
public interface ITagsHandler {
    // [OperationContract(IsInitiating = false)]
    // [FaultContract(typeof(VdcFault))]
    // void MoveTag(int tagId, int newParent);
    tags GetTagByTagName(String tagName);

    String GetTagIdAndChildrenIds(Guid tagId);

    String GetTagNameAndChildrenNames(Guid tagId);

    String GetTagIdsAndChildrenIdsByRegExp(String tagNameRegExp);

    String GetTagNamesAndChildrenNamesByRegExp(String tagNameRegExp);
}
