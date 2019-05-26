package modules;
/*	Author: Callan Hampton
 *  StudentNum: C3235869
 *
 *  TODO
 *  	Develop Unit tests
 *  	Error handling
 */

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

public class AverageLengthOfIdentifier implements ModuleInterface
{
	private ArrayList<String> collectedInformation;
	private static final String MODULE_NAME = "Average Identifier Length",
			MODULE_DESCRIPTION = "This module calculates the average character length of all identifiers that appear in a given Java file.";
	private static final String
			ZERO_AVERAGE_MESSAGE = "This type is ignored as there are no identifiers of this type used.\n",

	LOW_AVERAGE_MESSAGE = "This type of identifier falls in the low range, it indicates that the majority of identifiers used are very short in length.\n"+
			"It may be beneficial to examine the chosen identifiers and expand on the names.\n" +
			"By doing so the readability of the program will increase, benefiting future use and understanding.\n",

	MEDIUM_AVERAGE_MESSAGE ="This type of identifier falls in the medium range, this indicates that identifiers can mostly be identified as meaningful.\n"+
			"However there is still room to expand the identifiers to benefit the long term sustainability of the program.\n"+
			"The given number is acceptable but can be improved on.\n",

	HIGH_AVERAGE_MESSAGE =  "This range indicates that the identifiers consist of multiple words.\n" +
			"This length is beneficial to the long term sustainability of the code as more meaning is conveyed in the\n" +
			"identifiers allowing for easier understanding.\n" +
			"At the same identifiers of this length have the potential to be bloated and might have names that are convoluted.\n",

	VERY_HIGH_AVERAGE_MESSAGE = "At this level, the identifiers chosen have a lot of potential to be convoluted.\n" +
			"Examining the chosen names might reveal some identifiers that can be shortened or re-worded to make more sense.\n",

	LOW_STANDARD_DEVIATION_MESSAGE = "Identifiers of this category sit around the calculated average, there is not a lot of variance in the size.\n" ,

	MEDIUM_STANDARD_DEVIATION_MESSAGE = "Identifiers of this category vary by 1-2 words in length. For higher averages this acceptable, low to medium\n"+
			"averages might indicate that many words fall into lower or higher ranges that which would require review.\n",

	HIGH_STANDARD_DEVIATION_MESSAGE = "This category shows that identifiers vary by 2-3 words meaning that some identifiers will convey more meaning\n" +
			"than others. For this range it is suggest that more meaning is given to identifiers of shorter length to both\n"+
			"increase readability and keep the identifiers consistent in length\n";

	// numbers chosen to reflect average size of words
	private static final int LOW_AVERAGE = 9, MEDIUM_AVERAGE = 16, HIGH_AVERAGE = 28, VERY_HIGH_AVERAGE = 40;
	// These numbers were chosen in respect to the average size of words, higher deviation means higher variance of words used
	private static final int LOW_STANDARD_DEVIATION = 3, MEDIUM_STANDARD_DEVIATION = 7, HIGH_STANDARD_DEVIATION= 13;

	public AverageLengthOfIdentifier()
	{
		collectedInformation = new ArrayList<>();
	}

	/**
	 * @return The name of the module.
	 */
	@Override
	public String getName()
	{
		return MODULE_NAME;
	}

	/**
	 * @return a description of what the module is testing
	 */
	@Override
	public  String getDescription()
	{
		return MODULE_DESCRIPTION;
	}

	/**
	 * This calculates the average length of identifiers, first as a total group then for each type of identifier.
	 * These types include Classes, Variables, Methods, Parameters and packages. The Identifiers are retrieved from the compiliation unit
	 * and then processed. Appropriate metrics and messages are then produced for each Identifier group
	 * @param sourceRoot AST of Source Files
	 * @return           the summary results of the module being run
	 * @see              SourceRoot
	 *
	 */
	@Override
	public String[] executeModule(SourceRoot sourceRoot)
	{
		ArrayList<CompilationUnit> units = (ArrayList<CompilationUnit>)sourceRoot.getCompilationUnits();
		ArrayList<String>
				methods  	= new ArrayList<>(),
				variables 	= new ArrayList<>(),
				classes  	= new ArrayList<>(),
				packages 	= new ArrayList<>(),
				parameters 	= new ArrayList<>();

		for(CompilationUnit unit : units)
		{
			unit.accept(new MethodVisitor(), methods);
			unit.accept(new VariableVisitor(), variables);
			unit.accept(new ClassVisitor(), classes);
			unit.accept(new ParameterVisitor(), parameters);
			unit.accept(new PackageVisitor(), packages);
		}

		String[] allIdentifiers = evaluateMetrics("All Identifiers", methods,variables,classes,parameters,packages);
		String[] methodIdentifiers = evaluateMetrics("Method Identifiers",methods);
		String[] variableIdentifiers = evaluateMetrics("Variable Identifiers",variables);
		String[] classIdentifiers = evaluateMetrics("Class Identifiers",classes);
		String[] parameterIdentifiers = evaluateMetrics("Parameter Identifiers",parameters);
		String[] packageIdentifiers = evaluateMetrics("Package Identifiers",packages);
		return combineStringArrays(allIdentifiers,methodIdentifiers,variableIdentifiers,classIdentifiers,parameterIdentifiers,packageIdentifiers);
	}

	/**
	 * This returns a formatted string that presents the recommendations
	 * determined in the executeModule() function.
	 *
	 * @return a formatted string with recommendations
	 */
	public  String printMetrics()
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (String info : collectedInformation) {
			stringBuilder.append(info);
			stringBuilder.append(System.getProperty("line.separator"));
		}
		return stringBuilder.toString();
	}

	/**
	 * Combines a variable number of arrays into a single array.
	 * @param arraysToCombine 	Arrays that are to be combined into new array.
	 * @return 					Result of array combination.
	 */
	public String[] combineStringArrays(String[] ... arraysToCombine)
	{
		int newArraySize = 0;
		String[] combinedArray;
		for(String[] array: arraysToCombine)
		{
			newArraySize += array.length;
		}

		combinedArray = new String[newArraySize];
		int index = 0;
		for(String[] array : arraysToCombine)
		{
			for(int i = 0; i < array.length; i++,index++)
			{
				combinedArray[index] = array[i];
			}
		}
		return combinedArray;
	}

	/**
	 * Takes a variable number of lists and calculates the combined size of the lists
	 *
	 * @param identifiers 	A variable number of identifier lists
	 * @return 				total number of elements in lists
	 */
	public int  calculateNumberOfIdentifiers(ArrayList<String> ... identifiers)
	{
		int identifierCount = 0;
		for (ArrayList<String> identifierList : identifiers)
		{
			identifierCount += identifierList.size();
		}

		return identifierCount;
	}
	/**
	 * Calculates the average length of strings in multiple lists.
	 *
	 * @param identifierCount total number of strings in the lists.
	 * @param identifiers 	A variable number of identifier lists
	 * @return 				Average string size of given lists
	 */

	public double calculateAverageIdentifierLength(int identifierCount, ArrayList<String> ... identifiers)
	{
		double  average = 0;
		for(ArrayList<String> identifierList: identifiers)
		{
			for(String identifier : identifierList )
			{
				average += identifier.length();
			}
		}
		average = (identifierCount == 0 ) ? 0 :  average / identifierCount;
		return average;
	}

	/**
	 * Takes a variable number of lists containing strings and calculates the standard deviation in relation to string size
	 * @param identifierCount Total number of strings in the lists.
	 * @param average		Average string length of given lsits.
	 * @param identifiers 	A variable number of identifier lists
	 * @return 				total number of elements in lists
	 */

	public double calculateStandardDeviation( int identifierCount, double average, ArrayList<String> ... identifiers)
	{
		double standardDeviation;
		double difference;
		if(identifierCount != 0)
		{
			standardDeviation = 0;
			for (ArrayList<String> list : identifiers)
			{
				for (String identifier : list)
				{
					difference = identifier.length() - average;
					standardDeviation += difference * difference;
				}
			}
			standardDeviation = Math.sqrt(standardDeviation / (float) identifierCount);
		} else
		{
				standardDeviation = 0;
		}
		return standardDeviation;
	}

	/**
	 * Calculates the metrics of this module as well as generates appropriate messages based off results
	 * @param groupName		Name of the Identifier type (e.g. Class, Parameter, Method)
	 * @param identifiers	Identifiers within this group to be examined
	 * @return 				generated statistics for this group.
	 */

	public String[] evaluateMetrics(String groupName,ArrayList<String> ... identifiers )
	{
		NumberFormat formatter = new DecimalFormat("#0.00");
		int  identifierCount = calculateNumberOfIdentifiers(identifiers);
		double average = calculateAverageIdentifierLength(identifierCount,identifiers);
		double standardDeviation = calculateStandardDeviation(identifierCount,average,identifiers);

		String[] metrics =
				{
						"Category of identifier: " + groupName,
						"Number of identifiers: " + identifierCount,
						"Average identifier length: " + formatter.format(average),
						"Standard Deviation of identifiers: " + formatter.format(standardDeviation)
				};

		collectedInformation.add(groupName);
		collectedInformation.add("Number of identifiers: " + identifierCount);
		collectedInformation.add("Average identifier length: " + formatter.format(average));
		collectedInformation.add("Standard Deviation of identifiers: " + formatter.format(standardDeviation)+ "\n");

		if (average == 0)
		{
			collectedInformation.add(ZERO_AVERAGE_MESSAGE);
		}
		else if(average <= LOW_AVERAGE)
		{
			collectedInformation.add(LOW_AVERAGE_MESSAGE);
		} else if (average <= MEDIUM_AVERAGE)
		{
			collectedInformation.add(MEDIUM_AVERAGE_MESSAGE);
		} else if (average <= HIGH_AVERAGE)
		{
			collectedInformation.add(HIGH_AVERAGE_MESSAGE);
		} else if (average <= VERY_HIGH_AVERAGE)
		{
			collectedInformation.add(VERY_HIGH_AVERAGE_MESSAGE);
		}

		if (average == 0 )
		{
			// no need to add anything
		} else if (standardDeviation <= LOW_STANDARD_DEVIATION)
		{
			collectedInformation.add(LOW_STANDARD_DEVIATION_MESSAGE);
		} else if (standardDeviation <= MEDIUM_STANDARD_DEVIATION)
		{
			collectedInformation.add(MEDIUM_STANDARD_DEVIATION_MESSAGE);
		} else if (standardDeviation <= HIGH_STANDARD_DEVIATION)
		{
			collectedInformation.add(HIGH_STANDARD_DEVIATION_MESSAGE);
		}
		return metrics;
	}


	/**
	 *	Collects all variable identifiers in the compilation unit
	 */
	private static class VariableVisitor extends VoidVisitorAdapter<ArrayList<String>>
	{
		@Override
		public void  visit(VariableDeclarator n, ArrayList<String> arg)
		{
			if(arg == null)
			{
				arg = new ArrayList<>();
			}
			arg.add(n.getNameAsString());
			super.visit(n, arg);
		}
	}
	/**
	 *	Collects all variable identifiers in the compilation unit
	 */

	private static class MethodVisitor extends VoidVisitorAdapter<ArrayList<String>>
	{
		@Override
		public void  visit(MethodDeclaration n, ArrayList<String> arg)
		{
			if(arg == null)
			{
				arg = new ArrayList<>();
			}
			arg.add(n.getNameAsString());
			super.visit(n, arg);
		}
	}
	/**
	 *	Collects all class identifiers in the compilation unit
	 */

	private static class ClassVisitor extends VoidVisitorAdapter<ArrayList<String>>
	{
		@Override
		public void  visit(ClassOrInterfaceDeclaration n, ArrayList<String> arg)
		{
			if(arg == null)
			{
				arg = new ArrayList<>();
			}
			arg.add(n.getNameAsString());
			super.visit(n, arg);
		}
	}
	/**
	 *	Collects all parameter identifiers in the compilation unit
	 */

	private static class ParameterVisitor extends VoidVisitorAdapter<ArrayList<String>>
	{
		@Override
		public void  visit(Parameter n, ArrayList<String> arg)
		{
			if(arg == null)
			{
				arg = new ArrayList<>();
			}
			arg.add(n.getNameAsString());
			super.visit(n, arg);
		}
	}

	/**
	 *	Collects all package identifiers in the compilation unit
	 */
	private static class PackageVisitor extends VoidVisitorAdapter<ArrayList<String>>
	{
		@Override
		public void  visit(PackageDeclaration n, ArrayList<String> arg)
		{
			if(arg == null)
			{
				arg = new ArrayList<>();
			}
			arg.add(n.getNameAsString());
			super.visit(n, arg);
		}
	}
}