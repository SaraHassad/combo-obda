CREATE OR REPLACE PROCEDURE combo_complete_data
(
  IN project VARCHAR(20)
)
LANGUAGE SQL
BEGIN
  -- completion queries generate much better execution plans with the following optimization level
  EXECUTE IMMEDIATE 'SET CURRENT QUERY OPTIMIZATION 0';

  CALL combo_complete_init_helpertables(project);
  CALL combo_complete_riclosure(project);
  CALL combo_complete_cnclosure(project);
  CALL combo_complete_firstlevel(project);
  CALL combo_complete_redundant(project);
  CALL combo_complete_stage3(project);
  CALL combo_complete_anonymous(project);
END
@
