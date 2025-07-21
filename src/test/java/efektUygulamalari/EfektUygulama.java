package efektUygulamalari;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class EfektUygulama {

    // Efekt görevini tutacak veri sınıfı
    public record EfektGorevi(
            String girisDosyasi, String cikisDosyasi,
            int baslangicSaniyesi, int bitisSaniyesi,
            int x, int y, int genislik, int yukseklik, int blurSiddeti
    ) {}

    public static void main(String[] args) {
        String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";
        String excelDosyaYolu = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\efektler.xlsx";

        List<EfektGorevi> gorevler = exceldenEfektleriOku(excelDosyaYolu);

        if (gorevler.isEmpty()) {
            System.out.println("Uygulanacak efekt görevi bulunamadı.");
            return;
        }

        System.out.printf("Toplam %d adet efekt görevi bulundu.%n", gorevler.size());
        System.out.println("----------------------------------------");

        for (EfektGorevi gorev : gorevler) {
            blurEfektiUygula(ffmpegPath, gorev);
            System.out.println("----------------------------------------");
        }
        System.out.println("Tüm efekt görevleri tamamlandı.");
    }

    public static void blurEfektiUygula(String ffmpegPath, EfektGorevi gorev) {
        System.out.printf("Efekt uygulanıyor: %s -> %s%n", gorev.girisDosyasi(), gorev.cikisDosyasi());

        // FFmpeg'in filter_complex dizesini dinamik olarak oluştur
        String filterComplex = String.format(
                "[0:v]split[main][blur];" +
                        "[blur]crop=%d:%d:%d:%d,boxblur=%d[blurred_box];" +
                        "[main][blurred_box]overlay=%d:%d:enable='between(t,%d,%d)'",
                gorev.genislik(), gorev.yukseklik(), gorev.x(), gorev.y(), // crop
                gorev.blurSiddeti(), // boxblur
                gorev.x(), gorev.y(), // overlay
                gorev.baslangicSaniyesi(), gorev.bitisSaniyesi() // enable
        );

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ffmpegPath,
                    "-i", gorev.girisDosyasi(),
                    "-filter_complex", filterComplex,
                    "-c:a", "copy",
                    "-y", // Üzerine yaz
                    gorev.cikisDosyasi()
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFMPEG: " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.printf("\n✓ Efekt başarıyla uygulandı! Dosya: %s%n", gorev.cikisDosyasi());
            } else {
                System.err.printf("%n HATA: Efekt uygulanırken sorun oluştu. Çıkış kodu: %d%n", exitCode);
            }

        } catch (Exception e) {
            System.err.println("HATA: FFmpeg işlemi sırasında bir sorun oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<EfektGorevi> exceldenEfektleriOku(String dosyaYolu) {
        List<EfektGorevi> gorevler = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(dosyaYolu);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // İlk sayfayı oku
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 1'den başla (başlık satırını atla)
                Row satir = sheet.getRow(i);
                if (satir == null) continue;

                gorevler.add(new EfektGorevi(
                        satir.getCell(0).getStringCellValue(), // Giris_Dosyasi
                        satir.getCell(1).getStringCellValue(), // Cikis_Dosyasi
                        (int) satir.getCell(2).getNumericCellValue(), // Baslangic_Saniyesi
                        (int) satir.getCell(3).getNumericCellValue(), // Bitis_Saniyesi
                        (int) satir.getCell(4).getNumericCellValue(), // X_Koordinati
                        (int) satir.getCell(5).getNumericCellValue(), // Y_Koordinati
                        (int) satir.getCell(6).getNumericCellValue(), // Genislik
                        (int) satir.getCell(7).getNumericCellValue(), // Yukseklik
                        (int) satir.getCell(8).getNumericCellValue()  // Blur_Siddeti
                ));
            }
        } catch (Exception e) {
            System.err.println("HATA: Efektler için Excel dosyası okunurken bir sorun oluştu: " + e.getMessage());
            e.printStackTrace();
        }
        return gorevler;
    }
}