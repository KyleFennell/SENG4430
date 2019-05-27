package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AverageLengthOfIdentifierTest
{


        private AverageLengthOfIdentifier aloi;
        private static SourceRoot sourceRoot;

        @BeforeClass
        public static void setUpClass() throws Exception
        {
            sourceRoot = new SourceRoot(Paths.get("C:\\dev\\java\\Java-Static-Analyzer\\src\\main\\java"));
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
                            "Number of identifiers: 315",
                            "Average identifier length: 9.62",
                            "Standard Deviation of identifiers: 6.29",
                            "Category of identifier: Method Identifiers",
                            "Number of identifiers: 74",
                            "Average identifier length: 11.70",
                            "Standard Deviation of identifiers: 6.44",
                            "Category of identifier: Variable Identifiers",
                            "Number of identifiers: 153",
                            "Average identifier length: 10.00",
                            "Standard Deviation of identifiers: 6.32",
                            "Category of identifier: Class Identifiers",
                            "Number of identifiers: 17",
                            "Average identifier length: 14.00",
                            "Standard Deviation of identifiers: 4.97",
                            "Category of identifier: Parameter Identifiers",
                            "Number of identifiers: 63",
                            "Average identifier length: 5.29",
                            "Standard Deviation of identifiers: 3.79",
                            "Category of identifier: Package Identifiers",
                            "Number of identifiers: 8",
                            "Average identifier length: 7.75",
                            "Standard Deviation of identifiers: 2.82",
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
            String expected =
                    "All Identifiers\n" +
                            "Number of identifiers: 315\n" +
                            "Average identifier length: 9.62\n" +
                            "Standard Deviation of identifiers: 6.29\n" +
                            "\n" +
                            "This type of identifier falls in the medium range, this indicates that identifiers can mostly be identified as meaningful.\n" +
                            "However there is still room to expand the identifiers to benefit the long term sustainability of the program.\n" +
                            "The given number is acceptable but can be improved on.\n" +
                            "\n" +
                            "Identifiers of this category vary by 1-2 words in length. For higher averages this acceptable, low to medium\n" +
                            "averages might indicate that many words fall into lower or higher ranges that which would require review.\n" +
                            "\n" +
                            "Method Identifiers\n" +
                            "Number of identifiers: 74\n" +
                            "Average identifier length: 11.70\n" +
                            "Standard Deviation of identifiers: 6.44\n" +
                            "\n" +
                            "This type of identifier falls in the medium range, this indicates that identifiers can mostly be identified as meaningful.\n" +
                            "However there is still room to expand the identifiers to benefit the long term sustainability of the program.\n" +
                            "The given number is acceptable but can be improved on.\n" +
                            "\n" +
                            "Identifiers of this category vary by 1-2 words in length. For higher averages this acceptable, low to medium\n" +
                            "averages might indicate that many words fall into lower or higher ranges that which would require review.\n" +
                            "\n" +
                            "Variable Identifiers\n" +
                            "Number of identifiers: 153\n" +
                            "Average identifier length: 10.00\n" +
                            "Standard Deviation of identifiers: 6.32\n" +
                            "\n" +
                            "This type of identifier falls in the medium range, this indicates that identifiers can mostly be identified as meaningful.\n" +
                            "However there is still room to expand the identifiers to benefit the long term sustainability of the program.\n" +
                            "The given number is acceptable but can be improved on.\n" +
                            "\n" +
                            "Identifiers of this category vary by 1-2 words in length. For higher averages this acceptable, low to medium\n" +
                            "averages might indicate that many words fall into lower or higher ranges that which would require review.\n" +
                            "\n" +
                            "Class Identifiers\n" +
                            "Number of identifiers: 17\n" +
                            "Average identifier length: 14.00\n" +
                            "Standard Deviation of identifiers: 4.97\n" +
                            "\n" +
                            "This type of identifier falls in the medium range, this indicates that identifiers can mostly be identified as meaningful.\n" +
                            "However there is still room to expand the identifiers to benefit the long term sustainability of the program.\n" +
                            "The given number is acceptable but can be improved on.\n" +
                            "\n" +
                            "Identifiers of this category vary by 1-2 words in length. For higher averages this acceptable, low to medium\n" +
                            "averages might indicate that many words fall into lower or higher ranges that which would require review.\n" +
                            "\n" +
                            "Parameter Identifiers\n" +
                            "Number of identifiers: 63\n" +
                            "Average identifier length: 5.29\n" +
                            "Standard Deviation of identifiers: 3.79\n" +
                            "\n" +
                            "This type of identifier falls in the low range, it indicates that the majority of identifiers used are very short in length.\n" +
                            "It may be beneficial to examine the chosen identifiers and expand on the names.\n" +
                            "By doing so the readability of the program will increase, benefiting future use and understanding.\n" +
                            "\n" +
                            "Identifiers of this category vary by 1-2 words in length. For higher averages this acceptable, low to medium\n" +
                            "averages might indicate that many words fall into lower or higher ranges that which would require review.\n" +
                            "\n" +
                            "Package Identifiers\n" +
                            "Number of identifiers: 8\n" +
                            "Average identifier length: 7.75\n" +
                            "Standard Deviation of identifiers: 2.82\n" +
                            "\n" +
                            "This type of identifier falls in the low range, it indicates that the majority of identifiers used are very short in length.\n" +
                            "It may be beneficial to examine the chosen identifiers and expand on the names.\n" +
                            "By doing so the readability of the program will increase, benefiting future use and understanding.\n" +
                            "\n" +
                            "Identifiers of this category sit around the calculated average, there is not a lot of variance in the size.\n" +
                            "\n";
            assertEquals(expected, aloi.printMetrics());

        }

}
