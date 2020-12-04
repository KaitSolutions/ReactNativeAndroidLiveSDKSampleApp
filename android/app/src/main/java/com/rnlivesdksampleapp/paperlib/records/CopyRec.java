package com.rnlivesdksampleapp.paperlib.records;


import java.io.Serializable;

/**
 * Record class used to hold the <code>AFD Copy</code> data.
 */
public class CopyRec implements Serializable {

	private int copyIndex;
 	private long lowPageAddress;
 	private long highPageAddress;
	private int numberOfPages;

 	/**
 	 * The Default Constructor.
 	 */
 	public CopyRec() {
 		
 	}

 	/**
 	 * The Constructor.
 	 */
 	public CopyRec(int copy, long lowPA, long highPA) {

		this.copyIndex = copy;
		this.lowPageAddress = lowPA;
		this.highPageAddress = highPA;
 	}

 	// ***********************************************************************
 	// 							Getters and Setters
 	// ***********************************************************************


	public int getCopyIndex() {
		return copyIndex;
	}

	public void setCopyIndex(int copyIndex) {
		this.copyIndex = copyIndex;
	}

	public long getLowPageAddress() {
		return lowPageAddress;
	}

	public void setLowPageAdress(long lowPA) {
		this.lowPageAddress = lowPA;
	}

	public long getHighPageAddress() {
		return highPageAddress;
	}

	public void setHighPageAdress(long highPA) {
		this.highPageAddress = highPA;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

 	// ***********************************************************************
 	// 							Helper Methods
 	// ***********************************************************************
 	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CopyRec [copyIndex=");
		builder.append(this.copyIndex);
		builder.append(", start=");
		builder.append(lowPageAddress);
		builder.append(", end=");
		builder.append(highPageAddress);
		builder.append(", count=");
		builder.append(numberOfPages);
		builder.append("]");
		return builder.toString();
	}
}
