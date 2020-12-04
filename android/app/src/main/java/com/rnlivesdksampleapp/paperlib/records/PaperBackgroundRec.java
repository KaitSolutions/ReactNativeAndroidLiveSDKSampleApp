package com.rnlivesdksampleapp.paperlib.records;

import android.graphics.Rect;

import java.io.File;
import java.util.HashMap;

/**
 * A holder object for raw paper background drawing instructions and metadata and the wrapper to convert it to a proper Android drawable resource
 */
public class PaperBackgroundRec {

    private byte[] sourceBackgroundData;
    private long pageAddress;
    private int anotoWidth;
    private int anotoHeight;
    private Rect crop;
    private String filename;

    /**
     * The Constructor.
     */
    public PaperBackgroundRec(long address, int aWidth, int aHeight, Rect cropRect, String afdGuid, String cpsFilename) {
        this.pageAddress = address;
        this.anotoWidth = aWidth;
        this.anotoHeight = aHeight;
        this.crop = cropRect;

        if (afdGuid != null && cpsFilename != null) {
            if (cpsFilename.endsWith(".png"))
                cpsFilename = cpsFilename.replace(".png", ".cps");
            if (cpsFilename.endsWith(".eps"))
                cpsFilename = cpsFilename.replace(".eps", ".cps");

            int lastPathComponent = cpsFilename.lastIndexOf(File.separator);
            if (lastPathComponent > -1)
                cpsFilename = cpsFilename.substring(lastPathComponent + 1);

            this.filename = afdGuid + File.separator + cpsFilename;
        }
    }

    public void setSourceData(byte[] data) {
        sourceBackgroundData = data;
    }

    public HashMap<String, Object> getDictionary() {

        if (this.crop == null || this.filename == null || this.sourceBackgroundData == null)
            return null;

        HashMap<String, Object> dictionary = new HashMap<String, Object>();
        dictionary.put("pageAddress", this.pageAddress);
        dictionary.put("anotoWidth", this.anotoWidth);
        dictionary.put("anotoHeight", this.anotoHeight);
        dictionary.put("crop", this.crop);
        dictionary.put("filename", this.filename);
        dictionary.put("data", this.sourceBackgroundData);

        return dictionary;
    }

}
