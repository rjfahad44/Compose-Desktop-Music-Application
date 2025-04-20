package manual_di

import data.api.ApiClient
import data.api.RedditApi
import data.repository.RedditDataRepository
import data.repository.VideoDataRepository
import javax.naming.Context


interface ManualAppModule {
    val api: ApiClient
    val redditDataRepository: VideoDataRepository
}


class ManualAppModuleImpl(
) : ManualAppModule {

    override val api: ApiClient by lazy { ApiClient() }

    override val redditDataRepository: VideoDataRepository by lazy {
        RedditDataRepository(api = RedditApi(api.httpClient()))
    }
}