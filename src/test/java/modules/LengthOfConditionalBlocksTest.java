package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.*;

import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Project          : Software Quality Assignment
 * Class name       : LengthOfConditionalBlocksTest
 * Author(s)        : Ben Collins
 * Date Created     : 15/05/19
 * Purpose          : This class is used to test the LengthOfConditionalBlocks module
 */
public class LengthOfConditionalBlocksTest {

    private LengthOfConditionalBlocks locb;
    private static SourceRoot sourceRoot;
    private LengthOfConditionalBlocks locb2;
    private static SourceRoot sourceRoot2;

    @BeforeClass
    public static void setUpClass() throws Exception {
        sourceRoot = new SourceRoot(Paths.get("C:\\dev\\java\\Java-Static-Analyzer\\src\\main\\java"));
        sourceRoot.tryToParse();
        sourceRoot2 = new SourceRoot(Paths.get("src\\main\\java"));
        sourceRoot2.tryToParse();
    }

    @Before
    public void setUpMethod() {
        locb = new LengthOfConditionalBlocks();
        locb2 = new LengthOfConditionalBlocks();
    }

    @Test
    public void getName() {
        assertEquals(locb.getName(), "LengthOfConditionalBlocks");
    }

    @Test
    public void executeModule() {
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

    @Test
    public void executeModule2() {
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
    public void getDescription() {
        String description = "This is a measure of the size of conditional blocks in" +
                " the program. Generally, the larger the size of a block, the more" +
                " complex and error-prone that block is likely to be.";
        assertEquals(description, locb.getDescription());
    }

    @Test
    public void printMetrics() {
        locb.executeModule(sourceRoot);
        String expected = "C:\\dev\\java\\Java-Static-Analyzer\\src\\main\\java\\analyzer\\visitors\\MetricVisitor.java; Line 22: 11 lines. [Block larger than 10, may need reviewing]\r\n" +
                "C:\\dev\\java\\Java-Static-Analyzer\\src\\main\\java\\analyzer\\visitors\\VariableNamingConventionVisitor.java; Line 90: 11 lines. [Block larger than 10, may need reviewing]\r\n";
        assertEquals(expected, locb.printMetrics());
    }

    @Test
    public void printMetrics2() {
        locb2.executeModule(sourceRoot2);
        String expected = "";
        assertEquals(expected, locb.printMetrics());
    }
}