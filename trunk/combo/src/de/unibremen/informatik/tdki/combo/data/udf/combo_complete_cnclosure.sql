CREATE OR REPLACE PROCEDURE combo_complete_cnclosure
(
  IN project VARCHAR(20)
)
LANGUAGE SQL
BEGIN
  DECLARE RoleAssertions VARCHAR(50);
  DECLARE ConceptAssertions VARCHAR(50);
  DECLARE InclusionAxioms VARCHAR(50);

  DECLARE dsql CLOB(25000); 

  SET ConceptAssertions = project || '_ConceptAssertions';
  SET RoleAssertions = project || '_RoleAssertions';
  SET InclusionAxioms = project || '_InclusionAxioms';

  CALL combo_drop('TABLE workConceptAssertions');
  EXECUTE IMMEDIATE 'CREATE TABLE workConceptAssertions (concept integer, individual integer)';
  SET dsql = '
    WITH
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
    ),
    RoleInv (c0, c1) AS
    (
      SELECT 
	role, inv
      FROM 
	workRoleInv 
    ),
    InclusionAxioms (c0, c1) AS
    (
      SELECT 
	lhs, rhs
      FROM ' ||
	InclusionAxioms || '  
    ),
    ConceptNames (c0) AS
    (
      SELECT 
	concept
      FROM 
	workConceptNames 
    )
    SELECT
      t0.c2, t2.o
    FROM
      InclusionAxioms AS t0(c1,c2), ConceptNames AS t1(c2), ConceptAssertions AS t2(c1,o)
    WHERE
      t0.c1=t2.c1 AND t0.c2=t1.c2 AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  ConceptAssertions AS t(c2,o)
	WHERE
	  t.c2=t0.c2 AND t.o=t2.o
      )
    UNION
    SELECT
      t0.c, t2.o1
    FROM
      InclusionAxioms AS t0(r,c), ConceptNames AS t1(c), RoleAssertions AS t2(r,o1,o2)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  ConceptAssertions AS t(c,o1)
	WHERE
	  t.c=t0.c AND t.o1=t2.o1
      )
    UNION
    SELECT
      t0.c, t3.o2
    FROM
      InclusionAxioms AS t0(r2,c), ConceptNames AS t1(c), RoleInv AS t2(r1,r2), RoleAssertions AS t3(r1,o1,o2)
    WHERE
      t0.c=t1.c AND t2.r1=t3.r1 AND t0.r2=t2.r2+0 AND NOT EXISTS -- t2.r2+2 forces the planner to start from RoleInv and avoid a sort 
      (
	SELECT
	  1
	FROM
	  ConceptAssertions AS t(c,o2)
	WHERE
	  t.c=t0.c AND t.o2=t3.o2
      )';
  CALL combo_insert(dsql, 'workConceptAssertions');
  CALL combo_insert('SELECT * FROM workConceptAssertions', project || '_ConceptAssertions');
END
@
