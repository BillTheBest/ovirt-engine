-- The following stored procedures are relevant to oVirt Installer only

Create or replace FUNCTION inst_update_default_storage_pool_type(v_storage_pool_type INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      if (v_storage_pool_type > 0 and v_storage_pool_type < 5) then
         UPDATE storage_pool
         SET storage_pool_type = v_storage_pool_type, _update_date = LOCALTIMESTAMP
         WHERE storage_pool.name = 'Default' and not exists
         (select 1 from storage_domains where storage_domains.storage_pool_name = 'Default');
      end if;
END; $procedure$
LANGUAGE plpgsql;




-- This function calls insert_server_connections, insertstorage_domain_static,insertstorage_domain_dynamic
-- Any change to these functions may effect correctness of the installion.

Create or replace FUNCTION inst_add_iso_storage_domain(v_storage_domain_id UUID, v_name VARCHAR(250), v_connection VARCHAR(250),v_available int, v_used int)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_connection_id UUID;
BEGIN        
    v_connection_id := uuid_generate_v1();
    if not exists (select 1 from storage_server_connections where connection = v_connection) then
        -- Insert storage server connection info
        perform Insertstorage_server_connections(v_connection,cast(v_connection_id as varchar(250)),NULL,NULL,NULL,NULL,1,NULL);
        -- Insert storage domain static info
        perform Insertstorage_domain_static(v_storage_domain_id,cast(v_connection_id as varchar(250)),v_name,1,2,'0');
        -- Insert storage domain dynamic  info
        perform Insertstorage_domain_dynamic(v_available,v_storage_domain_id,v_used);
    end if;
    exception 
        when others then
	    RAISE EXCEPTION 'NUM:%, DETAILS:%', SQLSTATE, SQLERRM;        
END; $procedure$
LANGUAGE plpgsql;

