package modules;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.javaparser.GeneratedJavaParserConstants.SINGLE_LINE_COMMENT;

public class NumberOfComments implements ModuleInterface {

	private static final String ANALYSIS_ROOT = "resources/Example2";
	private static SourceRoot sourceRoot;
	private static Map<Path, String> classesAndLocation;

	private static boolean IS_INDEPENDENT = false;
	private static boolean CONFORMENCE = true;


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
	String[] executeModule() {
		return executeModule(sourceRoot);
	}
	@Override
	public String[] executeModule(SourceRoot sourceRoot) {
		if (!init())
			throw new IllegalStateException("Failed to initialise correctly. Wrong or empty folder.");

		System.out.println("Files being Analysed: ");
		printFilesAndLocation();

		// Begin analysing:
		// System.out.println(todoSubmetric());
		// System.out.println(copyrightSubmetric());
		System.out.println(consecutiveInlineSubmetric());

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
		int todoFound = 0;
		int totalComments = 0;
		List<String> criteria = new ArrayList<>(Arrays.asList("todo", "to-do", "fixme"));

		for (CompilationUnit unit : sourceRoot.getCompilationUnits()) {
			for (Comment com : unit.getAllContainedComments()) {
				if (criteria.parallelStream().anyMatch(com.getContent().toLowerCase()::contains))
					todoFound++;
			}
			totalComments += unit.getAllContainedComments().size();
		}

		if (totalComments == 0)
			return 1;
		return 1 / ((Math.pow(todoFound, Math.E) / totalComments) + 1);
	}


	/**
	 * At no point in time can a class have more than a single copyright header.
	 * If a class does not contain a copyright header, then this is considered neither negative nor positive.
	 * i.e. the optimal value for that file will be 0.5
	 * However, it WILL BE considered erroneous if “Compliance of Conformance” has the “consistency” flag enabled.
	 * <p>
	 * NOTE: The implementation only checks orphaned comments. i.e. comments that do not directly belong to a node
	 * within the AST. Therefore, any copyright comments in function headers, inline, etc. will not be counted.
	 * The reasoning behind this is to reduce false-positives.
	 *
	 * @return the optimal value for comments that fall under copyright headers
	 */
	private double copyrightSubmetric() {
		double sumOfFileScores = 0;
		List<FileCommentReport> fileCommentReports = new ArrayList<>();
		List<String> criteria = new ArrayList<>(Arrays.asList("copyright", "copy-right", "copy right"));

		for (CompilationUnit unit : sourceRoot.getCompilationUnits()) {
			int classDecLine = unit.getPrimaryType().get().getRange().get().begin.line;
			List<CommentReportEntry> comments = unit.getOrphanComments()
														.stream()
														.filter(p -> p.getRange().get().begin.line < classDecLine)
														.map(p -> new CommentReportEntry(p.getClass().getSimpleName(),
																						 p.getContent(),
																						 p.getRange().get().begin.line,
																						 !p.getCommentedNode().isPresent()))
														.collect(Collectors.toList());

			fileCommentReports.add(new FileCommentReport(unit.getPrimaryTypeName().get(),
														 unit.getPrimaryType().get().isClassOrInterfaceDeclaration(),
														 classDecLine,
														 comments));
		}

		// TODO gives a 0 for files that contain no copyright headers. Make a decision on whenever it should be flat 0 or 0.5
		for (FileCommentReport file : fileCommentReports) {
			// double fileScore = CONFORMENCE ? 0.5 : 0;
			double fileScore = 0;
			int commentsChecked = 0;
			int copyrightsFound = 0;

			for (CommentReportEntry comment : file.commentList) {
				commentsChecked++;
				if (criteria.parallelStream().anyMatch(comment.text.toLowerCase()::contains)) {
					copyrightsFound++;
					if (comment.type.equals("BlockComment") && (copyrightsFound == 1 && commentsChecked == 1)) {
						fileScore = 1;
					}
				}
			}
			if (copyrightsFound > 1)
				fileScore = 0;
			sumOfFileScores += fileScore;
		}
		return sumOfFileScores / fileCommentReports.size();
	}


	/**
	 * Attempts to find inline comments (i.e comments beginning with "//") that precede one another and will update
	 * the <code>optimalValue</code> based on how many consecutive inline comments exists per comment per file. <p>
	 * The <code>optimalValue</code> will begin by assuming all comments are correctly formatted.
	 * As consecutive inline comments are found, the value will update in a negative manner; i.e approaches 0. <p>
	 * The reasoning behind this is because most text-editors allow all other comment types to be automatically folded,
	 * where-as in-line comments are not.
	 * 		For example: Intellij will fold inline comments, but Notepad++ does not.
	 * This allows developers to focus solely on code after documentation is no longer needed. Similarly, will result
	 * in faster navigation of the codebase. <p>
	 * For example:
	 * <pre>
	 *     // Comment 1
	 *     // Comment 2
	 *
	 *     // Comment 3
	 * </pre>
	 * Should be formatted as:
	 * <pre>
	 *     /*
	 *     Comment 1
	 *     Comment 2
	 *     Comment 3
	 *     *&#47;
	 * </pre>
	 *
	 * @return the <code>optimalValue</code> between the range [0-1]
	 */
	private double consecutiveInlineSubmetric() {

		List<Deque> summary = new ArrayList<>();
		int totalComments = 0;
		int totalInline = 0;

		for (CompilationUnit unit : sourceRoot.getCompilationUnits()) {

			totalComments += unit.getAllContainedComments().size();
			JavaToken currentToken = unit.getTokenRange().get().getBegin();
			Deque<JavaToken> mergeList = new LinkedList<>();

			// TODO find out if there is a method of finding ONLY comment tokens. The current implementation iterates over all Java code; that is not relevant to this sub-module.
			while (currentToken.getNextToken().isPresent()) {
				if (currentToken.getKind() == SINGLE_LINE_COMMENT) {
					totalInline++;
					if (mergeList.isEmpty()) {
						mergeList.addFirst(currentToken);
					} else if (mergeList.peek().getRange().get().begin.column != currentToken.getRange().get().begin.column) {
						mergeList = new LinkedList<>();
					} else {
						mergeList.offer(currentToken);
					}
				}
				else if (!mergeList.isEmpty() && currentToken.getCategory().isWhitespace()) {
					mergeList.offer(currentToken);
				}
				else if (!mergeList.isEmpty()) {

					while (mergeList.peekLast() != null) {
						if (mergeList.peekLast().getCategory().isWhitespace()) {
							mergeList.removeLast();
						} else {
							if (mergeList.size() >= 3)
								summary.add(mergeList);
							mergeList = new LinkedList<>();
						}
					}

				}
				currentToken = currentToken.getNextToken().get();
			}
		}

		int needsRefactoring = 0;

		for (Deque<JavaToken> d : summary) {
			System.out.println("--- START MERGE ---");
			while (!d.isEmpty()) {
				System.out.println(d.pop().getText().trim());
				needsRefactoring++;
			}
			System.out.println("--- END MERGE ---" + System.lineSeparator());
		}
		System.out.println("Summary: ");
		System.out.println("Total comments: " + totalComments);
		System.out.println("Total line comments: " + totalInline);
		System.out.println("Total lines that need refactoring: " + needsRefactoring);

		// TODO figure out relevant equation
		return 0.0;
	}

	private double TrivialAndUnnecessarySubmetric() {
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
		classesAndLocation.forEach((k, v) -> System.out.println("\t * " + v + ": " + k));
	}


	private static class FileCommentReport {

		private String nodeName;
		private boolean isClassOrInterface;
		private boolean isCopyrightValid;
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
			return "\tLine:" + lineNumber + " " + type;
		}

	}


}
