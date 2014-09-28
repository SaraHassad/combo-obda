CREATE OR REPLACE PROCEDURE combo_complete_stage3and4
(
  IN project VARCHAR(20)
)
LANGUAGE SQL
BEGIN
  DECLARE RoleInclusions VARCHAR(50);
  DECLARE InclusionAxioms VARCHAR(50);
  DECLARE QualifiedExistentials VARCHAR(50);
  DECLARE Stage1 VARCHAR(50);
  DECLARE GeneratingRoles VARCHAR(50);

  DECLARE dsql CLOB(25000); 

  SET RoleInclusions = project || '_RoleInclusions';
  SET InclusionAxioms = project || '_InclusionAxioms';
  SET QualifiedExistentials = project || '_QualifiedExistentials';
  SET Stage1 = project || '_Stage1';
  SET GeneratingRoles = project || '_GeneratingRoles';

  SET dsql = '
    WITH
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
    ),
    RoleInclusions (c0, c1) AS
    (
      SELECT 
	lhs, rhs
      FROM ' ||
	RoleInclusions || ' 
    ),
    GeneratingRoles (c0, c1, c2, c3) AS
    (
      SELECT 
	anonindv, role, lhs, rhs
      FROM ' ||
	GeneratingRoles || ' 
    ),
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
    ),
    Stage1 (c0, c1, c2) AS
    (
      SELECT
        role, lhs, rhs
      FROM ' ||
        Stage1 || ' 
    ),
    Anonymous (c0) AS
    (
      SELECT
  	individual
      FROM
  	workAnonymous
    )
    SELECT 
      *
    FROM
      Stage1
    UNION ALL
    SELECT
      t0.r, t0.o, t0.r
    FROM
      FirstLevel AS t0(o,r), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t1(r)
    WHERE
      t0.r=t1.r AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r1
	    FROM
	      QualifiedExistentials AS t0(r1,r2,c)
	  ) AS t(r)
	WHERE
	  t.r=t0.r
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  Redundant AS t(o,r)
	WHERE
	  t.o=t0.o AND t.r=t0.r
      )
    UNION ALL
    SELECT
      t1.s, t0.r, t0.o
    FROM
      FirstLevel AS t0(o,r), RoleInv AS t1(s,r)
    WHERE
      t0.r=t1.r AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  Redundant AS t(o,r)
	WHERE
	  t.o=t0.o AND t.r=t0.r
      )
    UNION ALL
    SELECT
      t1.r2, t0.o, t0.r1
    FROM
      FirstLevel AS t0(o,r1), RoleInclusions AS t1(r1,r2), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t2(r2)
    WHERE
      t0.r1=t1.r1 AND t1.r2=t2.r2 AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  Redundant AS t(o,r1)
	WHERE
	  t.o=t0.o AND t.r1=t0.r1
      )
    UNION ALL
    SELECT
      t2.r2, t0.r1, t0.o
    FROM
      FirstLevel AS t0(o,r1), RoleInclusions AS t1(r1,s), RoleInv AS t2(r2,s)
    WHERE
      t0.r1=t1.r1 AND t1.s=t2.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  Redundant AS t(o,r1)
	WHERE
	  t.o=t0.o AND t.r1=t0.r1
      )
    UNION ALL
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
    UNION ALL
    SELECT
      t2.t, t1.o2, t1.o1
    FROM
      Anonymous AS t0(r), GeneratingRoles AS t1(r,s,o1,o2), RoleInv AS t2(t,s)
    WHERE
      t0.r=t1.r AND t1.s=t2.s';

  -- write Stage1, Stage3, and Stage4 to the main RoleAssertions table
  CALL combo_insert(dsql, project || '_RoleAssertions');
END
@
