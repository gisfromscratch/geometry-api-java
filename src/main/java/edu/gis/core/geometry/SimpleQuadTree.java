package edu.gis.core.geometry;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Simple implementation of a quad tree structure.
 */
public class SimpleQuadTree implements SpatialIndex {

    private static final Envelope sharedExtent = new Envelope();
    private final Envelope xyDomain;
    private final int[] ids;
    private int entryCount;

    private SimpleQuadTree northWest;
    private SimpleQuadTree northEast;
    private SimpleQuadTree southEast;
    private SimpleQuadTree southWest;

    /**
     * Create a new quad tree with the given xy domain.
     * @param xyDomain The extent of all entries.
     * @param maxEntryCount The maximum number of entries before this tree is subdivided.
     */
    public SimpleQuadTree(Envelope xyDomain, int maxEntryCount) {
        this.xyDomain = xyDomain;
        ids = new int[maxEntryCount];
    }

    @Override
    public boolean insert(int id, Geometry geometry) {
        geometry.queryEnvelope(sharedExtent);
        if (!xyDomain.isIntersecting(sharedExtent)) {
            return false;
        }

        if (entryCount == ids.length) {
            return subdivideAndInsert(id, sharedExtent);
        }
        addEntry(id);
        return true;
    }

    @Override
    public Iterator<Integer> intersect(final Geometry geometry) {
        geometry.queryEnvelope(sharedExtent);
        if (!xyDomain.isIntersecting(sharedExtent)) {
            return Collections.emptyIterator();
        }

        final Iterator<Integer> northWestIterator = (null != northWest) ? northWest.intersect(geometry) : null;
        final Iterator<Integer> northEastIterator = (null != northEast) ? northEast.intersect(geometry) : null;
        final Iterator<Integer> southEastIterator = (null != southEast) ? southEast.intersect(geometry) : null;
        final Iterator<Integer> southWestIterator = (null != southWest) ? southWest.intersect(geometry) : null;

        return new Iterator<Integer>() {

            private int index;

            @Override
            public boolean hasNext() {
                if (index < ids.length) {
                    return true;
                }

                if (null != northWestIterator) {
                    if (northWestIterator.hasNext()) {
                        return true;
                    }
                }
                if (null != northEastIterator) {
                    if (northEastIterator.hasNext()) {
                        return true;
                    }
                }
                if (null != southEastIterator) {
                    if (southEastIterator.hasNext()) {
                        return true;
                    }
                }
                if (null != southWestIterator) {
                    if (southWestIterator.hasNext()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Integer next() {
                if (index < ids.length) {
                    return ids[index++];
                }

                if (null != northWestIterator) {
                    if (northWestIterator.hasNext()) {
                        return northWestIterator.next();
                    }
                }
                if (null != northEastIterator) {
                    if (northEastIterator.hasNext()) {
                        return northEastIterator.next();
                    }
                }
                if (null != southEastIterator) {
                    if (southEastIterator.hasNext()) {
                        return southEastIterator.next();
                    }
                }
                if (null != southWestIterator) {
                    if (southWestIterator.hasNext()) {
                        return southWestIterator.next();
                    }
                }

                throw new NoSuchElementException("Iterator has no more elements!");
            }
        };
    }

    private boolean insert(int id, Envelope extent) {
        if (!xyDomain.isIntersecting(extent)) {
            return false;
        }
        addEntry(id);
        return true;
    }

    private void addEntry(int id) {
        ids[entryCount++] = id;
    }

    private boolean subdivideAndInsert(int id, Envelope extent) {
        northWest = new SimpleQuadTree(new Envelope(xyDomain.getXMin(), xyDomain.getCenterY(), xyDomain.getCenterX(), xyDomain.getYMax()), ids.length);
        northEast = new SimpleQuadTree(new Envelope(xyDomain.getCenterX(), xyDomain.getCenterY(), xyDomain.getXMax(), xyDomain.getYMax()), ids.length);
        southEast = new SimpleQuadTree(new Envelope(xyDomain.getCenterX(), xyDomain.getYMin(), xyDomain.getXMax(), xyDomain.getCenterY()), ids.length);
        southWest = new SimpleQuadTree(new Envelope(xyDomain.getXMin(), xyDomain.getYMin(), xyDomain.getCenterX(), xyDomain.getCenterY()), ids.length);

        if (northWest.insert(id, extent)) {
            return true;
        }
        if (northEast.insert(id, extent)) {
            return true;
        }
        if (southEast.insert(id, extent)) {
            return true;
        }
        if (southWest.insert(id, extent)) {
            return true;
        }
        return false;
    }
}
