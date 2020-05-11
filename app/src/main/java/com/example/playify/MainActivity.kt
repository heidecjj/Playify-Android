package com.example.playify

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        var pathText = findViewById<TextView>(R.id.spotifySettingsPath)

        var ver = Build.VERSION.SDK_INT
        when {
            ver >= Build.VERSION_CODES.P ->
                pathText.text = getString(R.string.spotify_settings_path_v28)
            ver >= Build.VERSION_CODES.M ->
                pathText.text = getString(R.string.spotify_settings_path_v23)
            else -> pathText.text = getString(R.string.spotify_settings_path_v9)
        }
    }

    override fun onResume() {
        super.onResume()
        showHideSpotifyInstructions()
    }

    private fun showHideSpotifyInstructions() {
        var spotifyCard = findViewById<CardView>(R.id.spotifyInstructionsCard)
        try {
            packageManager.getPackageInfo(getString(R.string.spotify_uri), 0)
            spotifyCard.visibility = View.VISIBLE
        } catch (err: PackageManager.NameNotFoundException) {
            spotifyCard.visibility = View.GONE
        }
    }

    fun startSpotifyAppInfo(view: View) {
        startActivity(Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:${getString(R.string.spotify_uri)}")))
    }

}
