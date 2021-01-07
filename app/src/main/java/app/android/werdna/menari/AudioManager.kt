package app.android.werdna.menari

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer


class AudioManager {

    companion object {
        fun haiya(context: Context) {
            val afd: AssetFileDescriptor = context.assets.openFd("haiya.mp3")
            val player = MediaPlayer()
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            player.prepare()
            player.start()
        }
    }
}