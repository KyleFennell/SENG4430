package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;
import modules.helpers.Analysis;
import modules.helpers.FileReport;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocalVariables implements ModuleInterface {

	@Override
	public String getName() {
		return "LocalVariables";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String printMetrics() {
		return null;
	}

	public String[] executeModule() {
		final String ANALYSIS_ROOT = "resources/Example2";
		SourceRoot sourceRoot = new SourceRoot(Paths.get(ANALYSIS_ROOT));
		return executeModule(sourceRoot);

	}
	@Override
	public String[] executeModule(SourceRoot sourceRoot) {

		List<FileReport> results = new ArrayList<>();

		try {
			sourceRoot.tryToParse();
			for (CompilationUnit unit : sourceRoot.getCompilationUnits()) {
				System.out.println(unit.getPrimaryTypeName().get());
				System.out.println(methodAndVariablesToString(getMethodVariableMap(unit)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String[0];
	}


	private Analysis findSameLineVariables(Map<String, List<VariableDeclarationExpr>> methodVariableMap) {
		Analysis fileAnal = new Analysis();
		int totalVariables = 0;
		int erroneousVariables = 0;

		for (Map.Entry<String, List<VariableDeclarationExpr>> entry : methodVariableMap.entrySet()) {
			String k = entry.getKey();
			List<VariableDeclarationExpr> v = entry.getValue();

			for (VariableDeclarationExpr expr : v) {
				if (expr.getVariables().size() > 1) {
					fileAnal.addWarning(
							k + "(): " + expr,
							"Multiple variables declared on one line are difficult to read; Refactor.",
							v.get(0).getRange().get());
					erroneousVariables += expr.getVariables().size();
				}
				totalVariables++;
			}
		}

		if (totalVariables != 0) fileAnal.setOptimalValue(1 - ((double) erroneousVariables / totalVariables));
		return fileAnal;
	}



	private Map<String, List<VariableDeclarationExpr>> getMethodVariableMap(CompilationUnit unit) {
		Map<String, List<VariableDeclarationExpr>> methodNames = new LinkedHashMap<>();
		VoidVisitor<Map<String, List<VariableDeclarationExpr>>> methodNameCollector = new MethodNameCollector();
		methodNameCollector.visit(unit, methodNames);
		return methodNames;
	}



	private String methodAndVariablesToString(Map<String, List<VariableDeclarationExpr>> methodNames) {
		final String NL = System.lineSeparator();
		StringBuilder sb = new StringBuilder();

		methodNames.forEach((k, v) -> {
			sb.append("\t").append(k).append("(): ");
			if (v.isEmpty())
				sb.append("[No Local Variables]");
			sb.append(NL);

			for (VariableDeclarationExpr expr : v) {
				for (VariableDeclarator variable : expr.getVariables())
					sb.append("\t\t").append(variable.getTypeAsString()).append(" ")
					  .append(variable.getNameAsString()).append(NL);
			}
		});
		return sb.toString();
	}



	private static class MethodNameCollector extends VoidVisitorAdapter< Map<String, List<VariableDeclarationExpr>> > {
		@Override
		public void visit(MethodDeclaration md, Map<String, List<VariableDeclarationExpr>> methodCollector) {
			super.visit(md, methodCollector);

			// Retrieving the variables from this current method being visited
			List<VariableDeclarationExpr> variablesFromMethod = new ArrayList<>();
			VoidVisitor<List<VariableDeclarationExpr>> methodNameVisitor = new MethodVariablesCollector();
			methodNameVisitor.visit(md, variablesFromMethod);

			// Adding the (Method, List<Variable>) pair to a map
			methodCollector.put(md.getNameAsString(), variablesFromMethod);
		}
	}


	private static class MethodVariablesCollector extends VoidVisitorAdapter< List<VariableDeclarationExpr> > {
		@Override
		public void visit(VariableDeclarationExpr n, List<VariableDeclarationExpr> variableCollector) {
			variableCollector.add(n);
		}
	}




}
