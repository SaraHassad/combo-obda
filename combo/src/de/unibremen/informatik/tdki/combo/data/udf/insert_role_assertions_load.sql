CREATE PROCEDURE insert_role_assertions
(IN query VARCHAR(6000),
 IN project VARCHAR(20))
LANGUAGE SQL	 
BEGIN
  CALL sysproc.admin_cmd('LOAD FROM (' || query || ') OF CURSOR INSERT INTO ' || project || '_RoleAssertions INDEXING MODE REBUILD');
END
@
