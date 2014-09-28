CREATE OR REPLACE PROCEDURE combo_complete_stage5
(
  IN project VARCHAR(20)
)
LANGUAGE SQL
BEGIN
  DECLARE Stage2 VARCHAR(50);
  DECLARE GeneratingConcepts VARCHAR(50);

  DECLARE dsql CLOB(25000); 

  SET Stage2 = project || '_Stage2';
  SET GeneratingConcepts = project || '_GeneratingConcepts';

  SET dsql = '
    WITH
    Anonymous (c0) AS
    (
      SELECT
  	individual
      FROM
  	workAnonymous
    ),
    Stage2 (c0, c1) AS
    (
      SELECT
        concept, individual
      FROM ' ||
        Stage2 || ' 
    ),
    GeneratingConcepts (c0, c1) AS
    (
      SELECT
        concept, individual
      FROM ' || 
        GeneratingConcepts || ' 
    )
    SELECT 
      *
    FROM
      Stage2
    UNION ALL
    SELECT
      t1.c, t0.r
    FROM
      Anonymous AS t0(r), GeneratingConcepts AS t1(c,r)
    WHERE
      t0.r=t1.r';

  -- write Stage2 and Stage5 to the main ConceptAssertions table
  CALL combo_insert(dsql, project || '_ConceptAssertions');
END
@
