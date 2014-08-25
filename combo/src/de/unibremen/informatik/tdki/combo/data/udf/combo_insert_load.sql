CREATE OR REPLACE PROCEDURE combo_insert
(
  IN query CLOB(25000),
  IN table VARCHAR(50)
)
LANGUAGE SQL	 
BEGIN
  CALL sysproc.admin_cmd('LOAD FROM (' || query || ') OF CURSOR INSERT INTO ' || table);
END
@
