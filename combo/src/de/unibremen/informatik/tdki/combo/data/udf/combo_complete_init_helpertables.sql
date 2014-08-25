CREATE OR REPLACE PROCEDURE combo_complete_init_helpertables
(
  IN project VARCHAR(20)
)
LANGUAGE SQL
BEGIN
  DECLARE InclusionAxioms VARCHAR(50);

  -- variable for storing dynamic SQL statements
  -- there is a limit on max length of VARCHAR that depends on the page size
  -- in any case, it is strictly less than the page size, which we assume to be 4096 bytes
--  DECLARE dsql VARCHAR(3500); 
  DECLARE dsql CLOB(25000); 

  SET InclusionAxioms = project || '_InclusionAxioms';

  CALL combo_drop('TABLE workConceptNames');
  -- CREATE TABLE below needs dynamic SQL; otherwise we get an error that the table exists
  EXECUTE IMMEDIATE 'CREATE TABLE workConceptNames (concept integer NOT NULL PRIMARY KEY)';
  SET dsql = '
    WITH
    InclusionAxioms (c0, c1) AS
    (
      SELECT 
	lhs, rhs
      FROM ' ||
	InclusionAxioms || ' 
    )
    SELECT 
      c0 
    FROM
      Inclusionaxioms 
    WHERE 
      BITAND(c0,12)=0
    UNION
    SELECT 
      c1
    FROM 
      Inclusionaxioms 
    WHERE 
      BITAND(c1,12)=0';
  CALL combo_insert(dsql, 'workConceptNames');
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE workConceptNames WITH DISTRIBUTION AND DETAILED INDEXES ALL'); -- TODO: make this a stored procedure

  CALL combo_drop('TABLE workRoleInv');
  EXECUTE IMMEDIATE 'CREATE TABLE workRoleInv (role integer, inv integer)';
  EXECUTE IMMEDIATE 'CREATE INDEX roleinv_role_inv ON workRoleInv (role, inv)';
  EXECUTE IMMEDIATE 'CREATE INDEX roleinv_inv_role ON workRoleInv (inv, role)';
  SET dsql = '
    WITH
    InclusionAxioms (c0, c1) AS
    (
      SELECT 
	lhs, rhs
      FROM ' ||
	InclusionAxioms || '  
    )
    SELECT
      t0.r, BITXOR(t0.r,2)
    FROM
      InclusionAxioms AS t0(r,s)
    WHERE
      BITAND(t0.r,10)=8
    UNION
    SELECT
      t0.s, BITXOR(t0.s,2)
    FROM
      InclusionAxioms AS t0(r,s)
    WHERE
      BITAND(t0.s,10)=8
    UNION
    SELECT
      BITXOR(t0.r,2), t0.r
    FROM
      InclusionAxioms AS t0(r,s)
    WHERE
      BITAND(t0.r,2)=2
    UNION
    SELECT
      BITXOR(t0.s,2), t0.s
    FROM
      InclusionAxioms AS t0(r,s)
    WHERE
      BITAND(t0.s,2)=2';
  CALL combo_insert(dsql, 'workRoleInv');
  CALL sysproc.admin_cmd('RUNSTATS ON TABLE workRoleInv WITH DISTRIBUTION AND DETAILED INDEXES ALL');
END
@
