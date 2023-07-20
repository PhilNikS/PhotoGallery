package com.lessons.photogallery

import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.lessons.photogallery.api.GalleryItem
import com.lessons.photogallery.databinding.PhotoGalleryItemBinding

class PhotoViewHolder(
    private val binding: PhotoGalleryItemBinding
): RecyclerView.ViewHolder(binding.root) {
    fun bind(galleryItem: GalleryItem){
        binding.photoItem.load(galleryItem.url){
            placeholder(R.drawable.bill_up_close)
        }

    }
}