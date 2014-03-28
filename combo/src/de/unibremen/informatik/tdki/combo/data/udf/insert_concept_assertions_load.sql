CREATE PROCEDURE insert_concept_assertions
(IN query VARCHAR(1024),
 IN project VARCHAR(20))
LANGUAGE SQL	 
BEGIN
  CALL sysproc.admin_cmd('LOAD FROM (' || query || ') OF CURSOR INSERT INTO ' || project || '_ConceptAssertions');
END
@
