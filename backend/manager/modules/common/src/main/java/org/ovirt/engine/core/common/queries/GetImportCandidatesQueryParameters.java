package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetImportCandidatesQueryParameters")
public class GetImportCandidatesQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 6808376571843897755L;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Path")
    private String privatePath;

    public String getPath() {
        return privatePath;
    }

    public void setPath(String value) {
        privatePath = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "CandidateSource")
    private ImportCandidateSourceEnum privateCandidateSource = ImportCandidateSourceEnum.forValue(0);

    public ImportCandidateSourceEnum getCandidateSource() {
        return privateCandidateSource;
    }

    public void setCandidateSource(ImportCandidateSourceEnum value) {
        privateCandidateSource = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "CandidateType")
    private ImportCandidateTypeEnum privateCandidateType = ImportCandidateTypeEnum.forValue(0);

    public ImportCandidateTypeEnum getCandidateType() {
        return privateCandidateType;
    }

    public void setCandidateType(ImportCandidateTypeEnum value) {
        privateCandidateType = value;
    }

    public GetImportCandidatesQueryParameters(String path, ImportCandidateSourceEnum candidateSource,
            ImportCandidateTypeEnum candidateType) {
        setPath(path);
        setCandidateSource(candidateSource);
        setCandidateType(candidateType);
    }

    @Override
    public RegisterableQueryReturnDataType GetReturnedDataTypeByVdcQueryType(VdcQueryType queryType) {
        return RegisterableQueryReturnDataType.UNDEFINED;
    }

    public GetImportCandidatesQueryParameters() {
    }
}
