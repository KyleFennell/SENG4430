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
            assertEquals(aloi.getName(), "LengthOfCode");
        }

        @Test
        public void executeModule() {
            String[] results = aloi.executeModule(sourceRoot);
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
            assertEquals(description, aloi.getDescription());
        }

}
