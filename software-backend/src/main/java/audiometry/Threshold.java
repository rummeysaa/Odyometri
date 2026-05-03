package audiometry;

import java.util.Objects;
import java.util.Optional;

/**
 * Belirli bir frekans için ölçülen işitme eşiği (hearing threshold).
 *
 * <p>Immutable record tasarımı: tüm alanlar {@code final} ve yalnızca
 * constructor üzerinden atanır. Pure-function pipeline'larında güvenle
 * kullanılabilir (yan etki yok).
 *
 * <p>Standart: IEC 60645-1 – eşik dB HL cinsinden ifade edilir.
 *
 * @param frequency   test edilen frekans (null olamaz)
 * @param thresholdDb ölçülen eşik değeri (dB HL); geçerli aralık [0, 120]
 * @param ear         test edilen kulak ({@link Ear})
 * @param isValid     eşiğin klinik olarak geçerli olup olmadığı
 */
public final class Threshold {

    /** Minimum geçerli eşik değeri (dB HL). */
    public static final int MIN_THRESHOLD_DB = 0;

    /** Maksimum geçerli eşik değeri (dB HL). */
    public static final int MAX_THRESHOLD_DB = 120;

    private final Frequency frequency;
    private final int thresholdDb;
    private final Ear ear;
    private final boolean isValid;

    /**
     * Tam parametreli constructor.
     *
     * @param frequency   test frekansı (null olamaz)
     * @param thresholdDb ölçülen eşik (dB HL)
     * @param ear         test edilen kulak (null olamaz)
     * @param isValid     klinik geçerlilik bayrağı
     * @throws NullPointerException     {@code frequency} veya {@code ear} null ise
     * @throws IllegalArgumentException {@code thresholdDb} [{@value #MIN_THRESHOLD_DB},
     *                                  {@value #MAX_THRESHOLD_DB}] dışındaysa
     */
    public Threshold(Frequency frequency, int thresholdDb, Ear ear, boolean isValid) {
        this.frequency   = Objects.requireNonNull(frequency, "frequency null olamaz");
        this.ear         = Objects.requireNonNull(ear,       "ear null olamaz");
        if (thresholdDb < MIN_THRESHOLD_DB || thresholdDb > MAX_THRESHOLD_DB) {
            throw new IllegalArgumentException(
                    "thresholdDb " + thresholdDb + " dB geçerli aralık [" +
                    MIN_THRESHOLD_DB + ", " + MAX_THRESHOLD_DB + "] dışında.");
        }
        this.thresholdDb = thresholdDb;
        this.isValid     = isValid;
    }

    // -----------------------------------------------------------------------
    // Erişimciler (getter)
    // -----------------------------------------------------------------------

    /** @return test frekansı */
    public Frequency getFrequency()  { return frequency;   }

    /** @return ölçülen eşik değeri (dB HL) */
    public int getThresholdDb()      { return thresholdDb; }

    /** @return test edilen kulak */
    public Ear getEar()              { return ear;         }

    /** @return klinik geçerlilik bayrağı */
    public boolean isValid()         { return isValid;     }

    // -----------------------------------------------------------------------
    // İşitme kaybı sınıflandırması (pure helper)
    // -----------------------------------------------------------------------

    /**
     * WHO işitme kaybı sınıflandırmasına (2021) göre kaybın derecesini döndürür.
     *
     * <ul>
     *   <li>0–25 dB → Normal</li>
     *   <li>26–40 dB → Hafif (Mild)</li>
     *   <li>41–60 dB → Orta (Moderate)</li>
     *   <li>61–80 dB → Şiddetli (Severe)</li>
     *   <li>81+ dB → Çok şiddetli (Profound)</li>
     * </ul>
     *
     * @return kaybı tanımlayan String; eşik geçersizse {@link Optional#empty()}
     */
    public Optional<String> classifyHearingLoss() {
        if (!isValid) {
            return Optional.empty();
        }
        if (thresholdDb <= 25)  return Optional.of("Normal");
        if (thresholdDb <= 40)  return Optional.of("Hafif (Mild)");
        if (thresholdDb <= 60)  return Optional.of("Orta (Moderate)");
        if (thresholdDb <= 80)  return Optional.of("Şiddetli (Severe)");
        return Optional.of("Çok Şiddetli (Profound)");
    }

    // -----------------------------------------------------------------------
    // equals / hashCode / toString
    // -----------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Threshold other)) return false;
        return thresholdDb == other.thresholdDb
                && isValid == other.isValid
                && frequency == other.frequency
                && ear == other.ear;
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequency, thresholdDb, ear, isValid);
    }

    @Override
    public String toString() {
        return "Threshold{" +
                "frequency=" + frequency +
                ", thresholdDb=" + thresholdDb + " dB HL" +
                ", ear=" + ear +
                ", isValid=" + isValid +
                '}';
    }

    // -----------------------------------------------------------------------
    // İç tip: Ear (kulak tarafı)
    // -----------------------------------------------------------------------

    /**
     * Test edilen kulak tarafını temsil eden enum.
     * Audiogramda sol (OS) ve sağ (OD) kulak ayrı ayrı işaretlenir.
     */
    public enum Ear {
        /** Sağ kulak (Oculus Dexter). */
        RIGHT,
        /** Sol kulak (Oculus Sinister). */
        LEFT
    }
}
