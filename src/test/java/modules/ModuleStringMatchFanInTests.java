package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleStringMatchFanInTests {

    @Test
    public void thisIsATestSoIDontHaveToRepro() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("test-projects/FanInOutTestCode"));
        sourceRoot.tryToParse();
        StringMatchFanIn smFanIn = new StringMatchFanIn();
        String[] results = smFanIn.executeModule(sourceRoot);
        for (String s : results){
            System.out.println(s);
        }
        System.out.println("RESULTS: " + smFanIn.printMetrics());
    }

    @Test
    public void testFanOutExecutionOutput() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("test-projects/FanInOutTestCode"));
        sourceRoot.tryToParse();
        StringMatchFanIn fanIn = new StringMatchFanIn();
        String[] results = fanIn.executeModule(sourceRoot);
        String[] expectedResults = {
                "Max: 44 ([println])",
                "Min: 0 ([main])",
                "Mean: 4 ([])",
                "Mode: 1 ([getEOQCount, run, editProductDemand, editName, editProduct, print, getMostProfitable, printRepStrat, instantiateProduct])",
        };
        for (int i = 0; i < results.length; i++){
            assertTrue(results[i].equals(expectedResults[i]));
        }
    }

    @Test
    public void testFanOutPrintMetrics() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("test-projects/FanInOutTestCode"));
        sourceRoot.tryToParse();
        StringMatchFanIn fanIn = new StringMatchFanIn();
        fanIn.executeModule(sourceRoot);
        String result = fanIn.printMetrics();
        String expectedResult = "String Match Fan In: \n" +
                "next: 2\n" +
                "nextFloat: 5\n" +
                "getName: 3\n" +
                "printInfo: 2\n" +
                "getEOQCount: 1\n" +
                "nextLine: 10\n" +
                "toLowerCase: 5\n" +
                "length: 2\n" +
                "getProductCount: 2\n" +
                "run: 1\n" +
                "main: 0\n" +
                "nextInt: 5\n" +
                "checkName: 15\n" +
                "editProductDemand: 1\n" +
                "editName: 1\n" +
                "editProduct: 1\n" +
                "println: 44\n" +
                "print: 1\n" +
                "equals: 2\n" +
                "getEOQ: 2\n" +
                "getMostProfitable: 1\n" +
                "deleteProduct: 2\n" +
                "printRepStrat: 1\n" +
                "instantiateProduct: 1\n";
        assertTrue(result.equals(expectedResult));
    }
}
