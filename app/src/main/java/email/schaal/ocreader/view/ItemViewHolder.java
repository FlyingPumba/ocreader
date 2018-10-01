/*
 * Copyright (C) 2015-2016 Daniel Schaal <daniel@schaal.email>
 *
 * This file is part of OCReader.
 *
 * OCReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCReader.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package email.schaal.ocreader.view;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.Date;

import email.schaal.ocreader.Preferences;
import email.schaal.ocreader.R;
import email.schaal.ocreader.database.model.Feed;
import email.schaal.ocreader.database.model.Item;
import email.schaal.ocreader.databinding.ListItemBinding;
import email.schaal.ocreader.util.FaviconLoader;
import email.schaal.ocreader.util.FeedColors;
import email.schaal.ocreader.util.StringUtils;

/**
 * RecyclerView.ViewHolder to display a feed Item.
 */
public class ItemViewHolder extends RecyclerView.ViewHolder implements FaviconLoader.FeedColorsListener {
    private static final String TAG = ItemViewHolder.class.getName();

    private final OnClickListener clickListener;

    @ColorInt private final int defaultFeedTextColor;

    private final ListItemBinding binding;

    private final View[] alphaViews;

    public ItemViewHolder(final ListItemBinding binding, final OnClickListener clickListener) {
        super(binding.getRoot());
        this.clickListener = clickListener;
        this.binding = binding;

        TypedArray typedArray = itemView.getContext().obtainStyledAttributes(new int[] { android.R.attr.textColorSecondary });
        try {
            defaultFeedTextColor = typedArray.getColor(0, 0);
        } finally {
            typedArray.recycle();
        }

        alphaViews = new View[] {
                binding.textViewTitle,
                binding.textViewFeedTitle,
                binding.textViewTime,
                binding.imageviewFavicon,
                binding.imageviewStar,
                binding.play
        };
    }

    public void bindItem(final Item item, final int position, boolean selected) {
        binding.textViewTitle.setText(item.getTitle());

        Feed feed = item.getFeed();
        if(feed != null) {
            binding.textViewFeedTitle.setText(feed.getName());
        } else {
            Log.w(TAG, "Feed == null");
            binding.textViewFeedTitle.setText("");
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(binding.getRoot().getContext());

        final Date date;
        if(Preferences.SORT_FIELD.getString(preferences).equals(Item.UPDATED_AT))
            date = item.getUpdatedAt();
        else
            date = item.getPubDate();

        binding.textViewTime.setText(StringUtils.getTimeSpanString(itemView.getContext(), date));

        binding.textViewFeedTitle.setTextColor(defaultFeedTextColor);

        new FaviconLoader.Builder(binding.imageviewFavicon).build().load(binding.imageviewFavicon.getContext(), feed, this);

        itemView.setOnClickListener(view -> clickListener.onItemClick(item, position));

        itemView.setOnLongClickListener(v -> {
            clickListener.onItemLongClick(item, position);
            return true;
        });

        if(item.getEnclosureLink() != null) {
            binding.play.setVisibility(View.VISIBLE);
            binding.play.setOnClickListener(view -> item.play(itemView.getContext()));
        } else {
            binding.play.setVisibility(View.GONE);
            binding.play.setOnClickListener(null);
        }

        setUnreadState(item.isUnread());
        setStarredState(item.isStarred());
        setSelected(selected);
    }

    private void setSelected(boolean selected) {
        int backgroundResource = R.drawable.item_background;
        if (!selected) {
            int[] attrs = new int[]{R.attr.selectableItemBackground};
            TypedArray typedArray = itemView.getContext().obtainStyledAttributes(attrs);
            backgroundResource = typedArray.getResourceId(0, 0);
            typedArray.recycle();
        }

        itemView.setBackgroundResource(backgroundResource);
    }

    // Workaround for bug pre sdk 19, where the padding is lost after updating the background resource
    // see http://stackoverflow.com/a/15060962
    private void setBackgroundResource(View view, int resource) {
        int[] padding = new int[]{view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingTop(), view.getPaddingBottom()};
        view.setBackgroundResource(resource);
        view.setPadding(padding[0], padding[1], padding[2], padding[3]);
    }

    private void setUnreadState(boolean unread) {
        float alpha = unread ? 1.0f : 0.5f;
        for(View view: alphaViews) {
            view.setAlpha(alpha);
        }
    }

    private void setStarredState(boolean starred) {
        binding.imageviewStar.setVisibility(starred ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onGenerated(@NonNull FeedColors feedColors) {
        binding.textViewFeedTitle.setTextColor(feedColors.getColor(FeedColors.Type.TEXT, defaultFeedTextColor));
    }

    @Override
    public void onStart() {

    }

    public interface OnClickListener {
        void onItemClick(Item item, int position);
        void onItemLongClick(Item item, int position);
    }
}
