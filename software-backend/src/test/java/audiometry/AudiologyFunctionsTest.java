package audiometry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AudiologyFunctions için birim test paketi.
 *
 * Çalıştırmak için: javac + JUnit 5 (Jupiter) veya Maven/Gradle gereklidir.
 * mvn test  ya da  ./gradlew test
 */
class AudiologyFunctionsTest {

    // =========================================================================
    // 1. evaluateResponseTime testleri
    // =========================================================================

    @ParameterizedTest(name = "[{index}] stimulusMs={0}, pressMs={1}, window={2} → {3}")
    @CsvSource({
        "1000, 1500, 2000, POSITIVE",   // 500 ms → geçerli pencere içinde
        "1000, 1100, 2000, INVALID",    // 100 ms < 200 ms → refleksif
        "1000, 3500, 2000, NEGATIVE",   // 2500 ms > pencere → geç yanıt
        "1000,   -1, 2000, NEGATIVE",   // -1 sentinel → hiç basılmadı
        "   0,  200, 2000, POSITIVE"    // Tam alt sınır: 200 ms
    })
    void evaluateResponseTime_variousScenarios(long stimulus, long press, int window, String expected) {
        Optional<ResponseStatus> result = AudiologyFunctions.evaluateResponseTime(stimulus, press, window);
        assertTrue(result.isPresent());
        assertEquals(ResponseStatus.valueOf(expected), result.get());
    }

    @Test
    void evaluateResponseTime_invalidWindowDuration_returnsEmpty() {
        // Pencere 0 → geçersiz parametre
        Optional<ResponseStatus> result =
                AudiologyFunctions.evaluateResponseTime(1000L, 1500L, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    void evaluateResponseTime_defaultWindow_works() {
        // 2-argümanlı overload (varsayılan 2000 ms)
        Optional<ResponseStatus> result =
                AudiologyFunctions.evaluateResponseTime(0L, 800L);
        assertTrue(result.isPresent());
        assertEquals(ResponseStatus.POSITIVE, result.get());
    }

    // =========================================================================
    // 2. calculateNextStep testleri
    // =========================================================================

    @ParameterizedTest(name = "[{index}] db={0}, heard={1} → {2}")
    @CsvSource({
        "50,  true,  40",   // duydu  → 50 - 10 = 40
        "50,  false, 55",   // duymadı → 50 + 5 = 55
        " 5,  true,   0",   // alt kırpma: 5 - 10 = -5 → 0
        "98,  false, 100"   // üst kırpma: 98 + 5 = 103 → 100
    })
    void calculateNextStep_variousInputs(int db, boolean heard, int expected) {
        assertEquals(expected, AudiologyFunctions.calculateNextStep(db, heard));
    }

    @ParameterizedTest(name = "[{index}] geçersiz db={0} → IllegalArgumentException")
    @CsvSource({
        "-5,  true",
        "110, false"
    })
    void calculateNextStep_outOfRangeInput_throwsException(int db, boolean heard) {
        assertThrows(IllegalArgumentException.class,
                () -> AudiologyFunctions.calculateNextStep(db, heard));
    }

    // =========================================================================
    // 3. isThresholdReached testleri
    // =========================================================================

    @Test
    void isThresholdReached_twoOfThree_returnsTrue() {
        assertTrue(AudiologyFunctions.isThresholdReached(List.of(true, false, true)));
    }

    @Test
    void isThresholdReached_threeOfFive_returnsFalse() {
        // 3/5 = 0.60 < 0.667 → eşik YOK
        assertFalse(AudiologyFunctions.isThresholdReached(List.of(true, false, true, false, true)));
    }

    @Test
    void isThresholdReached_threeOfFour_returnsTrue() {
        // 3/4 = 0.75 ≥ 2/3 → eşiğe ulaşıldı
        assertTrue(AudiologyFunctions.isThresholdReached(List.of(true, true, false, true)));
    }

    @Test
    void isThresholdReached_insufficientTrials_returnsFalse() {
        assertFalse(AudiologyFunctions.isThresholdReached(List.of(true, true)));
    }

    @Test
    void isThresholdReached_nullHistory_returnsFalse() {
        assertFalse(AudiologyFunctions.isThresholdReached(null));
    }

    @Test
    void isThresholdReached_allNegative_returnsFalse() {
        assertFalse(AudiologyFunctions.isThresholdReached(List.of(false, false, false)));
    }

    // =========================================================================
    // 4. getTestParameters testleri
    // =========================================================================

    @Test
    void getTestParameters_returnsCorrectFrequencies() {
        TestConfig config = AudiologyFunctions.getTestParameters();
        assertEquals(List.of(250, 500, 1000, 2000, 4000, 8000), config.getFrequencies());
    }

    @Test
    void getTestParameters_startsWith1000Hz() {
        TestConfig config = AudiologyFunctions.getTestParameters();
        assertEquals(1000, config.getStartFrequencyHz());
    }

    @Test
    void getTestParameters_startIntensityInRange() {
        TestConfig config = AudiologyFunctions.getTestParameters();
        int intensity = config.getStartIntensityDb();
        assertTrue(intensity >= 30 && intensity <= 40,
                "Başlangıç şiddeti 30–40 dB arasında olmalı, alınan: " + intensity);
    }

    @Test
    void getTestParameters_waveformIsPureSine() {
        TestConfig config = AudiologyFunctions.getTestParameters();
        assertEquals("PURE_SINE", config.getWaveform());
    }

    @Test
    void getTestParameters_frequencyListIsImmutable() {
        TestConfig config = AudiologyFunctions.getTestParameters();
        assertThrows(UnsupportedOperationException.class,
                () -> config.getFrequencies().add(3000));
    }

    @ParameterizedTest(name = "[{index}] freq={0} → izin verildi={1}")
    @CsvSource({
        "1000, true",
        "4000, true",
        "3000, false",
        "12000, false"
    })
    void isAllowedFrequency_variousValues(int freq, boolean expected) {
        assertEquals(expected, AudiologyFunctions.isAllowedFrequency(freq));
    }

    // =========================================================================
    // 5. formatResponseToMap testleri
    // =========================================================================

    @Test
    void formatResponseToMap_validInput_parsesCorrectly() {
        String raw = "RESPONSE|freq=1000;intensity=40;pressed=true";
        Optional<Map<String, String>> result = AudiologyFunctions.formatResponseToMap(raw);

        assertTrue(result.isPresent());
        Map<String, String> map = result.get();
        assertEquals("1000", map.get("freq"));
        assertEquals("40",   map.get("intensity"));
        assertEquals("true", map.get("pressed"));
    }

    @ParameterizedTest(name = "[{index}] girdi=\"{0}\" → boş sonuç")
    @CsvSource(value = {
        "NULL",           // null girdi
        "   ",            // boşluk
        "STATUS|freq=1000" // tanımsız prefix
    }, nullValues = "NULL")
    void formatResponseToMap_invalidInputs_returnsEmpty(String raw) {
        assertTrue(AudiologyFunctions.formatResponseToMap(raw).isEmpty());
    }

    @Test
    void formatResponseToMap_malformedPairs_filteredGracefully() {
        String raw = "RESPONSE|freq=1000;badtoken;intensity=40";
        Optional<Map<String, String>> result = AudiologyFunctions.formatResponseToMap(raw);

        assertTrue(result.isPresent());
        assertEquals("1000", result.get().get("freq"));
        assertEquals("40",   result.get().get("intensity"));
        assertNull(result.get().get("badtoken"));
    }

    @Test
    void formatResponseToMap_resultIsUnmodifiable() {
        String raw = "RESPONSE|freq=1000";
        Map<String, String> map = AudiologyFunctions.formatResponseToMap(raw).orElseThrow();
        assertThrows(UnsupportedOperationException.class,
                () -> map.put("newKey", "newValue"));
    }
}
