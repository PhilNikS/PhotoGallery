package com.lessons.photogallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lessons.photogallery.api.GalleryItem
import com.lessons.photogallery.databinding.PhotoGalleryItemBinding

class PhotoListAdapter(
    private val list: List<GalleryItem>
): RecyclerView.Adapter<PhotoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PhotoGalleryItemBinding.inflate(inflater,parent,false)
        return PhotoViewHolder(binding)
    }



    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {

        holder.bind(list[position])
    }
    override fun getItemCount(): Int {
        return list.size
    }

}