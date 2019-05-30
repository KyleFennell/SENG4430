package modules;

import com.github.javaparser.utils.SourceRoot;

import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

/**
 * Project          : Software Quality Assignment
 * Class name       : LengthOfCodeTest
 * Author(s)        : Ben Collins
 * Date Created     : 13/05/19
 * Purpose          : This class is used to test the LengthOfCode module
 */
class LengthOfCodeTest {

    private LengthOfCode loc;
    private static SourceRoot sourceRoot;
    private LengthOfCode loc2;
    private static SourceRoot sourceRoot2;
    
    @BeforeAll
    static void setUpClass() throws Exception {
        sourceRoot = new SourceRoot(Paths.get("test-projects\\Java-Static-Analyzer\\src\\main\\java"));
        sourceRoot.tryToParse();

        // For use on project completion
        sourceRoot2 = new SourceRoot(Paths.get("src\\main\\java"));
        sourceRoot2.tryToParse();
    }

    @BeforeEach
    void setUpMethod() {
        loc = new LengthOfCode();
        loc2 = new LengthOfCode();
    }

    @Test
    void getName() {
        assertEquals(loc.getName(), "LengthOfCode");
    }

    @Test
    void executeModule() {
        String[] results = loc.executeModule(sourceRoot);
        String[] expected = new String[] {
            "LengthOfCode",
            "LineCount:1089",
            "FilesRead:19",
            "SmallestSize:9",
            "MedianSize:51",
            "LargestSize:140"
        };
        assertArrayEquals(expected, results);
    }

    @Disabled("Tests the current project files. Requires project completion")
    @Test
    void executeModule2() {
        String[] results = loc2.executeModule(sourceRoot2);
        String[] expected = new String[] {
            "LengthOfCode",
            "LineCount:895",
            "FilesRead:9",
            "SmallestSize:7",
            "MedianSize:82",
            "LargestSize:343"
        };
        assertArrayEquals(expected, results);
    }

    @Test
    void getDescription() {
        String description = "This is a measure of the size of a program. Generally, the larger" +
                " the size of the code of a component, the more complex and error-prone" +
                " that component is likely to be. Length of code has been shown to be" +
                " one of the most reliable metrics for predicting error-proneness in components.";
        assertEquals(description, loc.getDescription());
    }

    @Test
    void printMetrics() {
        loc.executeModule(sourceRoot);
        String expected = "";
        assertEquals(expected, loc.printMetrics());
    }

    @Disabled("Tests the current project files. Requires project completion")
    @Test
    void printMetrics2() {
        loc2.executeModule(sourceRoot2);
        String[] expected = {
                "flowgraph\\FlowGraph.java: 322 lines. [File larger than 200, may need reviewing]",
                "flowgraph\\AbstractFlowGraphBuilder.java: 271 lines. [File larger than 200, may need reviewing]",
                "modules\\AverageLengthOfIdentifier.java: 268 lines. [File larger than 200, may need reviewing]",
                "modules\\UnmeetableCode.java: 247 lines. [File larger than 200, may need reviewing]",
                "ConsoleInterface.java: 367 lines. [File larger than 200, may need reviewing]",
                "modules\\FlowGraphNumberExtractor.java: 214 lines. [File larger than 200, may need reviewing]"
        };
        String actual = loc2.printMetrics();

        assertFalse(actual.isEmpty());
        for (String str : expected) {
            assertTrue(actual.contains(str));
        }
    }
}