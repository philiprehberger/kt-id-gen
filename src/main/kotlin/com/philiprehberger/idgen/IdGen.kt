package com.philiprehberger.idgen

import java.security.SecureRandom
import java.util.UUID

/**
 * Central ID generation facade providing multiple ID formats.
 *
 * All methods are thread-safe and use [SecureRandom] for cryptographic randomness.
 */
public object IdGen {

    private const val BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    /**
     * Generates a ULID (Universally Unique Lexicographically Sortable Identifier).
     *
     * ULIDs are 26-character Crockford Base32 encoded strings with a 48-bit
     * millisecond timestamp prefix, making them naturally sortable by creation time.
     * Monotonic within the same millisecond.
     *
     * @return a 26-character uppercase ULID string
     */
    public fun ulid(): String = Ulid.generate()

    /**
     * Generates a NanoID with configurable size and alphabet.
     *
     * NanoIDs are compact, URL-friendly identifiers using cryptographic randomness
     * with uniform distribution across the given alphabet.
     *
     * @param size the length of the generated ID (default 21)
     * @param alphabet the character set to use (default: URL-safe `A-Za-z0-9_-`)
     * @return a random string of the given [size]
     */
    public fun nanoid(size: Int = 21, alphabet: String = NanoId.DEFAULT_ALPHABET): String =
        NanoId.generate(size, alphabet)

    /**
     * Generates a standard UUID v4 string.
     *
     * @return a UUID string in the format `xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx`
     */
    public fun uuid(): String = UUID.randomUUID().toString()

    /**
     * Generates a prefixed ID by combining a string prefix with a ULID.
     *
     * Useful for creating human-readable, typed identifiers like `usr_01H5...` or `ord_01H5...`.
     *
     * @param prefix the prefix to prepend (e.g. "usr", "ord", "txn")
     * @return a string in the format `{prefix}_{ulid}`
     */
    public fun prefixed(prefix: String): String = "${prefix}_${ulid()}"

    /**
     * Generates a short UUID by encoding a UUID v4 in Base62.
     *
     * Produces a 22-character URL-safe string with the same 128 bits of entropy
     * as a standard UUID, but without hyphens and in a more compact format.
     *
     * @return a 22-character Base62-encoded UUID string
     */
    public fun uuidShort(): String {
        val uuid = UUID.randomUUID()
        return encodeBase62(uuid.mostSignificantBits, uuid.leastSignificantBits)
    }

    private fun encodeBase62(msb: Long, lsb: Long): String {
        // Convert 128-bit UUID to BigInteger then to base62
        val high = msb.toULong()
        val low = lsb.toULong()

        // Use BigInteger for precise base62 conversion
        val bigHigh = java.math.BigInteger.valueOf(msb ushr 32 and 0xFFFFFFFFL)
            .shiftLeft(96)
            .add(java.math.BigInteger.valueOf(msb and 0xFFFFFFFFL).shiftLeft(64))
            .add(java.math.BigInteger.valueOf(lsb ushr 32 and 0xFFFFFFFFL).shiftLeft(32))
            .add(java.math.BigInteger.valueOf(lsb and 0xFFFFFFFFL))

        // Make sure value is positive
        val value = if (bigHigh.signum() < 0) {
            bigHigh.add(java.math.BigInteger.ONE.shiftLeft(128))
        } else {
            bigHigh
        }

        val base = java.math.BigInteger.valueOf(62)
        val chars = CharArray(22)
        var remaining = value
        for (i in 21 downTo 0) {
            val (quotient, remainder) = remaining.divideAndRemainder(base)
            chars[i] = BASE62_ALPHABET[remainder.toInt()]
            remaining = quotient
        }

        return String(chars)
    }
}
