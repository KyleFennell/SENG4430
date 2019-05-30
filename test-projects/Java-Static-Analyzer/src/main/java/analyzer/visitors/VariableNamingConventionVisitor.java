package analyzer.visitors;

import analyzer.AbstractVoidVisitorAdapter;
import analyzer.Config;
import analyzer.collectors.Collector;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;

public class VariableNamingConventionVisitor extends AbstractVoidVisitorAdapter<Collector> {


    public static final String CLASS_NAME_REGEX = "([A-Z][a-z0-9]+)+";

    public static final String OK_REGEX = "/(([A-Z]+_[A-Z]*)+)/gx";

    protected String methodName;

    /**
     * Check if class is in camel case
     *
     * @param declaration
     * @param collector
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Collector collector) {


        if (Config.CAMEL_CASE_CLASS_NAME && declaration.getNameAsString().length() > 2 && !declaration.getNameAsString().matches(CLASS_NAME_REGEX)) {

            collector.addWarning(className, "Class name must be in CamelCase");

        }

        super.visit(declaration, collector);
    }


    /**
     * Check if method & method variables is in camelCase, not in underscore_case,
     * since in Java we use camelCase naming convention
     *
     * @param declaration
     * @param collector
     */
    @Override
    public void visit(MethodDeclaration declaration, Collector collector) {

        methodName = declaration.getNameAsString();

        if (Config.METHOD_IN_CAMEL_CASE) {
            if (methodName.contains("_")) {
                collector.addWarning(className, "Method \"" + methodName + "\"  should be in 'camelCase', not in 'underscore_case'");

            } else if (!methodName.matches("^[a-z][a-zA-Z0-9]*$")) {
                collector.addWarning(className, "Method \"" + methodName + "\"  should be in 'camelCase', not in 'underscore_case'");
            }
        }

        if (Config.PARAM_IN_CAMEL_CASE) {
            for (Parameter param: declaration.getParameters()) {

                if (param.getNameAsString().contains("_")) {

                    collector.addWarning(className, "Method \"" + methodName + "\" variable \"" + param.getName() +"\" should be in 'camelCase', not in 'underscore_case'");

                }

            }
        }


        super.visit(declaration, collector);

    }


    /**
     * Check if class variables is in camelCase, not in underscore_case,
     * since in Java we use camelCase naming convention
     *
     * @param declaration
     * @param collector
     */
    @Override
    public void visit(VariableDeclarationExpr declaration, Collector collector) {

        if (Config.PARAM_IN_CAMEL_CASE) {
            for (VariableDeclarator variable: declaration.getVariables()) {

                String name = variable.getNameAsString();

                if (name.matches(OK_REGEX)) // e.x. SOME_VARIABLE is OKAY
                    continue;


                if (name.contains("_")) {

                    collector.addWarning(className, "Method \"" + methodName + "\" variable \"" + name  +"\" should be in 'camelCase', not in 'underscore_case'");

                }

            }
        }

    }


}
