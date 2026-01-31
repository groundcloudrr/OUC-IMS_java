package cn.edu.ouc.servlet;

import cn.edu.ouc.listener.MediaContextListener;
import cn.edu.ouc.model.FileInfo;
import cn.edu.ouc.util.FileUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 文件上传Servlet：处理用户上传请求，保存文件并更新全局数据
 */
@WebServlet("/upload")
public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // 最大文件大小限制：500MB（500*1024*1024字节）
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 1. 验证请求类型是否为multipart/form-data（文件上传专用类型）
        if (!ServletFileUpload.isMultipartContent(request)) {
            out.print("<script>alert('请选择文件上传！');window.history.back();</script>");
            return;
        }

        // 2. 配置文件上传工厂
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1024 * 10); // 内存缓冲区大小：10KB
        String tempPath = getServletContext().getRealPath("/WEB-INF/temp/");
        FileUtil.createDir(tempPath);
        factory.setRepository(new File(tempPath)); // 临时文件存储目录

        // 3. 创建文件上传核心组件
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE); // 单文件大小限制
        upload.setSizeMax(MAX_FILE_SIZE * 10); // 总请求大小限制（多文件上传）
        upload.setHeaderEncoding("UTF-8"); // 文件名编码

        // 4. 获取上传路径和全局数据
        ServletContext context = getServletContext();
        String uploadPath = context.getInitParameter("uploadPath");
        List<FileInfo> fileList = (List<FileInfo>) context.getAttribute(MediaContextListener.FILE_LIST_KEY);
        Map<String, Integer> accessCountMap = (Map<String, Integer>) context.getAttribute(MediaContextListener.ACCESS_COUNT_KEY);

        try {
            // 5. 解析请求，获取文件项列表
            List<FileItem> fileItems = upload.parseRequest(request);

            // 6. 遍历文件项，处理上传文件
            for (FileItem item : fileItems) {
                if (!item.isFormField()) { // 忽略普通表单字段，只处理文件字段
                    String originalName = item.getName();
                    if (originalName == null || originalName.trim().isEmpty()) {
                        continue; // 跳过空文件
                    }

                    // 7. 验证文件类型
                    if (!FileUtil.isAllowedFile(originalName)) {
                        out.print("<script>alert('不允许上传该类型文件！支持jpg、png、gif、mp3、mp4格式');window.history.back();</script>");
                        return;
                    }

                    // 8. 生成服务器存储文件名，避免冲突
                    String serverFileName = FileUtil.generateServerFileName(originalName);
                    File uploadFile = new File(uploadPath + File.separator + serverFileName);

                    // 9. 写入文件到服务器
                    item.write(uploadFile);
                    System.out.println("文件上传成功：" + uploadFile.getAbsolutePath());

                    // 10. 封装FileInfo对象，更新全局数据（加同步锁，避免并发冲突）
                    String fileType = FileUtil.getFileType(originalName);
                    long fileSize = uploadFile.length();
                    Date uploadTime = new Date();
                    FileInfo fileInfo = new FileInfo(serverFileName, originalName, uploadTime, uploadFile.getAbsolutePath(), fileSize, fileType);

                    synchronized (fileList) {
                        fileList.add(fileInfo);
                    }
                    synchronized (accessCountMap) {
                        accessCountMap.put(fileInfo.getId(), 0);
                    }
                }
            }

            // 11. 上传成功，跳转到文件列表页
            out.print("<script>alert('文件上传成功！');window.location.href='" + context.getContextPath() + "/fileList';</script>");

        } catch (Exception e) {
            e.printStackTrace();
            // 处理文件大小超限异常
            if (e.getMessage() != null && e.getMessage().contains("exceeds maximum allowed size")) {
                out.print("<script>alert('文件大小超过限制！最大支持500MB');window.history.back();</script>");
            } else {
                out.print("<script>alert('文件上传失败：" + e.getMessage() + "');window.history.back();</script>");
            }
        } finally {
            out.close();
        }
    }
}