package data.repository

import data.test.SampleData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import models.VideoData

class SampleVideoDataRepository: VideoDataRepository {
    override fun fetchData(): Flow<List<VideoData>> = flowOf(value = SampleData.sampleVideos)
}