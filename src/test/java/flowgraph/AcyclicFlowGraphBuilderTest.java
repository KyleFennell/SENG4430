package flowgraph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : AcyclicFlowGraphBuilderTest
 * @author(s)       : Nicolas Klenert
 * Date             : 26/05/19
 * Purpose          : Tets FlowGraphs created from AcyclicFlowGraphBuilder
 */
public class AcyclicFlowGraphBuilderTest {
    
    private final AcyclicFlowGraphBuilder builder = new AcyclicFlowGraphBuilder();
   
    @DisplayName("AcyclicFlowGraphBuilder behaviour")
    @ParameterizedTest(name = "[{index}]: {1}")
    @CsvFileSource(resources = "/flowgraph/testdata/acyclic.csv", numLinesToSkip = 1)
    public void testAcyclicFlowGraphBuilder(String code, String file){
       TestUtils.compareGraphs(file, code, builder);
    }
    
}
