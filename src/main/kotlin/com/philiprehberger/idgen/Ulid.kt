package com.philiprehberger.idgen

import java.security.SecureRandom

/**
 * Internal ULID (Universally Unique Lexicographically Sortable Identifier) implementation.
 *
 * Generates 128-bit identifiers consisting of a 48-bit millisecond timestamp
 * and 80 bits of cryptographic randomness, encoded as 26 Crockford Base32 characters.
 * Monotonic within the same millisecond to preserve sort order.
 */
internal object Ulid {

    private const val ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    private const val ENCODED_LENGTH = 26
    private const val TIMESTAMP_LENGTH = 10
    private const val RANDOM_LENGTH = 16

    private val random = SecureRandom()

    @Volatile
    private var lastTimestamp: Long = -1L

    @Volatile
    private var lastRandomHigh: Long = 0L

    @Volatile
    private var lastRandomLow: Long = 0L

    /**
     * Generates a new ULID string.
     *
     * If called multiple times within the same millisecond, the random component
     * is incremented monotonically to ensure lexicographic sort order.
     *
     * @return a 26-character Crockford Base32 encoded ULID
     * @throws IllegalStateException if the random component overflows within a millisecond
     */
    @Synchronized
    fun generate(): String {
        val now = System.currentTimeMillis()

        if (now == lastTimestamp) {
            // Monotonic increment within same millisecond
            lastRandomLow++
            if (lastRandomLow == 0L) {
                lastRandomHigh++
                if (lastRandomHigh and 0xFFFFL == 0L) {
                    throw IllegalStateException("ULID random component overflow within millisecond")
                }
            }
        } else {
            lastTimestamp = now
            val bytes = ByteArray(10)
            random.nextBytes(bytes)
            lastRandomHigh = ((bytes[0].toLong() and 0xFF) shl 8) or (bytes[1].toLong() and 0xFF)
            lastRandomLow = 0L
            for (i in 2..9) {
                lastRandomLow = (lastRandomLow shl 8) or (bytes[i].toLong() and 0xFF)
            }
        }

        return encodeTimestamp(now) + encodeRandom(lastRandomHigh, lastRandomLow)
    }

    private fun encodeTimestamp(timestamp: Long): String {
        val chars = CharArray(TIMESTAMP_LENGTH)
        var t = timestamp
        for (i in TIMESTAMP_LENGTH - 1 downTo 0) {
            chars[i] = ENCODING[(t and 0x1F).toInt()]
            t = t shr 5
        }
        return String(chars)
    }

    private fun encodeRandom(high: Long, low: Long): String {
        val chars = CharArray(RANDOM_LENGTH)
        // Encode low 64 bits (13 characters, 65 bits - we use lower 64)
        var v = low
        for (i in RANDOM_LENGTH - 1 downTo 3) {
            chars[i] = ENCODING[(v and 0x1F).toInt()]
            v = v shr 5
        }
        // Encode high 16 bits (3 characters)
        var h = high
        for (i in 2 downTo 0) {
            chars[i] = ENCODING[(h and 0x1F).toInt()]
            h = h shr 5
        }
        return String(chars)
    }
}
