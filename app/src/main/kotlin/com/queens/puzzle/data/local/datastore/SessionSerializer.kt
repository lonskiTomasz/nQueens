package com.queens.puzzle.data.local.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * JSON for the session store. `null` is the absence of a saved game, and the default for a
 * store that has never been written.
 *
 * Unknown keys are ignored so a file written by a later version still loads.
 */
object SessionSerializer : Serializer<SavedSession?> {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = SavedSession.serializer().nullable

    override val defaultValue: SavedSession? = null

    override suspend fun readFrom(input: InputStream): SavedSession? {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return null
        return try {
            json.decodeFromString(serializer, bytes.decodeToString())
        } catch (cause: SerializationException) {
            throw CorruptionException("Unreadable saved session", cause)
        }
    }

    override suspend fun writeTo(t: SavedSession?, output: OutputStream) {
        output.write(json.encodeToString(serializer, t).encodeToByteArray())
    }
}
