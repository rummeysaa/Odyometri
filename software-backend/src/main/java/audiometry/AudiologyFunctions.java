package audiometry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Odyometri yazılım katmanı için saf (pure) fonksiyon koleksiyonu.
 *
 * <p>Tasarım ilkeleri:
 * <ul>
 *   <li>Tüm metodlar {@code static} ve yan etkisizdir (pure functions).</li>
 *   <li>Dış durum okunmaz, dış durum değiştirilmez.</li>
 *   <li>Hatalı girdiler {@link Optional#empty()} veya doğrudan enum değerleriyle
 *       işaret edilir; exception fırlatılmaz.</li>
 * </ul>
 *
 * Standart: IEC 60645-1 / Hughson-Westlake prosedürü
 */
public final class AudiologyFunctions {

    // -----------------------------------------------------------------------
    // Sabitler
    // -----------------------------------------------------------------------

    /** Refleksif yanıt sınırı (ms). Bu sürenin altındaki yanıtlar geçersizdir. */
    private static final int EARLY_RESPONSE_THRESHOLD_MS = 200;

    /** İzin verilen minimum çıkış şiddeti (dB). */
    private static final int MIN_INTENSITY_DB = 0;

    /** İzin verilen maksimum çıkış şiddeti (dB). */
    private static final int MAX_INTENSITY_DB = 100;

    /** "Duydu" durumunda uygulanacak adım (dB azalt). */
    private static final int DOWN_STEP_DB = 10;

    /** "Duymadı" durumunda uygulanacak adım (dB artır). */
    private static final int UP_STEP_DB = 5;

    /**
     * IEC 60645-1'e göre izin verilen odyometri frekansları (Hz).
     * Liste değiştirilemez (immutable).
     */
    private static final List<Integer> ALLOWED_FREQUENCIES =
            List.of(250, 500, 1000, 2000, 4000, 8000);

    /** Eşik doğrulama için gereken minimum deneme sayısı (2/3 kuralı). */
    private static final int THRESHOLD_MIN_TRIALS = 3;

    /** Eşik doğrulama için gereken minimum pozitif yanıt sayısı (2/3 kuralı). */
    private static final int THRESHOLD_MIN_POSITIVES = 2;

    /** Yapıcı gizlidir; yalnızca statik metodlar sunulur. */
    private AudiologyFunctions() {}

    // -----------------------------------------------------------------------
    // 1. evaluateResponseTime — Zaman Kısıt Analizi
    // -----------------------------------------------------------------------

    /**
     * Hastanın uyaran sonrası verdiği yanıtın klinik geçerliliğini değerlendirir.
     *
     * <p>Karar mantığı:
     * <ol>
     *   <li>{@code buttonPressTime} negatifse (hiç basılmadı → sentinel -1)
     *       veya fark {@code windowDuration}'ı aşıyorsa → {@code NEGATIVE}.</li>
     *   <li>Fark 200 ms'nin altındaysa (refleksif yanıt) → {@code INVALID}.</li>
     *   <li>[200 ms, windowDuration] aralığındaysa → {@code POSITIVE}.</li>
     * </ol>
     *
     * @param stimulusStartTime uyaranın başladığı zaman damgası (ms, epoch)
     * @param buttonPressTime   butonun basıldığı zaman damgası (ms, epoch);
     *                          hiç basılmadıysa {@code -1L} gönderilmeli
     * @param windowDuration    geçerli yanıt penceresi (ms); varsayılan 2000 ms
     * @return {@link ResponseStatus} değerini sarmalayan {@link Optional};
     *         parametre tutarsızlığında {@link Optional#empty()} döner
     */
    public static Optional<ResponseStatus> evaluateResponseTime(
            long stimulusStartTime,
            long buttonPressTime,
            int windowDuration) {

        // Girdi doğrulama: negatif pencere anlamlı değil
        if (windowDuration <= 0) {
            return Optional.empty();
        }

        // Hiç basılmadı (sentinel değer olarak -1 bekleniyor)
        if (buttonPressTime < 0) {
            return Optional.of(ResponseStatus.NEGATIVE);
        }

        long reactionTime = buttonPressTime - stimulusStartTime;

        // Zaman damgası tutarsızlığı (uyarandan önce basılmış)
        if (reactionTime < 0) {
            return Optional.empty();
        }

        // Pencere aşıldı → geç yanıt → NEGATIVE
        if (reactionTime > windowDuration) {
            return Optional.of(ResponseStatus.NEGATIVE);
        }

        // Refleksif yanıt → INVALID
        if (reactionTime < EARLY_RESPONSE_THRESHOLD_MS) {
            return Optional.of(ResponseStatus.INVALID);
        }

        // [200, windowDuration] → POSITIVE
        return Optional.of(ResponseStatus.POSITIVE);
    }

    /**
     * {@link #evaluateResponseTime(long, long, int)} için varsayılan pencere
     * (2000 ms) kullanan aşırı yükleme.
     *
     * @param stimulusStartTime uyaranın başladığı zaman damgası (ms)
     * @param buttonPressTime   butonun basıldığı zaman damgası (ms); -1 → basılmadı
     * @return {@link ResponseStatus} sarmalayan {@link Optional}
     */
    public static Optional<ResponseStatus> evaluateResponseTime(
            long stimulusStartTime,
            long buttonPressTime) {
        return evaluateResponseTime(stimulusStartTime, buttonPressTime, 2000);
    }

    // -----------------------------------------------------------------------
    // 2. calculateNextStep — Hughson-Westlake "10 aşağı / 5 yukarı" Algoritması
    // -----------------------------------------------------------------------

    /**
     * Hughson-Westlake "10 dB aşağı – 5 dB yukarı" protokolünü uygular ve
     * bir sonraki test şiddetini döndürür.
     *
     * <p>Donanım sınırları: sonuç [{@value #MIN_INTENSITY_DB},
     * {@value #MAX_INTENSITY_DB}] dB aralığında kırpılır.
     *
     * @param currentIntensity mevcut ses şiddeti (dB)
     * @param isPositive       hasta sesi duyduysa {@code true},
     *                         duymadıysa {@code false}
     * @return bir sonraki ses şiddeti (dB)
     * @throws IllegalArgumentException {@code currentIntensity} donanım
     *                                  sınırları dışındaysa
     */
    public static int calculateNextStep(int currentIntensity, boolean isPositive) {
        if (currentIntensity < MIN_INTENSITY_DB || currentIntensity > MAX_INTENSITY_DB) {
            throw new IllegalArgumentException(
                    "currentIntensity " + currentIntensity +
                    " dB geçerli aralık [" + MIN_INTENSITY_DB + ", " + MAX_INTENSITY_DB + "] dB dışında.");
        }

        int nextIntensity = isPositive
                ? currentIntensity - DOWN_STEP_DB   // Duydu → 10 dB azalt
                : currentIntensity + UP_STEP_DB;    // Duymadı → 5 dB artır

        // Donanım sınırlarına kırp
        return Math.max(MIN_INTENSITY_DB, Math.min(MAX_INTENSITY_DB, nextIntensity));
    }

    // -----------------------------------------------------------------------
    // 3. isThresholdReached — Eşik Değeri Doğrulama (2/3 Kuralı)
    // -----------------------------------------------------------------------

    /**
     * Belirli bir şiddet için eşiğin bulunup bulunmadığını doğrular.
     *
     * <p>Kural: Ascending (yukarı çıkış) fazında aynı dB seviyesinde yapılan
     * denemelerin en az {@value #THRESHOLD_MIN_POSITIVES}/{@value #THRESHOLD_MIN_TRIALS}'ünde
     * (2/3) pozitif yanıt alınmış olmalıdır.
     *
     * <p>Pure function garantisi: yalnızca {@code ascendingHistory} üzerinde
     * {@code filter} ve {@code count} işlemleri yapılır; dış değişken okunmaz.
     *
     * @param ascendingHistory yalnızca ascending (artan) fazdaki yanıt listesi;
     *                         {@code true} = pozitif yanıt, {@code false} = negatif yanıt
     * @return eşiğe ulaşıldıysa {@code true}, henüz ulaşılmadıysa {@code false}
     */
    public static boolean isThresholdReached(List<Boolean> ascendingHistory) {
        if (ascendingHistory == null || ascendingHistory.size() < THRESHOLD_MIN_TRIALS) {
            return false;
        }

        long positiveCount = ascendingHistory.stream()
                .filter(response -> response)   // sadece pozitif yanıtları say
                .count();

        // 2/3 kuralı: en az 2 pozitif / toplam deneme sayısı ≥ 2/3
        return positiveCount >= THRESHOLD_MIN_POSITIVES &&
               (double) positiveCount / ascendingHistory.size() >= (2.0 / 3.0);
    }

    // -----------------------------------------------------------------------
    // 4. getTestParameters — IEC 60645-1 Konfigürasyon Yönetimi
    // -----------------------------------------------------------------------

    /**
     * IEC 60645-1 standartlarına uygun test konfigürasyonunu döndürür.
     *
     * <p>Garanti edilen özellikler:
     * <ul>
     *   <li>Frekans listesi değiştirilemez ({@code List.copyOf}).</li>
     *   <li>Test her zaman 1000 Hz / 35 dB'den başlar (familiarization).</li>
     *   <li>Waveform sabit olarak "PURE_SINE" dir.</li>
     * </ul>
     *
     * @return test konfigürasyonunu içeren {@link TestConfig} nesnesi
     */
    public static TestConfig getTestParameters() {
        return new TestConfig(
                List.copyOf(ALLOWED_FREQUENCIES),   // immutable frekans listesi
                1000,                               // başlangıç frekansı (Hz) – familiarization
                35,                                 // başlangıç şiddeti (dB) – duyulabilir seviye
                "PURE_SINE"                         // sinyal biçimi
        );
    }

    /**
     * Verilen frekansın IEC 60645-1 standartlarına uygun olup olmadığını kontrol eder.
     *
     * @param frequencyHz test edilecek frekans (Hz)
     * @return izin verilen bir frekans ise {@code true}
     */
    public static boolean isAllowedFrequency(int frequencyHz) {
        return ALLOWED_FREQUENCIES.contains(frequencyHz);
    }

    // -----------------------------------------------------------------------
    // 5. formatResponseToMap — Seri Port Veri İşleme Zinciri
    // -----------------------------------------------------------------------

    /**
     * Arduino'dan gelen ham seri port mesajını ayrıştırarak anlamlı bir
     * {@code Map<String, String>} yapısına dönüştürür.
     *
     * <p>Beklenen format: {@code "RESPONSE|KEY1=VALUE1;KEY2=VALUE2;..."}
     * <br>Örnek: {@code "RESPONSE|freq=1000;intensity=40;pressed=true"}
     *
     * <p>Pure function garantileri:
     * <ul>
     *   <li>Hatalı / null girdide {@link Optional#empty()} döner; exception yok.</li>
     *   <li>Sistem akışı bozulmaz; yan etki yoktur.</li>
     *   <li>{@code filter} → {@code map} → {@code collect} zinciri kullanılır.</li>
     * </ul>
     *
     * @param rawSerialData Arduino'dan gelen ham seri port string'i
     * @return başarıyla ayrıştırılmışsa key-value haritasını saran {@link Optional},
     *         format hatası veya null girdi durumunda {@link Optional#empty()}
     */
    public static Optional<Map<String, String>> formatResponseToMap(String rawSerialData) {
        // null veya boş girdi koruması
        if (rawSerialData == null || rawSerialData.isBlank()) {
            return Optional.empty();
        }

        String trimmed = rawSerialData.trim();

        // "RESPONSE|" öneki zorunlu
        if (!trimmed.startsWith("RESPONSE|")) {
            return Optional.empty();
        }

        String payload = trimmed.substring("RESPONSE|".length());

        // Yük bölümü boşsa geçersiz
        if (payload.isBlank()) {
            return Optional.empty();
        }

        // "KEY=VALUE" çiftlerini ayır, hatalı olanları filtrele, Map'e topla
        Map<String, String> result = Arrays.stream(payload.split(";"))
                .filter(pair -> pair.contains("="))                 // '=' içermeyen token'ları ele
                .map(pair -> pair.split("=", 2))                   // en fazla 2 parçaya böl
                .filter(parts -> parts.length == 2                 // iki parça şart
                        && !parts[0].isBlank()                     // anahtar boş olamaz
                        && !parts[1].isBlank())                    // değer boş olamaz
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        parts -> parts[0].trim(),                  // key
                        parts -> parts[1].trim(),                  // value
                        (existing, duplicate) -> existing));       // duplicate key → ilkini koru

        // Hiçbir geçerli çift çıkmadıysa empty döndür
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
}
