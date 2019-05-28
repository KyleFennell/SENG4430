package tests.java;

import com.github.javaparser.utils.SourceRoot;
import modules.FanIn;
import modules.StringMatchFanIn;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class ModuleStringMatchFanInTests {

    @Test
    public void thisIsATestSoIDontHaveToRepro() throws IOException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("C:/Users/Kyle/Dropbox/UoN/4.1/SENG4430/src/main/java"));
        sourceRoot.tryToParse();
        StringMatchFanIn smFanIn = new StringMatchFanIn();
        String[] results = smFanIn.executeModule(sourceRoot);
        for (String s : results){
            System.out.println(s);
        }
        System.out.println("RESULTS: " + smFanIn.printMetrics());
    }
}
