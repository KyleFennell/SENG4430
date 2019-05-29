package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static modules.UnmeetableCode.NO_ELEMENTS_FOUND;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnmeetableCodeTest {

    private static SourceRoot sourceRoot;
    private UnmeetableCode unmeetableCode;

    @BeforeAll
    public static void setUpClass() throws Exception {
        sourceRoot = new SourceRoot(Paths.get("test-projects\\Java-Static-Analyzer\\src\\main\\java"));
        sourceRoot.tryToParse();
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

}