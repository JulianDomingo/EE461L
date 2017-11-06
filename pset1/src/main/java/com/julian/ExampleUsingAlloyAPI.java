/*
 * Name: Julian Domingo
 * UT EID: jad5348
 */

package com.julian;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig.Field;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Tuple;
import edu.mit.csail.sdg.alloy4compiler.translator.A4TupleSet;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExampleUsingAlloyAPI 
{
    //final static String PATH = "..."
    final static String PATH = "./src/main/java/com/julian/";

	public static void main(String[] args) throws Err 
    {
		String filename = PATH + "typehierarchy.als";
		A4Reporter rep = new A4Reporter();
		
		// Parse+typecheck the model
		System.out.println("=========== Parsing+Typechecking "+filename+" =============");
		Module world = CompUtil.parseEverything_fromFile(rep, null, filename);
		
		// Set options for how to execute the command
		A4Options options = new A4Options();
		options.solver = A4Options.SatSolver.SAT4J;
		
		Command command = world.getAllCommands().get(0);
		System.out.println("============ Command "+command+": ============");
		
		// generate and store all solutions
		List<A4Solution> allSols = new ArrayList<A4Solution>();
		int count = findAllSols(rep, world, options, command, allSols);
		System.out.println("number of solutions: " + count);
		
		// translate each solution into the corresponding Java program
		System.out.println("-----------");

		for (A4Solution sol: allSols) 
        {
			String program = createProgram(sol,
					getRelation(sol, "Type", "ext"),
					getRelation(sol, "Class", "impl"));
			System.out.print(program);
			System.out.println("-----------");
		}
	}
	
    /**
     * Adds all satisfiable solutions from the world to 'allSols' and
     * returns the number of satisfiable solutions.
     *
     * @param rep: the A4Reporter used for execute_command(). 
     * @param world: the world pertaining to the satisfiable solutions.
     * @param options: the options chosen for execute_command(). 
     * @param command: the first command in the world's list of commands.
     * @param allSols: the resultant list of satisfiable solutions to the world.
     */
	private static int findAllSols(A4Reporter rep, Module world,
	                        	   A4Options options, Command command, 
                                   List<A4Solution> allSols) 
                                   throws Err 
    {
        int solutions = 0;

        A4Solution solution = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options); 

        do 
        {
            if (solution.satisfiable())
            {
                solutions++;
                allSols.add(solution);
                solution = solution.next();
            }
        }
        while (solution.satisfiable());

        return solutions;	
	}

    /**
     * 
     * iterate over the sigs and their fields in <sol>
     * to find field <fieldname> in sig <signame>,
     * create a map that represents the tuples in the
     * corresponding relation, return the map
     * hint: use methods A4Solution.getAllReachableSigs() and
     * Sig.getFields() to iterate over all sigs and fields in <sol>;
     * use method A4Solution.eval(f) to get the value of field f in <sol>;
     * use method A4Tuple.atom(i) to get atom at position i in the tuple
     */
	private static Map<String, String> getRelation(A4Solution sol,
                                                   String sigName, 
                                                   String fieldName) 
    {
        //Map<String, String> result = new HashMap<String, String>();
        //for (Sig sig : sol.getAllReachableSigs())
        //{
            //if (!sig.toString().contains(sigName)) { continue; } 

            //for (Sig.Field field : sig.getFields())
            //{
                //if (!field.toString().contains(fieldName)) { continue; }

                //for (A4Tuple tuple : sol.eval(field))
                //{
                    //for (int i = 0; i < tuple.arity(); i++) {
                        //System.out.print(tuple.atom(i));
                        //if (i < tuple.arity() - 1) System.out.print(", ");
                    //}
                //}
            //}
        //}
        
        //return result;
        return null;
	}
		
	private static String createProgram(A4Solution sol,
                                        Map<String, String> supertype,
                                        Map<String, String> implementS) 
    {
        // assume input map <supertype> is already initialized
        // to represent the value of "ext" relation in <sol>
        // assume input map <implementS> is already initialized
        // to represent the value of "impl" relation in <sol>
        // return the Java program represented by <sol>
        
        // your code goes here
        return null;
	}
}
