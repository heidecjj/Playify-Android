package com.example.playify

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

class PlaylistAdapter(private val context: Context, private val tracks: JSONArray, val onItemClick: ((JSONObject) -> Unit)?) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    // holder class to hold reference
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAlbum: ImageView = view.findViewById(R.id.ivAlbum)
        val tvTrack: TextView = view.findViewById(R.id.tvName)
        val tvArtists: TextView = view.findViewById(R.id.tvArtists)

        init {
            view.setOnClickListener { onItemClick?.invoke(tracks.getJSONObject(adapterPosition)) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create view holder to hold reference
        return ViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.playlist_row, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //set values
        val track = tracks[position] as JSONObject
        val images = track.getJSONObject("album").getJSONArray("images")
        val url = if (images.length() > 0) images.getJSONObject(0).getString("url") else ""

        GlideApp.with(context).load(url).placeholder(android.R.drawable.ic_media_play).into(holder.ivAlbum)
        holder.tvTrack.text =  track.getString("name")
        val artists = track.getJSONArray("artists")
        val artistStrings = arrayListOf<String>()
        for (i in 0 until artists.length()) {
            artistStrings.add(artists.getString(i))
        }
        holder.tvArtists.text = artistStrings.joinToString(", ")
    }

    override fun getItemCount(): Int {
        return tracks.length()
    }

}
