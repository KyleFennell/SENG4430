package modules;

import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FogIndexTest
{

    @Test
    public void testFogIndexOutput() throws IOException
    {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("test-projects/FogIndexTests/"));
        sourceRoot.tryToParse();
        FogIndex fogIndex = new FogIndex();
        String[] results = fogIndex.executeModule(sourceRoot);
        for(String e: results)
        {
            System.out.println(e);
        }
        String[] expectedResults = {
                "Fog Index module tested 1 files. The average fog index value is 7.42\n" +
                "A Fog index of 9 or lower indicates that the comments are simple in nature.\n"+
                "This can indicate that the methods are fairly simple or coded in such a way that little explanation is required\n" +
                "It is also possible that the methods have few comments. It is advisable to include comments to clarify complicated logic or for justification of a portion of code."
        };
        ;
        for (int i = 0; i < results.length; i++){
            assertTrue(results[i].equals(expectedResults[i]));
        }
    }
}
