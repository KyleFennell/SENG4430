package modules;

import com.github.javaparser.JavaToken;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;
import com.mitchtalmadge.asciidata.table.ASCIITable;
import com.mitchtalmadge.asciidata.table.formats.ASCIITableFormat;
import modules.helpers.Warning;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.github.javaparser.GeneratedJavaParserConstants.SINGLE_LINE_COMMENT;


public class NumberOfComments implements ModuleInterface {

	private static final String ANALYSIS_ROOT = "resources/Example2";
	private List<FileReport> results;


	@Override
	public String getName() {
		return "NumberOfComments";
	}


	@Override
	public String getDescription() {
		return "This module has been split into the following sub-modules:\n" +
					   "\t - To-do\n" +
					   "\t - Copyright\n" +
					   "\t - Consecutive Line Comment Segmentation\n" +
					   "Each analysis will factor in several different ‘what if’ conditions and edge cases to " +
					   "return a single optimal value";
	}


	String[] executeModule() {
		SourceRoot sourceRoot = new SourceRoot(Paths.get(ANALYSIS_ROOT));
		return executeModule(sourceRoot);
	}
	@Override
	public String[] executeModule(SourceRoot sourceRoot) {
		try {
			sourceRoot.tryToParse();
			results = new ArrayList<>();
			for (CompilationUnit unit : sourceRoot.getCompilationUnits())
				results.add(analyse(unit));
			System.out.println(printMetricsTable(results));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String[] { printMetricsTable(results) };
	}


	@Override
	public String printMetrics() {
		return printMetricsTable(results);
	}


	/**
	 * Using the library ASCIITable, developed by Mitch Talmadge, this function will output the following in tabular form:
	 * Each {@code Analysis} object in the parameter {@code List<FileReport>} will act a column header.
	 * Each file that was analysed will be the row header containing each file's {@code optimalValue} between 0-1;
	 * rounded to 2 decimal places.
	 *
	 * It should be noted that at this current point in time, each column header will need to be explicitly typed here.
	 * Similarly,the {@code headers} size must match the size of each {@code res.analyses.length},
	 * otherwise an Exception will be thrown by ASCIITable. TODO: Helper that does this automatically.
	 *
	 * @param res A List of FileReport objects that were analysed via the {@code analyse()} function.
	 * @return The entire table containing analysis results as a String
	 */
	private String printMetricsTable(List<FileReport> res) {
		String[] headers = { "Class Name", "todo", "copyright", "l_commentSeg", "javadoc"};
		String[][] data = new String[res.size()][];

		for (int i = 0; i < res.size(); i++) {
			FileReport f = res.get(i);
			String[] row = new String[f.analyses.length+1];
			row[0] = f.fileName;

			// Collecting each file's analysis result. i.e. 'to-do, copyright, l_commentSeg'
			for (int j = 0; j < f.analyses.length; j++) {
				row[j+1] = String.format("%.2f", (f.analyses[j].optimalValue));
			}
			data[i] = row; // Add row to table
		}
		return ASCIITable.fromData(headers, data).withTableFormat(new ASCIITableFormat()).toString();
	}


	/**
	 * Needed due to the current structure of implementation.
	 * Add functions that return Analysis objects to the fileReport.analyses[] array that you wish to have associated
	 * to each file.
	 *
	 * @param unit The CompilationUnit that will be analysed with each function from the current array.
	 * @return A wrapper that contains: analysis results, optimalValue, file name, file path for each CompilationUnit
	 * @see FileReport
	 */
	private FileReport analyse(CompilationUnit unit) {
		FileReport fileReport = new FileReport(unit.getPrimaryTypeName().get(), unit.getStorage().get().getPath());
		fileReport.analyses = new Analysis[] {
				analyseTodo(unit),
				analyseCopyright(unit),
				analyseLineCommentSegmentation(unit),
				analyseJavadoc(unit)
		};
		return fileReport;
	}


	/**
	 * Using a visitor, extract each {@code unit}'s Methods, and determines if a Javadoc exists for it. Will add
	 * a warning to the returning @{code Analysis} if one does not exist.
	 *
	 * @param unit The compilationUnit that will be analysed
	 * @return An analysis file containing the {@code unit}'s optimalValue = {@code methodsWithJavaDoc / totalMethods}
	 */
	private Analysis analyseJavadoc(CompilationUnit unit) {
		Analysis fileAnal = new Analysis();
		Map<NameExpr, Boolean> methodDocMap = new HashMap<>();
		VoidVisitor<Map<NameExpr, Boolean>> methodNameCollector = new MethodNameCollector();
		methodNameCollector.visit(unit, methodDocMap);

		int methodsConforming = 0;
		for (Map.Entry<NameExpr, Boolean> entry : methodDocMap.entrySet()) {
			NameExpr key = entry.getKey();
			Boolean value = entry.getValue();
			if (value) methodsConforming++;
			else fileAnal.warnings.add(new Warning<>(
					"No Javadoc associated with " + unit.getStorage().get().getFileName() + "::"+ key.getNameAsString(),
					"Add Javadoc or move if whitespace exists between method.",
					key.getRange().get()));
		}

		if (!methodDocMap.isEmpty())
			fileAnal.optimalValue = (double) methodsConforming / methodDocMap.size();
		return fileAnal;
	}
	private static class MethodNameCollector extends VoidVisitorAdapter<Map<NameExpr, Boolean>> {
		@Override
		public void visit(MethodDeclaration md, Map<NameExpr, Boolean> collector) {
			super.visit(md, collector);
			collector.put(md.getNameAsExpression(), md.getComment().filter(Comment::isJavadocComment).isPresent());
		}
	}


	/**
	 * OLD DOC: WILL UPDATE IF IS_INDEPENDENT CONFIGURATION ISN'T IMPLEMENTED:
	 *      The following is for projects developed within a collaborate environment; which is enabled by default.
	 *      For independent projects, TO-DO comments will be treated as an inline or block comment. Therefore, this
	 *      sub-module will always return the most optimal value when the project has been marked as independent.
	 *      However, a warning will be issued stating there exists a TO-DO and it should be implemented or removed.
	 *
	 *      Old Formula: analysis.optimalValue = 1 / (Math.pow(todoFound, Math.E) + 1); <- Scales too fast
	 *
	 * @param unit The compilationUnit that will be analysed
	 * @return Analysis object with optimalValue = 1 if no to-do comments were found, otherwise: 0.5 * (0.5 / todoFound)
	 */
	private Analysis analyseTodo(CompilationUnit unit) {
		Analysis analysis = new Analysis();
		List<String> criteria = new ArrayList<>(Arrays.asList("todo", "to-do", "fixme"));
		int totalComments = unit.getAllContainedComments().size();
		int todoFound = 0;

		for (Comment c : unit.getAllContainedComments()) {
			if (criteria.stream().anyMatch(c.getContent().toLowerCase()::contains)) {
				todoFound++;
				analysis.warnings.add(new Warning<>("TODO found.", "Implement or remove TODO.", c.getRange().get()));
			}
		}

		if (todoFound == 0) 	analysis.optimalValue = 1;
		else 					analysis.optimalValue = 0.5 * (0.5 / todoFound);

		// Scope creep
		// analysis.addVariable("todoFound", todoFound);
		// analysis.addVariable("totalComments", totalComments);

		return analysis;
	}


	/**
	 * At no point in time can a class have more than a single copyright HEADER.
	 * If a class does not contain a copyright header, then this is considered neither negative nor positive.
	 * i.e. the optimal value for that file will be 0.5
	 *
	 * WARNING: This function will check all comments for Copyright information. This may result in false positives.
	 *
	 * @param unit The compilationUnit that will be analysed
	 * @return Analysis object with optimalValue =
	 * 		0.5 If no header found,
	 * 		1 * inlineCopyrights / copyrightsFound,
	 * 		1 - totalCopyrightsFound / TotalComments
	 */
	private Analysis analyseCopyright(CompilationUnit unit) {
		List<String> criteria = new ArrayList<>(Arrays.asList("copyright", "copy-right", "copy right", "license"));
		Analysis analysis = new Analysis();

		double weight = 0.5;
		int copyrightsFound = 0;
		int inline = 0;
		int comments = 0;
		boolean headerIsCorrectType = false;

		for (Comment c : unit.getAllContainedComments()) {
			if (criteria.stream().anyMatch(c.getContent().toLowerCase()::contains)) {
				if (copyrightsFound == 0 && comments == 0) {
					if (c.isBlockComment()) {
						headerIsCorrectType = true;
					}
				} else {
					analysis.warnings.add(new Warning<>("Found block copyright comment but header already exists.", "Add to Javadoc or remove.", c.getRange().get()));
				}
				if (c.isLineComment()) {
					analysis.warnings.add(new Warning<>("Copyright declared as line comment.", "Copyrights declared inline are difficult to see and/or useless within the current context. Move to file header or if authoring method, add to javaDoc with the @author annotation.", c.getRange().get()));
					inline++;
				}
				copyrightsFound++;
			}
			comments++;
		}

		// Need to think about these some more.
		if (headerIsCorrectType) 	weight = 1;
		if (inline != 0) 			weight *= 1 - (inline / (double) copyrightsFound);
		if (copyrightsFound > 1) 	weight *= 1 - copyrightsFound/(double)comments;

		analysis.optimalValue = weight;
		return analysis;
	}


	/**
	 * NOTE: This function is expensive as it iterates over every token in a file.
	 *
	 * Attempts to find inline comments (i.e comments beginning with "//") that precede one another.
	 *
	 * The optimal value will begin by assuming all comments are correctly formatted.
	 * As consecutive inline comments are found, the value will update in a negative manner; i.e approaches 0. <p>
	 * The reasoning behind this is because most text-editors allow all other comment types to be automatically folded,
	 * where-as in-line comments are not. For example: Intellij will fold inline comments, but Notepad++ does not.
	 * This allows developers to focus solely on code and increase navigation within the codebase after documentation
	 * is no longer needed.
	 *
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
	 * <p>
	 *
	 * @param unit The compilationUnit that will be analysed
	 * @return Analysis object with optimalValue =
	 *      1 if line comment segmentation is present, otherwise:
	 *      1 - totalLineCommentsSegmented / totalComments
	 *
	 */
	private Analysis analyseLineCommentSegmentation(CompilationUnit unit) {

		long totalCom = unit.getAllContainedComments().stream().filter(Comment::isLineComment).count();
		int inline = 0;

		JavaToken j = unit.getTokenRange().get().getBegin();
		Deque<JavaToken> err = new LinkedList<>();
		Analysis fileReport = new Analysis();

		while (j.getNextToken().isPresent()) {
			if (j.getKind() == SINGLE_LINE_COMMENT) {
				if (!err.isEmpty() && isSameColumn(j, err.peek())) {
					err = new LinkedList<>();
				}
				err.offer(j);
			} else if (!err.isEmpty() && !j.getCategory().isWhitespace()) {
				if (err.size() >= 2) {
					inline += err.size();
					fileReport.warnings.add(dequeToWarning(err, j.getRange().get()));
				} else {
					err = new LinkedList<>();
				}
			}
			j = j.getNextToken().get();
		}

		if (fileReport.warnings.isEmpty()) 	fileReport.optimalValue = 1;
		else 								fileReport.optimalValue = 1 - (inline / (double) totalCom);
		return fileReport;
	}



	private boolean isSameColumn(JavaToken t1, JavaToken t2) {
		return t1.getRange().get().begin.column != t2.getRange().get().begin.column;
	}



	private Warning<String, String, Range> dequeToWarning(Deque<JavaToken> warning, Range range) {
		final String NL = "\n";
		StringBuilder og = new StringBuilder();
		StringBuilder fo = new StringBuilder();

		fo.append("/*").append(NL);
		while (!warning.isEmpty()) {
			String commentText = warning.pop().getText().trim().replace("\t", "    ");
			og.append(commentText).append(NL);
			fo.append(commentText.replace("//", "")).append(NL);
		}
		fo.append("*/").append(NL);

		return new Warning<>(og.toString(), fo.toString(), range);
	}



	private class FileReport {
		private final String fileName;
		private final Path fileLocation;
		private Analysis[] analyses;

		FileReport(String fileName, Path filePath) {
			this.fileName = fileName;
			this.fileLocation = filePath;
		}
	}


	private class Analysis {
		private List<Warning> warnings;
		private double optimalValue;

		Analysis() {
			warnings = new ArrayList<>();
		}

		String asPercentage() {
			return String.format("%.2f", optimalValue*100) + "%";
		}

		String printWarnings() {
			if (!warnings.isEmpty()) {
				String[] headers = { "Line", "Cause", "Fix" };
				String[][] data = new String[warnings.size()][];

				for (int i = 0; i < warnings.size(); i++) {
					Warning w = warnings.get(i);
					String[] row = { String.valueOf(w.lineOrigin.begin.line), w.cause.toString(), w.recommendedFix.toString() };
					data[i] = row;
				}

				return ASCIITable.fromData(headers, data).withTableFormat(new ASCIITableFormat()).toString();
			}
			return null;
		}
	}

}
