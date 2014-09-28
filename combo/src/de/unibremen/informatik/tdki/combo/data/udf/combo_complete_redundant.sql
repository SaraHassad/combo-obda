CREATE OR REPLACE PROCEDURE combo_complete_redundant
(
  IN project VARCHAR(20)
)
LANGUAGE SQL
BEGIN
  DECLARE RoleInclusions VARCHAR(50);
  DECLARE InclusionAxioms VARCHAR(50);
  DECLARE QualifiedExistentials VARCHAR(50);

  DECLARE dsql CLOB(25000); 

  SET RoleInclusions = project || '_RoleInclusions';
  SET InclusionAxioms = project || '_InclusionAxioms';
  SET QualifiedExistentials = project || '_QualifiedExistentials';

  CALL combo_drop('TABLE workRedundant');
  EXECUTE IMMEDIATE 'CREATE TABLE workRedundant (role integer, individual integer)';
  EXECUTE IMMEDIATE 'CREATE INDEX red_role_individual ON workRedundant (role, individual)';
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
    InclusionAxioms (c0, c1) AS
    (
      SELECT 
	lhs, rhs
      FROM ' ||
	InclusionAxioms || ' 
    ),
    RoleInclusions (c0, c1) AS
    (
      SELECT 
	lhs, rhs
      FROM ' ||
	RoleInclusions || ' 
    ),
    FirstLevel (c0, c1) AS
    (
      SELECT
        role, individual
      FROM
        workFirstLevel
    )
    SELECT
      t0.r, t1.o
    FROM
      FirstLevel AS t1(s,o), FirstLevel AS t2(r,o), 
      (
	SELECT
	  t0.r, t0.s
	FROM
	  RoleInclusions AS t0(r,s)
	WHERE
	  NOT EXISTS 
	  (
	    SELECT
	      1
	    FROM
	      RoleInclusions AS t(s,r)
	    WHERE
	      t.s=t0.s AND t.r=t0.r
	  )
      ) AS t0(s,r)
    WHERE
      t1.o=t2.o AND t0.r=t2.r AND t0.s=t1.s
    UNION
    SELECT
      t0.r, t1.o
    FROM
      FirstLevel AS t1(s,o), FirstLevel AS t2(r,o), 
      (
	SELECT
	  t0.r, t0.s
	FROM
	  RoleInclusions AS t0(r,s), RoleInclusions AS t1(s,r)
	WHERE
	  t0.r=t1.r AND t0.s=t1.s
      ) AS t0(s,r)
    WHERE
      t1.o=t2.o AND t0.r=t2.r AND t0.s=t1.s AND t0.s < t0.r
    UNION
    SELECT
      t1.r2, t2.o
    FROM
      QualifiedExistentials AS t0(r1,s,c), QualifiedExistentials AS t1(r2,s,c), FirstLevel AS t2(r1,o), FirstLevel AS t3(r2,o)
    WHERE
      t0.c=t1.c AND t2.o=t3.o AND t0.r1=t2.r1 AND t1.r2=t3.r2 AND t0.s=t1.s AND t0.r1 < t1.r2
    UNION
    SELECT
      t1.r2, t5.o
    FROM
      QualifiedExistentials AS t0(r1,s,c1), QualifiedExistentials AS t1(r2,s,c2), RoleInv AS t2(r1,invR1), RoleInv AS t3(r2,invR2), InclusionAxioms AS t4(invR1,c2), FirstLevel AS t5(r1,o), FirstLevel AS t6(r2,o)
    WHERE
      t1.c2=t4.c2 AND t2.invR1=t4.invR1 AND t5.o=t6.o AND t0.r1=t2.r1 AND t0.r1=t5.r1 AND t1.r2=t3.r2 AND t1.r2=t6.r2 AND t0.s=t1.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  InclusionAxioms AS t(invR2,c1)
	WHERE
	  t.invR2=t3.invR2 AND t.c1=t0.c1
      )
    UNION
    SELECT
      t1.r2, t6.o
    FROM
      QualifiedExistentials AS t0(r1,s,c1), QualifiedExistentials AS t1(r2,s,c2), RoleInv AS t2(r1,invR1), RoleInv AS t3(r2,invR2), InclusionAxioms AS t4(invR1,c2), InclusionAxioms AS t5(invR2,c1), FirstLevel AS t6(r1,o), FirstLevel AS t7(r2,o)
    WHERE
      t0.c1=t5.c1 AND t1.c2=t4.c2 AND t2.invR1=t4.invR1 AND t3.invR2=t5.invR2 AND t6.o=t7.o AND t0.r1=t2.r1 AND t0.r1=t6.r1 AND t1.r2=t3.r2 AND t1.r2=t7.r2 AND t0.s=t1.s AND t0.r1 < t1.r2
    UNION
    SELECT
      t1.r2, t3.o
    FROM
      QualifiedExistentials AS t0(r1,s1,c), QualifiedExistentials AS t1(r2,s2,c), FirstLevel AS t3(r1,o), FirstLevel AS t4(r2,o), 
      (
	SELECT
	  t0.r, t0.s
	FROM
	  RoleInclusions AS t0(r,s)
	WHERE
	  NOT EXISTS 
	  (
	    SELECT
	      1
	    FROM
	      RoleInclusions AS t(s,r)
	    WHERE
	      t.s=t0.s AND t.r=t0.r
	  )
      ) AS t2(s1,s2)
    WHERE
      t0.c=t1.c AND t3.o=t4.o AND t0.r1=t3.r1 AND t1.r2=t4.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2
    UNION
    SELECT
      t1.r2, t3.o
    FROM
      QualifiedExistentials AS t0(r1,s1,c), QualifiedExistentials AS t1(r2,s2,c), FirstLevel AS t3(r1,o), FirstLevel AS t4(r2,o), 
      (
	SELECT
	  t0.r, t0.s
	FROM
	  RoleInclusions AS t0(r,s), RoleInclusions AS t1(s,r)
	WHERE
	  t0.r=t1.r AND t0.s=t1.s
      ) AS t2(s1,s2)
    WHERE
      t0.c=t1.c AND t3.o=t4.o AND t0.r1=t3.r1 AND t1.r2=t4.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2 AND t0.r1 < t1.r2
    UNION
    SELECT
      t1.r2, t5.o
    FROM
      QualifiedExistentials AS t0(r1,s1,c1), QualifiedExistentials AS t1(r2,s2,c2), RoleInv AS t3(r1,invR1), InclusionAxioms AS t4(invR1,c2), FirstLevel AS t5(r1,o), FirstLevel AS t6(r2,o), 
      (
	SELECT
	  t0.r, t0.s
	FROM
	  RoleInclusions AS t0(r,s)
	WHERE
	  NOT EXISTS 
	  (
	    SELECT
	      1
	    FROM
	      RoleInclusions AS t(s,r)
	    WHERE
	      t.s=t0.s AND t.r=t0.r
	  )
      ) AS t2(s1,s2)
    WHERE
      t1.c2=t4.c2 AND t3.invR1=t4.invR1 AND t5.o=t6.o AND t0.r1=t3.r1 AND t0.r1=t5.r1 AND t1.r2=t6.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2
    UNION
    SELECT
      t1.r2, t6.o
    FROM
      QualifiedExistentials AS t0(r1,s1,c1), QualifiedExistentials AS t1(r2,s2,c2), RoleInv AS t3(r1,invR1), RoleInv AS t4(r2,invR2), InclusionAxioms AS t5(invR1,c2), FirstLevel AS t6(r1,o), FirstLevel AS t7(r2,o), 
      (
	SELECT
	  t0.r, t0.s
	FROM
	  RoleInclusions AS t0(r,s), RoleInclusions AS t1(s,r)
	WHERE
	  t0.r=t1.r AND t0.s=t1.s
      ) AS t2(s1,s2)
    WHERE
      t1.c2=t5.c2 AND t3.invR1=t5.invR1 AND t6.o=t7.o AND t0.r1=t3.r1 AND t0.r1=t6.r1 AND t1.r2=t4.r2 AND t1.r2=t7.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2 AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  InclusionAxioms AS t(invR2,c1)
	WHERE
	  t.invR2=t4.invR2 AND t.c1=t0.c1
      )
    UNION
    SELECT
      t1.r2, t7.o
    FROM
      QualifiedExistentials AS t0(r1,s1,c1), QualifiedExistentials AS t1(r2,s2,c2), RoleInv AS t3(r1,invR1), RoleInv AS t4(r2,invR2), InclusionAxioms AS t5(invR1,c2), InclusionAxioms AS t6(invR2,c1), FirstLevel AS t7(r1,o), FirstLevel AS t8(r2,o), 
      (
	SELECT
	  t0.r, t0.s
	FROM
	  RoleInclusions AS t0(r,s), RoleInclusions AS t1(s,r)
	WHERE
	  t0.r=t1.r AND t0.s=t1.s
      ) AS t2(s1,s2)
    WHERE
      t0.c1=t6.c1 AND t1.c2=t5.c2 AND t3.invR1=t5.invR1 AND t4.invR2=t6.invR2 AND t7.o=t8.o AND t0.r1=t3.r1 AND t0.r1=t7.r1 AND t1.r2=t4.r2 AND t1.r2=t8.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2 AND t0.r1 < t1.r2
    UNION
    SELECT
      t0.r2, t4.o
    FROM
      QualifiedExistentials AS t0(r2,s,c), RoleInv AS t2(r1,invR1), InclusionAxioms AS t3(invR1,c), FirstLevel AS t4(r1,o), FirstLevel AS t5(r2,o), 
      (
	SELECT
	  t0.r, t0.s
	FROM
	  RoleInclusions AS t0(r,s)
	WHERE
	  NOT EXISTS 
	  (
	    SELECT
	      1
	    FROM
	      RoleInclusions AS t(s,r)
	    WHERE
	      t.s=t0.s AND t.r=t0.r
	  )
      ) AS t1(r1,s)
    WHERE
      t0.c=t3.c AND t2.invR1=t3.invR1 AND t4.o=t5.o AND t1.r1=t2.r1 AND t1.r1=t4.r1 AND t0.r2=t5.r2 AND t0.s=t1.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r1
	    FROM
	      QualifiedExistentials AS t0(r1,r2,c)
	  ) AS t(r1)
	WHERE
	  t.r1=t1.r1
      )';
  CALL combo_insert(dsql, 'workRedundant');
  CALL combo_updatestats('workRedundant');
END
@
