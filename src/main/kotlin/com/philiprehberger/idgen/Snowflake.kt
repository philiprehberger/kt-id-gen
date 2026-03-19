package com.philiprehberger.idgen

/**
 * Snowflake ID generator producing 64-bit unique identifiers.
 *
 * The bit layout is:
 * - 1 bit: unused sign bit
 * - 41 bits: millisecond timestamp (relative to [epoch])
 * - 10 bits: machine ID (0..1023)
 * - 12 bits: per-millisecond sequence (0..4095)
 *
 * Thread-safe via synchronization on [nextId].
 *
 * @param machineId a unique identifier for this generator instance (0..1023)
 * @param epoch the custom epoch in milliseconds (defaults to 2020-01-01T00:00:00Z)
 * @throws IllegalArgumentException if [machineId] is outside 0..1023
 */
public class SnowflakeGenerator(
    private val machineId: Int,
    private val epoch: Long = 1577836800000L, // 2020-01-01T00:00:00Z
) {
    init {
        require(machineId in 0..MAX_MACHINE_ID) {
            "machineId must be in 0..$MAX_MACHINE_ID, got $machineId"
        }
    }

    private var lastTimestamp: Long = -1L
    private var sequence: Long = 0L

    /**
     * Generates the next unique Snowflake ID.
     *
     * If called multiple times within the same millisecond, the sequence number
     * is incremented. If the sequence overflows, the generator waits until the
     * next millisecond.
     *
     * @return a 64-bit Snowflake ID
     * @throws IllegalStateException if the system clock moves backwards
     */
    @Synchronized
    public fun nextId(): Long {
        var timestamp = System.currentTimeMillis() - epoch

        if (timestamp < lastTimestamp) {
            throw IllegalStateException(
                "Clock moved backwards. Refusing to generate ID for ${lastTimestamp - timestamp}ms"
            )
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            if (sequence == 0L) {
                // Sequence exhausted, wait for next millisecond
                timestamp = waitNextMillis(lastTimestamp)
            }
        } else {
            sequence = 0L
        }

        lastTimestamp = timestamp

        return (timestamp shl TIMESTAMP_SHIFT) or
            (machineId.toLong() shl MACHINE_SHIFT) or
            sequence
    }

    private fun waitNextMillis(lastTs: Long): Long {
        var ts = System.currentTimeMillis() - epoch
        while (ts <= lastTs) {
            ts = System.currentTimeMillis() - epoch
        }
        return ts
    }

    public companion object {
        private const val MACHINE_BITS = 10
        private const val SEQUENCE_BITS = 12
        private const val MACHINE_SHIFT = SEQUENCE_BITS
        private const val TIMESTAMP_SHIFT = MACHINE_BITS + SEQUENCE_BITS

        /** Maximum machine ID (1023). */
        public const val MAX_MACHINE_ID: Int = (1 shl MACHINE_BITS) - 1

        /** Maximum sequence number per millisecond (4095). */
        public const val MAX_SEQUENCE: Long = (1L shl SEQUENCE_BITS) - 1
    }
}
