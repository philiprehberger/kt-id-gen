# id-gen

[![Tests](https://github.com/philiprehberger/kt-id-gen/actions/workflows/publish.yml/badge.svg)](https://github.com/philiprehberger/kt-id-gen/actions/workflows/publish.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.philiprehberger/id-gen.svg)](https://central.sonatype.com/artifact/com.philiprehberger/id-gen)
[![License](https://img.shields.io/github/license/philiprehberger/kt-id-gen)](LICENSE)

ID generation for Kotlin: ULID, NanoID, Snowflake, and prefixed IDs.

## Installation

### Gradle Kotlin DSL

```kotlin
implementation("com.philiprehberger:id-gen:0.1.0")
```

### Maven

```xml
<dependency>
    <groupId>com.philiprehberger</groupId>
    <artifactId>id-gen</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage

```kotlin
import com.philiprehberger.idgen.IdGen
import com.philiprehberger.idgen.SnowflakeGenerator

// ULID — sortable, 26-char Crockford Base32
val ulid = IdGen.ulid()           // "01H5RZJK3E8GPQM9N2VBCDEFGH"

// NanoID — compact, URL-safe
val nano = IdGen.nanoid()         // "V1StGXR8_Z5jdHi6B-myT"
val custom = IdGen.nanoid(size = 10, alphabet = "abc123")

// UUID v4
val uuid = IdGen.uuid()           // "550e8400-e29b-41d4-a716-446655440000"

// Short UUID — Base62-encoded, 22 chars
val short = IdGen.uuidShort()     // "6Bx0lWzT3QR7YNg1k5vJ2m"

// Prefixed ID — typed identifiers
val userId = IdGen.prefixed("usr") // "usr_01H5RZJK3E8GPQM9N2VBC"

// Snowflake — 64-bit, machine-scoped
val snowflake = SnowflakeGenerator(machineId = 1)
val id = snowflake.nextId()        // 123456789012345678
```

## API

| Function / Class | Description |
|---|---|
| `IdGen.ulid()` | 26-char ULID with Crockford Base32 encoding, monotonic within millisecond |
| `IdGen.nanoid(size, alphabet)` | URL-safe NanoID with configurable length and alphabet |
| `IdGen.uuid()` | Standard UUID v4 string |
| `IdGen.uuidShort()` | Base62-encoded UUID, 22 characters |
| `IdGen.prefixed(prefix)` | Prefixed ULID in format `{prefix}_{ulid}` |
| `SnowflakeGenerator(machineId)` | Creates a Snowflake generator for 64-bit IDs |
| `SnowflakeGenerator.nextId()` | Generates next Snowflake ID (41-bit timestamp + 10-bit machine + 12-bit sequence) |

## Development

```bash
./gradlew build
./gradlew test
```

## License

MIT
