package com.example.playify

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class PlaylistViewActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_view_activity)

        val url = intent.getStringExtra(getString(R.string.EXTRA_PLAYLIST_IMAGE))
        val ivPlaylist = findViewById<ImageView>(R.id.ivPlaylist)
        GlideApp.with(this).load(url).placeholder(android.R.drawable.ic_media_play).into(ivPlaylist)

        val name = intent.getStringExtra(getString(R.string.EXTRA_PLAYLIST_NAME))
        findViewById<TextView>(R.id.tvPlaylistTitle).text = name
        val description = intent.getStringExtra(getString(R.string.EXTRA_PLAYLIST_DESCRIPTION))
        findViewById<TextView>(R.id.tvPlaylistDescription).text = description
        val creator = intent.getStringExtra(getString(R.string.EXTRA_PLAYLIST_CREATOR))
        findViewById<TextView>(R.id.tvPlaylistCreator).text = creator

        val tracks = JSONArray(intent.getStringExtra(getString(R.string.EXTRA_TRACKS)))
        val recyclerView = findViewById<RecyclerView>(R.id.rvPlaylist)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PlaylistAdapter(this, tracks) { track ->
            startActivity(trackIntent(track))
        }

    }
}
