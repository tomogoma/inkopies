package ke.co.definition.inkopies.presentation.common

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import ke.co.definition.inkopies.R


/**
 * Created by tomogoma
 * On 01/03/18.
 */

fun AppCompatActivity.replaceFragBackStack(where: Int, frag: Fragment) {
    supportFragmentManager.beginTransaction()
            .replace(where, frag, frag.javaClass.name)
            .addToBackStack(frag.javaClass.name)
            .commit()
}

fun AppCompatActivity.replaceFrag(where: Int, frag: Fragment) {
    supportFragmentManager.beginTransaction()
            .replace(where, frag, frag.javaClass.name)
            .commit()
}

fun AppCompatActivity.loadProfilePic(url: GlideUrl, v: ImageView, l: RequestListener<Drawable>?) {
    GlideApp
            .with(this)
            .load(url)
            .listener(l)
            .circleCrop()
            .error(R.drawable.avatar)
            .into(v)
}

fun AppCompatActivity.loadPic(url: GlideUrl, v: ImageView, l: RequestListener<Drawable>?) {
    GlideApp
            .with(this)
            .load(url)
            .listener(l)
            .centerCrop()
            .into(v)
}