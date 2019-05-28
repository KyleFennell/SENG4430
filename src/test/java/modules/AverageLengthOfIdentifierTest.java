package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

import static modules.AverageLengthOfIdentifier.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public  class AverageLengthOfIdentifierTest
{
        private AverageLengthOfIdentifier aloi;
        private static SourceRoot sourceRoot;

        @BeforeClass
        public static void setUpClass() throws Exception
        {
            sourceRoot = new SourceRoot(Paths.get("src\\main\\java"));
            sourceRoot.tryToParse();
        }

        @Before
        public void setUpMethod()
        {
            aloi = new AverageLengthOfIdentifier();
        }

        @Test
        public void getName()
        {
            assertEquals(aloi.getName(), "Average Identifier Length");
        }

        @Test
        public void executeModule() {
            String[] results = aloi.executeModule(sourceRoot);
            String[] expected = new String[]
                    {
            "Category of identifier: All Identifiers",
            "Number of identifiers: 346",
            "Average identifier length: 9.05",
            "Standard Deviation of identifiers: 6.30",
            "Category of identifier: Method Identifiers",
            "Number of identifiers: 87",
            "Average identifier length: 11.74",
            "Standard Deviation of identifiers: 6.56",
            "Category of identifier: Variable Identifiers",
            "Number of identifiers: 157",
            "Average identifier length: 9.25",
            "Standard Deviation of identifiers: 6.15",
            "Category of identifier: Class Identifiers",
            "Number of identifiers: 16",
            "Average identifier length: 14.69",
            "Standard Deviation of identifiers: 5.31",
            "Category of identifier: Package Identifiers",
            "Number of identifiers: 9",
            "Average identifier length: 7.67",
            "Standard Deviation of identifiers: 2.67",
            "Category of identifier: Parameter  Identifiers",
            "Number of identifiers: 77",
            "Average identifier length: 4.57",
            "Standard Deviation of identifiers: 3.28"
                    };
            assertArrayEquals(expected, results);
        }

        @Test
        public void getDescription() {
            String description = "This module calculates the average character length of all identifiers that appear in given Java file.\n" +
                                 "The purpose of this is to get a sense of how much meaning the identifiers convey.\n" +
                                 "Longer identifiers indicate more understandable code.";
            assertEquals(description, aloi.getDescription());
        }
        @Test
    public void printMetrics()
        {
            aloi.executeModule(sourceRoot);
            String expected =
                    "All Identifiers\n" +
                            "Number of identifiers: 315\n" +
                            "Average identifier length: 9.62\n" +
                            "Standard Deviation of identifiers: 6.29\n" +
                            "\n" +
                            MEDIUM_AVERAGE_MESSAGE +
                            "\n" +
                            MEDIUM_STANDARD_DEVIATION_MESSAGE +
                            "\n" +
                            "Method Identifiers\n" +
                            "Number of identifiers: 74\n" +
                            "Average identifier length: 11.70\n" +
                            "Standard Deviation of identifiers: 6.44\n" +
                            "\n" +
                            MEDIUM_AVERAGE_MESSAGE +
                            "\n" +
                            MEDIUM_STANDARD_DEVIATION_MESSAGE +
                            "\n" +
                            "Variable Identifiers\n" +
                            "Number of identifiers: 153\n" +
                            "Average identifier length: 10.00\n" +
                            "Standard Deviation of identifiers: 6.32\n" +
                            "\n" +
                            MEDIUM_AVERAGE_MESSAGE +
                            "\n" +
                            MEDIUM_STANDARD_DEVIATION_MESSAGE +
                            "\n" +
                            "Class Identifiers\n" +
                            "Number of identifiers: 17\n" +
                            "Average identifier length: 14.00\n" +
                            "Standard Deviation of identifiers: 4.97\n" +
                            "\n" +
                            MEDIUM_AVERAGE_MESSAGE +
                            "\n" +
                            MEDIUM_AVERAGE_MESSAGE +
                            "\n" +
                            "Parameter Identifiers\n" +
                            "Number of identifiers: 63\n" +
                            "Average identifier length: 5.29\n" +
                            "Standard Deviation of identifiers: 3.79\n" +
                            "\n" +
                            LOW_AVERAGE_MESSAGE +
                            "\n" +
                            LOW_STANDARD_DEVIATION_MESSAGE +
                            "\n" +
                            "Package Identifiers\n" +
                            "Number of identifiers: 8\n" +
                            "Average identifier length: 7.75\n" +
                            "Standard Deviation of identifiers: 2.82\n" +
                            "\n" +
                            LOW_AVERAGE_MESSAGE +
                            "\n" +
                            LOW_STANDARD_DEVIATION_MESSAGE +
                            "\n";
            assertEquals(expected, aloi.printMetrics());

        }

}
