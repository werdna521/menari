package app.android.werdna.menari.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.android.werdna.menari.AudioManager
import app.android.werdna.menari.R
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tunak.setOnClickListener {
            AudioManager.haiya(this)
            val intent = Intent(this, SongActivity::class.java)
            startActivity(intent)
        }

//        listOf(song_btn, fav_btn, search_btn).forEach { btn ->
//            btn.setOnClickListener {
//                Toast.makeText(this, "너 거시기", Toast.LENGTH_SHORT).show()
//            }
//        }
    }
}