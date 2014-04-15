CREATE PROCEDURE insert_concept_assertions
(IN query CLOB(10000),
 IN project VARCHAR(20))
LANGUAGE SQL	 
BEGIN
  CALL sysproc.admin_cmd('LOAD FROM (' || query || ') OF CURSOR INSERT INTO ' || project || '_ConceptAssertions');
END
@
