package flowgraph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/** Tests {@link AcyclicFlowGraphBuilder}.
 *  Tests FlowGraphs created from AcyclicFlowGraphBuilder.
 *  Most cases are already covered by {@link CyclicFlowGraphBuilderTest} such that
 *  only loop-like graphs have to be tested.
 * 
 * <p>Project       : Software Quality Assignment 1<br>
 * Date             : 26/05/19</p>
 * 
 * @author Nicolas Klenert
 * @see AcyclicFlowGraphBuilder
 * @see CyclicFlowGraphBuilderTest
 */
public class AcyclicFlowGraphBuilderTest {
    
    private final AcyclicFlowGraphBuilder builder = new AcyclicFlowGraphBuilder();
   
    /** Main test.
     * 
     *  <p>Creates a {@code FlowGraph} out of the code snippet
     *  and one out of the given file and compares them to each other.</p>
     *  
     * @param code code snippet from which a {@code FlowGraph} is created
     * @param file name of file in the {@code test/resource/flowgraph/graph} directory from which
     *              the other {@code FlowGraph} is created.
     * @see CyclicFlowGraphBuilderTest
     */
    @DisplayName("AcyclicFlowGraphBuilder behaviour")
    @ParameterizedTest(name = "[{index}]: {1}")
    @CsvFileSource(resources = "/flowgraph/testdata/acyclic.csv", numLinesToSkip = 1)
    public void testAcyclicFlowGraphBuilder(String code, String file){
       TestUtils.compareGraphs(file, code, builder, "Statement");
    }
    
}
