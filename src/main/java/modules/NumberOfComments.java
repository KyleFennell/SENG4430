package modules;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
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
	@Override
	public String printMetrics() {
		return null;
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
		// todoSubmetric();
		copyrightSubmetric();

		return new String[0];
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

		List<String> criteria = new ArrayList<>(Arrays.asList("todo", "to-do"));

		int todoFound = 0;
		int totalComments = 0;

		for (CompilationUnit unit : sourceRoot.getCompilationUnits()) {
			for (Comment com : unit.getAllContainedComments()) {
				if (criteria.parallelStream().anyMatch(com.getContent().toLowerCase()::contains)) {
					System.out.println(unit.getPrimaryTypeName().get() + "::"
											   + com.getRange().get().begin.line + " = \""
											   + com.getContent() + "\"");
					todoFound++;
				}
			}
			totalComments += unit.getAllContainedComments().size();
		}

		System.out.println(System.lineSeparator() + "Summary: ");
		System.out.println("\tComments found: " +  totalComments + System.lineSeparator() +"\tTODO found: " + todoFound);

		if (totalComments == 0) {
			return 1;
		} else if (IS_INDEPENDENT && todoFound > 0) {
			System.out.println("Recommendation: " + System.lineSeparator()
									   + "\tIndependently developed code will receive the optimal value for this sub metric, however, it is recommended that the above TODOs be implemented or removed.");
		}
		return 1 / ((Math.pow(todoFound, Math.E) / totalComments) + 1);



	}


	/**
	 * At no point in time can a class have more than a single copyright header.
	 * 		If a class does not contain a copyright header, then this is NOT considered erroneous.
	 * 		However, it WILL BE considered erroneous if “Compliance of Conformance” has the “consistency” flag enabled.
	 * 			This will also directly affect the final optimal value of that sub-module as well.
	 *
	 * @return
	 */
	private double copyrightSubmetric() {

		Predicate<Comment> isJavadoc = Comment::isJavadocComment;
		Predicate<Comment> isBlock = Comment::isBlockComment;
		List<FileCommentReport> fileCommentReports = new ArrayList<>();

		for (CompilationUnit unit : sourceRoot.getCompilationUnits()) {
			List<CommentReportEntry> comments = unit.getOrphanComments()
														.stream()
														.filter(isJavadoc.or(isBlock))
														.map(p -> new CommentReportEntry(p.getClass().getSimpleName(),
																						 p.getContent(),
																						 p.getRange().get().begin.line,
																						 !p.getCommentedNode().isPresent()))
														.collect(Collectors.toList());
			if (!comments.isEmpty())
				fileCommentReports.add(new FileCommentReport(unit.getPrimaryTypeName().get(),
															 unit.getPrimaryType().get().isClassOrInterfaceDeclaration(),
															 unit.getPrimaryType().get().getRange().get().begin.line,
															 comments));
		}

		fileCommentReports.forEach(System.out::println);

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



	/*
	These methods were developed with the idea of being able to analyse a file directly OR an entire root.
	At this point in time, they are redundant but I may develop further.
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



	private static class FileCommentReport {
		private String nodeName;
		private boolean isClassOrInterface;
		private int lineDeclarationOn;
		private List<CommentReportEntry> commentList;

		FileCommentReport(String nodeName, boolean isClassOrInterface, int lineDeclarationOn, List<CommentReportEntry> commentList) {
			this.nodeName = nodeName;
			this.isClassOrInterface = isClassOrInterface;
			this.lineDeclarationOn = lineDeclarationOn;
			this.commentList = commentList;
		}
		@Override
		public String toString() {
			return nodeName +
						   "{" +
						   System.lineSeparator() +
						   "isClassOrInterface=" + isClassOrInterface +
						   ", lineDeclarationOn=" + lineDeclarationOn +
						   ", commentList=" + commentList +
						   System.lineSeparator() +
						   '}';
		}
	}


	/*
	 * Book			: 	JavaParser: Visited
	 * Authors		: 	Nicholas Smith, Danny van Bruggen, and Federico Tomassetti
	 * Created		: 	May 2018
	 * Modified		: 	April 2019
	 * Link			:	https://github.com/javaparser/javaparser-visited
	 *
	 * @return
	 */
	private static class CommentReportEntry {
		private String type;
		private String text;
		private int lineNumber;
		private boolean isOrphan;

		CommentReportEntry(String type, String text, int lineNumber, boolean isOrphan) {
			this.type = type;
			this.text = text;
			this.lineNumber = lineNumber;
			this.isOrphan = isOrphan;
		}

		@Override
		public String toString() {
			return System.lineSeparator() +
						   "\t" +
						   "CommentReportEntry{" +
						   "type='" + type + '\'' +
						   ", lineNumber=" + lineNumber +
						   ", isOrphan=" + isOrphan +
						   '}';
		}
	}


}
