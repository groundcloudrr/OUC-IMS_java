import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Properties;
import java.util.Vector;

/**
 * 单文件版文件加解密工具（整合所有功能：GUI、3DES、多线程、配置文件）
 * 满足实验全部要求：图形界面、密钥保存、多文件处理、进度显示、日志输出
 */
public class FileEncryptDecryptTool extends JFrame {
    // 界面组件
    private JTextField keyField; // 密钥输入框
    private JList<File> fileList; // 待处理文件列表
    private JTextArea logArea; // 日志显示域
    private JProgressBar progressBar; // 进度条
    private Vector<File> selectedFiles; // 选中的文件集合

    // 常量定义
    private static final String ALGORITHM = "DESede/ECB/PKCS5Padding"; // 3DES算法（带填充，兼容不同环境）
    private static final String CONFIG_PATH = "config.properties"; // 配置文件路径
    private static final int KEY_LENGTH = 24; // 密钥长度（3DES要求24字节）

    /**
     * 主方法：程序入口
     */
    public static void main(String[] args) {
        //  Swing线程安全启动
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // 系统风格界面
                new FileEncryptDecryptTool().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 构造方法：初始化界面和组件
     */
    public FileEncryptDecryptTool() {
        // 窗口基本设置
        setTitle("文件加解密工具（3DES算法）");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. 顶部区域：密钥输入 + 功能按钮
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("加密密钥（24位字符）："));

        // 密钥输入框（读取配置文件自动填充）
        keyField = new JTextField(20);
        String savedKey = ConfigUtil.getKey();
        if (savedKey != null) {
            keyField.setText(savedKey);
        }
        topPanel.add(keyField);

        // 选择文件按钮
        JButton selectBtn = new JButton("选择文件");
        selectBtn.addActionListener(new SelectFileListener());
        topPanel.add(selectBtn);

        // 加解密按钮（默认禁用，密钥符合要求后启用）
        JButton processBtn = new JButton("开始加/解密");
        processBtn.addActionListener(new ProcessFileListener());
        processBtn.setEnabled(checkKeyValid()); // 校验初始密钥有效性
        topPanel.add(processBtn);

        // 密钥输入框监听：实时校验密钥长度
        keyField.addActionListener(e -> processBtn.setEnabled(checkKeyValid()));
        keyField.addCaretListener(e -> processBtn.setEnabled(checkKeyValid()));

        add(topPanel, BorderLayout.NORTH);

        // 2. 中间区域：文件列表 + 日志显示
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // 文件列表（带滚动条）
        selectedFiles = new Vector<>();
        fileList = new JList<>(selectedFiles);
        fileList.setBorder(BorderFactory.createTitledBorder("待处理文件"));
        centerPanel.add(new JScrollPane(fileList));

        // 日志显示域（带滚动条，不可编辑）
        logArea = new JTextArea();
        logArea.setBorder(BorderFactory.createTitledBorder("操作日志"));
        logArea.setEditable(false);
        logArea.setLineWrap(true); // 自动换行
        centerPanel.add(new JScrollPane(logArea));

        add(centerPanel, BorderLayout.CENTER);

        // 3. 底部区域：进度条
        progressBar = new JProgressBar();
        progressBar.setBorder(BorderFactory.createTitledBorder("处理进度"));
        progressBar.setMaximum(100);
        add(progressBar, BorderLayout.SOUTH);
    }

    /**
     * 校验密钥有效性（必须24位字符）
     */
    private boolean checkKeyValid() {
        String key = keyField.getText().trim();
        return key.length() == KEY_LENGTH;
    }

    /**
     * 内部类：选择文件按钮监听器
     */
    private class SelectFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true); // 支持多文件选择
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // 只选文件
            int result = fileChooser.showOpenDialog(FileEncryptDecryptTool.this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                selectedFiles.clear();
                for (File file : files) {
                    selectedFiles.add(file);
                }
                fileList.updateUI(); // 刷新文件列表
                logArea.append("已选择 " + files.length + " 个文件\n");
            }
        }
    }

    /**
     * 内部类：开始加解密按钮监听器
     */
    private class ProcessFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(FileEncryptDecryptTool.this, "请先选择文件！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String key = keyField.getText().trim();
            // 保存密钥到配置文件
            ConfigUtil.saveKey(key);
            logArea.append("密钥已保存至配置文件\n");

            // 为每个文件启动独立线程处理（多线程并发）
            for (File file : selectedFiles) {
                new FileProcessThread(file, key).start();
            }
        }
    }

    /**
     * 内部类：文件加解密线程（每个文件一个线程，避免界面卡顿）
     */
    private class FileProcessThread extends Thread {
        private File file;
        private String key;

        public FileProcessThread(File file, String key) {
            this.file = file;
            this.key = key;
        }

        @Override
        public void run() {
            try {
                // 1. 读取文件内容
                logArea.append("开始处理文件：" + file.getName() + "\n");
                FileInputStream fis = new FileInputStream(file);
                byte[] fileData = new byte[fis.available()];
                fis.read(fileData);
                fis.close();

                // 2. 判断加密/解密模式（.enc后缀为解密，否则为加密）
                byte[] resultData;
                String outputPath;
                boolean isEncrypt = !file.getName().endsWith(".enc");

                if (isEncrypt) {
                    // 加密：生成.enc后缀文件
                    resultData = DESUtil.encrypt(fileData, key.getBytes());
                    outputPath = file.getAbsolutePath() + ".enc";
                } else {
                    // 解密：去掉.enc后缀，添加时间戳避免覆盖
                    resultData = DESUtil.decrypt(fileData, key.getBytes());
                    String originalName = file.getName().substring(0, file.getName().lastIndexOf(".enc"));
                    outputPath = file.getParent() + File.separator + System.currentTimeMillis() + "-" + originalName;
                }

                // 3. 写入处理结果到文件（更新进度条）
                FileOutputStream fos = new FileOutputStream(outputPath);
                // 分块写入，更新进度（模拟大文件进度显示）
                int blockSize = 1024 * 10; // 10KB/块
                int totalBlocks = (fileData.length + blockSize - 1) / blockSize;
                int currentBlock = 0;

                for (int i = 0; i < resultData.length; i += blockSize) {
                    int writeSize = Math.min(blockSize, resultData.length - i);
                    fos.write(resultData, i, writeSize);
                    currentBlock++;
                    int progress = (currentBlock * 100) / totalBlocks;
                    // Swing线程安全更新进度条
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                }

                fos.close();
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(100); // 单个文件处理完成，进度条满格
                    logArea.append((isEncrypt ? "加密" : "解密") + "成功：" + outputPath + "\n");
                });

            } catch (Exception ex) {
                // 异常信息写入日志（线程安全）
                SwingUtilities.invokeLater(() -> {
                    logArea.append("处理失败：" + file.getName() + "，原因：" + ex.getMessage() + "\n");
                    progressBar.setValue(0);
                });
                ex.printStackTrace();
            }
        }
    }

    /**
     * 静态内部类：3DES加解密工具类（核心算法封装）
     */
    private static class DESUtil {
        /**
         * 加密方法
         */
        public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
            SecretKey secretKey = new SecretKeySpec(key, ALGORITHM.split("/")[0]);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        }

        /**
         * 解密方法
         */
        public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
            SecretKey secretKey = new SecretKeySpec(key, ALGORITHM.split("/")[0]);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(data);
        }
    }

    /**
     * 静态内部类：配置文件工具类（密钥持久化）
     */
    private static class ConfigUtil {
        /**
         * 读取保存的密钥
         */
        public static String getKey() {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
                props.load(fis);
                return props.getProperty("encrypt.key");
            } catch (FileNotFoundException e) {
                // 配置文件不存在时返回null（首次启动）
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * 保存密钥到配置文件
         */
        public static void saveKey(String key) {
            Properties props = new Properties();
            try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
                props.setProperty("encrypt.key", key);
                props.store(fos, "File Encrypt Tool - 3DES Key"); // 配置文件注释
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}