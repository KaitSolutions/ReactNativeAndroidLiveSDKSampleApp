package com.rnlivesdksampleapp.paperlib.afd;

import android.content.Context;
import android.util.Log;

import com.livescribe.afp.PageAddress;
import com.rnlivesdksampleapp.paperlib.records.AfdRec;
import com.rnlivesdksampleapp.paperlib.records.CopyRec;

import java.io.File;
import java.util.List;

public class AFDHelper {

	private final static String TAG = AFDHelper.class.getSimpleName();
	private static final boolean DEBUG_LOG = true; //BuildConfig.debugLogging; //true;

	public static long convertPageAddress2long(String strPageAddress) {
		long lPageAddress = -1;
		try {
			PageAddress pa = new PageAddress(strPageAddress);
			if (null != pa)
				lPageAddress = pa.getId();
		} catch (Exception ex) {
			// do nothing as default is false
		}

		return lPageAddress;
	}

	public static String convertPageAddress2String(long lPageAddress) {
		String strPageAddress = PageAddress.toString(lPageAddress);
		try {
			PageAddress pa = new PageAddress(lPageAddress);
			if (null != pa)
				strPageAddress = pa.toString().substring(2);
		} catch (Exception ex) {
			// do nothing as default is false
		}

		return strPageAddress;
	}

	public static AfdRec getAfdfromGuidAndVersion(List<AfdRec> afds, String guid, String version) {

		if ((afds != null) && (guid != null) && !guid.isEmpty() && (version != null) && !version.isEmpty()) {

			for (AfdRec theAfd : afds) {

				if (theAfd.getGuid().equalsIgnoreCase(guid) && theAfd.getVersion().equalsIgnoreCase(version)) {
					return theAfd;
				}
			}
		}

		return null;
	}

	public static AfdRec getAfdFromGuid(List<AfdRec> afds, String guid) {

		if ((afds != null) && (guid != null) && !guid.isEmpty()) {

			for (AfdRec theAfd : afds) {

				if (theAfd.getGuid().equalsIgnoreCase(guid)) {
					return theAfd;
				}
			}
		}

		return null;
	}

	public static AfdRec getAfdFromPage(List<AfdRec> afds, PageAddress pageAddress)	{
		final String METHOD = "getAfdFromPage(): ";
		AfdRec foundAfdRec = null;

		// loop thru all the entries in the list and
		// check for the page range that is in the range
		for (AfdRec anAfd: afds) {

			// get info from Afd record
			//String startPA = PageAddress.toString(anAfd.getLowPageAddress()).substring(2);
			//String stopPA = PageAddress.toString(anAfd.getHighPageAddress()).substring(2);
			//String currentPA = pageAddress.toString().substring(2);
			long startPA = anAfd.getLowPageAddress();
			long stopPA = anAfd.getHighPageAddress();
			long currentPA = pageAddress.getId();
			boolean within = false;

			int resultLeft = AFDHelper.comparePageAddress(startPA, currentPA);
			if (0 == resultLeft) {
				//Log.d(TAG, METHOD + "GGGGG start - " + startPA + ", query=" + currentPA + ", stop=" + stopPA + ". Left=" + resultLeft + ", copies=" + anAfd.getCopies());
				within = true;
			} else {
				int resultRight = AFDHelper.comparePageAddressExcludeLast(currentPA, stopPA);
				//Log.d(TAG, METHOD + "GGGGG start - " + startPA + ", query=" + currentPA + ", stop=" + stopPA + ". Left=" + resultLeft + ", Right=" + resultRight + ", copies=" + anAfd.getCopies());
				if (0 == resultRight) {
					within = true;
				} else {
					if (resultLeft < 0 && resultRight < 0)
						within = true;
					else
						within = false;
				}
			}

			// Is it within the range of the Notebook/Afd page addresses
			if (within) {
				// here we want to get one deep search for this Afd only by hitting the database once
				if ((0 == resultLeft) || (anAfd.getCopies() == 1) || doesPageAddressExistInCopies(anAfd, pageAddress.toString().substring(2))) {

					// got it
					foundAfdRec = anAfd;

					// break only if it has been determined that the page is within this afd
					// Being in the outer range of the AFd is not a guarantee it is in the AFD
					// As the range is given to the company and they can use copies from different
					// location to make and AFD background patterns
					break;
				}
			}
		}

		return foundAfdRec;
	}

	public static boolean doesPageAddressExistInCopies(AfdRec anAfd, String desiredPageAddress) {
		final String METHOD = "doesPageAddressExistInCopies(): ";
		boolean success = true;

		// check for existing ones, if any
		List<CopyRec> existingPageRanges = anAfd.getCopyList();
		if (existingPageRanges.size() > 0) {

			long lDesiredPageAddress = convertPageAddress2long(desiredPageAddress);

			// check for each record
			for (CopyRec aPageRange: existingPageRanges) {

				long result1 = lDesiredPageAddress - aPageRange.getLowPageAddress();
				//Log.i(TAG, METHOD + "Left check " + result1);
				if (0 == result1) {
					success = true;
				} else {
					long result2 = aPageRange.getHighPageAddress() - lDesiredPageAddress;
					//Log.i(TAG, METHOD + "Right check " + result2);
					if (0 == result2) {
						success = true;
					} else {
						if (result1 > 0 && result2 > 0)
							success = true;
						else
							success = false;
					}
				}

				//Log.i(TAG, METHOD + "Begin " + aPageRange.getLowPageAddress() + ", End " + aPageRange.getHighPageAddress() +
				//		"  Does " + desiredPageAddress + " page exist for Afd with guid " + anAfd.getGuid() + "? " + success);
				if (success)
					break;
			}

			// if nothing found so far so the page does not exist in this AFD
			if (!success)
				if (DEBUG_LOG)
                    Log.d(TAG, METHOD + desiredPageAddress + " page does not exist in any copy of " + anAfd.getGuid() + " Afd");

		} else {
            if (DEBUG_LOG)
                Log.d(TAG, METHOD + "Page Range of copies don't exist, assuming only one copy. So page " + desiredPageAddress +  " does NOT EXIST in Afd with guid " + anAfd.getGuid());
			success = false;
		}

		return success;
	}

	/**
	 * Get AFD file
	 * @param context
	 * @return
	 */
	public static File getPaperAFDFolder(Context context) {

		final String METHOD = "LiveSDK > AFD > getPaperAFDFolder > ";
		String afdFolder = context.getExternalCacheDir() + "/afds/";

		Log.d(TAG, METHOD + "afdFolder > " + afdFolder);


		// now we need to ensure if it exists
		File afdDir = new File(afdFolder);
		if (!afdDir.exists()) {
			// create the folder now
			if (!afdDir.mkdirs()) {
                if (DEBUG_LOG)
                    Log.d(TAG, METHOD + "Paper AFDs folder does not exit or unable to create: " + afdFolder);
				return null;
			} else {
                if (DEBUG_LOG)
                    Log.d(TAG, METHOD + "Paper AFDs folder was created successfully: " + afdFolder);
			}
		}

		return afdDir;
	}

	public static boolean fileExist(String fileName) {
		File aFile = new File(fileName);
		return aFile.exists();
	}

	public static int comparePageAddress(String pa1, String pa2) {

		int result = 0;

		String[] pa1Part = pa1.split("\\.");
		String[] pa2Part = pa2.split("\\.");

		//Log.i(TAG, "      Left " + pa1Part[0] + ", Right " + pa2Part[0]);
		if (Integer.parseInt(pa1Part[0]) < Integer.parseInt(pa2Part[0]))
			return -1;
		if (Integer.parseInt(pa1Part[0]) > Integer.parseInt(pa2Part[0]))
			return 1;

		//Log.i(TAG, "      Left " + pa1Part[1] + ", Right " + pa2Part[1]);
		if (Integer.parseInt(pa1Part[1]) < Integer.parseInt(pa2Part[1]))
			return -1;
		if (Integer.parseInt(pa1Part[1]) > Integer.parseInt(pa2Part[1]))
			return 1;

		//Log.i(TAG, "      Left " + pa1Part[2] + ", Right " + pa2Part[2]);
		if (Integer.parseInt(pa1Part[2]) < Integer.parseInt(pa2Part[2]))
			return -1;
		if (Integer.parseInt(pa1Part[2]) > Integer.parseInt(pa2Part[2]))
			return 1;

		//Log.i(TAG, "      Left " + pa1Part[3] + ", Right " + pa2Part[3]);
		if (Integer.parseInt(pa1Part[3]) < Integer.parseInt(pa2Part[3]))
			return -1;
		if (Integer.parseInt(pa1Part[3]) > Integer.parseInt(pa2Part[3]))
			return 1;

		//Log.i(TAG, "      All Equal.");
		return result;
	}

	public static int comparePageAddressExcludeLast(String pa1, String pa2) {

		int result = 0;

		String[] pa1Part = pa1.split("\\.");
		String[] pa2Part = pa2.split("\\.");

		//Log.i(TAG, "      Left " + pa1Part[0] + ", Right " + pa2Part[0]);
		if (Integer.parseInt(pa1Part[0]) < Integer.parseInt(pa2Part[0]))
			return -1;
		if (Integer.parseInt(pa1Part[0]) > Integer.parseInt(pa2Part[0]))
			return 1;

		//Log.i(TAG, "      Left " + pa1Part[1] + ", Right " + pa2Part[1]);
		if (Integer.parseInt(pa1Part[1]) < Integer.parseInt(pa2Part[1]))
			return -1;
		if (Integer.parseInt(pa1Part[1]) > Integer.parseInt(pa2Part[1]))
			return 1;

		//Log.i(TAG, "      Left " + pa1Part[2] + ", Right " + pa2Part[2]);
		if (Integer.parseInt(pa1Part[2]) < Integer.parseInt(pa2Part[2]))
			return -1;
		if (Integer.parseInt(pa1Part[2]) > Integer.parseInt(pa2Part[2]))
			return 1;

		// exclude last page number
		//Log.i(TAG, "      Left " + pa1Part[3] + ", Right " + pa2Part[3]);
		if (Integer.parseInt(pa1Part[3]) < (Integer.parseInt(pa2Part[3])-1))
			return -1;
		if (Integer.parseInt(pa1Part[3]) > (Integer.parseInt(pa2Part[3])-1))
			return 1;

		//Log.i(TAG, "      All Equal.");
		return result;
	}

	public static int comparePageAddress(long pa1, long pa2) {

		if (pa1 < pa2)
			return -1;
		if (pa1 > pa2)
			return 1;

		return 0;
	}

	public static int comparePageAddressExcludeLast(long pa1, long pa2) {
		return comparePageAddress(pa1, pa2-1);
	}

	public static boolean isValidPageAddress(String pageAddress) {
		PageAddress pa = new PageAddress(pageAddress);

		return (null != pa);
	}

}
