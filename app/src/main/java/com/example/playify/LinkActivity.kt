package com.example.playify

import android.app.AlertDialog
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class LinkActivity : AppCompatActivity() {
    private val mediaIntents = mapOf<String, (JSONObject) -> Any>(
        "track" to ::trackIntent,
        "artist" to ::artistIntent,
        "album" to ::albumIntent,
        "playlist" to ::playlistIntent
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.link_activity)
    }

    override fun onStart() {
        super.onStart()
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_VIEW == intent.action) {
            val (type, id) = parseSpotifyUrl(intent.dataString)
            val url = generatePlayifyUrl(type, id)

            val request = JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener<JSONObject> { response ->
                    handleResponse(type, id, response)
                },
                Response.ErrorListener { _ -> })

            val queue = Volley.newRequestQueue(this)
            queue.add(request)
        }
    }

    private fun handleResponse(type: String?, id: String?, response: JSONObject) {
        if (type.equals("playlist")) {
            handlePlaylist(type, id, response)
        } else {
            handleMedia(type, response)
        }
    }

    private fun handleMedia(type: String?, response: JSONObject) {
        var i = mediaIntents[type]?.invoke(response) as Intent
        try {
            startActivity(i)
            finish()
            return
        } catch (_: ActivityNotFoundException) { }

        i.action = MediaStore.INTENT_ACTION_MEDIA_SEARCH
        try {
            startActivity(i)
            finish()
            return
        } catch (_: ActivityNotFoundException) { }

        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(("Unable to find an app to play \"%s\". Make sure you have an app like " +
                    "Google Play Music, Youtube, or a web browser installed")
                .format(i.extras?.get(SearchManager.QUERY)))
            .setNeutralButton("ok") { _, _ ->  finish() }
            .setOnCancelListener { _ -> finish() }
            .create().show()
        finish()
    }

    private fun handlePlaylist(type: String?, id: String?, response: JSONObject) {
        val limit = response.getInt("limit")
        val total = response.getInt("total")
        val page = response.getInt("page")

        if (page < (total - 1) / limit) {
            val url = generatePlayifyUrl(type, id, page + 1)

            val request = JsonObjectRequest(
                Request.Method.GET, url, null,
                Response.Listener<JSONObject> { newResponse ->
                    val newTracks = newResponse.getJSONArray("tracks")
                    val oldTracks = response.getJSONArray("tracks")
                    for (i in 0 until newTracks.length()) {
                        oldTracks.put(newTracks.get(i))
                    }
                    response.put("page", page + 1)
                    handlePlaylist(type, id, response)
                },
                Response.ErrorListener { _ -> })

            val queue = Volley.newRequestQueue(this)
            queue.add(request)
        } else {
            handleMedia(type, response)
        }

    }

    private fun parseSpotifyUrl(url: String?) : Pair<String?, String?> {
        val split = url?.split('?')?.get(0)?.split('/')
        val type = split?.get(split.size-2)
        val id = split?.get(split.size-1)
        return Pair(type, id)
    }

    private fun generatePlayifyUrl(type: String?, id: String?, page: Int = 0) : String? {
        if (page > 0) {
            return "https://playify.uc.r.appspot.com/${type}/${page}?id=${id}"
        }
        return "https://playify.uc.r.appspot.com/${type}?id=${id}"
    }

    private fun artistIntent(response: JSONObject) : Intent {
        val artist = response.get("name") as String

        return Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)
            putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist)
            putExtra(SearchManager.QUERY, "${artist}")
        }
    }

    private fun albumIntent(response: JSONObject) : Intent {
        val album = response.get("name") as String
        val artists = response.get("artists") as JSONArray
        val firstArtist = artists[0] as String

        return Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)
            putExtra(MediaStore.EXTRA_MEDIA_ALBUM, album)
            putExtra(MediaStore.EXTRA_MEDIA_ARTIST, firstArtist)
            putExtra(SearchManager.QUERY, "${album} - ${firstArtist}")
        }
    }

    private fun playlistIntent(response: JSONObject) : Intent {
        val playlistImage = response.getJSONArray("images").getJSONObject(0).getString("url")
        val playlistName = response.getString("name")
        val playlistDescription = response.getString("description")
        val playlistCreator = response.getString("creator")
        val tracks = response.getJSONArray("tracks")

        return Intent(this, PlaylistViewActivity::class.java).apply {
            putExtra(getString(R.string.EXTRA_PLAYLIST_IMAGE), playlistImage)
            putExtra(getString(R.string.EXTRA_PLAYLIST_NAME), playlistName)
            putExtra(getString(R.string.EXTRA_PLAYLIST_DESCRIPTION), playlistDescription)
            putExtra(getString(R.string.EXTRA_PLAYLIST_CREATOR), playlistCreator)
            putExtra(getString(R.string.EXTRA_TRACKS), tracks.toString())
        }
    }
}
