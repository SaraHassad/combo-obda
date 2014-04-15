CREATE PROCEDURE insert_concept_assertions
(IN query CLOB(10000),
 IN project VARCHAR(20))
LANGUAGE SQL	 
BEGIN
  EXECUTE IMMEDIATE 'INSERT INTO ' || project || '_ConceptAssertions ' || query;
END
@
