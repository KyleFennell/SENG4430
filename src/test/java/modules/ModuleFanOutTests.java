package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class ModuleFanOutTests {

    @Test
    public void thisIsATestSoIDontHaveToRepro() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("src/main/java"));
        sourceRoot.tryToParse();
        FanOut fanOut = new FanOut();
        String[] results = fanOut.executeModule(sourceRoot);
        for (String s : results){
            System.out.println(s);
        }
        System.out.println(fanOut.printMetrics());
    }
}
