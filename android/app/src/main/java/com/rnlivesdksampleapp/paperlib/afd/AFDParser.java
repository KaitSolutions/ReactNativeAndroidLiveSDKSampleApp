package com.rnlivesdksampleapp.paperlib.afd;

import android.graphics.Rect;
import android.util.Log;

import com.livescribe.afp.GraphicsElement;
import com.livescribe.afp.PageTemplate;
import com.livescribe.afp.PropertyCollection;
import com.livescribe.afp.Region;
import com.livescribe.afp.Shape;
import com.rnlivesdksampleapp.paperlib.records.AfdRec;
import com.rnlivesdksampleapp.paperlib.records.CopyRec;
import com.rnlivesdksampleapp.paperlib.records.ImageRec;
import com.rnlivesdksampleapp.paperlib.records.PaperBackgroundRec;
import com.rnlivesdksampleapp.paperlib.records.TemplateRec;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The parser apis that read the AFD file in a zip format and sub file and extract the relevant
 * information.
 */
public class AFDParser {

	private final static String TAG = AFDParser.class.getSimpleName();
    private static final boolean DEBUG_LOG = true; //BuildConfig.debugLogging; //true;

	private String mAFDFilePath = null;

	public AFDParser() {
        if (DEBUG_LOG)
            Log.d(TAG, "Default constructor of AFDParser class");
	}

	public String getAfdPath()
	{
		return mAFDFilePath;
	}

	public void setAfdPath(String afdPath) {
		mAFDFilePath = afdPath;
        if (DEBUG_LOG)
            Log.d(TAG, "AFD Path set is " + mAFDFilePath);
	}

	public AfdRec processAfd2(File afdFile) {

		final String METHOD = "LiveSDK > AFD > processAfd2 > ";
		Log.d(TAG, METHOD + "starting process");

		boolean success = true;
		AfdRec newAfdRecord = null;
		
		// validate
		if (afdFile != null && afdFile.exists()) {
			ZipFile	afdZipFile = null;
			com.livescribe.afp.Document theAFPDoc = null;
			String afdGuid = null;
			String afdVer = null;
			String afdTitle = null;
			
			// access is as a zip file
			try {
				afdZipFile = new ZipFile(afdFile); // Zipped AFD file being processed

				// get the APP document
				theAFPDoc = new com.livescribe.afp.Document(afdZipFile.getName());
				afdGuid = theAFPDoc.getGUID();
				afdVer = theAFPDoc.getAFDVersion();
				afdTitle = theAFPDoc.getTitle();
				int afdCopies = theAFPDoc.getCopyCount();
				String startPage = theAFPDoc.getPageStart().toString().substring(2);
				String stopPage = theAFPDoc.getPageStop().toString().substring(2);

				// add the AFD entry to the table
				newAfdRecord = new AfdRec();
				newAfdRecord.setGuid(afdGuid);
				newAfdRecord.setVersion(afdVer);
				newAfdRecord.setTitle(afdTitle);
				newAfdRecord.setLowPageAddress(startPage);
				newAfdRecord.setHighPageAddress(stopPage);
				newAfdRecord.setCopies(afdCopies);
				newAfdRecord.setCopyList(extractPageRangeOfCopies(theAFPDoc));
				newAfdRecord.setNumberOfPagesInEachCopy(newAfdRecord.getCopyList().get(0).getNumberOfPages());

//				Log.i(TAG, METHOD + "New AFD record is " + newAfdRecord.toString());
//				afdDao.persist(newAfdRecord);
//				Log.i(TAG, METHOD + "New AFD record added to the table " + newAfdRecord.toString());
				//Log.i(TAG, METHOD + "Returning AFD record to be added to the table " + newAfdRecord.toString());
			}
			catch (ZipException ze) {
				Log.e(TAG, "Fatal Zip Exception: ", ze);
				success = false;
			}
			catch (Exception ex) {
				Log.e(TAG, "Fatal Exception: ", ex);
				success = false;
			} finally {
				//Log.d(TAG, METHOD + "finally - " + (afdZipFile != null));

				// clean up and release resource
				if (theAFPDoc != null)
					theAFPDoc.close();
				try {
					if (afdZipFile != null) {
						afdZipFile.close();

						// Now also rename this file to correct name 
						renameAfd(afdFile, afdGuid, afdVer, afdTitle);
					}
				} catch (IOException iOE) {
					// no action just cleanup
				}
				//Log.d(TAG, METHOD + "finally - done, success=" + success);
			}
			
		} else {
			success = false; //ResponseCode.AFD_DATA_MISSING;
            if (DEBUG_LOG)
                Log.d(TAG, "AFD file is missing during upload");
		}

		//if (DEBUG_LOG && null != newAfdRecord)
		//	Log.d(TAG, METHOD + "Guid=" + newAfdRecord.getGuid() + ", pages=" + newAfdRecord.getNumberOfPagesInEachCopy() + ", time=" + (System.currentTimeMillis()-startTime) + ", title=" + newAfdRecord.getTitle());
		return newAfdRecord;
	}
	
	private void renameAfd(File file, String guid, String version, String title) {

		final String METHOD = "renameAfd(): ";
		//Log.d(TAG, METHOD + "title=" + title + ", guid=" + guid + ", ver=" + version);
		
		// validate data
		if (file != null && guid != null && !guid.isEmpty()) {
			
			String currentName = file.getName();
			
			if (!currentName.isEmpty()) {
				// validate it has correct format if it starts with guid
				if (currentName.startsWith(getPaperStartFilename(guid))) {

                    if (DEBUG_LOG)
                        Log.d(TAG, METHOD + "File name already contains guid. No action for NOW. currentName=" + currentName + ", guid=" + guid);
					// NOW no action required
					// LATER check if the versions are same, if so then just remove the old one with the new one
					
				} else {
					String newFullPath = file.getAbsolutePath().replace(file.getName(), getPaperFilename(guid));
					if (null != newFullPath) {
						// the current name needs to be overwritten
						// the new name is "<guid>"
						File newFilename = new File(newFullPath);
						if (newFilename.exists()) {
							// LATER rename the previous version of it
							// NOW just delete the old file
							if (newFilename.delete()) {
								newFilename = null;
								//Log.i(TAG, METHOD + "Deleted the old file that already existed with the desired file name " + newFullPath);
							} else {
                                if (DEBUG_LOG)
                                    Log.d(TAG, METHOD + "Failed to delete the old file that already existed with the desired file name " + newFullPath);
							}
						}
					}
					
					// start renaming process
					try {
						if (!file.renameTo(new File(newFullPath))) {
							// failed to rename
                            if (DEBUG_LOG)
                                Log.d(TAG, METHOD + "Unable to rename the file to its desired name - from '" + file.getAbsolutePath() + "' to '" + newFullPath +  "'");
						} else {
							//Log.i(TAG, METHOD + "Renamed the file to its desired name, from '" + file.getAbsolutePath() + "' to '" + newFullPath +  "'");
						}
					} catch (SecurityException se) {
                        if (DEBUG_LOG)
                            Log.e(TAG, METHOD + "Unable to copy the file to its desired name. from '" + file.getAbsolutePath() + "' to '" + newFullPath +  "'");
                        if (DEBUG_LOG)
                            Log.e(TAG, METHOD + "SecurityException: ", se);
						se.printStackTrace();
					}
				}
			} else {
                if (DEBUG_LOG)
                    Log.d(TAG, METHOD + "Current file name does not exist - " + currentName);
			}
		}
	}
	
	public String getPaperFilename(String guid)
	{
		return "AFD" + guid + ".zip";
	}
	private String getPaperStartFilename(String guid)
	{
		return "AFD" + guid;
	}

	public PaperBackgroundRec extractPaperBackground(String afdGuid, String afdVer, long pageAddress) {
		final String METHOD = "extractPaperBackground(): ";
		PaperBackgroundRec paperBackground = null;

		// get afd file
		File afdFile = new File(mAFDFilePath, getPaperFilename(afdGuid));
		// validate
		if (afdFile != null && afdFile.exists()) {

			ZipFile	afdZipFile = null;
			com.livescribe.afp.Document theAFPDoc = null;

			// access is as a zip file
			try {
				afdZipFile = new ZipFile(afdFile); // Zipped AFD file being processed

				// get the APP document
				theAFPDoc = new com.livescribe.afp.Document(afdZipFile.getName());
				String afdTitle = theAFPDoc.getTitle();
				int afdCopies = theAFPDoc.getCopyCount();
				int pageNumber = theAFPDoc.getPage(pageAddress);
				String pageLabel = getPageLabel(theAFPDoc, pageAddress, pageNumber);
				if (DEBUG_LOG)
					Log.d(TAG, METHOD + "Numbers - Title=" + afdTitle + ", Copies=" + afdCopies + ", pageNum=" + pageNumber);

				// count of all PageTemplates available from theAFPDoc
				int theAFPDocTemplateCount = theAFPDoc.getPageTemplateCount();
				if (DEBUG_LOG)
					Log.d(TAG, METHOD + "Numbers - getPageTemplateCount=" + theAFPDocTemplateCount + ", getPatternPageTemplateCount=" + theAFPDoc.getPatternPageTemplateCount());

				// for each template get id and corresponding data
				com.livescribe.afp.PageTemplate aPageTemplate = theAFPDoc.getPageTemplate(pageNumber);
				if (aPageTemplate != null) {

					//Let's start off by getting the basepath value and testing to see if we have an EN Template for it
					String aPageTemplateBasePath = aPageTemplate.getBasePath();
					if (aPageTemplateBasePath != null) {

						// this is the template index
						int aPageTemplateIndex = Integer.parseInt(aPageTemplateBasePath.split("\\/")[1]);

						// just validate the index
						if (aPageTemplateIndex >= 0) {

							Rect cropArea = null;
							for (Region aRegion : aPageTemplate.getRegions()) {
								Shape shp = aRegion.getShape();
								long areaId = (aRegion.getId() >> 8) & 0x0000ffffl;
								//Log.d(TAG, METHOD + "Area=" + areaId + " " + aRegion.toString());
								if (areaId == 0) {
									int[] first = shp.getVertex(0);
									int[] second = shp.getVertex(1);
									cropArea = new Rect(first[0], first[1], second[0], second[1]);
									if (DEBUG_LOG)
										Log.d(TAG, METHOD + "Area=" + areaId + " " + cropArea + " " + aRegion.toString());
								}
							}

							// to create an image object also get dimensions of the image from the AFD
							int anotoWidth = 0;
							int anotoHeight = 0;
                            String sourceFilename = null;
							for (GraphicsElement aPageTemplateGfxElement : aPageTemplate.getGraphicsElements()) {
                                // TODO: this just grabs the dimensions of the first image; double-check that this is sufficient
								if (aPageTemplateGfxElement.getClass().equals(GraphicsElement.Image.class)) {
									if (DEBUG_LOG)
										Log.d(TAG, METHOD + aPageTemplateGfxElement.toString());

									// get the width and height in MM
									anotoWidth = (int) aPageTemplateGfxElement.getWidth();
									anotoHeight = (int) aPageTemplateGfxElement.getHeight();

                                    // get the template image file location in the AFD
                                    String sourcePath = ((GraphicsElement.Image)aPageTemplateGfxElement).getFileName();
                                    if (sourcePath != null) {
                                        int lastSlash = sourcePath.lastIndexOf("\\");
                                        if (lastSlash == -1)
                                            sourceFilename = sourcePath;
                                        else
                                            sourceFilename = sourcePath.substring(lastSlash + 1);
                                    }

                                    break;
								}
							}

							paperBackground = new PaperBackgroundRec(pageAddress, anotoWidth, anotoHeight, cropArea, afdGuid, sourceFilename);

                            if (paperBackground != null) {
                                // get the file data from the actual CPS file and then cache it to disk
                                byte[] data = getTemplateCPS(aPageTemplate, afdZipFile);
                                paperBackground.setSourceData(data);
                            }

                            if (DEBUG_LOG)
								Log.d(TAG, METHOD + "Image [" + afdTitle + "," + afdGuid + "," + afdVer + "," + afdCopies + "] with template index [" + aPageTemplateIndex + "," + aPageTemplateBasePath + "] " + paperBackground.toString());

						} else {
							if (DEBUG_LOG)
								Log.d(TAG, METHOD + "Invalid(skipping) record [" + afdTitle + "," + afdGuid + "," + afdVer + "," + afdCopies + "] with template index [" + aPageTemplateIndex + "," + aPageTemplateBasePath + "]");
						}
					} else {
						if (DEBUG_LOG)
							Log.d(TAG, METHOD + "theAFPDoc [" + afdTitle + "," + afdGuid + "," + afdVer + "," + afdCopies + "] PageTemplate for [" + pageNumber+ "] BasePath value == null.");
					}
				} else {
					if (DEBUG_LOG)
						Log.d(TAG, METHOD + "theAFPDoc [" + afdTitle + "," + afdGuid + "," + afdVer + "," + afdCopies + "] PageTemplate for [" + pageNumber + "] == null.");
				}
			}
			catch (ZipException ze) {
				if (DEBUG_LOG)
					Log.e(TAG, "Fatal Zip Exception: ", ze);
			}
			catch (Exception ex) {
				if (DEBUG_LOG)
					Log.e(TAG, "Fatal Exception: ", ex);
			} finally {
				if (DEBUG_LOG)
					Log.d(TAG, METHOD + "finally - " + (afdZipFile != null));

				// clean up and release resource
				if (theAFPDoc != null)
					theAFPDoc.close();
				try {
					if (afdZipFile != null) {
						afdZipFile.close();
					}
				} catch (IOException iOE) {
					// no action just cleanup
				}
			}

		} else {
			if (DEBUG_LOG)
				Log.d(TAG, METHOD + "AFD file is missing during upload - " + afdFile.getAbsolutePath());
		}


		return paperBackground;
	}

	public ImageRec extractImage(String afdGuid, String afdVer, long pageAddress) {
		return extractImageSelective(afdGuid, afdVer, pageAddress, true, false);
	}

	public ImageRec extractImageSelective(String afdGuid, String afdVer, long pageAddress, boolean needImage, boolean needLabel) {
		final String METHOD = "extractImageSelective(): ";
		ImageRec imageRec = null;

		// get afd file
		File afdFile = new File(mAFDFilePath, getPaperFilename(afdGuid));
		// validate
		if (afdFile != null && afdFile.exists()) {

			ZipFile	afdZipFile = null;
			com.livescribe.afp.Document theAFPDoc = null;
			
			// access is as a zip file
			try {
				afdZipFile = new ZipFile(afdFile); // Zipped AFD file being processed

				// get the APP document
				theAFPDoc = new com.livescribe.afp.Document(afdZipFile.getName());
				String afdTitle = theAFPDoc.getTitle();
				int afdCopies = theAFPDoc.getCopyCount();
				int pageNumber = theAFPDoc.getPage(pageAddress);
				String pageLabel = (needLabel) ? getPageLabel(theAFPDoc, pageAddress, pageNumber) : null;
                if (DEBUG_LOG)
                    Log.d(TAG, METHOD + "Numbers - Title=" + afdTitle + ", Copies=" + afdCopies + ", pageNum=" + pageNumber);

				// count of all PageTemplates available from theAFPDoc
				int theAFPDocTemplateCount = theAFPDoc.getPageTemplateCount();
                if (DEBUG_LOG)
                    Log.d(TAG, METHOD + "Numbers - getPageTemplateCount=" + theAFPDocTemplateCount + ", getPatternPageTemplateCount=" + theAFPDoc.getPatternPageTemplateCount());

				// for each template get id and corresponding data
				com.livescribe.afp.PageTemplate aPageTemplate = theAFPDoc.getPageTemplate(pageNumber);
				if (aPageTemplate != null) {

					//Let's start off by getting the basepath value and testing to see if we have an EN Template for it
					String aPageTemplateBasePath = aPageTemplate.getBasePath();
					//Log.d(TAG, METHOD + "basePath=" + aPageTemplateBasePath);
					if (aPageTemplateBasePath != null) {

						// this is the template index
						int aPageTemplateIndex = Integer.parseInt(aPageTemplateBasePath.split("\\/")[1]);

						// just validate the index
						if (aPageTemplateIndex >= 0) {

							Rect cropArea = null;
							Collection<Region> abc = aPageTemplate.getRegions();
							for (Region aRegion : aPageTemplate.getRegions()) {
								Shape shp = aRegion.getShape();
								long areaId = (aRegion.getId() >> 8) & 0x0000ffffl;
								//Log.d(TAG, METHOD + "Area=" + areaId + " " + aRegion.toString());
								if (areaId == 0) {
									int[] first = shp.getVertex(0);
									int[] second = shp.getVertex(1);
									cropArea = new Rect(first[0], first[1], second[0], second[1]);
                                    if (DEBUG_LOG)
                                        Log.d(TAG, METHOD + "Area=" + areaId + " " + cropArea + " " + aRegion.toString());
								}
//								for (int idx=0; idx < shp.getVertexCount(); idx++) {
//									//Log.d(TAG, METHOD + "Vertex " + idx);
//									int[] coord = shp.getVertex(idx);
//									for (int xy : coord)
//										Log.d(TAG, METHOD + "Vertex " + idx + " is " + xy);
//								}
							}

//							for (GraphicsElement aPageTemplateGfxElement : aPageTemplate.getGraphicsElements()) {
//								Log.d(TAG, METHOD + aPageTemplateGfxElement.toString());
//							}

							// to create an image object also get dimensions of the image from the AFD
							double height = Double.MIN_VALUE;
							double width = Double.MIN_VALUE;
							int anotoWidth = 0;
							int anotoHeight = 0;
							for (GraphicsElement aPageTemplateGfxElement : aPageTemplate.getGraphicsElements()) {
								if (aPageTemplateGfxElement.getClass().equals(GraphicsElement.Image.class)) {
                                    if (DEBUG_LOG)
                                        Log.d(TAG, METHOD + aPageTemplateGfxElement.toString());

									// get the width and height in MM
									anotoWidth = (int) aPageTemplateGfxElement.getWidth();
									anotoHeight = (int) aPageTemplateGfxElement.getHeight();
									width = com.livescribe.util.Units.auToMM(anotoWidth);
									height = com.livescribe.util.Units.auToMM(anotoHeight);
									break;
								}
							}

							// get the actual image data
							byte[] data = (needImage) ? getTemplateBackground(aPageTemplate, afdZipFile) : null;

							// finally got the data for the page
							imageRec = new ImageRec(pageAddress, anotoWidth, anotoHeight, (float) width, (float) height, pageNumber);
							if (cropArea != null) {
								imageRec.setCropArea(cropArea);
							}
							if (null != pageLabel) {
								imageRec.setPageLabel(pageLabel);
							}
							if (null != data) {
								imageRec.setBkgndImage(data);
							}
                            if (DEBUG_LOG)
                                Log.d(TAG, METHOD + "Image [" + afdTitle + "," + afdGuid + "," + afdVer + "," + afdCopies + "] with template index [" + aPageTemplateIndex + "," + aPageTemplateBasePath + "] " + imageRec.toString());

						} else {
                            if (DEBUG_LOG)
                                Log.d(TAG, METHOD + "Invalid(skipping) record [" + afdTitle + "," + afdGuid + "," + afdVer + "," + afdCopies + "] with template index [" + aPageTemplateIndex + "," + aPageTemplateBasePath + "]");
						}
					} else {
                        if (DEBUG_LOG)
                            Log.d(TAG, METHOD + "theAFPDoc [" + afdTitle + "," + afdGuid + "," + afdVer + "," + afdCopies + "] PageTemplate for [" + pageNumber+ "] BasePath value == null.");
					}
				} else {
                    if (DEBUG_LOG)
                        Log.d(TAG, METHOD + "theAFPDoc [" + afdTitle + "," + afdGuid + "," + afdVer + "," + afdCopies + "] PageTemplate for [" + pageNumber + "] == null.");
				}
			}
			catch (ZipException ze) {
                if (DEBUG_LOG)
                    Log.e(TAG, "Fatal Zip Exception: ", ze);
			}
			catch (Exception ex) {
                if (DEBUG_LOG)
                    Log.e(TAG, "Fatal Exception: ", ex);
			} finally {
                if (DEBUG_LOG)
                    Log.d(TAG, METHOD + "finally - " + (afdZipFile != null));

				// clean up and release resource
				if (theAFPDoc != null)
					theAFPDoc.close();
				try {
					if (afdZipFile != null) {
						afdZipFile.close();
					}
				} catch (IOException iOE) {
					// no action just cleanup
				}
			}
			
		} else {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "AFD file is missing during upload - " + afdFile.getAbsolutePath());
		}
		
		return imageRec;
	}

	public String extractLabel(String afdGuid, String afdVer, long pageAddress) {

		final String METHOD = "extractLabel(): ";
		String label = null;

		// get afd file
		File afdFile = new File(mAFDFilePath, getPaperFilename(afdGuid));
		// validate
		if (afdFile != null && afdFile.exists()) {

			ZipFile afdZipFile = null;
			com.livescribe.afp.Document theAFPDoc = null;

			// access is as a zip file
			try {
				afdZipFile = new ZipFile(afdFile); // Zipped AFD file being processed

				// get the APP document
				theAFPDoc = new com.livescribe.afp.Document(afdZipFile.getName());
				int pageIndex = theAFPDoc.getPage(pageAddress);
				label = getPageLabel(theAFPDoc, pageAddress, pageIndex);
                if (DEBUG_LOG)
                    Log.d(TAG, METHOD + "Page - Label=" + label + ", Index=" + pageIndex);

			} catch (ZipException ze) {
                if (DEBUG_LOG)
                    Log.e(TAG, "Fatal Zip Exception: ", ze);
			} catch (Exception ex) {
                if (DEBUG_LOG)
                    Log.e(TAG, "Fatal Exception: ", ex);
			} finally {
                if (DEBUG_LOG)
                    Log.d(TAG, METHOD + "finally - " + (afdZipFile != null));

				// clean up and release resource
				if (theAFPDoc != null)
					theAFPDoc.close();
				try {
					if (afdZipFile != null) {
						afdZipFile.close();
					}
				} catch (IOException iOE) {
					// no action just cleanup
				}
			}

		} else {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "AFD file is missing during upload - " + afdFile.getAbsolutePath());
		}

		return label;
	}

    private byte[] getTemplateCPS(com.livescribe.afp.PageTemplate aPageTemplate, ZipFile afdZipFile) {
        final String METHOD = "getTemplateCPS(): ";
        byte[] paperBackgroundData = null;

        // loop through all the graphic elements in the template and stop as soon as you get the background image
        for (GraphicsElement aPageTemplateGfxElement : aPageTemplate.getGraphicsElements()) {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "Page Template Gfx Element is " + aPageTemplateGfxElement.toString());
            if (aPageTemplateGfxElement.getClass().equals(GraphicsElement.Image.class)) {

                // get the template image file location in the AFD
                String aPageTemplateGfxElementSrcPath = ((GraphicsElement.Image)aPageTemplateGfxElement).getFileName();
                if (aPageTemplateGfxElementSrcPath != null) {

                    // Need to manipulate the src path appropriately
                    // Need to test for file extension (if == .eps,.png -> .cps)
					if (aPageTemplateGfxElementSrcPath.endsWith(".png")) aPageTemplateGfxElementSrcPath = aPageTemplateGfxElementSrcPath.replace(".png", ".cps");
					if (aPageTemplateGfxElementSrcPath.endsWith(".eps")) aPageTemplateGfxElementSrcPath = aPageTemplateGfxElementSrcPath.replace(".eps", ".cps");

                    // read from disk
                    InputStream in;
                    try {
                        in = afdZipFile.getInputStream(afdZipFile.getEntry(aPageTemplateGfxElementSrcPath));

                        byte[] buff = new byte[8000];
                        int bytesRead = 0;
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        while ((bytesRead = in.read(buff)) != -1) {
                            bao.write(buff, 0, bytesRead);
                        }
                        paperBackgroundData = bao.toByteArray();
                        bao.close();

                        in.close();
                        if (DEBUG_LOG)
                            Log.d(TAG, METHOD + "Obtained " + aPageTemplateGfxElementSrcPath + " from AFD. Size=" + paperBackgroundData.length);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        if (DEBUG_LOG)
                            Log.e(TAG, METHOD + "Failed to get " + aPageTemplateGfxElementSrcPath + " from AFD.", ex);
                    }
                }
                break;
            }

        }

        return paperBackgroundData;
    }

    private byte[] getTemplateBackground(com.livescribe.afp.PageTemplate aPageTemplate, ZipFile afdZipFile) {
		final String METHOD = "getTemplateBackground(): ";
		byte[] backgroundImage = null;

		// loop through all the graphic elements in the template and stop as soon as you get the background image
		for (GraphicsElement aPageTemplateGfxElement : aPageTemplate.getGraphicsElements()) {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "Page Template Gfx Element is " + aPageTemplateGfxElement.toString());
			if (aPageTemplateGfxElement.getClass().equals(GraphicsElement.Image.class)) {

				// get the template image file location in the AFD
				String aPageTemplateGfxElementSrcPath = ((GraphicsElement.Image)aPageTemplateGfxElement).getFileName();
				if (aPageTemplateGfxElementSrcPath != null) {

					// Need to manipulate the src path appropriately
					// Need to test for file extension (if == .eps -> .png)
					if (aPageTemplateGfxElementSrcPath.endsWith(".eps")) aPageTemplateGfxElementSrcPath = aPageTemplateGfxElementSrcPath.replace(".eps", ".png");
					
					// read from disk
					InputStream in;
					try {
						in = afdZipFile.getInputStream(afdZipFile.getEntry(aPageTemplateGfxElementSrcPath));

						byte[] buff = new byte[8000];
						int bytesRead = 0;
						ByteArrayOutputStream bao = new ByteArrayOutputStream();
						while ((bytesRead = in.read(buff)) != -1) {
							bao.write(buff, 0, bytesRead);
						}
						backgroundImage = bao.toByteArray();
						bao.close();

						in.close();
                        if (DEBUG_LOG)
                            Log.d(TAG, METHOD + "Obtained " + aPageTemplateGfxElementSrcPath + " from AFD. Size=" + backgroundImage.length);
					} catch (IOException ex) {
						ex.printStackTrace();
                        if (DEBUG_LOG)
                            Log.e(TAG, METHOD + "Failed to get " + aPageTemplateGfxElementSrcPath + " from AFD.", ex);
					}
				}
				break;
			}

		}

		return backgroundImage;
	}

	public byte[] extractNotebookCover(String afdGuid, int copyIndex, int thumbnailSize) {

		final String METHOD = "extractNotebookCover(): ";
		byte[] data = null;

		// get afd file
		File afdFile = new File(mAFDFilePath, getPaperFilename(afdGuid));
		// validate
		if (afdFile != null && afdFile.exists()) {

			ZipFile afdZipFile = null;

			// access is as a zip file
			try {
				// Zipped AFD file being processed
				afdZipFile = new ZipFile(afdFile);

				// get the actual image data bytes
				data = getCoverImage(afdZipFile, copyIndex, thumbnailSize);
			}
			catch (ZipException ze) {
				if (DEBUG_LOG)
					Log.e(TAG, "Fatal Zip Exception: ", ze);
			}
			catch (Exception ex) {
				if (DEBUG_LOG)
					Log.e(TAG, "Fatal Exception: ", ex);
			} finally {
				if (DEBUG_LOG)
					Log.d(TAG, METHOD + "finally - " + (afdZipFile != null));

				// clean up and release resource
				try {
					if (afdZipFile != null) {
						afdZipFile.close();
					}
				} catch (IOException iOE) {
					// no action just cleanup
				}
			}

		} else {
			if (DEBUG_LOG)
				Log.d(TAG, METHOD + "AFD file is missing during upload - " + afdFile.getAbsolutePath());
		}

		return data;
	}

	public ArrayList<CopyRec> extractPageRangeOfCopies(com.livescribe.afp.Document theAFPDoc) {
		final String METHOD = "extractPageRangeOfCopies(): ";
		//Log.d(TAG, METHOD + "starting process");

		ArrayList<CopyRec> copyRecs = new ArrayList<CopyRec>();

		// validate
		if (theAFPDoc != null) {

			try {
				// get the APP document
				int afdCopies = theAFPDoc.getCopyCount();
				
				// do we need to create page range records?
				if (afdCopies > 0) {

					// prepare for the
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();

					// 1. Get the pattern file and verify the root element
					InputStream is = theAFPDoc.getInputStream("main.pattern");
					Document xmlPatternDoc = builder.parse(is);
					Element root = xmlPatternDoc.getDocumentElement();
					//Log.d(TAG, METHOD + "Root is - " + root.getTagName());
					if ("pattern".equalsIgnoreCase(root.getTagName())) {

						// 2. for each pattern get the license file name
						// iterate through child elements of root with element name "item"
						NodeList copyItems = root.getElementsByTagName("item");
						for (int copyIndex=0; copyIndex < copyItems.getLength(); copyIndex++) {
							Node aCopyItem = copyItems.item(copyIndex);

							// 3. Within each copy item we get the start page address but to get the
							// end page address we need to see the license file to complete the page range

							// first get the attributes list of this node
							if (aCopyItem.hasAttributes()) {

								// find the start_pa attribute
								NamedNodeMap attrMap = aCopyItem.getAttributes();
								Node itemNode = attrMap.getNamedItem("start_pa");
								String pageBegin = itemNode.getNodeValue();
								//Log.d(TAG, METHOD + "Copy Item start_pa=" + pageBegin + ", name is - " + aCopyItem.toString());
								// validate begin address presence
								if (null != pageBegin) {

									// get the end page address
									String pageData[] = getPageEndAddress(theAFPDoc, pageBegin);
									// validate stop_pa presence
									if (null != pageData && 2 == pageData.length) {

										// add the new record to the database table
										CopyRec copyRange = new CopyRec();
										copyRange.setCopyIndex(copyIndex);
										copyRange.setLowPageAdress(AFDHelper.convertPageAddress2long(pageBegin));
										copyRange.setHighPageAdress(AFDHelper.convertPageAddress2long(pageData[0]));
										copyRange.setNumberOfPages(Integer.parseInt(pageData[1]));
										copyRecs.add(copyRange);
//										Log.d(TAG, METHOD + "Copy Page Range added to table [" + copyIndex + "] with BeginPA[" + pageBegin + "], endPA[" + pageData[0] + "], count[" + pageData[1] + "]");

									} else {
										Log.e(TAG, METHOD + "Copy Page Range missing in AFD for [" + theAFPDoc.getTitle() + "," + theAFPDoc.getGUID() + "," + theAFPDoc.getAFDVersion() + "," + afdCopies + "] with BeginPA  [" + pageBegin + "] page end [" + pageData.toString() + "], skipping.");
									}
								} else {
                                    if (DEBUG_LOG)
                                        Log.d(TAG, METHOD + "Copy Page Range missing in AFD for [" + theAFPDoc.getTitle() + "," + theAFPDoc.getGUID() + "," + theAFPDoc.getAFDVersion() + "," + afdCopies + "] with BeginPA  [" + pageBegin + "], skipping.");
								}
							}
						}

						//The list is sorted based on start page address and the copy index is set accordingly
					}
				}

				//Log.d(TAG, METHOD + "Number of unique templates found in AFD - " + templateIds.size());
			}
			catch (Exception ex) {
                if (DEBUG_LOG)
                    Log.e(TAG, "Fatal Exception: ", ex);
			}
			
		} else {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "Invalid AFD document passed");
		}
		
		return copyRecs;
	}

	private String[] getPageEndAddress(com.livescribe.afp.Document theAFPDoc, String pageBegin) {

		final String METHOD = "getPageEndAddress(): ";
		String licensePrefix = "licenses/" + pageBegin + "_";
		String licensePostFix = ".license";
		String licenseFile = null;

		InputStream is = null;
		int levelNum = 2;
		
		// first get the license file name, it starts at 2 mainly  
		for (; levelNum < 10; levelNum++ ) {
			licenseFile = licensePrefix + levelNum + licensePostFix;
			try {
				is = theAFPDoc.getInputStream(licenseFile);
				break;
			} catch (IOException e) {
				// do nothing just go to next level
                if (DEBUG_LOG)
                    Log.e(TAG, METHOD + "Need to increase the level by 1 from " + levelNum + ", license file name was " + licenseFile);
				is = null;
			}
		}
		
		try {
			// Within the license file get the page start and end
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlPatternDoc = builder.parse(is);
			Element root = xmlPatternDoc.getDocumentElement();
			//Log.d(TAG, METHOD + "Root is - " + root.getTagName());

			if ("licensecontainer".equalsIgnoreCase(root.getTagName())) {
					
				// iterate through child elements of root with element name "license"
				NodeList licenses = root.getElementsByTagName("license");
				for (int indx=0; indx < licenses.getLength(); indx++) {
					Node aLicense = licenses.item(indx);
					NamedNodeMap attrMap = aLicense.getAttributes();
					Node levelNode = attrMap.getNamedItem("level");
					int levelVal = Integer.parseInt(levelNode.getNodeValue());

					// look for the one with level = 2
					if (levelNum == levelVal) {
						
						// look for a child elements for a pattern with consumer category
						NodeList patterns = aLicense.getChildNodes();// ("pattern");
						for (int indx1=0; indx1 < patterns.getLength(); indx1++) {
							Node node = patterns.item(indx1);
							if (node.getNodeName().equalsIgnoreCase("pattern")) {

								NamedNodeMap patternAttrMap = node.getAttributes();
								Node patternNode = patternAttrMap.getNamedItem("category");
								String category = patternNode.getNodeValue();
								//Log.d(TAG, METHOD + " Node name=" + patternNode.getNodeName() + ", val=" + category);
								if (null != category && category.length() > 0) {

									// look for a child under this element for a pattern with consumer category
									NodeList pageAddressList = node.getChildNodes();
									for (int indx2=0; indx2 < pageAddressList.getLength(); indx2++) {
										Node aPageAddress = pageAddressList.item(indx2);
										if (aPageAddress.getNodeName().equalsIgnoreCase("pageaddress")) {

											NamedNodeMap pageAddrAttrMap = aPageAddress.getAttributes();
											Node startNode = pageAddrAttrMap.getNamedItem("start");
											String start = startNode.getNodeValue();
											Node stopNode = pageAddrAttrMap.getNamedItem("stop");
											String stop = stopNode.getNodeValue();
											Node pagesNode = pageAddrAttrMap.getNamedItem("pagesleft");
											String pagesleft = pagesNode.getNodeValue();
											//Log.d(TAG, METHOD + "Element start=" + start + ", stop=" + stop + ", pagesleft=" + pagesleft + " and name is - " + aPageAddress.getNodeName());
											if (null != start && null != stop && start.length() > 7 && stop.length() > 7 && null != pagesleft && pageBegin.equalsIgnoreCase(start)) {

												String[] data = new String[2];

												data[0] = stop; // pageEnd
												data[1] = pagesleft; // numberOfPages

												//Log.d(TAG, METHOD + "Success return for " + pageBegin + " with " + pageEnd);
												return data;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
            if (DEBUG_LOG)
                Log.e(TAG, METHOD + "Issue with the license file " + licenseFile + " - " + e.getMessage());
			e.printStackTrace();
//		} catch (DocumentException e) {
//			Log.e(TAG, METHOD + "Issue with the XML contents of license file " + licenseFile + " - " + e.getMessage());
//			e.printStackTrace();
		} catch (ParserConfigurationException e) {
            if (DEBUG_LOG)
                Log.e(TAG, METHOD + "Issue with the XML contents of license file " + licenseFile + " - " + e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
            if (DEBUG_LOG)
                Log.e(TAG, METHOD + "Issue with the XML contents of license file " + licenseFile + " - " + e.getMessage());
			e.printStackTrace();
		}

        if (DEBUG_LOG)
            Log.d(TAG, METHOD + "Failure return for " + pageBegin + " with NO info");
		return null;
	}

	public TemplateRec getTemplateIndex(String afdFileName, String pageAddress) {
		final String METHOD = "LiveSDK > AFD > getTemplateIndex > ";
		TemplateRec pageDto = null;
		
		com.livescribe.afp.Document theAFPDoc = null;
		try {
			// get the APP document
		    long lPageAddr = com.livescribe.afp.PageAddress.parse(pageAddress);
			theAFPDoc = new com.livescribe.afp.Document(afdFileName);
		    int pageNum = theAFPDoc.getPage(lPageAddr);
			//Log.d(TAG, METHOD + "pageNum=" + pageNum);
		    int pageCopy = theAFPDoc.getCopy(lPageAddr);
			//Log.d(TAG, METHOD + "pageCopy=" + pageCopy);
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "Page: pa=" + pageAddress + ", pageId=" + lPageAddr + "Num=" + pageNum + ", Copy=" + pageCopy);
		    int templateIndex = 0;
		    PageTemplate thePageTemplate = theAFPDoc.getPageTemplate(pageNum);
			if (thePageTemplate != null) {
				String basePath = thePageTemplate.getBasePath();
                if (DEBUG_LOG)
                    Log.d(TAG, METHOD + "basePath=" + basePath);
				if (basePath != null) {
					
					templateIndex = Integer.parseInt(basePath.split("\\/")[1]);
					
					// create the page dto and fill it up
					pageDto = new TemplateRec();
					if (pageDto != null) {
						pageDto.setPageTemplateIndex(templateIndex);
						pageDto.setAnotoWidth(thePageTemplate.getWidth());
						pageDto.setAnotoHeight(thePageTemplate.getHeight());
                        if (DEBUG_LOG)
                            Log.d(TAG, METHOD + "Template: file=" + pageDto.getBkgndFile() + ", width=" + pageDto.getAnotoWidth() + ", height=" + pageDto.getAnotoHeight());
					}
				}
			}
		}
		catch (Exception ex) {
            if (DEBUG_LOG)
                Log.e(TAG, METHOD + "Fatal Exception: ", ex);
		} finally {
			// clean up and release resource
			if (theAFPDoc != null)
				theAFPDoc.close();
			//Log.d(TAG, METHOD + "finally - done, pa=" + pageAddress);
		}
		
		return pageDto; 
	}

	public String getPageLabel(com.livescribe.afp.Document theAFPDoc, long pageAddress, int logicalPage) {

		final String METHOD = "getPageLabel(): ";
		String label = null;
		try {
			PropertyCollection pc = new PropertyCollection(theAFPDoc.createFile("userdata/pagenumbers.xml").openInputStream());
			label = pc.find(logicalPage);
			if (label == null) {
                if (DEBUG_LOG)
                    Log.d(TAG, METHOD + "No physical page number found, setting logical page (" + logicalPage + ") label to empty for " + theAFPDoc.getTitle());
			}
		} catch (IOException ex) {
            if (DEBUG_LOG)
                Log.e(TAG, METHOD + "AFD did not contain a page number map or unable to get it - " + ex.getMessage());
		}

		return label;
	}

	/*
	public String getStringFromInputStream(InputStream stream, String charsetName) {
		final String METHOD = "getStringFromInputStream(): ";

		StringWriter writer = new StringWriter();
		try {
			InputStreamReader reader = new InputStreamReader(stream, charsetName);

			int bytesRead = 0;
			char[] buffer = new char[1024*16];
			while (-1 != (bytesRead = reader.read(buffer))) {
				writer.write(buffer, 0, bytesRead);
			}
		} catch (Exception ex) {
			Log.e(TAG, METHOD + "Exception while reading InputStream ", ex);
		}

		return writer.toString();
	}
	*/

	private byte[] getCoverImage(ZipFile afdZipFile, int copyIndex, int thumbSize) {

		final String METHOD = "getCoverImage(): ";
		byte[] coverImage = null;
		final String coverFileName = "/active-" + thumbSize + ".png";

		// get the cover image file location in the AFD
		String coverSrcFullName = "userdata/cover" + coverFileName;
		ZipEntry zEntry = afdZipFile.getEntry(coverSrcFullName);
		if (null == zEntry) {
			coverSrcFullName = "userdata/covers" + "/" + copyIndex + coverFileName;
			zEntry = afdZipFile.getEntry(coverSrcFullName);
		}

		// read the entry in the zip file.
		if (null != zEntry) {

			// read from disk
			InputStream in;
			try {
				in = afdZipFile.getInputStream(zEntry);

				byte[] buff = new byte[8000];
				int bytesRead = 0;
				ByteArrayOutputStream bao = new ByteArrayOutputStream();
				while ((bytesRead = in.read(buff)) != -1) {
					bao.write(buff, 0, bytesRead);
				}
				coverImage = bao.toByteArray();
				bao.close();

				in.close();
				if (DEBUG_LOG)
					Log.d(TAG, METHOD + "Cover image obtained " + coverSrcFullName + " from AFD. Image size=" + coverImage.length);

			} catch (IOException ex) {
				//ex.printStackTrace();
				if (DEBUG_LOG)
					Log.e(TAG, METHOD + "Failed to get " + coverSrcFullName + " from AFD.", ex);
			}
		} else {
			if (DEBUG_LOG)
				Log.d(TAG, METHOD + "Failed to obtained cover image for " + coverSrcFullName + " from AFD.");
		}

		return coverImage;
	}

}
