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

import com.google.gson.annotations.SerializedName;

public class JsonNotification {

    @SerializedName("version")
    private long version;

    @SerializedName("pen_id")
    private long penId;

    @SerializedName("page_address")
    private long pageAddress;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("x")
    private int x;

    @SerializedName("y")
    private int y;

    @SerializedName("pressure")
    private int pressure;

    @SerializedName("message_type")
    private int messageType;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getPenId() {
        return penId;
    }

    public void setPenId(long penId) {
        this.penId = penId;
    }

    public long getPageAddress() {
        return pageAddress;
    }

    public void setPageAddress(long pageAddress) {
        this.pageAddress = pageAddress;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public boolean isDrawingElement() {
        return messageType == Constants.Notify.TYPE_PENDOWN ||
                messageType == Constants.Notify.TYPE_COORD ||
                messageType == Constants.Notify.TYPE_COORD_WITH_TIMESTAMP ||
                messageType == Constants.Notify.TYPE_PENUP;
    }

    public boolean hasNoPressure() {
        return pressure == 0;
    }


}
