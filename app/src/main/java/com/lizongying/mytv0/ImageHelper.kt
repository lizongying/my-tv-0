package com.lizongying.mytv0

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun loadNextUrl(
    context: Context,
    imageView: ImageView,
    bitmap: Bitmap,
    urlList: List<String>,
    index: Int,
    handler: Handler,
    onSuccess: (Int) -> Unit
) {
    if (urlList.isEmpty()) {
        return
    }
    if (index >= urlList.size) {
        return
    }
    val url = urlList[index]
    if (url.isEmpty()) {
        Glide.with(context)
            .load(bitmap)
            .fitCenter()
            .into(imageView)
    } else {
        Glide.with(context)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    onSuccess(index)
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    handler.post {
                        loadNextUrl(
                            context,
                            imageView,
                            bitmap,
                            urlList,
                            index + 1,
                            handler,
                            onSuccess
                        )
                    }
                    return true
                }
            })
            .placeholder(BitmapDrawable(context.resources, bitmap))
            .fitCenter()
            .into(imageView)
    }
}