CREATE PROCEDURE insert_role_assertions
(IN query CLOB(10000),
 IN project VARCHAR(20))
LANGUAGE SQL	 
BEGIN
  EXECUTE IMMEDIATE 'INSERT INTO ' || project || '_RoleAssertions ' || query;
END
@
