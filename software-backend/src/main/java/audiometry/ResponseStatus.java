package audiometry;

/**
 * Hastanın uyaran sonrası verdiği yanıtın geçerlilik durumunu temsil eden enum.
 *
 * POSITIVE : Yanıt zaman penceresinde alındı → klinik olarak geçerli.
 * NEGATIVE : Yanıt hiç gelmedi ya da zaman penceresi doldu.
 * INVALID  : Yanıt çok erken geldi (< 200 ms) → refleksif, geçersiz.
 */
public enum ResponseStatus {
    POSITIVE,
    NEGATIVE,
    INVALID
}
