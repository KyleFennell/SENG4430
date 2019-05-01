package modules;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NumberOfComments implements ModuleInterface {

	private static final String ANALYSIS_ROOT = "resources/Example1";
	private static SourceRoot sourceRoot;
	private static Map<Path, String> classesAndLocation;

	private static boolean IS_INDEPENDENT = false;


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
		return executeModule(sourceRoot);
	}
	@Override
	public String[] executeModule(SourceRoot sourceRoot) {

		if (init()) {
			System.out.println("Files being Analysed: ");
			printFilesAndLocation();
			System.out.println(System.lineSeparator());
		} else {
			throw new IllegalStateException("Failed to initialise correctly. Wrong or empty folder.");
		}

		// Begin analysing:
		todoSubmetric();


		return new String[0];
	}

	/*
		The methods here were developed with the idea of being able to analyse a file directly OR an entire root.
		At this point in time, they are redundant but may add that extra function if decided to be implemented but I
		will still override here just to show it works.
	 */
	private static boolean init() {
		return init(ANALYSIS_ROOT);
	}
	private static boolean init(String folderLocation) {
		sourceRoot = new SourceRoot(Paths.get(folderLocation));
		try {
			List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
			return getNameLocationMap(parseResults
											  .stream().filter(ParseResult::isSuccessful)
											  .map(r -> r.getResult().get())
											  .collect(Collectors.toList())
									 );
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	private static boolean getNameLocationMap(List<CompilationUnit> cu) {
		classesAndLocation = new HashMap<>();
		for (CompilationUnit compilationUnit : cu)
			classesAndLocation.put(compilationUnit.getStorage().get().getPath(), compilationUnit.getStorage().get().getFileName());
		return !classesAndLocation.isEmpty();
	}
	private static void printFilesAndLocation() {
		classesAndLocation.forEach((k, v) -> System.out.println("\t * " + v + System.lineSeparator() + "\t\t ^ " + k));
	}

	/**
	 * The following rationale is for projects developed within a collaborate environment; which is enabled by default.
	 * For independent projects, TO-DO comments will be treated as an inline or block comment. Therefore, this
	 * sub-module will always return the most optimal value when the project has been marked as independent.
	 * However, a warning will be issued stating there exists a TO-DO and it should be implemented or removed.
	 *
	 * @return the optimal value of comments that fall under the sub-class of TO-DO
	 */
	private double todoSubmetric() {
		if (IS_INDEPENDENT)
			return 1; // TODO (ironic) Print location and recommend that TODO should be implemented

		List<Comment> allComments = new ArrayList<>();

		for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {
			allComments.addAll(cu.getAllContainedComments());
		}
		// System.out.println(allComments.toString());



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
