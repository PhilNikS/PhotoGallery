package com.lessons.photogallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessons.photogallery.api.GalleryItem
import com.lessons.photogallery.api.PreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

private const val TAG = "PhotoGalleryViewModelTag"
class PhotoGalleryViewModel: ViewModel() {

    private val photoRepository = PhotoRepository()
    private val preferencesRepository:PreferencesRepository = PreferencesRepository.get()




    private val _uiState: MutableStateFlow<PhotoGalleryUiState> =
        MutableStateFlow(PhotoGalleryUiState())
    val uiState:StateFlow<PhotoGalleryUiState>
    get() = _uiState.asStateFlow()



    init {
        viewModelScope.launch {
            preferencesRepository.storedQuery.collectLatest {storedQuery ->
                try {
                    val items = fetchGalleryItems(storedQuery)
                    _uiState.update { oldValue ->
                        oldValue.copy(
                            images = items,
                            query = storedQuery
                        )
                    }
                } catch (ex: Exception){Log.e(TAG,"$ex")}
            }
        }
        viewModelScope.launch {
            preferencesRepository.isPolling.collect{isPolling->
                _uiState.update { it.copy(isPolling = isPolling) }
            }
        }

    }

    fun setQuery(query: String){
        viewModelScope.launch {
            preferencesRepository.setStoredQuery(query)
        }
    }
    fun toggleIsPolling(){
        viewModelScope.launch{
            preferencesRepository.setPolling(!uiState.value.isPolling)
        }
    }
    private suspend fun fetchGalleryItems(query:String):List<GalleryItem>{
        return if(query.isNotEmpty()){
            photoRepository.searchPhotos(query)
        }else{
            photoRepository.fetchPhotos()
        }
    }
}

data class PhotoGalleryUiState(
    val images:List<GalleryItem> = listOf(),
    val query:String = "",
    val isPolling:Boolean = false
)