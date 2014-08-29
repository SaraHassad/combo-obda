CREATE OR REPLACE PROCEDURE combo_updatestats_project
( 
  IN project VARCHAR(20)
)
  LANGUAGE SQL
BEGIN
  DECLARE tablename VARCHAR(50);

  SET tablename = project || '_RoleInclusions';
  CALL combo_updatestats(tablename);  
   
  SET tablename = project || '_InclusionAxioms';
  CALL combo_updatestats(tablename);  
  
  SET tablename = project || '_GeneratingRoles';
  CALL combo_updatestats(tablename);  
  
  SET tablename = project || '_GeneratingConcepts';
  CALL combo_updatestats(tablename);  
  
  SET tablename = project || '_QualifiedExistentials';
  CALL combo_updatestats(tablename);  
  
  SET tablename = project || '_ConceptAssertions';
  CALL combo_updatestats(tablename);  
  
  SET tablename = project || '_RoleAssertions';
  CALL combo_updatestats(tablename);  
 
  SET tablename = project || '_Symbols';
  CALL combo_updatestats(tablename);  
END
@
