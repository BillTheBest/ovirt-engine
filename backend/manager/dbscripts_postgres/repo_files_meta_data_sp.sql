----------------------------------------------------------------
-- [repo_file_meta_data] Table
--
Create or replace FUNCTION InsertRepo_domain_file_meta_data(v_repo_domain_id UUID,
    v_repo_file_name VARCHAR(256),
    v_size BIGINT,
    v_date_created TIMESTAMP WITH TIME ZONE,
    v_last_refreshed BIGINT,
    v_file_type INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN

INSERT INTO repo_file_meta_data(repo_domain_id, repo_file_name, size, date_created, last_refreshed, file_type)
    VALUES(v_repo_domain_id, v_repo_file_name, v_size, v_date_created, v_last_refreshed, v_file_type);

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteRepo_domain_file_list(v_repo_domain_id UUID, v_file_type INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
    
   DELETE FROM repo_file_meta_data
   WHERE repo_domain_id = v_repo_domain_id
     AND file_type = v_file_type;
    
   RETURN;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetRepo_files_by_storage_domain_and_storage_pool(v_storage_pool_id UUID,
	v_storage_domain_id UUID, v_file_type INTEGER)
RETURNS SETOF storage_domain_file_repos
   AS $procedure$
BEGIN
-- SET TRANSACTION ISOLATION LEVEL READ COMMITTED
   RETURN QUERY SELECT storage_domain_file_repos.*
   FROM storage_domain_file_repos
   WHERE storage_domain_id = v_storage_domain_id
   AND storage_pool_id = v_storage_pool_id
   AND storage_domain_file_repos.file_type = v_file_type
   ORDER BY storage_domain_file_repos.last_refreshed;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetRepo_files_by_storage_domain(v_storage_domain_id UUID, v_file_type INTEGER)
RETURNS SETOF repo_file_meta_data
   AS $procedure$
BEGIN
-- SET TRANSACTION ISOLATION LEVEL READ COMMITTED
   RETURN QUERY SELECT repo_file_meta_data.*
   FROM repo_file_meta_data
   WHERE repo_domain_id = v_storage_domain_id
   AND repo_file_meta_data.file_type = v_file_type
   ORDER BY repo_file_meta_data.last_refreshed;
END; $procedure$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS GetRepo_files_in_all_storage_pools_rs CASCADE;
CREATE TYPE GetRepo_files_in_all_storage_pools_rs AS(storage_domain_id UUID, last_refreshed BIGINT, file_type INTEGER);

Create or replace FUNCTION GetRepo_files_in_all_storage_pools(v_storage_domain_type INTEGER, v_storage_pool_status INTEGER,
   v_vds_status INTEGER, v_storage_domain_status INTEGER)
RETURNS SETOF GetRepo_files_in_all_storage_pools_rs
   AS $procedure$
BEGIN
-- SET TRANSACTION ISOLATION LEVEL READ COMMITTED
 RETURN QUERY SELECT distinct b.storage_domain_id,c.last_refreshed,b.file_type
   FROM storage_domain_file_repos b  
   LEFT OUTER JOIN 
   (SELECT storage_domain_id,file_type,min(last_refreshed) as last_refreshed
      FROM storage_domain_file_repos a
   	  Group by storage_domain_id,file_type) as c ON b.storage_domain_id = c.storage_domain_id
                                                 AND b.file_type = c.file_type
   WHERE b.storage_domain_type = v_storage_domain_type
     and b.storage_pool_status = v_storage_pool_status
     and b.storage_domain_status = v_storage_domain_status
	 and b.vds_status = v_vds_status;
END; $procedure$
LANGUAGE plpgsql;