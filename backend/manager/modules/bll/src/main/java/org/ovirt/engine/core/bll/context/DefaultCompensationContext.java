package org.ovirt.engine.core.bll.context;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot.EntityStatusSnapshot;
import org.ovirt.engine.core.common.businessentities.BusinessEntitySnapshot.SnapshotType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO;
import org.ovirt.engine.core.utils.Serializer;

/**
 * Default context used to track entities that are changing during a command's execution and save the changes at each
 * state change to the DB.
 */
public class DefaultCompensationContext implements CompensationContext {

    /**
     * A set of all the entities which have been snapshotted ever in this context, since we only want to save the
     * initial snapshot of each entity the comand has changed/created.
     */
    private Set<CachedEntityEntry> cachedEntities = new HashSet<CachedEntityEntry>();

    /**
     * All the entities that have been changed/added since the state/command began.
     */
    private List<BusinessEntitySnapshot> entitiesToPersist = new LinkedList<BusinessEntitySnapshot>();

    /**
     * The serializer which is used to convert the entity to a snapshot.
     */
    private Serializer snapshotSerializer;

    /**
     * The DAO which is used to track all the changed business entities.
     */
    private BusinessEntitySnapshotDAO businessEntitySnapshotDAO;

    /**
     * The id of the command which this context is tracking.
     */
    private Guid commandId;

    /**
     * The type of the command which this context is tracking.
     */
    private String commandType;

    /**
     * @param snapshotSerializer
     *            the snapshotSerializer to set
     */
    public void setSnapshotSerializer(Serializer snapshotSerializer) {
        this.snapshotSerializer = snapshotSerializer;
    }

    /**
     * @param businessEntitySnapshotDAO
     *            the businessEntitySnapshotDAO to set
     */
    public void setBusinessEntitySnapshotDAO(BusinessEntitySnapshotDAO businessEntitySnapshotDAO) {
        this.businessEntitySnapshotDAO = businessEntitySnapshotDAO;
    }

    /**
     * @param commandId
     *            the commandId to set
     */
    public void setCommandId(Guid commandId) {
        this.commandId = commandId;
    }

    /**
     * @param commandType
     *            the commandType to set
     */
    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    @Override
    public void snapshotEntity(BusinessEntity<?> entity) {
        snapshotEntityInMemory(entity, entity, SnapshotType.CHANGED_ENTITY);
    }

    @Override
    public void snapshotNewEntity(BusinessEntity<?> entity) {
        snapshotEntityInMemory(entity, entity.getId(), SnapshotType.NEW_ENTITY_ID);
    }

    @Override
    public void snapshotEntityStatus(BusinessEntity<?> entity, Enum<?> status) {
        EntityStatusSnapshot snapshot = new EntityStatusSnapshot();
        snapshot.setId(entity.getId());
        snapshot.setStatus(status);
        snapshotEntityInMemory(entity, snapshot, SnapshotType.CHANGED_STATUS_ONLY);
    }

    /**
     * Save a snapshot of the entity but only if it is new to this context.
     *
     * @param entity
     *            The entity to save a snapshot of.
     * @param payload
     *            The payload to be serialized and saved.
     * @param snapshotType
     *            The type of snapshot we're taking, so that in compensation we know what is the payload type, and how
     *            to use it to revert the entity state.
     */
    private void snapshotEntityInMemory(BusinessEntity<?> entity, Serializable payload, SnapshotType snapshotType) {
        CachedEntityEntry cachedEntityEntry = new CachedEntityEntry(entity, snapshotType);
        if (!cachedEntities.contains(cachedEntityEntry)) {
            cachedEntities.add(cachedEntityEntry);

            BusinessEntitySnapshot entitySnapshot = new BusinessEntitySnapshot();
            entitySnapshot.setCommandId(commandId);
            entitySnapshot.setCommandType(commandType);
            entitySnapshot.setEntityId(String.valueOf(entity.getId()));
            entitySnapshot.setEntityType(entity.getClass().getName());
            entitySnapshot.setEntitySnapshot((String) snapshotSerializer.serialize(payload));
            entitySnapshot.setSnapshotClass(payload.getClass().getName());
            entitySnapshot.setSnapshotType(snapshotType);
            entitySnapshot.setInsertionOrder(cachedEntities.size());

            entitiesToPersist.add(entitySnapshot);
        }
    }

    @Override
    public void stateChanged() {
        for (BusinessEntitySnapshot snapshot : entitiesToPersist) {
            businessEntitySnapshotDAO.save(snapshot);
        }

        entitiesToPersist.clear();
    }

    @Override
    public void resetCompensation() {
        businessEntitySnapshotDAO.removeAllForCommandId(commandId);
        cachedEntities.clear();
        entitiesToPersist.clear();
    }

    /* -- Inner types -- */

    /**
     * Represents a cached entity which is made of the snapshot type, the entity class and the id, so that we can track
     * which entities have already been recorded and which entities have not.
     */
    private class CachedEntityEntry {

        /**
         * The id of the cached entity.
         */
        private Object id;

        /**
         * The class of the cached entity.
         */
        private Class<?> entityClass;

        /**
         * The type of snapshot that is cached.
         */
        private SnapshotType snapshotType;

        /**
         * Construct a new cached entry for the given entity.
         *
         * @param entity
         *            The entity to construct a cache entry for.
         * @param snapshotType
         *            The type of snapshot.
         */
        public CachedEntityEntry(BusinessEntity<?> entity, SnapshotType snapshotType) {
            super();
            this.id = entity.getId();
            this.entityClass = entity.getClass();
            this.snapshotType = snapshotType;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((entityClass == null) ? 0 : entityClass.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((snapshotType == null) ? 0 : snapshotType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CachedEntityEntry other = (CachedEntityEntry) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (entityClass == null) {
                if (other.entityClass != null) {
                    return false;
                }
            } else if (!entityClass.equals(other.entityClass)) {
                return false;
            }
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            if (snapshotType != other.snapshotType) {
                return false;
            }
            return true;
        }

        private DefaultCompensationContext getOuterType() {
            return DefaultCompensationContext.this;
        }
    }
}
