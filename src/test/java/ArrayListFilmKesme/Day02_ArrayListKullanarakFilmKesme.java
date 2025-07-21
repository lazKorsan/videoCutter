package ArrayListFilmKesme;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Day02_ArrayListKullanarakFilmKesme {

    /**
     * Kesim aralıklarını (başlangıç ve bitiş saniyeleri) tutmak için kullanılan
     * basit bir veri taşıma sınıfı (record). Kodun okunabilirliğini artırır.
     */
    public record KesimAraligi(long baslangicSaniyesi, long bitisSaniyesi) {}

    public static void main(String[] args) {
        // --- GENEL AYARLAR ---
        String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";
        String girisDosyasi = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\kesilecek.mp4";
        String cikisKlasoru = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\";

        // --- KESİLECEK ARALIKLAR LİSTESİ ---
        // Yeni bir kesim eklemek için bu listeye yeni bir "KesimAraligi" eklemeniz yeterli!
        List<KesimAraligi> kesimListesi = new ArrayList<>();
        kesimListesi.add(new KesimAraligi(10, 20)); // 1. kesim: 10-20 saniye
        kesimListesi.add(new KesimAraligi(25, 35)); // 2. kesim: 25-35 saniye
        kesimListesi.add(new KesimAraligi(40, 60)); // 3. kesim: 40-60 saniye

        System.out.println("Liste tabanlı kesme işlemi başlıyor...");
        System.out.println("Toplam " + kesimListesi.size() + " adet parça kesilecek.");
        System.out.println("----------------------------------------");

        // --- LİSTEYİ DÖNGÜ İLE İŞLEME ---
        for (KesimAraligi aralik : kesimListesi) {
            // Her aralık için dinamik bir çıktı dosyası adı oluştur
            String cikisDosyasi = String.format("%skesilmis_video_%d-%dsn.mp4",
                    cikisKlasoru,
                    aralik.baslangicSaniyesi(),
                    aralik.bitisSaniyesi());

            // Kesme işlemini yapan metodu çağır
            kesmeIslemiYap(ffmpegPath, girisDosyasi, cikisDosyasi, aralik.baslangicSaniyesi(), aralik.bitisSaniyesi());
            System.out.println("----------------------------------------");
        }

        System.out.println("\nTüm kesme işlemleri başarıyla tamamlandı.");
    }

    /**
     * Belirtilen videoyu, verilen başlangıç ve bitiş saniyelerine göre keser.
     * Bu metot, önceki dosyalardaki ile aynıdır ve kod tekrarını önler.
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
                    .setVideoCodec("copy")
                    .setAudioCodec("copy")
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