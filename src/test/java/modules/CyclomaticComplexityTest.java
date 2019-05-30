package modules;

import com.github.javaparser.utils.SourceRoot;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Tests the {@link CyclomaticComplexity} module.
 * In specific the finding algorithm of it's parent class {@link FlowGraphNumberExtractor}.
 * 
 * <p>Project       : Software Quality Assignment 1<br>
 * Date             : 29/05/19</p>
 * 
 * @author Nicolas Klenert
 * @see FlowGraphNumberExtractor
 * @see CyclomaticComplexity
 */
public class CyclomaticComplexityTest {
    //TODO: do some tests!
    
    static Stream<Arguments> NumberOfComponentsProvider(){
        return Stream.of(
                Arguments.of("graphics",5),
                Arguments.of("pa01",53),
                Arguments.of("sudoku",21)
        );
    }
    
    @ParameterizedTest
    @MethodSource("NumberOfComponentsProvider")
    void NumberOfComponentsTest(String folder, int number){
        Path source = Paths.get("src/test/resources/javacode/"+folder);
        if(!Files.isDirectory(source)){
            fail(source.toString()+" is not a directory!");
        }
        SourceRoot sourceRoot = new SourceRoot(source);
        try{
            sourceRoot.tryToParse();
        }catch(IOException e){
            fail("Directory could not be parsed!");
        }
        FlowGraphNumberExtractor module = new CyclomaticComplexity();
        module.executeModule(sourceRoot);
        assertEquals(number,module.blocks.size());
    }
}
