package edu.gis.core.geometry;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;

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
