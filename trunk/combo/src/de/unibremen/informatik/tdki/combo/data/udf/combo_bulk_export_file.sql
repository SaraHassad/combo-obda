CREATE OR REPLACE PROCEDURE combo_bulk_export_file
(
  IN filename VARCHAR(1024),
  IN tablename VARCHAR(50)
)
LANGUAGE SQL	 
BEGIN
  CALL sysproc.admin_cmd('EXPORT TO ' || filename || ' OF DEL MODIFIED BY NOCHARDEL COLDELx09 SELECT * FROM ' || tablename);
END
@
