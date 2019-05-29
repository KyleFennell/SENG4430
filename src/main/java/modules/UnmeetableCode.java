package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import java.util.ArrayList;

/**
 * @Author(s): Callan Hampton
 * StudentNum: C3235869
 */
public class UnmeetableCode implements ModuleInterface {

    public static final String NO_ELEMENTS_FOUND = "No unmeetable conditions found",
            UNREACHABLE_CODE_FOUND = "unmeetable conditions were found at the following positions.";
    private static final String MODULE_NAME = "UnmeetableCode",
            MODULE_DESCRIPTION = "This module finds code that will potentially never run.\n " +
                    "It is a utility that is useful for finding missed debug Statements to disable output";
    private ArrayList<String> CollectedCodePositions;


    public UnmeetableCode() {
        CollectedCodePositions = new ArrayList<>();
    }

    /**
     * This returns the name of the module.
     *
     * @return Module name
     */
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    /**
     *  This examines the code for all conditions it can find that it deems unreachable.
     *  The positions of the code within their files are returned so that they can be located.
     *  e.g. if (false) and if(6 >7)
     * @param sourceRoot The parsed program to be analysed
     * @return positions of all unreachable code
     */
    @Override
    public String[] executeModule(SourceRoot sourceRoot) {
        ArrayList<CompilationUnit> units = (ArrayList<CompilationUnit>) sourceRoot.getCompilationUnits();

        for (CompilationUnit unit : units) {
            unit.accept(new ConditionVisitor(), CollectedCodePositions);
        }
        if (CollectedCodePositions.size() == 0) {
            CollectedCodePositions.add(NO_ELEMENTS_FOUND);
        } else {
            CollectedCodePositions.add(0, UNREACHABLE_CODE_FOUND);
        }

        return CollectedCodePositions.toArray(new String[0]);
    }

    /**
     * This returns the module description to be presented to the user.
     *
     * @return descrption of the module
     */
    @Override
    public String getDescription() {
        return MODULE_DESCRIPTION;
    }

    /**
     *  Returns a formatted string that contains all the relevant information gathered by the module.
     * @return a formatted string with recommendations
     */
    @Override
    public String printMetrics() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String info : CollectedCodePositions) {
            stringBuilder.append(info);
            stringBuilder.append(System.getProperty("line.separator"));
        }
        return stringBuilder.toString();

    }

    private static class ConditionVisitor extends VoidVisitorAdapter<ArrayList<String>> {

        /**
         * Examines a statement and determines if it has unmeetable conditions, any found will be recorded in the passed list
         *
         * @param n   The statement to be examined
         * @param arg The list to add output to
         */
        private void findUnmeetableStatements(Statement n, ArrayList<String> arg) {
            Expression condition = extractConditionExpression(n);
            if (isUnmeetableCondition(condition)) {
                if (n.getBegin().isPresent() && n.findCompilationUnit().isPresent()) {
                    if (n.findCompilationUnit().get().getStorage().isPresent()) {
                        arg.add(n.getBegin().get().toString() + "," + n.findCompilationUnit().get().getStorage().get().getFileName());
                    }
                }
            }
        }

        /**
         * Overloaded method that visits the if statements
         * @param  n The if statement to analyse
         * @param arg The list to add output to
         */
        @Override
        public void visit(IfStmt n, ArrayList<String> arg) {
            findUnmeetableStatements(n, arg);
            super.visit(n, arg);
        }

        /**
         * Overloaded method that visits the if statements
         * @param  n The while statement to analyse
         * @param arg The list to add output to
         */
        @Override
        public void visit(WhileStmt n, ArrayList<String> arg) {
            findUnmeetableStatements(n, arg);
            super.visit(n, arg);
        }

        /**
         * Overloaded method that visits the if statements
         * @param  n The if statement to analyse
         * @param arg The list to add output to
         */
        @Override
        public void visit(ForStmt n, ArrayList<String> arg) {
            findUnmeetableStatements(n, arg);
            super.visit(n, arg);
        }

        /**
         * Overloaded method that visits the if statements
         *
         * @param n   The if statement to analyse
         * @param arg The list to add output to
         */
        @Override
        public void visit(DoStmt n, ArrayList<String> arg) {
            findUnmeetableStatements(n, arg);
            super.visit(n, arg);
        }


        /**
         * Analyses the Condition expression to determine if the statement will always be false.
         * As it stands there are only a few met conditions are there are many cases to check.
         * As of now it checks for boolean literals e.g. (if(false)),
         * the && and || operators e.g (x > 7 && false || false)
         * and the  >= , > , <,<= operators e.g if(7 < 0)
         * In future this would be extended to include more cases
         *
         * @param expr The expression to examine
         * @return Returns whether the expression is satisfiable
         */
        private boolean isUnmeetableCondition(Expression expr) {
            boolean ret = false;
            if (expr.isBooleanLiteralExpr()) {
                if (expr.toString().equals("false")) {
                    ret = true;
                }
            } else if (expr.isBinaryExpr()) {
                BinaryExpr exprAsBinary = expr.asBinaryExpr();
                if (exprAsBinary.getOperator() == BinaryExpr.Operator.AND) {
                    if (exprAsBinary.getLeft().toString().contains("false") || exprAsBinary.getRight().toString().contains("false")) {
                        ret = true;
                    }
                } else if (exprAsBinary.getOperator() == BinaryExpr.Operator.OR) {
                    ret = isUnmeetableCondition(exprAsBinary.getLeft()) && isUnmeetableCondition(exprAsBinary.getRight());
                } else if (exprAsBinary.getOperator() == BinaryExpr.Operator.LESS) {
                    Expression left = exprAsBinary.getLeft();
                    Expression right = exprAsBinary.getRight();
                    if (left.isLiteralExpr() && right.isLiteralExpr()) {
                        int leftInt = left.asIntegerLiteralExpr().asInt();
                        int rightInt = right.asIntegerLiteralExpr().asInt();
                        if (leftInt >= rightInt) {
                            ret = true;
                        }
                    }
                } else if (exprAsBinary.getOperator() == BinaryExpr.Operator.GREATER) {
                    Expression left = exprAsBinary.getLeft();
                    Expression right = exprAsBinary.getRight();
                    if (left.isLiteralExpr() && right.isLiteralExpr()) {
                        int leftInt = left.asIntegerLiteralExpr().asInt();
                        int rightInt = right.asIntegerLiteralExpr().asInt();
                        if (leftInt <= rightInt) {
                            ret = true;
                        }
                    }
                } else if (exprAsBinary.getOperator() == BinaryExpr.Operator.GREATER_EQUALS) {
                    Expression left = exprAsBinary.getLeft();
                    Expression right = exprAsBinary.getRight();
                    if (left.isLiteralExpr() && right.isLiteralExpr()) {
                        int leftInt = left.asIntegerLiteralExpr().asInt();
                        int rightInt = right.asIntegerLiteralExpr().asInt();
                        if (leftInt < rightInt) {
                            ret = true;
                        }
                    }
                } else if (exprAsBinary.getOperator() == BinaryExpr.Operator.LESS_EQUALS) {
                    Expression left = exprAsBinary.getLeft();
                    Expression right = exprAsBinary.getRight();
                    if (left.isLiteralExpr() && right.isLiteralExpr()) {
                        int leftInt = left.asIntegerLiteralExpr().asInt();
                        int rightInt = right.asIntegerLiteralExpr().asInt();
                        if (leftInt > rightInt) {
                            ret = true;
                        }
                    }
                }

            }
            return ret;
        }

        /**
         * Extracts the conditional expression from a statement by first determining the type and then applying appropriate
         * Methods to extract the correct condition
         *
         * @param n The statement to extract
         * @return The extracted condition expression from the statement
         */
        private Expression extractConditionExpression(Statement n) {
            Expression condition = null;
            if (n.isExpressionStmt()) {
                if (n.asExpressionStmt().getExpression().isConditionalExpr()) {
                    condition = n.asExpressionStmt().getExpression().asConditionalExpr().getCondition();
                }
            } else if (n.isIfStmt()) {
                condition = n.asIfStmt().getCondition();
            } else if (n.isWhileStmt()) {
                condition = n.asWhileStmt().getCondition();
            } else if (n.isDoStmt()) {
                condition = n.asDoStmt().getCondition();
            } else if (n.isForStmt()) {
                if (n.asForStmt().getCompare().isPresent()) {
                    condition = n.asForStmt().getCompare().get();
                }
            }
            return condition;
        }
    }
}

