package ZZZhemKesHemBirlestir;

import java.io.*;
import java.util.*;

public class VideoCutterMergerFromTxt {

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

    public static void main(String[] args) {
        String folderPath = "C:\\Users\\Hp\\OneDrive\\Desktop\\filmKesmeklik";
        File workingDir = new File(folderPath);

        String inputMp4 = "kesilecek.mp4";
        String araliklarTxt = "kesmearaliklari.txt";
        String mergedOutput = "merged.mp4";
        String fileList = "filelist.txt";

        // Dosya kontrolleri
        File mp4File = new File(workingDir, inputMp4);
        File txtFile = new File(workingDir, araliklarTxt);
        if (!mp4File.exists()) {
            System.err.println("MP4 dosyası bulunamadı: " + mp4File.getAbsolutePath());
            return;
        }
        if (!txtFile.exists()) {
            System.err.println("Aralıklar dosyası bulunamadı: " + txtFile.getAbsolutePath());
            return;
        }

        // Aralıkları oku
        List<String[]> intervals = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(txtFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("-");
                if (parts.length == 2 && parts[0].matches("\\d+") && parts[1].matches("\\d+")) {
                    intervals.add(parts);
                } else {
                    System.err.println("Geçersiz aralık satırı: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Aralıklar dosyası okunamadı: " + e.getMessage());
            return;
        }

        if (intervals.isEmpty()) {
            System.err.println("Aralık dosyasında geçerli satır yok.");
            return;
        }

        // Her aralığı kes ve dosya adlarını topla
        List<String> cutFiles = new ArrayList<>();
        int idx = 1;
        for (String[] interval : intervals) {
            try {
                int start = Integer.parseInt(interval[0]);
                int end = Integer.parseInt(interval[1]);
                int duration = end - start;
                if (duration <= 0) {
                    System.err.println("Geçersiz süre: " + start + "-" + end);
                    continue;
                }
                String cutFile = String.format("cut%d.mp4", idx++);
                runFFmpegCommand(Arrays.asList(
                        "ffmpeg", "-y", "-i", inputMp4, "-ss", String.valueOf(start), "-t", String.valueOf(duration), "-c", "copy", cutFile
                ), workingDir);
                cutFiles.add(cutFile);
            } catch (Exception e) {
                System.err.println("Aralık işlenemedi: " + Arrays.toString(interval) + " Hata: " + e.getMessage());
            }
        }

        if (cutFiles.isEmpty()) {
            System.err.println("Hiçbir dosya kesilemedi.");
            return;
        }

        // filelist.txt oluştur
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(workingDir, fileList)))) {
            for (String cutFile : cutFiles) {
                pw.println("file '" + cutFile + "'");
            }
        } catch (IOException e) {
            System.err.println("filelist.txt oluşturulamadı: " + e.getMessage());
            return;
        }

        // Birleştir
        try {
            runFFmpegCommand(Arrays.asList(
                    "ffmpeg", "-y", "-f", "concat", "-safe", "0", "-i", fileList, "-c", "copy", mergedOutput
            ), workingDir);
        } catch (Exception e) {
            System.err.println("Birleştirme hatası: " + e.getMessage());
            return;
        }

        System.out.println("Tüm aralıklar kesildi ve birleştirildi: " + mergedOutput);
    }
}