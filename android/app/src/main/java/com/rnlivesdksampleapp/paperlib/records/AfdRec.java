package com.rnlivesdksampleapp.paperlib.records;

import com.rnlivesdksampleapp.paperlib.afd.AFDHelper;

import java.io.Serializable;
import java.util.List;

/**
 * Record class used to hold the <code>AFD</code> data.
 */
public class AfdRec implements Serializable {

	private String guid;
	private String version;
	private String title;
	private int copies;
	private long lowPageAddress;
	private long highPageAddress;
	private List<CopyRec> ranges;
	private List<TemplateRec> templates;
	private int numberOfPagesInEachCopy;

 	/**
 	 * The Default Constructor.
 	 */
 	public AfdRec() {
 		
 	}

 	/**
 	 * The Constructor.
 	 */
 	public AfdRec(String guid, String version, String title, int copies, String startPa, String endPa) {

		this.guid = guid;
		this.version = version;
		this.title = title;
		this.copies = copies;
		this.lowPageAddress = AFDHelper.convertPageAddress2long(startPa);
		this.highPageAddress = AFDHelper.convertPageAddress2long(endPa);
 	}
	public AfdRec(String guid, String version, String title, int copies, long startPa, long endPa) {

		this.guid = guid;
		this.version = version;
		this.title = title;
		this.copies = copies;
		this.lowPageAddress = startPa;
		this.highPageAddress = endPa;
	}

 	// ***********************************************************************
 	// 							Getters and Setters
 	// ***********************************************************************


	public String getGuid() {
		return this.guid;
	}

	public void setGuid(String afdGuid) {
		this.guid = afdGuid;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String afdVersion) {
		this.version = afdVersion;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String afdTitle) {
		this.title = afdTitle;
	}

	public int getCopies() {
		return this.copies;
	}

	public void setCopies(int afdCopies) {
		this.copies = afdCopies;
	}

	public long getLowPageAddress() {
		return this.lowPageAddress;
	}

	public void setLowPageAddress(String lowPA)
	{
		this.lowPageAddress = AFDHelper.convertPageAddress2long(lowPA);
	}

	public void setLowPageAddress(long lLowPA)
	{
		this.lowPageAddress = lLowPA;
	}

	public long getHighPageAddress() {
		return this.highPageAddress;
	}

	public void setHighPageAddress(String highPA) {
		this.highPageAddress = AFDHelper.convertPageAddress2long(highPA);
	}

	public void setHighPageAddress(long lHighPA) {
		this.highPageAddress = lHighPA;
	}

	public List<CopyRec> getCopyList() {
		return this.ranges;
	}

	public void setCopyList(List<CopyRec> crList) {
		this.ranges = crList;
	}

	public List<TemplateRec> getTemplates() {
		return this.templates;
	}

	public void setCopyRanges(List<TemplateRec> tpList) {
		this.templates = tpList;
	}

	public int getNumberOfPagesInEachCopy() {
		return numberOfPagesInEachCopy;
	}

	public void setNumberOfPagesInEachCopy(int numberOfPagesInEachCopy) {
		this.numberOfPagesInEachCopy = numberOfPagesInEachCopy;
	}

	// ***********************************************************************
	// 							Helper Methods
	// ***********************************************************************
 	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AfdRec [guid=");
		builder.append(guid);
		builder.append(", version=");
		builder.append(version);
		builder.append(", title=");
		builder.append(title);
		builder.append(", copies=");
		builder.append(copies);
		builder.append(", lowPageAddress=");
		builder.append(lowPageAddress);
		builder.append(", highPageAddress=");
		builder.append(highPageAddress);
		builder.append(", numberOfPagesInEachCopy=");
		builder.append(numberOfPagesInEachCopy);
		if (ranges != null && ranges.size() > 0) {
			builder.append(", ranges=");
			builder.append(ranges.size());
		}
		if (templates != null && templates.size() > 0) {
			builder.append(", templates=");
			builder.append(templates.size());
		}
		builder.append("]");
		return builder.toString();
	}
}
