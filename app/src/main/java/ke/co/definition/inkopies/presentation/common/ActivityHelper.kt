package ke.co.definition.inkopies.presentation.common

import android.graphics.drawable.Drawable
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
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

fun AppCompatActivity.getProgressBarIndeterminate(): Drawable? {
    val attrs = intArrayOf(android.R.attr.indeterminateDrawable)
    val attrsIndeterminateDrawableIndex = 0
    val a = obtainStyledAttributes(android.R.style.Widget_ProgressBar, attrs)
    try {
        return a.getDrawable(attrsIndeterminateDrawableIndex)
    } finally {
        a.recycle()
    }
}

fun AppCompatActivity.loadProfilePic(url: String, v: ImageView) {
    GlideApp
            .with(this)
            .load(url)
            .circleCrop()
            .placeholder(getProgressBarIndeterminate())
            .into(v)
}

fun AppCompatActivity.loadPic(url: String, v: ImageView) {
    GlideApp
            .with(this)
            .load(url)
            .centerCrop()
            .placeholder(getProgressBarIndeterminate())
            .error(R.drawable.avatar)
            .into(v)
}