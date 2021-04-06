# Playing with chocosolver

Simple stuff, really.

##  LogicalSatisfiability

Source: [LogicalSatisfiability](src/main/java/com/sonalake/choco/LogicalSatisfiability.java)

A very simple example:
 - Given a logical statement
 - Find a set of boolean parameters so that it evaluates to `true`
 
  

## Sudoku

Source: [Sudoku](src/main/java/com/sonalake/choco/Sudoku.java)

Solves sudoku

- Given a grid of 81 variables: some with a 1-digit domain, and others in the `1-9` domain
- Add constraints so rows are all different, columns are all different, 
and each of the 9 sub-squares on non different
- Solve it, and print it


## Graph colouring

Source [GraphColouring](src/main/java/com/sonalake/choco/GraphColouring.java)

More complex graph colouring example

This is trying to minimize the number of distinct colours, instead 
of just  finding the first solution (as the sudoku one does).

It's also applying a rule that no colour can be used more than N times, 
to mimic the idea that this could be modelling a human worker, who could
only do so many tasks in a day.

## Travelling salesman

Source [TravellingSalesman](src/main/java/com/sonalake/choco/TravellingSalesman.java)

A copy of [this sample](https://choco-solver.org/tutos/traveling-salesman-problem/description/),
 but with comments to explain it better to me ;)
 
