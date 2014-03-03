#ifndef FILTER_H
#define	FILTER_H

#include <set>
#include <vector>
#include <boost/tuple/tuple_comparison.hpp>

using namespace std;

struct RoleAtom {
    int role;
    int v1;
    int v2;

public:

  RoleAtom() {
  }

  RoleAtom(int role, int v1, int v2)
  : role(role), v1(v1), v2(v2) {
  }

  bool operator<(RoleAtom const& other) const {
    return boost::tie(this->role, this->v1, this->v2) < boost::tie(other.role, other.v1, other.v2);
  }
};

int filter(int values[], int const varcount);
bool filter(vector<int> const& freeVars, vector<RoleAtom> const& roleAtoms, int values[], int const varcount, set<RoleAtom> const& genRoles);
#endif

