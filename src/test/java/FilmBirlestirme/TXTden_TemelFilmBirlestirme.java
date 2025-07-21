package FilmBirlestirme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TXTden_TemelFilmBirlestirme {

    public static void main(String[] args) {
        // --- AYARLAR ---
        String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";

        // 1. Adımda oluşturduğumuz, birleştirilecek videoların listesini içeren TXT dosyası.
        String listeDosyasi = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\birlestirilecek_filmler.txt";

        // 2. Tüm videoların birleştirileceği nihai çıktı dosyası.
        String cikisDosyasi = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\BIRLESTIRILMIS_VIDEO.mp4";

        System.out.println("Video birleştirme işlemi başlıyor...");
        System.out.println("Liste dosyası: " + listeDosyasi);

        // FFmpeg komutunu oluşturuyoruz.
        // -f concat: FFmpeg'e birleştirme (concatenate) işlemi yapacağını söyler.
        // -safe 0: Liste dosyasındaki mutlak yolların güvenli olduğunu belirtir (Gerekli bir ayar).
        // -i listeDosyasi: Girdi olarak TXT dosyasını kullanır.
        // -c copy: Videoları yeniden kodlamadan, olduğu gibi kopyalar. Bu, işlemi çok hızlı yapar.
        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath,
                "-f", "concat",
                "-safe", "0",
                "-i", listeDosyasi,
                "-c", "copy",
                cikisDosyasi
        );

        // Komutun çalışması sırasında oluşabilecek hataları daha iyi görebilmek için
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            // FFmpeg'in çıktısını (bilgi ve hata mesajları) anlık olarak konsola yazdır
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFMPEG: " + line);
                }
            }

            // İşlemin bitmesini bekle ve çıkış kodunu al
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("\n✓ Videolar başarıyla birleştirildi!");
                System.out.println("Çıktı: " + cikisDosyasi);
            } else {
                System.err.println("\n HATA: Video birleştirme işlemi başarısız oldu. Çıkış kodu: " + exitCode);
                System.err.println("Lütfen yukarıdaki FFMPEG çıktılarını kontrol edin.");
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("HATA: FFmpeg işlemi başlatılırken bir sorun oluştu.");
            e.printStackTrace();
        }
    }
}