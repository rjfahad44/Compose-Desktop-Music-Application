package data.repository

import kotlinx.coroutines.flow.Flow
import models.VideoData

interface VideoDataRepository {
    fun fetchData(): Flow<List<VideoData>>
}