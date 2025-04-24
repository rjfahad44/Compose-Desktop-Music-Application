package data.api

import data.models.RedditResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.path
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.header

class RedditApi(private val httpClient: HttpClient) {

//    @GET("/r/tiktokcringe/{sort}.json?raw_json=1")
//    suspend fun tikTokCringe(
//        @Path("sort") sort: String? = "top",
//        @Query("t") top: String? = "today"
//    ): RedditResponse

    suspend fun getTikTokCringe(
        sort: String = "top",
        top: String = "today"
    ): RedditResponse {
        return httpClient.get(ApiClient.BASE_URL) {
            url {
                path("r", "tiktokcringe", "$sort.json")
                parameter("raw_json", 1)
                parameter("t", top)
                parameter("limit", 10)
                header(HttpHeaders.UserAgent, "Mozilla/5.0 (Ktor Kotlin Client)")
            }
        }.body()
    }
}