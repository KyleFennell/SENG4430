package com.dcaiti.TraceLoader.odometrie;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dcaiti.traceloader.odometrie.TimeSectionHelper;
import com.dcaiti.traceloader.odometrie.ExtendedIntegral;

public class TestExtendedIntegral {

    private static long[] times;
    private static double[] ones;
    private static double[] twos;
    private static double[] threes;
    private static TimeSectionHelper helper;
    private static List<double[]> twoList;
    private static List<double[]> threeList;
   
    @BeforeClass
    public static void setUpBeforeClass(){
        times = new long[101];
        ones = new double[101];
        twos = new double[101];
        threes = new double[101];
        
        for(int i = 0; i <= 100; ++i){
            times[i] = i*20;
            ones[i] = 1d;
            twos[i] = 2d;
            threes[i] = 3d;
        }
        
        helper = new TimeSectionHelper();
        helper.closeBuild();
        twoList = new ArrayList<double[]>(2);
        twoList.add(twos);
        twoList.add(ones);
        
        threeList = new ArrayList<double[]>(3);
        threeList.add(twos);
        threeList.add(ones);
        threeList.add(threes);
    }
    
    @Test
    public void testWithoutTimeSections() {
        helper.clear();
        helper.closeBuild();
        double sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
        assertTrue("value is "+sum,sum == 8000);
    }
    
    @Test
    public void testWithOneTimeSections(){
        helper.clear();
        helper.setStart(150l);
        helper.setEnd(1150l);
        helper.closeBuild();
        double sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
        assertTrue(sum == 6000);
        
        helper.clear();
        helper.setStart(160l);
        helper.setEnd(1160l);
        helper.closeBuild();
        sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
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
        double sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
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
        double cross = ExtendedIntegral.extendedIntegral(twoList, times, helper);
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
        assertTrue(helper.inSection(0l) != 0);
        double sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
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
        double sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
        assertTrue(sum+"",sum == 4356);
        
        helper.clear();
        helper.setStart(675l);
        helper.setEnd(1421l);
        helper.setStart(1616l);
        helper.setEnd(2000l);
        helper.closeBuild();
        sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
        assertTrue(sum+"",sum == 5740);
        
        helper.clear();
        helper.setStart(-19l);
        helper.setEnd(0l);
        helper.setStart(1097l);
        helper.setEnd(1635l);
        helper.closeBuild();
        sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
        assertTrue(sum+"",sum == 6924);
        
        helper.clear();
        helper.setStart(915l);
        helper.setEnd(1409l);
        helper.setStart(1409l);
        helper.setEnd(1594l);
        helper.closeBuild();
        sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
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
        
        double sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
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
        
        sum = ExtendedIntegral.extendedIntegral(twoList, times, helper);
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
    
    private static long getWidth(long[] tim, long offset){
        long width = 0;
        long min = 0 - offset;
        long max = 2000 - offset;
        for(int i = 0; i < tim.length; i += 2){
            if(tim[i] < min && tim[i+1] > max){
                width += max-min;
            }else if(tim[i] < min && tim[i+1] > min){
                width += tim[i+1] - min;
            }else if(tim[i+1] > max && tim[i] < max){
                width += max - tim[i];
            }else if(tim[i] >= min && tim[i+1] <= max){
                width += tim[i+1]-tim[i];                
            }
        }
        return width;
    }
    
    private static long[] getWidths(long[] tim, long offset, int[] ids, int maxId){
        assertTrue(tim.length/2 == ids.length);
        long[] widths = new long[maxId+1];
        long min = 0 - offset;
        long max = 2000 - offset;
        for(int i = 0; i < tim.length; i += 2){
            if(tim[i] < min && tim[i+1] > max){
                widths[ids[i/2]] += max-min;
            }else if(tim[i] < min && tim[i+1] > min){
                widths[ids[i/2]] += tim[i+1] - min;
            }else if(tim[i+1] > max && tim[i] < max){
                widths[ids[i/2]] += max - tim[i];
            }else if(tim[i] >= min && tim[i+1] <= max){
                widths[ids[i/2]] += tim[i+1]-tim[i];                
            }
        }
        //set widths[0]:
        long sum = 0;
        for(int i = 1; i < widths.length; ++i){
            sum += widths[i];
        }
        widths[0] = (max-min) - sum;
        assertTrue(widths[0] >= 0);
        return widths;
    }
    
    private static long[] getRandomTimes(){
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
        return sec;
    }
    
    private static void setHelper(long[] tim, long offset){
        setHelper(tim,offset,null);
    }
    
    private static void setHelper(long[] tim, long offset, int[] ids){
        assertTrue(ids == null || tim.length == ids.length * 2);
        
        helper.clear();
        helper.setOffset(offset);
        
        for(int i = 0; i < tim.length; i += 2){
            if(ids == null){
                helper.setStart(tim[i]);
            }else{
                helper.setStart(tim[i], ids[i/2]);
            }
            helper.setEnd(tim[i+1]);
        }
        
        helper.closeBuild();
    }
    
    private static int[] getRandomIdArray(int size,int maxId){
        int[] ids = new int[size];
        for(int i = 0; i < size; ++i){
            ids[i] = (int)(Math.random() * maxId +1);
        }
        return ids;
    }
    
    @Test
    public void testThreeValuesRandom(){
        long[] tim = getRandomTimes();
        int[] ids = getRandomIdArray(tim.length/2,2);
        setHelper(tim,0,ids);
        long[] widths = getWidths(tim,0,ids,2);
        
        double sum = widths[0]*4 + widths[1]*2 + widths[2]*6;
        double cross = ExtendedIntegral.extendedIntegral(threeList, times, helper);
        
        System.out.println(helper.getMaxId());
        
        assertTrue(sum+" =!= "+cross,sum == cross);
        
        helper.clear();
    }
    
    @Test
    public void testThreeValues(){
        long[] tim = new long[4];
        tim[0] = 1000l;
        tim[1] = 1500l;
        tim[2] = 1500l;
        tim[3] = 2000l;
        
        int[] ids = new int[2];
        ids[0] = 1;
        ids[1] = 2;
        
        setHelper(tim, 0, ids);
        
        double sum = 1000*4+500*2+500*6;
        double cross = ExtendedIntegral.extendedIntegral(threeList, times, helper);
        
        assertTrue(sum+" =!= "+cross,sum == cross);
        
        helper.clear();
    }
    
    @Test
    public void testThreeValues2(){
        long[] tim = new long[4];
        tim[0] = 1000l;
        tim[1] = 1501l;
        tim[2] = 1501l;
        tim[3] = 2000l;
        
        int[] ids = new int[2];
        ids[0] = 1;
        ids[1] = 2;
        
        setHelper(tim, 0, ids);
        
        double sum = 1000*4+501*2+499*6;
        double cross = ExtendedIntegral.extendedIntegral(threeList, times, helper);
        
        assertTrue(sum+" =!= "+cross,sum == cross);
        
        helper.clear();
    }

}
