import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static final String[] robotStatus = new String[]{"relocalization success","Latest pose node has not been added",
            "MOVING", "DownloadSourceTesterCallable",
            "DownloadStateManager", "huanming","on emergency status change -> continue move",
    "on emergency status change -> pause move","start move task"};
    private static final boolean ifAuto = true;
    private static MemoryMonitor memoryMonitor;
    private static String memoryFile;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Please import args");
            return;
        }
        String userHome = System.getProperty("user.home");
        memoryFile = userHome + "/" + "memoryLog";
        System.out.println(memoryFile);

        File file = new File(memoryFile);
        FileWriter fileWriter = null;
        try {
            if (file.exists() || file.createNewFile()) {
                fileWriter = new FileWriter(file, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //new Object
        memoryMonitor = new MemoryMonitor(memoryFile, fileWriter, robotStatus);

        //TODO:mabe needTO reset match word.
        //"java","syrius"
        String[] matches = {"java","syrius"};

        //手动收集模式   间隔时间为ns  机器人阶段名称为
        if (args[0].equals("no")) {
            long intervalMills = 1000;
            String title = "No define step,plese reset";
            switch (args.length) {
                case 2:
                    intervalMills = (Long.parseLong(args[1])) * 1000;
                    break;
                case 3:
                    title = args[2];
                    intervalMills = (Long.parseLong(args[1])) * 1000;
                    System.out.println("手动收集模式staart," + "间隔时间为" + args[1] + "s" + ",机器人阶段名称为:" + title);
                    break;
            }
            memoryMonitor.collectData(matches, intervalMills, title);
        } else {
            System.out.println("自动收集模式开启：  自动判断当前机器人阶段，并将内存数据保存于文件中");
            memoryMonitor.autoCollectData(matches);
        }
    }

}
