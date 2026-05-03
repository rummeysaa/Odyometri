package audiometry;

import java.util.List;
import java.util.Objects;

/**
 * IEC 60645-1 standardına göre odyometri testi konfigürasyonu.
 *
 * <p>Bu sınıf tamamen immutable'dır; tüm alanlar final ve dışarıya
 * yalnızca getter ile sunulur.
 */
public final class TestConfig {

    /** IEC 60645-1'e göre test yapılacak frekanslar (Hz), değiştirilemez liste. */
    private final List<Integer> frequencies;

    /** Familiarization için başlangıç frekansı (Hz). */
    private final int startFrequencyHz;

    /** Familiarization için başlangıç şiddeti (dB). */
    private final int startIntensityDb;

    /** Sinyal biçimi – her zaman "PURE_SINE". */
    private final String waveform;

    /**
     * Yeni bir {@code TestConfig} oluşturur.
     *
     * @param frequencies      test frekans listesi (immutable olmalı)
     * @param startFrequencyHz başlangıç frekansı (Hz)
     * @param startIntensityDb başlangıç şiddeti (dB)
     * @param waveform         sinyal biçimi
     */
    public TestConfig(
            List<Integer> frequencies,
            int startFrequencyHz,
            int startIntensityDb,
            String waveform) {
        this.frequencies = List.copyOf(Objects.requireNonNull(frequencies, "frequencies null olamaz"));
        this.startFrequencyHz = startFrequencyHz;
        this.startIntensityDb = startIntensityDb;
        this.waveform = Objects.requireNonNull(waveform, "waveform null olamaz");
    }

    /** @return test yapılacak frekans listesi (değiştirilemez) */
    public List<Integer> getFrequencies() {
        return frequencies;
    }

    /** @return familiarization başlangıç frekansı (Hz) */
    public int getStartFrequencyHz() {
        return startFrequencyHz;
    }

    /** @return familiarization başlangıç şiddeti (dB) */
    public int getStartIntensityDb() {
        return startIntensityDb;
    }

    /** @return sinyal biçimi (örn. "PURE_SINE") */
    public String getWaveform() {
        return waveform;
    }

    @Override
    public String toString() {
        return "TestConfig{" +
                "frequencies=" + frequencies +
                ", startFrequencyHz=" + startFrequencyHz +
                ", startIntensityDb=" + startIntensityDb +
                ", waveform='" + waveform + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestConfig other)) return false;
        return startFrequencyHz == other.startFrequencyHz
                && startIntensityDb == other.startIntensityDb
                && frequencies.equals(other.frequencies)
                && waveform.equals(other.waveform);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequencies, startFrequencyHz, startIntensityDb, waveform);
    }
}
