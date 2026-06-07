package com.dieghosty10.ghostymusicy

import com.dieghosty10.ghostymusicy.innertube.YouTube
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.assertTrue

class YouTubeTest {
    @Test
    fun testSearch() = runBlocking {
        val result = YouTube.search("bad bunny", YouTube.SearchFilter.FILTER_SONG)
        println("SEARCH RESULT IS SUCCESS: " + result.isSuccess)
        if (result.isFailure) {
            println("SEARCH EXCEPTION: " + result.exceptionOrNull()?.message)
            result.exceptionOrNull()?.printStackTrace()
        }
        assertTrue(result.isSuccess)
        println("SEARCH ITEMS: " + result.getOrNull()?.items?.size)
    }
}
