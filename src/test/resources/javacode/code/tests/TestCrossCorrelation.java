package com.dcaiti.TraceLoader.odometrie;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dcaiti.traceloader.odometrie.TraceFilter2;


/** old Test Unit -> not used anymore. Instead use TestExtendedIntegral
 * 
 * @author nkl
 *
 */
public class TestCrossCorrelation {

    private static long[] times;
    private static double[] values;
    private static double[] gpsValues;
    private static TraceFilter2.TimeSectionsHelper helper;
    private static TraceFilter2 filter;
   
    @BeforeClass
    public static void setUpBeforeClass(){
        times = new long[101];
        values = new double[101];
        gpsValues = new double[101];
        
        for(int i = 0; i <= 100; ++i){
            times[i] = i*20;
            values[i] = 1d;
            gpsValues[i] = 2d;
        }
        
        helper = new TraceFilter2.TimeSectionsHelper();
        helper.closeBuild();
     
        filter = new TraceFilter2();
    }
    
    @Test
    public void testWithoutTimeSections() {
        helper.clear();
        helper.closeBuild();
        double sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum == 8000);
    }
    
    @Test
    public void testWithOneTimeSections(){
        helper.clear();
        helper.setStart(150l);
        helper.setEnd(1150l);
        helper.closeBuild();
        double sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum == 6000);
        
        helper.clear();
        helper.setStart(160l);
        helper.setEnd(1160l);
        helper.closeBuild();
        sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum == 6000);
    }
    
    @Test
    public void testWithSomeTimeSections(){
        helper.clear();
        helper.setStart(-100l);
        helper.setEnd(-50l);
        helper.setStart(100l);
        helper.setEnd(200l);
        helper.setStart(730l);
        helper.setEnd(830l);
        helper.setStart(1900l);
        helper.setEnd(2200l);
        helper.closeBuild();
        double sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum == 7400);
    }
    
    @Test
    public void testRandomTimes(){
        int times = 300;
        for(int i = 0; i < times; ++i){
            testRandom(0);
        }
    }
    
    @Test
    public void testRandomTimesWithOffset(){
        int timesWithOffset = 100;
        long maxOffset = 3000;
        long minOffset = 0;
        for(int i = 0; i < timesWithOffset; ++i){
            testRandom((long)(Math.random()*(maxOffset-minOffset)) -minOffset);
        }
    }
        
    private void testRandom(long offset){
        int maxSections = 100;
        int minSections = 2;
        int count = (int)Math.ceil(Math.random()*(maxSections - minSections))+minSections;
        count -= count % 2;
        long[] sec = new long[count];
        for(int i = 0; i < sec.length; ++i){
            //get random long from -100 to 2100
            sec[i] = (long)Math.ceil((Math.random() * 2200))-100l;
        }
        Arrays.sort(sec);
        
        long width = 0;
        helper.clear();
        
        for(int i = 0; i < sec.length; i += 2){
            helper.setStart(sec[i]);
            helper.setEnd(sec[i+1]);
            long min = 0 - offset;
            long max = 2000 - offset;
            if(sec[i] < min && sec[i+1] > max){
                width += max-min;
            }else if(sec[i] < min && sec[i+1] > min){
                width += sec[i+1] - min;
            }else if(sec[i+1] > max && sec[i] < max){
                width += max - sec[i];
            }else if(sec[i] >= min && sec[i+1] <= max){
                width += sec[i+1]-sec[i];                
            }
        }
        
//        System.out.println(width);
//        System.out.println(Arrays.toString(sec));
        helper.setOffset(offset);
        helper.closeBuild();
        double sum = 8000-width*2;
        double cross = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        if(sum != cross){
            System.out.println(helper.getTimeStamps().toString());
        }
        assertTrue(sum+" =!= "+cross+" with offset: "+offset,sum == cross);

    }
    
    @Test
    public void testSomeThings(){
        helper.clear();
        helper.setStart(-78l);
        helper.setEnd(164l);
        helper.setStart(200l);
        helper.setEnd(259l);
        helper.closeBuild();
        assertTrue(helper.inSection(0l));
        double sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum+"",sum == 7554);
    }
    
    @Test
    public void testSpecialCases(){
        helper.clear();
        helper.setStart(0l);
        helper.setEnd(48l);
        helper.setStart(81l);
        helper.setEnd(1855l);
        helper.closeBuild();
        double sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum+"",sum == 4356);
        
        helper.clear();
        helper.setStart(675l);
        helper.setEnd(1421l);
        helper.setStart(1616l);
        helper.setEnd(2000l);
        helper.closeBuild();
        sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum+"",sum == 5740);
        
        helper.clear();
        helper.setStart(-19l);
        helper.setEnd(0l);
        helper.setStart(1097l);
        helper.setEnd(1635l);
        helper.closeBuild();
        sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum+"",sum == 6924);
        
        helper.clear();
        helper.setStart(915l);
        helper.setEnd(1409l);
        helper.setStart(1409l);
        helper.setEnd(1594l);
        helper.closeBuild();
        sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum+"",sum == 6642);
    }
    
    @Test
    public void testSpecialCasesWithOffset(){
        long offset = 196;
        helper.clear();
        helper.setStart(736l);
        helper.setEnd(1844);
        helper.setOffset(offset);
        helper.closeBuild();
        
        List<Long> tim = new ArrayList<Long>();
        tim.add(736l);
        tim.add(1844l);
        
        List<Long> gottedTimes = helper.getTimeStamps();
        boolean same = true;
        for(int i = 0; i < tim.size(); ++i){
            if(!gottedTimes.get(i).equals(tim.get(i)+offset)){
                same = false;
            }
        }
        assertTrue(same);
        
        double sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum+"",sum == 5864);
        
        
        //--------------------------
        
        tim = new ArrayList<Long>();
        //[-71, 150, 152, 164, 174, 242, 259, 269, 285, 414, 418, 420, 662, 814, 1213, 1267, 1508, 1599, 1683, 1727, 1758, 1946, 1991, 1998]
        tim.add(-71l);
        tim.add(150l);
//        tim.add(152l);
//        tim.add(164l);
//        tim.add(174l);
//        tim.add(242l);
//        tim.add(259l);
        //-----
        
        //---------
//        tim.add(1946l);
        tim.add(1991l);
        tim.add(1998l);
        
        offset = 6;
        helper.clear();
        
        for(int i = 0; i < tim.size(); i += 2){
            helper.setStart(tim.get(i));
            helper.setEnd(tim.get(i+1));
        }
        
        helper.setOffset(offset);
        helper.closeBuild();
        
        double predict = 8000-getWidth(tim,offset)*2;
        
        sum = filter.lagFilter.crossCorrelation(values, times, gpsValues, helper);
        assertTrue(sum+" != "+predict,sum == predict);
        
    }
    
    private static long getWidth(List<Long> tim, long offset){
        long width = 0;
        long min = 0 - offset;
        long max = 2000 - offset;
        for(int i = 0; i < tim.size(); i += 2){
            if(tim.get(i) < min && tim.get(i+1) > max){
                width += max-min;
            }else if(tim.get(i) < min && tim.get(i+1) > min){
                width += tim.get(i+1) - min;
            }else if(tim.get(i+1) > max && tim.get(i) < max){
                width += max - tim.get(i);
            }else if(tim.get(i) >= min && tim.get(i+1) <= max){
                width += tim.get(i+1)-tim.get(i);                
            }
        }
        return width;
    }

}
