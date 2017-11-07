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
import java.lang.StringBuilder;
import java.util.*;

public class ExampleUsingAlloyAPI 
{
    private static final String CONCRETE = "Concrete";
    private static final String ABSTRACT = "Abstract";
    private static final String INTERFACE = "Interface";
    private static final String OBJECT = "Object";

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
     * Adds all satisfiable solutions from the world to 'allSols'. 
     *
     * @param rep: the A4Reporter used for execute_command(). 
     * @param world: the world pertaining to the satisfiable solutions.
     * @param options: the options chosen for execute_command(). 
     * @param command: the first command in the world's list of commands.
     * @param allSols: the resultant list of satisfiable solutions to the world.
     * @return the number of satisfiable solutions. 
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
                System.out.println(solution);
                solutions++;
                allSols.add(solution);
                solution = solution.next();
            }
        }
        while (solution.satisfiable());

        return solutions;	
	}

    /**
     * Searches the sigs of the solution for the tuples specified by the
     * sig name and field name.
     *
     * @param solution: The solution to fetch sigs and their fields from.
     * @param sigName: The name of the sig of interest.
     * @param fieldName: The name of the field of interest from the sig 'sigName'.
     * @return a mapping of the tuples in the relation.
     */
	private static Map<String, String> getRelation(A4Solution solution,
                                                   String sigName, 
                                                   String fieldName) 
    {
        Map<String, String> result = new TreeMap<String, String>();

        for (Sig sig : solution.getAllReachableSigs())
        {
            if (!sig.toString().contains(sigName)) { continue; } 

            for (Sig.Field field : sig.getFields())
            {
                if (!field.toString().contains(fieldName)) { continue; }
    
                for (A4Tuple tuple : solution.eval(field))
                {
                    final String key = tuple.atom(0);
                    final String value = tuple.atom(1); 

                    result.put(key, value);
                }
            }
        }

        return result;
	}

    /**
     * Returns a string of the resultant Java program from 'solution'.
     *
     * @param solution: The solution to fetch edge cases from to add to the resultant string.
     * @param extend: The mapping of all classes/interfaces extending other classes or interfaces.
     * @param implement : The mapping of all classes/interfaces implementing interfaces (that aren't itself). 
     * @return the string output of the resultant Java program.
     */
	private static String createProgram(A4Solution solution,
                                        Map<String, String> extend,
                                        Map<String, String> implement) 
    {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : extend.entrySet())
        {
            StringBuilder sb = new StringBuilder(); 

            switch (getClassType(entry.getKey()))
            {
                case CONCRETE:
                    sb.append("class C" + getClassNumber(entry.getKey()));  
                    break;

                case ABSTRACT:
                    sb.append("abstract class A" + getClassNumber(entry.getKey()));
                    break;

                case INTERFACE:
                    sb.append("interface I" + getClassNumber(entry.getKey()));
                    break;
            }
            
            addExtendedClass(sb, entry);
            addImplementedInterfaces(sb, entry, implement);

            sb.append(" {}\n");

            result.append(sb.toString());
        }

        // Edge case: handle solutions which don't have 'extends' or 'implements', but
        // still contain a lone interface. 
        for (Sig sig : solution.getAllReachableSigs())
        {
            if (sig.toString().contains("univ")) 
            {
                for (A4Tuple tuple : solution.eval(sig))
                {
                    String universeMember = tuple.toString();

                    if (universeMember.contains(OBJECT)) { continue; }

                    switch (getClassType(universeMember))
                    {
                        case CONCRETE:
                            universeMember = "class C"; 
                            break;

                        case ABSTRACT:
                            universeMember = "abstract class A"; 
                            break;

                        case INTERFACE:
                            universeMember = "interface I";
                            break;
                    }

                    universeMember += getClassNumber(tuple.toString());

                    if (!result.toString().contains(universeMember))
                    {
                        result.append(universeMember)
                              .append(" {}")
                              .append("\n");                        
                    }
                }
            }
        }

        return result.toString();
	}

    /**
     * A helper method returning the appropriate class/interface from 'classType'.
     *
     * @param classType: The string used to compare with the global constants in 
     *                   determining the identity of 'classType'. 
     * @return the correct identity of 'classType'.
     * @throws IllegalArgumentException: when 'classType' doesn't fit the identity
     *                                   of any of the below global constants.
     */
    private static String getClassType(String classType)
    {
        if (classType.contains(CONCRETE)) { return CONCRETE; }
        else if (classType.contains(ABSTRACT)) { return ABSTRACT; }
        else if (classType.contains(INTERFACE)) { return INTERFACE; }

        throw new IllegalArgumentException();
    }

    /**
     * A helper method returning the class/interface number of 'mapValue'.
     *
     * @param string: The string used to get the class number from. 
     * @return the substring of the number string value.
     */
    private static String getClassNumber(String string) 
    {
        return string.substring(string.indexOf('$') + 1, string.length());
    }

    /**
     * A helper method to add the implemented interface to the existing StringBuilder.
     *
     * @param sb: The StringBuilder object to potentially add implemented interfaces to.
     * @param entry: The Map.Entry obejct containing the potentially implemented interface.
     * @param implemented: The Map object containing the mappings of 'implements' relationships.
     */
    private static void addImplementedInterfaces(StringBuilder sb, 
                                                 Map.Entry<String, String> entry, 
                                                 Map<String, String> implemented)
    {
        if (implemented.containsKey(entry.getKey()))
        {
            String implementedInterface = implemented.get(entry.getKey());

            sb.append(" implements I").append(getClassNumber(implementedInterface));
        }
    }

    /**
     * A helper method to append the extended class string to the existing StringBuilder. 
     *
     * @param sb: The StringBuilder object to potentially add extended classes to.
     * @param entry: The Map.Entry object containing the potentially extended class.
     */
    private static void addExtendedClass(StringBuilder sb, Map.Entry<String, String> entry)
    {
        if (!entry.getValue().contains(OBJECT))
        {
            sb.append(" extends ")
              .append(getClassType(entry.getValue()).contains(CONCRETE) ? "C" :
                      getClassType(entry.getValue()).contains(ABSTRACT) ? "A" : "I")
              .append(getClassNumber(entry.getValue()));
        }
    }
}
