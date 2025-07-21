package temelFilmKesme;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Day01_temelFilmKesme {
    public static void main(String[] args) {
        // --- AYARLAR ---

        // 1. FFmpeg.exe dosyanızın tam yolunu buraya yazın.
        //    Bu yol yanlışsa kod çalışmaz.
        String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";

        // 2. Kesilecek videonun ve çıktının yollarını belirtin.
        String girisDosyasi = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\kesilecek.mp4";
        String cikisDosyasi = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\kesilmis_video_20-40sn.mp4";

        // 3. Kesme aralığını saniye olarak tanımlayın.
        long baslangicSaniyesi = 20;
        long bitisSaniyesi = 40;
        long kesimSuresi = bitisSaniyesi - baslangicSaniyesi; // Kesilecek parçanın toplam süresi

        // --- İŞLEM KISMI ---

        System.out.println("Video kesme işlemi başlıyor...");
        System.out.println("Kaynak: " + girisDosyasi);

        try {
            // FFmpeg nesnesini oluştur
            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);

            // FFmpeg komutunu adım adım oluştur
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(girisDosyasi)              // Giriş dosyası
                    .overrideOutputFiles(true)           // Çıktı dosyası varsa üzerine yaz
                    .addOutput(cikisDosyasi)             // Çıktı dosyası
                    .setStartOffset(baslangicSaniyesi, TimeUnit.SECONDS) // Başlangıç zamanı
                    .setDuration(kesimSuresi, TimeUnit.SECONDS)          // Kesimin ne kadar süreceği
                    .done();                             // Komut oluşturmayı bitir

            // Oluşturulan komutu çalıştır
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
            executor.createJob(builder).run();

            System.out.println("✓ Video başarıyla kesildi!");
            System.out.println("Çıktı: " + cikisDosyasi);

        } catch (IOException e) {
            System.err.println("HATA: Video kesme işlemi sırasında bir sorun oluştu.");
            System.err.println("Lütfen ffmpegPath'in doğru olduğundan ve video dosyasının varlığından emin olun.");
            e.printStackTrace();
        }
    }
}