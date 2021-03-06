package com.esri.core.geometry;

import edu.gis.core.geometry.SimpleQuadTree;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class TestQuadTree extends TestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    @Test
    public void testQuadTreeConstructionAtRandom() {
        Random random = new Random();
        Envelope2D wgs84XYDomain = new Envelope2D(-180, -90, 180, 90);

        final int TreeHeight = 8;
        //QuadTree quadTree = new QuadTree(wgs84XYDomain, TreeHeight);
        SimpleQuadTree simpleQuadTree = new SimpleQuadTree(new Envelope(-180, -90, 180, 90), TreeHeight);

        final int ChunkRegionSize = (int) 1e5;
        final int NumberOfRegions = (int) 100e6;
        Envelope extent = new Envelope();
        for (int regionIndex = 0; regionIndex < NumberOfRegions; regionIndex++) {
            double x = (random.nextDouble() - 0.5) * wgs84XYDomain.getWidth();
            double y = (random.nextDouble() - 0.5) * wgs84XYDomain.getHeight();
            Point location = new Point(x, y);
            //location.queryEnvelope(extent);
            //quadTree.insert(regionIndex, new Envelope2D(extent.getXMin(), extent.getYMin(), extent.getXMax(), extent.getYMax()));
            simpleQuadTree.insert(regionIndex, location);
            if (0 == (regionIndex + 1) % ChunkRegionSize) {
                System.out.print(regionIndex + 1 + " regions created . . . ");
                System.out.println(Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB allocated.");
            }
        }

        System.out.print(NumberOfRegions + " regions created. ");
        System.out.println(Runtime.getRuntime().totalMemory() / 1024 / 1024 + " MB allocated.");
    }

    @Test
    public void testQuadTreeIntersection() {
        final int TreeHeight = 8;
        Envelope xyDomain = new Envelope(-180, -90, 180, 90);
        SimpleQuadTree simpleQuadTree = new SimpleQuadTree(xyDomain, TreeHeight);

        Point lowerLeft = new Point(xyDomain.getXMin(), xyDomain.getYMin());
        Point lowerRight = new Point(xyDomain.getXMax(), xyDomain.getYMin());
        Point upperLeft = new Point(xyDomain.getXMin(), xyDomain.getYMax());
        Point upperRight = new Point(xyDomain.getYMax(), xyDomain.getYMax());
        Point center = xyDomain.getCenter();
        simpleQuadTree.insert(0, lowerLeft);
        simpleQuadTree.insert(1, lowerRight);
        simpleQuadTree.insert(2, upperLeft);
        simpleQuadTree.insert(3, upperRight);
        simpleQuadTree.insert(4, center);

        Iterator<Integer> intersection = simpleQuadTree.intersect(xyDomain);
        assertTrue("The intersection must not be empty!", intersection.hasNext());

        HashSet<Integer> ids = new HashSet<Integer>();
        while (intersection.hasNext()) {
            Integer id = intersection.next();
            ids.add(id);
        }
        assertTrue("Wrong number of intersections!", 5 == ids.size());
    }

	@Test
	public static void test1() {
		Polyline polyline;
		polyline = makePolyline();

		MultiPathImpl polylineImpl = (MultiPathImpl) polyline._getImpl();
		QuadTree quadtree = buildQuadTree_(polylineImpl);

		Line queryline = new Line(34, 9, 66, 46);
		QuadTree.QuadTreeIterator qtIter = quadtree.getIterator();
		assertTrue(qtIter.next() == -1);

		qtIter.resetIterator(queryline, 0.0);

		int element_handle = qtIter.next();
		while (element_handle > 0) {
			int index = quadtree.getElement(element_handle);
			assertTrue(index == 6 || index == 8 || index == 14);
			element_handle = qtIter.next();
		}
		
		Envelope2D envelope = new Envelope2D(34, 9, 66, 46);
		Polygon queryPolygon = new Polygon();
		queryPolygon.addEnvelope(envelope, true);

			qtIter.resetIterator(queryline, 0.0);

		element_handle = qtIter.next();
		while (element_handle > 0) {
			int index = quadtree.getElement(element_handle);
			assertTrue(index == 6 || index == 8 || index == 14);
			element_handle = qtIter.next();
		}
	}

	@Test
	public static void test2() {
		MultiPoint multipoint = new MultiPoint();

		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				multipoint.add(i, j);
			}
		}

		Envelope2D extent = new Envelope2D();
		multipoint.queryEnvelope2D(extent);

		MultiPointImpl multipointImpl = (MultiPointImpl) multipoint._getImpl();
		QuadTree quadtree = buildQuadTree_(multipointImpl);

		QuadTree.QuadTreeIterator qtIter = quadtree.getIterator();
		assertTrue(qtIter.next() == -1);

		int count = 0;
		qtIter.resetIterator(extent, 0.0);

		while (qtIter.next() != -1) {
			count++;
		}

		assertTrue(count == 10000);
	}

	public static Polyline makePolyline() {
		Polyline poly = new Polyline();

		// 0
		poly.startPath(0, 40);
		poly.lineTo(30, 0);

		// 1
		poly.startPath(20, 70);
		poly.lineTo(45, 100);

		// 2
		poly.startPath(50, 100);
		poly.lineTo(50, 60);

		// 3
		poly.startPath(35, 25);
		poly.lineTo(65, 45);

		// 4
		poly.startPath(60, 10);
		poly.lineTo(65, 35);

		// 5
		poly.startPath(60, 60);
		poly.lineTo(100, 60);

		// 6
		poly.startPath(80, 10);
		poly.lineTo(80, 99);

		// 7
		poly.startPath(60, 60);
		poly.lineTo(65, 35);

		return poly;
	}

	static QuadTree buildQuadTree_(MultiPathImpl multipathImpl) {
		Envelope2D extent = new Envelope2D();
		multipathImpl.queryEnvelope2D(extent);
		QuadTree quadTree = new QuadTree(extent, 8);
		int hint_index = -1;
		Envelope2D boundingbox = new Envelope2D();
		SegmentIteratorImpl seg_iter = multipathImpl.querySegmentIterator();
		while (seg_iter.nextPath()) {
			while (seg_iter.hasNextSegment()) {
				Segment segment = seg_iter.nextSegment();
				int index = seg_iter.getStartPointIndex();
				segment.queryEnvelope2D(boundingbox);
				hint_index = quadTree.insert(index, boundingbox, hint_index);
			}
		}

		return quadTree;
	}

	static QuadTree buildQuadTree_(MultiPointImpl multipointImpl) {
		Envelope2D extent = new Envelope2D();
		multipointImpl.queryEnvelope2D(extent);
		QuadTree quadTree = new QuadTree(extent, 8);
		Envelope2D boundingbox = new Envelope2D();
		Point2D pt;

		for (int i = 0; i < multipointImpl.getPointCount(); i++) {
			pt = multipointImpl.getXY(i);
			boundingbox.setCoords(pt.x, pt.y, pt.x, pt.y);
			quadTree.insert(i, boundingbox, -1);
		}

		return quadTree;
	}
}
