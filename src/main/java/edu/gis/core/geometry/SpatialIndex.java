package edu.gis.core.geometry;

import com.esri.core.geometry.Geometry;

/**
 * Interface for spatial index implementations.
 */
public interface SpatialIndex {

    /**
     * Tries to insert an entry by using an unique ID and a geometry.
     * @param id The unique ID representing the entry.
     * @param geometry The geometry of the entry.
     * @return true when the entry could be inserted.
     */
    boolean insert(int id, Geometry geometry);

    /**
     * Intersects the entries of this index with the specified geometry.
     * @param geometry The geometry which should be part of the intersection.
     * @return A list of ids which have an intersection with the specified geometry.
     */
    Iterable<Integer> intersect(Geometry geometry);
}
