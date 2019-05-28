package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class ModuleFanInTests {

    @Test
    public void thisIsATestSoIDontHaveToRepro() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("C:/Users/Kyle/Dropbox/UoN/4.1/SENG4430/src/main/java"));
        sourceRoot.tryToParse();
        FanIn fanIn = new FanIn();
        fanIn.executeModule(sourceRoot);
    }
}
