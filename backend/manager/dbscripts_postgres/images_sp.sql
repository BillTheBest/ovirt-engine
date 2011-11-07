

----------------------------------------------------------------
-- [images] Table
--




Create or replace FUNCTION InsertImage(v_creation_date TIMESTAMP WITH TIME ZONE,
	v_description VARCHAR(4000) ,
	v_image_guid UUID,
	v_internal_drive_mapping VARCHAR(50) ,
	v_it_guid UUID,
	v_size BIGINT,
	v_ParentId UUID,
    v_imageStatus INTEGER ,
    v_lastModified TIMESTAMP WITH TIME ZONE,
	v_app_list VARCHAR(4000) ,
	v_storage_id UUID ,
	v_vm_snapshot_id UUID ,
	v_volume_type INTEGER,
	v_volume_format INTEGER,
	v_disk_type INTEGER,
	v_image_group_id UUID ,
	v_disk_interface INTEGER,
    v_boot BOOLEAN,
    v_wipe_after_delete BOOLEAN,
    v_propagate_errors INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO images(creation_date, description, image_guid, internal_drive_mapping, it_guid, size, ParentId,imageStatus,lastModified, app_list,
    storage_id, vm_snapshot_id, volume_type, image_group_id, volume_format, disk_type, disk_interface, boot, wipe_after_delete, propagate_errors)
	VALUES(v_creation_date, v_description, v_image_guid, v_internal_drive_mapping, v_it_guid, v_size, v_ParentId, v_imageStatus, v_lastModified,v_app_list,
	v_storage_id, v_vm_snapshot_id, v_volume_type, v_image_group_id, v_volume_format, v_disk_type, v_disk_interface, v_boot, v_wipe_after_delete, v_propagate_errors);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION UpdateImage(v_creation_date TIMESTAMP WITH TIME ZONE,
	v_description VARCHAR(4000) ,
	v_image_guid UUID,
	v_internal_drive_mapping VARCHAR(50) ,
	v_it_guid UUID,
	v_size BIGINT,
	v_ParentId UUID,
    v_imageStatus INTEGER ,
    v_lastModified TIMESTAMP WITH TIME ZONE,
	v_app_list VARCHAR(4000) ,
	v_storage_id UUID ,
	v_vm_snapshot_id UUID ,
	v_volume_type INTEGER,
	v_volume_format INTEGER,
	v_disk_type INTEGER,
	v_image_group_id UUID ,
	v_disk_interface INTEGER,
    v_boot BOOLEAN,
    v_wipe_after_delete BOOLEAN,
    v_propagate_errors INTEGER)
RETURNS VOID

	--The [images] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE images
      SET creation_date = v_creation_date,description = v_description,internal_drive_mapping = v_internal_drive_mapping, 
      it_guid = v_it_guid,size = v_size, 
      ParentId = v_ParentId,imageStatus = v_imageStatus,lastModified = v_lastModified, 
      app_list = v_app_list,storage_id = v_storage_id, 
      vm_snapshot_id = v_vm_snapshot_id,volume_type = v_volume_type,image_group_id = v_image_group_id, 
      volume_format = v_volume_format,disk_type = v_disk_type, 
      disk_interface = v_disk_interface,boot = v_boot,wipe_after_delete = v_wipe_after_delete, 
      propagate_errors = v_propagate_errors, 
      _update_date = LOCALTIMESTAMP
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteImage(v_image_guid UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
		-- Get (and keep) a shared lock with "right to upgrade to exclusive"
		-- in order to force locking parent before children 
      select   image_guid INTO v_val FROM images  WHERE image_guid = v_image_guid     FOR UPDATE;
      DELETE FROM images
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromImages() RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetImageByImageGuid(v_image_guid UUID)
RETURNS SETOF vm_images_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM vm_images_view
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAncestralImageByImageGuid(v_image_guid UUID)
RETURNS SETOF images
   AS $procedure$
BEGIN
      RETURN QUERY WITH RECURSIVE ancestor_image(image_guid, parentid) AS (
         SELECT image_guid, parentid 
         FROM images
         WHERE image_guid = v_image_guid
         UNION ALL
         SELECT i.image_guid, i.parentid
         FROM images i, ancestor_image ai
         WHERE i.image_guid = ai.parentid
      )
      SELECT i.*
      FROM ancestor_image ai, images i
      WHERE ai.parentid = '00000000-0000-0000-0000-000000000000'
      AND ai.image_guid = i.image_guid;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetSnapshotByGuid(v_image_guid UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetSnapshotsByStorageDomainId(v_storage_domain_id UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view
      WHERE storage_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetSnapshotByParentGuid(v_parent_guid UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view
      WHERE ParentId = v_parent_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmImageByImageGuid(v_image_guid UUID) 
RETURNS SETOF vm_images_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM vm_images_view
      WHERE image_guid = v_image_guid;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetImagesByVmGuid(v_vm_guid UUID) 
RETURNS SETOF vm_images_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM vm_images_view
      WHERE
      vm_guid = v_vm_guid;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetSnapshotsByVmSnapshotId(v_vm_snapshot_id UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view
      WHERE vm_snapshot_id = v_vm_snapshot_id;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetSnapshotsByImageGroupId(v_image_group_id UUID)
RETURNS SETOF images_storage_domain_view
   AS $procedure$
BEGIN
      RETURN QUERY SELECT *
      FROM images_storage_domain_view images_storage_domain_view
      WHERE image_group_id = v_image_group_id;
END; $procedure$
LANGUAGE plpgsql;


