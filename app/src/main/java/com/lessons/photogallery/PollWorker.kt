package com.lessons.photogallery

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lessons.photogallery.api.NOTIFICATION_CHANEL_ID
import com.lessons.photogallery.api.PreferencesRepository
import kotlinx.coroutines.flow.first
import okhttp3.internal.notify

private const val TAG = "PollWorkerTag"
class PollWorker(
    private val context: Context,
    workerParameters: WorkerParameters
    ):CoroutineWorker(context,workerParameters) {
    override suspend fun doWork(): Result {
        val preferencesRepository = PreferencesRepository.get()
        val photoRepository = PhotoRepository()

        val query = preferencesRepository.storedQuery.first()
        val lastId = preferencesRepository.latestResultId.first()

        if(query.isEmpty()){
            Log.i(TAG,"Query is empty, finishing early")
            return Result.success()
        }
        return try{
            val items = photoRepository.searchPhotos(query)
            if(items.isNotEmpty()){
                val newResultId = items.first().id
                if(newResultId == lastId){
                    Log.i(TAG,"Still have the same id $newResultId")
                }else{
                    Log.i(TAG, "Got a new result id $newResultId")
                    preferencesRepository.setLastResultId(newResultId)
                    notifyUser()
                }
            }
            Result.success()
        } catch (ex:Exception){
            Log.e(TAG,"Background work is failed",ex)
            Result.failure()
        }
    }

    private fun notifyUser(){
        val intent = MainActivity.newIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val resources = context.resources

        val notification = NotificationCompat
            .Builder(context, NOTIFICATION_CHANEL_ID)
            .setTicker(resources.getString(R.string.new_pictures_title))
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentTitle(resources.getString(R.string.new_pictures_title))
            .setContentText(resources.getString(R.string.new_pictures_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if(ContextCompat.checkSelfPermission(context,android.Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(0, notification)
        }else Log.i(TAG,"Notification is not evaluable")




    }
}