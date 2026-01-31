package cn.edu.ouc.listener;

import cn.edu.ouc.model.FileInfo;
import cn.edu.ouc.util.FileUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局上下文监听器：服务器启动时初始化文件列表和访问计数
 */
@WebListener
public class MediaContextListener implements ServletContextListener {
    // 全局数据存储键（供其他组件访问）
    public static final String FILE_LIST_KEY = "mediaFileList";
    public static final String ACCESS_COUNT_KEY = "mediaAccessCountMap";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        System.out.println("=== 媒体服务器初始化开始 ===");

        // 1. 读取web.xml中的上传路径配置
        String uploadPath = context.getInitParameter("uploadPath");
        System.out.println("上传路径：" + uploadPath);

        // 2. 创建上传目录（若不存在）
        boolean dirCreated = FileUtil.createDir(uploadPath);
        if (!dirCreated) {
            System.err.println("上传目录创建失败，系统初始化终止！");
            return;
        }

        // 3. 初始化全局数据结构
        List<FileInfo> fileList = new ArrayList<>();
        Map<String, Integer> accessCountMap = new HashMap<>();

        // 4. 遍历上传目录，加载已存在的媒体文件
        File uploadDir = new File(uploadPath);
        File[] files = uploadDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    String serverFileName = file.getName();
                    // 解析文件名：时间戳_原文件名（跳过非系统上传的文件）
                    int underscoreIndex = serverFileName.indexOf("_");
                    if (underscoreIndex == -1) {
                        continue;
                    }
                    String originalName = serverFileName.substring(underscoreIndex + 1);
                    String fileType = FileUtil.getFileType(originalName);
                    long fileSize = file.length();
                    Date uploadTime = new Date(file.lastModified());

                    // 创建FileInfo对象并添加到列表
                    FileInfo fileInfo = new FileInfo(serverFileName, originalName, uploadTime, file.getAbsolutePath(), fileSize, fileType);
                    fileList.add(fileInfo);
                    accessCountMap.put(fileInfo.getId(), 0); // 初始访问次数为0
                }
            }
            System.out.println("加载已存在文件数量：" + fileList.size());
        } else {
            System.out.println("上传目录无已存在文件");
        }

        // 5. 将全局数据存入ServletContext
        context.setAttribute(FILE_LIST_KEY, fileList);
        context.setAttribute(ACCESS_COUNT_KEY, accessCountMap);

        System.out.println("=== 媒体服务器初始化完成 ===");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== 媒体服务器关闭，资源释放完成 ===");
        // 无需特殊资源释放，仅记录日志
    }
}