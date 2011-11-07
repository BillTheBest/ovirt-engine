package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.apache.commons.httpclient.HttpClient;

import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusOnlyReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StorageDomainListReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public class IrsServerWrapper implements IIrsServer {

    private IrsServerConnector irsServer;
    private HttpClient httpClient;

    public IrsServerWrapper(IrsServerConnector innerImplementor, HttpClient httpClient) {
        this.irsServer = innerImplementor;
        this.httpClient = httpClient;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public OneUuidReturnForXmlRpc createVolume(String sdUUID, String spUUID, String imgGUID, String size,
            int volFormat, int volType, int diskType, String volUUID, String descr, String srcImgGUID, String srcVolUUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.createVolume(sdUUID, spUUID, imgGUID, size, volFormat,
                volType, diskType, volUUID, descr, srcImgGUID, srcVolUUID);
        OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public OneUuidReturnForXmlRpc copyImage(String sdUUID, String spUUID, String vmGUID, String srcImgGUID,
            String srcVolUUID, String dstImgGUID, String dstVolUUID, String descr, String dstSdUUID, int volType,
            int volFormat, int preallocate, String postZero, String force) {
        Map<String, Object> xmlRpcReturnValue = irsServer.copyImage(sdUUID, spUUID, vmGUID, srcImgGUID, srcVolUUID,
                dstImgGUID, dstVolUUID, descr, dstSdUUID, volType, volFormat, preallocate, postZero, force);
        OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc setVolumeDescription(String sdUUID, String spUUID, String imgGUID, String volUUID,
            String description) {
        Map<String, Object> xmlRpcReturnValue = irsServer.setVolumeDescription(sdUUID, spUUID, imgGUID, volUUID,
                description);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc setVolumeLegality(String sdUUID, String spUUID, String imgGUID, String volUUID,
            String legality) {
        Map<String, Object> xmlRpcReturnValue = irsServer.setVolumeLegality(sdUUID, spUUID, imgGUID, volUUID, legality);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public OneUuidReturnForXmlRpc mergeSnapshots(String sdUUID, String spUUID, String vmGUID, String imgGUID,
            String ancestorUUID, String successorUUID, String postZero) {
        Map<String, Object> xmlRpcReturnValue = irsServer.mergeSnapshots(sdUUID, spUUID, vmGUID, imgGUID, ancestorUUID,
                successorUUID, postZero);
        OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public OneUuidReturnForXmlRpc deleteVolume(String sdUUID, String spUUID, String imgGUID, String[] volUUID,
            String postZero, String force) {
        Map<String, Object> xmlRpcReturnValue = irsServer.deleteVolume(sdUUID, spUUID, imgGUID, volUUID, postZero,
                force);
        OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public UuidListReturnForXmlRpc getVolumesList(String sdUUID, String spUUID, String imgGUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getVolumesList(sdUUID, spUUID, imgGUID);
        UuidListReturnForXmlRpc wrapper = new UuidListReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public OneImageInfoReturnForXmlRpc getVolumeInfo(String sdUUID, String spUUID, String imgGUID, String volUUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getVolumeInfo(sdUUID, spUUID, imgGUID, volUUID);
        OneImageInfoReturnForXmlRpc wrapper = new OneImageInfoReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public IrsStatsAndStatusXmlRpc getIrsStats() {
        Map<String, Object> xmlRpcReturnValue = irsServer.getStats();
        IrsStatsAndStatusXmlRpc wrapper = new IrsStatsAndStatusXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public OneUuidReturnForXmlRpc exportCandidate(String sdUUID, String vmGUID, String[] volumesList, String vmMeta,
            String templateGUID, String templateVolGUID, String templateMeta, String expPath, String collapse,
            String force) {
        Map<String, Object> xmlRpcReturnValue = irsServer.exportCandidate(sdUUID, vmGUID, volumesList, vmMeta,
                templateGUID, templateVolGUID, templateMeta, expPath, collapse, force);
        OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public IrsVMListReturnForXmlRpc getImportCandidates(String path, String type, String vmType) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getImportCandidates(path, type, vmType);
        IrsVMListReturnForXmlRpc wrapper = new IrsVMListReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public ImportCandidatesInfoReturnForXmlRpc getImportCandidatesInfo(String path, String type, String vmType) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getImportCandidatesInfo(path, type, vmType);
        ImportCandidatesInfoReturnForXmlRpc wrapper = new ImportCandidatesInfoReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public ImportCandidateInfoReturnForXmlRpc getCandidateInfo(String candidateGUID, String path, String type) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getCandidateInfo(candidateGUID, path, type);
        ImportCandidateInfoReturnForXmlRpc wrapper = new ImportCandidateInfoReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public OneUuidReturnForXmlRpc importCandidate(String sdUUID, String vmGUID, String templateGUID,
            String templateVolGUID, String path, String type, String force) {
        Map<String, Object> xmlRpcReturnValue = irsServer.importCandidate(sdUUID, vmGUID, templateGUID,
                templateVolGUID, path, type, force);
        OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public IsoListReturnForXmlRpc getIsoList(String spUUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getIsoList(spUUID);
        IsoListReturnForXmlRpc wrapper = new IsoListReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public IsoListReturnForXmlRpc getFloppyList(String spUUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getFloppyList(spUUID);
        IsoListReturnForXmlRpc wrapper = new IsoListReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc extendVolume(String sdUUID, String spUUID, String imgGUID, String volUUID,
            int newSize) {
        Map<String, Object> xmlRpcReturnValue = irsServer.extendVolume(sdUUID, spUUID, imgGUID, volUUID, newSize);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StorageStatusReturnForXmlRpc activateStorageDomain(String sdUUID, String spUUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.activateStorageDomain(sdUUID, spUUID);
        StorageStatusReturnForXmlRpc wrapper = new StorageStatusReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc deactivateStorageDomain(String sdUUID, String spUUID, String msdUUID,
            int masterVersion) {
        Map<String, Object> xmlRpcReturnValue = irsServer.deactivateStorageDomain(sdUUID, spUUID, msdUUID,
                masterVersion);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc detachStorageDomain(String sdUUID, String spUUID, String msdUUID, int masterVersion) {
        Map<String, Object> xmlRpcReturnValue = irsServer.detachStorageDomain(sdUUID, spUUID, msdUUID, masterVersion);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc forcedDetachStorageDomain(String sdUUID, String spUUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.forcedDetachStorageDomain(sdUUID, spUUID);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc attachStorageDomain(String sdUUID, String spUUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.attachStorageDomain(sdUUID, spUUID);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc setStorageDomainDescription(String sdUUID, String description) {
        Map<String, Object> xmlRpcReturnValue = irsServer.setStorageDomainDescription(sdUUID, description);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StorageDomainListReturnForXmlRpc reconstructMaster(String spUUID, String msdUUID, String masterVersion) {
        Map<String, Object> xmlRpcReturnValue = irsServer.reconstructMaster(spUUID, msdUUID, masterVersion);
        StorageDomainListReturnForXmlRpc wrapper = new StorageDomainListReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc extendStorageDomain(String sdUUID, String spUUID, String[] devlist) {
        Map<String, Object> xmlRpcReturnValue = irsServer.extendStorageDomain(sdUUID, spUUID, devlist);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc setStoragePoolDescription(String spUUID, String description) {
        Map<String, Object> xmlRpcReturnValue = irsServer.setStoragePoolDescription(spUUID, description);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StoragePoolInfoReturnForXmlRpc getStoragePoolInfo(String spUUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getStoragePoolInfo(spUUID);
        StoragePoolInfoReturnForXmlRpc wrapper = new StoragePoolInfoReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc destroyStoragePool(String spUUID, int hostSpmId, String SCSIKey) {
        Map<String, Object> xmlRpcReturnValue = irsServer.destroyStoragePool(spUUID, hostSpmId, SCSIKey);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public OneUuidReturnForXmlRpc deleteImage(String sdUUID, String spUUID, String imgGUID, String postZero,
            String force) {
        Map<String, Object> xmlRpcReturnValue = irsServer.deleteImage(sdUUID, spUUID, imgGUID, postZero, force);
        OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public OneUuidReturnForXmlRpc moveImage(String spUUID, String srcDomUUID, String dstDomUUID, String imgGUID,
            String vmGUID, int op, String postZero, String force) {
        Map<String, Object> xmlRpcReturnValue = irsServer.moveImage(spUUID, srcDomUUID, dstDomUUID, imgGUID, vmGUID,
                op, postZero, force);
        OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public OneUuidReturnForXmlRpc moveMultipleImages(String spUUID, String srcDomUUID, String dstDomUUID,
            XmlRpcStruct imgDict, String vmGUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.moveMultipleImages(spUUID, srcDomUUID, dstDomUUID,
                imgDict.getInnerMap(), vmGUID);
        OneUuidReturnForXmlRpc wrapper = new OneUuidReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StorageDomainGuidListReturnForXmlRpc getImageDomainsList(String spUUID, String imgUUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getImageDomainsList(spUUID, imgUUID);
        StorageDomainGuidListReturnForXmlRpc wrapper = new StorageDomainGuidListReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc setMaxHosts(int maxHosts) {
        Map<String, Object> xmlRpcReturnValue = irsServer.setMaxHosts(maxHosts);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    @Override
    public StatusOnlyReturnForXmlRpc updateVM(String spUUID, Map[] vms) {
        Map<String, Object> xmlRpcReturnValue = irsServer.updateVM(spUUID, vms);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc removeVM(String spUUID, String vmGUID) {
        Map<String, Object> xmlRpcReturnValue = irsServer.removeVM(spUUID, vmGUID);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    @Override
    public StatusOnlyReturnForXmlRpc updateVMInImportExport(String spUUID, Map[] vms, String StorageDomainId) {
        Map<String, Object> xmlRpcReturnValue = irsServer.updateVM(spUUID, vms, StorageDomainId);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public StatusOnlyReturnForXmlRpc removeVM(String spUUID, String vmGUID, String storageDomainId) {
        Map<String, Object> xmlRpcReturnValue = irsServer.removeVM(spUUID, vmGUID, storageDomainId);
        StatusOnlyReturnForXmlRpc wrapper = new StatusOnlyReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public GetVmsInfoReturnForXmlRpc getVmsInfo(String storagePoolId, String storageDomainId, String[] VMIDList) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getVmsInfo(storagePoolId, storageDomainId, VMIDList);
        GetVmsInfoReturnForXmlRpc wrapper = new GetVmsInfoReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }

    public GetVmsListReturnForXmlRpc getVmsList(String storagePoolId, String storageDomainId) {
        Map<String, Object> xmlRpcReturnValue = irsServer.getVmsList(storagePoolId, storageDomainId);
        GetVmsListReturnForXmlRpc wrapper = new GetVmsListReturnForXmlRpc(xmlRpcReturnValue);
        return wrapper;
    }
}
