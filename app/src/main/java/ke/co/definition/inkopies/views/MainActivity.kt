package ke.co.definition.inkopies.views

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    companion object {
        fun start(a: AppCompatActivity) {
            val i = Intent(a, MainActivity::class.java)
            a.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        supportFragmentManager.beginTransaction()
                .add(R.id.frame, ShoppingListsListFragment.instantiate())
                .commit()
    }
}
