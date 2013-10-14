CREATE PROCEDURE insert_concept_assertions
(IN query VARCHAR(1024),
 IN project VARCHAR(20))
LANGUAGE SQL	 
BEGIN
  DECLARE stmt VARCHAR(1200);
  SET stmt = 'INSERT INTO ' || project || '_ConceptAssertions ' || query;
  PREPARE s1 FROM stmt; 
  EXECUTE s1;
END
@
