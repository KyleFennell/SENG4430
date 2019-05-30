package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static modules.AverageLengthOfIdentifier.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AverageLengthOfIdentifierTest {
    private static SourceRoot sourceRoot;
    private AverageLengthOfIdentifier aloi;

    @BeforeAll
    public static void setUpClass() throws Exception {
        sourceRoot = new SourceRoot(Paths.get("test-projects\\Java-Static-Analyzer\\src\\main\\java"));
        sourceRoot.tryToParse();
    }

    @BeforeEach
    public void setUpMethod() {
        aloi = new AverageLengthOfIdentifier();
    }

    @Test
    public void getName() {
        assertEquals("AverageIdentifierLength", aloi.getName());
    }

    @Test
    public void executeModule() {
        String[] results = aloi.executeModule(sourceRoot);
        String[] expected = new String[]
                {
                        "Category of identifier: All Identifiers",
                        "Number of identifiers: 245",
                        "Average identifier length: 10.09",
                        "Standard Deviation of identifiers: 5.58",
                        "Category of identifier: Method Identifiers",
                        "Number of identifiers: 51",
                        "Average identifier length: 8.12",
                        "Standard Deviation of identifiers: 5.03",
                        "Category of identifier: Variable Identifiers",
                        "Number of identifiers: 73",
                        "Average identifier length: 11.41",
                        "Standard Deviation of identifiers: 6.31",
                        "Category of identifier: Class Identifiers",
                        "Number of identifiers: 19",
                        "Average identifier length: 15.47",
                        "Standard Deviation of identifiers: 6.54",
                        "Category of identifier: Package Identifiers",
                        "Number of identifiers: 18",
                        "Average identifier length: 15.22",
                        "Standard Deviation of identifiers: 3.91",
                        "Category of identifier: Parameter  Identifiers",
                        "Number of identifiers: 84",
                        "Average identifier length: 7.83",
                        "Standard Deviation of identifiers: 2.62"
                };
        assertArrayEquals(expected, results);
        // assertEquals("false", "false");
    }

    @Test
    public void getDescription() {
        String description = "This module calculates the average character length of all identifiers that appear in given Java file.\n" +
                "The purpose of this is to get a sense of how much meaning the identifiers convey.\n" +
                "Longer identifiers indicate more understandable code.";
        assertEquals(description, aloi.getDescription());
    }

    @Test
    public void printMetrics() {
        aloi.executeModule(sourceRoot);
        String[] expected = new String[]{
                "All Identifiers",
                "Number of identifiers: 245",
                "Average identifier length: 10.09",
                "Standard Deviation of identifiers: 5.58\n",
                MEDIUM_AVERAGE_MESSAGE,
                MEDIUM_STANDARD_DEVIATION_MESSAGE,
                "Method Identifiers",
                "Number of identifiers: 51",
                "Average identifier length: 8.12",
                "Standard Deviation of identifiers: 5.03\n",
                LOW_AVERAGE_MESSAGE,
                MEDIUM_STANDARD_DEVIATION_MESSAGE,
                "Variable Identifiers",
                "Number of identifiers: 73",
                "Average identifier length: 11.41",
                "Standard Deviation of identifiers: 6.31\n",
                MEDIUM_AVERAGE_MESSAGE,
                MEDIUM_STANDARD_DEVIATION_MESSAGE,
                "Class Identifiers",
                "Number of identifiers: 19",
                "Average identifier length: 15.47",
                "Standard Deviation of identifiers: 6.54\n",
                MEDIUM_AVERAGE_MESSAGE,
                MEDIUM_STANDARD_DEVIATION_MESSAGE,
                "Package Identifiers",
                "Number of identifiers: 18",
                "Average identifier length: 15.22",
                "Standard Deviation of identifiers: 3.91\n",
                MEDIUM_AVERAGE_MESSAGE,
                MEDIUM_STANDARD_DEVIATION_MESSAGE,
                "Parameter  Identifiers",
                "Number of identifiers: 84",
                "Average identifier length: 7.83",
                "Standard Deviation of identifiers: 2.62\n",
                LOW_AVERAGE_MESSAGE
                ,
                LOW_STANDARD_DEVIATION_MESSAGE

        };
        String[] result = aloi.printMetrics().split(System.getProperty("line.separator"));
        for (int x = 0; x < expected.length; x++) {
            assertEquals(expected[x], result[x]);
        }
        // assertArrayEquals(expected, ));
    }
}
