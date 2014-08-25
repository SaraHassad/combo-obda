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

  CALL combo_drop('TABLE workRedInclusions');
  EXECUTE IMMEDIATE 'CREATE TABLE workRedInclusions (lhs integer, rhs integer)';
  EXECUTE IMMEDIATE 'CREATE INDEX redinc_lhs_rhs ON workRedInclusions (lhs, rhs)';
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
    )
    SELECT
      t1.c, t0.r
    FROM
      InclusionAxioms AS t1(c,s), 
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
      t0.s=t1.s
    UNION
    SELECT
      t1.c, t0.r
    FROM
      InclusionAxioms AS t1(c,s), 
      (
  	SELECT
  	  t0.r, t0.s
  	FROM
  	  RoleInclusions AS t0(r,s), RoleInclusions AS t1(s,r)
  	WHERE
  	  t0.r=t1.r AND t0.s=t1.s
      ) AS t0(s,r)
    WHERE
      t0.s=t1.s AND t0.s < t0.r
    UNION
    SELECT
      t2.d, t1.r2
    FROM
      QualifiedExistentials AS t0(r1,s,c), QualifiedExistentials AS t1(r2,s,c), InclusionAxioms AS t2(d,r1)
    WHERE
      t0.c=t1.c AND t0.r1=t2.r1 AND t0.s=t1.s AND t0.r1 < t1.r2
    UNION
    SELECT
      t5.d, t1.r2
    FROM
      QualifiedExistentials AS t0(r1,s,c1), QualifiedExistentials AS t1(r2,s,c2), RoleInv AS t2(r1,invR1), RoleInv AS t3(r2,invR2), InclusionAxioms AS t4(invR1,c2), InclusionAxioms AS t5(d,r1)
    WHERE
      t1.c2=t4.c2 AND t2.invR1=t4.invR1 AND t0.r1=t2.r1 AND t0.r1=t5.r1 AND t1.r2=t3.r2 AND t0.s=t1.s AND NOT EXISTS 
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
      t6.d, t1.r2
    FROM
      QualifiedExistentials AS t0(r1,s,c1), QualifiedExistentials AS t1(r2,s,c2), RoleInv AS t2(r1,invR1), RoleInv AS t3(r2,invR2), InclusionAxioms AS t4(invR1,c2), InclusionAxioms AS t5(invR2,c1), InclusionAxioms AS t6(d,r1)
    WHERE
      t0.c1=t5.c1 AND t1.c2=t4.c2 AND t2.invR1=t4.invR1 AND t3.invR2=t5.invR2 AND t0.r1=t2.r1 AND t0.r1=t6.r1 AND t1.r2=t3.r2 AND t0.s=t1.s AND t0.r1 < t1.r2
    UNION
    SELECT
      t3.d, t1.r2
    FROM
      QualifiedExistentials AS t0(r1,s1,c), QualifiedExistentials AS t1(r2,s2,c), InclusionAxioms AS t3(d,r1), 
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
      t0.c=t1.c AND t0.r1=t3.r1 AND t0.s1=t2.s1 AND t1.s2=t2.s2
    UNION
    SELECT
      t3.d, t1.r2
    FROM
      QualifiedExistentials AS t0(r1,s1,c), QualifiedExistentials AS t1(r2,s2,c), InclusionAxioms AS t3(d,r1), 
      (
  	SELECT
  	  t0.r, t0.s
  	FROM
  	  RoleInclusions AS t0(r,s), RoleInclusions AS t1(s,r)
  	WHERE
  	  t0.r=t1.r AND t0.s=t1.s
      ) AS t2(s1,s2)
    WHERE
      t0.c=t1.c AND t0.r1=t3.r1 AND t0.s1=t2.s1 AND t1.s2=t2.s2 AND t0.r1 < t1.r2
    UNION
    SELECT
      t5.d, t1.r2
    FROM
      QualifiedExistentials AS t0(r1,s1,c1), QualifiedExistentials AS t1(r2,s2,c2), RoleInv AS t3(r1,invR1), InclusionAxioms AS t4(invR1,c2), InclusionAxioms AS t5(d,r1), 
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
      t1.c2=t4.c2 AND t3.invR1=t4.invR1 AND t0.r1=t3.r1 AND t0.r1=t5.r1 AND t0.s1=t2.s1 AND t1.s2=t2.s2
    UNION
    SELECT
      t6.d, t1.r2
    FROM
      QualifiedExistentials AS t0(r1,s1,c1), QualifiedExistentials AS t1(r2,s2,c2), RoleInv AS t3(r1,invR1), RoleInv AS t4(r2,invR2), InclusionAxioms AS t5(invR1,c2), InclusionAxioms AS t6(d,r1), 
      (
  	SELECT
  	  t0.r, t0.s
  	FROM
  	  RoleInclusions AS t0(r,s), RoleInclusions AS t1(s,r)
  	WHERE
  	  t0.r=t1.r AND t0.s=t1.s
      ) AS t2(s1,s2)
    WHERE
      t1.c2=t5.c2 AND t3.invR1=t5.invR1 AND t0.r1=t3.r1 AND t0.r1=t6.r1 AND t1.r2=t4.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2 AND NOT EXISTS 
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
      t7.d, t1.r2
    FROM
      QualifiedExistentials AS t0(r1,s1,c1), QualifiedExistentials AS t1(r2,s2,c2), RoleInv AS t3(r1,invR1), RoleInv AS t4(r2,invR2), InclusionAxioms AS t5(invR1,c2), InclusionAxioms AS t6(invR2,c1), InclusionAxioms AS t7(d,r1), 
      (
  	SELECT
  	  t0.r, t0.s
  	FROM
  	  RoleInclusions AS t0(r,s), RoleInclusions AS t1(s,r)
  	WHERE
  	  t0.r=t1.r AND t0.s=t1.s
      ) AS t2(s1,s2)
    WHERE
      t0.c1=t6.c1 AND t1.c2=t5.c2 AND t3.invR1=t5.invR1 AND t4.invR2=t6.invR2 AND t0.r1=t3.r1 AND t0.r1=t7.r1 AND t1.r2=t4.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2 AND t0.r1 < t1.r2
    UNION
    SELECT
      t4.d, t0.r2
    FROM
      QualifiedExistentials AS t0(r2,s,c), RoleInv AS t2(r1,invR1), InclusionAxioms AS t3(invR1,c), InclusionAxioms AS t4(d,r1), 
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
      t0.c=t3.c AND t2.invR1=t3.invR1 AND t1.r1=t2.r1 AND t1.r1=t4.r1 AND t0.s=t1.s AND NOT EXISTS 
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
  CALL combo_insert(dsql, 'workRedInclusions');
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE workRedInclusions WITH DISTRIBUTION AND DETAILED INDEXES ALL');

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
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE workRIRhsRN WITH DISTRIBUTION AND DETAILED INDEXES ALL');
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
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE workRIRhsInv WITH DISTRIBUTION AND DETAILED INDEXES ALL');

  CALL combo_drop('TABLE workRoleAssertions');
  EXECUTE IMMEDIATE 'CREATE TABLE workRoleAssertions (role integer, lhs integer, rhs integer)';
  SET dsql = '
    WITH
    RoleInv (c0, c1) AS
    (
      SELECT 
  	role, inv
      FROM 
  	workRoleInv 
    ),
    RedInclusions (c0, c1) AS
    (
      SELECT
  	lhs, rhs
      FROM
  	workRedInclusions
    ),
    RoleInclusions (c0, c1) AS
    (
      SELECT
  	lhs, rhs
      FROM ' ||
  	RoleInclusions || ' 
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
    ),
    RIRhsRN (c0, c1) AS
    (
      SELECT
  	lhs, rhs
      FROM
  	workRIRhsRN
    ),
    RIRhsInv (c0, c1) AS
    (
      SELECT
  	lhs, rhs
      FROM
  	workRIRhsInv
    )
    SELECT
      t0.r, t0.o, t0.r
    FROM
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o, t0.c
  	    FROM
  	      ConceptAssertions AS t0(c,o)
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t0(o,r), 
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
      )
    UNION
    SELECT
      t0.r, t0.o, t0.r
    FROM
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o1, t0.r
  	    FROM
  	      RoleAssertions AS t0(r,o1,o2)
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t0(o,r), 
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
      )
    UNION
    SELECT
      t0.r, t0.o, t0.r
    FROM
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o2, t1.s
  	    FROM
  	      RoleAssertions AS t0(r,o1,o2), RoleInv AS t1(r,s)
  	    WHERE
  	      t0.r=t1.r
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t0(o,r), 
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
      )
    UNION
    SELECT
      t1.s, t0.r, t0.o
    FROM
      RoleInv AS t1(s,r), 
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o, t0.c
  	    FROM
  	      ConceptAssertions AS t0(c,o)
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t0(o,r)
    WHERE
      t0.r=t1.r
    UNION
    SELECT
      t1.s, t0.r, t0.o
    FROM
      RoleInv AS t1(s,r), 
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o1, t0.r
  	    FROM
  	      RoleAssertions AS t0(r,o1,o2)
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t0(o,r)
    WHERE
      t0.r=t1.r
    UNION
    SELECT
      t1.s, t0.r, t0.o
    FROM
      RoleInv AS t1(s,r), 
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o2, t1.s
  	    FROM
  	      RoleAssertions AS t0(r,o1,o2), RoleInv AS t1(r,s)
  	    WHERE
  	      t0.r=t1.r
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t0(o,r)
    WHERE
      t0.r=t1.r
    UNION
    SELECT
      t0.r2, t1.o, t0.r1
    FROM
      RIRhsRN AS t0(r1,r2), 
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o, t0.c
  	    FROM
  	      ConceptAssertions AS t0(c,o)
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t1(o,r1)
    WHERE
      t0.r1=t1.r1
    UNION
    SELECT
      t0.r2, t1.o, t0.r1
    FROM
      RIRhsRN AS t0(r1,r2), 
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o1, t0.r
  	    FROM
  	      RoleAssertions AS t0(r,o1,o2)
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t1(o,r1)
    WHERE
      t0.r1=t1.r1
    UNION
    SELECT
      t0.r2, t1.o, t0.r1
    FROM
      RIRhsRN AS t0(r1,r2), 
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o2, t1.s
  	    FROM
  	      RoleAssertions AS t0(r,o1,o2), RoleInv AS t1(r,s)
  	    WHERE
  	      t0.r=t1.r
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t1(o,r1)
    WHERE
      t0.r1=t1.r1
    UNION
    SELECT
      t0.r2, t0.r1, t1.o
    FROM
      RIRhsInv AS t0(r1,r2), 
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o, t0.c
  	    FROM
  	      ConceptAssertions AS t0(c,o)
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t1(o,r1)
    WHERE
      t0.r1=t1.r1
    UNION
    SELECT
      t0.r2, t0.r1, t1.o
    FROM
      RIRhsInv AS t0(r1,r2), 
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o1, t0.r
  	    FROM
  	      RoleAssertions AS t0(r,o1,o2)
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t1(o,r1)
    WHERE
      t0.r1=t1.r1
    UNION
    SELECT
      t0.r2, t0.r1, t1.o
    FROM
      RIRhsInv AS t0(r1,r2), 
      (
  	SELECT
  	  t1.o, t0.r
  	FROM
  	  InclusionAxioms AS t0(c,r), 
  	  (
  	    SELECT
  	      t0.o2, t1.s
  	    FROM
  	      RoleAssertions AS t0(r,o1,o2), RoleInv AS t1(r,s)
  	    WHERE
  	      t0.r=t1.r
  	  ) AS t1(o,c)
  	WHERE
  	  t0.c=t1.c AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      RedInclusions AS t(c,r)
  	    WHERE
  	      t.c=t0.c AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o1, t0.r
  		FROM
  		  RoleAssertions AS t0(r,o1,o2)
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t0.o2, t1.r
  		FROM
  		  RoleAssertions AS t0(s,o1,o2), RoleInv AS t1(s,r)
  		WHERE
  		  t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
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
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  ) AND NOT EXISTS 
  	  (
  	    SELECT
  	      1
  	    FROM
  	      (
  		SELECT
  		  t2.o2, t0.r
  		FROM
  		  QualifiedExistentials AS t0(r,s,c), RoleInv AS t1(p,s), RoleAssertions AS t2(p,o1,o2), ConceptAssertions AS t3(c,o1)
  		WHERE
  		  t0.c=t3.c AND t2.o1=t3.o1 AND t1.p=t2.p AND t0.s=t1.s
  	      ) AS t(o,r)
  	    WHERE
  	      t.o=t1.o AND t.r=t0.r
  	  )
      ) AS t1(o,r1)
    WHERE
      t0.r1=t1.r1';
  CALL combo_insert(dsql, 'workRoleAssertions');
  CALL combo_insert('SELECT * FROM workRoleAssertions', project || '_RoleAssertions');

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
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE workAnonymous WITH DISTRIBUTION AND DETAILED INDEXES ALL');

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
