CREATE OR REPLACE PROCEDURE combo_complete_firstlevel
(
  IN project VARCHAR(20)
)
LANGUAGE SQL
BEGIN
  DECLARE ConceptAssertions VARCHAR(50);
  DECLARE RoleAssertions VARCHAR(50);
  DECLARE InclusionAxioms VARCHAR(50);
  DECLARE QualifiedExistentials VARCHAR(50);

  DECLARE dsql CLOB(25000); 

  SET ConceptAssertions = project || '_ConceptAssertions';
  SET RoleAssertions = project || '_RoleAssertions';
  SET InclusionAxioms = project || '_InclusionAxioms';
  SET QualifiedExistentials = project || '_QualifiedExistentials';

  CALL combo_drop('TABLE workFirstLevel');
  EXECUTE IMMEDIATE 'CREATE TABLE workFirstLevel (individual integer, role integer)';
  EXECUTE IMMEDIATE 'CREATE INDEX fl_role_individual ON workFirstLevel (role, individual)';
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
    ConceptAssertions (c0, c1) AS
    (
      SELECT
	concept, individual
      FROM ' ||
	ConceptAssertions || ' 
    ),
    RoleAssertions (c0, c1, c2) AS
    (
      SELECT
	role, lhs, rhs
      FROM ' ||
	RoleAssertions || ' 
    )
    (
      SELECT
	t2.o, t0.r
      FROM
	InclusionAxioms AS t0(c,r), 
	(
	  SELECT
	    t0.r
	  FROM
	    RoleInv AS t0(r,s)
	) AS t1(r), 
	(
	  SELECT
	    t0.o, t0.c
	  FROM
	    ConceptAssertions AS t0(c,o)
	) AS t2(o,c)
      WHERE
	t0.c=t2.c AND t0.r=t1.r
      UNION ALL
      SELECT
	t2.o, t0.r
      FROM
	InclusionAxioms AS t0(c,r), 
	(
	  SELECT
	    t0.r
	  FROM
	    RoleInv AS t0(r,s)
	) AS t1(r), 
	(
	  SELECT
	    t0.o1, t0.r
	  FROM
	    RoleAssertions AS t0(r,o1,o2)
	) AS t2(o,c)
      WHERE
	t0.c=t2.c AND t0.r=t1.r
      UNION ALL
      SELECT
	t2.o, t0.r
      FROM
	InclusionAxioms AS t0(c,r), 
	(
	  SELECT
	    t0.r
	  FROM
	    RoleInv AS t0(r,s)
	) AS t1(r), 
	(
	  SELECT
	    t0.o2, t1.s
	  FROM
	    RoleAssertions AS t0(r,o1,o2), RoleInv AS t1(r,s)
	  WHERE
	    t0.r=t1.r
	) AS t2(o,c)
      WHERE
	t0.c=t2.c AND t0.r=t1.r
      UNION ALL
      SELECT
	t2.o, t0.r
      FROM
	InclusionAxioms AS t0(c,r), RoleInv AS t1(s,r), 
	(
	  SELECT
	    t0.o, t0.c
	  FROM
	    ConceptAssertions AS t0(c,o)
	) AS t2(o,c)
      WHERE
	t0.c=t2.c AND t0.r=t1.r
      UNION ALL
      SELECT
	t2.o, t0.r
      FROM
	InclusionAxioms AS t0(c,r), RoleInv AS t1(s,r), 
	(
	  SELECT
	    t0.o1, t0.r
	  FROM
	    RoleAssertions AS t0(r,o1,o2)
	) AS t2(o,c)
      WHERE
	t0.c=t2.c AND t0.r=t1.r
      UNION ALL
      SELECT
	t2.o, t0.r
      FROM
	InclusionAxioms AS t0(c,r), RoleInv AS t1(s,r), 
	(
	  SELECT
	    t0.o2, t1.s
	  FROM
	    RoleAssertions AS t0(r,o1,o2), RoleInv AS t1(r,s)
	  WHERE
	    t0.r=t1.r
	) AS t2(o,c)
      WHERE
	t0.c=t2.c AND t0.r=t1.r
    )
    EXCEPT
    (
      SELECT
	t0.o1, t0.r
      FROM
	RoleAssertions AS t0(r,o1,o2)
      UNION ALL
      SELECT
	t0.o2, t1.r
      FROM
	RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
      WHERE
	t0.s=t1.s
      UNION ALL
      SELECT
	t2.o1, t0.r
      FROM
	QualifiedExistentials AS t0(r,s,c), RoleAssertions AS t2(s,o1,o2), ConceptAssertions AS t3(c,o2), 
	(
	  SELECT
	    t0.r
	  FROM
	    RoleInv AS t0(r,s)
	) AS t1(s)
      WHERE
	t0.c=t3.c AND t2.o2=t3.o2 AND t0.s=t1.s AND t0.s=t2.s
      UNION ALL
      SELECT
	t2.o2, t0.r
      FROM
	QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
      WHERE
	t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
    )';
  CALL combo_insert(dsql, 'workFirstLevel');
END
@
