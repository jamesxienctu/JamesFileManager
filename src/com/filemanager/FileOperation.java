package com.filemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileOperation {
	public static void copyFile(String oldPath, String newPath)
			throws IOException {
		int bytesum = 0;
		int byteread = 0;
		File oldfile = new File(oldPath);
		if (oldfile.exists()) {
			InputStream inStream = new FileInputStream(oldPath); // 讀入原檔案
			FileOutputStream fs = new FileOutputStream(newPath);
			byte[] buffer = new byte[4096];
			//int length;
			while ((byteread = inStream.read(buffer)) != -1) {
				bytesum += byteread; // 位元組數 檔大小
				fs.write(buffer, 0, byteread);
			}
			inStream.close();
		}
	}

	/**
	 * 遞迴獲取資料夾裡所有檔的總大小
	 * BUG: 不能分辨連結檔
	 * */
	public static long getDirectorySize(File f) throws IOException{
		long size = 0;
		File flist[] = f.listFiles();
		if (flist == null)
			return f.length();
		int length = flist.length;
		for (int i = 0; i < length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getDirectorySize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}

	/**
	 * BUG: 不能分辨連結檔
	 * */
	public static long getDirectorySize(String fp) throws IOException{
		long size = 0;
		File f = new File(fp);
		File flist[] = f.listFiles();
		if (flist == null)
			return f.length();
		int length = flist.length;
		for (int i = 0; i < length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getDirectorySize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
}
