package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

import java.util.ArrayList;

public class UnmeetableCode implements ModuleInterface {

    @Override
    public String getName() {
        return "Unmeetable Code";
    }


    public void doesNothing() {
        if (true && true && true && false && true) {
            throw new StackOverflowError();
        }
        if (6 < 6) {

        }
    }

    @Override
    public String[] executeModule(SourceRoot sourceRoot) {
        ArrayList<CompilationUnit> units = (ArrayList<CompilationUnit>) sourceRoot.getCompilationUnits();
        ArrayList<String> conditions = new ArrayList<>();

        for (CompilationUnit unit : units) {
            unit.accept(new ConditionVisitor(), conditions);
        }
        return conditions.toArray(new String[conditions.size()]);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String printMetrics() {
        return null;
    }

    /**
     * Collects all package identifiers in the compilation unit
     */
    private static class ConditionVisitor extends VoidVisitorAdapter<ArrayList<String>> {

        public void findUnmeetableStatements(Statement n, ArrayList<String> arg) {
            Expression condition = extractConditionExpression(n);
            if (isUnmeetableCondition(condition)) {
                arg.add(n.getBegin().get().toString() + "," + n.findCompilationUnit().get().getStorage().get().getFileName());
            }
        }

        @Override
        public void visit(IfStmt n, ArrayList<String> arg) {
            findUnmeetableStatements(n, arg);
        }

        @Override
        public void visit(WhileStmt n, ArrayList<String> arg) {
            findUnmeetableStatements(n, arg);
        }

        @Override
        public void visit(ForStmt n, ArrayList<String> arg) {
            findUnmeetableStatements(n, arg);
        }

        public void visit(DoStmt n, ArrayList arg) {
            findUnmeetableStatements(n, arg);
        }

        public boolean isUnmeetableCondition(Expression condition) {
            boolean ret = false;
            if (condition.isBooleanLiteralExpr()) {
                if (condition.toString().equals("false")) {
                    ret = true;
                }
            } else if (condition.isBinaryExpr()) {
                BinaryExpr expression = (BinaryExpr) condition;
                ret = isUnmeetableBinaryExpression(expression);
            }
            return ret;
        }

        public boolean isUnmeetableBinaryExpression(BinaryExpr expr) {
            // && should actually be pretty simple, if theres a && false then we have dramas
            // || is similar check left and right to see if there are other || operators
            boolean ret = false;
            BinaryExpr.Operator test = BinaryExpr.Operator.LESS;
            if (expr.getOperator().equals(test)) {
                if (expr.getLeft().isIntegerLiteralExpr()) {
                    System.out.println("FOUND AN AND");
                    System.out.println(expr.getRight());

                    // ret = isUnmeetableBinaryExpression((BinaryExpr)expr.getLeft());
                } else if (expr.getLeft().isBooleanLiteralExpr()) {
                    System.out.println(expr.getLeft().asBooleanLiteralExpr().toString());
                }
                System.out.println("Hallelujeah! " + expr.toString());

            } else {
                //   System.out.println(  expr.toString());
            }
            return false;
        }

        public Expression extractConditionExpression(Statement n) {
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

