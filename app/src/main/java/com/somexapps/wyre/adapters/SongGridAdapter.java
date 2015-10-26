package com.somexapps.wyre.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.somexapps.wyre.R;
import com.somexapps.wyre.models.Song;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Copyright 2015 Michael Limb
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SongGridAdapter extends ArrayAdapter<Song> {

    private static class ViewHolder {
        ImageView songImageView;
        TextView songName;
        TextView artistName;
    }

    public SongGridAdapter(Context context, List<Song> songs) {
        super(context, R.layout.song_grid_item, songs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get data item for passed position
        Song song = getItem(position);

        // Create holder for view
        ViewHolder viewHolder;

        // Create new view holder if doesn't exist
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.song_grid_item,
                    parent, false);

            // Set up references to view holder
            viewHolder.songImageView =
                    (ImageView) convertView.findViewById(R.id.song_grid_item_image);
            viewHolder.songName =
                    (TextView) convertView.findViewById(R.id.song_grid_item_song_name);
            viewHolder.artistName =
                    (TextView) convertView.findViewById(R.id.song_grid_item_artist_name);

            // Set tag to view holder
            convertView.setTag(viewHolder);
        } else {
            // Grab reference to view holder
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set up song image
        if (song.getAlbumArtPath() != null) {
            if (song.getAlbumArtPath().startsWith("http")) {
                // Request image
                Picasso
                        .with(getContext())
                        .load(
                                Uri.parse(song.getAlbumArtPath())
                        )
                        .into(viewHolder.songImageView);
            } else {
                viewHolder.songImageView.setImageURI(Uri.parse(song.getAlbumArtPath()));
            }
        }
        viewHolder.songName.setText(song.getTitle());
        viewHolder.artistName.setText(song.getArtist());

        return convertView;
    }
}
