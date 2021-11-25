package org.eyeseetea.malariacare.data.remote.api

import android.util.Log
import org.eyeseetea.malariacare.data.ReadableServerDataSource
import org.eyeseetea.malariacare.data.ServerDataSourceFailure
import org.eyeseetea.malariacare.data.database.utils.PreferencesState
import org.eyeseetea.malariacare.data.remote.poeditor.PoEditorApiClient
import org.eyeseetea.malariacare.data.remote.poeditor.Term
import org.eyeseetea.malariacare.domain.common.Either
import org.eyeseetea.malariacare.domain.entity.Server
import org.eyeseetea.malariacare.domain.entity.ServerClassification
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class ServerRemoteDataSource(private val poEditorApiClient: PoEditorApiClient) :
    ReadableServerDataSource {

    override fun getAll(): List<Server> {
        return when (val result = poEditorApiClient.getTerms("en")) {
            is Either.Left -> listOf()
            is Either.Right -> {
                findServerUrls(result.value).map { serverInfo ->
                    val serverInfoArray = serverInfo.split(SERVER_TERM_SEPARATOR)

                    if (serverInfoArray.size == 1) {
                        Server(url = serverInfoArray[0])
                    } else {
                        val classification = parseCompetencies(serverInfoArray[1].toUpperCase())

                        Server(url = serverInfoArray[0], classification = classification)
                    }
                }
            }
        }
    }

    private fun parseCompetencies(competenciesText: String): ServerClassification {
        return try {
            ServerClassification.valueOf(competenciesText)
        } catch (ex: Exception) {
            ServerClassification.COMPETENCIES
        }
    }

    override fun get(): Either<ServerDataSourceFailure, Server> {
        val server: Server
        try {
            val credentials = PreferencesState.getInstance().creedentials

            val response = OkHttpClientDataSource.executeCall(
                BasicAuthenticator(credentials),
                credentials!!.serverURL, SERVER_VERSION_CALL
            )
            val jsonNode = OkHttpClientDataSource.parseResponse(response.body()!!.string())
            val keyFlag = jsonNode[KEY_FLAG_FIELD].asText()

            val applicationTitle = jsonNode[APPLICATION_TITLE_FIELD].asText()

            val logo = getLogo(keyFlag, credentials.serverURL)

            server = Server(credentials.serverURL, applicationTitle, logo, true)
            return Either.Right(server)
        } catch (ex: Exception) {
            return Either.Left(ServerDataSourceFailure.NetworkFailure)
        }
    }

    private fun getLogo(keyFlag: String, serverUrl: String): ByteArray? {
        val logoEndpoint = String.format(LOGO_URL_ENDPOINT, keyFlag)
        val baseUrl: URL
        return try {
            baseUrl = URL(serverUrl)
            val logoUrl = URL(baseUrl.protocol, baseUrl.host, baseUrl.port, logoEndpoint)
            getLogoFromURL(logoUrl)
        } catch (e: MalformedURLException) {
            Log.e(TAG, e.message.toString())
            e.printStackTrace()
            null
        }
    }

    private fun getLogoFromURL(url: URL): ByteArray? {
        return try {
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.doInput = true
            httpURLConnection.connect()
            val inputStream = httpURLConnection.inputStream
            inputStream.readBytes()
        } catch (e: Exception) {
            Log.e(this.javaClass.simpleName, e.message.toString())
            e.printStackTrace()
            null
        }
    }

    private fun findServerUrls(terms: List<Term>): List<String> {
        val serversTerm = terms.firstOrNull { t -> t.term == SERVERS_TERM }

        return if (serversTerm?.translation != null) {
            serversTerm.translation.content.split(SERVERS_TERM_SEPARATOR)
        } else {
            listOf()
        }
    }

    companion object {
        private const val SERVER_VERSION_CALL = "api/systemSettings/"
        private const val KEY_FLAG_FIELD = "keyFlag"
        private const val APPLICATION_TITLE_FIELD = "applicationTitle"
        private const val LOGO_URL_ENDPOINT = "dhis-web-commons/flags/%s.png"
        private const val TAG = ".ServerRemoteDataSource"
        private const val SERVERS_TERM = "server_list"
        private const val SERVERS_TERM_SEPARATOR = "\n"
        private const val SERVER_TERM_SEPARATOR = "|"
    }
}
