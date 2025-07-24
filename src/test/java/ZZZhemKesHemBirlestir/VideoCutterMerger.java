package ZZZhemKesHemBirlestir;

import java.io.*;
import java.util.*;

public class VideoCutterMerger {

    // ffmpeg komutunu çalıştıran yardımcı metot
    private static void runFFmpegCommand(List<String> command, File workingDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg komutu başarısız: " + String.join(" ", command));
        }
    }

    public static void main(String[] args) throws Exception {
        // Video dosyasının bulunduğu klasör
        String folderPath = "C:\\Users\\Hp\\OneDrive\\Desktop\\filmKesmeklik";
        File workingDir = new File(folderPath);

        String inputFile = "kesilecek.mp4";
        String output1 = "cut1.mp4";
        String output2 = "cut2.mp4";
        String mergedOutput = "merged.mp4";
        String fileList = "filelist.txt";

        // 10-20 arası (10 saniye başı, 10 saniye uzunluk)
        runFFmpegCommand(Arrays.asList(
                "ffmpeg", "-y", "-i", inputFile, "-ss", "10", "-t", "10", "-c", "copy", output1
        ), workingDir);

        // 20-40 arası (20 saniye başı, 20 saniye uzunluk)
        runFFmpegCommand(Arrays.asList(
                "ffmpeg", "-y", "-i", inputFile, "-ss", "20", "-t", "20", "-c", "copy", output2
        ), workingDir);

        // filelist.txt dosyasını oluştur
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(workingDir, fileList)))) {
            pw.println("file '" + output1 + "'");
            pw.println("file '" + output2 + "'");
        }

        // Birleştir
        runFFmpegCommand(Arrays.asList(
                "ffmpeg", "-y", "-f", "concat", "-safe", "0", "-i", fileList, "-c", "copy", mergedOutput
        ), workingDir);

        System.out.println("Kırpma ve birleştirme tamamlandı: " + mergedOutput);
    }
}