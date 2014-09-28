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
  CALL combo_complete_anonymous(project);
  CALL combo_complete_stage3and4(project);
  CALL combo_complete_stage5(project);
  -- clean up intermediate tables
  CALL combo_drop('TABLE workConceptName');
  CALL combo_drop('TABLE workRoleInv');
  CALL combo_drop('TABLE workFirstLevel');
  CALL combo_drop('TABLE workRedundant');
  CALL combo_drop('TABLE workAnonymous');
  CALL combo_drop('TABLE ' || project || '_Stage1');
  CALL combo_drop('TABLE ' || project || '_Stage2');
END
@
