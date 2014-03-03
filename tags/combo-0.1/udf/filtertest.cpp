#include <map>
#include <vector>
#include <iostream>
#include <memory>
#include <boost/test/included/unit_test.hpp>
#include <boost/assign/list_of.hpp>
#include <boost/ptr_container/ptr_map.hpp>
#include "filter.h"

using namespace boost::unit_test;
using namespace boost::assign;
using namespace std;

class NameManager {
private:
  map<string, int> nameid_map;
  int symbolCounter;
  int individualCounter;
public: 
  NameManager();
  void addSymbol(string name);
  void addIndividual(string name);
  int getRoleNameId(string name);
  int getCopyRoleNameId(string name);
  int getInvRoleId(string name);
  int getIndividualId(string name);
};

NameManager::NameManager() {
  symbolCounter = 0;
  individualCounter = 0;
}

void NameManager::addSymbol(string name) {
  if (nameid_map.find(name) == nameid_map.end()) {
    symbolCounter++;
    nameid_map[name] = symbolCounter; 
  }
}

void NameManager::addIndividual(string name) {
  if (nameid_map.find(name) == nameid_map.end()) {
    individualCounter--;
    nameid_map[name] = individualCounter; 
  }
}

int NameManager::getRoleNameId(string name) {
  int result = -1;
  if (nameid_map.find(name) != nameid_map.end()) {
    result = ((nameid_map[name] * 16) | 0x00000008);
  }
  return result;
}

int NameManager::getInvRoleId(string name) {
  int result = -1;
  if (nameid_map.find(name) != nameid_map.end()) {
    result = ((nameid_map[name] * 16) | 0x0000000a);
  }
  return result;
}

int NameManager::getCopyRoleNameId(string name) {
  int result = -1;
  if (nameid_map.find(name) != nameid_map.end()) {
    result = ((nameid_map[name] * 16) | 0x00000009);
  }
  return result;
}

int NameManager::getIndividualId(string name) {
  return nameid_map.at(name);
  // int result = -1;
  // if (nameid_map.find(name) != nameid_map.end()) {
  //   result = nameid_map[name];
  // }
  // return result;
}

void testNameManager() {
  NameManager nm;
  BOOST_CHECK(nm.getRoleNameId("R") == -1);
  nm.addSymbol("R");
  nm.addSymbol("T");
  nm.addIndividual("a");
  nm.addIndividual("b");
  BOOST_CHECK(nm.getRoleNameId("R") == 24);
  BOOST_CHECK(nm.getCopyRoleNameId("R") == 25);
  BOOST_CHECK(nm.getInvRoleId("R") == 26);
  BOOST_CHECK(nm.getRoleNameId("T") == 40);
  BOOST_CHECK(nm.getIndividualId("a") == -1);
  BOOST_CHECK(nm.getIndividualId("b") == -2);
}

void testReferences() {
  auto_ptr<vector<int> > v(new vector<int>());
  v->push_back(1);

  vector<int> & vRef = (*v);
  BOOST_CHECK(v.get() != NULL);
  BOOST_CHECK(&vRef == v.get());

  vector<int> vCopy = (*v);
  BOOST_CHECK(vCopy.size() == 1);
  BOOST_CHECK(vCopy.at(0) == 1);
  BOOST_CHECK(v.get() != NULL);
  BOOST_CHECK(&vCopy != v.get());

  vector<int> const& vConstRef = (*v);
  BOOST_CHECK(v.get() != NULL);
  BOOST_CHECK(&vConstRef == v.get());

  auto_ptr<vector<int> > vNew(new vector<int>(vConstRef));
  BOOST_CHECK(v.get() != NULL);
  BOOST_CHECK(vNew.get() != v.get());
  BOOST_CHECK(vNew->size() == 1);
  BOOST_CHECK(vNew->at(0) == 1);

  boost::ptr_map<int, vector<int> > map;
  map.insert(1, vNew);
  BOOST_CHECK(vNew.get() == NULL);
}

void testInitializeRoleAtomVector() {
  vector<RoleAtom> ias = list_of(RoleAtom(2, 3, 4))(RoleAtom(5, 6, 7));
  BOOST_CHECK(ias.size() == 2);
  BOOST_CHECK(ias.at(0).role == 2);
  BOOST_CHECK(ias.at(0).v1 == 3);
  BOOST_CHECK(ias.at(0).v2 == 4);
  BOOST_CHECK(ias.at(1).role == 5);
  BOOST_CHECK(ias.at(1).v1 == 6);
  BOOST_CHECK(ias.at(1).v2 == 7);
}

void testBitOperation() {
  int v = 20;
  BOOST_CHECK( (v & 4) == 4 );
  v = 8;
  BOOST_CHECK( (v & 4) != 4 );
}

void testRoleAtomComparison() {
  RoleAtom ia123(1, 2, 3);
  RoleAtom ia234(2, 3, 4);
  BOOST_CHECK(ia123 < ia234);
  RoleAtom ia111(1, 1, 1);
  BOOST_CHECK(ia111 < ia123);
  BOOST_CHECK(!(ia123 < ia123));
  BOOST_CHECK(!(ia234 < ia123));
}

void testExample9() {
  NameManager nm;
  nm.addSymbol("T");
  nm.addSymbol("R");
  nm.addIndividual("a");
  nm.addIndividual("b");

  int T = nm.getRoleNameId("T");
  int R = nm.getRoleNameId("R");
  int a = nm.getIndividualId("a");
  int b = nm.getIndividualId("b");

  set<RoleAtom> genRoles;
  genRoles.insert(RoleAtom(R, T, R));
  genRoles.insert(RoleAtom(T, R, T));  
  // the query
  vector<RoleAtom> query = vector<RoleAtom>();
  query.push_back(RoleAtom(T, 0, 1));
  query.push_back(RoleAtom(T, 2, 1));
  // free variables
  vector<int> freeVars = vector<int>();
  freeVars.push_back(0);
  freeVars.push_back(2);

  int values1[] = {a, T, a};
  BOOST_CHECK(filter(freeVars, query, values1, 3, genRoles) == true);

  int values2[] = {b, T, b};
  BOOST_CHECK(filter(freeVars, query, values2, 3, genRoles) == true);
   
  int values3[] = {a, T, b};
  BOOST_CHECK(filter(freeVars, query, values3, 3, genRoles) == false);
    
  int values4[] = {b, T, a};
  BOOST_CHECK(filter(freeVars, query, values4, 3, genRoles) == false);
}

void testExample10() {
  NameManager nm;
  nm.addSymbol("T");
  nm.addSymbol("R");
  nm.addIndividual("a");
  nm.addIndividual("b");

  int T = nm.getRoleNameId("T");
  int R = nm.getRoleNameId("R");
  int a = nm.getIndividualId("a");
  int b = nm.getIndividualId("b");

  set<RoleAtom> genRoles;
  genRoles.insert(RoleAtom(R, T, R));
  genRoles.insert(RoleAtom(T, R, T));      
  // the query
  vector<RoleAtom> query = vector<RoleAtom>();
  query.push_back(RoleAtom(T, 0, 1));
  query.push_back(RoleAtom(R, 1, 2));
  query.push_back(RoleAtom(T, 2, 1));
  // free variables
  vector<int> freeVars = vector<int>();
  freeVars.push_back(0);

  int values1[] = {a, T, R};
  BOOST_CHECK(filter(freeVars, query, values1, 3, genRoles) == false);
    
  int values2[] = {b, T, R};
  BOOST_CHECK(filter(freeVars, query, values2, 3, genRoles) == false);
}

void testQuery1() {
  NameManager nm;
  nm.addSymbol("T");
  nm.addSymbol("R");
  nm.addIndividual("a");

  int T = nm.getRoleNameId("T");
  int R = nm.getRoleNameId("R");
  int a = nm.getIndividualId("a");

  set<RoleAtom> genRoles;
  genRoles.insert(RoleAtom(R, T, R));
  genRoles.insert(RoleAtom(T, R, T));      

  vector<RoleAtom> query = vector<RoleAtom>();
  query.push_back(RoleAtom(T, 1, 0));

  // free variables is empty
  vector<int> freeVars = vector<int>();

  int values1[] = {T, a};
  BOOST_CHECK(filter(freeVars, query, values1, 2, genRoles) == true);
    
  int values2[] = {T, R};
  BOOST_CHECK(filter(freeVars, query, values2, 2, genRoles) == true);
}

void testExample7FromFilteringPaper() {
  NameManager nm;
  nm.addSymbol("degreeFrom");
  nm.addSymbol("deptOf");
  nm.addSymbol("teachesAt");

  int degreeFrom = nm.getRoleNameId("degreeFrom");
  int deptOf = nm.getRoleNameId("deptOf");
  int teachesAt = nm.getRoleNameId("teachesAt");
  int invDeptOf = nm.getInvRoleId("deptOf");
  int invTeachesAt = nm.getInvRoleId("teachesAt");

  set<RoleAtom> genRoles;
  genRoles.insert(RoleAtom(invDeptOf, degreeFrom, invDeptOf));
  genRoles.insert(RoleAtom(invTeachesAt, invDeptOf, invTeachesAt));
  genRoles.insert(RoleAtom(degreeFrom, invTeachesAt, degreeFrom));

  vector<RoleAtom> query = vector<RoleAtom>();
  query.push_back(RoleAtom(degreeFrom, 0, 1));
  query.push_back(RoleAtom(deptOf, 2, 1));
  query.push_back(RoleAtom(teachesAt, 0, 2));

  // free variables is empty
  vector<int> freeVars = vector<int>();

  int values[] = {invTeachesAt, degreeFrom, invDeptOf};
  BOOST_CHECK(filter(freeVars, query, values, 3, genRoles) == false);
}

void testExample8FromFilteringPaper() {
  NameManager nm;
  nm.addSymbol("paysSalaryOf");
  nm.addSymbol("worksFor");
  nm.addSymbol("isAffiliatedWith");
  nm.addIndividual("a");

  int worksFor0 = nm.getRoleNameId("worksFor");
  int paysSalaryOf0 = nm.getRoleNameId("paysSalaryOf");
  int worksFor1 = nm.getCopyRoleNameId("worksFor");
  int paysSalaryOf1 = nm.getCopyRoleNameId("paysSalaryOf");
  int isAffiliatedWith = nm.getRoleNameId("isAffiliatedWith");
  int invWorksFor = nm.getInvRoleId("worksFor");
  int invPaysSalaryOf = nm.getInvRoleId("paysSalaryOf");
  int invIsAffiliatedWith = nm.getInvRoleId("isAffiliatedWith");
  int a = nm.getIndividualId("a");

  set<RoleAtom> genRoles;
  genRoles.insert(RoleAtom(paysSalaryOf0, worksFor0, paysSalaryOf0));
  genRoles.insert(RoleAtom(worksFor0, paysSalaryOf0, worksFor1));
  genRoles.insert(RoleAtom(paysSalaryOf0, worksFor1, paysSalaryOf1));
  genRoles.insert(RoleAtom(worksFor0, paysSalaryOf1, worksFor0));
  // consequences of role inclusions
  genRoles.insert(RoleAtom(isAffiliatedWith, worksFor0, paysSalaryOf0));
  genRoles.insert(RoleAtom(isAffiliatedWith, worksFor1, paysSalaryOf1));
  genRoles.insert(RoleAtom(invIsAffiliatedWith, paysSalaryOf0, worksFor1));
  genRoles.insert(RoleAtom(invIsAffiliatedWith, paysSalaryOf1, worksFor0));

  vector<RoleAtom> query = vector<RoleAtom>();
  query.push_back(RoleAtom(worksFor0, 0, 1));
  query.push_back(RoleAtom(paysSalaryOf0, 1, 2));
  query.push_back(RoleAtom(isAffiliatedWith, 3, 2));

  vector<int> freeVars = vector<int>();
  freeVars.push_back(0);

  // the match used in the example
  int values1[] = {a, worksFor0, paysSalaryOf0, worksFor1};
  BOOST_CHECK(filter(freeVars, query, values1, 4, genRoles) == true);

  // this is also a possible match that should not be filtered
  int values2[] = {a, worksFor0, paysSalaryOf0, worksFor0};
  BOOST_CHECK(filter(freeVars, query, values2, 4, genRoles) == true);

  // new query which guarantees that values2 should be filtered
  query = vector<RoleAtom>();
  query.push_back(RoleAtom(worksFor0, 0, 1));
  query.push_back(RoleAtom(paysSalaryOf0, 1, 2));
  query.push_back(RoleAtom(worksFor0, 2, 3));

  BOOST_CHECK(filter(freeVars, query, values1, 4, genRoles) == true);
  BOOST_CHECK(filter(freeVars, query, values2, 4, genRoles) == false);
}

test_suite*
init_unit_test_suite( int, char* [] ) {
  framework::master_test_suite().add(BOOST_TEST_CASE(&testInitializeRoleAtomVector));
  framework::master_test_suite().add(BOOST_TEST_CASE(&testBitOperation));
  framework::master_test_suite().add(BOOST_TEST_CASE(&testRoleAtomComparison));
  framework::master_test_suite().add(BOOST_TEST_CASE(&testNameManager));
  framework::master_test_suite().add(BOOST_TEST_CASE(&testReferences));
  framework::master_test_suite().add(BOOST_TEST_CASE(&testExample9));
  framework::master_test_suite().add(BOOST_TEST_CASE(&testExample10));
  framework::master_test_suite().add(BOOST_TEST_CASE(&testQuery1));
  framework::master_test_suite().add(BOOST_TEST_CASE(&testExample7FromFilteringPaper));
  framework::master_test_suite().add(BOOST_TEST_CASE(&testExample8FromFilteringPaper));
  return 0;
}
