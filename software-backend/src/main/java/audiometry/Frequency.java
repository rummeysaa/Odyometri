package audiometry;

/**
 * IEC 60645-1 standartlarında tanımlanan odyometri test frekansları.
 *
 * <p>Her sabit, frekansın Hz cinsinden değerini taşır.
 * Enum kullanımı sayesinde geçersiz frekans değerleri derleme zamanında
 * engellenir (pure-function tasarımını destekler).
 *
 * <p>Standart: IEC 60645-1:2017 – Tablo 1 (saf ton frekans adımları)
 */
public enum Frequency {

    HZ_250(250),
    HZ_500(500),
    HZ_1000(1000),
    HZ_2000(2000),
    HZ_4000(4000),
    HZ_8000(8000);

    /** Frekansın Hz cinsinden sayısal değeri. */
    private final int hz;

    Frequency(int hz) {
        this.hz = hz;
    }

    /**
     * Frekansın Hz cinsinden sayısal değerini döndürür.
     *
     * @return frekans (Hz)
     */
    public int getHz() {
        return hz;
    }

    /**
     * Verilen Hz değerine karşılık gelen {@link Frequency} sabitini döndürür.
     *
     * @param hz aranacak frekans değeri
     * @return eşleşen {@link Frequency}
     * @throws IllegalArgumentException geçersiz/desteklenmeyen frekans girilirse
     */
    public static Frequency fromHz(int hz) {
        for (Frequency f : values()) {
            if (f.hz == hz) {
                return f;
            }
        }
        throw new IllegalArgumentException(
                "Desteklenmeyen frekans: " + hz + " Hz. " +
                "IEC 60645-1 kapsamındaki değerler: 250, 500, 1000, 2000, 4000, 8000 Hz.");
    }

    @Override
    public String toString() {
        return hz + " Hz";
    }
}
