package com.lessons.photogallery.api

import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = "769040300733806e9fd9a1c42954b9ef"
interface FlickrApi {

    @GET("services/rest/?method=flickr.interestingness.getList")
    suspend fun fetchPhotos():FlickrResponse

    @GET("services/rest/?method=flickr.photos.search")
    suspend fun searchPhoto(@Query("text")query: String):FlickrResponse
}