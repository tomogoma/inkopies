package ke.co.definition.inkopies.presentation.common

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

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