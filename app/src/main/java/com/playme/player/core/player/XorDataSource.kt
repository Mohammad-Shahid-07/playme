package com.playme.player.core.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.TransferListener

@OptIn(UnstableApi::class)
class XorDataSource(
    private val upstream: DataSource,
    private val key: Byte = 0x5A.toByte()
) : DataSource {

    override fun addTransferListener(transferListener: TransferListener) {
        upstream.addTransferListener(transferListener)
    }

    override fun open(dataSpec: DataSpec): Long {
        return upstream.open(dataSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val bytesRead = upstream.read(buffer, offset, length)
        if (bytesRead == C.RESULT_END_OF_INPUT) {
            return C.RESULT_END_OF_INPUT
        }
        for (i in offset until offset + bytesRead) {
            buffer[i] = (buffer[i].toInt() xor key.toInt()).toByte()
        }
        return bytesRead
    }

    override fun getUri(): Uri? = upstream.uri

    override fun close() {
        upstream.close()
    }

    class Factory(
        private val context: Context,
        private val key: Byte = 0x5A.toByte()
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource {
            val defaultSource = DefaultDataSource.Factory(context).createDataSource()
            return XorDataSource(defaultSource, key)
        }
    }
}
