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
  DECLARE Stage1 VARCHAR(50);
  -- the query is really long and concatenating one CLOB variable avoids 32K string limit
  DECLARE Stage2 CLOB(50);

  DECLARE dsql CLOB(35000); 

  SET ConceptAssertions = project || '_ConceptAssertions';
  SET RoleAssertions = project || '_RoleAssertions';
  SET InclusionAxioms = project || '_InclusionAxioms';
  SET QualifiedExistentials = project || '_QualifiedExistentials';
  SET Stage1 = project || '_Stage1';
  SET Stage2 = project || '_Stage2';

  CALL combo_drop('TABLE workFirstLevel');
  EXECUTE IMMEDIATE 'CREATE TABLE workFirstLevel (role integer, individual integer)';
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
    InclusionAxioms (c0, c1) AS
    (
      SELECT 
	lhs, rhs
      FROM ' ||
	InclusionAxioms || ' 
    ),
    QualifiedExistentials (c0, c1, c2) AS
    (
      SELECT 
	newrole, originalrole, conceptname
      FROM ' ||
	QualifiedExistentials || ' 
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
    Stage1 (c0, c1, c2) AS
    (
      SELECT 
	role, lhs, rhs
      FROM ' ||
	Stage1 || '
    ),
    Stage2 (c0, c1) AS
    (
      SELECT 
	concept, individual
      FROM ' ||
	Stage2 || '
    )
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), 
      (
	SELECT
	  t0.c, t0.o
	FROM
	  ConceptAssertions AS t0(c,o)
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t2(r)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
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
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), 
      (
	SELECT
	  t0.c, t0.o
	FROM
	  Stage2 AS t0(c,o)
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t2(r)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
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
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), 
      (
	SELECT
	  t0.r, t0.o1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      RoleAssertions AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t2(r)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
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
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), 
      (
	SELECT
	  t0.r, t0.o1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      Stage1 AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t2(r)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
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
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), 
      (
	SELECT
	  t1.s, t0.o2
	FROM
	  RoleInv AS t1(r,s), 
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      RoleAssertions AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
	WHERE
	  t0.r=t1.r
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t2(r)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
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
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), 
      (
	SELECT
	  t1.s, t0.o2
	FROM
	  RoleInv AS t1(r,s), 
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      Stage1 AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
	WHERE
	  t0.r=t1.r
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t2(r)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
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
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(r,o)
	WHERE
	  t.r=t0.r AND t.o=t1.o
      )' || '
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), RoleInv AS t2(s,r), 
      (
	SELECT
	  t0.c, t0.o
	FROM
	  ConceptAssertions AS t0(c,o)
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), RoleInv AS t2(s,r), 
      (
	SELECT
	  t0.c, t0.o
	FROM
	  Stage2 AS t0(c,o)
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), RoleInv AS t2(s,r), 
      (
	SELECT
	  t0.r, t0.o1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      RoleAssertions AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), RoleInv AS t2(s,r), 
      (
	SELECT
	  t0.r, t0.o1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      Stage1 AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), RoleInv AS t2(s,r), 
      (
	SELECT
	  t1.s, t0.o2
	FROM
	  RoleInv AS t1(r,s), 
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      RoleAssertions AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
	WHERE
	  t0.r=t1.r
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), RoleInv AS t2(s,r), 
      (
	SELECT
	  t1.s, t0.o2
	FROM
	  RoleInv AS t1(r,s), 
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      Stage1 AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
	WHERE
	  t0.r=t1.r
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2)
	  ) AS t(s,o)
	WHERE
	  t.s=t2.s AND t.o=t1.o
      )' || '
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), 
      (
	SELECT
	  t0.c, t0.o
	FROM
	  ConceptAssertions AS t0(c,o)
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t3(s)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), 
      (
	SELECT
	  t0.c, t0.o
	FROM
	  Stage2 AS t0(c,o)
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t3(s)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), 
      (
	SELECT
	  t0.r, t0.o1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      RoleAssertions AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t3(s)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), 
      (
	SELECT
	  t0.r, t0.o1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      Stage1 AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t3(s)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), 
      (
	SELECT
	  t1.s, t0.o2
	FROM
	  RoleInv AS t1(r,s), 
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      RoleAssertions AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
	WHERE
	  t0.r=t1.r
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t3(s)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), 
      (
	SELECT
	  t1.s, t0.o2
	FROM
	  RoleInv AS t1(r,s), 
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      Stage1 AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
	WHERE
	  t0.r=t1.r
      ) AS t1(c,o), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t3(s)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o1
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o2)
	    WHERE
	      t0.o2=t1.o2
	  ) AS t(s,d,o)
	WHERE
	  t.s=t2.s AND t.d=t2.d AND t.o=t1.o
      )' || '
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), RoleInv AS t3(t,s), 
      (
	SELECT
	  t0.c, t0.o
	FROM
	  ConceptAssertions AS t0(c,o)
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), RoleInv AS t3(t,s), 
      (
	SELECT
	  t0.c, t0.o
	FROM
	  Stage2 AS t0(c,o)
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), RoleInv AS t3(t,s), 
      (
	SELECT
	  t0.r, t0.o1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      RoleAssertions AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), RoleInv AS t3(t,s), 
      (
	SELECT
	  t0.r, t0.o1
	FROM
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      Stage1 AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), RoleInv AS t3(t,s), 
      (
	SELECT
	  t1.s, t0.o2
	FROM
	  RoleInv AS t1(r,s), 
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      RoleAssertions AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
	WHERE
	  t0.r=t1.r
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      )
    UNION
    SELECT
      t0.r, t1.o
    FROM
      InclusionAxioms AS t0(c,r), QualifiedExistentials AS t2(r,s,d), RoleInv AS t3(t,s), 
      (
	SELECT
	  t1.s, t0.o2
	FROM
	  RoleInv AS t1(r,s), 
	  (
	    SELECT
	      t0.r, t0.o1, t0.o2
	    FROM
	      Stage1 AS t0(r,o1,o2)
	  ) AS t0(r,o1,o2)
	WHERE
	  t0.r=t1.r
      ) AS t1(c,o)
    WHERE
      t0.c=t1.c AND t0.r=t2.r AND t2.s=t3.s AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  ConceptAssertions AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  RoleAssertions AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      ) AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  (
	    SELECT
	      t0.r, t1.c, t0.o2
	    FROM
	      (
		SELECT
		  t0.r, t0.o1, t0.o2
		FROM
		  Stage1 AS t0(r,o1,o2)
	      ) AS t0(r,o1,o2), 
	      (
		SELECT
		  t0.c, t0.o
		FROM
		  Stage2 AS t0(c,o)
	      ) AS t1(c,o1)
	    WHERE
	      t0.o1=t1.o1
	  ) AS t(t,d,o)
	WHERE
	  t.t=t3.t AND t.d=t2.d AND t.o=t1.o
      )';
  CALL combo_insert(dsql, 'workFirstLevel');
  CALL combo_updatestats('workFirstLevel');
END
@
