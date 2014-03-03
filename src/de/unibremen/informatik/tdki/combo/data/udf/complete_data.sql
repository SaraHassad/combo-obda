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

  -- The following are the completion queries we are going to use

  -- role inclusion completion query
  DECLARE complete_ri VARCHAR(1024); 

  -- concept inclusion completion query with concept names on the right
  DECLARE complete_ci_x_cn VARCHAR(1024);

  -- role assertions connecting named individuals to anonymous ones
  DECLARE complete_first_level VARCHAR(5500);

  --  query for selecting anonymous individuals
  DECLARE select_anonymous_individuals VARCHAR(255); 
 
  -- role assertions among anonymous individuals
  DECLARE select_anonymous_role VARCHAR(1024);

  -- concept assertions for anonymous individuals
  DECLARE select_anonymous_concept VARCHAR(1024);

  SET ConceptAssertions = project || '_ConceptAssertions';
  SET RoleAssertions = project || '_RoleAssertions';
  SET RoleInclusions = project || '_RoleInclusions';
  SET InclusionAxioms = project || '_InclusionAxioms';
  SET QualifiedExistentials = project || '_QualifiedExistentials';
  SET GeneratingRoles = project || '_GeneratingRoles';
  SET GeneratingConcepts = project || '_GeneratingConcepts';

  SET complete_ri = '
    WITH
    ri_rn_rn (role, lhs, rhs) AS (
      SELECT ri.rhs, ra.lhs, ra.rhs 
      FROM ' || RoleAssertions || ' ra, ' || RoleInclusions || ' ri 
      WHERE ra.role=ri.lhs AND BITAND(ri.rhs,10)=8
    ),
    ri_rn_inv (role, lhs, rhs) AS (
      SELECT BITXOR(ri.rhs,2), ra.rhs, ra.lhs 
      FROM ' || RoleAssertions || ' ra, ' || RoleInclusions || ' ri 
      WHERE ra.role=ri.lhs AND BITAND(ri.rhs,2)=2
    )
    (SELECT * FROM ri_rn_rn UNION SELECT * FROM ri_rn_inv) EXCEPT SELECT * FROM ' || RoleAssertions;

  SET complete_ci_x_cn = '
    WITH
    ci_cn_cn (concept, individual) AS (
    SELECT rhs, individual 
    FROM ' || ConceptAssertions || ', ' || InclusionAxioms || '
    WHERE concept=lhs AND BITAND(rhs,12)=0
    ),
    ci_rn_cn (concept, individual) AS (
    SELECT ia.rhs, ra.lhs 
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia 
    WHERE ra.role=ia.lhs AND BITAND(ia.rhs,12)=0
    ),
    ci_inv_cn (concept, individuals) AS (
    SELECT ia.rhs, ra.rhs 
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia 
    WHERE BITAND(ia.lhs,2)=2 AND ra.role=BITXOR(ia.lhs,2) AND BITAND(ia.rhs,12)=0
    )
    (SELECT * FROM ci_cn_cn UNION SELECT * FROM ci_rn_cn UNION SELECT * FROM ci_inv_cn) EXCEPT SELECT * FROM ' || ConceptAssertions;

  SET complete_first_level = '
    WITH
    ci_cn_rn (role, lhs) AS (
    SELECT ia.rhs, ca.individual
    FROM ' || ConceptAssertions || ' ca, ' || InclusionAxioms || ' ia 
    WHERE ca.concept=ia.lhs AND BITAND(ia.rhs,10)=8 AND
    ia.rhs NOT IN (SELECT newrole FROM ' || QualifiedExistentials || ')
    ),
    ci_rn_rn (role, lhs) AS (
    SELECT ia.rhs, ra.lhs
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia 
    WHERE ra.role=ia.lhs AND BITAND(ia.rhs,10)=8 AND
    ia.rhs NOT IN (SELECT newrole FROM ' || QualifiedExistentials || ')
    ),
    ci_inv_rn (role, lhs) AS (
    SELECT ia.rhs, ra.rhs
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia 
    WHERE BITAND(ia.lhs,2)=2 AND ra.role=BITXOR(ia.lhs,2) AND BITAND(ia.rhs,10)=8 AND
    ia.rhs NOT IN (SELECT newrole FROM ' || QualifiedExistentials || ')
    ),
    ci_cn_qrn (role, lhs, rhs) AS (
    SELECT qe.originalrole, ca.individual, qe.newrole
    FROM ' || ConceptAssertions || ' ca, ' || InclusionAxioms || ' ia, ' || QualifiedExistentials || ' qe
    WHERE ca.concept=ia.lhs AND ia.rhs=qe.newrole AND BITAND(qe.originalrole,10)=8 
    ),
    ci_rn_qrn (role, lhs, rhs) AS (
    SELECT qe.originalrole, ra.lhs, qe.newrole
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia, ' || QualifiedExistentials || ' qe 
    WHERE ra.role=ia.lhs AND ia.rhs=qe.newrole AND BITAND(qe.originalrole,10)=8
    ),
    ci_inv_qrn (role, lhs, rhs) AS (
    SELECT qe.originalrole, ra.rhs, qe.newrole
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia, ' || QualifiedExistentials || ' qe 
    WHERE BITAND(ia.lhs,2)=2 AND ra.role=BITXOR(ia.lhs,2) AND ia.rhs=qe.newrole AND BITAND(qe.originalrole,10)=8
    ),    
    first_level_urn (role, lhs) AS ((SELECT * FROM ci_cn_rn UNION SELECT * FROM ci_rn_rn UNION SELECT * FROM ci_inv_rn) EXCEPT SELECT role, lhs FROM ' || RoleAssertions || '),
    first_level_qrn (role, lhs, rhs) AS (
      (SELECT * FROM ci_cn_qrn UNION SELECT * FROM ci_rn_qrn UNION SELECT * FROM ci_inv_qrn) EXCEPT 
      SELECT qe.originalrole, ra.lhs, qe.newrole
      FROM ' || RoleAssertions || ' ra, ' || ConceptAssertions || ' ca, ' || QualifiedExistentials || ' qe 
      WHERE ra.role=qe.originalrole AND ra.rhs=ca.individual AND ca.concept=qe.conceptname
    ),
    ci_cn_inv (lhs, rhs) AS (    
    SELECT ia.rhs, ca.individual 
    FROM ' || ConceptAssertions || ' ca, ' || InclusionAxioms || ' ia 
    WHERE ca.concept=ia.lhs AND BITAND(ia.rhs,2)=2
    ),
    ci_rn_inv (lhs, rhs) AS (
    SELECT ia.rhs, ra.lhs 
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia 
    WHERE ra.role=ia.lhs AND BITAND(ia.rhs,2)=2
    ),
    ci_inv_inv (lhs, rhs) AS (
    SELECT ia.rhs, ra.rhs 
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia 
    WHERE BITAND(ia.lhs,2)=2 AND ra.role=BITXOR(ia.lhs,2) AND BITAND(ia.rhs,2)=2
    ),
    ci_cn_qinv (role, lhs, rhs) AS (
    SELECT BITXOR(qe.originalrole,2), qe.newrole, ca.individual 
    FROM ' || ConceptAssertions || ' ca, ' || InclusionAxioms || ' ia, ' || QualifiedExistentials || ' qe 
    WHERE ca.concept=ia.lhs AND ia.rhs=qe.newrole AND BITAND(qe.originalrole,2)=2
    ), 
    ci_rn_qinv (role, lhs, rhs) AS (
    SELECT BITXOR(qe.originalrole,2), qe.newrole, ra.lhs 
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia, ' || QualifiedExistentials || ' qe 
    WHERE ra.role=ia.lhs AND ia.rhs=qe.newrole AND BITAND(qe.originalrole,2)=2
    ),
    ci_inv_qinv (role, lhs, rhs) AS (
    SELECT BITXOR(qe.originalrole,2), qe.newrole, ra.rhs 
    FROM ' || RoleAssertions || ' ra, ' || InclusionAxioms || ' ia, ' || QualifiedExistentials || ' qe 
    WHERE BITAND(ia.lhs,2)=2 AND ra.role=BITXOR(ia.lhs,2) AND ia.rhs=qe.newrole AND BITAND(qe.originalrole,2)=2
    ),
    first_level_uinv (lhs, rhs) AS ((SELECT * FROM ci_cn_inv UNION SELECT * FROM ci_rn_inv UNION SELECT * FROM ci_inv_inv) EXCEPT SELECT BITXOR(role,2), rhs FROM ' || RoleAssertions || '),
    first_level_qinv (role, lhs, rhs) AS (
      (SELECT * FROM ci_cn_qinv UNION SELECT * FROM ci_rn_qinv UNION SELECT * FROM ci_inv_qinv) EXCEPT
      SELECT BITXOR(qe.originalrole,2), qe.newrole, ra.rhs 
      FROM ' || RoleAssertions || ' ra, ' || ConceptAssertions || ' ca, ' || QualifiedExistentials || ' qe 
      WHERE BITAND(qe.originalrole,2)=2 AND ra.role=BITXOR(qe.originalrole,2) AND ra.lhs=ca.individual AND ca.concept=qe.conceptname
    )    
    SELECT role, lhs, role FROM first_level_urn UNION 
    SELECT role, lhs, rhs FROM first_level_qrn UNION
    SELECT BITXOR(lhs,2), lhs, rhs FROM first_level_uinv UNION
    SELECT role, lhs, rhs FROM first_level_qinv';

  SET select_anonymous_individuals = 'SELECT lhs FROM ' || RoleAssertions || ' WHERE lhs > 0 UNION SELECT rhs FROM ' || RoleAssertions || ' WHERE rhs > 0';

  SET select_anonymous_role = '
    WITH
    Anonymous (individual) AS (' || select_anonymous_individuals || ')

    SELECT gr.role, gr.lhs, gr.rhs 
    FROM ' || GeneratingRoles || ' gr 
    WHERE gr.anonindv IN (SELECT individual FROM Anonymous) AND BITAND(gr.role,10)=8
    UNION
    SELECT BITXOR(gr.role,2), gr.rhs, gr.lhs 
    FROM ' || GeneratingRoles || ' gr 
    WHERE gr.anonindv IN (SELECT individual FROM Anonymous) AND BITAND(gr.role,2)=2';

  SET select_anonymous_concept = '
    WITH
    Anonymous (individual) AS (' || select_anonymous_individuals || ')

    SELECT concept, individual
    FROM ' || GeneratingConcepts || '
    WHERE individual IN (SELECT individual FROM Anonymous)';

  CALL insert_role_assertions(complete_ri, project);
  CALL insert_concept_assertions(complete_ci_x_cn, project);
  CALL insert_role_assertions(complete_first_level, project);
  CALL insert_role_assertions(complete_ri, project);
  CALL insert_role_assertions(select_anonymous_role, project);
  CALL insert_concept_assertions(select_anonymous_concept, project);
END
@
