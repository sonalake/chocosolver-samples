package com.sonalake.choco;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;

import static org.chocosolver.solver.constraints.nary.cnf.LogOp.and;
import static org.chocosolver.solver.constraints.nary.cnf.LogOp.or;


/**
 * Two variables in a logical statement.
 */
public class LogicalSatisfiability {

  static public void main(String... args) {
    Model model = new Model("NSAT");

    // two variables
    BoolVar p = model.boolVar("p");
    BoolVar q = model.boolVar("q");

    // the constraint we want to satisfy
    LogOp constraint = and(
      or(p, q),
      or(p, q.not()),
      or(p.not(), q)
    );
    model.addClauses(constraint);


    // solve it

    Solver solver = model.getSolver();
    solver.showShortStatistics();
    while (solver.solve()) {
      System.out.println("p: " + p);
      System.out.println("q: " + q);
    }


  }
}
