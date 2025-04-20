package data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiClient() {
    companion object {
        const val BASE_DOMAIN = "old.reddit.com"
        const val BASE_URL = "https://old.reddit.com/"
    }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        defaultRequest {
            host = BASE_DOMAIN
            url {
                protocol = URLProtocol.HTTPS
            }
            contentType(ContentType.Application.Json)
            header(HttpHeaders.UserAgent, "MyRedditApp/1.0 (Compose Desktop)") // Required by Reddit
        }

        engine {
            config {
                followRedirects(true)
            }
        }
    }

    fun httpClient(): HttpClient = client
}