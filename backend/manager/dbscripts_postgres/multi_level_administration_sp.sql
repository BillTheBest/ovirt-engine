

----------------------------------------------------------------
-- [permissions] Table
--




Create or replace FUNCTION InsertPermission(v_ad_element_id UUID,
	v_id UUID,
	v_role_id UUID,
	v_object_id UUID,
	v_object_type_id INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO permissions(ad_element_id, id, role_id, object_id, object_type_id)
	VALUES(v_ad_element_id, v_id, v_role_id, v_object_id, v_object_type_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeletePermission(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
	
	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
	-- in order to force locking parent before children
   select   id INTO v_val FROM permissions  WHERE id = v_id     FOR UPDATE;

   DELETE FROM permissions
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetPermissionsByid(v_id UUID)
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetPermissionsByAdElementId(v_ad_element_id UUID)
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE permissions_view.ad_element_id = v_ad_element_id
   or ad_element_id in(select id from getUserAndGroupsById(v_ad_element_id));

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetPermissionsByRoleId(v_role_id UUID)
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetPermissionsByRoleIdAndAdElementId(v_role_id UUID,
	v_ad_element_id UUID) RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id and ad_element_id = v_ad_element_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetPermissionsByRoleIdAndAdElementIdAndObjectId(v_role_id UUID,
	v_ad_element_id UUID,v_object_id UUID) 
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id and ad_element_id = v_ad_element_id and object_id = v_object_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetDirectPermissionsByAdElementId(v_ad_element_id UUID)
RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE permissions_view.ad_element_id = v_ad_element_id;

END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [roles] Table
--


Create or replace FUNCTION InsertRole(v_description VARCHAR(4000) ,
	v_id UUID,
	v_name VARCHAR(126),
	v_is_readonly BOOLEAN,
	v_role_type INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO roles(description, id, name, is_readonly, role_type)
	VALUES(v_description, v_id, v_name, v_is_readonly, v_role_type);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateRole(v_description VARCHAR(4000) ,
	v_id UUID,
	v_name VARCHAR(126),
	v_is_readonly BOOLEAN,
	v_role_type INTEGER)
RETURNS VOID

	--The [roles] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE roles
      SET description = v_description,name = v_name,is_readonly = v_is_readonly, 
      role_type = v_role_type
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteRole(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
	
	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
   select   id INTO v_val FROM roles  WHERE id = v_id     FOR UPDATE;

   DELETE FROM roles
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAllFromRole() RETURNS SETOF roles
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetRolsByid(v_id UUID) RETURNS SETOF roles
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetRoleByName(v_name VARCHAR(126))
RETURNS SETOF roles
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles
   WHERE name = v_name;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAllRolesByAdElementId(v_ad_element_id UUID) 
RETURNS SETOF roles
   AS $procedure$
BEGIN
   RETURN QUERY SELECT roles.*
   FROM roles INNER JOIN
   permissions ON permissions.role_id = roles.id
   WHERE permissions.ad_element_id = v_ad_element_id
   or permissions.ad_element_id in(select id from getUserAndGroupsById(v_ad_element_id));

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetRolesByAdElementId(v_ad_element_id UUID)
RETURNS SETOF roles
   AS $procedure$
BEGIN
    RETURN QUERY SELECT roles.*
   FROM roles
   inner join permissions permissions on roles.id = permissions.role_id
   WHERE ad_element_id = v_ad_element_id;
END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [roles_relations] Table
--


Create or replace FUNCTION Insertroles_relations(v_role_container_id UUID,
	v_role_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO roles_relations(role_container_id, role_id)
	VALUES(v_role_container_id, v_role_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updateroles_relations(v_role_container_id UUID,
	v_role_id UUID)
RETURNS VOID

	--The [roles_relations] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deleteroles_relations(v_role_container_id UUID,
	v_role_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM roles_relations
   WHERE role_container_id = v_role_container_id AND role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromroles_relations() RETURNS SETOF roles_relations
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles_relations;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getroles_relationsByrole_container_idAndByrole_id(v_role_container_id UUID, v_role_id UUID) RETURNS SETOF roles_relations
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles_relations
   WHERE role_container_id = v_role_container_id AND role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getrole_children(v_role_id UUID) RETURNS SETOF roles_relations
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles_relations
   WHERE role_container_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [user_action_map] Table
--






Create or replace FUNCTION GetPermissionByRoleId(v_role_id UUID) 
RETURNS SETOF permissions
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   from permissions
   where role_id = v_role_id;
END; $procedure$
LANGUAGE plpgsql;





-- gets entity permissions given the user id, action group id and the object type and id
Create or replace FUNCTION get_entity_permissions(v_user_id UUID,v_action_group_id INTEGER,v_object_id UUID,v_object_type_id INTEGER,
OUT v_permission_id UUID)
	-- Add the parameters for the stored procedure here
   AS $procedure$
   DECLARE
   v_everyone_object_id  UUID;
BEGIN
   v_everyone_object_id := getGlobalIds('everyone'); -- hardcoded also in MLA Handler
   select   id INTO v_permission_id from permissions where
		-- get all roles of action
   role_id in(select role_id from roles_groups where action_group_id = v_action_group_id)
		-- get allparents of object
   and (object_id in(select id from  fn_get_entity_parents(v_object_id,v_object_type_id)))
		-- get user and his groups
   and (ad_element_id = v_everyone_object_id or
   ad_element_id = v_user_id or ad_element_id in(select id from getUserAndGroupsById(v_user_id)))   LIMIT 1;
END; $procedure$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [roles_groups] Table
--



Create or replace FUNCTION Insert_roles_groups(v_action_group_id INTEGER,
	v_role_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO roles_groups(action_group_id, role_id)
	VALUES(v_action_group_id, v_role_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Delete_roles_groups(v_action_group_id INTEGER,
	v_role_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM roles_groups
   WHERE action_group_id = v_action_group_id AND role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Get_roles_groups_By_action_group_id_And_By_role_id(v_action_group_id INTEGER,v_role_id UUID) RETURNS SETOF roles_groups 
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles_groups
   where
   action_group_id = v_action_group_id AND
   role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Get_role_groups_By_role_id(v_role_id UUID)
RETURNS SETOF roles_groups
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles_groups
   where
   role_id = v_role_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetPermissionsByEntityId(v_id UUID)
RETURNS SETOF permissions_view
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE object_id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeletePermissionsByEntityId(v_id UUID)
RETURNS VOID
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
   AS $procedure$
BEGIN
   DELETE FROM permissions
   WHERE object_id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetRoleActionGroupsByRoleId(v_id UUID)
RETURNS SETOF roles_groups
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM roles_groups
   WHERE role_id = v_id;
END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION GetPermissionsTreeByEntityId(v_id UUID,
v_object_type_id INTEGER) RETURNS SETOF permissions_view
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE object_id in(select id from  fn_get_entity_parents(v_id,v_object_type_id));
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetPermissionsByRoleIdAndObjectId(v_role_id UUID,
	v_object_id UUID) RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id and object_id = v_object_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetForRoleAndAdElementAndObject_wGroupCheck(v_role_id UUID,
	v_ad_element_id UUID, v_object_id UUID) RETURNS SETOF permissions_view
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM permissions_view
   WHERE role_id = v_role_id and object_id = v_object_id and ad_element_id in (
         select id from getUserAndGroupsById(v_ad_element_id));
END; $procedure$
LANGUAGE plpgsql;

