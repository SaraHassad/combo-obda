CREATE PROCEDURE insert_role_assertions
(IN query VARCHAR(6000),
 IN project VARCHAR(20))
LANGUAGE SQL	 
BEGIN
  DECLARE stmt VARCHAR(6100);
  SET stmt = 'INSERT INTO ' || project || '_RoleAssertions ' || query;
  PREPARE s1 FROM stmt; 
  EXECUTE s1;
END
@
