package pdm.chessroyale

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

private const val STUDENTS_NAMES = "Diogo Novo \nJoão Arcanjo \nSandro Marques"
private const val LICHESS_URL = "https://lichess.org/api"
private const val ISEL_URL = "https://www.isel.pt"

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val studentsInfo = findViewById<TextView>(R.id.studentsInfoText)
        studentsInfo.text = STUDENTS_NAMES

        onClickListenerAux(R.id.lichessIcon, LICHESS_URL)
        onClickListenerAux(R.id.iselIcon, ISEL_URL)
    }

    private fun onClickListenerAux(imageId: Int, url: String) {
        findViewById<ImageView>(imageId).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }
    }
}