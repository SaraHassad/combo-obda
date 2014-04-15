-- anonymous individual check: column > 0
-- concept name check: BITAND(column,12)=0
-- role name check: BITAND(column,10)=8
-- inverse role check: BITAND(column,2)=2
-- convert inverse role to role name: BITXOR(column,2)
-- convert role name to inverse role: BITXOR(column,2)
CREATE PROCEDURE complete_data
(IN project VARCHAR(20))
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
  -- there is a limit on max length of VARCHAR that depends on page size
  -- in any case, it is strictly less than the page size, which we assume to be 4096 bytes
--  DECLARE dsql VARCHAR(3500); 
  DECLARE dsql CLOB(10500); 

  -- assign table names
  SET ConceptAssertions = project || '_ConceptAssertions';
  SET RoleAssertions = project || '_RoleAssertions';
  SET RoleInclusions = project || '_RoleInclusions';
  SET InclusionAxioms = project || '_InclusionAxioms';
  SET QualifiedExistentials = project || '_QualifiedExistentials';
  SET GeneratingRoles = project || '_GeneratingRoles';
  SET GeneratingConcepts = project || '_GeneratingConcepts';

  -- role inclusion completion query
  SET dsql = '
    WITH
    ri_rn_rn (role, lhs, rhs) AS (
      SELECT ri.rhs, ra.lhs, ra.rhs 
      FROM ' || RoleAssertions || ' ra, ' || RoleInclusions || ' ri 
      WHERE BITAND(ri.lhs,10)=8 AND BITAND(ri.rhs,10)=8 AND ra.role=ri.lhs 
    ),
    ri_rn_inv (role, lhs, rhs) AS (
      SELECT BITXOR(ri.rhs,2), ra.rhs, ra.lhs 
      FROM ' || RoleAssertions || ' ra, ' || RoleInclusions || ' ri 
      WHERE BITAND(ri.lhs,10)=8 AND BITAND(ri.rhs,2)=2 AND ra.role=ri.lhs
    )
    (SELECT * FROM ri_rn_rn UNION SELECT * FROM ri_rn_inv) EXCEPT SELECT * FROM ' || RoleAssertions;

  DECLARE GLOBAL TEMPORARY TABLE Stage1 (role integer, lhs integer, rhs integer) ON COMMIT PRESERVE ROWS NOT LOGGED WITH REPLACE;
  EXECUTE IMMEDIATE 'INSERT INTO Session.Stage1 ' || dsql;
  CALL insert_role_assertions('SELECT * FROM Session.Stage1', project);
  -- Do not drop this temporary table yet, we will use it
  -- DROP TABLE Session.Stage1;

  -- concept inclusion completion query with concept names on the right
  SET dsql = '
    WITH
    ci_cn_cn (concept, individual) AS (
    SELECT rhs, individual 
    FROM ' || ConceptAssertions || ', ' || InclusionAxioms || '
    WHERE BITAND(lhs,12)=0 AND BITAND(rhs,12)=0 AND concept=lhs
    ),
    ci_rn_cn (concept, individual) AS (
    SELECT ia.rhs, ra.lhs 
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia 
    WHERE BITAND(ia.lhs,10)=8 AND BITAND(ia.rhs,12)=0 AND ra.role=ia.lhs
    ),
    ci_inv_cn (concept, individuals) AS (
    SELECT ia.rhs, ra.rhs 
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia 
    WHERE BITAND(ia.lhs,2)=2 AND BITAND(ia.rhs,12)=0 AND ra.role=BITXOR(ia.lhs,2)
    )
    (SELECT * FROM ci_cn_cn UNION SELECT * FROM ci_rn_cn UNION SELECT * FROM ci_inv_cn) EXCEPT SELECT * FROM ' || ConceptAssertions;

  DECLARE GLOBAL TEMPORARY TABLE Stage2 (concept integer, individual integer) ON COMMIT PRESERVE ROWS NOT LOGGED WITH REPLACE;
  EXECUTE IMMEDIATE 'INSERT INTO Session.Stage2 ' || dsql;
  CALL insert_concept_assertions('SELECT * FROM Session.Stage2', project);
  -- Do not drop this temporary table yet, we will use it
  -- DROP TABLE Session.Stage2;

  -- Pairs of the form (o,c_r), where o is a named individual, c_r anonymous, and o requires an r-successor
  -- There may be a lot of redundancies, e.g., o might already have an r-successor
  -- Nevertheless, we need to materialize some of these pairs in order to be able to eliminate redundancies efficiently later on
  SET dsql = '
    WITH
    hasOutgoingRN(c0, c1) AS 
    (
      SELECT
        t0.o1, t0.r
      FROM ' ||
        RoleAssertions || ' AS t0(r,o1,o2)
    ),
    hasOutgoingInv(c0, c1) AS 
    (
      SELECT
        t0.o2, BITXOR(t0.r,2)
      FROM ' ||
        RoleAssertions || ' AS t0(r,o1,o2)
    ),
    FirstLevel(c0, c1) AS 
    (
      SELECT
        t1.o, t0.r
      FROM ' ||
        InclusionAxioms || ' AS t0(c,r), ' || ConceptAssertions || ' AS t1(c,o)
      WHERE
        t0.c=t1.c AND BITAND(t0.c,12)=0 AND BITAND(t0.r,10)=8
      UNION
      SELECT
        t1.o, t0.r2
      FROM ' ||
        InclusionAxioms || ' AS t0(r1,r2), hasOutgoingRN AS t1(o,r1)
      WHERE
        t0.r1=t1.r1 AND BITAND(t0.r1,10)=8 AND BITAND(t0.r2,10)=8
      UNION
      SELECT
        t1.o, t0.r2
      FROM ' ||
        InclusionAxioms || ' AS t0(r1,r2), hasOutgoingInv AS t1(o,r1)
      WHERE
        t0.r1=t1.r1 AND BITAND(t0.r1,2)=2 AND BITAND(t0.r2,10)=8
      UNION
      SELECT
        t1.o, t0.r
      FROM ' || 
        InclusionAxioms || ' AS t0(c,r), ' || ConceptAssertions || ' AS t1(c,o)
      WHERE
        t0.c=t1.c AND BITAND(t0.c,12)=0 AND BITAND(t0.r,2)=2
      UNION
      SELECT
        t1.o, t0.r2
      FROM ' ||
        InclusionAxioms || ' AS t0(r1,r2), hasOutgoingRN AS t1(o,r1)
      WHERE
        t0.r1=t1.r1 AND BITAND(t0.r1,10)=8 AND BITAND(t0.r2,2)=2
      UNION
      SELECT
        t1.o, t0.r2
      FROM ' ||
        InclusionAxioms || ' AS t0(r1,r2), hasOutgoingInv AS t1(o,r1)
      WHERE
        t0.r1=t1.r1 AND BITAND(t0.r1,2)=2 AND BITAND(t0.r2,2)=2
    ),
    hasOutgoingRNQ(c0, c1, c2) AS 
    (
      SELECT
        t0.o1, t0.r, t1.c
      FROM ' ||
        RoleAssertions || ' AS t0(r,o1,o2), ' || ConceptAssertions || ' AS t1(c,o2)
      WHERE
        t0.o2=t1.o2
    ),
    hasOutgoingInvQ(c0, c1, c2) AS 
    (
      SELECT
        t0.o2, BITXOR(t0.r,2), t1.c
      FROM ' ||
        RoleAssertions || ' AS t0(r,o1,o2), ' || ConceptAssertions || ' AS t1(c,o1)
      WHERE
        t0.o1=t1.o1
    ),
    RedundantFLReasonABox(c0, c1) AS 
    (
      SELECT
        t0.o, t0.r
      FROM
        hasOutgoingRN AS t0(o,r)
      UNION
      SELECT
        t0.o, t0.r
      FROM
        hasOutgoingInv AS t0(o,r)
      UNION
      SELECT
        t1.o, t0.r
      FROM ' ||
        QualifiedExistentials || ' AS t0(r,s,c), hasOutgoingRNQ AS t1(o,s,c)
      WHERE
        t0.c=t1.c AND t0.s=t1.s AND BITAND(t0.s,10)=8
      UNION
      SELECT
        t1.o, t0.r
      FROM ' ||
        QualifiedExistentials || ' AS t0(r,s,c), hasOutgoingInvQ AS t1(o,s,c)
      WHERE
        t0.c=t1.c AND t0.s=t1.s AND BITAND(t0.s,2)=2
    )
    SELECT * FROM FirstLevel EXCEPT SELECT * FROM RedundantFLReasonABox'; 

  DECLARE GLOBAL TEMPORARY TABLE Stage3 (individual integer, anonymous integer) ON COMMIT PRESERVE ROWS NOT LOGGED WITH REPLACE;
  CREATE INDEX Session.Stage3_ind_anon ON Session.Stage3
  (
    individual, anonymous
  );
  EXECUTE IMMEDIATE 'INSERT INTO Session.Stage3 ' || dsql;
  
  SET dsql = '  
    WITH
    SubRoleNotEq(c0, c1) AS 
    (
      SELECT
        t0.r, t0.s
      FROM ' ||
        RoleInclusions || ' AS t0(r,s)
      WHERE
        NOT EXISTS 
        (
          SELECT
            1
          FROM ' ||
            RoleInclusions || ' AS t(s,r)
          WHERE
            t.s=t0.s AND t.r=t0.r
        )
    ),
    EqRole(c0, c1) AS 
    (
      SELECT
        t0.r, t0.s
      FROM ' ||
        RoleInclusions || ' AS t0(r,s), ' || RoleInclusions || ' AS t1(s,r)
      WHERE
        t0.r=t1.r AND t0.s=t1.s
    ),
    SubCNNotEq(c0, c1) AS 
    (
      SELECT
        t0.c, t0.d
      FROM ' ||
        InclusionAxioms || ' AS t0(c,d)
      WHERE
        NOT EXISTS 
        (
          SELECT
            1
          FROM ' ||
            InclusionAxioms || ' AS t(d,c)
          WHERE
            t.d=t0.d AND t.c=t0.c
        ) AND BITAND(t0.c,12)=0 AND BITAND(t0.d,12)=0
    ),
    EqCN(c0, c1) AS 
    (
      SELECT
        t0.c, t0.d
      FROM ' ||
        InclusionAxioms || ' AS t0(c,d), ' || InclusionAxioms || ' AS t1(d,c)
      WHERE
        t0.c=t1.c AND t0.d=t1.d AND BITAND(t0.c,12)=0 AND BITAND(t0.d,12)=0
    ),
    RedundantFLReasonTBox(c0, c1) AS 
    (
      SELECT
        t1.o, t0.s
      FROM
        SubRoleNotEq AS t0(r,s), Session.Stage3 AS t1(o,r), Session.Stage3 AS t2(o,s)
      WHERE
        t1.o=t2.o AND t0.r=t1.r AND t0.s=t2.s 
      UNION
      SELECT
        t1.o, t0.s
      FROM
        EqRole AS t0(r,s), Session.Stage3 AS t1(o,r), Session.Stage3 AS t2(o,s)
      WHERE
        t1.o=t2.o AND t0.r=t1.r AND t0.s=t2.s AND t0.r < t0.s 
      UNION
      SELECT
        t2.o, t1.r2
      FROM ' ||
        QualifiedExistentials || ' AS t0(r1,s,c), ' || QualifiedExistentials || ' AS t1(r2,s,c), Session.Stage3 AS t2(o,r1), Session.Stage3 AS t3(o,r2)
      WHERE
        t0.c=t1.c AND t2.o=t3.o AND t0.r1=t2.r1 AND t1.r2=t3.r2 AND t0.s=t1.s AND t0.r1 < t1.r2 
      UNION
      SELECT
        t3.o, t1.r2
      FROM ' ||
        QualifiedExistentials || ' AS t0(r1,s,c1), ' || QualifiedExistentials || ' AS t1(r2,s,c2), SubCNNotEq AS t2(c1,c2), Session.Stage3 AS t3(o,r1), Session.Stage3 AS t4(o,r2)
      WHERE
        t0.c1=t2.c1 AND t1.c2=t2.c2 AND t3.o=t4.o AND t0.r1=t3.r1 AND t1.r2=t4.r2 AND t0.s=t1.s 
      UNION
      SELECT
        t3.o, t1.r2
      FROM ' ||
        QualifiedExistentials || ' AS t0(r1,s,c1), ' || QualifiedExistentials || ' AS t1(r2,s,c2), EqCN AS t2(c1,c2), Session.Stage3 AS t3(o,r1), Session.Stage3 AS t4(o,r2)
      WHERE
        t0.c1=t2.c1 AND t1.c2=t2.c2 AND t3.o=t4.o AND t0.r1=t3.r1 AND t1.r2=t4.r2 AND t0.s=t1.s AND t0.r1 < t1.r2 
      UNION
      SELECT
        t3.o, t1.r2
      FROM ' ||
        QualifiedExistentials || ' AS t0(r1,s1,c), ' || QualifiedExistentials || ' AS t1(r2,s2,c), SubRoleNotEq AS t2(s1,s2), Session.Stage3 AS t3(o,r1), Session.Stage3 AS t4(o,r2)
      WHERE
        t0.c=t1.c AND t3.o=t4.o AND t0.r1=t3.r1 AND t1.r2=t4.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2 
      UNION
      SELECT
        t3.o, t1.r2
      FROM ' ||
        QualifiedExistentials || ' AS t0(r1,s1,c), ' || QualifiedExistentials || ' AS t1(r2,s2,c), EqRole AS t2(s1,s2), Session.Stage3 AS t3(o,r1), Session.Stage3 AS t4(o,r2)
      WHERE
        t0.c=t1.c AND t3.o=t4.o AND t0.r1=t3.r1 AND t1.r2=t4.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2 AND t0.r1 < t1.r2	
      UNION
      SELECT
        t4.o, t1.r2
      FROM ' ||
        QualifiedExistentials || ' AS t0(r1,s1,c1), ' || QualifiedExistentials || ' AS t1(r2,s2,c2), SubRoleNotEq AS t2(s1,s2), SubCNNotEq AS t3(c1,c2), Session.Stage3 AS t4(o,r1), Session.Stage3 AS t5(o,r2)
      WHERE
        t0.c1=t3.c1 AND t1.c2=t3.c2 AND t4.o=t5.o AND t0.r1=t4.r1 AND t1.r2=t5.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2
      UNION
      SELECT
        t4.o, t1.r2
      FROM ' ||
        QualifiedExistentials || ' AS t0(r1,s1,c1), ' || QualifiedExistentials || ' AS t1(r2,s2,c2), SubRoleNotEq AS t2(s1,s2), EqCN AS t3(c1,c2), Session.Stage3 AS t4(o,r1), Session.Stage3 AS t5(o,r2)
      WHERE
        t0.c1=t3.c1 AND t1.c2=t3.c2 AND t4.o=t5.o AND t0.r1=t4.r1 AND t1.r2=t5.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2
      UNION
      SELECT
        t4.o, t1.r2
      FROM ' ||
        QualifiedExistentials || ' AS t0(r1,s1,c1), ' || QualifiedExistentials || ' AS t1(r2,s2,c2), EqRole AS t2(s1,s2), SubCNNotEq AS t3(c1,c2), Session.Stage3 AS t4(o,r1), Session.Stage3 AS t5(o,r2)
      WHERE
        t0.c1=t3.c1 AND t1.c2=t3.c2 AND t4.o=t5.o AND t0.r1=t4.r1 AND t1.r2=t5.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2
      UNION
      SELECT
        t4.o, t1.r2
      FROM ' ||
        QualifiedExistentials || ' AS t0(r1,s1,c1), ' || QualifiedExistentials || ' AS t1(r2,s2,c2), EqRole AS t2(s1,s2), EqCN AS t3(c1,c2), Session.Stage3 AS t4(o,r1), Session.Stage3 AS t5(o,r2)
      WHERE
        t0.c1=t3.c1 AND t1.c2=t3.c2 AND t4.o=t5.o AND t0.r1=t4.r1 AND t1.r2=t5.r2 AND t0.s1=t2.s1 AND t1.s2=t2.s2 AND t0.r1 < t1.r2
    ),
    Stage4(c0, c1) AS 
    (
      SELECT
        t0.o, t0.r
      FROM
        Session.Stage3 AS t0(o,r)
      EXCEPT
      SELECT
        t.o, t.r
      FROM RedundantFLReasonTBox AS t(o,r)
    ),
    isNewRole(c0) AS 
    (
      SELECT
        t0.r1
      FROM ' ||
        QualifiedExistentials || ' AS t0(r1,r2,c)
    )
    SELECT
      t0.r, t0.o, t0.r
    FROM
      Stage4 AS t0(o,r)
    WHERE
      NOT EXISTS 
      (
        SELECT
          1
        FROM
          isNewRole AS t(r)
        WHERE
          t.r=t0.r
      ) AND BITAND(t0.r,10)=8
    UNION
    SELECT
      BITXOR(t0.r,2), t0.r, t0.o
    FROM
      Stage4 AS t0(o,r)
    WHERE
      BITAND(t0.r,2)=2
    UNION
    SELECT
      t0.r2, t1.o, t0.r1
    FROM ' || 
      RoleInclusions || ' AS t0(r1,r2), Stage4 AS t1(o,r1)
    WHERE
      t0.r1=t1.r1 AND BITAND(t0.r2,10)=8
    UNION
    SELECT
      BITXOR(t0.r2,2), t0.r1, t1.o
    FROM ' ||
      RoleInclusions || ' AS t0(r1,r2), Stage4 AS t1(o,r1)
    WHERE
      t0.r1=t1.r1 AND BITAND(t0.r2,2)=2';

  CALL insert_role_assertions(dsql, project);
  DROP TABLE Session.Stage3;

  SET dsql = '                                                                                                                                                                                                              
    WITH
    Anonymous (individual) AS 
    (
      SELECT 
        lhs 
      FROM ' || 
        RoleAssertions || ' 
      WHERE lhs > 0 
      UNION 
      SELECT 
        rhs 
      FROM ' || 
        RoleAssertions || ' 
      WHERE rhs > 0
    )                                                                                                                                                                       
    SELECT 
      gr.role, gr.lhs, gr.rhs                                                                                                                                                                                                           
    FROM ' || 
      GeneratingRoles || ' gr                                                                                                                                                                                                        
    WHERE 
      gr.anonindv IN (SELECT individual FROM Anonymous) AND BITAND(gr.role,10)=8                                                                                                                                                     
    UNION
    SELECT 
      BITXOR(gr.role,2), gr.rhs, gr.lhs                                                                                                                                                                                                 
    FROM ' || GeneratingRoles || ' gr   
    WHERE 
      gr.anonindv IN (SELECT individual FROM Anonymous) AND BITAND(gr.role,2)=2';

  EXECUTE IMMEDIATE 'INSERT INTO Session.Stage1 ' || dsql;
  CALL insert_role_assertions('SELECT * FROM Session.Stage1', project);
  DROP TABLE Session.Stage1;

  SET dsql = '                                                                                                                                                                                                              
    WITH
    Anonymous (individual) AS 
    (
      SELECT 
        lhs 
      FROM ' || 
        RoleAssertions || ' 
      WHERE lhs > 0 
      UNION 
      SELECT 
        rhs 
      FROM ' || 
        RoleAssertions || ' 
      WHERE rhs > 0
    )
    SELECT 
      concept, individual
    FROM ' || 
      GeneratingConcepts || '                                                                                                                                                                                                        
    WHERE 
      individual IN (SELECT individual FROM Anonymous)';

  EXECUTE IMMEDIATE 'INSERT INTO Session.Stage2 ' || dsql;
  CALL insert_concept_assertions('SELECT * FROM Session.Stage2', project);
  DROP TABLE Session.Stage2;
END
@
