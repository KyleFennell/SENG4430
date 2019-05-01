package modules;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NumberOfComments implements ModuleInterface {

	private static final String ANALYSIS_ROOT = "C:/Users/PapaCache/Desktop/SENG4430/MultipleFiles/ttClasses/src";
	private static SourceRoot sourceRoot;
	private static List<CompilationUnit> compilationUnits;

	@Override
	public String getName() {
		return "Number of Comments";
	}
	@Override
	public String getDescription() {
		return "This module has been split into the following sub-modules:\n" +
					   "\t - To-do\n" +
					   "\t - Copyright\n" +
					   "\t - Trivial and Unnecessary\n" +
					   "\t - Surrounded By\n" +
					   "\t - Compliance of Conformance\n" +
					   "\n" +
					   "Each analysis will factor in several different ‘what if’ conditions and edge cases to " +
					   "return a single optimal value";
	}

	// ! OVERLOADED FOR DRIVER CLASS
	public String[] executeModule() {
		executeModule(sourceRoot);
		return null;
	}

	@Override
	public String[] executeModule(SourceRoot sourceRoot) {

		boolean isInitialised = init();
		if (isInitialised) {
			System.out.println("Files being Analysed: ");
			printFilesAndLocation(compilationUnits);
			System.out.println(System.lineSeparator());
		} else
			throw new IllegalStateException("Failed to initialise correctly. Incorrect or empty location.");


		return new String[0];
	}


	private boolean init() {
		sourceRoot = new SourceRoot(Paths.get(ANALYSIS_ROOT));
		try {
			List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
			compilationUnits = parseResults
									   .stream().filter(ParseResult::isSuccessful)
									   .map(r -> r.getResult().get())
									   .collect(Collectors.toList());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static Map<Path, String> getNameLocationMap(List<CompilationUnit> cu) {
		Map<Path, String> classAndLocation = new HashMap<>();
		for (CompilationUnit compilationUnit : cu)
			classAndLocation.put(compilationUnit.getStorage().get().getPath(), compilationUnit.getStorage().get().getFileName());
		return classAndLocation;
	}

	private static void printFilesAndLocation(List<CompilationUnit> compilationUnits) {
		getNameLocationMap(compilationUnits).forEach((k, v) -> System.out.println("\t * " + v + System.lineSeparator() + "\t\t ^ " + k));
	}

	/**
	 * The following rationale is for projects developed within a collaborate environment; which is enabled by default.
	 * For independent projects, TO-DO comments will be treated as an inline or block comment. Therefore, this
	 * sub-module will always return the most optimal value when the project has been marked as independent.
	 * However, a warning will be issued stating there exists a TO-DO and it should be implemented or removed.
	 *
	 * @return the optimal value of comments that fall under the sub-class of TO-DO
	 */
	private double todoSubmetric(SourceRoot sourceRoot) {



		return 0.0;
	}



	private double copyrightSubmetric() {
		return 0.0;
	}
	private double TrivialAndUnnecessarySubmetric() {
		return 0.0;
	}
	private double SurroundedBySubmetric() {
		return 0.0;
	}
	private double ComplianceOfConformanceSubmetric() {
		return 0.0;
	}

	@Override
	public String printMetrics() {
		return null;
	}

}
