package com.android.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

/**
 * 功能： 1.實現把指定資料夾下的所有檔案壓縮為指定資料夾下指定 zip 檔案。
 *              2.實現把指定資料夾下的 zip 檔案解壓到指定目錄下。
 */
public class ZipUtils {

    public boolean zipTerminal = false;

    public ZipUtils() {

    }

    public void terminal() {
        zipTerminal = true;
    }

    public boolean isTerminal() {
        return zipTerminal;
    }

    /**
     * 功能：把 sourceDir 目錄下的所有檔進行 zip 格式的壓縮，保存為指定 zip 檔案
     *
     * @param sourceDir
     * @param zipFile
     */
    public void zip(String sourceDir, String zipFile) {

        OutputStream os;
        zipTerminal = false;
        try {

            os = new FileOutputStream(zipFile);

            BufferedOutputStream bos = new BufferedOutputStream(os);

            ZipOutputStream zos = new ZipOutputStream(bos);

            File file = new File(sourceDir);

            String basePath = null;

            if (file.isDirectory()) {

                basePath = file.getPath();

            } else {// 直接壓縮單個檔時，取父目錄

                basePath = file.getParent();

            }

            //zos.setEncoding("gbk");

            zipFile(file, basePath, zos);
            if (zipTerminal) {
                file = new File(zipFile);
                if (file.exists())
                    file.delete();
            }

            zos.closeEntry();

            zos.close();
            bos.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public void zipMult(String basePath, ArrayList<String> sourceDir,
                        String zipFile) {

        OutputStream os;
        zipTerminal = false;
        try {

            os = new FileOutputStream(zipFile);

            BufferedOutputStream bos = new BufferedOutputStream(os);

            ZipOutputStream zos = new ZipOutputStream(bos);

            // File file = new File(sourceDir);

            int length = sourceDir.size();
            File file = null;
            String pathName = null;
            for (int i = 0; i < length; i++) {
                file = new File(sourceDir.get(i));
                if (file.isDirectory()) {
                    pathName = file.getPath().substring(basePath.length() + 1)
                            + "/";
                    zos.putNextEntry(new ZipEntry(pathName));
                }
                zipFile(file, basePath, zos);
                if (zipTerminal)
                    break;
            }

            zos.closeEntry();

            zos.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        if (zipTerminal) {
            File file = new File(zipFile);
            if (file.exists())
                file.delete();
        }
    }

    /**
     * 功能：執行檔案壓縮成zip檔
     *
     * @param source
     * @param basePath 待壓縮檔根目錄
     * @param zos
     */

    private void zipFile(File source, String basePath,

                         ZipOutputStream zos) {

        File[] files = new File[0];

        if (source.isDirectory()) {

            files = source.listFiles();

        } else {

            files = new File[1];

            files[0] = source;

        }

        String pathName;// 存相對路徑(相對於待壓縮的根目錄)

        byte[] buf = new byte[1024];

        int length = 0;

        try {

            for (File file : files) {

                if (file.isDirectory()) {

                    pathName = file.getPath().substring(basePath.length() + 1)

                            + "/";
                    ZipEntry ze = new ZipEntry(pathName);
                    zos.putNextEntry(ze);

                    zipFile(file, basePath, zos);

                } else {

                    pathName = file.getPath().substring(basePath.length() + 1);

                    InputStream is = new FileInputStream(file);

                    BufferedInputStream bis = new BufferedInputStream(is);

                    zos.putNextEntry(new ZipEntry(pathName));

                    while ((length = bis.read(buf)) > 0 && (!zipTerminal)) {

                        zos.write(buf, 0, length);

                    }

                    is.close();

                }
                if (zipTerminal)
                    return;

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    /**
     * 功能：解壓 zip 檔，只能解壓 zip 檔
     *
     * @param zipfile
     * @param destDir
     */

    public void unZip(String zipfile, String destDir) {

        destDir = destDir.endsWith("\\") ? destDir : destDir + "\\";

        byte b[] = new byte[1024];

        int length;

        ZipFile zipFile;
        zipTerminal = false;
        try {

            zipFile = new ZipFile(new File(zipfile));

            @SuppressWarnings("rawtypes")
            Enumeration enumeration = zipFile.getEntries();

            ZipEntry zipEntry = null;

            while (enumeration.hasMoreElements()) {

                zipEntry = (ZipEntry) enumeration.nextElement();
                //String name = destDir + new String(zipEntry.getName().getBytes("gbk"), "utf-8");
                //File loadFile = new File(name);
                File loadFile = new File(destDir + zipEntry.getName());
                if (zipEntry.isDirectory()) {

                    loadFile.mkdirs();

                } else {

                    if (!loadFile.getParentFile().exists()) {

                        loadFile.getParentFile().mkdirs();

                    }

                    OutputStream outputStream = new FileOutputStream(loadFile);

                    InputStream inputStream = zipFile.getInputStream(zipEntry);

                    while ((length = inputStream.read(b)) > 0 && (!zipTerminal))
                        outputStream.write(b, 0, length);

                }
                if (zipTerminal)
                    break;

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

    }
}