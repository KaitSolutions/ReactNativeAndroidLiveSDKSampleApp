package com.rnlivesdksampleapp.paperlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;


import com.livescribe.afp.Document;
import com.livescribe.afp.PageAddress;
import com.livescribe.afp.PageTemplate;
import com.livescribe.afp.Region;
import com.livescribe.afp.Shape;
import com.livescribe.util.RegionID;
import com.livescribe.util.Regions;
import com.rnlivesdksampleapp.paperlib.afd.AFDHelper;
import com.rnlivesdksampleapp.paperlib.afd.AFDParser;
import com.rnlivesdksampleapp.paperlib.records.AfdRec;
import com.rnlivesdksampleapp.paperlib.records.CopyRec;
import com.rnlivesdksampleapp.paperlib.records.ImageRec;
import com.rnlivesdksampleapp.paperlib.records.PaperBackgroundRec;
import com.rnlivesdksampleapp.paperlib.records.TemplateRec;

import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * AFD document handler code that manages the list of AFDs and provides data related to it.
 */
public class AfdHandler {

	private final static String TAG = AfdHandler.class.getSimpleName();
	private static final boolean DEBUG_LOG = true; //BuildConfig.debugLogging;

	private static AfdHandler mInstance = null;
	private static Context mContext;
	private static String mAfdFolder;

	private List<AfdRec> mAfdTable;
	private AFDParser mAfdParser;

	private ArrayList<Document> documents;

	private AfdHandler() {}

	/**
	 * Get Instance of AFDHandler
	 * @param context
	 * @return
	 */
	public static AfdHandler getInstance(Context context) {
		// the handler will get created only when the context is provided
		if ((null == mInstance) && (null != context)) {
			mInstance = new AfdHandler();
			mContext = context;
			mInstance.init();
		}

		Log.d(TAG,"LiveSDK > AFD > Get AfdHandler instance()");

		return mInstance;
	}

    public PaperBackgroundRec getPaperBackground(long pageAddress) {
        return getPaperBackground(0, null, pageAddress);
	}

	public ImageRec getPageBackground(String pageAddress) {
		return getPageBackground(0, null, pageAddress);
	}

	public ImageRec getPageBackground(long pageAddress) {
		//getPageAfdLocation(pageAddress); for Test
		return getPageBackground(0, null, AFDHelper.convertPageAddress2String(pageAddress));
	}

	public String getPageLabel(long pageAddress) {
		return getPageLabel(0, null, AFDHelper.convertPageAddress2String(pageAddress));
	}

	public AfdRec getAfdInfo(long pageAddress) {
		return getAfdInfo(0, null, pageAddress);
	}

	public Hashtable getPageInfo(long pageAddress) {
		return getPageData(pageAddress);
	}

	public Hashtable getDocumentInfo(String guid) {
		return getAfdData(guid);
	}

	public String getPageAfdLocation(long pageAddress) {
		return getPageAfdLocation(0, null, pageAddress);
	}

	public Bitmap getNotebookCover(String afdGuid, int copyIndex, int thumbnailSize) {
		return getNotebookCover(0, null, afdGuid, copyIndex, thumbnailSize);
	}

	/**
	 * Search for regions using the afds to get the regionID where the strokes are done
	 *
	 * @param pageAddressLong
	 * @param pen_coordinates
	 * @return RegionID
	 */
	public Region searchRegions(long pageAddressLong, Point pen_coordinates) {

		for (Document doc : documents) {
			if (doc.getPageStart().getId() <= pageAddressLong && doc.getPageStop().getId() >= pageAddressLong) {
				int pageNumber;
				try {
					pageNumber = doc.getPage(pageAddressLong);
				} catch (IndexOutOfBoundsException e) {
					// Keep searching as the document found is not the correct one. Some notebooks have more than one
					// afd, for instance, some have one afd for the covers and another for the pages
					continue;
				}

				Map<Long,String> appIdToAppMap = new HashMap<>();
				try {
					appIdToAppMap = Regions.loadAppMap(doc);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				}

				PageTemplate page = doc.getPageTemplate(pageNumber);

				if (page != null) {
					for (Region region : page.getRegions()) {
						Shape shape = region.getShape();
						long id = region.getId();
						long appId = RegionID.getAppInstanceId(id);
						region.setPenletClassName(appIdToAppMap.get(appId));

						if (shape instanceof Shape.Point) {
							int[] center = shape.getVertex(0);
							boolean matchPoint = (center[0] == pen_coordinates.x) && (center[1] == pen_coordinates.y);
							if (matchPoint) {
//								result = RegionID.getAppAreaId(id);
								return region;
							}
						} else if (shape instanceof Shape.Ellipse) {
							int[] center = shape.getVertex(0);
							int[] distance = shape.getVertex(1);

							double rx = Math.abs(distance[0] - center[0]);
							double ry = Math.abs(distance[1] - center[1]);

							Point normalized = new Point(pen_coordinates.x - center[0], pen_coordinates.y - center[1]);

							boolean matchEllipse = (normalized.x * normalized.x) / (rx * rx) + (normalized.y * normalized.y) / (ry * ry) <= 1;
							if (matchEllipse) {

								return region;
							}
						} else if (shape instanceof Shape.LineSegment) {
							Point from = new Point(shape.getVertex(0)[0], shape.getVertex(0)[1]);
							Point to = new Point(shape.getVertex(1)[0], shape.getVertex(1)[1]);

							boolean matchLineSegment = (distance(from, pen_coordinates) + distance(to, pen_coordinates)) == distance(from, to);

							if (matchLineSegment) {

								return region;
							}
						} else if (shape instanceof Shape.Rectangle) {
							Point a = new Point(shape.getVertex(0)[0], shape.getVertex(0)[1]);
							double width = Math.abs(shape.getVertex(1)[0] - shape.getVertex(0)[0]);
							double height = Math.abs(shape.getVertex(1)[1] - shape.getVertex(0)[1]);

							boolean matchRectTangle = (a.x <= pen_coordinates.x) &&
									(pen_coordinates.x < (a.x + width)) &&
									(a.y <= pen_coordinates.y) &&
									(pen_coordinates.y < (a.y + height));

							if (matchRectTangle) {

								return region;
							}
						}
					}
				}
			}
		}

		return null;
	}

	public PageTemplate getPageTemplate(long pageAddressLong) {

		for (Document doc : documents) {
			if (doc.getPageStart().getId() <= pageAddressLong && doc.getPageStop().getId() >= pageAddressLong) {
				int pageNumber;
				try {
					pageNumber = doc.getPage(pageAddressLong);
				} catch (IndexOutOfBoundsException e) {
					// Keep searching as the document found is not the correct one. Some notebooks have more than one
					// afd, for instance, some have one afd for the covers and another for the pages
					continue;
				}

				PageTemplate page = doc.getPageTemplate(pageNumber);

				return page;
			}
		}

		return null;
	}

	/**
	 * AFD Handler init
	 */
	private void init() {
		if (DEBUG_LOG)
			Log.d(TAG, "Starting AFD Parsing to load Paper Products in a Background thread.");

		mInstance.mAfdParser = new AFDParser();

		// copy all the afd assets to the cache folder
		// and process it to build the Afd table in this process.
		File paperAfdFolder = AFDHelper.getPaperAFDFolder(mContext);
		if (paperAfdFolder != null) { // Currently the AFD fodler is located on the external cache dir which is not always available G.T.
			mInstance.mAfdFolder = paperAfdFolder.getAbsolutePath();
			mInstance.mAfdParser.setAfdPath(mAfdFolder);

			// Process the loading of the AFd information during the first get instance call
			LoadPaperProducts loadTask = new LoadPaperProducts();
			loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	/**
	 * Load All AFD Products
	 * @return
	 */
	private boolean loadAllPaperProducts() {
		final String METHOD = "LiveSDK > AFD > loadAllPaperProducts > ";
		boolean success = false;
		documents = new ArrayList<>();

		File paperAfdFolder = AFDHelper.getPaperAFDFolder(mContext);
		if (paperAfdFolder == null) {
			Log.e(TAG, METHOD + "paperAfdFolder does not exist!");
			return success;
		}

		if (DEBUG_LOG) {
			Log.d(TAG, METHOD + "to - " + paperAfdFolder.getAbsolutePath());
		}


		String[] afdNames = null;
		try {
			afdNames = mContext.getAssets().list("afds");
			for (String afdName : afdNames) {

				// what will be its destination name, if it exist then skip this asset
				String destFileName = paperAfdFolder.getAbsolutePath() + "/" + afdName + ".zip";
				if (!AFDHelper.fileExist(destFileName)) {

					// now create a new temporary file on the device cache area
					File fileCopy = File.createTempFile(afdName, ".zip", paperAfdFolder);
					//afdFilePath = fileCopy.getAbsolutePath();
					Log.d(TAG, METHOD + "AFD asset will be copied to " + fileCopy.getAbsolutePath()/* + ", OR " + fileCopy.getCanonicalPath()*/);

					try {
						InputStream stream = mContext.getAssets().open("afds/" + afdName);
						if (stream != null) {

							//byte[] buffer = new byte[4096];

							FileOutputStream fOut = new FileOutputStream(fileCopy);

							int size = stream.available();
							byte[] buffer = new byte[size];
							Log.d(TAG, METHOD + "Source " + afdName + " has length of " + size + " bytes" + ", buffer length=" + buffer.length);

							// read input
							stream.read(buffer);

							// write out put
							BufferedOutputStream bufferOut = new BufferedOutputStream(fOut, buffer.length);
							//while ((size = stream.read(buffer, 0, buffer.length)) != -1) {
							bufferOut.write(buffer, 0, size);
							//}
							bufferOut.flush();
							bufferOut.close();

							fOut.close();
							stream.close();

							// Save to list of Documents to search for regions or other content
							Document document = new Document(fileCopy.getAbsolutePath());
							documents.add(document);

							// process the AFD file and load the record
							loadPaperProduct(fileCopy);

						}
					} catch (IOException e) {
						if (DEBUG_LOG)
							Log.e(TAG, METHOD + "Exception occurred during copying an asset - " + afdName, e);
						e.printStackTrace();
					}
				}
			}

		} catch (IOException ex) {
			if (DEBUG_LOG)
				Log.e(TAG, METHOD + "Exception occurred during creating asset list.", ex);
		}

		// Save the table so that we don't need to load it again
		if (mAfdTable != null) {
			if (DEBUG_LOG) {
				Log.d(TAG, METHOD + "AFD Table has " + mAfdTable.size() + " entries");
				//for (int i = 0; i < mAfdTable.size(); i++)
				//	Log.d(TAG, METHOD + "AFD Table " + i + " " + mAfdTable.get(i).toString());
			}
		} else {
			Log.e(TAG, METHOD + "AFD Table is EMPTY");
		}

		return success;
	}

	private boolean loadPaperProduct(File afdFile) {

		final String METHOD = "uploadAfd(): ";
		boolean success = false;

		ArrayList<TemplateRec> templateRecList = new ArrayList<TemplateRec>();

		Log.d(TAG,"AFD > loadPaperProduct");

		// Ensure that it is a AFD file and rename it with the GUID + Title as the file name here
		long afdRecId = 0l;
		AfdRec newAfdRec = mAfdParser.processAfd2(afdFile);
		if (null != newAfdRec) {

			// check for existing ones, if any
			AfdRec existingAfd = AFDHelper.getAfdfromGuidAndVersion(mAfdTable, newAfdRec.getGuid(), newAfdRec.getVersion());
			if (existingAfd != null) {

				if (DEBUG_LOG)
					Log.d(TAG, METHOD + "Uploaded AFD record already exists. Found an AFD entry with guid=" + existingAfd.getGuid() + ", ver=" + existingAfd.getVersion());
				// update the record and merge it
				existingAfd.setTitle(newAfdRec.getTitle());
				existingAfd.setLowPageAddress(newAfdRec.getLowPageAddress());
				existingAfd.setHighPageAddress(newAfdRec.getHighPageAddress());
				existingAfd.setCopies(newAfdRec.getCopies());
				success = true;

			} else {

				// check if this a first one being loaded
				if (null == mAfdTable)
					mAfdTable = new ArrayList<AfdRec>();

				// add the new one, if needed
				//Log.d(TAG, METHOD + mAfdTable.size() + ". New AFD record being added to table with guid=" + newAfdRec.getGuid() + ", ver=" + newAfdRec.getVersion() + ", title=" + newAfdRec.getTitle());

				// add a new entry here
				mAfdTable.add(newAfdRec);
				success = true;
			}

		}

		return success;
	}

	/**
	 * Get AFD files from local storage
	 * @param penSerialNumber
	 * @param penFirmwareVersion
	 * @param afdGuid
	 * @param pageAddress
	 * @return
	 */
	private AfdRec getPenAFDLocal(long penSerialNumber, String penFirmwareVersion, String afdGuid, String pageAddress) {
		String METHOD = "AFD > getPenAFDLocal > ";

		Log.d(TAG, METHOD + "pen=" + penSerialNumber + ", fver=" + penFirmwareVersion + ", guid=" + afdGuid + ", page=" + pageAddress);

		AfdRec anAfd = null;

		if ((null != afdGuid) && (afdGuid.length() > 2) && afdGuid.startsWith("0x")) {
			// get the all afds, if needed
			if (null == mAfdTable) {
				//getAllPenAFDsLocal(penSerialNumber, penFirmwareVersion);
				loadAllPaperProducts();
			}

			if (null != mAfdTable) {
				// get the specific afd from the page begin and end from the
				anAfd = AFDHelper.getAfdFromGuid(mAfdTable, afdGuid);

			} else {
				if (DEBUG_LOG)
					Log.d(TAG, METHOD + "Unable to get the all afds from the database.");
			}

		} else if ((null != pageAddress) && (pageAddress.length() > 6) && AFDHelper.isValidPageAddress(pageAddress)) {

			// get the all afds, if needed
			if (null == mAfdTable) {
				loadAllPaperProducts();
				//getAllPenAFDsLocal(penSerialNumber, penFirmwareVersion);
			}

			if (null != mAfdTable) {
				// get the specific afd from the page begin and end from the
				anAfd = AFDHelper.getAfdFromPage(mAfdTable, new PageAddress(pageAddress));
			} else {
				if (DEBUG_LOG)
					Log.d(TAG, METHOD + "Unable to get the all afds from the database.");
			}

		} else {
			if (DEBUG_LOG)
				Log.d(TAG, METHOD + "Both guid and page address are missing, at least one is needed.");
		}

		return anAfd;
	}

	private PaperBackgroundRec getPaperBackground(long penSerialNumber, String penFirmwareVersion, long pageAddress) {
        String METHOD = "getPaperBackground(): ";

        if (DEBUG_LOG)
            Log.d(TAG, METHOD + "pen=" + penSerialNumber + ", fver=" + penFirmwareVersion + ", page=" + pageAddress);

        AfdRec anAfd = getPenAFDLocal(penSerialNumber, penFirmwareVersion, null, AFDHelper.convertPageAddress2String(pageAddress));
        if (null != anAfd) {

            PaperBackgroundRec background = mAfdParser.extractPaperBackground(anAfd.getGuid(), anAfd.getVersion(), pageAddress);
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "ImageRec - guid=" + anAfd.getGuid() + ", ver=" + anAfd.getVersion() + ", title=" + anAfd.getTitle() + ", copies=" + anAfd.getCopies());

            return (background);
        } else {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "AFD not found in the database for page address - " + pageAddress);
        }

        return null;
	}

	private ImageRec getPageBackground(long penSerialNumber, String penFirmwareVersion, String pageAddress) {
		String METHOD = "getPageBackground(): ";

        if (DEBUG_LOG)
            Log.d(TAG, METHOD + "pen=" + penSerialNumber + ", fver=" + penFirmwareVersion + ", page=" + pageAddress);

		AfdRec anAfd = getPenAFDLocal(penSerialNumber, penFirmwareVersion, null, pageAddress);
		if (null != anAfd) {

			ImageRec bkgndImageRec = mAfdParser.extractImage(anAfd.getGuid(), anAfd.getVersion(), AFDHelper.convertPageAddress2long(pageAddress));
            if (bkgndImageRec != null) {
				if (DEBUG_LOG)
					Log.d(TAG, METHOD + "ImageRec - guid=" + anAfd.getGuid() + ", ver=" + anAfd.getVersion() + ", title=" + anAfd.getTitle() + ", copies=" + anAfd.getCopies() + ", imageSize=" + bkgndImageRec.getBkgndImageLength());

				return (bkgndImageRec);
			} else {
				Log.w(TAG, METHOD + "ImageRec is null!");
			}
		} else {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "AFD not found in the database for page address - " + pageAddress);
		}

		return null;
	}

	private ImageRec getPageDetails(long penSerialNumber, String penFirmwareVersion, String pageAddress) {
		String METHOD = "getPageDetails(): ";

		if (DEBUG_LOG)
			Log.d(TAG, METHOD + "pen=" + penSerialNumber + ", fver=" + penFirmwareVersion + ", page=" + pageAddress);

		AfdRec anAfd = getPenAFDLocal(penSerialNumber, penFirmwareVersion, null, pageAddress);
		if (null != anAfd) {

			ImageRec bkgndImageRec = mAfdParser.extractImageSelective(anAfd.getGuid(), anAfd.getVersion(), AFDHelper.convertPageAddress2long(pageAddress), false, true);
			if (bkgndImageRec != null) {
				if (DEBUG_LOG)
					Log.d(TAG, METHOD + "ImageRec - guid=" + anAfd.getGuid() + ", ver=" + anAfd.getVersion() + ", title=" + anAfd.getTitle() + ", copies=" + anAfd.getCopies() + ", imageSize=" + bkgndImageRec.getBkgndImageLength());

				return (bkgndImageRec);
			} else {
				Log.w(TAG, METHOD + "ImageRec is null!");
			}
		} else {
			if (DEBUG_LOG)
				Log.d(TAG, METHOD + "AFD not found in the database for page address - " + pageAddress);
		}

		return null;
	}

	private String getPageLabel(long penSerialNumber, String penFirmwareVersion, String pageAddress) {
		String METHOD = "getPageLabel(): ";

        if (DEBUG_LOG)
            Log.d(TAG, METHOD + "pen=" + penSerialNumber + ", fver=" + penFirmwareVersion + ", page=" + pageAddress);

		AfdRec anAfd = getPenAFDLocal(penSerialNumber, penFirmwareVersion, null, pageAddress);
		if (null != anAfd) {

			String label = mAfdParser.extractLabel(anAfd.getGuid(), anAfd.getVersion(), AFDHelper.convertPageAddress2long(pageAddress));
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "guid=" + anAfd.getGuid() + ", ver=" + anAfd.getVersion() + ", title=" + anAfd.getTitle() + ", copies=" + anAfd.getCopies() + ", label=" + label);

			return (label);
		} else {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "AFD not found in the database for page address - " + pageAddress);
		}

		return null;
	}

	private class LoadPaperProducts extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {

			Log.d(TAG,"LiveSDK > loadAllPaperProducts");
			mInstance.loadAllPaperProducts();
			return null;
		}
	}

	private AfdRec getAfdInfo(long penSerialNumber, String penFirmwareVersion, long pageAddress) {
		String METHOD = "getAfdInfo(): ";
		String strPageAddr = AFDHelper.convertPageAddress2String(pageAddress);

		//Log.d(TAG, METHOD + "pen=" + penSerialNumber + ", fver=" + penFirmwareVersion + ", page=" + pageAddress + ", page=" + strPageAddr);

		AfdRec anAfd = getPenAFDLocal(penSerialNumber, penFirmwareVersion, null, strPageAddr);
		if (null == anAfd) {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "AFD not found in the database for page address - " + pageAddress);
		}

		return anAfd;
	}

	private String getPageAfdLocation(long penSerialNumber, String penFirmwareVersion, long pageAddress) {
		String METHOD = "getPageBackgroundAfd(): ";
		String afdPath = null;

		//Log.d(TAG, METHOD + "pen=" + penSerialNumber + ", fver=" + penFirmwareVersion + ", page=" + pageAddress);

		AfdRec anAfd = getPenAFDLocal(penSerialNumber, penFirmwareVersion, null, AFDHelper.convertPageAddress2String(pageAddress));
		if (null != anAfd) {

			afdPath = mAfdFolder + "/" + mAfdParser.getPaperFilename(anAfd.getGuid());
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "Path - guid=" + anAfd.getGuid() + ", title=" + anAfd.getTitle() + ", path=" + afdPath);

		} else {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "AFD not found in the database for page address - " + pageAddress);
		}

		return afdPath;
	}

	private Bitmap getNotebookCover(long penSerialNumber, String penFirmwareVersion, String afdGuid, int copyIndex, int thumbnailSize) {
		String METHOD = "getNotebookCover(): ";

		// validate thumbnail sizes -
		if (thumbnailSize <= 64)
			thumbnailSize = 64;
		else if (thumbnailSize <= 128)
			thumbnailSize = 128;
		else
			thumbnailSize = 256;

		if (DEBUG_LOG)
			Log.d(TAG, METHOD + "pen=" + penSerialNumber + ", fver=" + penFirmwareVersion + ", afdGuid=" + afdGuid + ", copyIndex=" + copyIndex + ", thumbnailSize=" + thumbnailSize);

		byte[] coverBytes = mAfdParser.extractNotebookCover(afdGuid, copyIndex, thumbnailSize);
		if (coverBytes != null && coverBytes.length > 0) {

			return BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.length);
		}

		return (null);
	}

	private Hashtable getAfdData(String guid) {
		Hashtable<String, Object> afdInfo = null;

		// TODO Complete it. It is not being used currently.

		return afdInfo;
	}

	private Hashtable getPageData(long pageAddress) {
		String METHOD = "getAfdData(pa): ";

		Hashtable<String, Object> responseInfo = null;
		String sPageAddress = AFDHelper.convertPageAddress2String(pageAddress);

		AfdRec afdRec = getPenAFDLocal(0, null, null, sPageAddress);
		if (null != afdRec) {

			// overall
			responseInfo = new Hashtable<String, Object>();

			// Doc info
			{
				Hashtable<String, Object> afdInfo = new Hashtable<String, Object>();
				afdInfo.put(ProtocolConstants.KEY_RES_TGPI_DOC_GUID, afdRec.getGuid());
				afdInfo.put(ProtocolConstants.KEY_RES_TGPI_FILE_NAME, mAfdParser.getPaperFilename(afdRec.getGuid()));
				afdInfo.put(ProtocolConstants.KEY_RES_TGPI_DOC_NAME, afdRec.getTitle());
				afdInfo.put(ProtocolConstants.KEY_TGPI_VERSION, afdRec.getVersion());
				afdInfo.put(ProtocolConstants.KEY_TPDI_BUILD_NUMBER, 0); // Unknown for now
				afdInfo.put(ProtocolConstants.KEY_TPDI_COPY_COUNT, afdRec.getCopies());
				afdInfo.put(ProtocolConstants.KEY_TPDI_HIGH_ADDRESS, afdRec.getHighPageAddress());
				afdInfo.put(ProtocolConstants.KEY_TPDI_LOW_ADDRESS, afdRec.getLowPageAddress());
				afdInfo.put(ProtocolConstants.KEY_TPDI_PAGE_COUNT, afdRec.getNumberOfPagesInEachCopy());
				responseInfo.put(ProtocolConstants.KEY_TGPI_DOC_INFO, afdInfo);
			}

			// Copy info
			{
				Hashtable<String, Object> copyInfo = new Hashtable<String, Object>();
				List<CopyRec> ranges = afdRec.getCopyList();
				if (ranges != null && ranges.size() > 0) {
					// Send copy info of only the relevant notebook which the specified page belongs to
					for (int index = 0; index < ranges.size(); index++) {
						CopyRec copyRec = ranges.get(index);
						if (copyRec != null) {
							long low  = copyRec.getLowPageAddress();
							long high = copyRec.getHighPageAddress();
							if ((low <= pageAddress) && (pageAddress <= high)) {
								copyInfo.put(ProtocolConstants.KEY_TPDI_COPY, copyRec.getCopyIndex());
								copyInfo.put(ProtocolConstants.KEY_TPDI_COPY_LOW_ADDRESS, low);
								copyInfo.put(ProtocolConstants.KEY_TPDI_COPY_HIGH_ADDRESS, high);
							}
						}
					}
				} else {
					// in case the AFD has no notebook, then provide the basic info itself.
					copyInfo.put(ProtocolConstants.KEY_TPDI_COPY, 0);
					copyInfo.put(ProtocolConstants.KEY_TPDI_COPY_LOW_ADDRESS, afdRec.getLowPageAddress());
					copyInfo.put(ProtocolConstants.KEY_TPDI_COPY_HIGH_ADDRESS, afdRec.getHighPageAddress());
				}
				responseInfo.put(ProtocolConstants.KEY_TGPI_COPY_INFO, copyInfo);
			}

			// Page info
			{
				ImageRec iRec = getPageDetails(0, null, sPageAddress);
				if (iRec != null) {
					Hashtable<String, Object> pageInfo = new Hashtable<String, Object>();
					pageInfo.put(ProtocolConstants.KEY_TPDI_PAGE_WIDTH, iRec.getAnotoWidth());
					pageInfo.put(ProtocolConstants.KEY_TPDI_PAGE_HEIGHT, iRec.getAnotoHeight());
					pageInfo.put(ProtocolConstants.KEY_TPDI_PAGE_NUMBER, iRec.getPageNumber());
					Integer ppn = 0;
					try {
						ppn = Integer.parseInt(iRec.getPageLabel());
					} catch (NumberFormatException ex) {
					}
					pageInfo.put(ProtocolConstants.KEY_TPDI_PRINTED_PAGE_NUMBER, ppn);

					Rect cropArea = iRec.getCropArea();
					if (null != cropArea) {
						pageInfo.put(ProtocolConstants.KEY_TPDI_MEDIA_CROP_X0, cropArea.left);
						pageInfo.put(ProtocolConstants.KEY_TPDI_MEDIA_CROP_Y0, cropArea.top);
						pageInfo.put(ProtocolConstants.KEY_TPDI_MEDIA_CROP_X1, cropArea.right);
						pageInfo.put(ProtocolConstants.KEY_TPDI_MEDIA_CROP_Y1, cropArea.bottom);
					}

					responseInfo.put(ProtocolConstants.KEY_TGPI_PAGE_INFO, pageInfo);

				} else {
					if (DEBUG_LOG) {
						Log.w(TAG, METHOD + "getPageDetails() returned null for page address - " + pageAddress);
					}
				}
			}

			// Input info - page address
			responseInfo.put(ProtocolConstants.KEY_TPDI_INPUT_PAGE_ADDRESS, pageAddress);

		} else {
			if (DEBUG_LOG)
				Log.w(TAG, METHOD + "AFD not found in the database for page address - " + pageAddress);
		}

		return responseInfo;
	}

	// COPIED FROM THE FILE com.livescribe.android.sdk.transport.ProtocolConstants that are needed here.
	private class ProtocolConstants {

		// Doc Info
		public final static String KEY_TGPI_DOC_INFO = "doc";            // Document Info
		public final static String KEY_RES_TGPI_FILE_NAME = "fna";      // Event Store response key for afd file name
		public final static String KEY_RES_TGPI_DOC_NAME = "nam";       // Event Store response key for doc name
		public final static String KEY_RES_TGPI_DOC_GUID = "gui";       // Event Store response key for doc guid
		public final static String KEY_TGPI_VERSION = "ver";            // Pen version info
		public final static String KEY_TPDI_BUILD_NUMBER = "bnu";
		public final static String KEY_TPDI_LOW_ADDRESS = "loa";
		public final static String KEY_TPDI_HIGH_ADDRESS = "hia";
		public final static String KEY_TPDI_COPY_COUNT = "ncp";
		public final static String KEY_TPDI_PAGE_COUNT = "tpg";

		//  Copy Info
		public final static String KEY_TGPI_COPY_INFO = "copy";
		public final static String KEY_TPDI_COPY = "cpy";
		public final static String KEY_TPDI_COPY_LOW_ADDRESS = "lpa";
		public final static String KEY_TPDI_COPY_HIGH_ADDRESS = "hpa";

		//  Page Info
		public final static String KEY_TGPI_PAGE_INFO = "page";
		public final static String KEY_TPDI_PAGE_HEIGHT = "pgh";
		public final static String KEY_TPDI_PAGE_WIDTH = "pgw";
		public final static String KEY_TPDI_PAGE_NUMBER = "pgn";
		public final static String KEY_TPDI_PRINTED_PAGE_NUMBER = "ppn";
		public final static String KEY_TPDI_MEDIA_CROP_X0 = "x0";
		public final static String KEY_TPDI_MEDIA_CROP_X1 = "x1";
		public final static String KEY_TPDI_MEDIA_CROP_Y0 = "y0";
		public final static String KEY_TPDI_MEDIA_CROP_Y1 = "y1";

		public final static String KEY_TPDI_INPUT_PAGE_ADDRESS = "pad"; // Identify document by Page Address

	}

	private double distance(Point pointInitial, Point pointFinal) {
		return Math.sqrt(Math.pow(pointFinal.x - pointInitial.x, 2) + Math.pow(pointFinal.y - pointInitial.y, 2));
	}
}
