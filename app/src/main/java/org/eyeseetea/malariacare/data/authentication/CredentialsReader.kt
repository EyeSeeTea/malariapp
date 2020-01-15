package org.eyeseetea.malariacare.data.authentication

import android.content.Context
import com.google.gson.Gson
import org.eyeseetea.malariacare.R
import org.eyeseetea.malariacare.domain.common.Either
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringWriter
import java.io.Writer

data class Credentials(val poEditor: PoEditorCredentials)
data class PoEditorCredentials(val token: String, val projectId: String)

sealed class CredentialsFailure {
    object ParseFailure : CredentialsFailure()
}

object CredentialsReader {
    private var credentialsCache: Credentials? = null

    fun poEditorCredentials(context: Context): Either<CredentialsFailure, PoEditorCredentials> {
        return if (credentialsCache != null) {
            Either.Right(credentialsCache!!.poEditor)
        } else {
            try {
                val credentials = loadCredentials(context)

                if (credentials.poEditor?.projectId == null ||
                    credentials.poEditor?.token == null) {
                    Either.Left(CredentialsFailure.ParseFailure)
                } else {
                    credentialsCache = credentials
                    Either.Right(credentialsCache!!.poEditor)
                }
            } catch (e: Exception) {
                Either.Left(CredentialsFailure.ParseFailure)
            }
        }
    }

    private fun loadCredentials(context: Context): Credentials {
        val json = loadConfigJson(context)
        val gson = Gson()

        return gson.fromJson(json, Credentials::class.java)
    }

    private fun loadConfigJson(context: Context): String {
        val stream: InputStream = context.resources.openRawResource(R.raw.config)
        val writer: Writer = StringWriter()
        val buffer = CharArray(1024)
        stream.use {
            val reader: Reader = BufferedReader(InputStreamReader(it, "UTF-8"))
            var bytesRead: Int
            while (reader.read(buffer).also { bytes -> bytesRead = bytes } != -1) {
                writer.write(buffer, 0, bytesRead)
            }
        }

        return writer.toString()
    }
}
