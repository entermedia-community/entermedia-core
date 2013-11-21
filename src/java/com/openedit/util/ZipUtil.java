/*
 * Created on Apr 22, 2006
 */
package com.openedit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.repository.ContentItem;
import org.openedit.util.WindowsUtil;

import com.openedit.OpenEditException;
import com.openedit.page.manage.PageManager;
import com.openedit.users.User;

public class ZipUtil {
	private static final Log log = LogFactory.getLog(ZipUtil.class);
	protected List fieldExcludes;

	protected OutputFiller filler = new OutputFiller();
  
	protected String fieldFindFileName;

	protected boolean fieldExitOnFirstFind;
	protected String fieldFolderToStripOnZip;

	public String getFolderToStripOnZip() {
		return fieldFolderToStripOnZip;
	}

	public void setFolderToStripOnZip(String inFolderToStripOnZip) {
		
		fieldFolderToStripOnZip = inFolderToStripOnZip.substring(1);
	}

	protected File fieldRoot;

	protected WindowsUtil fieldWindowsUtil;
	protected PageManager fieldPageManager;

	public PageManager getPageManager() {
		return fieldPageManager;
	}

	public void setPageManager(PageManager inPageManager) {
		fieldPageManager = inPageManager;
	}

	public WindowsUtil getWindowsUtil() {
		if (fieldWindowsUtil == null) {
			fieldWindowsUtil = new WindowsUtil();
			fieldWindowsUtil.setRoot(getRoot());
		}
		return fieldWindowsUtil;
	}

	public List getExcludes() {
		if (fieldExcludes == null) {
			fieldExcludes = new ArrayList();
		}

		return fieldExcludes;
	}

	public void addExclude(String inPath) {
		// dont unzip or zip containing this path
		if (inPath != null) {
			inPath = inPath.trim();
		}
		getExcludes().add(inPath);
	}

	// TODO: Should these use the getRoot()
	public void unzip(String inPage, String inDest) throws Exception {
		unzip(new File(inPage), new File(inDest));
	}

	public List unzip(File inPage, File inDest) throws IOException {
		InputStream in = null;
		List unzippedFiles = null;
		try {
			in = new FileInputStream(inPage);
			unzippedFiles = unzip(in, inDest);
		} finally {
			FileUtils.safeClose(in);
		}
		return unzippedFiles;
	}

	public List unzip(InputStream inZip, File inDest) throws IOException {
		return unzip(inZip, inDest, null);
	}

	/**
	 * Wow, this is hard to understand...
	 * 
	 * @param inZip
	 *            -- The Zip InputStream entries have paths buried in them e.g.,
	 *            data/subdir/file.txt
	 * @param inDest
	 *            -- The destination folder, e.g. /opt/appserver/webapp/ROOT
	 * @param inPathSegment
	 *            -- A path segment that might exist in the Zip entries, e.g.
	 *            /data or data
	 * @param inNewPathSegment
	 *            -- A new path segment we want to use, e.g. /backup to make
	 *            backup/subdir/file.txt
	 * @throws IOException
	 */
	public List unzip(InputStream inZip, File inDest, String inCutOffLeadingPath)
			throws IOException {
		ZipInputStream unzip = new ZipInputStream(inZip);
		ZipEntry entry = unzip.getNextEntry();
		List unzippedFiles = new ArrayList();

		while (entry != null) {
			String entryPath = composeEntryPath(entry.getName(),
					inCutOffLeadingPath);
			if (fieldFindFileName != null) {
				if (!PathUtilities.match(entry.getName(), fieldFindFileName)) // TODO
				// :
				// Handle
				// leading
				// /
				{
					entry = unzip.getNextEntry();
					continue;
				}
			}
			if (entryPath.contains("__MACOSX")) {
				entry = unzip.getNextEntry();
				continue;
			}

			// int zipeSize = (int) entry.getSize();
			if (!entry.isDirectory()) {
				File ufile = null;

				if (inDest != null) {
					ufile = new File(inDest, entryPath);
				} else {
					ufile = new File(entryPath);
				}
				ufile.getParentFile().mkdirs();
				if (ufile.exists() && !ufile.delete()) {
					getWindowsUtil().delete(ufile);
				}
				FileOutputStream tout = new FileOutputStream(ufile);
				try {
					filler.fill(unzip, tout);
				} finally {
					FileUtils.safeClose(tout);
				}
				ufile.setLastModified(entry.getTime());
				if( log.isDebugEnabled() )
				{
					log.debug(ufile.getAbsolutePath());
				}
				unzippedFiles.add(ufile);
				if (isExitOnFirstFind()) {
					return unzippedFiles;
				}
			}
			entry = unzip.getNextEntry();
		}
		return unzippedFiles;
	}

	protected String composeEntryPath(String name, String inCutOffLeadingPath) {
		if (inCutOffLeadingPath != null) {
			return name.substring(inCutOffLeadingPath.length());
		}
		return name;
	}

	/*
	 * public void zipFilesIn( File inDir,OutputStream inToBrowser ) throws
	 * IOException, OpenEditException { zipFilesIn(inDir,"/", inToBrowser); }
	 */
	/*
	 * public void zipFilesIn( File inDir,String inIncludePath, OutputStream
	 * inToBrowser ) throws IOException, OpenEditException {
	 * 
	 * String startingdir = inDir.getAbsolutePath(); if (
	 * !startingdir.endsWith(File.separator)) { startingdir = startingdir +
	 * File.separator; } if( inIncludePath.length() > 1) { inDir = new File(
	 * inDir, inIncludePath); }
	 * 
	 * ZipOutputStream finalZip = new ZipOutputStream(inToBrowser);
	 * 
	 * File[] children = inDir.listFiles(); if ( children != null) { for (int i
	 * = 0; i < children.length; i++) { addToZip( children[i], startingdir,
	 * finalZip); } } finalZip.close(); }
	 */
	public void addTozip(String inContent, String inName,
			ZipOutputStream finalZip) throws IOException {
		ZipEntry entry = new ZipEntry(inName);
		entry.setSize(inContent.length());
		entry.setTime(new Date().getTime());
		finalZip.putNextEntry(entry);
		finalZip.write(inContent.getBytes("UTF-8"));
		finalZip.closeEntry();
	}

	public void addTozip(ContentItem inContent, String inName,
			ZipOutputStream finalZip) throws IOException {
		InputStream is = inContent.getInputStream();
		if (is == null) {
			log.error("Couldn't add file to zip: "
					+ inContent.getAbsolutePath());
			return;
		}
		ZipEntry entry = null;
		if (getFolderToStripOnZip() != null) {
			if(inName.contains(getFolderToStripOnZip())){
				String stripped = inName.substring(getFolderToStripOnZip().length(), inName.length());
				entry = new ZipEntry(stripped);
			} else{
				entry = new ZipEntry(inName);
			}
			
		} else {

			entry = new ZipEntry(inName);
		}
		entry.setSize(inContent.getLength());
		entry.setTime(inContent.lastModified().getTime());

		finalZip.putNextEntry(entry);
		try {
			new OutputFiller().fill(is, finalZip);
		} finally {
			is.close();
		}
		finalZip.closeEntry();
	}

	public void zip(String inOpenEditPath, ZipOutputStream inStream,
			boolean recursive) {
		ZipProcessor zipper = new ZipProcessor();
		zipper.setPageManager(getPageManager());
		zipper.zipOutputStream = inStream;
		zipper.setRecursive(recursive);
		zipper.process(inOpenEditPath);

	}

	public void zip(String inOpenEditPath, ZipOutputStream inStream) {
		zip(inOpenEditPath, inStream, true);
	}

	public void zipFile(String inOpenEditPath, OutputStream inToBrowser)
			throws IOException, OpenEditException {
		ZipOutputStream finalZip = new ZipOutputStream(inToBrowser);
		zip(inOpenEditPath, finalZip);
		finalZip.close();
	}

	public void zipFiles(List inPaths, OutputStream inToBrowser,
			boolean recursive) throws IOException, OpenEditException {
		// TODO: inRelative isn't used. Fix up SyncToServer to not use it
		ZipOutputStream finalZip = new ZipOutputStream(inToBrowser);
		for (Iterator iterator = inPaths.iterator(); iterator.hasNext();) {
			String path = (String) iterator.next();
			zip(path, finalZip, recursive);
		}

		// Throws exception if the folder was empty
		try {
			finalZip.close();
		} catch (ZipException ex) {
			// zip might have been empty
			FileUtils.safeClose(inToBrowser);
		}

	}

	public void zipFiles(List inPaths, OutputStream inToBrowser)
			throws IOException, OpenEditException {
		zipFiles(inPaths, inToBrowser, true);
	}

	class ZipProcessor extends PathProcessor {
		ZipOutputStream zipOutputStream;

		public void processFile(ContentItem inContent, User inUser)
				throws OpenEditException {
			String path = inContent.getPath();
			// if (inContent instanceof FileItem)
			// {
			// String abs = ((FileItem) inContent).getAbsolutePath();
			// if (getRoot() != null)
			// {
			// String root = getRoot().getAbsolutePath();
			// if (abs.contains(getRoot().getAbsolutePath()))
			// {
			// path = abs.substring(root.length());
			// }
			// }
			// }

			// The zip entry can't begin with any slashes
			while (path.startsWith("/")) {
				path = path.substring(1, path.length());
			}

			try {
				addTozip(inContent, path, zipOutputStream);
			} catch (Exception e) 
			{
				if( e.getMessage().startsWith("duplicate"))
				{
					return;
				}
				throw new OpenEditException(e);
			}

		}

		public boolean acceptFile(ContentItem inFileItem) {
			String openeditpath = inFileItem.getPath();// .replace('\\', '/');
														// // Windows
			// replace
			if (inFileItem.isFolder() && !openeditpath.endsWith("/")) {
				openeditpath = openeditpath + "/"; // Make sure we leave the /
				// on there for directories
			}

			return isValidEntry(openeditpath);
		}

		public boolean acceptDir(ContentItem inDir) {
			return acceptFile(inDir);
		}
	}

	protected boolean isValidEntry(String inPath) throws OpenEditException {
		if (exclude(inPath)) {
			return false;
		}
		return true;
	}

	protected boolean exclude(String inOpenEditPath) {
		if (fieldExcludes != null) {
			for (Iterator iter = getExcludes().iterator(); iter.hasNext();) {
				String wild = (String) iter.next();
				if (PathUtilities.match(inOpenEditPath, wild)) {
					return true;
				}
			}
		}
		return false;
	}

	public String getFindFileName() {
		return fieldFindFileName;
	}

	public void setFindFileName(String inFindFileName) {
		fieldFindFileName = inFindFileName;
	}

	public boolean isExitOnFirstFind() {
		return fieldExitOnFirstFind;
	}

	public void setExitOnFirstFind(boolean inExitOnFirstFind) {
		fieldExitOnFirstFind = inExitOnFirstFind;
	}

	public File getRoot() {
		return fieldRoot;
	}

	public void setRoot(File inRoot) {
		fieldRoot = inRoot;
	}

	/**
	 * Unzips a GZIP file that contains only one file.
	 * 
	 * @param inSource
	 *            The source GZIP file.
	 * @param inDest
	 *            destination file.
	 * @return true if destination file exists after uncompressing.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public boolean gunzip(File inSource, File inDest)
			throws FileNotFoundException, IOException {
		if (inDest.exists())
			inDest.delete();

		InputStream in = new GZIPInputStream(new FileInputStream(inSource));
		OutputStream out = new FileOutputStream(inDest);
		new OutputFiller().fill(in, out);
		in.close();
		out.close();

		return inDest.exists();
	}

}