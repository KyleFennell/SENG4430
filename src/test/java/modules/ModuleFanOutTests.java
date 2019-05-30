package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleFanOutTests {

    @Test
    public void thisIsATestSoIDontHaveToRepro() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("test-projects/FanInOutTestCode"));
        sourceRoot.tryToParse();
        FanOut fanOut = new FanOut();
        String[] results = fanOut.executeModule(sourceRoot);
        for (String s : results){
//            System.out.println(s);
        }
//        System.out.println(fanOut.printMetrics());
    }

    @Test
    public void testFanOutExecutionOutput() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("test-projects/FanInOutTestCode"));
        sourceRoot.tryToParse();
        FanOut fanOut = new FanOut();
        String[] results = fanOut.executeModule(sourceRoot);
        String[] expectedResults = {
                "TotalFanOut: ",
                "Max: 109 ([StarberksInterface.java_run])",
                "Min: 1 ([StarberksInterface.java_main])",
                "Mean: 55 ([])",
                "Mode: 1 ([StarberksInterface.java_main])",
                "UniqueFanOut: ",
                "Max: 22 ([StarberksInterface.java_run])",
                "Min: 1 ([StarberksInterface.java_main])",
                "Mean: 11 ([])",
                "Mode: 1 ([StarberksInterface.java_main])"
        };
                ;
        for (int i = 0; i < results.length; i++){
            assertTrue(results[i].equals(expectedResults[i]));
        }
    }

    @Test
    public void testFanOutPrintMetrics() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("test-projects/FanInOutTestCode"));
        sourceRoot.tryToParse();
        FanOut fanOut = new FanOut();
        fanOut.executeModule(sourceRoot);
        String result = fanOut.printMetrics();
        String expectedResult = "TOTAL FAN OUT METRIC: \n" +
                "\tStarberksInterface.java\n" +
                "\t\trun: 109\n" +
                "\t\tmain: 1\n" +
                "UNIQUE FAN OUT METRIC: \n" +
                "\tStarberksInterface.java\n" +
                "\t\trun: 22\n" +
                "\t\tmain: 1\n";
        assertTrue(result.equals(expectedResult));
    }
}
