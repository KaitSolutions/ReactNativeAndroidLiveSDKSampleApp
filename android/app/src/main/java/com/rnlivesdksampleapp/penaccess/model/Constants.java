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

public class Constants {
    public static class Notify {
        public static final byte TYPE_NEWSESSION = (byte) 0x02;
        public static final byte TYPE_COORD = (byte) 0x03;
        public static final byte TYPE_PENUP = (byte) 0x04;
        public static final byte TYPE_ACTIONAREA = (byte) 0x05;
        public static final byte TYPE_PENDOWN = (byte) 0x06;
        public static final byte TYPE_COORD_WITH_TIMESTAMP = (byte) 0x07;

        public static final int MY_PERMISSIONS_REQUEST_ALL            = 0;
    }
}
