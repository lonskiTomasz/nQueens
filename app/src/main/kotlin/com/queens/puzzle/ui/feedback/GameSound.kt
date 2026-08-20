package com.queens.puzzle.ui.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.queens.puzzle.R

/**
 * The click a queen makes landing on the board.
 *
 * An interface so a test can bind a recording implementation and assert *that* a sound was
 * requested, with no device involved.
 */
interface GameSound {

    fun place()
}

/** Does nothing. Bound when the player has sound switched off. */
object SilentGameSound : GameSound {
    override fun place() = Unit
}

private class SoundPoolGameSound(private val soundPool: SoundPool, private val soundId: Int) : GameSound {

    private var loaded = false

    init {
        soundPool.setOnLoadCompleteListener { _, id, status ->
            if (id == soundId && status == 0) loaded = true
        }
    }

    override fun place() {
        // A tap that lands before decoding finishes just stays silent rather than queuing up.
        if (loaded) soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
    }
}

/**
 * Honours the player's sound setting by only decoding and loading the clip once it is on,
 * and releasing the pool the moment it is switched off.
 */
@Composable
fun rememberGameSound(enabled: Boolean): GameSound {
    val context = LocalContext.current
    val holder = remember(enabled) { if (enabled) SoundPoolHolder(context) else null }

    DisposableEffect(holder) {
        onDispose { holder?.release() }
    }

    return holder?.sound ?: SilentGameSound
}

private class SoundPoolHolder(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    val sound: GameSound = SoundPoolGameSound(soundPool, soundPool.load(context, R.raw.queen_place, 1))

    fun release() = soundPool.release()
}
