CREATE OR REPLACE PROCEDURE combo_drop_project
( 
  IN project VARCHAR(20)
)
  LANGUAGE SQL
BEGIN
  DECLARE tablename VARCHAR(50);

  SET tablename = project || '_RoleInclusions';
  CALL combo_drop('TABLE ' || tablename);
   
  SET tablename = project || '_InclusionAxioms';
  CALL combo_drop('TABLE ' || tablename);

  SET tablename = project || '_GeneratingRoles';
  CALL combo_drop('TABLE ' || tablename);

  SET tablename = project || '_GeneratingConcepts';
  CALL combo_drop('TABLE ' || tablename);

  SET tablename = project || '_QualifiedExistentials';
  CALL combo_drop('TABLE ' || tablename);

  SET tablename = project || '_ConceptAssertions';
  CALL combo_drop('TABLE ' || tablename);

  SET tablename = project || '_RoleAssertions';
  CALL combo_drop('TABLE ' || tablename);

  SET tablename = project || '_Symbols';
  CALL combo_drop('TABLE ' || tablename);

  EXECUTE IMMEDIATE 'DELETE FROM Projects WHERE name=''' || project || '''';
END
@
