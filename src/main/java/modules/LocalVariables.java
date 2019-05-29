package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

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
