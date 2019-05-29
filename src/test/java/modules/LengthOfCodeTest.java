package modules;

import com.github.javaparser.utils.SourceRoot;

import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Project          : Software Quality Assignment
 * Class name       : LengthOfCodeTest
 * Author(s)        : Ben Collins
 * Date Created     : 13/05/19
 * Purpose          : This class is used to test the LengthOfCode module
 */
public class LengthOfCodeTest {

    private LengthOfCode loc;
    private static SourceRoot sourceRoot;
    private LengthOfCode loc2;
    private static SourceRoot sourceRoot2;
    
    @BeforeAll
    public static void setUpClass() throws Exception {
        sourceRoot = new SourceRoot(Paths.get("C:\\dev\\java\\Java-Static-Analyzer\\src\\main\\java"));
        sourceRoot.tryToParse();
        sourceRoot2 = new SourceRoot(Paths.get("src\\main\\java"));
        sourceRoot2.tryToParse();
    }

    @BeforeEach
    public void setUpMethod() {
        loc = new LengthOfCode();
        loc2 = new LengthOfCode();
    }

    @Test
    public void getName() {
        assertEquals(loc.getName(), "LengthOfCode");
    }

    @Test
    public void ExecuteModule() {
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
    public void executeModule2() {
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
    public void getDescription() {
        String description = "This is a measure of the size of a program. Generally, the larger" +
                " the size of the code of a component, the more complex and error-prone" +
                " that component is likely to be. Length of code has been shown to be" +
                " one of the most reliable metrics for predicting error-proneness in components.";
        assertEquals(description, loc.getDescription());
    }

    @Test
    public void printMetrics() {
        loc.executeModule(sourceRoot);
        String expected = "";
        assertEquals(expected, loc.printMetrics());
    }

    @Test
    public void printMetrics2() {
        loc2.executeModule(sourceRoot2);
        String expected = "C:\\dev\\uni\\SENG4430\\src\\main\\java\\ConsoleInterface.java: 343 lines. [File larger than 200, may need reviewing]\r\n";
        assertEquals(expected, loc2.printMetrics());
    }
}