package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;

public class AuditLogableBaseTest {

    private static final Guid GUID = new Guid("11111111-1111-1111-1111-111111111111");
    private static final Guid GUID2 = new Guid("11111111-1111-1111-1111-111111111112");
    private static final Guid GUID3 = new Guid("11111111-1111-1111-1111-111111111113");
    private static final String NAME = "testName";

    @Test
    public void nGuidCtor() {
        final AuditLogableBase b = new AuditLogableBase(GUID);
        final Guid v = b.getVdsId();
        assertEquals(GUID, v);
    }

    @Test
    public void nGuidCtorNull() {
        final NGuid n = null;
        final AuditLogableBase b = new AuditLogableBase(n);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void nGuidGuidCtor() {
        final AuditLogableBase b = new AuditLogableBase(GUID, GUID2);
        final Guid g = b.getVdsId();
        assertEquals(GUID, g);
        final Guid gu = b.getVmId();
        assertEquals(GUID2, gu);
    }

    @Test
    public void nGuidGuidCtorNullNGuid() {
        final AuditLogableBase b = new AuditLogableBase(null, GUID2);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
        final Guid gu = b.getVmId();
        assertEquals(GUID2, gu);
    }

    @Test
    public void nGuidGuidCtorNullGuid() {
        final AuditLogableBase b = new AuditLogableBase(GUID, null);
        final Guid g = b.getVdsId();
        assertEquals(GUID, g);
        final Guid gu = b.getVmId();
        assertEquals(Guid.Empty, gu);
    }

    @Test
    public void nGuidGuidCtorNull() {
        final AuditLogableBase b = new AuditLogableBase(null, null);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
        final Guid gu = b.getVmId();
        assertEquals(Guid.Empty, gu);
    }

    @Test
    public void getUserIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final NGuid g = b.getUserId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void getUserIdIdSet() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setUserId(GUID);
        final NGuid g = b.getUserId();
        assertEquals(GUID, g);
    }

    @Test
    public void getUserIdVdcUserDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final VdcUser u = new VdcUser();
        b.setCurrentUser(u);
        final NGuid g = b.getUserId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void getUserIdVdcUserId() {
        final AuditLogableBase b = new AuditLogableBase();
        final VdcUser u = new VdcUser();
        u.setUserId(GUID);
        b.setCurrentUser(u);
        final NGuid g = b.getUserId();
        assertEquals(GUID, g);
    }

    @Test
    public void getUserNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String n = b.getUserName();
        assertNull(n);
    }

    @Test
    public void getUserNameNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setUserName(null);
        final String n = b.getUserName();
        assertNull(n);
    }

    @Test
    public void getUserName() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setUserName(NAME);
        final String n = b.getUserName();
        assertEquals(NAME, n);
    }

    @Test
    public void GetUserNameFromUser() {
        final AuditLogableBase b = new AuditLogableBase();
        final VdcUser u = new VdcUser();
        u.setUserName(NAME);
        b.setCurrentUser(u);
        final String un = b.getUserName();
        assertEquals(NAME, un);
    }

    @Test
    public void currentUserDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final IVdcUser u = b.getCurrentUser();
        assertNull(u);
    }

    @Test
    public void currentUserNull() {
        final AuditLogableBase b = new AuditLogableBase();
        final IVdcUser u = null;
        b.setCurrentUser(u);
        final IVdcUser cu = b.getCurrentUser();
        assertEquals(u, cu);
    }

    @Test
    public void currentUser() {
        final AuditLogableBase b = new AuditLogableBase();
        final IVdcUser u = new VdcUser();
        b.setCurrentUser(u);
        final IVdcUser cu = b.getCurrentUser();
        assertEquals(u, cu);
    }

    @Test
    public void vmTemplateIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getVmTemplateId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vmTemplateId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmTemplateId(GUID);
        final Guid g = b.getVmTemplateId();
        assertEquals(GUID, g);
    }

    @Test
    public void vmTemplateIdRefDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final NGuid g = b.getVmTemplateIdRef();
        assertNull(g);
    }

    @Test
    public void vmTemplateIdRef() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmTemplateId(GUID);
        final NGuid g = b.getVmTemplateIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vmTemplateIdRefWithVm() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM v = new VM();
        b.setVm(v);
        final NGuid g = b.getVmTemplateIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vmTemplateNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String n = b.getVmTemplateName();
        assertNull(n);
    }

    @Test
    public void vmTemplateName() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmTemplateName(NAME);
        final String nm = b.getVmTemplateName();
        assertEquals(NAME, nm);
    }

    @Test
    public void vmTemplateNameWithVm() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM v = new VM();
        b.setVm(v);
        final String n = b.getVmTemplateName();
        assertEquals(NAME, n);
    }

    @Test
    public void vmIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid i = b.getVmId();
        assertEquals(Guid.Empty, i);
    }

    @Test
    public void vmIdNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmId(null);
        final Guid i = b.getVmId();
        assertEquals(Guid.Empty, i);
    }

    @Test
    public void vmId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmId(GUID);
        final Guid i = b.getVmId();
        assertEquals(GUID, i);
    }

    @Test
    public void snapshotNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String s = b.getSnapshotName();
        assertNull(s);
    }

    @Test
    public void snapshotNameNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setSnapshotName(null);
        final String s = b.getSnapshotName();
        assertNull(s);
    }

    @Test
    public void snapshotNameEmpty() {
        final AuditLogableBase b = new AuditLogableBase();
        final String e = "";
        b.setSnapshotName(e);
        final String s = b.getSnapshotName();
        assertEquals(e, s);
    }

    @Test
    public void snapshotName() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setSnapshotName(NAME);
        final String s = b.getSnapshotName();
        assertEquals(NAME, s);
    }

    @Test
    public void vmIdRefDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final NGuid g = b.getVmIdRef();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vmIdRefNullVmId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmId(null);
        final NGuid g = b.getVmIdRef();
        assertNull(g);
    }

    @Test
    public void vmIdRefNullVm() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmId(null);
        final VM v = new VM();
        v.setvm_guid(GUID);
        b.setVm(v);
        final NGuid g = b.getVmIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vmNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String n = b.getVmName();
        assertNull(n);
    }

    @Test
    public void vmNameNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmName(null);
        final String n = b.getVmName();
        assertNull(n);
    }

    @Test
    public void vmNameNullVm() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmName(null);
        final VM v = new VM();
        v.setvm_name(NAME);
        b.setVm(v);
        final String n = b.getVmName();
        assertEquals(NAME, n);
    }

    @Test
    public void vmName() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmName(NAME);
        final String n = b.getVmName();
        assertEquals(NAME, n);
    }

    @Test
    public void vdsIdRefDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final NGuid g = b.getVdsIdRef();
        assertNull(g);
    }

    @Test
    public void vdsIdRefNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsIdRef(null);
        final NGuid g = b.getVdsIdRef();
        assertNull(g);
    }

    @Test
    public void vdsIdRef() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsIdRef(GUID);
        final NGuid g = b.getVdsIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void VdsIdRefVds() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsIdRef(null);
        final VDS v = new VDS();
        v.setvds_id(GUID);
        b.setVds(v);
        final NGuid g = b.getVdsIdRef();
        assertEquals(GUID, g);
    }

    @Test
    public void vdsIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vdsIdNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsId(null);
        final Guid g = b.getVdsId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void vdsId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsId(GUID);
        final Guid g = b.getVdsId();
        assertEquals(GUID, g);
    }

    @Test
    public void vdsNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String s = b.getVdsName();
        assertNull(s);
    }

    @Test
    public void vdsNameNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsName(null);
        final String s = b.getVdsName();
        assertNull(s);
    }

    @Test
    public void vdsName() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsName(NAME);
        final String s = b.getVdsName();
        assertEquals(NAME, s);
    }

    @Test
    public void VdsNameVds() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsName(null);
        final VDS v = new VDS();
        v.setvds_name(NAME);
        b.setVds(v);
        final String s = b.getVdsName();
        assertEquals(NAME, s);
    }

    @Test
    public void storageDomainDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final storage_domains s = b.getStorageDomain();
        assertNull(s);
    }

    @Test
    public void storageDomainNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStorageDomain(null);
        final storage_domains s = b.getStorageDomain();
        assertNull(s);
    }

    @Test
    public void storageDomain() {
        final AuditLogableBase b = new AuditLogableBase();
        final storage_domains s = new storage_domains();
        b.setStorageDomain(s);
        final storage_domains st = b.getStorageDomain();
        assertEquals(s, st);
    }

    @Test
    public void storageDomainWithId() {
        final TestAuditLogableBase b = new TestAuditLogableBase();
        b.setStorageDomainId(GUID);
        b.setStoragePoolId(GUID);
        final storage_domains s = b.getStorageDomain();
        assertEquals(b.STORAGE_DOMAIN, s);
    }

    @Test
    public void storageDomainWithIdNullPool() {
        final TestAuditLogableBase b = new TestAuditLogableBase();
        b.setStorageDomainId(GUID);
        b.setStoragePoolId(GUID2);
        final storage_domains s = b.getStorageDomain();
        assertNull(s);
    }

    @Test
    public void storageDomainWithNullId() {
        final TestAuditLogableBase b = new TestAuditLogableBase();
        b.setStorageDomainId(GUID2);
        final storage_domains s = b.getStorageDomain();
        assertEquals(b.STORAGE_DOMAIN, s);
    }

    @Test
    public void storageDomainIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final NGuid g = b.getStorageDomainId();
        assertNull(g);
    }

    @Test
    public void storageDomainIdNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStorageDomainId(null);
        final NGuid g = b.getStorageDomainId();
        assertNull(g);
    }

    @Test
    public void storageDomainId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStorageDomainId(GUID);
        final NGuid g = b.getStorageDomainId();
        assertEquals(GUID, g);
    }

    @Test
    public void storageDomainIdWithStorageDomain() {
        final AuditLogableBase b = new AuditLogableBase();
        final storage_domains s = new storage_domains();
        s.setid(GUID);
        b.setStorageDomain(s);
        final NGuid g = b.getStorageDomainId();
        assertEquals(GUID, g);
    }

    @Test
    public void storageDomainNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String s = b.getStorageDomainName();
        assertEquals("", s);
    }

    @Test
    public void storageDomainName() {
        final AuditLogableBase b = new AuditLogableBase();
        final storage_domains s = new storage_domains();
        s.setstorage_name(NAME);
        b.setStorageDomain(s);
        final String n = b.getStorageDomainName();
        assertEquals(NAME, n);
    }

    @Test
    public void storagePoolDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final storage_pool p = b.getStoragePool();
        assertNull(p);
    }

    @Test
    public void storagePoolWithId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        b.setStoragePoolId(GUID);
        final storage_pool p = b.getStoragePool();
        assertNotNull(p);
    }

    @Test
    public void storagePool() {
        final AuditLogableBase b = new AuditLogableBase();
        final storage_pool p = new storage_pool();
        b.setStoragePool(p);
        final storage_pool sp = b.getStoragePool();
        assertEquals(p, sp);
    }

    @Test
    public void storagePoolIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final NGuid n = b.getStoragePoolId();
        assertNull(n);
    }

    @Test
    public void storagePoolIdNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStoragePoolId(null);
        final NGuid n = b.getStoragePoolId();
        assertNull(n);
    }

    @Test
    public void storagePoolId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStoragePoolId(GUID);
        final NGuid n = b.getStoragePoolId();
        assertEquals(GUID, n);
    }

    @Test
    public void storagePoolIdWithStoragePool() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStoragePoolId(null);
        final storage_pool p = new storage_pool();
        p.setId(GUID);
        b.setStoragePool(p);
        final NGuid n = b.getStoragePoolId();
        assertEquals(GUID, n);
    }

    @Test
    public void storagePoolIdWithStorageDomain() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setStoragePoolId(null);
        b.setStoragePool(null);
        final storage_domains s = new storage_domains();
        s.setstorage_pool_id(GUID);
        b.setStorageDomain(s);
        final NGuid n = b.getStoragePoolId();
        assertEquals(GUID, n);
    }

    @Test
    public void storagePoolNameDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final String s = b.getStoragePoolName();
        assertEquals("", s);
    }

    @Test
    public void storagePoolName() {
        final AuditLogableBase b = new AuditLogableBase();
        final storage_pool p = new storage_pool();
        p.setname(NAME);
        b.setStoragePool(p);
        final String s = b.getStoragePoolName();
        assertEquals(NAME, s);
    }

    @Test
    public void auditLogTypeValue() {
        final AuditLogableBase b = new AuditLogableBase();
        final AuditLogType t = b.getAuditLogTypeValue();
        assertEquals(AuditLogType.UNASSIGNED, t);
    }

    @Test
    public void getVdsDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVdsNullAll() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDS vds = null;
        final VM vm = null;
        final Guid vdsId = null;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVdsNullVdsId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDS vds = null;
        final VM vm = new VM();
        vm.setrun_on_vds(GUID3);
        final Guid vdsId = null;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVdsNullRun() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDS vds = null;
        final VM vm = new VM();
        vm.setrun_on_vds(null);
        final Guid vdsId = null;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVdsWithVds() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDS vds = new VDS();
        final VM vm = null;
        final Guid vdsId = null;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertEquals(vds, v);
    }

    @Test
    public void getVdsWithVdsId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDS vds = null;
        final VM vm = new VM();
        vm.setrun_on_vds(GUID2);
        final Guid vdsId = GUID;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertEquals(GUID, v.getvds_id());
    }

    @Test
    public void getVdsWithVm() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDS vds = null;
        final VM vm = new VM();
        vm.setrun_on_vds(GUID2);
        final Guid vdsId = null;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertEquals(GUID2, v.getvds_id());
    }

    @Test
    public void getVdsSwallowsException() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDS vds = null;
        final VM vm = new VM();
        vm.setrun_on_vds(GUID2);
        final Guid vdsId = GUID3;
        b.setVds(vds);
        b.setVdsId(vdsId);
        b.setVm(vm);
        final VDS v = b.getVds();
        assertNull(v);
    }

    @Test
    public void getVmDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final VM v = b.getVm();
        assertNull(v);
    }

    @Test
    public void getVm() {
        final AuditLogableBase b = new AuditLogableBase();
        final VM v = new VM();
        b.setVm(v);
        final VM vm = b.getVm();
        assertEquals(v, vm);
    }

    @Test
    public void getVmNullId() {
        final AuditLogableBase b = new AuditLogableBase();
        final VM v = null;
        b.setVm(v);
        b.setVmId(null);
        final VM vm = b.getVm();
        assertNull(vm);
    }

    @Test
    public void getVmEmptyId() {
        final AuditLogableBase b = new AuditLogableBase();
        final VM v = null;
        b.setVm(v);
        b.setVmId(Guid.Empty);
        final VM vm = b.getVm();
        assertNull(vm);
    }

    @Test
    public void getVmFromId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM v = null;
        b.setVm(v);
        b.setVmId(GUID);
        final VM vm = b.getVm();
        assertNotNull(vm);
    }

    @Test
    public void getVmSwallowsExceptions() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM v = null;
        b.setVm(v);
        b.setVmId(GUID3);
        final VM vm = b.getVm();
        assertNull(vm);
    }

    @Test
    public void getVmTemplateDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final VmTemplate t = b.getVmTemplate();
        assertNull(t);
    }

    @Test
    public void getVmTemplateNull() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVmTemplate(null);
        final VmTemplate t = b.getVmTemplate();
        assertNull(t);
    }

    @Test
    public void getVmTemplateWithId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        b.setVmTemplate(null);
        b.setVmTemplateId(GUID);
        final VmTemplate t = b.getVmTemplate();
        assertNotNull(t);
    }

    @Test
    public void getVmTemplateWithVm() {
        final AuditLogableBase b = new TestAuditLogableBase();
        b.setVmTemplate(null);
        b.setVmTemplateId(null);
        final VM vm = new VM();
        vm.setvmt_guid(GUID);
        b.setVm(vm);
        final VmTemplate t = b.getVmTemplate();
        assertNotNull(t);
    }

    @Test
    public void getVdsGroupIdDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final Guid g = b.getVdsGroupId();
        assertEquals(Guid.Empty, g);
    }

    @Test
    public void getVdsGroupId() {
        final AuditLogableBase b = new AuditLogableBase();
        b.setVdsGroupId(GUID);
        final Guid g = b.getVdsGroupId();
        assertEquals(GUID, g);
    }

    @Test
    public void getVdsGroupIdVdsGroup() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDSGroup gr = new VDSGroup();
        gr.setID(GUID);
        b.setVdsGroup(gr);
        final Guid g = b.getVdsGroupId();
        assertEquals(GUID, g);
    }

    @Test
    public void getVdsGroupDefault() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDSGroup g = b.getVdsGroup();
        assertNull(g);
    }

    @Test
    public void getVdsGroupNotNull() {
        final AuditLogableBase b = new AuditLogableBase();
        final VDSGroup g = new VDSGroup();
        b.setVdsGroup(g);
        final VDSGroup gr = b.getVdsGroup();
        assertEquals(g, gr);
    }

    @Test
    public void getVdsGroupWithId() {
        final AuditLogableBase b = new TestAuditLogableBase();
        b.setVdsGroupId(GUID);
        final VDSGroup g = b.getVdsGroup();
        assertEquals(GUID, g.getID());
    }

    @Test
    public void getVdsGroupWithVds() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDS v = new VDS();
        v.setvds_group_id(GUID);
        b.setVds(v);
        final VDSGroup g = b.getVdsGroup();
        assertEquals(GUID, g.getID());
    }

    @Test
    public void getVdsGroupWithVm() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VM v = new VM();
        v.setvds_group_id(GUID);
        b.setVm(v);
        final VDSGroup g = b.getVdsGroup();
        assertEquals(GUID, g.getID());
    }

    @Test
    public void getVdsGroupNameDefault() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String n = b.getVdsGroupName();
        assertEquals("", n);
    }

    @Test
    public void getVdsGroupNameNullVds() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDSGroup g = null;
        b.setVdsGroup(g);
        final String n = b.getVdsGroupName();
        assertEquals("", n);
    }

    @Test
    public void getVdsGroupName() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final VDSGroup g = new VDSGroup();
        g.setname(NAME);
        b.setVdsGroup(g);
        final String n = b.getVdsGroupName();
        assertEquals(NAME, n);
    }

    @Test(expected = NullPointerException.class)
    public void addCustomValueDoesNotHandleNullKeys() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = null;
        final String value = NAME;
        b.AddCustomValue(key, value);
        final String v = b.GetCustomValue(key);
        assertEquals(value, v);
    }

    @Test
    public void addCustomValueWillNotReturnANull() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = NAME;
        final String value = null;
        b.AddCustomValue(key, value);
        final String v = b.GetCustomValue(key);
        assertEquals("", v);
    }

    @Test
    public void customValue() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = NAME;
        b.AddCustomValue(key, value);
        final String v = b.GetCustomValue(key);
        assertEquals(value, v);
    }

    @Test
    public void getCustomValuesLeaksInternalStructure() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = NAME;
        b.AddCustomValue(key, value);
        final String v = b.GetCustomValue(key);
        assertEquals(value, v);
        final Map<String, String> m = b.getCustomValues();
        m.clear();
        final String s = b.GetCustomValue(key);
        assertEquals("", s);
    }

    @Test
    public void appendCustomValue() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = NAME;
        final String sep = "_";
        b.AppendCustomValue(key, value, sep);
        final String s = b.GetCustomValue(key);
        assertEquals(value, s);
    }

    @Test
    public void appendCustomValueAppend() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = NAME;
        final String newVal = "bar";
        final String sep = "_";
        b.AddCustomValue(key, value);
        b.AppendCustomValue(key, newVal, sep);
        final String s = b.GetCustomValue(key);
        assertEquals(value + sep + newVal, s);
    }

    @Test(expected = NullPointerException.class)
    public void appendCustomValueDoesntHandleNullKeys() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = null;
        final String value = NAME;
        final String sep = "_";
        b.AppendCustomValue(key, value, sep);
        final String s = b.GetCustomValue(key);
        assertEquals(value, s);
    }

    @Test
    public void appendCustomValueAppendsWithNull() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = null;
        final String newVal = "bar";
        final String sep = "_";
        b.AddCustomValue(key, value);
        b.AppendCustomValue(key, newVal, sep);
        final String s = b.GetCustomValue(key);
        assertEquals(value + sep + newVal, s);
    }

    @Test
    public void appendCustomValueUsesNullSeparator() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String key = "foo";
        final String value = NAME;
        final String newVal = "bar";
        final String sep = null;
        b.AddCustomValue(key, value);
        b.AppendCustomValue(key, newVal, sep);
        final String s = b.GetCustomValue(key);
        assertEquals(value + sep + newVal, s);
    }

    @Test
    public void getCustomValueFromEmptyMap() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String s = b.GetCustomValue(NAME);
        assertEquals("", s);
    }

    @Test
    public void key() {
        final AuditLogableBase b = new TestAuditLogableBase();
        final String s = b.getKey();
        assertEquals(AuditLogType.UNASSIGNED.toString(), s);
    }

    private class TestAuditLogableBase extends AuditLogableBase {
        public final storage_domains STORAGE_DOMAIN = new storage_domains();

        @Override
        public VmTemplateDAO getVmTemplateDAO() {
            final VmTemplateDAO vt = mock(VmTemplateDAO.class);
            final VmTemplate t = new VmTemplate();
            t.setId(GUID);
            t.setname(NAME);
            when(vt.get(Guid.Empty)).thenReturn(t);
            when(vt.get(GUID)).thenReturn(new VmTemplate());
            return vt;
        }

        @Override
        protected VmDAO getVmDAO() {
            final VmDAO v = mock(VmDAO.class);
            when(v.getById(GUID)).thenReturn(new VM());
            when(v.getById(GUID3)).thenThrow(new RuntimeException());
            return v;
        }

        @Override
        public StorageDomainDAO getStorageDomainDAO() {
            final StorageDomainDAO d = mock(StorageDomainDAO.class);
            when(d.getForStoragePool(GUID, GUID)).thenReturn(STORAGE_DOMAIN);
            when(d.getAllForStorageDomain(GUID2)).thenReturn(getStorageDomainList());
            return d;
        }

        @Override
        public StoragePoolDAO getStoragePoolDAO() {
            final StoragePoolDAO s = mock(StoragePoolDAO.class);
            final storage_pool p = new storage_pool();
            p.setId(GUID);
            when(s.get(GUID)).thenReturn(p);
            when(s.get(GUID2)).thenReturn(null);
            return s;
        }

        @Override
        public VdsDAO getVdsDAO() {
            final VdsDAO v = mock(VdsDAO.class);
            final VDS vds1 = new VDS();
            vds1.setvds_id(GUID);
            final VDS vds2 = new VDS();
            vds2.setvds_id(GUID2);
            when(v.get(GUID)).thenReturn(vds1);
            when(v.get(GUID2)).thenReturn(vds2);
            when(v.get(GUID3)).thenThrow(new RuntimeException());
            return v;
        }

        @Override
        protected VdsGroupDAO getVdsGroupDAO() {
            final VdsGroupDAO v = mock(VdsGroupDAO.class);
            final VDSGroup g = new VDSGroup();
            g.setvds_group_id(GUID);
            when(v.get(GUID)).thenReturn(g);
            return v;
        }

        private List<storage_domains> getStorageDomainList() {
            final List<storage_domains> l = new ArrayList<storage_domains>();
            final storage_domains s = new storage_domains();
            s.setstatus(StorageDomainStatus.InActive);
            l.add(s);
            final storage_domains s2 = new storage_domains();
            s2.setstatus(null);
            l.add(s2);
            STORAGE_DOMAIN.setstatus(StorageDomainStatus.Active);
            l.add(STORAGE_DOMAIN);
            return l;
        }
    }
}
