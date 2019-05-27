package tests.java;

import com.github.javaparser.utils.SourceRoot;
import modules.FanOut;
import utils.Logger;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class ModuleFanOutTests {

    @Test
    public void thisIsATestSoIDontHaveToRepro() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("C:/Users/Kyle/Dropbox/UoN/4.1/SENG4430/src/main/java"));
        sourceRoot.tryToParse();
        FanOut fanOut = new FanOut();
        fanOut.executeModule(sourceRoot);
        System.out.println(fanOut.printMetrics());
    }
}
