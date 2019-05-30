package com.dcaiti.TraceLoader.odometrie;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.dcaiti.traceloader.odometrie.numbermapping.LinearMapping;

public class TestLinearMapping {

    private static LinearMapping lin;
    
    @BeforeClass
    public static void setUpBeforeClass(){
        
    }
    
    
    @Test
    public void testWithoutOffset() {
        lin = new LinearMapping(0,100);
        assertTrue(lin.value(0) == 0);
        assertTrue(lin.value(-1) == 0);
        assertTrue(lin.value(100) == 1);
        assertTrue(lin.value(101) == 1);
        
        assertTrue(lin.value(50) == 0.5);
        assertTrue(lin.value(75) == 0.75);
        assertTrue(lin.value(1) == 0.01);
    }
    
    @Test
    public void testWithOffset() {
        lin = new LinearMapping(50,100, 0.5, 1);        
        assertTrue(lin.value(50) == 0.5);
        assertTrue(lin.value(75) == 0.75);
        assertTrue(lin.value(74) == 0.74);
        assertTrue(lin.value(49) == 0.5);
        assertTrue(lin.value(100) == 1);
        assertTrue(lin.value(101) == 1);
        
    }
    
    
    

}
