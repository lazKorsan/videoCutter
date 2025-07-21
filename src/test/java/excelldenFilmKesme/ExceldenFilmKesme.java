package excelldenFilmKesme;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExceldenFilmKesme {

    /**
     * Excel'den okunan her bir kesme görevini temsil eden veri sınıfı.
     */
    public record KesimGorevi(String girisDosyasi, String cikisDosyasi, double baslangicSaniyesi, double bitisSaniyesi) {}

    public static void main(String[] args) {
        // --- GENEL AYARLAR ---
        String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";
        String excelDosyaYolu = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\gorevler.xlsx";

        System.out.println("Excel dosyasından kesim görevleri okunuyor...");

        // 1. Excel dosyasını oku ve görev listesini oluştur
        List<KesimGorevi> gorevListesi = exceldenGorevleriOku(excelDosyaYolu);

        if (gorevListesi.isEmpty()) {
            System.out.println("Excel dosyasında geçerli bir görev bulunamadı veya dosya okunamadı.");
            return;
        }

        System.out.println("Toplam " + gorevListesi.size() + " adet kesme görevi bulundu.");
        System.out.println("----------------------------------------");

        // 2. Listeyi döngü ile işle ve her görev için kesim yap
        for (KesimGorevi gorev : gorevListesi) {
            kesmeIslemiYap(ffmpegPath, gorev.girisDosyasi(), gorev.cikisDosyasi(), gorev.baslangicSaniyesi(), gorev.bitisSaniyesi());
            System.out.println("----------------------------------------");
        }

        System.out.println("\nTüm kesme görevleri başarıyla tamamlandı.");
    }

    /**
     * Verilen Excel dosyasını okur ve içindeki her satırı bir KesimGorevi nesnesine
     * dönüştürerek bir liste halinde döndürür.
     */
    public static List<KesimGorevi> exceldenGorevleriOku(String dosyaYolu) {
        List<KesimGorevi> gorevler = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(dosyaYolu);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // İlk sayfayı al
            int sonSatir = sheet.getLastRowNum();

            // İlk satır başlık olduğu için 1'den başla
            for (int i = 1; i <= sonSatir; i++) {
                Row satir = sheet.getRow(i);
                if (satir == null) continue; // Boş satırları atla

                try {
                    String giris = satir.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL).getStringCellValue();
                    String cikis = satir.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL).getStringCellValue();
                    double baslangic = satir.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL).getNumericCellValue();
                    double bitis = satir.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL).getNumericCellValue();

                    // Eğer ilk hücre boşsa o satırı geçersiz say
                    if (giris == null || giris.trim().isEmpty()) {
                        continue;
                    }

                    gorevler.add(new KesimGorevi(giris, cikis, baslangic, bitis));

                } catch (Exception e) {
                    System.err.printf("UYARI: Excel'in %d. satırı okunamadı veya formatı hatalı. Satır atlandı.%n", i + 1);
                }
            }

        } catch (IOException e) {
            System.err.println("HATA: Excel dosyası okunurken bir sorun oluştu: " + e.getMessage());
        }
        return gorevler;
    }

    /**
     * Belirtilen videoyu, verilen bilgilere göre keser.
     */
    public static void kesmeIslemiYap(String ffmpegPath, String girisDosyasi, String cikisDosyasi, double baslangicSaniyesi, double bitisSaniyesi) {
        double kesimSuresi = bitisSaniyesi - baslangicSaniyesi;
        if (kesimSuresi <= 0) {
            System.err.printf("HATA: Geçersiz aralık (%.2f-%.2f), kesim atlandı.%n", baslangicSaniyesi, bitisSaniyesi);
            return;
        }

        System.out.printf("İşlem: %s -> %s (%.2f sn - %.2f sn arası)%n", girisDosyasi, cikisDosyasi, baslangicSaniyesi, bitisSaniyesi);

        try {
            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(girisDosyasi)
                    .overrideOutputFiles(true)
                    .addOutput(cikisDosyasi)
                    .setStartOffset((long)(baslangicSaniyesi * 1000), TimeUnit.MILLISECONDS)
                    .setDuration((long)(kesimSuresi * 1000), TimeUnit.MILLISECONDS)
                    .setVideoCodec("copy")
                    .setAudioCodec("copy")
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
            executor.createJob(builder).run();

            System.out.println("✓ Başarıyla kesildi!");

        } catch (IOException e) {
            System.err.println("HATA: Bu görev işlenirken bir sorun oluştu: " + e.getMessage());
        }
    }
}