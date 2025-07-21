package FilmBirlestirme;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Excelden_FilmBirlestirme {

    // Excel'den okunan verileri tutmak için veri sınıfları (record)
    public record BirlestirilecekDosya(String dosyaYolu, int siraNo) {}
    public record BirlestirmeGorevi(String gorevId, String cikisDosyasi, List<BirlestirilecekDosya> dosyalar) {}

    public static void main(String[] args) {
        // --- GENEL AYARLAR ---
        String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";
        String excelDosyaYolu = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\birlestirme_gorevleri.xlsx";

        System.out.println("Excel dosyasından birleştirme görevleri okunuyor...");

        // 1. Excel'i oku ve görevleri bir haritada (Map) sakla
        Map<String, BirlestirmeGorevi> gorevler = exceldenGorevleriOku(excelDosyaYolu);

        if (gorevler.isEmpty()) {
            System.out.println("Excel dosyasında geçerli bir görev bulunamadı veya dosya okunamadı.");
            return;
        }

        System.out.printf("Toplam %d adet birleştirme görevi bulundu.%n", gorevler.size());
        System.out.println("----------------------------------------");

        // 2. Her bir görevi döngü ile işle
        for (BirlestirmeGorevi gorev : gorevler.values()) {
            birlestirmeIslemiYap(ffmpegPath, gorev);
            System.out.println("----------------------------------------");
        }

        System.out.println("\nTüm birleştirme görevleri başarıyla tamamlandı.");
    }

    public static Map<String, BirlestirmeGorevi> exceldenGorevleriOku(String dosyaYolu) {
        Map<String, BirlestirmeGorevi> gorevler = new LinkedHashMap<>(); // Ekleme sırasını korumak için

        try (FileInputStream fis = new FileInputStream(dosyaYolu);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // --- Sayfa 1: BirlestirmeGorevleri Oku ---
            Sheet gorevlerSheet = workbook.getSheet("BirlestirmeGorevleri");
            if (gorevlerSheet == null) {
                System.err.println("HATA: Excel'de 'BirlestirmeGorevleri' sayfası bulunamadı.");
                return Collections.emptyMap();
            }
            for (int i = 1; i <= gorevlerSheet.getLastRowNum(); i++) {
                Row satir = gorevlerSheet.getRow(i);
                if (satir == null || satir.getCell(0) == null) continue;

                String gorevId = satir.getCell(0).getStringCellValue();
                String cikisDosyasi = satir.getCell(1).getStringCellValue();
                if (gorevId != null && !gorevId.trim().isEmpty()) {
                    gorevler.put(gorevId, new BirlestirmeGorevi(gorevId, cikisDosyasi, new ArrayList<>()));
                }
            }

            // --- Sayfa 2: BirlestirilecekDosyalar Oku ---
            Sheet dosyalarSheet = workbook.getSheet("BirlestirilecekDosyalar");
            if (dosyalarSheet == null) {
                System.err.println("HATA: Excel'de 'BirlestirilecekDosyalar' sayfası bulunamadı.");
                return Collections.emptyMap();
            }
            for (int i = 1; i <= dosyalarSheet.getLastRowNum(); i++) {
                Row satir = dosyalarSheet.getRow(i);
                if (satir == null || satir.getCell(0) == null) continue;

                String gorevId = satir.getCell(0).getStringCellValue();
                String parcaDosyaYolu = satir.getCell(1).getStringCellValue();
                int siraNo = (int) satir.getCell(2).getNumericCellValue();

                // İlgili görevi bul ve dosyayı listesine ekle
                if (gorevler.containsKey(gorevId)) {
                    // *** DÜZELTME BURADA YAPILDI ***
                    // 'dosyaYolu' yerine doğru değişken olan 'parcaDosyaYolu' kullanıldı.
                    gorevler.get(gorevId).dosyalar().add(new BirlestirilecekDosya(parcaDosyaYolu, siraNo));
                }
            }

            // Her görevin dosya listesini Sira_No'ya göre sırala
            gorevler.values().forEach(gorev ->
                    gorev.dosyalar().sort(Comparator.comparingInt(BirlestirilecekDosya::siraNo))
            );

        } catch (Exception e) {
            System.err.println("HATA: Excel dosyası okunurken bir sorun oluştu: " + e.getMessage());
            e.printStackTrace();
        }
        return gorevler;
    }

    public static void birlestirmeIslemiYap(String ffmpegPath, BirlestirmeGorevi gorev) {
        System.out.printf("Görev Başlatılıyor: '%s' -> %s%n", gorev.gorevId(), gorev.cikisDosyasi());

        if (gorev.dosyalar().isEmpty()) {
            System.err.println("UYARI: Bu görev için birleştirilecek dosya bulunamadı. Görev atlandı.");
            return;
        }

        Path listeDosyasiYolu = null;
        try {
            // 1. FFmpeg için geçici bir liste dosyası oluştur
            listeDosyasiYolu = Files.createTempFile("ffmpeg_concat_list_", ".txt");
            try (BufferedWriter writer = Files.newBufferedWriter(listeDosyasiYolu)) {
                for (BirlestirilecekDosya dosya : gorev.dosyalar()) {
                    // FFmpeg formatı: file 'dosya/yolu.mp4'
                    writer.write("file '" + dosya.dosyaYolu().replace("\\", "/") + "'");
                    writer.newLine();
                }
            }

            // 2. ProcessBuilder ile FFmpeg komutunu çalıştır
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ffmpegPath,
                    "-f", "concat",
                    "-safe", "0",
                    "-i", listeDosyasiYolu.toAbsolutePath().toString(),
                    "-c", "copy",
                    "-y", // Çıktı dosyası varsa üzerine yaz
                    gorev.cikisDosyasi()
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // FFmpeg çıktısını konsola yazdır
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFMPEG: " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Dosyanın tam yolunu da yazdırıyoruz!
                System.out.printf("\n✓ Görev başarıyla tamamlandı! Dosya şuraya kaydedildi: %s%n", gorev.cikisDosyasi());
            } else {
                System.err.printf("%n HATA: Görev başarısız oldu. Çıkış kodu: %d%n", exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("HATA: FFmpeg işlemi sırasında bir sorun oluştu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 3. Geçici liste dosyasını sil
            if (listeDosyasiYolu != null) {
                try {
                    Files.delete(listeDosyasiYolu);
                } catch (IOException e) {
                    System.err.println("UYARI: Geçici liste dosyası silinemedi: " + listeDosyasiYolu);
                }
            }
        }
    }
}