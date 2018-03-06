package ke.co.definition.inkopies.presentation.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ke.co.definition.inkopies.R
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)
    }

    companion object {
        fun start(a: Activity) {
            a.startActivity(Intent(a, ProfileActivity::class.java))
        }
    }
}
