package com.lessons.photogallery

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.*
import com.lessons.photogallery.api.FlickrApi
import com.lessons.photogallery.databinding.FragmentPhotoGalleryBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.create
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val  POLL_WORK = "poll_work"
class PhotoGalleryFragment : Fragment() {

    private var _binding:FragmentPhotoGalleryBinding? = null
    private val binding get() = checkNotNull(_binding){"Cannot access binding"}
    private val photoGalleryViewModel:PhotoGalleryViewModel by viewModels()
    private var searchView:SearchView? = null
    private var pollingMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhotoGalleryBinding.inflate(inflater,container,false)

        binding.photoGrid.layoutManager = GridLayoutManager(context,3)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                photoGalleryViewModel.uiState.collect{state->
                    binding.photoGrid.adapter = PhotoListAdapter(state.images)
                    searchView?.setQuery(state.query, false)
                    updateIsPolling(state.isPolling)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery,menu)

        val searchItem:MenuItem = menu.findItem(R.id.menu_item_search)
        searchView = searchItem.actionView as? SearchView

        searchView?.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                photoGalleryViewModel.setQuery(query?:"")
                searchView?.clearFocus()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        pollingMenuItem = menu.findItem(R.id.menu_item_toggle_polling)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_item_clear->{
                photoGalleryViewModel.setQuery("")
                true
            }
            R.id.menu_item_toggle_polling->{
                photoGalleryViewModel.toggleIsPolling()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateIsPolling(isPolling:Boolean){
        val toggleItemTitle = if(isPolling){
            R.string.stop_polling
        }
        else R.string.start_polling
        pollingMenuItem?.setTitle(toggleItemTitle)
        if(isPolling){
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            val periodRequest = PeriodicWorkRequestBuilder<PollWorker>(5,TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                POLL_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                periodRequest
            )
        }else{
            WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
        }
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        searchView = null
        pollingMenuItem = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}