/*
Copyright (c) 2013 - 2018 Anoto AB. All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

Redistribution of source code must retain the above copyright notice, this list of
conditions and the following disclaimer.

Redistribution in binary form must reproduce the above copyright notice, this list of
conditions and the following disclaimer in the documentation and/or other materials provided
with the distribution.

Neither the name of Anoto AB nor the names of contributors may be used to endorse or promote
products derived from this software without specific prior written permission.

This software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR IMPLIED
CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.
ANOTO AB AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A
RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT
WILL ANOTO AB OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS
SOFTWARE, EVEN IF ANOTO AB HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
*/

package com.rnlivesdksampleapp.penaccess.model;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class CoordinateBundle {
    private Coordinate prevCoordinate;
    private List<Coordinate> coordinates;
    private long pageAddress;

    private CoordinateBundle(List<Coordinate> coordinates, long pageAddress, Coordinate prevCoordinate) {
        this.coordinates = coordinates;
        this.pageAddress = pageAddress;
        this.prevCoordinate = prevCoordinate;
    }

    public CoordinateBundle(long pageAddress, Coordinate prevCoordinate) {
        this.pageAddress = pageAddress;
        this.prevCoordinate = prevCoordinate;
    }

    public void addCoordinate(Coordinate coordinate) {
        if (coordinates == null) {
            coordinates = new ArrayList<>();
        }

        coordinates.add(coordinate);
    }

    public void setPrevCoordinate(Coordinate prevCoordinate) {
        this.prevCoordinate = prevCoordinate;
    }

    public CoordinateBundle copy() {
        return new CoordinateBundle(this.coordinates, this.pageAddress, this.prevCoordinate);
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public long getPageAddress() {
        return pageAddress;
    }

    public Point[] getPoints() {
        int size = coordinates.size();

        boolean hasPrev = prevCoordinate != null;

        Point[] points;
        if (hasPrev) {
            points = new Point[size + 1];
            points[0] = new Point(prevCoordinate.getX(), prevCoordinate.getY());
        } else {
            points = new Point[size];
        }

        for (int i = 0; i < size; i++) {
            Coordinate coordinate = coordinates.get(i);
            int position = hasPrev ? i + 1 : i;
            points[position] = new Point(coordinate.getX(), coordinate.getY());
        }

        return points;
    }

    public Rect getBounds() {
        Rect bounds = null;

        Point[] points = getPoints();
        int size = points.length;

        for (int i = 0; i < size - 1; i++) {
            Point current = points[i];
            Point next = points[i+1];
            if (bounds == null) {
                bounds = new Rect(current.x, current.y, next.x, next.y);
            }

            Rect currentBounds =  new Rect(current.x, current.y, next.x, next.y);
            bounds.union(currentBounds);
        }

        return bounds;
    }
}
