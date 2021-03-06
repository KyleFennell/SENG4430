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
import modules.helpers.Analysis;
import modules.helpers.FileReport;
import modules.helpers.TableUtil;
import modules.helpers.Warning;

import java.io.IOException;
import java.util.*;

import static com.github.javaparser.GeneratedJavaParserConstants.SINGLE_LINE_COMMENT;

/**
 * Project          :   Software Quality Assignment
 * Class name       :   NumberOfComments.java
 * Author(s)        :   John Barr
 * Date Created     :   2/05/19
 * Purpose          :   This class is used to analyse sub-classes of comments within a project.
 *                      Based on the results of each analysis, warnings will be served and solutions to mitigate
 *                      said warnings will be recommended. Due to the lack of a specifications when it comes to
 *                      comments, this module is more about conforming to one consistent style rather than following
 *                      a correct standard.
 */
public class NumberOfComments implements ModuleInterface {
	private List<FileReport> results;
	private int totalComments = 0;

	@Override
	public String getName() { return "NumberOfComments"; }

	@Override
	public String getDescription() {
		return "This module has been split into the following sub-modules:\n" +
					   "\t - To-do\n" +
					   "\t - Copyright\n" +
					   "\t - Consecutive Line Comment Segmentation\n" +
					   "Each analysis will factor in several different ‘what if’ conditions and edge cases to " +
					   "return a single optimal value";
	}

	@Override
	public String printMetrics() {
		String[] headers = { "Class Name", "Comments", "todo_OV", "copyright_OV", "l_commentSeg_OV", "javadoc_OV", "Total File Score:" };
		return "Breakdown: " + System.lineSeparator()
				       + TableUtil.metricTablePrint(results, headers) + System.lineSeparator()
				       + "Warnings: " + System.lineSeparator()
				       + TableUtil.printWarningsTable(results);
	}



	private String[] moduleOutput(List<FileReport> results) {
		double[] averageSubMetrics = new double[4];
		int totalFiles = results.size();
		double finalScore = 0.0;

		for (FileReport file : results) {
			finalScore += file.calcFileScore();
			for (int i = 0; i < file.getAnalyses().length; i++)
				averageSubMetrics[i] += file.getAnalyses()[i].getOptimalValue();
		}

		for (int i = 0; i < averageSubMetrics.length; i++)
			averageSubMetrics[i] = averageSubMetrics[i] / (double) totalFiles;

		return new String[] {
				getName(),
				"totalFiles: "          + totalFiles,
				"totalComments: "       + totalComments,
				"AverageTodo: "         + String.format("%.2f", averageSubMetrics[0]),
				"AverageCopyRight: "    + String.format("%.2f", averageSubMetrics[1]),
				"AverageCommentSeg: "   + String.format("%.2f", averageSubMetrics[2]),
				"AverageJavadoc: "      + String.format("%.2f", averageSubMetrics[3]),
				"Total Score: "         + String.format("%.2f", ((finalScore / totalFiles) * 100.0)) + "%"
		};
	}


	@Override
	public String[] executeModule(SourceRoot sourceRoot) {
		try {
			sourceRoot.tryToParse();
			results = new ArrayList<>();
			for (CompilationUnit unit : sourceRoot.getCompilationUnits())
				results.add(analyse(unit));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return moduleOutput(results);
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
		int commentCount = getCommentCount(unit);
		totalComments += commentCount;

		fileReport.setSum(commentCount);
		fileReport.setAnalyses(new Analysis[] {
				analyseTodo(unit),
				analyseCopyright(unit),
				analyseLineCommentSegmentation(unit),
				analyseJavadoc(unit)

		});
		return fileReport;
	}



	private int getCommentCount(CompilationUnit unit) {
		return unit.getAllContainedComments().size();
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
			else fileAnal.addWarning(
					"No Javadoc associated with " + unit.getPrimaryTypeName().get() + "::"+ key.getNameAsString(),
					"Add Javadoc or move if whitespace exists between method.",
					key.getRange().get());
		}

		if (!methodDocMap.isEmpty())
			fileAnal.setOptimalValue((double) methodsConforming / methodDocMap.size());
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
		int todoFound = 0;

		for (Comment c : unit.getAllContainedComments()) {
			if (criteria.stream().anyMatch(c.getContent().toLowerCase()::contains)) {
				todoFound++;
				analysis.addWarning("TODO found.", "Implement or remove TODO.", c.getRange().get());
			}
		}

		if (todoFound == 0) 	analysis.setOptimalValue(1);
		else 					analysis.setOptimalValue(0.5 * (0.5 / todoFound));

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
					analysis.addWarning("Found block copyright comment but header already exists.", "Add to Javadoc or remove.", c.getRange().get());
				}
				if (c.isLineComment()) {
					analysis.addWarning("Copyright declared as line comment.",
							"Copyrights declared inline are difficult to see and/or useless within the current context. Move to file header or if authoring method, add to javaDoc with the @author annotation.",
							c.getRange().get());
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

		analysis.setOptimalValue(weight);
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
					fileReport.addWarning( dequeToWarning(err, j.getRange().get()) );
				} else {
					err = new LinkedList<>();
				}
			}
			j = j.getNextToken().get();
		}

		if (fileReport.getWarnings().isEmpty()) 	fileReport.setOptimalValue(1);
		else 								        fileReport.setOptimalValue(1 - (inline / (double) totalCom));
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


}
