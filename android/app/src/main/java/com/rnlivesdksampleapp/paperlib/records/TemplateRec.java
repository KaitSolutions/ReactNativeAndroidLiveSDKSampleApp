package com.rnlivesdksampleapp.paperlib.records;


import java.io.Serializable;

/**
 * Record class used to hold the page template specific data.
 */
public class TemplateRec implements Serializable {

 	private int templateIndex;
 	private int aWidth;
	private int aHeight;
	private double width;
	private double height;
	private String bkgndFile;

 	/**
 	 * The Default Constructor.
 	 */
 	public TemplateRec() {
 		
 	}

 	/**
 	 * The Constructor.
 	 */
 	public TemplateRec(int tempIndex, int anotoWidth, int anotoHeight, float width, float height) {

		this.templateIndex = tempIndex;
		this.aWidth = anotoWidth;
		this.aHeight = anotoHeight;
		this.width = width;
		this.height = height;
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

	public int getPageTemplateIndex() {
		return templateIndex;
	}

	public void setPageTemplateIndex(int tempIdx) {
		this.templateIndex = tempIdx;
	}

	public String getBkgndFile() {
		return bkgndFile;
	}

	public void setBkgndFile(String fileLocation) {
		this.bkgndFile = fileLocation;
	}

 
 	// ***********************************************************************
 	// 							Helper Methods
 	// ***********************************************************************
 	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemplateRec [templateIdx=");
		builder.append(templateIndex);
		builder.append(", width=");
		builder.append(width);
		builder.append(", height=");
		builder.append(height);
		builder.append(", aWidth=");
		builder.append(aWidth);
		builder.append(", aHeight=");
		builder.append(aHeight);
		builder.append(", file=");
		builder.append(bkgndFile);
		builder.append("]");
		return builder.toString();
	}
}
