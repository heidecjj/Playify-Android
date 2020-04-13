package com.example.playify

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val mediaHandlers = mapOf<String, (JSONObject) -> Any>(
        "track" to { r : JSONObject -> handleTrack(r) },
        "artist" to { r : JSONObject -> handleArtist(r) },
        "album" to { r : JSONObject -> handleAlbum(r) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_VIEW == intent.action) {
            val (type, id) = parseSpotifyUrl(intent.dataString)
            val url = generatePlayifyUrl(type, id)
            findViewById<TextView>(R.id.spotifyURL).text = intent.dataString
            val request = JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    mediaHandlers[type]?.invoke(response)
                },
                Response.ErrorListener { error -> })

            val queue = Volley.newRequestQueue(this)
            queue.add(request)
        }
    }

    private fun parseSpotifyUrl(url: String?) : Pair<String?, String?> {
        val split = url?.split('?')?.get(0)?.split('/')
        val type = split?.get(split?.size-2)
        val id = split?.get(split?.size-1)
        return Pair(type, id)
    }

    private fun generatePlayifyUrl(type: String?, id: String?) : String? {
        return "https://playify.uc.r.appspot.com/${type}?id=${id}"
    }

    private fun handleTrack(response: JSONObject) {
        val title = response.get("name") as String
        val artists = response.get("artists") as JSONArray
        val firstArtist = artists[0] as String
        val album = (response.get("album") as JSONObject).get("name") as String
        findViewById<TextView>(R.id.resultText).text =
            "Title: ${title}\nArtists: ${artists.join(", ")}\nAlbum: ${album}"
        val i = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/audio")
            putExtra(MediaStore.EXTRA_MEDIA_TITLE, title)
            putExtra(MediaStore.EXTRA_MEDIA_ARTIST, firstArtist)
            putExtra(MediaStore.EXTRA_MEDIA_ALBUM, album)
            putExtra(SearchManager.QUERY, "${title} - ${firstArtist}")
        }
        startActivity(i)
    }

    private fun handleArtist(response: JSONObject) {
        val artist = response.get("name") as String

        findViewById<TextView>(R.id.resultText).text = "Artist: ${artist}"
        val i = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)
            putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist)
            putExtra(SearchManager.QUERY, "${artist}")
        }
        startActivity(i)
    }

    private fun handleAlbum(response: JSONObject) {
        val album = response.get("name") as String
        val artists = response.get("artists") as JSONArray
        val firstArtist = artists[0] as String
        findViewById<TextView>(R.id.resultText).text =
            "Album: ${album}\nArtists: ${artists.join(", ")}"
        val i = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)
            putExtra(MediaStore.EXTRA_MEDIA_ALBUM, album)
            putExtra(MediaStore.EXTRA_MEDIA_ARTIST, firstArtist)
            putExtra(SearchManager.QUERY, "${album} - ${firstArtist}")
        }
        startActivity(i)
    }
}
