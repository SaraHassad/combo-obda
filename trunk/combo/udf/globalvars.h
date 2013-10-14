#ifndef GLOBALVARS_H
#define	GLOBALVARS_H

#include <boost/assign/list_of.hpp>

using namespace boost::assign;

const vector<RoleAtom> roleAtoms = list_of(RoleAtom(1, 2, 3))(RoleAtom(4, 5, 6));
const set<RoleAtom> genRoles = list_of(RoleAtom(7, 8, 9))(RoleAtom(10, 11, 12));
const vector<int> freeVars = list_of(4)(2)(7);

#endif
