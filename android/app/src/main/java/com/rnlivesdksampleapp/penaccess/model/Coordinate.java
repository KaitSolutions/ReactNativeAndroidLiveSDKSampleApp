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

public class Coordinate extends Point {
    private long pressure;
    private long timestamp;

    public static Coordinate build(JsonNotification jsonNotification, long timestamp) {
        return new Coordinate(jsonNotification.getX(),
                jsonNotification.getY(),
                jsonNotification.getPressure(),
                timestamp
        );
    }

    public static Coordinate build(JsonNotification jsonNotification) {
        return build(jsonNotification, jsonNotification.getTimestamp());
    }

    private Coordinate(int x, int y, int pressure) {
        super(x, y);
        this.pressure = pressure;
    }

    Coordinate(int x, int y, int pressure, long time) {
        this(x, y, pressure);
        this.timestamp = time;
    }

    /**
     * Getters
     */

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getX() {
        return x;
    }


    public int getY() {
        return y;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
