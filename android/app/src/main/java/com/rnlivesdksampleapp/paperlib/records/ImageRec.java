package com.rnlivesdksampleapp.paperlib.records;


import android.graphics.Rect;

import java.io.Serializable;

/**
 * Record class used to hold the Image specific data.
 */
public class ImageRec implements Serializable {

 	private long pageAddress;
 	private int aWidth;
	private int aHeight;
	private double width;
	private double height;
	private Rect cropArea = null;
	private byte[] bkgndImage;
	private int pageNumber;
	private String pageLabel;

 	/**
 	 * The Default Constructor.
 	 */
 	public ImageRec() {

 	}

 	/**
 	 * The Constructor.
 	 */
 	public ImageRec(long pa, int anotoWidth, int anotoHeight, float width, float height, int pageNumber) {

		this.pageAddress = pa;
		this.aWidth = anotoWidth;
		this.aHeight = anotoHeight;
		this.width = width;
		this.height = height;
		this.pageNumber = pageNumber;
 	}

 	// ***********************************************************************
 	// 							Getters and Setters
 	// ***********************************************************************


	public double getWidth() {
		return this.width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public double getHeight() {
		return this.height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public int getAnotoWidth() {
		return this.aWidth;
	}

	public void setAnotoWidth(int aWidth) {
		this.aWidth = aWidth;
	}

	public int getAnotoHeight() {
		return this.aHeight;
	}

	public void setAnotoHeight(int height) {
		this.aHeight = height;
	}

	public long getPageAddress() {
		return pageAddress;
	}

	public void setPageAddress(long pa) {
		this.pageAddress = pa;
	}

	public Rect getCropArea() {
		return cropArea;
	}

	public void setCropArea(Rect cropArea) {
		this.cropArea = cropArea;
	}

	public byte[] getBkgndImage() {
		return bkgndImage;
	}

	public int getBkgndImageLength() {
		return (bkgndImage != null) ? bkgndImage.length : 0;
	}

	public void setBkgndImage(byte[] data) {
		this.bkgndImage = data;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public String getPageLabel() {
		return pageLabel;
	}

	public void setPageLabel(String pageLabel) {
		this.pageLabel = pageLabel;
	}

	// ***********************************************************************
 	// 							Helper Methods
 	// ***********************************************************************
 	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ImageRec [pageAddress=");
		builder.append(pageAddress);
		builder.append(", width=");
		builder.append(width);
		builder.append(", height=");
		builder.append(height);
		builder.append(", aWidth=");
		builder.append(aWidth);
		builder.append(", aHeight=");
		builder.append(aHeight);
		builder.append(", crop area=");
		builder.append(cropArea);
		builder.append(", Image size=");
		builder.append(getBkgndImageLength() + " bytes");
		builder.append(", number=");
		builder.append(pageNumber);
		builder.append(", label=");
		builder.append(pageLabel);
		builder.append("]");
		return builder.toString();
	}
}
