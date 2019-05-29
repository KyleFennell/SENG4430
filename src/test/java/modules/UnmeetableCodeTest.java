package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static modules.UnmeetableCode.NO_ELEMENTS_FOUND;
import static modules.UnmeetableCode.UNREACHABLE_CODE_FOUND;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnmeetableCodeTest {

    private static SourceRoot sourceRoot, sourceRoot2;
    private UnmeetableCode unmeetableCode;

    @BeforeAll
    public static void setUpClass() throws Exception {
        sourceRoot = new SourceRoot(Paths.get("test-projects\\Java-Static-Analyzer\\src\\main\\java"));
        sourceRoot.tryToParse();
        sourceRoot2 = new SourceRoot(Paths.get("test-projects\\Unreachable-Code-Tests\\main\\src"));
        sourceRoot2.tryToParse();

    }

    @BeforeEach
    public void setUpMethod() {
        unmeetableCode = new UnmeetableCode();
    }

    @Test
    public void getName() {
        assertEquals("UnmeetableCode", unmeetableCode.getName());
    }

    @Test
    public void executeModule() {
        String[] expected = new String[]
                {
                        NO_ELEMENTS_FOUND
                };
        String[] results = unmeetableCode.executeModule(sourceRoot);
        assertArrayEquals(expected, results);
    }

    @Test
    public void executeModule2() {
        String[] expected = new String[]
                {
                        UNREACHABLE_CODE_FOUND,
                        "(line 6,col 9),UnreachableCodeFile.java",
                        "(line 7,col 9),UnreachableCodeFile.java",
                        "(line 9,col 9),UnreachableCodeFile.java",
                        "(line 10,col 9),UnreachableCodeFile.java",
                        "(line 12,col 9),UnreachableCodeFile.java",
                        "(line 13,col 9),UnreachableCodeFile.java",
                        "(line 15,col 9),UnreachableCodeFile.java",
                        "(line 16,col 9),UnreachableCodeFile.java",
                        "(line 18,col 9),UnreachableCodeFile.java",
                        "(line 19,col 9),UnreachableCodeFile.java",
                        "(line 21,col 9),UnreachableCodeFile.java",
                        "(line 22,col 9),UnreachableCodeFile.java",
                        "(line 24,col 9),UnreachableCodeFile.java",
                        "(line 25,col 9),UnreachableCodeFile.java",
                };
        String[] results = unmeetableCode.executeModule(sourceRoot2);
        assertArrayEquals(expected, results);
    }

    @Test
    public void getDescription() {
        String expected = "This module finds code that will potentially never run.\n " +
                "It is a utility that is useful for finding missed debug Statements to disable output";
        assertEquals(expected, unmeetableCode.getDescription());
    }

    @Test
    public void printMetrics() {
        String expected = NO_ELEMENTS_FOUND + System.getProperty("line.separator");
        unmeetableCode.executeModule(sourceRoot);
        assertEquals(expected, unmeetableCode.printMetrics());
    }

    @Test
    public void printMetrics2() {
        String separator = System.getProperty("line.separator");
        String expected =
                UNREACHABLE_CODE_FOUND + separator +
                        "(line 6,col 9),UnreachableCodeFile.java" + separator +
                        "(line 7,col 9),UnreachableCodeFile.java" + separator +
                        "(line 9,col 9),UnreachableCodeFile.java" + separator +
                        "(line 10,col 9),UnreachableCodeFile.java" + separator +
                        "(line 12,col 9),UnreachableCodeFile.java" + separator +
                        "(line 13,col 9),UnreachableCodeFile.java" + separator +
                        "(line 15,col 9),UnreachableCodeFile.java" + separator +
                        "(line 16,col 9),UnreachableCodeFile.java" + separator +
                        "(line 18,col 9),UnreachableCodeFile.java" + separator +
                        "(line 19,col 9),UnreachableCodeFile.java" + separator +
                        "(line 21,col 9),UnreachableCodeFile.java" + separator +
                        "(line 22,col 9),UnreachableCodeFile.java" + separator +
                        "(line 24,col 9),UnreachableCodeFile.java" + separator +
                        "(line 25,col 9),UnreachableCodeFile.java" + separator;

        unmeetableCode.executeModule(sourceRoot2);
        assertEquals(expected, unmeetableCode.printMetrics());
    }


}