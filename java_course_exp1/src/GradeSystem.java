import java.io.*;
import java.util.*;

class Student {
    private String id;
    private String name;
    private int java;
    private int math;

    public Student(String i, String n, int j, int m) {
        id = i;
        name = n;
        java = j;
        math = m;
    }

    public int total() { return java + math; }
    public int getJava() { return java; }
    public int getMath() { return math; }
    public String getId() { return id; }
    public String getName() { return name; }

    public String info() {
        return String.format("ID: %-5s 姓名: %-8s Java: %-3d 数学: %-3d 总分: %-4d",
                id, name, java, math, total());
    }

    public String toFile() {
        return id + "," + name + "," + java + "," + math;
    }
}

class Manager {
    private Student[] sts = new Student[100];
    private int cnt = 0;
    private static final String FILE_PATH = "students.txt";

    public boolean add(String id, String name, int java, int math) {
        if (java < 0 || java > 100 || math < 0 || math > 100) {
            System.out.println("成绩必须在0-100范围内");
            return false;
        }
        if (cnt >= 100) {
            System.out.println("已达最大容量");
            return false;
        }
        sts[cnt++] = new Student(id, name, java, math);
        return true;
    }

    public void showAll() {
        if (cnt == 0) {
            System.out.println("没有学生数据");
            return;
        }
        for (int i = 0; i < cnt; i++) {
            System.out.println(sts[i].info());
        }
    }

    public void find(String id) {
        for (int i = 0; i < cnt; i++) {
            if (sts[i].getId().equals(id)) {
                System.out.println("找到: " + sts[i].info());
                return;
            }
        }
        System.out.println("未找到学号: " + id);
    }

    public double classAvg(int dim) {
        if (cnt == 0) return 0;
        double sum = 0;
        for (int i = 0; i < cnt; i++) {
            switch(dim) {
                case 0: sum += sts[i].total(); break;       // 总分(200分制)
                case 1: sum += sts[i].getJava(); break;      // Java(100分制)
                case 2: sum += sts[i].getMath(); break;      // 数学(100分制)
            }
        }
        return sum / cnt;
    }

    private void countGrades(int[] grades, int dim) {
        Arrays.fill(grades, 0);
        for (int i = 0; i < cnt; i++) {
            int score;
            switch (dim) {
                case 0: // 总分按平均分计算等级(100分制)
                    score = sts[i].total() / 2;
                    break;
                case 1: // Java成绩(100分制)
                    score = sts[i].getJava();
                    break;
                default: // 数学成绩(100分制)
                    score = sts[i].getMath();
            }

            if (score >= 90) grades[0]++;
            else if (score >= 80) grades[1]++;
            else if (score >= 70) grades[2]++;
            else if (score >= 60) grades[3]++;
            else grades[4]++;
        }
    }

    public void showDist(int dim) {
        int[] grades = new int[5];
        countGrades(grades, dim);

        String[] titles = {"总分(换算百分制)", "Java", "数学"};
        System.out.println("\n" + titles[dim] + "等级分布:");
        System.out.printf("A(90-100): %-2d | ", grades[0]);
        System.out.println("*".repeat(grades[0]));
        System.out.printf("B(80-89):  %-2d | ", grades[1]);
        System.out.println("*".repeat(grades[1]));
        System.out.printf("C(70-79):  %-2d | ", grades[2]);
        System.out.println("*".repeat(grades[2]));
        System.out.printf("D(60-69):  %-2d | ", grades[3]);
        System.out.println("*".repeat(grades[3]));
        System.out.printf("E(0-59):   %-2d | ", grades[4]);
        System.out.println("*".repeat(grades[4]));
    }

    public void topBottom(int dim) {
        if (cnt == 0) {
            System.out.println("无学生数据");
            return;
        }

        Student max = sts[0], min = sts[0];
        for (int i = 1; i < cnt; i++) {
            int cur = getScore(sts[i], dim);
            int maxVal = getScore(max, dim);
            int minVal = getScore(min, dim);

            if (cur > maxVal) max = sts[i];
            if (cur < minVal) min = sts[i];
        }

        String[] titles = {"总分", "Java", "数学"};
        String maxInfo = String.format("ID: %-5s 姓名: %-8s Java: %-3d 数学: %-3d 总分: %-4d",
                max.getId(), max.getName(), max.getJava(), max.getMath(), max.total());
        String minInfo = String.format("ID: %-5s 姓名: %-8s Java: %-3d 数学: %-3d 总分: %-4d",
                min.getId(), min.getName(), min.getJava(), min.getMath(), min.total());

        System.out.println(titles[dim] + "最高分: " + maxInfo);
        System.out.println(titles[dim] + "最低分: " + minInfo);
    }

    private int getScore(Student s, int dim) {
        return switch(dim) {
            case 0 -> s.total();     // 总分(200分制)
            case 1 -> s.getJava();   // Java(100分制)
            default -> s.getMath();  // 数学(100分制)
        };
    }

    public void sortByTotal() {
        for (int i = 0; i < cnt - 1; i++) {
            for (int j = 0; j < cnt - i - 1; j++) {
                if (sts[j].total() < sts[j+1].total()) {
                    Student tmp = sts[j];
                    sts[j] = sts[j+1];
                    sts[j+1] = tmp;
                }
            }
        }
        System.out.println("已按总分从高到低排序");
    }

    public void sortById() {
        for (int i = 0; i < cnt - 1; i++) {
            for (int j = 0; j < cnt - i - 1; j++) {
                if (sts[j].getId().compareTo(sts[j+1].getId()) > 0) {
                    Student tmp = sts[j];
                    sts[j] = sts[j+1];
                    sts[j+1] = tmp;
                }
            }
        }
        System.out.println("已按学号升序排序");
    }

    public void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (int i = 0; i < cnt; i++) {
                pw.println(sts[i].toFile());
            }
            System.out.println("数据已保存至 " + FILE_PATH);
        } catch (IOException e) {
            System.out.println("保存失败: " + e.getMessage());
        }
    }
}

public class GradeSystem {
    public static void main(String[] args) {
        Manager mgr = new Manager();
        Scanner sc = new Scanner(System.in);
        int opt;

        loadFromFile(mgr);

        do {
            System.out.println("\n===== 学生成绩管理系统 =====");
            System.out.println("1. 添加学生");
            System.out.println("2. 显示所有学生");
            System.out.println("3. 按学号查找");
            System.out.println("4. 计算平均分");
            System.out.println("5. 统计成绩分布");
            System.out.println("6. 查找最高/最低分");
            System.out.println("7. 按总分排序");
            System.out.println("8. 按学号排序");
            System.out.println("9. 保存数据到文件");
            System.out.println("0. 退出系统");
            System.out.print("请选择功能: ");

            opt = sc.nextInt();
            sc.nextLine();

            switch (opt) {
                case 1 -> addStudent(mgr, sc);
                case 2 -> mgr.showAll();
                case 3 -> findStudent(mgr, sc);
                case 4 -> calcAvg(mgr, sc);
                case 5 -> showDist(mgr, sc);
                case 6 -> findTopBottom(mgr, sc);
                case 7 -> mgr.sortByTotal();
                case 8 -> mgr.sortById();
                case 9 -> mgr.saveData();
                case 0 -> System.out.println("系统退出");
                default -> System.out.println("无效选项");
            }
        } while (opt != 0);

        sc.close();
    }

    private static void loadFromFile(Manager mgr) {
        try (BufferedReader br = new BufferedReader(new FileReader("students.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    mgr.add(parts[0], parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                }
            }
            System.out.println("已加载历史数据");
        } catch (IOException | NumberFormatException e) {
            System.out.println("无历史数据文件");
        }
    }

    private static void addStudent(Manager mgr, Scanner sc) {
        System.out.print("学号: ");
        String id = sc.nextLine();
        System.out.print("姓名: ");
        String name = sc.nextLine();
        System.out.print("Java成绩: ");
        int java = sc.nextInt();
        System.out.print("数学成绩: ");
        int math = sc.nextInt();
        sc.nextLine();
        mgr.add(id, name, java, math);
    }

    private static void findStudent(Manager mgr, Scanner sc) {
        System.out.print("输入学号: ");
        String id = sc.nextLine();
        mgr.find(id);
    }

    private static void calcAvg(Manager mgr, Scanner sc) {
        System.out.println("1. 总分平均  2. Java平均  3. 数学平均");
        System.out.print("请选择维度: ");
        int dim = getValidDim(sc);
        double avg = mgr.classAvg(dim);
        String[] titles = {"总分(200分制)", "Java(100分制)", "数学(100分制)"};
        System.out.printf("%s平均分: %.1f\n", titles[dim], avg);
    }

    private static void showDist(Manager mgr, Scanner sc) {
        System.out.println("1. 总分分布  2. Java分布  3. 数学分布");
        System.out.print("请选择维度: ");
        int dim = getValidDim(sc);
        mgr.showDist(dim);
    }

    private static void findTopBottom(Manager mgr, Scanner sc) {
        System.out.println("1. 总分排名  2. Java排名  3. 数学排名");
        System.out.print("请选择维度: ");
        int dim = getValidDim(sc);
        mgr.topBottom(dim);
    }

    private static int getValidDim(Scanner sc) {
        int input = sc.nextInt();
        sc.nextLine();

        if (input < 1 || input > 3) {
            System.out.println("无效维度! 默认使用总分维度");
            return 0;
        }
        return input - 1;
    }
}