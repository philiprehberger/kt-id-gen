package com.philiprehberger.idgen

import java.security.SecureRandom

/**
 * Internal NanoID implementation.
 *
 * Generates compact, URL-friendly unique identifiers using cryptographic randomness.
 * Uses a bitmask technique to ensure uniform distribution across any alphabet.
 */
internal object NanoId {

    private val random = SecureRandom()

    /** Default URL-safe alphabet: `A-Za-z0-9_-` */
    const val DEFAULT_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-"

    /**
     * Generates a NanoID string.
     *
     * @param size the length of the generated ID
     * @param alphabet the character set to use
     * @return a random string of the given [size] using characters from [alphabet]
     * @throws IllegalArgumentException if [size] is not positive or [alphabet] is empty or exceeds 255 characters
     */
    fun generate(size: Int = 21, alphabet: String = DEFAULT_ALPHABET): String {
        require(size > 0) { "Size must be positive" }
        require(alphabet.isNotEmpty()) { "Alphabet must not be empty" }
        require(alphabet.length <= 255) { "Alphabet must not exceed 255 characters" }

        val mask = (2 shl (31 - Integer.numberOfLeadingZeros(alphabet.length - 1 or 1))) - 1
        val step = Math.ceil(1.6 * mask * size / alphabet.length).toInt()

        val result = StringBuilder(size)
        val bytes = ByteArray(step)

        while (result.length < size) {
            random.nextBytes(bytes)
            for (i in 0 until step) {
                val index = bytes[i].toInt() and mask
                if (index < alphabet.length) {
                    result.append(alphabet[index])
                    if (result.length == size) break
                }
            }
        }

        return result.toString()
    }
}
