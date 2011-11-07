/*
* Copyright © 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.util;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Test;

import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.Storage;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VM;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class LinkHelperTest extends Assert {

    private static final String VM_ID = "awesome";
    private static final String CLUSTER_ID = "alarming";
    private static final String TEMPLATE_ID = "astonishing";
    private static final String VM_POOL_ID = "beautiful";
    private static final String STORAGE_DOMAIN_ID = "breathtaking";
    private static final String HOST_ID = "magnificent";
    private static final String DATA_CENTER_ID = "majestic";
    private static final String NETWORK_ID = "stupendous";
    private static final String TAG_ID = "outstanding";
    private static final String FILE_ID = "faroutdude";
    private static final String CDROM_ID = "wonderful";
    private static final String DISK_ID = "fantastic";
    private static final String NIC_ID = "super";
    private static final String STORAGE_ID = "sensational";
    private static final String USER_ID = "doublerainbowalltheway";
    private static final String GROUP_ID = "bankruptnation";
    private static final String EVENT_ID = "eventtest";
    private static final String STATISTIC_ID = "bleedindeadly";

    private static final String URI_ROOT = "http://localhost:8080";
    private static final String BASE_PATH = "/restapi-definition-powershell";

    private static final String VM_HREF = BASE_PATH + "/vms/" + VM_ID;
    private static final String CLUSTER_HREF = BASE_PATH + "/clusters/" + CLUSTER_ID;
    private static final String TEMPLATE_HREF = BASE_PATH + "/templates/" + TEMPLATE_ID;
    private static final String VM_POOL_HREF = BASE_PATH + "/vmpools/" + VM_POOL_ID;
    private static final String STORAGE_DOMAIN_HREF = BASE_PATH + "/storagedomains/" + STORAGE_DOMAIN_ID;
    private static final String ATTACHED_STORAGE_DOMAIN_HREF = BASE_PATH + "/datacenters/" + DATA_CENTER_ID + "/storagedomains/" + STORAGE_DOMAIN_ID;
    private static final String STORAGE_DOMAIN_VM_HREF = STORAGE_DOMAIN_HREF + "/vms/" + VM_ID;
    private static final String STORAGE_DOMAIN_TEMPLATE_HREF = STORAGE_DOMAIN_HREF + "/templates/" + TEMPLATE_ID;
    private static final String HOST_HREF = BASE_PATH + "/hosts/" + HOST_ID;
    private static final String DATA_CENTER_HREF = BASE_PATH + "/datacenters/" + DATA_CENTER_ID;
    private static final String NETWORK_HREF = BASE_PATH + "/networks/" + NETWORK_ID;
    private static final String TAG_HREF = BASE_PATH + "/tags/" + TAG_ID;
    private static final String VM_TAG_HREF = BASE_PATH + "/vms/" + VM_ID + "/tags/" + TAG_ID;
    private static final String HOST_TAG_HREF = BASE_PATH + "/hosts/" + HOST_ID + "/tags/" + TAG_ID;
    private static final String USER_TAG_HREF = BASE_PATH + "/users/" + USER_ID + "/tags/" + TAG_ID;
    private static final String CLUSTER_NETWORK_HREF = BASE_PATH + "/clusters/" + CLUSTER_ID + "/networks/" + NETWORK_ID;
    private static final String FILE_HREF = BASE_PATH + "/storagedomains/" + STORAGE_DOMAIN_ID + "/files/" + FILE_ID;
    private static final String CDROM_HREF = VM_HREF + "/cdroms/" + CDROM_ID;
    private static final String DISK_HREF = VM_HREF + "/disks/" + DISK_ID;
    private static final String NIC_HREF = VM_HREF + "/nics/" + NIC_ID;
    private static final String STORAGE_HREF = HOST_HREF + "/storage/" + STORAGE_ID;
    private static final String GROUP_HREF = BASE_PATH + "/groups/" + GROUP_ID;
    private static final String EVENT_HREF = BASE_PATH + "/events/" + EVENT_ID;
    private static final String STATISTIC_HREF = VM_HREF + "/statistics/" + STATISTIC_ID;

    @Test
    public void testEventLinks() throws Exception {
        Event event = new Event();
        event.setId(EVENT_ID);

        LinkHelper.addLinks(setUpUriExpectations(), event);

        assertEquals(EVENT_HREF, event.getHref());
    }

    @Test
    public void testVmLinks() throws Exception {
        doTestVmLinks(false);
    }

    @Test
    public void testVmLinksSuggestedParent() throws Exception {
        doTestVmLinks(true);
    }

    private void doTestVmLinks(boolean suggestParent) throws Exception {
        VM vm = new VM();
        vm.setId(VM_ID);
        vm.setCluster(new Cluster());
        vm.getCluster().setId(CLUSTER_ID);
        vm.setTemplate(new Template());
        vm.getTemplate().setId(TEMPLATE_ID);
        vm.setVmPool(new VmPool());
        vm.getVmPool().setId(VM_POOL_ID);

        if (suggestParent) {
            LinkHelper.addLinks(setUpUriExpectations(), vm, VM.class);
        } else {
            LinkHelper.addLinks(setUpUriExpectations(), vm);
        }

        assertEquals(VM_HREF, vm.getHref());
        assertEquals(CLUSTER_HREF, vm.getCluster().getHref());
        assertEquals(TEMPLATE_HREF, vm.getTemplate().getHref());
        assertEquals(VM_POOL_HREF, vm.getVmPool().getHref());
    }

    @Test
    public void testClusterLinks() throws Exception {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        cluster.setDataCenter(new DataCenter());
        cluster.getDataCenter().setId(DATA_CENTER_ID);

        LinkHelper.addLinks(setUpUriExpectations(), cluster);

        assertEquals(CLUSTER_HREF, cluster.getHref());
        assertEquals(DATA_CENTER_HREF, cluster.getDataCenter().getHref());
    }

    @Test
    public void testHostLinks() throws Exception {
        Host host = new Host();
        host.setId(HOST_ID);

        LinkHelper.addLinks(setUpUriExpectations(), host);

        assertEquals(HOST_HREF, host.getHref());
    }

    @Test
    public void testStorageDomainLinks() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(STORAGE_DOMAIN_ID);

        storageDomain.setStorage(new Storage());
        storageDomain.getStorage().setPath("foo");

        LinkHelper.addLinks(setUpUriExpectations(), storageDomain);

        assertEquals(STORAGE_DOMAIN_HREF, storageDomain.getHref());
        assertNull(storageDomain.getStorage().getHref());
    }

    @Test
    public void testAttachedStorageDomainLinks() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(STORAGE_DOMAIN_ID);

        storageDomain.setDataCenter(new DataCenter());
        storageDomain.getDataCenter().setId(DATA_CENTER_ID);

        LinkHelper.addLinks(setUpUriExpectations(), storageDomain);

        assertEquals(ATTACHED_STORAGE_DOMAIN_HREF, storageDomain.getHref());
    }

    @Test
    public void testStorageDomainVmLinks() throws Exception {
        VM vm = new VM();
        vm.setId(VM_ID);

        vm.setStorageDomain(new StorageDomain());
        vm.getStorageDomain().setId(STORAGE_DOMAIN_ID);

        vm = LinkHelper.addLinks(setUpUriExpectations(), vm);

        assertEquals(STORAGE_DOMAIN_VM_HREF, vm.getHref());
        assertEquals(STORAGE_DOMAIN_HREF, vm.getStorageDomain().getHref());
    }

    @Test
    public void testStorageDomainTemplateLinks() throws Exception {
        Template template = new Template();
        template.setId(TEMPLATE_ID);

        template.setStorageDomain(new StorageDomain());
        template.getStorageDomain().setId(STORAGE_DOMAIN_ID);

        template = LinkHelper.addLinks(setUpUriExpectations(), template);

        assertEquals(STORAGE_DOMAIN_TEMPLATE_HREF, template.getHref());
        assertEquals(STORAGE_DOMAIN_HREF, template.getStorageDomain().getHref());
    }

    @Test
    public void testDataCenterLinks() throws Exception {
        DataCenter dataCenter = new DataCenter();
        dataCenter.setId(DATA_CENTER_ID);

        LinkHelper.addLinks(setUpUriExpectations(), dataCenter);

        assertEquals(DATA_CENTER_HREF, dataCenter.getHref());
    }

    @Test
    public void testNetworkLinks() throws Exception {
        Network network = new Network();
        network.setId(NETWORK_ID);

        LinkHelper.addLinks(setUpUriExpectations(), network);

        assertEquals(NETWORK_HREF, network.getHref());
    }

    @Test
    public void testClusterNetworkLinks() throws Exception {
        Network network = new Network();
        network.setId(NETWORK_ID);
        network.setCluster(new Cluster());
        network.getCluster().setId(CLUSTER_ID);

        LinkHelper.addLinks(setUpUriExpectations(), network);

        assertEquals(CLUSTER_NETWORK_HREF, network.getHref());
    }

    @Test
    public void testTagLinks() throws Exception {
        Tag tag = new Tag();
        tag.setId(TAG_ID);

        LinkHelper.addLinks(setUpUriExpectations(), tag);

        assertEquals(TAG_HREF, tag.getHref());
    }

    @Test
    public void testVmTagLinks() throws Exception {
        Tag tag = new Tag();
        tag.setId(TAG_ID);
        tag.setVm(new VM());
        tag.getVm().setId(VM_ID);

        LinkHelper.addLinks(setUpUriExpectations(), tag);

        assertEquals(VM_TAG_HREF, tag.getHref());
    }

    @Test
    public void testHostTagLinks() throws Exception {
        Tag tag = new Tag();
        tag.setId(TAG_ID);
        tag.setHost(new Host());
        tag.getHost().setId(HOST_ID);

        LinkHelper.addLinks(setUpUriExpectations(), tag);

        assertEquals(HOST_TAG_HREF, tag.getHref());
    }

    @Test
    public void testUserTagLinks() throws Exception {
        Tag tag = new Tag();
        tag.setId(TAG_ID);
        tag.setUser(new User());
        tag.getUser().setId(USER_ID);

        LinkHelper.addLinks(setUpUriExpectations(), tag);

        assertEquals(USER_TAG_HREF, tag.getHref());
    }

    @Test
    public void testFileLinks() throws Exception {
        File file = new File();
        file.setId(FILE_ID);

        file.setStorageDomain(new StorageDomain());
        file.getStorageDomain().setId(STORAGE_DOMAIN_ID);

        LinkHelper.addLinks(setUpUriExpectations(), file);

        assertEquals(FILE_HREF, file.getHref());
    }

    @Test
    public void testCdRomLinks() throws Exception {
        CdRom cdrom = new CdRom();
        cdrom.setId(CDROM_ID);

        cdrom.setVm(new VM());
        cdrom.getVm().setId(VM_ID);

        LinkHelper.addLinks(setUpUriExpectations(), cdrom);

        assertEquals(CDROM_HREF, cdrom.getHref());
    }

    @Test
    public void testDiskLinks() throws Exception {
        Disk disk = new Disk();
        disk.setId(DISK_ID);

        disk.setVm(new VM());
        disk.getVm().setId(VM_ID);

        LinkHelper.addLinks(setUpUriExpectations(), disk);

        assertEquals(DISK_HREF, disk.getHref());
    }

    @Test
    public void testNicLinks() throws Exception {
        NIC nic = new NIC();
        nic.setId(NIC_ID);

        nic.setVm(new VM());
        nic.getVm().setId(VM_ID);

        LinkHelper.addLinks(setUpUriExpectations(), nic);

        assertEquals(NIC_HREF, nic.getHref());
    }

    @Test
    public void testStorageLinks() throws Exception {
        Storage storage = new Storage();
        storage.setId(STORAGE_ID);

        storage.setHost(new Host());
        storage.getHost().setId(HOST_ID);

        LinkHelper.addLinks(setUpUriExpectations(), storage);

        assertEquals(STORAGE_HREF, storage.getHref());
        assertEquals(HOST_HREF, storage.getHost().getHref());
    }

    @Test
    public void testGroupLinks() throws Exception {
        Group group = new Group();
        group.setId(GROUP_ID);

        LinkHelper.addLinks(setUpUriExpectations(), group);

        assertEquals(GROUP_HREF, group.getHref());
    }

    @Test
    public void testStatisticLinks() throws Exception {
        Statistic statistic = new Statistic();
        statistic.setId(STATISTIC_ID);

        statistic.setVm(new VM());
        statistic.getVm().setId(VM_ID);

        LinkHelper.addLinks(setUpUriExpectations(), statistic);

        assertEquals(STATISTIC_HREF, statistic.getHref());
    }

    @Test
    public void testCombine() throws Exception {
        assertEquals("/foo/bar", LinkHelper.combine("/foo", "bar"));
        assertEquals("/foo/bar", LinkHelper.combine("/foo/", "bar"));
        assertEquals("/foo/bar", LinkHelper.combine("/foo/", "/bar"));
        assertEquals("/foo/bar", LinkHelper.combine("/foo", "/bar"));
    }

    private UriInfo setUpUriExpectations() {
        UriInfo uriInfo = createMock(UriInfo.class);
        expect(uriInfo.getBaseUri()).andReturn(URI.create(URI_ROOT + BASE_PATH)).anyTimes();
        replay(uriInfo);
        return uriInfo;
    }
}
