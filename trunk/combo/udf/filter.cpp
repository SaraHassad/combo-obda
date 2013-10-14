#include <vector>
#include <iostream>
#include <algorithm>
#include <map>
#include <boost/tuple/tuple.hpp>
#include <boost/tuple/tuple_comparison.hpp>
#include "filter.h"
#include "globalvars.h"

using namespace std;
using namespace boost;

inline int getInverse(int role) {
  return (role ^ 2);
}

inline bool isIndvName(int value) {
  return value < 0;
}

inline bool isCopyOfRole(int value) {
  return ((value & 1) == 1);
}

inline int getNormalOfCopy(int value) {
  return value ^ 1;
}

void printVector(vector<int> const& v) {
  for (vector<int>::const_iterator i = v.begin(); i != v.end(); ++i) {
    cout << *i << ", ";
  }
  cout << endl;
}

void printMap(map<int, vector<int> > const& tau) {
    for (map<int, vector<int> >::const_iterator i = tau.begin(); i != tau.end(); i++) {
        cout << "Key: " << i->first << ", Value: ";
        printVector(i->second);
    }
}

bool isLeadsTo(int role, int v1, int v2, int values[], set<RoleAtom> const& genRoles) {
  bool result = false;
  if (isIndvName(values[v1]) && !isIndvName(values[v2])) {
    // no need to check anything in this case since the match comes from the canonical model
    // and the definition of the canonical model guarantees that leadsto holds.
    result = true;
  } else if (!isIndvName(values[v1]) && !isIndvName(values[v2])) {
    RoleAtom ra(role, values[v1], values[v2]);
    result = (genRoles.find(ra) != genRoles.end());
  }
  return result;
}

void extendTau(map<int, vector<int> > & tau, set< tuple<int, vector<int> > > & newMappings, int role, int v1, int v2, int values[], set<RoleAtom> const& genRoles) {
  int invRole = getInverse(role);
  if (isLeadsTo(role, v1, v2, values, genRoles)) {
    vector<int> v(tau.at(v1));
    v.push_back(values[v2]);
    newMappings.insert(make_tuple(v2, v));
  } 
  if (isLeadsTo(invRole, v2, v1, values, genRoles)) {
    int v1PathLength = tau.at(v1).size();
    if (v1PathLength > 1) {
      int last = tau.at(v1).at(v1PathLength - 1);
      int beforelast = tau.at(v1).at(v1PathLength - 2);
      if (last == values[v1] && beforelast == values[v2]) {
	vector<int> v(tau.at(v1).begin(), tau.at(v1).end() - 1);
	newMappings.insert(make_tuple(v2, v));
      }
    }
  }
}

bool completeTau(map<int, vector<int> > & tau, vector<RoleAtom> const& roleAtoms, int values[], int const varcount, set<RoleAtom> const& genRoles) {
  bool sequenceStabilized;
  do {
    sequenceStabilized = true;
    set< tuple<int, vector<int> > > newMappings;
    for (vector<RoleAtom>::const_iterator i = roleAtoms.begin(); i != roleAtoms.end(); ++i) {
      const RoleAtom &ra = (*i);
      // tau(v1) is defined
      if (tau.count(ra.v1) != 0) {
	extendTau(tau, newMappings, ra.role, ra.v1, ra.v2, values, genRoles);
      }
      // tau(v2) is defined
      if (tau.count(ra.v2) != 0) {
	extendTau(tau, newMappings, getInverse(ra.role), ra.v2, ra.v1, values, genRoles);      
      }
    }
    for (set< tuple<int, vector<int> > >::const_iterator i = newMappings.begin(); i != newMappings.end(); ++i) {
      int var = i->get<0>();
      vector<int> const& vec = i->get<1>();
      if (tau.count(var) == 0) {
        tau[var] = vec;
	sequenceStabilized = false;
	// printMap(tau);
      } else if (tau.at(var) != vec) {
	return false;
      }
    } 
  } while (!sequenceStabilized);
  return (tau.size() == varcount); // at this point, we return true if tau is a total function
}

bool filter(vector<int> const& freeVars, vector<RoleAtom> const& roleAtoms, int values[], int const varcount, set<RoleAtom> const& genRoles) {
  // if a free variable is not matched with an individual name, then that tuple is filtered immediately
  for (vector<int>::const_iterator i = freeVars.begin(); i != freeVars.end(); ++i) {
    int n = (*i);
    if (!isIndvName(values[n])) {
      return false;
    }
  }

  map<int, vector<int> > tau; 
  // if some terms are matched with individual names, then initialize the root configuration with those terms and their matches
  bool indvnameExists = false;
  for (int i = 0; i < varcount; i++) {
      if (isIndvName(values[i])) {
          indvnameExists = true;
	  tau[i].push_back(values[i]);
      }
  }

  if (indvnameExists) {
    return completeTau(tau, roleAtoms, values, varcount, genRoles);    
  } else { // no term is matched with an individual name
    for (int i = 0; i < varcount; i++) {
      tau.clear(); // clear the previously constructed mapping before we start with a new root configuration
      tau[i].push_back(values[i]); // make the ith term the root configuration
      if (completeTau(tau, roleAtoms, values, varcount, genRoles) == true) {
	return true;
      }
    }
  }
  return false;
}

int filter(int values[], int const varcount) {
  bool result = filter(freeVars, roleAtoms, values, varcount, genRoles);
  return (result == true ? 1 : 0);
}
