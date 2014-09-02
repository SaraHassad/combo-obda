CREATE OR REPLACE PROCEDURE combo_complete_anonymous
(
  IN project VARCHAR(20)
)
LANGUAGE SQL
BEGIN
  DECLARE GeneratingConcepts VARCHAR(50);
  DECLARE GeneratingRoles VARCHAR(50);

  DECLARE dsql CLOB(25000);

  SET GeneratingConcepts = project || '_GeneratingConcepts';
  SET GeneratingRoles = project || '_GeneratingRoles';

  CALL combo_drop('TABLE workAnonymous');
  EXECUTE IMMEDIATE 'CREATE TABLE workAnonymous (individual integer NOT NULL PRIMARY KEY)';
  SET dsql = '
    INSERT INTO workAnonymous
    WITH
    FirstLevel (c0, c1) AS
    (
      SELECT
	individual, role
      FROM
	workFirstLevel
    ),
    Redundant (c0, c1) AS
    (
      SELECT
	individual, role
      FROM
	workRedundant
    )
    SELECT DISTINCT
      t0.r
    FROM
      FirstLevel AS t0(o,r)
    WHERE
      NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  Redundant AS t(o,r)
	WHERE
	  t.o=t0.o AND t.r=t0.r
      )';
  EXECUTE IMMEDIATE dsql;
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