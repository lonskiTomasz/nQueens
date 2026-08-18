package com.queens.puzzle.data.local.datastore

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SessionSerializerTest {

    @Test
    fun `a session survives a write and read`() = runTest {
        val session = SavedSession(
            boardSize = 6,
            queens = listOf(SavedPosition(0, 1), SavedPosition(2, 3)),
            moves = listOf(
                SavedMove(SavedMoveKind.Place, SavedPosition(0, 1)),
                SavedMove(SavedMoveKind.Remove, SavedPosition(4, 4)),
            ),
            taps = 3,
            undos = 1,
            elapsedMillis = 42_000,
        )

        assertEquals(session, roundTrip(session))
    }

    @Test
    fun `null survives a write and read`() = runTest {
        assertNull(roundTrip(null))
    }

    @Test
    fun `a store that has never been written reads as nothing saved`() = runTest {
        assertNull(SessionSerializer.readFrom(ByteArrayInputStream(ByteArray(0))))
    }

    @Test
    fun `keys written by a later version are ignored`() = runTest {
        val json = """
            {"boardSize":4,"queens":[],"moves":[],"taps":0,"undos":0,"elapsedMillis":0,
             "hintsUsed":3}
        """.trimIndent()

        val restored = SessionSerializer.readFrom(ByteArrayInputStream(json.encodeToByteArray()))

        assertEquals(4, restored?.boardSize)
    }

    @Test
    fun `an unreadable file is reported as corruption`() = runTest {
        val input = ByteArrayInputStream("not json at all".encodeToByteArray())

        val thrown = runCatching { SessionSerializer.readFrom(input) }.exceptionOrNull()

        assertTrue(
            "expected a CorruptionException, was $thrown",
            thrown is CorruptionException,
        )
    }

    private suspend fun roundTrip(session: SavedSession?): SavedSession? {
        val output = ByteArrayOutputStream()
        SessionSerializer.writeTo(session, output)
        return SessionSerializer.readFrom(ByteArrayInputStream(output.toByteArray()))
    }
}
