package com.example.playify

import android.app.SearchManager
import android.content.Intent
import android.provider.MediaStore
import org.json.JSONArray
import org.json.JSONObject

fun trackIntent(response: JSONObject) : Intent {
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
