package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.compat.Guid;

public class NetworkClusterDAOTest extends BaseDAOTestCase {
    private static final int NETWORK_CLUSTER_COUNT = 2;
    private NetworkClusterDAO dao;
    private VDSGroup cluster;
    private network network;
    private network_cluster newNetworkCluster;
    private network networkNoCluster;
    private network_cluster existingNetworkCluster;
    private VDSGroup freeCluster;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getNetworkClusterDAO());

        VdsGroupDAO vdsGroupDAO = prepareDAO(dbFacade.getVdsGroupDAO());

        cluster = vdsGroupDAO.get(new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1"));
        freeCluster = vdsGroupDAO.get(new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d3"));

        NetworkDAO networkDAO = prepareDAO(dbFacade.getNetworkDAO());

        network = networkDAO.getByName("engine");
        networkNoCluster = networkDAO.getByName("engine3");

        newNetworkCluster = new network_cluster();
        newNetworkCluster.setnetwork_id(networkNoCluster.getId());
        newNetworkCluster.setcluster_id(freeCluster.getID());

        existingNetworkCluster = dao.getAll().get(0);
    }

    /**
     * Ensures that retrieving all instances works as expected.
     */
    @Test
    public void testGetAll() {
        List<network_cluster> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(NETWORK_CLUSTER_COUNT, result.size());
    }

    /**
     * Ensures that an empty collection is returned when the cluster specified doesn't have any networks.
     */
    @Test
    public void testGetAllForClusterWithInvalidCluster() {
        List<network_cluster> result = dao.getAllForCluster(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that retrieving all for a specific cluster works as expected.
     */
    @Test
    public void testGetAllForCluster() {
        List<network_cluster> result = dao.getAllForCluster(cluster.getID());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (network_cluster thiscluster : result) {
            assertEquals(cluster.getID(), thiscluster.getcluster_id());
        }
    }

    /**
     * Ensures that an empty collection is returned if the network has no clusters.
     */
    @Test
    public void testGetAllForNetworkWithInvalidNetwork() {
        List<network_cluster> result = dao.getAllForNetwork(networkNoCluster.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Ensures that the right set is returned for the specified network.
     */
    @Test
    public void testGetAllForNetwork() {
        List<network_cluster> result = dao.getAllForNetwork(network.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (network_cluster cluster : result) {
            assertEquals(network.getId(), cluster.getnetwork_id());
        }
    }

    /**
     * Ensures that saving a cluster works as expected.
     */
    @Test
    public void testSave() {
        List<network_cluster> before = dao.getAllForNetwork(networkNoCluster.getId());

        // ensure that we have nothing to start
        assertTrue(before.isEmpty());

        dao.save(newNetworkCluster);

        List<network_cluster> after = dao.getAllForNetwork(networkNoCluster.getId());

        assertFalse(after.isEmpty());
        assertEquals(newNetworkCluster, after.get(0));
    }

    /**
     * Ensures that updating a cluster works as expected.
     */
    @Test
    public void testUpdate() {
        existingNetworkCluster.setstatus(existingNetworkCluster.getstatus() * 2);

        dao.update(existingNetworkCluster);

        List<network_cluster> result = dao.getAll();
        boolean itworked = false;

        for (network_cluster thiscluster : result) {
            itworked |= (thiscluster.getcluster_id().equals(existingNetworkCluster.getcluster_id())) &&
                    (thiscluster.getnetwork_id().equals(existingNetworkCluster.getnetwork_id())) &&
                    (thiscluster.getstatus() == existingNetworkCluster.getstatus());
        }

        assert (itworked);
    }

    /**
     * Ensures that updating a cluster status works as expected.
     */
    @Test
    public void testUpdateStatus() {
        existingNetworkCluster.setstatus(existingNetworkCluster.getstatus() * 2);

        dao.updateStatus(existingNetworkCluster);

        List<network_cluster> result = dao.getAll();
        boolean itworked = false;

        for (network_cluster thiscluster : result) {
            itworked |= (thiscluster.getcluster_id().equals(existingNetworkCluster.getcluster_id())) &&
            (thiscluster.getnetwork_id().equals(existingNetworkCluster.getnetwork_id())) &&
            (thiscluster.getstatus() == existingNetworkCluster.getstatus());
        }

        assert (itworked);
    }

    /**
     * Ensures that removing a network cluster works.
     */
    @Test
    public void testRemove() {
        int before = dao.getAll().size();

        dao.remove(existingNetworkCluster.getcluster_id(), existingNetworkCluster.getnetwork_id());

        int after = dao.getAll().size();

        assertEquals(before - 1, after);
    }
}
