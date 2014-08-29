CREATE OR REPLACE PROCEDURE combo_updatestats
( 
  IN tablename VARCHAR(50) 
)
LANGUAGE SQL
BEGIN
  DECLARE schemaname VARCHAR(1000);
  SELECT current_schema INTO schemaname FROM Sysibm.Sysdummy1;
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE ' || schemaname || '.' || tablename  || ' WITH DISTRIBUTION AND DETAILED INDEXES ALL');  
END
@
