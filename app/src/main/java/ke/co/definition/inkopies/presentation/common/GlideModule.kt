package ke.co.definition.inkopies.presentation.common

import android.graphics.drawable.Drawable
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * Created by tomogoma
 * On 09/03/18.
 */
@GlideModule
class InkopiesGlideModule : AppGlideModule()

fun newRequestListener(completeListener: () -> Unit) = object : RequestListener<Drawable> {
    override fun onLoadFailed(e: GlideException?, model: Any?,
                              target: Target<Drawable>?,
                              isFirstResource: Boolean): Boolean {
        completeListener()
        return false // let glide handle the error
    }

    override fun onResourceReady(resource: Drawable?, model: Any?,
                                 target: Target<Drawable>?,
                                 dataSource: DataSource?,
                                 isFirstResource: Boolean): Boolean {
        completeListener()
        return false // let glide handle the success
    }

}