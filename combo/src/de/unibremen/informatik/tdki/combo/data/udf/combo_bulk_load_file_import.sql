CREATE OR REPLACE PROCEDURE combo_bulk_load_file
(
  IN filename VARCHAR(1024),
  IN tablename VARCHAR(50)
)
LANGUAGE SQL	 
BEGIN
  CALL sysproc.admin_cmd('IMPORT FROM ' || filename || ' OF DEL MODIFIED BY NOCHARDEL COLDELx09 REPLACE INTO ' || tablename);
END
@
