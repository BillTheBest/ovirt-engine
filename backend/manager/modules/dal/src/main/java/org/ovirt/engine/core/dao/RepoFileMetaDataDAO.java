package org.ovirt.engine.core.dao;

import java.util.List;
import org.ovirt.engine.core.common.businessentities.FileTypeExtension;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>RepoFileMetaDataDao</code> defines a type for performing CRUD operations on instances of
 * {@link RepoFileMetaData}.
 *
 */
public interface RepoFileMetaDataDAO extends DAO {
    /**
     * Remove repository file list from cache table, of domain with the specified id.
     *
     * @param id - The domain id.
     * @param filetype - The file Extension, which should be removed.
     */
    void removeRepoDomainFileList(Guid id, FileTypeExtension filetype);

    /**
     * Add repository file to cache table.
     *
     * @param map - The repository file meta data to insert.
     */
    public void addRepoFileMap(RepoFileMetaData map);

    /**
     * Returns a list of repository files with specific file extension from storage domain id with specific status. If
     * no repository found, will return an empty list.
     */
    public List<RepoFileMetaData> getRepoListForStorageDomainAndStoragePool(Guid storagePoolId, Guid storageDomainId,
            FileTypeExtension fileType);

    /**
     * Returns a list of repository files with specific file extension from storage domain id.<BR/>
     * If no repository found, will return an empty list.
     */
    public List<RepoFileMetaData> getRepoListForStorageDomain(Guid storageDomainId,
            FileTypeExtension fileType);

    /**
     * Returns list of the oldest last refreshed repository files,
     * for each storage pool, storage domain and file type in all System,
     * which meets the same storage domain status, type and SPM status.
     */
    public List<RepoFileMetaData> getAllRepoFilesForAllStoragePools(StorageDomainType storageDomainType,
            StoragePoolStatus storagePoolStatus, StorageDomainStatus storageDomainStatus,
            VDSStatus vdsStatus);
}
