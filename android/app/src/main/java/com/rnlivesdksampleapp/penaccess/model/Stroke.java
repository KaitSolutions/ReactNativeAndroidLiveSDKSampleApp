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

public class Stroke {
    private long pageAddress;
    private long initialTimestamp;
    private long finalTimestamp;

    private List<Coordinate> coordinates;

    public Stroke(long pageAddress, long initialTimestamp) {
        this.pageAddress = pageAddress;
        this.initialTimestamp = initialTimestamp;
    }

    private Stroke(long pageAddress, long initialTimestamp, long finalTimestamp, List<Coordinate> coordinates) {
        this.pageAddress = pageAddress;
        this.initialTimestamp = initialTimestamp;
        this.finalTimestamp = finalTimestamp;
        this.coordinates = coordinates;
    }

    public void addCoordinate(Coordinate coordinate) {
        if (coordinates == null) {
            coordinates = new ArrayList<>();
        }

        coordinates.add(coordinate);
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public void setFinalTimestamp(long finalTimestamp) {
        this.finalTimestamp = finalTimestamp;
        coordinates.get(coordinates.size() - 1).setTimestamp(finalTimestamp);
    }

    public Rect getStrokeBounds() {
        Rect strokeBounds = null;
        int size = coordinates.size();


        for (int i = 0; i < size - 1; i++) {
            Coordinate current = coordinates.get(i);
            Coordinate next = coordinates.get(i + 1);
            if (strokeBounds == null) {
                strokeBounds = new Rect(current.getX(), current.getY(), next.getX(), next.getY());
            }

            Rect currentBounds = new Rect(current.getX(), current.getY(), next.getX(), next.getY());
            strokeBounds.union(currentBounds);
        }

        return strokeBounds;
    }

    public Point[] getPoints() {
        int size = coordinates.size();
        Point[] points = new Point[size];
        for (int i = 0; i < size; i++) {
            Coordinate coordinate = coordinates.get(i);
            points[i] = new Point(coordinate.getX(), coordinate.getY());
        }

        return points;
    }

    public Stroke copy() {
        return new Stroke(this.pageAddress, this.initialTimestamp,
                this.finalTimestamp, this.coordinates);
    }

    public long getPageAddress() {
        return pageAddress;
    }
}