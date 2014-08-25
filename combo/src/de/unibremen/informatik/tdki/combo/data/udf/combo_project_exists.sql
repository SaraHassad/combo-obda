CREATE OR REPLACE PROCEDURE combo_project_exists
( 
  IN project VARCHAR(20),
  OUT project_exists INT
)
  LANGUAGE SQL
BEGIN
  SET project_exists = 0;
  SELECT 1 INTO project_exists FROM Projects WHERE name=project;
END
@
