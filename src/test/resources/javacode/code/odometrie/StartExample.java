package com.dcaiti.traceloader.odometrie;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TraceLoaderAccumulo2;
import com.dcaiti.traceloader.odometrie.TraceFilter.Output;
import com.dcaiti.traceloader.odometrie.onepartition.GaussianOnePartition;
import com.dcaiti.traceloader.odometrie.onepartition.LinearOnePartition;
import com.dcaiti.traceloader.odometrie.onepartition.OnePartition;
import com.dcaiti.traceloader.odometrie.onepartition.SplineOnePartition;

/** a example class for using the OdometriePackage
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class StartExample {

    public static void main(String[] args){
        
        //---------first we have to load a trace-----------
        String vehId = "cmtcde2u062_86540";//"Section_20150319_234701"  Section_20150323_234252"; 
        Trace trace = loadTrace(vehId);
        
        //-------we want to use TraceFilter to get better results afterwards---------
        
        //helper method, if you just want to filter the trace really quick
        //trace = TraceFilter.correct(trace);
        
        //if you want to adjust some settings, use the object oriented approach
        TraceFilter filter = new TraceFilter(trace);
        
        //if you want to cut the trace to protect the filter against time-jumps
        //TraceFilter filter = new TraceFilter(trace,true);
        
        //adjust if and what you want to see
//        filter.setFlag(EnumSet.of(Output.SHOW_GPS, Output.SHOW_SPEED));
        filter.setFlag(Output.SHOW);
        
        //use all corrections
        filter.correct();
        
        //just use specific corrections and not all
        //filter.correctSensorLag();
        
        //get the filtered Trace
        trace = filter.getCorrectedTrace();
        
        
        //---------------we want to use TraceMerger to use the power of odometrie---------
        
        //create our Tracemerger and give it our improved trace
        TraceMerger merger = new TraceMerger(trace);
        
        //improve our trace - with default PieceCreator etc.
        Trace improvedTrace = merger.improve();
        
        //visualize most pieces created, the resulting trace AND Triggers found
        merger.visualizeAllTraces();
        
        
        //----------------------------Other Adjustments and Visualizing------------------------
        
        //create other TraceMerger which will be more adjusted
        TraceMerger merger2 = new TraceMerger(trace);
        
        //::create OnePartitions (Partition of Unity / Zerlegung der Eins) to adjust the merging of our Trace::
        
        //linear uniform Partition for merging 2 pieces
        OnePartition distr1 = new LinearOnePartition(2);
        
        //linear Partition for merging 4 pieces with adjusted peaks
        OnePartition distr2 = new LinearOnePartition(new double[]{0,0.2,0.4,1});
        
        //unifrom b-spline Partition for merging 5 Pieces with degreee 3
        OnePartition distr3 = new SplineOnePartition(5,3);
        
        //uniform gaussian Partition for merging 4 Pieces with normal witdh
        OnePartition distr4 = new GaussianOnePartition(4);
        
        //unifrom gaussian Partition for merging 4 Pieces with smaller width
        OnePartition distr5 = new GaussianOnePartition(4,0.2);
        
        //gaussian Partition for merging 4 Pieces with width and height adjustet for every piece
        OnePartition distr6 = new GaussianOnePartition(new double[]{0.1,0.3,0.6,0.9}, new double[]{0.4,2,3,0.3}, new double[]{2,0.5,3,1});
        
        //visualize a Partition with 100 points
        distr6.visualize();
        
        //visualize a Partition with only 10 points
        distr5.visualize(10);
        
        //::create PieceCreator::
        
        //for now we only have DeadReckoningPieceCreator with 2 pieces
        PieceCreator creator = new DeadReckoningPieceCreator(distr1);
        
        //set the pieceCreator to the list of possible creators for merging
        merger.addCreator(creator);
        
        //merge after this adjustments
        Trace improvesLinearTrace = merger.improve();
        
    }
 
    public static Trace loadTrace(String vehId){
        String table = "foba_daimler_traces_1";
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);;
        loader.loadDaimlerTracesByVehicleId(vehId,table, true);     //"foba_daimler_traces_2"
        return loader.traces.get(0);
    }
    
}
