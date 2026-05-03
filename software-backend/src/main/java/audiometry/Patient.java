package audiometry;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Odyometri testine tabi tutulan hastayı temsil eden immutable veri sınıfı.
 *
 * <p>Tasarım ilkeleri:
 * <ul>
 *   <li>Tüm alanlar {@code final} → thread-safe, yan etki yok.</li>
 *   <li>Threshold listesi dışarıdan değiştirilemez ({@code Collections.unmodifiableList}).</li>
 *   <li>Yeni threshold eklemek için {@link #withThreshold(Threshold)} builder
 *       metodu yeni bir {@code Patient} nesnesi döndürür (immutable pattern).</li>
 * </ul>
 */
public final class Patient {

    private final String id;
    private final String fullName;
    private final LocalDate birthDate;
    private final List<Threshold> thresholds;

    /**
     * Tam parametreli constructor (threshold listesi henüz yokken kullanılır).
     *
     * @param id       hasta kimlik numarası (null/boş olamaz)
     * @param fullName hastanın tam adı (null/boş olamaz)
     * @param birthDate doğum tarihi (null olamaz)
     */
    public Patient(String id, String fullName, LocalDate birthDate) {
        this(id, fullName, birthDate, Collections.emptyList());
    }

    /**
     * Threshold listesiyle birlikte constructor.
     *
     * @param id        hasta kimlik numarası (null/boş olamaz)
     * @param fullName  hastanın tam adı (null/boş olamaz)
     * @param birthDate doğum tarihi (null olamaz)
     * @param thresholds ölçülmüş eşik listesi (null olamaz; boş olabilir)
     * @throws NullPointerException     herhangi bir parametre null ise
     * @throws IllegalArgumentException {@code id} veya {@code fullName} boş ise
     */
    public Patient(String id, String fullName, LocalDate birthDate, List<Threshold> thresholds) {
        Objects.requireNonNull(id,         "id null olamaz");
        Objects.requireNonNull(fullName,   "fullName null olamaz");
        Objects.requireNonNull(birthDate,  "birthDate null olamaz");
        Objects.requireNonNull(thresholds, "thresholds null olamaz");

        if (id.isBlank())       throw new IllegalArgumentException("id boş olamaz");
        if (fullName.isBlank()) throw new IllegalArgumentException("fullName boş olamaz");

        this.id         = id.trim();
        this.fullName   = fullName.trim();
        this.birthDate  = birthDate;
        this.thresholds = Collections.unmodifiableList(List.copyOf(thresholds));
    }

    // -----------------------------------------------------------------------
    // Erişimciler (getter)
    // -----------------------------------------------------------------------

    /** @return hasta kimlik numarası */
    public String getId()              { return id;         }

    /** @return hastanın tam adı */
    public String getFullName()        { return fullName;   }

    /** @return doğum tarihi */
    public LocalDate getBirthDate()    { return birthDate;  }

    /**
     * Ölçülmüş eşik listesini döndürür.
     *
     * @return değiştirilemez threshold listesi
     */
    public List<Threshold> getThresholds() { return thresholds; }

    // -----------------------------------------------------------------------
    // Fonksiyonel yardımcı metodlar (pure)
    // -----------------------------------------------------------------------

    /**
     * Verilen eşiği eklenmiş yeni bir {@link Patient} nesnesi döndürür.
     * Orijinal nesne değişmez (immutable pattern).
     *
     * @param threshold eklenecek eşik (null olamaz)
     * @return yeni threshold içeren yeni {@link Patient}
     */
    public Patient withThreshold(Threshold threshold) {
        Objects.requireNonNull(threshold, "threshold null olamaz");
        List<Threshold> updated = new java.util.ArrayList<>(this.thresholds);
        updated.add(threshold);
        return new Patient(id, fullName, birthDate, updated);
    }

    /**
     * Belirli bir frekans ve kulak tarafı için kaydedilmiş eşiği döndürür.
     *
     * @param frequency aranacak frekans
     * @param ear       aranacak kulak tarafı
     * @return eşik bulunursa {@link Optional} içinde, bulunamazsa {@link Optional#empty()}
     */
    public Optional<Threshold> getThresholdFor(Frequency frequency, Threshold.Ear ear) {
        return thresholds.stream()
                .filter(t -> t.getFrequency() == frequency && t.getEar() == ear)
                .findFirst();
    }

    /**
     * Hastanın yaşını hesaplar.
     *
     * @return bugün itibarıyla yaş (tam yıl)
     */
    public int getAge() {
        return birthDate.until(LocalDate.now()).getYears();
    }

    /**
     * Tüm eşiklerin klinik olarak geçerli olup olmadığını kontrol eder.
     *
     * @return tüm eşikler geçerliyse {@code true}; liste boşsa veya
     *         herhangi bir geçersiz eşik varsa {@code false}
     */
    public boolean allThresholdsValid() {
        if (thresholds.isEmpty()) return false;
        return thresholds.stream().allMatch(Threshold::isValid);
    }

    // -----------------------------------------------------------------------
    // equals / hashCode / toString
    // -----------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient other)) return false;
        return id.equals(other.id);   // ID yeterli – klinik bağlamda benzersiz
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", birthDate=" + birthDate +
                ", thresholds=" + thresholds.size() + " kayıt" +
                '}';
    }
}
