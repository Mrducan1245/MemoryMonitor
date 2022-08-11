import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MemoryMonitor {
    private final String memoryFIle;
    private final String[] robotStatus;

    private FileWriter fileWriter;
    private String temp = "firstWords";

    public MemoryMonitor(String memoryFIle, FileWriter fileWriter, String[] robotStatus) {
        this.memoryFIle = memoryFIle;
        this.fileWriter = fileWriter;
        this.robotStatus = robotStatus;
    }

    public BufferedReader excuteCommand(String[] command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        BufferedReader reader;
        try {
            Process process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return reader;
    }

    public void writeFileTitle(String title) {
        ifFileDeadThenLive(memoryFIle);
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            if (fileWriter==null) fileWriter = new FileWriter(memoryFIle,true);
            fileWriter.write("\n");
            fileWriter.write("\n");
            fileWriter.write("==============================================================================================================================" + "\n");
            fileWriter.write("Current step is :" + title + "\t" + dateFormat.format(date) + "\n");
            fileWriter.write("==============================================================================================================================" + "\n");
            fileWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Todo:fileWrite be closed.
//        if (fileWriter != null) {
////            try {
////                fileWriter.close();
////            } catch (IOException e) {
////                throw new RuntimeException(e);
////            }
//        }
    }

    /**
     * 6 memo ,4% memo;comandName,11  ",+"
     *
     * @param oldLine
     */
    public void interfiteLine(String oldLine, String[] strMachList) {
        //TODO:the
        for (String s : strMachList) {
            if (!oldLine.contains(s) || oldLine.contains("MemoryMonitor.jar")) {
                return;
            }
        }
        String[] words = oldLine.split(" +");
        //no" "word
        String dataTime = words[8];
        int pos = oldLine.lastIndexOf(dataTime);
        if (pos == -1) return;
        int comandNmaePos = pos + 13;

        String[] checkTotalMeme = {"free", "-m"};
        BufferedReader reader = excuteCommand(checkTotalMeme);
        try {
//            juMp to next LINE
            reader.readLine();
            String str = reader.readLine();
            String[] strList = str.split(" +");

            float memo = (float) (Float.parseFloat(words[3]) * 81.92);
            //TODO:sava memo if same will not write
            float memo_penc = memo / (Float.parseFloat(strList[1])) * 100;
            String comandNmae = oldLine.substring(comandNmaePos);
            String newLine = String.format("%-6.3f", memo) + "\t" + String.format("%5.2f", memo_penc) + "%" + "\t" + comandNmae + "\n";

            fileWriter = new FileWriter(memoryFIle, true);
            fileWriter.write(newLine);
            fileWriter.flush();
            System.out.println(newLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //journalctl -f | grep huanming
    public void autoCollectData(String[] matchStr) {
        String[] command = {"journalctl", "-f"};
        BufferedReader reader = excuteCommand(command);
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                for (String status : robotStatus) {
                    if (line.contains(status)) {
                        if (status.equals(temp)) continue;
                        else temp = status;
                        writeFileTitle(status);
                        String[] command2 = {"ps", "aux"};
                        BufferedReader reader2 = excuteCommand(command2);
                        String dataLine = null;
                        int count = 0;
                        while (true) {
                            try {
                                if ((dataLine = reader2.readLine()) == null) break;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            interfiteLine(dataLine, matchStr);
                            count++;
                        }
                        try {
                            reader2.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (reader != null) reader.close();
                if (fileWriter != null) fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void collectData(String[] matchStrs, long mills, String title) {
        String[] command2 = {"ps", "aux"};
        BufferedReader reader2;
        while (true) {
            reader2 = excuteCommand(command2);
            String dataLine = null;
            try {
                writeFileTitle(title);
                while ((dataLine = reader2.readLine()) != null) {
                    interfiteLine(dataLine, matchStrs);
                }
                reader2.close();
                Thread.sleep(mills);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void ifFileDeadThenLive(String memoryFIle) {
        File file = new File(memoryFIle);
        try {
            if (!file.exists() && file.createNewFile()) {
                fileWriter = new FileWriter(file, true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
