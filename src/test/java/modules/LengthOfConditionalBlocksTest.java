package modules;

import com.github.javaparser.utils.SourceRoot;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Project          : Software Quality Assignment
 * Class name       : LengthOfConditionalBlocksTest
 * Author(s)        : Ben Collins
 * Date Created     : 15/05/19
 * Purpose          : This class is used to test the LengthOfConditionalBlocks module
 */
class LengthOfConditionalBlocksTest {

    private LengthOfConditionalBlocks locb;
    private static SourceRoot sourceRoot;
    private LengthOfConditionalBlocks locb2;
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
        locb = new LengthOfConditionalBlocks();
        locb2 = new LengthOfConditionalBlocks();
    }

    @Test
    void getName() {
        assertEquals(locb.getName(), "LengthOfConditionalBlocks");
    }

    @Test
    void executeModule() {
        String[] results = locb.executeModule(sourceRoot);
        String[] expected = new String[] {
            "LengthOfConditionalBlocks",
            "LineCount:174",
            "NumberOfBlocks:43",
            "SmallestSize:2",
            "MedianSize:3",
            "LargestSize:11"
        };
        assertArrayEquals(expected, results);
    }

    @Disabled("Tests the current project files. Requires project completion")
    @Test
    void executeModule2() {
        String[] results = locb2.executeModule(sourceRoot2);
        String[] expected = new String[] {
            "LengthOfConditionalBlocks",
            "LineCount:212",
            "NumberOfBlocks:44",
            "SmallestSize:2",
            "MedianSize:4",
            "LargestSize:18"
        };
        assertArrayEquals(expected, results);
    }

    @Test
    void getDescription() {
        String description = "This is a measure of the size of conditional blocks in" +
                " the program. Generally, the larger the size of a block, the more" +
                " complex and error-prone that block is likely to be.";
        assertEquals(description, locb.getDescription());
    }

    @Test
    void printMetrics() {
        locb.executeModule(sourceRoot);
        String[] expected = {
                "analyzer\\visitors\\MetricVisitor.java; Line 22: 11 lines. [Block larger than 10, may need reviewing]",
                "analyzer\\visitors\\VariableNamingConventionVisitor.java; Line 90: 11 lines. [Block larger than 10, may need reviewing]"
        };
        String actual = locb.printMetrics();

        assertFalse(actual.isEmpty());
        for (String str : expected) {
            assertTrue(actual.contains(str));
        }
    }

    @Disabled("Tests the current project files. Requires project completion")
    @Test
    void printMetrics2() {
        locb2.executeModule(sourceRoot2);
        String expected = "";
        assertEquals(expected, locb.printMetrics());
    }
}