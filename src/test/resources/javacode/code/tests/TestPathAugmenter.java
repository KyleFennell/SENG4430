package com.dcaiti.TraceLoader.odometrie;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dcaiti.traceloader.odometrie.PathAugmenter;
import com.vividsolutions.jts.geom.Coordinate;

public class TestPathAugmenter {

    private static PathAugmenter path;
    private static List<Coordinate> coor;
    private static List<Long> time;
    private static List<Long> testTimes;
    
    @BeforeClass
    public static void setUpBeforeClass(){
        coor = new ArrayList<Coordinate>();
        time = new ArrayList<Long>();
        
        coor.add(new Coordinate(10,10));
        coor.add(new Coordinate(20,20));
        coor.add(new Coordinate(30,30));
        coor.add(new Coordinate(40,40));
        coor.add(new Coordinate(50,50));
        
        time.add(100l);
        time.add(200l);
        time.add(300l);
        time.add(350l);
        time.add(375l);
        
        testTimes = new ArrayList<Long>();
        for(long i = 90l; i < 400l; i += 5){
            testTimes.add(i);
        }
        
        path = new PathAugmenter(coor,time);
    }
        
    @Test
    public void testIfSorted(){
//        System.out.println(path.getCoorMinTime());
        assertTrue(path.getCoorMinTime() == 100l);
        assertTrue(path.getCoorMaxTime() == 375l);
    }
    
    @Test
    public void testByEnd(){
        //test if too big and too low timestamps will get you the end
        assertTrue(path.getCoorByTime(50l).equals(new Coordinate(10,10)));
        assertTrue(path.getCoorByTime(500l).equals(new Coordinate(50,50)));
    }
    
    @Test
    public void testByExactTime(){
      //test if you get the real coor if you use an exact time
        for(int i = 0; i < coor.size(); ++i){
            assertTrue(path.getCoorByTime(time.get(i)).equals(coor.get(i)));
        }
        assertTrue(path.getCoorByTime(350l).equals(new Coordinate(40,40)));
    }
    
    @Test
    public void testTrigonometrie(){
        //test if it makes a little sense with the interpolation
        Coordinate coor1 = path.getCoorByTime(120l);
        Coordinate coor2 = path.getCoorByTime(180l);
        Coordinate left = new Coordinate(10,10);
        Coordinate right = new Coordinate(20,20);
        assertTrue(coor1.distance(left) < coor1.distance(right));
        assertTrue(coor2.distance(right) < coor2.distance(left));
        assertTrue(coor1.distance(coor2) < coor1.distance(right));
        assertTrue(coor2.distance(coor1) < coor2.distance(left));
    }
    
    @Test
    public void testSpeed(){
        assertTrue(path.getSpeedByTime(150l) == path.getSpeedByTime(250l));
        assertTrue(path.getSpeedByTime(325l)+" not < as "+path.getSpeedByTime(370l),path.getSpeedByTime(325l) < path.getSpeedByTime(370l));
    }
    
    @Test
    public void testTokens(){
        List<Coordinate> coor = new ArrayList<Coordinate>();
        List<Long> time = new ArrayList<Long>();
        for(int i = 0; i < TestPathAugmenter.time.size(); ++i){
            coor.add(TestPathAugmenter.coor.get(i));
            time.add(TestPathAugmenter.time.get(i));
        }
        PathAugmenter augment = new PathAugmenter(coor,time);
        List<Coordinate> coorsBeforeChange = new ArrayList<Coordinate>();
        List<Double> speedBeforeChange = new ArrayList<Double>();
        for(int i = 0; i < testTimes.size(); ++i){
            coorsBeforeChange.add(augment.getCoorByTime(testTimes.get(i)));
            speedBeforeChange.add(augment.getSpeedByTime(testTimes.get(i)));
        }
        coor.set(0, new Coordinate(-40,-40));
        coor.set(2, new Coordinate(0,600));
        coor.get(1).x = 500000;
        coor.get(3).x = -1000;
        time.set(0, -4000l);
        List<Coordinate> coorsAfterChange = new ArrayList<Coordinate>();
        List<Double> speedAfterChange = new ArrayList<Double>();
        for(int i = 0; i < testTimes.size(); ++i){
            coorsAfterChange.add(augment.getCoorByTime(testTimes.get(i)));
            speedAfterChange.add(augment.getSpeedByTime(testTimes.get(i)));
        }
        assertTrue("PathAugmenter is vulnerable against changes on the input data!",coorsBeforeChange.equals(coorsAfterChange));
        assertTrue("PathAugmenter is vulnerable against changes on the input data!",speedBeforeChange.equals(speedAfterChange));
    }
        
    @Test
    public void testOffset(){
        PathAugmenter augment = new PathAugmenter(coor,time);
        augment.setOffset(100l);
        
        assertTrue(path.getCoorMaxTime()+100l == augment.getCoorMaxTime());
        assertTrue(path.getCoorMinTime()+100l == augment.getCoorMinTime());
        
        for(int i = 0; i < testTimes.size(); ++i){
            assertTrue(path.getCoorByTime(testTimes.get(i)).equals(augment.getCoorByTime(testTimes.get(i) + 100l)));
            assertTrue(path.getSpeedByTime(testTimes.get(i)) == augment.getSpeedByTime(testTimes.get(i) + 100l));
        }
        
        augment = new PathAugmenter(coor,time);
        augment.setOffset(-100l);
        
        assertTrue(path.getCoorMaxTime()-100l == augment.getCoorMaxTime());
        assertTrue(path.getCoorMinTime()-100l == augment.getCoorMinTime());
        
        for(int i = 0; i < testTimes.size(); ++i){
            assertTrue(path.getCoorByTime(testTimes.get(i)).equals(augment.getCoorByTime(testTimes.get(i) - 100l)));
            assertTrue(path.getSpeedByTime(testTimes.get(i)) == augment.getSpeedByTime(testTimes.get(i) - 100l));
        }
    }
    
    @Test
    public void testHeading(){
        for(int i = 0; i < testTimes.size(); ++i){
            assertTrue(Math.abs(path.getHeadingByTime(testTimes.get(i))- Math.PI*0.25) < 0.0001);
        }
    }
    
    @Test
    public void testYawRate(){
        List<Coordinate> coor = new ArrayList<Coordinate>(10);
        List<Long> time = new ArrayList<Long>(10);
        coor.add(new Coordinate(0,0));
        coor.add(new Coordinate(10,10));
        coor.add(new Coordinate(20,20));
        coor.add(new Coordinate(30,30));
        coor.add(new Coordinate(40,20));
        coor.add(new Coordinate(50,10));
        coor.add(new Coordinate(60,0));
        
        time.add(0l);
        time.add(1000l);
        time.add(2000l);
        time.add(3000l);
        time.add(4000l);
        time.add(5000l);
        time.add(6000l);
        
        PathAugmenter augment = new PathAugmenter(coor,time);
        assertTrue(augment.getYawRateByTime(3500l) != 0);
    }

}
