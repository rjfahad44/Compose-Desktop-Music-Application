package data.repository


import data.api.RedditApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import models.VideoData
import java.util.concurrent.CancellationException

class RedditDataRepository(
    private val api: RedditApi
): VideoDataRepository {
    override fun fetchData(): Flow<List<VideoData>> = flow {
        try {
            val response = api.getTikTokCringe()
            val videoData = response
                .data
                ?.posts
                ?.map { post ->
                    val video = post.data?.secureMedia?.video
                    val width = video?.width
                    val height = video?.height
                    val aspectRatio = if (width != null && height != null) {
                        width.toFloat() / height.toFloat()
                    } else {
                        null
                    }
                    VideoData(
                        id = post.data?.id.orEmpty(),
                        mediaUri = video?.hlsUrl.orEmpty(),
                        previewImageUri = post.data?.preview?.images?.firstOrNull()?.source?.url.orEmpty(),
                        aspectRatio = aspectRatio
                    )
                }
                ?.filter { videoData ->
                    videoData.id.isNotBlank()
                            && videoData.mediaUri.isNotBlank()
                            && videoData.previewImageUri.isNotBlank()
                }
                .orEmpty()

            emit(videoData)
        } catch (throwable: Throwable) {
            println(throwable.message)
            if (throwable is CancellationException) throw throwable
        }
    }
}