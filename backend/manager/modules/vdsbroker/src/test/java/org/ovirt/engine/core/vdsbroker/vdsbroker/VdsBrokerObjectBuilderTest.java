package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DbFacade.class })
public class VdsBrokerObjectBuilderTest {

    private final Guid imageId = Guid.createGuidFromString("ed185868-3f9e-4040-a340-e1a64726ebc0");
    private final Guid vmId = Guid.createGuidFromString("71ca53fb-c223-4b31-926d-de1c2ab0b0a9");
    private final String WRITE_LATENCY = "writeLatency";
    private final String READ_LATENCY = "readLatency";
    private final String FLUSH_LATENCY = "flushLatency";
    private final String DEFAULT_VALUE = "0.00";

    @Mock
    private DiskImageDAO diskImageDAO;

    @Mock
    private DbFacade dbFacade;

    public VdsBrokerObjectBuilderTest() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DbFacade.class);
        mockStatic(VdsBrokerObjectsBuilder.class);
    }

    @Test
    public void testDisksUsages() {
        Object[] disksUsages = initDisksUsageData();
        XmlRpcStruct xml = setDisksUsageInXmlRpc(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, xml);
    }

    @Test
    public void testEmptyDisksUsages() {
        Object[] disksUsages = new Object[0];
        XmlRpcStruct xml = setDisksUsageInXmlRpc(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, xml);
    }

    @Test
    public void testDisksUsagesWithEmptyEntry() {
        Object[] disksUsages = initDisksUsageData();
        disksUsages[1] = new HashMap<String, String>();
        XmlRpcStruct xml = setDisksUsageInXmlRpc(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, xml);
    }

    @Test
    public void testDisksUsagesWithNullEntry() {
        Object[] disksUsages = initDisksUsageData();
        disksUsages[1] = null;
        XmlRpcStruct xml = setDisksUsageInXmlRpc(disksUsages);
        validateDisksUsagesList(getVmStatistics(), disksUsages, xml);
    }

    @Test
    public void testNullDisksUsages() {
        VmStatistics vmStatistics = getVmStatistics();
        Object[] disksUsages = null;
        XmlRpcStruct xml = setDisksUsageInXmlRpc(disksUsages);
        VdsBrokerObjectsBuilder.updateVMStatisticsData(vmStatistics, xml);
        assertEquals(null, vmStatistics.getDisksUsage());
    }

    @Test
    public void testFlushLatency() {
        String doulbeValue = "1";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(FLUSH_LATENCY, doulbeValue);
        XmlRpcStruct xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml);
        assertEquals(vmDynamic.getDisks().get(0).getFlushLatency(), new Double("0.000000001"));
    }

    @Test
    public void testReadLatency() {
        String doulbeValue = "2";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(READ_LATENCY, doulbeValue);
        XmlRpcStruct xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml);
        assertEquals(vmDynamic.getDisks().get(0).getReadLatency(), new Double("0.000000002"));
    }

    @Test
    public void testWriteLatency() {
        String doulbeValue = "3";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(WRITE_LATENCY, doulbeValue);
        XmlRpcStruct xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml);
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), new Double("0.000000003"));
    }

    @Test
    public void testOneSecondLatency() {
        String doulbeValue = "1000000000";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(WRITE_LATENCY, doulbeValue);
        XmlRpcStruct xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml);
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), new Double("1"));
    }

    @Test
    public void testZeroLatency() {
        String doulbeValue = "0";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(WRITE_LATENCY, doulbeValue);
        XmlRpcStruct xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml);
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), new Double("0"));
    }

    @Test
    public void testMaximumLatency() {
        String doulbeValue = "999999999000000000";
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(WRITE_LATENCY, doulbeValue);
        XmlRpcStruct xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml);
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), new Double("999999999"));
    }

    @Test
    public void testNullValuesLatency() {
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = setDiskData();
        diskData.put(WRITE_LATENCY, null);
        diskData.put(READ_LATENCY, null);
        diskData.put(FLUSH_LATENCY, null);
        XmlRpcStruct xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml);
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), new Double(DEFAULT_VALUE));
        assertEquals(vmDynamic.getDisks().get(0).getReadLatency(), new Double(DEFAULT_VALUE));
        assertEquals(vmDynamic.getDisks().get(0).getFlushLatency(), new Double(DEFAULT_VALUE));
    }

    @Test
    public void testWhenVDSMNotSendingFields() {
        VmDynamic vmDynamic = getVmDynamic();
        Map<String, Object> diskData = new HashMap<String, Object>();
        diskData.put("readRate", DEFAULT_VALUE);
        diskData.put("imageID", imageId.toString());
        diskData.put("writeRate", DEFAULT_VALUE);

        // Set the default values to the fields.
        diskData.put(FLUSH_LATENCY, DEFAULT_VALUE);

        XmlRpcStruct xml = setMockForTesting(diskData);
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xml);
        assertEquals(vmDynamic.getDisks().get(0).getWriteLatency(), null);
        assertEquals(vmDynamic.getDisks().get(0).getReadLatency(), null);
        assertEquals(vmDynamic.getDisks().get(0).getFlushLatency(), new Double(DEFAULT_VALUE));
    }

    private void validateDisksUsagesList(VmStatistics vmStatistics, Object[] disksUsages, XmlRpcStruct xml) {
        VdsBrokerObjectsBuilder.updateVMStatisticsData(vmStatistics, xml);
        assertEquals(Arrays.asList(disksUsages),
                new JsonObjectDeserializer().deserialize(vmStatistics.getDisksUsage(), ArrayList.class));
    }

    private XmlRpcStruct setMockForTesting(Map<String, Object> diskData) {
        Map<String, Map<String, Object>> disksData = new HashMap<String, Map<String, Object>>();
        disksData.put("vda", diskData);
        XmlRpcStruct xml = setDisksInXmlRpc(disksData);
        mockDiskImageDao();
        return xml;
    }

    private VmStatistics getVmStatistics() {
        VmStatistics vmStatistics = new VmStatistics();
        vmStatistics.setId(vmId);
        return vmStatistics;
    }
    private VmDynamic getVmDynamic() {
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setId(vmId);
        return vmDynamic;
    }

    private void mockDiskImageDao() {
        List<DiskImage> diskImageList = setVmDiskImagesList();
        when(DbFacade.getInstance()).thenReturn(dbFacade);
        when(dbFacade.getDiskImageDAO()).thenReturn(diskImageDAO);
        when(diskImageDAO.getAllForVm(vmId))
                .thenReturn(diskImageList);
    }

    private List<DiskImage> setVmDiskImagesList() {
        List<DiskImage> diskImageList = new ArrayList<DiskImage>();
        DiskImage diskImage = new DiskImage();
        diskImage.setId(imageId);
        diskImage.setimage_group_id(imageId);
        diskImageList.add(diskImage);
        return diskImageList;
    }

    private XmlRpcStruct setDisksInXmlRpc(Map<String, Map<String, Object>> disksData) {
        XmlRpcStruct xml = new XmlRpcStruct();
        Map<String, Object> innerMap = xml.getInnerMap();
        innerMap.put(VdsProperties.vm_disks, disksData);
        return xml;
    }

    private XmlRpcStruct setDisksUsageInXmlRpc(Object[] disksUsageData) {
        XmlRpcStruct xml = new XmlRpcStruct();
        Map<String, Object> innerMap = xml.getInnerMap();
        innerMap.put(VdsProperties.VM_DISKS_USAGE, disksUsageData);
        return xml;
    }

    private Map<String, Object> setDiskData() {
        Map<String, Object> diskData = new HashMap<String, Object>();
        diskData.put("readRate", DEFAULT_VALUE);
        diskData.put("imageID", imageId.toString());
        diskData.put("writeRate", DEFAULT_VALUE);

        // Set the default values to the fields.
        diskData.put(FLUSH_LATENCY, DEFAULT_VALUE);
        diskData.put(WRITE_LATENCY, DEFAULT_VALUE);
        diskData.put(READ_LATENCY, DEFAULT_VALUE);
        return diskData;
    }

    private Object[] initDisksUsageData() {
        Object[] disksUsage = new Object[2];
        disksUsage[0] = getDiskUsageAsMap("11704201216", "FAT32", "c:\\", "9516027904");
        disksUsage[1] = getDiskUsageAsMap("133543936", "CDFS", "d:\\", "133543936");
        return disksUsage;
    }

    private Map<String, String> getDiskUsageAsMap(String total, String fs, String path, String used) {
        Map<String, String> diskUsage = new HashMap<String, String>();
        diskUsage.put("total", total);
        diskUsage.put("fs", fs);
        diskUsage.put("path", path);
        diskUsage.put("used", used);
        return diskUsage;
    }
}
