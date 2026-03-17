package com.philiprehberger.idgen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdGenTest {

    private val crockfordAlphabet = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toSet()

    @Test
    fun `ulid is 26 characters of Crockford Base32`() {
        val id = IdGen.ulid()
        assertEquals(26, id.length)
        assertTrue(id.all { it in crockfordAlphabet }, "ULID contains invalid characters: $id")
    }

    @Test
    fun `ulids are lexicographically sortable by time`() {
        val first = IdGen.ulid()
        Thread.sleep(2)
        val second = IdGen.ulid()
        assertTrue(first < second, "Expected $first < $second")
    }

    @Test
    fun `ulids within same millisecond are monotonic`() {
        val ids = (1..100).map { IdGen.ulid() }
        val sorted = ids.sorted()
        assertEquals(ids, sorted, "ULIDs generated in sequence should already be sorted")
    }

    @Test
    fun `nanoid has correct default length`() {
        val id = IdGen.nanoid()
        assertEquals(21, id.length)
    }

    @Test
    fun `nanoid respects custom size`() {
        val id = IdGen.nanoid(size = 10)
        assertEquals(10, id.length)
    }

    @Test
    fun `nanoid uses custom alphabet`() {
        val alphabet = "abc"
        val id = IdGen.nanoid(size = 50, alphabet = alphabet)
        assertEquals(50, id.length)
        assertTrue(id.all { it in alphabet }, "NanoID contains characters outside alphabet: $id")
    }

    @Test
    fun `nanoid default alphabet is URL-safe`() {
        val id = IdGen.nanoid(size = 100)
        val urlSafe = ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('_', '-')
        assertTrue(id.all { it in urlSafe }, "NanoID contains non-URL-safe characters: $id")
    }

    @Test
    fun `uuid has correct format`() {
        val id = IdGen.uuid()
        val uuidRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
        assertTrue(uuidRegex.matches(id), "UUID format invalid: $id")
    }

    @Test
    fun `prefixed id has correct format`() {
        val id = IdGen.prefixed("usr")
        assertTrue(id.startsWith("usr_"), "Prefixed ID should start with 'usr_': $id")
        val suffix = id.removePrefix("usr_")
        assertEquals(26, suffix.length, "Suffix should be a 26-char ULID")
        assertTrue(suffix.all { it in crockfordAlphabet }, "Suffix is not valid ULID: $suffix")
    }

    @Test
    fun `uuidShort is 22 characters`() {
        val id = IdGen.uuidShort()
        assertEquals(22, id.length)
    }

    @Test
    fun `uuidShort uses base62 characters only`() {
        val base62 = ('0'..'9') + ('A'..'Z') + ('a'..'z')
        val id = IdGen.uuidShort()
        assertTrue(id.all { it in base62 }, "uuidShort contains invalid characters: $id")
    }

    @Test
    fun `snowflake generates positive IDs`() {
        val gen = SnowflakeGenerator(machineId = 1)
        val id = gen.nextId()
        assertTrue(id > 0, "Snowflake ID should be positive: $id")
    }

    @Test
    fun `snowflake IDs are monotonically increasing`() {
        val gen = SnowflakeGenerator(machineId = 1)
        val ids = (1..1000).map { gen.nextId() }
        val sorted = ids.sorted()
        assertEquals(ids, sorted, "Snowflake IDs should be monotonically increasing")
    }

    @Test
    fun `snowflake IDs from different machines differ`() {
        val gen1 = SnowflakeGenerator(machineId = 1)
        val gen2 = SnowflakeGenerator(machineId = 2)
        val id1 = gen1.nextId()
        val id2 = gen2.nextId()
        assertTrue(id1 != id2, "Snowflake IDs from different machines should differ")
    }

    @Test
    fun `all generated IDs are unique across 1000 iterations`() {
        val ulids = (1..1000).map { IdGen.ulid() }.toSet()
        assertEquals(1000, ulids.size, "Expected 1000 unique ULIDs")

        val nanoids = (1..1000).map { IdGen.nanoid() }.toSet()
        assertEquals(1000, nanoids.size, "Expected 1000 unique NanoIDs")

        val uuids = (1..1000).map { IdGen.uuid() }.toSet()
        assertEquals(1000, uuids.size, "Expected 1000 unique UUIDs")

        val snowflake = SnowflakeGenerator(machineId = 0)
        val snowflakeIds = (1..1000).map { snowflake.nextId() }.toSet()
        assertEquals(1000, snowflakeIds.size, "Expected 1000 unique Snowflake IDs")
    }
}
