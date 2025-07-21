package temelFilmKesme;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Day01_uclu_kesme {

    public static void main(String[] args) {
        // --- GENEL AYARLAR ---
        // Bu yolların doğru olduğundan emin olun
        String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";
        String girisDosyasi = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\kesilecek.mp4";
        String cikisKlasoru = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\";

        System.out.println("Üç parçalı kesme işlemi başlıyor...");
        System.out.println("----------------------------------------");

        // --- 1. KESİM: 20-30 saniye arası ---
        kesmeIslemiYap(ffmpegPath, girisDosyasi, cikisKlasoru + "kesilmis_video_20-30sn.mp4", 20, 30);
        System.out.println("----------------------------------------");

        // --- 2. KESİM: 34-55 saniye arası ---
        kesmeIslemiYap(ffmpegPath, girisDosyasi, cikisKlasoru + "kesilmis_video_34-55sn.mp4", 34, 55);
        System.out.println("----------------------------------------");

        // --- 3. KESİM: 60-70 saniye arası ---
        kesmeIslemiYap(ffmpegPath, girisDosyasi, cikisKlasoru + "kesilmis_video_60-70sn.mp4", 60, 70);

        System.out.println("\nTüm kesme işlemleri başarıyla tamamlandı.");
    }

    /**
     * Belirtilen videoyu, verilen başlangıç ve bitiş saniyelerine göre keser.
     * Bu metot, kod tekrarını önlemek için kullanılır.
     *
     * @param ffmpegPath        FFmpeg.exe'nin tam yolu.
     * @param girisDosyasi      Kesilecek orijinal video.
     * @param cikisDosyasi      Kesilmiş videonun kaydedileceği yer.
     * @param baslangicSaniyesi Kesimin başlayacağı saniye.
     * @param bitisSaniyesi     Kesimin biteceği saniye.
     */
    public static void kesmeIslemiYap(String ffmpegPath, String girisDosyasi, String cikisDosyasi, long baslangicSaniyesi, long bitisSaniyesi) {
        long kesimSuresi = bitisSaniyesi - baslangicSaniyesi;

        System.out.printf("İşlem: %d. ve %d. saniyeler arası kesiliyor...%n", baslangicSaniyesi, bitisSaniyesi);
        System.out.println("Kaynak: " + girisDosyasi);

        try {
            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(girisDosyasi)
                    .overrideOutputFiles(true)
                    .addOutput(cikisDosyasi)
                    .setStartOffset(baslangicSaniyesi, TimeUnit.SECONDS)
                    .setDuration(kesimSuresi, TimeUnit.SECONDS)
                    .setVideoCodec("copy") // Videoyu yeniden kodlamadan kopyala (çok daha hızlı)
                    .setAudioCodec("copy") // Sesi yeniden kodlamadan kopyala
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
            executor.createJob(builder).run();

            System.out.println("✓ Başarıyla kesildi!");
            System.out.println("Çıktı: " + cikisDosyasi);

        } catch (IOException e) {
            System.err.println("HATA: Bu aralık kesilirken bir sorun oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}