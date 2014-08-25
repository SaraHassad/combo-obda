CREATE OR REPLACE PROCEDURE combo_update_stats
( 
  IN project VARCHAR(20)
)
  LANGUAGE SQL
BEGIN
  DECLARE tablename VARCHAR(50);

  SET tablename = project || '_RoleInclusions';
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE ' || tablename  || ' WITH DISTRIBUTION AND DETAILED INDEXES ALL');  
   
  SET tablename = project || '_InclusionAxioms';
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE ' || tablename  || ' WITH DISTRIBUTION AND DETAILED INDEXES ALL');  
  
  SET tablename = project || '_GeneratingRoles';
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE ' || tablename  || ' WITH DISTRIBUTION AND DETAILED INDEXES ALL');  
  
  SET tablename = project || '_GeneratingConcepts';
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE ' || tablename  || ' WITH DISTRIBUTION AND DETAILED INDEXES ALL');  
  
  SET tablename = project || '_QualifiedExistentials';
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE ' || tablename  || ' WITH DISTRIBUTION AND DETAILED INDEXES ALL');  
  
  SET tablename = project || '_ConceptAssertions';
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE ' || tablename  || ' WITH DISTRIBUTION AND DETAILED INDEXES ALL');  
  
  SET tablename = project || '_RoleAssertions';
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE ' || tablename  || ' WITH DISTRIBUTION AND DETAILED INDEXES ALL');  
 
  SET tablename = project || '_Symbols';
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE ' || tablename  || ' WITH DISTRIBUTION AND DETAILED INDEXES ALL');  
END
@
