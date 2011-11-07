package org.ovirt.engine.core.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.ovirt.engine.core.common.action.AddDiskToVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;

/**
 * <code>AsyncTaskDAOTest</code> performs tests against the {@link AsyncTaskDAO} type.
 *
 *
 */
public class AsyncTaskDAOTest extends BaseDAOTestCase {
    private static final int TASK_COUNT = 2;
    private AsyncTaskDAO dao;
    private async_tasks newAsyncTask;
    private async_tasks existingAsyncTask;

    private VdcActionParametersBase params;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        dao = prepareDAO(dbFacade.getAsyncTaskDAO());

        params = new VdcActionParametersBase();
        params.setSessionId("ASESSIONID");
        params.setTransactionScopeOption(TransactionScopeOption.RequiresNew);

        // create some test data
        newAsyncTask = new async_tasks();
        newAsyncTask.settask_id(Guid.NewGuid());
        newAsyncTask.setaction_type(VdcActionType.AddDiskToVm);
        newAsyncTask.setstatus(AsyncTaskStatusEnum.running);
        newAsyncTask.setresult(AsyncTaskResultEnum.success);
        newAsyncTask.setaction_parameters(params);

        existingAsyncTask = dao.get(new Guid("340fd52b-3400-4cdd-8d3f-C9d03704b0aa"));
    }

    /**
     * Ensures that if the id is invalid then no AsyncTask is returned.
     */
    @Test
    public void testGetWithInvalidId() {
        async_tasks result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that, if the id is valid, then retrieving a AsyncTask works as expected.
     */
    @Test
    public void testGet() {
        async_tasks result = dao.get(existingAsyncTask.gettask_id());

        // NOTE: There is no equals() method in async_tasks class.
        assertNotNull(result);

        assertEquals(existingAsyncTask, result);
    }

    /**
     * Ensures that finding all AsyncTasks works as expected.
     */
    @Test
    public void testGetAll() {
        List<async_tasks> result = dao.getAll();

        assertEquals(TASK_COUNT, result.size());
    }

    /**
     * Ensures that saving a ad_group works as expected.
     */
    @Test
    public void testSave() {
        dao.save(newAsyncTask);

        async_tasks result = dao.get(newAsyncTask.gettask_id());

        assertEquals(newAsyncTask, result);
    }

    /**
     * Ensures that updating a ad_group works as expected.
     */
    @Test
    public void testUpdate() {
        existingAsyncTask.setstatus(AsyncTaskStatusEnum.aborting);
        existingAsyncTask.setresult(AsyncTaskResultEnum.failure);
        existingAsyncTask.setaction_type(VdcActionType.AddDiskToVm);
        AddDiskToVmParameters addDiskToVmParams = new AddDiskToVmParameters();
        addDiskToVmParams.setSessionId("SESSION_ID");
        existingAsyncTask.setaction_parameters(addDiskToVmParams);
        dao.update(existingAsyncTask);

        async_tasks result = dao.get(existingAsyncTask.gettask_id());

        assertEquals(existingAsyncTask, result);
    }

    /**
     * Ensures that removing a ad_group works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingAsyncTask.gettask_id());

        async_tasks result = dao.get(existingAsyncTask.gettask_id());

        assertNull(result);
    }
}
