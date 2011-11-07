 

----------------------------------------------------------------
-- [audit_log] Table
--




Create or replace FUNCTION InsertAuditLog(INOUT v_audit_log_id INTEGER ,  
 v_log_time TIMESTAMP WITH TIME ZONE,  
 v_log_type INTEGER,  
    v_log_type_name VARCHAR(100),  
 v_severity INTEGER,  
 v_message VARCHAR(4000),  
 v_user_id UUID ,  
 v_user_name VARCHAR(255) ,  
 v_vds_id UUID ,  
 v_vds_name VARCHAR(255) ,  
 v_vm_id UUID ,  
 v_vm_name VARCHAR(255) ,  
 v_vm_template_id UUID ,  
    v_vm_template_name VARCHAR(40) ,  
    v_storage_pool_id UUID ,  
    v_storage_pool_name VARCHAR(40) ,  
    v_storage_domain_id UUID ,  
    v_storage_domain_name VARCHAR(250) ,
    v_vds_group_id UUID ,  
    v_vds_group_name VARCHAR(255))
   AS $procedure$
   DECLARE
   v_min_alret_severity  INTEGER;
BEGIN
      v_min_alret_severity := 10;
	-- insert regular log messages (non alerts)
      if (v_severity < v_min_alret_severity) then
	 
INSERT INTO audit_log(LOG_TIME, log_type, log_type_name, severity,message, user_id, USER_NAME, vds_id, VDS_NAME, vm_id, VM_NAME,vm_template_id,VM_TEMPLATE_NAME,storage_pool_id,STORAGE_POOL_NAME,storage_domain_id,STORAGE_DOMAIN_NAME,vds_group_id,vds_group_name)
		VALUES(v_log_time, v_log_type, v_log_type_name, v_severity, v_message, v_user_id, v_user_name, v_vds_id, v_vds_name, v_vm_id, v_vm_name,v_vm_template_id,v_vm_template_name,v_storage_pool_id,v_storage_pool_name,v_storage_domain_id,v_storage_domain_name,v_vds_group_id,v_vds_group_name);
		
         v_audit_log_id := CURRVAL('audit_log_seq');
      else
         if (not exists(select audit_log_id from audit_log where vds_name = v_vds_name and log_type = v_log_type)) then
		 
INSERT INTO audit_log(LOG_TIME, log_type, log_type_name, severity,message, user_id, USER_NAME, vds_id, VDS_NAME, vm_id, VM_NAME,vm_template_id,VM_TEMPLATE_NAME,storage_pool_id,STORAGE_POOL_NAME,storage_domain_id,STORAGE_DOMAIN_NAME,vds_group_id,vds_group_name)
			VALUES(v_log_time, v_log_type, v_log_type_name, v_severity, v_message, v_user_id, v_user_name, v_vds_id, v_vds_name, v_vm_id, v_vm_name,v_vm_template_id,v_vm_template_name,v_storage_pool_id,v_storage_pool_name,v_storage_domain_id,v_storage_domain_name,v_vds_group_id,v_vds_group_name);
			
            v_audit_log_id := CURRVAL('audit_log_seq');
         else
            select   audit_log_id INTO v_audit_log_id from audit_log where vds_name = v_vds_name and log_type = v_log_type;
         end if;
      end if;
END; $procedure$
LANGUAGE plpgsql;    




Create or replace FUNCTION UpdateAuditLog(v_audit_log_id INTEGER,  
 v_log_time TIMESTAMP WITH TIME ZONE,  
 v_log_type INTEGER,  
 v_severity INTEGER,  
 v_message VARCHAR(4000),  
 v_user_id UUID ,  
 v_user_name VARCHAR(255) ,  
 v_vds_id UUID ,  
 v_vds_name VARCHAR(255) ,  
 v_vm_id UUID ,  
 v_vm_name VARCHAR(255) ,   
 v_vm_template_id UUID ,  
    v_vm_template_name VARCHAR(40) ,  
    v_storage_pool_id UUID ,  
    v_storage_pool_name VARCHAR(40) ,  
    v_storage_domain_id UUID ,  
    v_storage_domain_name VARCHAR(250),
    v_vds_group_id UUID ,
    v_vds_group_name VARCHAR(255))
RETURNS VOID

	--The [audit_log] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE audit_log
      SET LOG_TIME = v_log_time,log_type = v_log_type,severity = v_severity,message = v_message, 
      user_id = v_user_id,USER_NAME = v_user_name,vds_id = v_vds_id, 
      VDS_NAME = v_vds_name,vm_id = v_vm_id,VM_NAME = v_vm_name,
      vm_template_id = v_vm_template_id,VM_TEMPLATE_NAME = v_vm_template_name,
      storage_pool_id = v_storage_pool_id,STORAGE_POOL_NAME = v_storage_pool_name, 
      storage_domain_id = v_storage_domain_id,STORAGE_DOMAIN_NAME = v_storage_domain_name,
      vds_group_id = v_vds_group_id,vds_group_name = v_vds_group_name
      WHERE audit_log_id = v_audit_log_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteAuditLog(v_audit_log_id INTEGER)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  INTEGER;
BEGIN
		-- Get (and keep) a shared lock with "right to upgrade to exclusive"
		-- in order to force locking parent before children 
      select   audit_log_id INTO v_val FROM audit_log  WHERE audit_log_id = v_audit_log_id     FOR UPDATE;
      DELETE FROM audit_log
      WHERE audit_log_id = v_audit_log_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromAuditLog() RETURNS SETOF audit_log
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM audit_log;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAuditLogByAuditLogId(v_audit_log_id INTEGER) RETURNS SETOF audit_log
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM audit_log
      WHERE audit_log_id = v_audit_log_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAuditLogLaterThenDate(v_date TIMESTAMP WITH TIME ZONE) 
RETURNS SETOF audit_log
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM audit_log
      WHERE LOG_TIME >= v_date;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteAuditLogOlderThenDate(v_date TIMESTAMP WITH TIME ZONE)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_id  INTEGER;
   SWV_RowCount INTEGER;
BEGIN
        -- get first the id from which to remove in order to use index
      select   audit_log_id INTO v_id FROM audit_log WHERE LOG_TIME < v_date   order by audit_log_id desc LIMIT 1;
        -- check if there are candidates to remove
      GET DIAGNOSTICS SWV_RowCount = ROW_COUNT;
      if (SWV_RowCount > 0) then
         DELETE FROM audit_log
         WHERE audit_log_id <= v_id and processed = TRUE and
         audit_log_id not in(select audit_log_id from event_notification_hist);
      end if;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION DeleteAuditAlertLogByVdsIDAndType(v_vds_id UUID,
    v_log_type INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      delete from audit_log where vds_id = v_vds_id and log_type = v_log_type;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION DeleteAuditLogAlertsByVdsID(v_vds_id UUID,
    v_delete_config_alerts BOOLEAN=true)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_min_alret_severity  INTEGER;
   v_no_config_alret_type  INTEGER;
BEGIN
      v_min_alret_severity := 10;
      v_no_config_alret_type := 9000;
      if (v_delete_config_alerts = true) then
         delete from audit_log where vds_id = v_vds_id and severity >= v_min_alret_severity;
      else
         delete from audit_log where vds_id = v_vds_id and severity >= v_min_alret_severity and log_type > v_no_config_alret_type;
      end if;
END; $procedure$
LANGUAGE plpgsql;

/*
Used to find out how many seconds to wait after Start/Stop/Restart PM operations
v_vds_name     - The host name
v_event        - The event [USER_VDS_STOP | USER_VDS_START | USER_VDS_RESTART]
v_wait_for_sec - Configurable time in seconds to wait from last operation.
Returns : The number of seconds we have to wait (negative value means we can do the operation immediately)
*/
Create or replace FUNCTION get_seconds_to_wait_before_pm_operation(v_vds_name varchar(255), v_event varchar(100), v_wait_for_sec INTEGER ) RETURNS INTEGER
   AS $procedure$
declare v_last_event_dt timestamp with time zone;
declare v_now_dt timestamp with time zone;
BEGIN
      if exists(select 1 from audit_log where vds_name = v_vds_name and log_type_name = v_event) then
       begin
          v_last_event_dt := log_time
          from audit_log
          where vds_name = v_vds_name and log_type_name = v_event
          order by audit_log_id desc limit 1;
          v_now_dt :=  CURRENT_TIMESTAMP;
          RETURN cast((extract(epoch from v_last_event_dt) + v_wait_for_sec) - extract(epoch from v_now_dt) as int);
       end;
     else
          RETURN 0;
     end if;
END; $procedure$
LANGUAGE plpgsql;
