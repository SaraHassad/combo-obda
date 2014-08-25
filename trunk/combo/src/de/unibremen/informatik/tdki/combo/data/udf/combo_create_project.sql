CREATE OR REPLACE PROCEDURE combo_create_project
( 
  IN project VARCHAR(20),
  OUT project_exists INT
)
  LANGUAGE SQL
BEGIN
  DECLARE tablename VARCHAR(50);

  SET project_exists = 0;
  SELECT 1 INTO project_exists FROM Projects WHERE name=project;

  IF (project_exists = 0) THEN
    SET tablename = project || '_RoleInclusions';
    CALL combo_drop('TABLE ' || tablename);
    EXECUTE IMMEDIATE 'CREATE TABLE ' || tablename || ' (lhs integer NOT NULL, rhs integer NOT NULL)';
    EXECUTE IMMEDIATE 'CREATE INDEX ' || project || '_ri_lhs_rhs ON ' || tablename || ' (lhs, rhs)';
   
    SET tablename = project || '_InclusionAxioms';
    CALL combo_drop('TABLE ' || tablename);
    EXECUTE IMMEDIATE 'CREATE TABLE ' || tablename || ' (lhs integer NOT NULL, rhs integer NOT NULL)';
    EXECUTE IMMEDIATE 'CREATE INDEX ' || project || '_ia_lhs_rhs ON ' || tablename || ' (lhs, rhs)';

    SET tablename = project || '_GeneratingRoles';
    CALL combo_drop('TABLE ' || tablename);
    EXECUTE IMMEDIATE 'CREATE TABLE ' || tablename || ' (anonindv integer NOT NULL, role integer NOT NULL, lhs integer NOT NULL, rhs integer NOT NULL)';
    EXECUTE IMMEDIATE 'CREATE INDEX ' || project || '_genroles_anon_role_lhs_rhs ON ' || tablename || ' (anonindv, role, lhs, rhs)';

    SET tablename = project || '_GeneratingConcepts';
    CALL combo_drop('TABLE ' || tablename);
    EXECUTE IMMEDIATE 'CREATE TABLE ' || tablename || '  (concept integer NOT NULL, individual integer NOT NULL)';
    EXECUTE IMMEDIATE 'CREATE INDEX ' || project || '_genconcepts_concept_individual ON ' || tablename || ' (concept, individual)';

    SET tablename = project || '_QualifiedExistentials';
    CALL combo_drop('TABLE ' || tablename);
    EXECUTE IMMEDIATE 'CREATE TABLE ' || tablename || ' (newrole integer NOT NULL PRIMARY KEY, originalrole integer NOT NULL, conceptname integer NOT NULL)';

    SET tablename = project || '_ConceptAssertions';
    CALL combo_drop('TABLE ' || tablename);
    EXECUTE IMMEDIATE 'CREATE TABLE ' || tablename || ' (concept integer NOT NULL, individual integer NOT NULL)';
    EXECUTE IMMEDIATE 'CREATE INDEX ' || project || '_oca_concept_individual ON ' || tablename || ' (concept, individual)';

    SET tablename = project || '_RoleAssertions';
    CALL combo_drop('TABLE ' || tablename);
    EXECUTE IMMEDIATE 'CREATE TABLE ' || tablename || ' (role integer NOT NULL, lhs integer NOT NULL, rhs integer NOT NULL)';
    EXECUTE IMMEDIATE 'CREATE INDEX ' || project || '_ora_role_lhs_rhs ON ' || tablename || ' (role, lhs, rhs)';
    EXECUTE IMMEDIATE 'CREATE INDEX ' || project || '_ora_role_rhs_lhs ON ' || tablename || ' (role, rhs, lhs)';

    SET tablename = project || '_Symbols';
    CALL combo_drop('TABLE ' || tablename);
    EXECUTE IMMEDIATE 'CREATE TABLE ' || tablename || ' (name character varying(150) NOT NULL PRIMARY KEY, id integer NOT NULL)';
    EXECUTE IMMEDIATE 'CREATE INDEX ' || project || '_symbols_id_name ON ' || tablename || ' (id, name)';

    EXECUTE IMMEDIATE 'INSERT INTO Projects VALUES (''' || project || ''')';
  END IF;
END
@
