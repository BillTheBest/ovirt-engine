----------------------------------------------------------------
-- [vm_pools] Table
--


Create or replace FUNCTION InsertVm_pools(v_vm_pool_description VARCHAR(4000),  
 INOUT v_vm_pool_id UUID ,  
 v_vm_pool_name VARCHAR(255),  
 v_vm_pool_type INTEGER,  
 v_parameters VARCHAR(200),  
 v_vds_group_id UUID)
   AS $procedure$
BEGIN
      v_vm_pool_id := uuid_generate_v1();
      INSERT INTO vm_pools(vm_pool_id,vm_pool_description, vm_pool_name,
								  vm_pool_type,parameters,vds_group_id)
	VALUES(v_vm_pool_id,v_vm_pool_description, v_vm_pool_name,v_vm_pool_type,v_parameters,v_vds_group_id);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION UpdateVm_pools(v_vm_pool_description VARCHAR(4000),  
 v_vm_pool_id UUID,  
 v_vm_pool_name VARCHAR(255),  
 v_vm_pool_type INTEGER,  
 v_parameters VARCHAR(200),  
 v_vds_group_id UUID)
RETURNS VOID

	--The [vm_pools] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE vm_pools
      SET vm_pool_description = v_vm_pool_description,vm_pool_name = v_vm_pool_name,
      vm_pool_type = v_vm_pool_type,parameters = v_parameters,vds_group_id = v_vds_group_id
      WHERE vm_pool_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteVm_pools(v_vm_pool_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
		-- Get (and keep) a shared lock with "right to upgrade to exclusive"
		-- in order to force locking parent before children 
      select   vm_pool_id INTO v_val FROM vm_pools  WHERE vm_pool_id = v_vm_pool_id     FOR UPDATE;
      DELETE FROM vm_pools
      WHERE vm_pool_id = v_vm_pool_id;

		-- delete VmPool permissions --
      DELETE FROM permissions where object_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;




DROP TYPE IF EXISTS GetAllFromVm_pools_rs CASCADE;
Create type GetAllFromVm_pools_rs AS (vm_pool_id UUID, assigned_vm_count INTEGER, vm_running_count INTEGER, vm_pool_description VARCHAR(4000), vm_pool_name VARCHAR(255), vm_pool_type INTEGER, parameters VARCHAR(200), vds_group_id UUID, vds_group_name VARCHAR(40));
Create or replace FUNCTION GetAllFromVm_pools() RETURNS SETOF GetAllFromVm_pools_rs
   AS $procedure$
BEGIN
      -- BEGIN TRAN
BEGIN
         CREATE GLOBAL TEMPORARY TABLE tt_VM_POOL_GROUP
         (
            vm_pool_id UUID,
            assigned_vm_count INTEGER
         ) WITH OIDS;
         exception when others then
            truncate table tt_VM_POOL_GROUP;
      END;
      insert INTO tt_VM_POOL_GROUP(vm_pool_id,
					assigned_vm_count)
      select
      vm_pools_view.vm_pool_id,
			  count(vm_pool_map.vm_pool_id)
      from vm_pools_view
      left join vm_pool_map on vm_pools_view.vm_pool_id = vm_pool_map.vm_pool_id
      group by vm_pools_view.vm_pool_id,vm_pool_map.vm_pool_id;
      BEGIN
         CREATE GLOBAL TEMPORARY TABLE tt_VM_POOL_RUNNING
         (
            vm_pool_id UUID,
            vm_running_count INTEGER
         ) WITH OIDS;
         exception when others then
            truncate table tt_VM_POOL_RUNNING;
      END;
      insert INTO tt_VM_POOL_RUNNING(vm_pool_id,
					vm_running_count)
      select vm_pools_view.vm_pool_id, count(vm_pools_view.vm_pool_id)
      from vm_pools_view
      left join vm_pool_map on vm_pools_view.vm_pool_id = vm_pool_map.vm_pool_id
      left join vm_dynamic on vm_pool_map.vm_guid = vm_dynamic.vm_guid
      where vm_dynamic.status > 0
      group by vm_pools_view.vm_pool_id;
      BEGIN
         CREATE GLOBAL TEMPORARY TABLE tt_VM_POOL_PRERESULT
         (
            vm_pool_id UUID,
            assigned_vm_count INTEGER,
            vm_running_count INTEGER
         ) WITH OIDS;
         exception when others then
            truncate table tt_VM_POOL_PRERESULT;
      END;
      insert INTO tt_VM_POOL_PRERESULT(vm_pool_id,
					assigned_vm_count,
					vm_running_count)
      select pg.vm_pool_id, pg.assigned_vm_count, pr.vm_running_count
      from tt_VM_POOL_GROUP pg
      left join tt_VM_POOL_RUNNING pr on pg.vm_pool_id = pr.vm_pool_id;
      update tt_VM_POOL_PRERESULT
      set vm_running_count = 0
      where vm_running_count is NULL;
      BEGIN
         CREATE GLOBAL TEMPORARY TABLE tt_VM_POOL_RESULT
         (
            vm_pool_id UUID,
            assigned_vm_count INTEGER,
            vm_running_count INTEGER,
            vm_pool_description VARCHAR(4000),
            vm_pool_name VARCHAR(255),
            vm_pool_type INTEGER,
            parameters VARCHAR(200),
            vds_group_id UUID,
            vds_group_name VARCHAR(40)
         ) WITH OIDS;
         exception when others then
            truncate table tt_VM_POOL_RESULT;
      END;
      insert INTO tt_VM_POOL_RESULT(vm_pool_id,
					assigned_vm_count,
					vm_running_count,
					vm_pool_description,
					vm_pool_name,
				vm_pool_type,
				parameters,
				vds_group_id,
				vds_group_name)
      select ppr.vm_pool_id, ppr.assigned_vm_count, ppr.vm_running_count,
  				 p.vm_pool_description, p.vm_pool_name, p.vm_pool_type, p.parameters,
					 p.vds_group_id, p.vds_group_name
      from tt_VM_POOL_PRERESULT ppr
      inner join vm_pools_view p on ppr.vm_pool_id = p.vm_pool_id;
      RETURN QUERY select *
      from tt_VM_POOL_RESULT;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVm_poolsByvm_pool_id(v_vm_pool_id UUID) RETURNS SETOF vm_pools_full_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_pools_full_view.*
      FROM vm_pools_full_view
      WHERE vm_pool_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVm_poolsByvm_pool_name(v_vm_pool_name VARCHAR(255)) RETURNS SETOF vm_pools_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_pools_view.*
      FROM vm_pools_view
      WHERE vm_pool_name = v_vm_pool_name;
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION GetAllVm_poolsByUser_id(v_user_id UUID) RETURNS SETOF vm_pools_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT  DISTINCT vm_pools_view.*
      FROM         users_and_groups_to_vm_pool_map_view INNER JOIN
      vm_pools_view ON
      users_and_groups_to_vm_pool_map_view.vm_pool_id = vm_pools_view.vm_pool_id
      WHERE     (users_and_groups_to_vm_pool_map_view.user_id = v_user_id);
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetVm_poolsByAdGroup_id(v_ad_group_id UUID) RETURNS SETOF vm_pools_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vm_pools_view.*
      FROM  ad_vm_pool_group_map INNER JOIN
      vm_pools_view ON ad_vm_pool_group_map.vm_pool_id = vm_pools_view.vm_pool_id
      WHERE(ad_vm_pool_group_map.group_id = v_ad_group_id);
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetVm_poolsByAdGroup_names(v_ad_group_names VARCHAR(4000)) RETURNS SETOF vm_pools_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vm_pools_view.*
      FROM         ad_groups INNER JOIN
      users_and_groups_to_vm_pool_map_view ON
      ad_groups.id = users_and_groups_to_vm_pool_map_view.user_id INNER JOIN
      vm_pools_view ON users_and_groups_to_vm_pool_map_view.vm_pool_id = vm_pools_view.vm_pool_id
      WHERE     (ad_groups.name in(select Id from fnSplitter(v_ad_group_names)));
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [time_lease_vm_pool_map] Table
--


Create or replace FUNCTION Inserttime_lease_vm_pool_map(v_end_time TIMESTAMP WITH TIME ZONE,  
 v_id UUID,  
 v_start_time TIMESTAMP WITH TIME ZONE,  
 v_type INTEGER,  
 v_vm_pool_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO time_lease_vm_pool_map(end_time, id, start_time, type, vm_pool_id)
	VALUES(v_end_time, v_id, v_start_time, v_type, v_vm_pool_id);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION Updatetime_lease_vm_pool_map(v_end_time TIMESTAMP WITH TIME ZONE,
	v_id UUID,
	v_start_time TIMESTAMP WITH TIME ZONE,
	v_type INTEGER,
	v_vm_pool_id UUID)
RETURNS VOID

	--The [time_lease_vm_pool_map] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE time_lease_vm_pool_map
      SET end_time = v_end_time,start_time = v_start_time,type = v_type
      WHERE id = v_id AND vm_pool_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletetime_lease_vm_pool_map(v_id UUID,  
 v_vm_pool_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
      DELETE FROM time_lease_vm_pool_map
      WHERE id = v_id AND vm_pool_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromtime_lease_vm_pool_map() RETURNS SETOF time_lease_vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT time_lease_vm_pool_map.*
      FROM time_lease_vm_pool_map;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Gettime_lease_vm_pool_mapByidAndByvm_pool_id(v_id UUID,  
 v_vm_pool_id UUID) RETURNS SETOF time_lease_vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT time_lease_vm_pool_map.*
      FROM time_lease_vm_pool_map
      WHERE id = v_id AND vm_pool_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllUsersFromtime_lease_vm_pool_map() RETURNS SETOF time_lease_vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT time_lease_vm_pool_map.*
      FROM time_lease_vm_pool_map
      WHERE time_lease_vm_pool_map.type = 0;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAllGroupsFromtime_lease_vm_pool_map() RETURNS SETOF time_lease_vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT time_lease_vm_pool_map.*
      FROM time_lease_vm_pool_map
      WHERE time_lease_vm_pool_map.type = 0;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION Gettime_lease_vm_pool_mapByid(v_id UUID,  
 v_vm_pool_id UUID) RETURNS SETOF time_lease_vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT time_lease_vm_pool_map.*
      FROM time_lease_vm_pool_map
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Gettime_lease_vm_pool_mapByvm_pool_id(v_id UUID,  
 v_vm_pool_id UUID) RETURNS SETOF time_lease_vm_pool_map
   AS $procedure$
BEGIN
      RETURN QUERY SELECT time_lease_vm_pool_map.*
      FROM time_lease_vm_pool_map
      WHERE vm_pool_id = v_vm_pool_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION Getvm_pools_by_time_leased_id(v_id UUID) RETURNS SETOF vm_pools_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT vm_pools_view.*
      FROM         time_lease_vm_pool_map INNER JOIN
      vm_pools_view ON
      time_lease_vm_pool_map.vm_pool_id = vm_pools_view.vm_pool_id
      WHERE     (time_lease_vm_pool_map.id = v_id);
END; $procedure$
LANGUAGE plpgsql;







Create or replace FUNCTION Gettime_leasedusers_by_vm_pool_id(v_vm_pool_id UUID) RETURNS SETOF users
   AS $procedure$
DECLARE
   v_row RECORD;
   v_users users%ROWTYPE;
BEGIN
      FOR v_row IN SELECT users.*,
			time_lease_vm_pool_map.start_time AS "from",
			time_lease_vm_pool_map.end_time AS "to"
      FROM         users INNER JOIN
      time_lease_vm_pool_map ON users.user_id = time_lease_vm_pool_map.id
      WHERE     (time_lease_vm_pool_map.vm_pool_id = v_vm_pool_id) LOOP
         SELECT INTO v_users * FROM users WHERE users.user_id = v_row.user_id;
         RETURN NEXT v_users;
      END LOOP;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Gettime_leasedad_groups_by_vm_pool_id(v_vmPoolId UUID) RETURNS SETOF ad_groups
   AS $procedure$
DECLARE
   v_row RECORD;
   v_ad_groups ad_groups%ROWTYPE;
BEGIN
      FOR v_row IN SELECT DISTINCT ad_groups.*,
						time_lease_vm_pool_map.start_time AS "from",
						time_lease_vm_pool_map.end_time AS "to"
      FROM         ad_groups INNER JOIN
      time_lease_vm_pool_map
      ON ad_groups.id = time_lease_vm_pool_map.id
      WHERE     (time_lease_vm_pool_map.vm_pool_id = v_vmPoolId) LOOP
         SELECT INTO v_ad_groups * FROM ad_groups WHERE ad_groups.id = v_row.id;
         RETURN NEXT v_ad_groups;
      END LOOP;
END; $procedure$
LANGUAGE plpgsql;


