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
        "track" to { r : JSONObject -> trackIntent(r) },
        "artist" to { r : JSONObject -> artistIntent(r) },
        "album" to { r : JSONObject -> albumIntent(r) }
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
                    handleMedia(type, response)
                    finish()
                },
                Response.ErrorListener { _ -> })

            val queue = Volley.newRequestQueue(this)
            queue.add(request)
        }
    }

    private fun handleMedia(type: String?, response: JSONObject) {
        var i = mediaIntents[type]?.invoke(response) as Intent
        try {
            startActivity(i)
            return
        } catch (_: ActivityNotFoundException) { }

        i.action = MediaStore.INTENT_ACTION_MEDIA_SEARCH
        try {
            startActivity(i)
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
    }

    private fun parseSpotifyUrl(url: String?) : Pair<String?, String?> {
        val split = url?.split('?')?.get(0)?.split('/')
        val type = split?.get(split.size-2)
        val id = split?.get(split.size-1)
        return Pair(type, id)
    }

    private fun generatePlayifyUrl(type: String?, id: String?) : String? {
        return "https://playify.uc.r.appspot.com/${type}?id=${id}"
    }

    private fun trackIntent(response: JSONObject) : Intent {
        val title = response.get("name") as String
        val artists = response.get("artists") as JSONArray
        val firstArtist = artists[0] as String
        val album = (response.get("album") as JSONObject).get("name") as String

        return Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/audio")
            putExtra(MediaStore.EXTRA_MEDIA_TITLE, title)
            putExtra(MediaStore.EXTRA_MEDIA_ARTIST, firstArtist)
            putExtra(MediaStore.EXTRA_MEDIA_ALBUM, album)
            putExtra(SearchManager.QUERY, "${title} - ${firstArtist}")
        }
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
}
