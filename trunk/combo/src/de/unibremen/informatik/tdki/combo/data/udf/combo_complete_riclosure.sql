CREATE OR REPLACE PROCEDURE combo_complete_riclosure
(
  IN project VARCHAR(20)
)
LANGUAGE SQL
BEGIN
  DECLARE RoleAssertions VARCHAR(50);
  DECLARE RoleInclusions VARCHAR(50);
  DECLARE Stage1 VARCHAR(50);

  DECLARE dsql CLOB(25000); 

  SET RoleAssertions = project || '_RoleAssertions';
  SET RoleInclusions = project || '_RoleInclusions';
  SET Stage1 = project || '_Stage1';

  CALL combo_drop('TABLE ' || Stage1);
  EXECUTE IMMEDIATE 'CREATE TABLE ' || Stage1 ||' (role integer, lhs integer, rhs integer)';
  EXECUTE IMMEDIATE 'CREATE INDEX ' || project || '_stg1_role_lhs_rhs ON ' || Stage1 || ' (role, lhs, rhs)';
  EXECUTE IMMEDIATE 'CREATE INDEX ' || project || '_stg1_role_rhs_lhs ON ' || Stage1 || ' (role, rhs, lhs)';

  SET dsql = '
    WITH
    RoleInclusions (c0, c1) AS
    (
      SELECT 
	lhs, rhs
      FROM  ' ||
	RoleInclusions || '  
    ),
    RoleInv (c0, c1) AS
    (
      SELECT 
	role, inv
      FROM 
	workRoleInv 
    ),
    RoleAssertions (c0, c1, c2) AS
    (
      SELECT 
	role, lhs, rhs
      FROM ' ||
	RoleAssertions || '  
    )
    SELECT
      t0.r2, t2.o1, t2.o2
    FROM
      RoleInclusions AS t0(r1,r2), RoleAssertions AS t2(r1,o1,o2), 
      (
	SELECT
	  t0.r
	FROM
	  RoleInv AS t0(r,s)
      ) AS t1(r2)
    WHERE
      t0.r1=t2.r1 AND t0.r2=t1.r2 AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  RoleAssertions AS t(r2,o1,o2)
	WHERE
	  t.r2=t0.r2 AND t.o1=t2.o1 AND t.o2=t2.o2
      )
    UNION
    SELECT
      t1.s, t2.o2, t2.o1
    FROM
      RoleInclusions AS t0(r1,r2), RoleInv AS t1(s,r2), RoleAssertions AS t2(r1,o1,o2)
    WHERE
      t0.r1=t2.r1 AND t0.r2=t1.r2 AND NOT EXISTS 
      (
	SELECT
	  1
	FROM
	  RoleAssertions AS t(s,o2,o1)
	WHERE
	  t.s=t1.s AND t.o2=t2.o2 AND t.o1=t2.o1
      )';
  CALL combo_insert(dsql, Stage1);
  CALL combo_updatestats(Stage1);  
END
@
