package TxtDosyasindanFilmKesme;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Day01_TXT_dosyasindanFilmKesme {

    /**
     * Kesim aralıklarını (başlangıç ve bitiş saniyeleri) tutmak için kullanılan
     * basit bir veri taşıma sınıfı (record).
     */
    public record KesimAraligi(long baslangicSaniyesi, long bitisSaniyesi) {}

    public static void main(String[] args) {
        // --- GENEL AYARLAR ---
        // txt dosyasinin adı araliklar.txt
        String ffmpegPath = "C:\\ffmpeg\\bin\\ffmpeg.exe";
        String girisDosyasi = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\kesilecek.mp4";
        String cikisKlasoru = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\";
        // Okunacak TXT dosyasının yolu
        String txtDosyaYolu = "C:\\Users\\Hp\\OneDrive\\Desktop\\Film_Kesme\\araliklar.txt";

        System.out.println("TXT dosyasından kesim aralıkları okunuyor...");

        // 1. TXT dosyasını oku ve kesim listesini oluştur
        List<KesimAraligi> kesimListesi = txtDosyasindanAraliklariOku(txtDosyaYolu);

        if (kesimListesi.isEmpty()) {
            System.out.println("TXT dosyasında geçerli bir kesim aralığı bulunamadı veya dosya boş.");
            return; // Programı sonlandır
        }

        System.out.println("Toplam " + kesimListesi.size() + " adet parça kesilecek.");
        System.out.println("----------------------------------------");

        // 2. Listeyi döngü ile işle ve her aralık için kesim yap
        for (KesimAraligi aralik : kesimListesi) {
            String cikisDosyasi = String.format("%skesilmis_video_%d-%dsn.mp4",
                    cikisKlasoru,
                    aralik.baslangicSaniyesi(),
                    aralik.bitisSaniyesi());

            kesmeIslemiYap(ffmpegPath, girisDosyasi, cikisDosyasi, aralik.baslangicSaniyesi(), aralik.bitisSaniyesi());
            System.out.println("----------------------------------------");
        }

        System.out.println("\nTüm kesme işlemleri başarıyla tamamlandı.");
    }

    /**
     * Verilen yoldaki TXT dosyasını satır satır okur, "başlangıç,bitiş" formatındaki
     * verileri ayrıştırır ve bir KesimAraligi listesi olarak döndürür.
     *
     * @param dosyaYolu Okunacak TXT dosyasının tam yolu.
     * @return Kesim aralıklarını içeren bir liste.
     */
    public static List<KesimAraligi> txtDosyasindanAraliklariOku(String dosyaYolu) {
        List<KesimAraligi> araliklar = new ArrayList<>();
        Path path = Paths.get(dosyaYolu);

        if (!Files.exists(path)) {
            System.err.println("HATA: Belirtilen TXT dosyası bulunamadı: " + dosyaYolu);
            return araliklar; // Boş liste döndür
        }

        try {
            List<String> satirlar = Files.readAllLines(path);
            for (String satir : satirlar) {
                satir = satir.trim(); // Satır başı ve sonundaki boşlukları temizle

                // Yorum satırlarını veya boş satırları atla
                if (satir.isEmpty() || satir.startsWith("#")) {
                    continue;
                }

                String[] parcalar = satir.split(",");
                if (parcalar.length == 2) {
                    try {
                        long baslangic = Long.parseLong(parcalar[0].trim());
                        long bitis = Long.parseLong(parcalar[1].trim());
                        araliklar.add(new KesimAraligi(baslangic, bitis));
                    } catch (NumberFormatException e) {
                        System.err.println("UYARI: Geçersiz formatlı satır atlandı: '" + satir + "'");
                    }
                } else {
                    System.err.println("UYARI: Eksik veya hatalı formatlı satır atlandı: '" + satir + "'");
                }
            }
        } catch (IOException e) {
            System.err.println("HATA: TXT dosyası okunurken bir sorun oluştu: " + e.getMessage());
        }

        return araliklar;
    }

    /**
     * Belirtilen videoyu, verilen başlangıç ve bitiş saniyelerine göre keser.
     * Bu metot, önceki dosyalardaki ile aynıdır ve kod tekrarını önler.
     */
    public static void kesmeIslemiYap(String ffmpegPath, String girisDosyasi, String cikisDosyasi, long baslangicSaniyesi, long bitisSaniyesi) {
        long kesimSuresi = bitisSaniyesi - baslangicSaniyesi;
        if (kesimSuresi <= 0) {
            System.err.printf("HATA: Geçersiz aralık (%d-%d), kesim atlandı.%n", baslangicSaniyesi, bitisSaniyesi);
            return;
        }

        System.out.printf("İşlem: %d. ve %d. saniyeler arası kesiliyor...%n", baslangicSaniyesi, bitisSaniyesi);

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

            System.out.println("✓ Başarıyla kesildi! -> " + cikisDosyasi);

        } catch (IOException e) {
            System.err.println("HATA: Bu aralık kesilirken bir sorun oluştu: " + e.getMessage());
        }
    }
}