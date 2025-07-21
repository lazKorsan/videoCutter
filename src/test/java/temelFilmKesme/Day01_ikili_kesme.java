package temelFilmKesme;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Day01_ikili_kesme {

    public static void main(String[] args) {
        // --- GENEL AYARLAR ---
        String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";
        String girisDosyasi = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\kesilecek.mp4";
        String cikisKlasoru = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\";

        System.out.println("Birden fazla kesme işlemi başlıyor...");

        // --- 1. KESİM: 10-20 saniye arası ---
        String cikisDosyasi1 = cikisKlasoru + "kesilmis_video_10-20sn.mp4";
        kesmeIslemiYap(ffmpegPath, girisDosyasi, cikisDosyasi1, 10, 20);

        System.out.println("----------------------------------------");

        // --- 2. KESİM: 35-55 saniye arası ---
        String cikisDosyasi2 = cikisKlasoru + "kesilmis_video_35-55sn.mp4";
        kesmeIslemiYap(ffmpegPath, girisDosyasi, cikisDosyasi2, 35, 55);

        System.out.println("\nTüm kesme işlemleri tamamlandı.");
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