package ZZZhemKesHemBirlestir;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class VideoCutterMergerFromExcel {

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
        String folderPath = "C:\\Users\\Hp\\OneDrive\\Desktop\\filmKesmeklik";
        File workingDir = new File(folderPath);

        String inputMp4 = "kesilecek.mp4";
        String excelFileName = "kesimaraliklari.xlsx";
        String mergedOutput = "merged.mp4";
        String fileList = "filelist.txt";

        // Dosya kontrolleri
        File mp4File = new File(workingDir, inputMp4);
        File excelFile = new File(workingDir, excelFileName);
        if (!mp4File.exists()) {
            System.err.println("MP4 dosyası bulunamadı: " + mp4File.getAbsolutePath());
            return;
        }
        if (!excelFile.exists()) {
            System.err.println("Excel dosyası bulunamadı: " + excelFile.getAbsolutePath());
            return;
        }

        // Excel'den aralıkları oku
        List<int[]> intervals = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(excelFile)) {
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Başlığı atla
                Cell startCell = row.getCell(0);
                Cell endCell = row.getCell(1);
                if (startCell == null || endCell == null) continue;
                if (startCell.getCellType() == CellType.NUMERIC && endCell.getCellType() == CellType.NUMERIC) {
                    int start = (int) startCell.getNumericCellValue();
                    int end = (int) endCell.getNumericCellValue();
                    if (start < end)
                        intervals.add(new int[]{start, end});
                }
            }
            workbook.close();
        }

        if (intervals.isEmpty()) {
            System.err.println("Excel dosyasında geçerli aralık bulunamadı.");
            return;
        }

        // Her aralığı kes ve dosya adlarını topla
        List<String> cutFiles = new ArrayList<>();
        int idx = 1;
        for (int[] interval : intervals) {
            int start = interval[0];
            int end = interval[1];
            int duration = end - start;
            String cutFile = String.format("cut%d.mp4", idx++);
            runFFmpegCommand(Arrays.asList(
                    "ffmpeg", "-y", "-i", inputMp4, "-ss", String.valueOf(start), "-t", String.valueOf(duration), "-c", "copy", cutFile
            ), workingDir);
            cutFiles.add(cutFile);
        }

        // filelist.txt oluştur
        try (PrintWriter pw = new PrintWriter(new FileWriter(new File(workingDir, fileList)))) {
            for (String cutFile : cutFiles) {
                pw.println("file '" + cutFile + "'");
            }
        }

        // Birleştir
        runFFmpegCommand(Arrays.asList(
                "ffmpeg", "-y", "-f", "concat", "-safe", "0", "-i", fileList, "-c", "copy", mergedOutput
        ), workingDir);

        System.out.println("Tüm aralıklar kesildi ve birleştirildi: " + mergedOutput);
    }
}