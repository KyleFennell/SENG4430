package com.dcaiti.TraceLoader.odometrie;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dcaiti.traceloader.odometrie.blender.TraceOdometrie;
import com.vividsolutions.jts.geom.Coordinate;

public class IntersectionTestCase {

	@Test
	public void test() {
		Coordinate a = new Coordinate(0,0);
		Coordinate b = new Coordinate(1,1);
		Coordinate c = new Coordinate(1,0);
		Coordinate d = new Coordinate(0,1);
		Coordinate e = new Coordinate(0,-1);
		Coordinate f = new Coordinate(0.5,0);
		Coordinate g = new Coordinate(0,0.5);
		Coordinate h = new Coordinate(-1,-2);
		Coordinate i = new Coordinate(-1,0);
		
		assertEquals(true,TraceOdometrie.crosses(a, b, c, d));
		assertEquals(true,TraceOdometrie.crosses(c, b, c, d,true));
		assertEquals(false,TraceOdometrie.crosses(c, b, c, d,false));
		assertEquals(false,TraceOdometrie.crosses(a, d, c, b));
		assertEquals(true,TraceOdometrie.crosses(a, b, f, g));
		assertEquals(true,TraceOdometrie.crosses(a, c, e, d,true));
		assertEquals(false,TraceOdometrie.crosses(a, c, e, d,false));
		assertEquals(false,TraceOdometrie.crosses(h, c, f, b,false));
		assertEquals(false,TraceOdometrie.crosses(h, c, f, b,true));
		assertEquals(false,TraceOdometrie.crosses(a, b, i, e,true));
		assertEquals(false,TraceOdometrie.crosses(a, b, i, e,false));
		assertEquals(false,TraceOdometrie.crosses(e, c, f, b,false));
		assertEquals(false,TraceOdometrie.crosses(e, c, f, b,true));
	}

}
