package cn.edu.ouc.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Arrays;

/**
 * 文件工具类：提供文件类型判断、日期格式化、文件名生成等通用方法
 */
public class FileUtil {
    // 日期格式化工具（统一时间显示格式）
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // 允许上传的文件后缀
    private static final String[] ALLOWED_SUFFIX = {"jpg", "png", "gif", "jpeg", "mp3", "mp4"};

    /**
     * 格式化日期为字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDate(Date date) {
        return sdf.format(date);
    }

    /**
     * 根据文件名判断文件类型（image/audio/video/other）
     */
    public static String getFileType(String fileName) {
        String suffix = getFileSuffix(fileName);
        switch (suffix) {
            case "jpg":
            case "png":
            case "gif":
            case "jpeg":
                return "image";
            case "mp3":
                return "audio";
            case "mp4":
                return "video";
            default:
                return "other";
        }
    }

    /**
     * 生成服务器存储文件名：毫秒级时间戳+原文件名
     */
    public static String generateServerFileName(String originalName) {
        long timestamp = System.currentTimeMillis();
        return timestamp + "_" + originalName;
    }

    /**
     * 验证文件是否允许上传（检查后缀）
     */
    public static boolean isAllowedFile(String fileName) {
        String suffix = getFileSuffix(fileName);
        return Arrays.asList(ALLOWED_SUFFIX).contains(suffix);
    }

    /**
     * 创建目录（若不存在则递归创建）
     */
    public static boolean createDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            return dir.mkdirs(); // mkdirs()创建多级目录，mkdir()仅创建单级
        }
        return true;
    }

    /**
     * 提取文件后缀（忽略大小写）
     */
    private static String getFileSuffix(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}