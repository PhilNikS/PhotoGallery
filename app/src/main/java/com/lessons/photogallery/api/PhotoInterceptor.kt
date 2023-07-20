package com.lessons.photogallery.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val API_KEY = "769040300733806e9fd9a1c42954b9ef"
class PhotoInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newUrl = originalRequest.url.newBuilder()
            .addQueryParameter("api_key", API_KEY)
            .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback","1")
            .addQueryParameter("extras","url_s")
            .addQueryParameter("safe_search","1")
            .build()

        val newRequest:Request = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }

}