package com.sonalake.choco;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;

/**
 * This is more complex than the sudoku approach, because:
 * - It's a more generic solution for all graphs
 * - It assumes
 * and also assumes we wantto use the least number of colours to fill in this graph, without over using colours.
 */
public class GraphColouring {

  static public void main(String... args) {

    // Build our model
    Model model = new Model("colouring");

    // count the vertices
    int vertexCount = 10;

    // start with as many colours as vertices, and then try to bring this down
    int colourCount = vertexCount;

    // this array holds the actual colour of each vertex
    // colours run from 1 - 9 (both ends are INCLUSIVE)
    IntVar[] vertexColours = model.intVarArray("vertexColours", vertexCount, 1, colourCount - 1);


    // set up the constraints so we won't try to colour in adjacent vertices with the same colour
    constrainEdges(model, vertexColours, 0, 1, 5);
    constrainEdges(model, vertexColours, 1, 2, 6);
    constrainEdges(model, vertexColours, 2, 3, 7);
    constrainEdges(model, vertexColours, 3, 4, 8);
    constrainEdges(model, vertexColours, 4, 0, 9);

    constrainEdges(model, vertexColours, 5, 7, 8);
    constrainEdges(model, vertexColours, 6, 8, 9);
    constrainEdges(model, vertexColours, 7, 9, 5);
    constrainEdges(model, vertexColours, 8, 5, 6);
    constrainEdges(model, vertexColours, 9, 6, 7);

    // set up the constraints so we try to use the least number of colours
    minimiseColourUsage(model, colourCount, vertexColours, 3);

    // solve it
    Solver solver = model.getSolver();
    solver.showShortStatistics();


    // do this until we give up, the last result we get will be the best the solver can find, but may be
    // no better than the first
    while (solver.solve()) {
      System.out.println(solver.getSolutionCount() + " solutions found");
      describeSolution(model);
    }

    if (solver.getSolutionCount() == 0) {
      System.out.println("No solutions found");
    }

  }


  /**
   * Constrain the edges so they can't be the same colour
   *
   * @param model         the underlying model
   * @param vertexColours the vertex colours
   * @param from          the from point of the edges
   * @param toSet         the target points of the edges
   */
  private static void constrainEdges(Model model, IntVar[] vertexColours, int from, int... toSet) {
    for (int to : toSet) {
      model.allDifferent(vertexColours[from], vertexColours[to]).post();
    }
  }


  /**
   * Create all the constraints required to minimise the number of colours used. This means:
   * <p>
   * - Count how many times each colour is used
   * - Create a bitset of each colour to say if it's used or not
   * - Sum over this bitset to get a count of unique colours
   * - Set an objective to minimise the number of colours being used
   * - Set an objective to prefer lower ordinal colours (we're pretending  these are cheaper)
   *
   * @param model             the model
   * @param colourCount       how many colours are there
   * @param vertexColours     the actual colours for each node
   * @param maxUsagePerColour what is the maximum number of times a colour can be used
   */
  private static void minimiseColourUsage(Model model, int colourCount, IntVar[] vertexColours, int maxUsagePerColour) {
    final int vertexCount = vertexColours.length;
    // this will hold how many time each colour was used
    IntVar[] appliedColourCount = model.intVarArray("appliedColourCount", vertexCount, 0, vertexCount - 1);

    // this will count 1 for each colour used - we will use the globalCardinality to fill these in
    IntVar[] appliedColoursBitSet = model.intVarArray("appliedColoursBitSet", vertexCount, 0, 1);

    // this is our list of colour options, the globalCardinality below requires this
    int[] options = IntStream.range(0, colourCount).toArray();
    Arrays.stream(options).forEach(
      colour -> model.min(
        // get the minimum value and store it in appliedColoursBitSet
        appliedColoursBitSet[colour],
        // comparing 1
        model.intVar(1),
        // to the count of how many times this colour was used
        appliedColourCount[colour])
        .post()
    );


    // Here's where the above constraints all fall together
    //  - we count over the vertex colours
    //  - the colours are one of these option values, there is one per appliedColourCount
    //  - the count for the nth option value across all vertexColours goes into the nth appliedColourCount
    // the magic is then that the constraints applied to
    model.globalCardinality(vertexColours, options, appliedColourCount, false).post();

    // now make sure we're not using any given colour too many times
    IntVar maxColourUsageDomain = model.intVar("max usage per colour", 0, maxUsagePerColour);
    model.max(maxColourUsageDomain, appliedColourCount).post();

    // finally we sum the appliedColoursBitSet to get a count of how many unique colours there are
    // and set the objective to minimise this
    IntVar uniqueColourCount = model.intVar("Unique colour count", 0, colourCount);
    model.sum(appliedColoursBitSet, "=", uniqueColourCount).post();
    model.setObjective(Model.MINIMIZE, uniqueColourCount);


  }

  /**
   * Print out the solution that was found
   *
   * @param model
   */
  private static void describeSolution(Model model) {
    Map<String, Integer> vertexColours = groupVarsByNamePrefix(model, "vertexColours", true);
    Map<String, Integer> usedColours = groupVarsByNamePrefix(model, "appliedColourCount", false);


    System.out.println(String.format("\tusedColours (%s): %s ", usedColours.size(), usedColours));
    System.out.println("\tVertices: " + vertexColours);
  }

  /**
   * Get all the vars with a given prefix and put their int value into a map
   *
   * @param model       the model
   * @param name the    name prefix
   * @param retainZeros if false, then filter out any entry with a value of 0
   * @return a map of the name -> int value of the given entries
   */
  private static Map<String, Integer> groupVarsByNamePrefix(Model model, String name, boolean retainZeros) {
    return new TreeMap<>(stream(model.getVars())
      .filter(v -> v.getName().startsWith(name))
      .map(Variable::asIntVar)
      .filter(f -> retainZeros || f.getValue() > 0)
      .collect(Collectors.toMap(Variable::getName, IntVar::getValue)));
  }

}
