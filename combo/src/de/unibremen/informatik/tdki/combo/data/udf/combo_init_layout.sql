CREATE OR REPLACE PROCEDURE combo_init_layout
  LANGUAGE SQL
BEGIN
  FOR p_row AS SELECT name FROM Projects 
  DO
    CALL combo_drop_project(p_row.name);
  END FOR;

  CALL combo_drop('TABLE Projects');
  EXECUTE IMMEDIATE 'CREATE TABLE Projects (name VARCHAR(20) NOT NULL PRIMARY KEY)';
END
@
