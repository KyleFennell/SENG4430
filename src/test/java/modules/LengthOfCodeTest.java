package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.file.Paths;

/**
 * Project          : Software Quality Assignment
 * Class name       : LengthOfCodeTest
 * Author(s)        : Ben Collins
 * Date Created     : 13/05/19
 * Purpose          : This class is used to test the LengthOfCode module
 */
public class LengthOfCodeTest {

    private LengthOfCode loc;
    private SourceRoot sourceRoot;

    @Before
    public void setUp() throws Exception {
        loc = new LengthOfCode();
        sourceRoot = new SourceRoot(Paths.get("C:\\dev\\java\\Java-Static-Analyzer\\src\\main\\java"));
        //TODO: sourceRoot = new SourceRoot(Paths.get("src\\main\\java"));
        sourceRoot.tryToParse();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getName() {
        assertEquals(loc.getName(), "LengthOfCode");
    }

    @Test
    public void executeModule() {
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

    @Test
    public void getDescription() {
        String description = "This is a measure of the size of a program. Generally, the larger" +
                " the size of the code of a component, the more complex and error-prone" +
                " that component is likely to be. Length of code has been shown to be" +
                " one of the most reliable metrics for predicting error-proneness in components.";
        assertEquals(description, loc.getDescription());
    }

    //TODO: New Test Data
    @Test
    public void printMetrics() {
        loc.executeModule(sourceRoot);
        String expected = "";
        assertEquals(expected, loc.printMetrics());
    }
}