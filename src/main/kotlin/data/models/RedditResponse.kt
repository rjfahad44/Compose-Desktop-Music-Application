package data.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RedditResponse(
    val data: Data1?
) {
    @Serializable
    data class Data1(
        @SerialName("children")
        val posts: List<Post>?
    ) {
        @Serializable
        data class Post(
            val data: Data2?
        ) {
            @Serializable
            data class Data2(
                val id: String,
                @SerialName("secure_media")
                val secureMedia: SecureMedia?,
                val preview: Preview?
            ) {
                @Serializable
                data class SecureMedia(
                    @SerialName("reddit_video")
                    val video: Video?
                ) {
                    @Serializable
                    data class Video(
                        val width: Int?,
                        val height: Int?,
                        val duration: Int?,
                        @SerialName("hls_url")
                        val hlsUrl: String?,
                        @SerialName("dash_url")
                        val dashUrl: String?
                    )
                }

                @Serializable
                data class Preview(
                    val images: List<Image>?
                ) {
                    @Serializable
                    data class Image(
                        val source: Source?
                    ) {
                        @Serializable
                        data class Source(
                            val url: String?,
                            val width: Int?,
                            val height: Int?
                        )
                    }
                }
            }
        }
    }
}
