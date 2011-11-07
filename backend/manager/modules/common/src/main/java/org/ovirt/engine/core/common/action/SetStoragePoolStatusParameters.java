package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.errors.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetStoragePoolStatusParameters")
public class SetStoragePoolStatusParameters extends StoragePoolParametersBase {
    private static final long serialVersionUID = 264321499194008199L;
    @XmlElement(name = "Status")
    private StoragePoolStatus privateStatus = StoragePoolStatus.forValue(0);

    public StoragePoolStatus getStatus() {
        return privateStatus;
    }

    public void setStatus(StoragePoolStatus value) {
        privateStatus = value;
    }

    @XmlElement(name = "AuditLogType")
    private AuditLogType privateAuditLogType = AuditLogType.forValue(0);

    public AuditLogType getAuditLogType() {
        return privateAuditLogType;
    }

    public void setAuditLogType(AuditLogType value) {
        privateAuditLogType = value;
    }

    @XmlElement(name = "Error")
    private VdcBllErrors privateError = VdcBllErrors.forValue(0);

    public VdcBllErrors getError() {
        return privateError;
    }

    public void setError(VdcBllErrors value) {
        privateError = value;
    }

    public SetStoragePoolStatusParameters(Guid storagePoolId, StoragePoolStatus status, AuditLogType auditLogType) {
        super(storagePoolId);
        setStatus(status);
        setAuditLogType(auditLogType);
    }

    public SetStoragePoolStatusParameters() {
    }
}
