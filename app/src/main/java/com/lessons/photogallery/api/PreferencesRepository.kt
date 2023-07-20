package com.lessons.photogallery.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*


import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PreferencesRepository private constructor(
    private val dataStore: DataStore<Preferences>
){
    val storedQuery: Flow<String> = dataStore.data.map {
        it[SEARCH_QUERY_KEY] ?: ""
    }.distinctUntilChanged()

    val latestResultId:Flow<String> = dataStore.data.map {
        it[LAST_PREF_RESULT_ID] ?: ""
    }

    companion object {
        private val SEARCH_QUERY_KEY = stringPreferencesKey("search_query")
        private val LAST_PREF_RESULT_ID = stringPreferencesKey("LastResultId")
        private val PREF_IS_POLLING = booleanPreferencesKey("isPolling")
        private var INSTANCE: PreferencesRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                val dataStore = PreferenceDataStoreFactory.create {
                    context.preferencesDataStoreFile("settings")
                }

                INSTANCE = PreferencesRepository(dataStore)
            }
        }
        fun get():PreferencesRepository{
            return INSTANCE?:throw IllegalStateException("Instance must be initialized")
        }
    }

    suspend fun setStoredQuery(query:String){
        dataStore.edit{
            it[SEARCH_QUERY_KEY] = query
        }
    }

    suspend fun setLastResultId(latestResultId:String){
        dataStore.edit {
            it[LAST_PREF_RESULT_ID] = latestResultId
        }
    }

    val isPolling:Flow<Boolean> = dataStore.data.map {
        it[PREF_IS_POLLING]?:false
    }.distinctUntilChanged()

    suspend fun setPolling(isPolling:Boolean){
        dataStore.edit {
            it[PREF_IS_POLLING] = isPolling
        }
    }
}