CREATE OR REPLACE PROCEDURE combo_complete_data
(
  IN project VARCHAR(20)
)
LANGUAGE SQL
BEGIN
  -- The following are the tables we are going to use for completion
  DECLARE ConceptAssertions VARCHAR(50);
  DECLARE RoleAssertions VARCHAR(50);
  DECLARE RoleInclusions VARCHAR(50);
  DECLARE InclusionAxioms VARCHAR(50);
  DECLARE QualifiedExistentials VARCHAR(50);
  DECLARE GeneratingRoles VARCHAR(50);
  DECLARE GeneratingConcepts VARCHAR(50);

  -- variable for storing dynamic SQL statements
  -- there is a limit on max length of VARCHAR that depends on the page size
  -- in any case, it is strictly less than the page size, which we assume to be 4096 bytes
--  DECLARE dsql VARCHAR(3500); 
  DECLARE dsql CLOB(25000); 

  -- assign table names
  SET ConceptAssertions = project || '_ConceptAssertions';
  SET RoleAssertions = project || '_RoleAssertions';
  SET RoleInclusions = project || '_RoleInclusions';
  SET InclusionAxioms = project || '_InclusionAxioms';
  SET QualifiedExistentials = project || '_QualifiedExistentials';
  SET GeneratingRoles = project || '_GeneratingRoles';
  SET GeneratingConcepts = project || '_GeneratingConcepts';

  EXECUTE IMMEDIATE 'SET CURRENT QUERY OPTIMIZATION 0';

  CALL combo_complete_init_helpertables(project);
  CALL combo_complete_riclosure(project);
  CALL combo_complete_cnclosure(project);
  CALL combo_complete_firstlevel(project);
  CALL combo_complete_redundant(project);
  CALL combo_complete_stage3(project);

  CALL combo_drop('TABLE workRIRhsRN');
  EXECUTE IMMEDIATE 'CREATE TABLE workRIRhsRN (lhs integer, rhs integer)';
  EXECUTE IMMEDIATE 'CREATE INDEX rirhsrn_lhs_rhs ON workRIRhsRN (lhs, rhs)';
  SET dsql = ' 
    WITH
    RoleInv (c0, c1) AS
    (
      SELECT 
  	role, inv
      FROM 
  	workRoleInv 
    ),
    RoleInclusions (c0, c1) AS
    (
      SELECT
  	lhs, rhs
      FROM ' ||
  	RoleInclusions || ' 
    )
    SELECT
      t0.r, t0.s
    FROM
      RoleInclusions AS t0(r,s), 
      (
  	SELECT
  	  t0.r
  	FROM
  	  RoleInv AS t0(r,s)
      ) AS t1(s)
    WHERE
      t0.s=t1.s';
  CALL combo_insert(dsql, 'workRIRhsRN');
  CALL combo_updatestats('workRIRhsRN');
  CALL combo_drop('TABLE workRIRhsInv');
  EXECUTE IMMEDIATE 'CREATE TABLE workRIRhsInv (lhs integer, rhs integer)';
  EXECUTE IMMEDIATE 'CREATE INDEX rirhsinv_lhs_rhs ON workRIRhsInv (lhs, rhs)';
  SET dsql = '
    WITH
    RoleInv (c0, c1) AS
    (
      SELECT 
  	role, inv
      FROM 
  	workRoleInv 
    ),
    RoleInclusions (c0, c1) AS
    (
      SELECT
  	lhs, rhs
      FROM ' ||
  	RoleInclusions || ' 
    )
    SELECT
      t0.r, t1.t
    FROM
      RoleInclusions AS t0(r,s), RoleInv AS t1(t,s)
    WHERE
      t0.s=t1.s';
  CALL combo_insert(dsql, 'workRIRhsInv');
  CALL combo_updatestats('workRIRhsInv');

  CALL combo_drop('TABLE workAnonymous');
  EXECUTE IMMEDIATE 'CREATE TABLE workAnonymous (individual integer NOT NULL PRIMARY KEY)';
  SET dsql = '
    WITH
    RoleAssertions (c0, c1, c2) AS
    (
      SELECT
  	role, lhs, rhs
      FROM ' ||
  	RoleAssertions || ' 
    ),
    RoleInv (c0, c1) AS
    (
      SELECT
  	role, inv
      FROM
  	workRoleInv
    ),
    QualifiedExistentials (c0, c1, c2) AS
    (
      SELECT
  	newrole, originalrole, conceptname
      FROM ' ||
        QualifiedExistentials || ' 
    )
    SELECT
      t0.r1
    FROM
      RoleAssertions AS t0(r1,o,r2)
    WHERE
      t0.r1=t0.r2
    UNION
    SELECT
      t0.s
    FROM
      RoleAssertions AS t0(r,s,o), RoleInv AS t1(r,s)
    WHERE
      t0.r=t1.r AND t0.s=t1.s
    UNION
    SELECT
      t0.r
    FROM
      QualifiedExistentials AS t0(r,s,c), RoleAssertions AS t1(s,o,r)
    WHERE
      t0.r=t1.r AND t0.s=t1.s
    UNION
    SELECT
      t0.r
    FROM
      QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(t,s), RoleAssertions AS t2(t,r,o)
    WHERE
      t0.r=t2.r AND t0.s=t1.s AND t1.t=t2.t';
  CALL combo_insert(dsql, 'workAnonymous');
  CALL combo_updatestats('workAnonymous');

  SET dsql = '
    WITH
    RoleInv (c0, c1) AS
    (
      SELECT
  	role, inv
      FROM
  	workRoleInv
    ),
    GeneratingRoles (c0, c1, c2, c3) AS
    (
      SELECT
  	anonindv, role, lhs, rhs
      FROM ' ||
  	GeneratingRoles || ' 
    ),
    Anonymous (c0) AS
    (
      SELECT
  	individual
      FROM
  	workAnonymous
    )
    SELECT
      t1.s, t1.o1, t1.o2
    FROM
      Anonymous AS t0(r), GeneratingRoles AS t1(r,s,o1,o2), 
      (
  	SELECT
  	  t0.r
  	FROM
  	  RoleInv AS t0(r,s)
      ) AS t2(s)
    WHERE
      t0.r=t1.r AND t1.s=t2.s
    UNION
    SELECT
      t2.t, t1.o2, t1.o1
    FROM
      Anonymous AS t0(r), GeneratingRoles AS t1(r,s,o1,o2), RoleInv AS t2(t,s)
    WHERE
      t0.r=t1.r AND t1.s=t2.s';
  CALL combo_insert(dsql, project || '_RoleAssertions');

  SET dsql = '
    WITH
    GeneratingConcepts (c0, c1) AS
    (
      SELECT
  	concept, individual
      FROM ' ||
  	GeneratingConcepts || ' 
    ),
    Anonymous (c0) AS
    (
      SELECT
  	individual
      FROM
  	workAnonymous
    )
    SELECT
      t1.c, t1.r
    FROM
      Anonymous AS t0(r), GeneratingConcepts AS t1(c,r)
    WHERE
      t0.r=t1.r';
  CALL combo_insert(dsql, project || '_ConceptAssertions');
END
@
