package com.android.utils;


public class Common {

    /**
     * 將數字轉為帶 , 分段的字條串
     */
    public static String formatString(String str) {
        int i = str.length();
        while (i > 3) {
            str = str.substring(0, i - 3) + "," + str.substring(i - 3);
            i -= 3;
        }
        return str;
    }

    /**
     * 截取檔路徑的最後檔案名. 根目錄返回 根目錄 /, 如果檔案名最後是/, 返回空字串.
     * /  		--> /
     * /path 	--> /path
     * /path/1 	--> 1
     * /path/1/ --> ""
     */
    public static String getPathName(String path) {
        int index = path.lastIndexOf('/');
        if (index == -1 || index == 0)
            return path;
        return path.substring(index + 1);
    }

    /**
     * /		--> /
     * /path 	--> path
     * /path/1	--> 1
     * /path/1/ --> 1
     */
    public static String getPathName2(String path) {
        int index = path.lastIndexOf('/');
        if (index == -1 || path.equals("/"))
            return path;
        if (index == 0 && path.length() > 1) {
            return path.substring(1);
        }
        if (index == (path.length() - 1))
            return getPathName2(path.substring(0, path.length() - 1));
        return path.substring(index + 1);
    }


    /**
     * 將檔案名後面,尾碼前面加apd.
     */
    public static String fileNameAppend(String name, String apd) {
        int i = name.lastIndexOf(".");
        if (i == -1 || i == 0)
            return name + apd;
        return name.substring(0, i) + apd + name.substring(i, name.length());
    }

    /**
     * 將檔路徑後面,尾碼前面加apd.
     */
    public static String pathNameAppend(String path, String apd) {
        String name = getPathName(path);
        int i = name.lastIndexOf(".");
        if (i == -1 || i == 0)
            return path + apd;
        int l = path.lastIndexOf("/") + 1;
        return path.substring(0, l + i) + apd + path.substring(l + i);
    }


    public static String getParentPath(String path) {
        if (path.equals("/"))
            return path;
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        path = path.substring(0, path.lastIndexOf("/"));
        return path.equals("") ? "/" : path;
    }

    public static String formatFromSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null)
            resultBuffer.append(suffix);
        return resultBuffer.toString();
    }
}
