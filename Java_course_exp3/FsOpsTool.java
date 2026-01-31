import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Java命令行文件操作工具（单文件完整版）
 * 支持mkdir、nfile、rm、ls、help、self命令，含配置文件过滤功能
 */
public class FsOpsTool {
    // 配置文件路径
    private static final String CONFIG_FILE = "fsops.conf";
    // 时间格式化工具
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        // 无参数时显示帮助信息
        if (args.length == 0) {
            printHelp();
            return;
        }

        // 解析命令行参数
        String cmd = args[0];
        switch (cmd) {
            case "mkdir":
                handleMkdir(args);
                break;
            case "nfile":
                handleNfile(args);
                break;
            case "rm":
                handleRm(args);
                break;
            case "ls":
                handleLs(args);
                break;
            case "help":
                printHelp();
                break;
            case "self":
                printSelfInfo();
                break;
            default:
                System.out.println("未知命令：" + cmd);
                printHelp();
                break;
        }
    }

    // ======================== 命令处理方法 ========================
    /** 处理mkdir命令：创建一个或多个目录 */
    private static void handleMkdir(String[] args) {
        if (args.length < 2) {
            System.out.println("错误：请指定要创建的目录路径！");
            return;
        }

        // 遍历所有路径参数，批量创建目录
        for (int i = 1; i < args.length; i++) {
            String path = args[i];
            File dir = new File(path);
            if (dir.exists()) {
                System.out.println("提示：目录已存在 → " + path);
                continue;
            }

            // mkdirs()支持多级目录创建
            if (dir.mkdirs()) {
                System.out.println("成功：目录创建完成 → " + path);
            } else {
                System.out.println("失败：目录创建失败 → " + path);
            }
        }
    }

    /** 处理nfile命令：新建空文件 */
    private static void handleNfile(String[] args) {
        if (args.length != 2) {
            System.out.println("错误：请指定要创建的文件路径（仅支持单个文件）！");
            return;
        }

        String path = args[1];
        File file = new File(path);
        if (file.exists()) {
            System.out.println("提示：文件已存在 → " + path);
            return;
        }

        try {
            // 创建父目录（若父目录不存在）
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (file.createNewFile()) {
                System.out.println("成功：文件创建完成 → " + path);
            } else {
                System.out.println("失败：文件创建失败 → " + path);
            }
        } catch (IOException e) {
            System.out.println("异常：文件创建出错 → " + e.getMessage());
        }
    }

    /** 处理rm命令：删除文件或目录（递归删除非空目录） */
    private static void handleRm(String[] args) {
        if (args.length != 2) {
            System.out.println("错误：请指定要删除的文件/目录路径（仅支持单个对象）！");
            return;
        }

        String path = args[1];
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("提示：文件/目录不存在 → " + path);
            return;
        }

        // 递归删除文件/目录
        boolean deleteSuccess = deleteFileOrDir(file);
        if (deleteSuccess) {
            System.out.println("成功：删除完成 → " + path);
        } else {
            System.out.println("失败：删除失败 → " + path);
        }
    }

    /** 处理ls命令：列出目录内容，支持配置文件过滤 */
    private static void handleLs(String[] args) {
        if (args.length != 2) {
            System.out.println("错误：请指定要列出的目录路径！");
            return;
        }

        String path = args[1];
        File dir = new File(path);
        if (!dir.exists()) {
            System.out.println("错误：目录不存在 → " + path);
            return;
        }
        if (!dir.isDirectory()) {
            System.out.println("错误：指定路径不是目录 → " + path);
            return;
        }

        // 读取配置文件中的过滤规则
        String fileFilter = getFileFilterFromConfig();
        System.out.println("=== 目录列表：" + dir.getAbsolutePath() + " ===");
        System.out.printf("%-20s %-20s %-20s %s%n", "名称", "最后修改时间", "类型", "路径");
        System.out.println("----------------------------------------------------------------------");

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("提示：该目录为空");
            return;
        }

        for (File file : files) {
            // 应用过滤规则
            if (!isFileMatchFilter(file, fileFilter)) {
                continue;
            }

            String name = file.getName();
            String modifyTime = SDF.format(new Date(file.lastModified()));
            String type = file.isDirectory() ? "目录(d)" : "文件(f)";
            String filePath = file.getAbsolutePath();
            System.out.printf("%-20s %-20s %-20s %s%n", name, modifyTime, type, filePath);
        }
    }

    // ======================== 工具方法 ========================
    /** 递归删除文件或目录 */
    private static boolean deleteFileOrDir(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    boolean success = deleteFileOrDir(child);
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        // 删除文件或空目录
        return file.delete();
    }

    /** 从配置文件读取文件过滤规则 */
    private static String getFileFilterFromConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            String filter = props.getProperty("file.filter", "").trim();
            return filter.isEmpty() ? null : filter;
        } catch (Exception e) {
            // 配置文件不存在或读取失败时，返回null（不过滤）
            return null;
        }
    }

    /** 判断文件是否符合过滤规则 */
    private static boolean isFileMatchFilter(File file, String filter) {
        // 无过滤规则时，全部显示
        if (filter == null || filter.isEmpty()) {
            return true;
        }

        // 目录默认全部显示，文件按后缀过滤
        if (file.isDirectory()) {
            return true;
        }

        String fileName = file.getName();
        return fileName.endsWith("." + filter);
    }

    /** 打印帮助信息 */
    private static void printHelp() {
        System.out.println("=== Java命令行文件操作工具(fsops) - 帮助手册 ===");
        System.out.println("使用方法：java -jar fsops.jar [命令] [参数]");
        System.out.println("支持的命令：");
        System.out.println("  mkdir [路径1] [路径2]...  - 批量创建目录（支持多级目录）");
        System.out.println("  nfile [文件路径]         - 新建空文件（父目录不存在时自动创建）");
        System.out.println("  rm [文件/目录路径]       - 删除文件或目录（非空目录递归删除）");
        System.out.println("  ls [目录路径]            - 列出目录内容（支持配置文件过滤）");
        System.out.println("  help                     - 显示本帮助信息");
        System.out.println("  self                     - 显示软件基本信息");
        System.out.println("=== 配置文件说明 ===");
        System.out.println("在程序运行目录创建 fsops.conf 文件，可设置文件过滤规则：");
        System.out.println("  file.filter=txt  # ls命令仅显示.txt文件（留空显示所有文件）");
    }

    /** 打印软件基本信息 */
    private static void printSelfInfo() {
        System.out.println("=== Java命令行文件操作工具(fsops) - 软件信息 ===");
        System.out.println("软件名称：fsops（File System Operations Tool）");
        System.out.println("版本号：v1.0");
        System.out.println("开发语言：Java");
        System.out.println("开发环境：JDK 1.8+");
        System.out.println("核心功能：目录/文件创建、删除、列表查询、配置过滤");
        System.out.println("开发者：[你的姓名]");
        System.out.println("编写日期：[实验日期]");
        System.out.println("使用说明：运行 'java -jar fsops.jar help' 查看详细命令");
    }
}