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

package email.schaal.ocreader.api.json;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import email.schaal.ocreader.database.model.Item;
import email.schaal.ocreader.util.StringUtils;

/**
 * TypeAdapter to deserialize the JSON response for feed Items.
 */
public class ItemTypeAdapter extends JsonAdapter<Item> {
    private final static String TAG = ItemTypeAdapter.class.getName();

    @Override
    public void toJson(@NonNull JsonWriter out, Item item) throws IOException {
        out.beginObject();

        out.name(Item.ID).value(item.getId());
        out.name(Item.CONTENT_HASH).value(item.getContentHash());

        if(item.isUnreadChanged())
            out.name("isUnread").value(item.isUnread());

        if(item.isStarredChanged())
            out.name("isStarred").value(item.isStarred());

        out.endObject();
    }

    @Override
    public Item fromJson(@NonNull JsonReader in) throws IOException {
        if (in.peek() == JsonReader.Token.NULL) {
            in.nextNull();
            return null;
        }

        final NullableJsonReader reader = new NullableJsonReader(in);

        final Item.Builder builder = new Item.Builder();

        in.beginObject();

        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "id":
                    builder.setId(in.nextLong());
                    break;
                case "guid":
                    builder.setGuid(in.nextString());
                    break;
                case "guidHash":
                    builder.setGuidHash(in.nextString());
                    break;
                case "url":
                    builder.setUrl(reader.nextString());
                    break;
                case "title":
                    builder.setTitle(StringUtils.cleanString(in.nextString()));
                    break;
                case "author":
                    builder.setAuthor(StringUtils.emptyToNull(in.nextString()));
                    break;
                case "pubDate":
                    builder.setPubDate(new Date(in.nextLong() * 1000));
                    break;
                case "body":
                    builder.setBody(in.nextString());
                    break;
                case "enclosureMime":
                    if(in.peek() != JsonReader.Token.NULL)
                        builder.setEnclosureMime(StringUtils.emptyToNull(in.nextString()));
                    else
                        in.skipValue();
                    break;
                case "enclosureLink":
                    if(in.peek() != JsonReader.Token.NULL)
                        builder.setEnclosureLink(StringUtils.emptyToNull(in.nextString()));
                    else
                        in.skipValue();
                    break;
                case "publishedAt":
                    builder.setPubDate(parseDate(in.nextString()));
                    break;
                case "updatedAt":
                    builder.setUpdatedAt(parseDate(in.nextString()));
                    break;
                case "enclosure":
                    parseEnclosure(reader, builder);
                    break;
                case "feedId":
                    builder.setFeedId(in.nextLong());
                    break;
                case "isUnread":
                case "unread":
                    builder.setUnread(in.nextBoolean());
                    break;
                case "starred":
                case "isStarred":
                    builder.setStarred(in.nextBoolean());
                    break;
                case "lastModified":
                    builder.setLastModified(in.nextLong());
                    break;
                case "rtl":
                    in.skipValue();
                    break;
                case "fingerprint":
                    builder.setFingerprint(reader.nextString());
                    break;
                case "contentHash":
                    // ignore for now, old items don't have this set yet.
                    //item.setContentHash(in.nextString());
                    in.skipValue();
                    break;
                case "updatedDate":
                    if(in.peek() == JsonReader.Token.NUMBER)
                        builder.setUpdatedAt(new Date(in.nextLong() * 1000));
                    else
                        in.skipValue();
                    break;
                default:
                    Log.w(TAG, "Unknown value in item json: " + name);
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return builder.build();
    }

    private void parseEnclosure(NullableJsonReader reader, Item.Builder builder) throws IOException {
        reader.in.beginObject();
        while(reader.in.hasNext()) {
            switch (reader.in.nextName()) {
                case "mimeType":
                    builder.setEnclosureMime(reader.nextString());
                    break;
                case "url":
                    builder.setEnclosureLink(reader.nextString());
                    break;
            }
        }
        reader.in.endObject();
    }

    private final static DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US);

    @Nullable
    private Date parseDate(String source) {
        try {
            return iso8601Format.parse(source);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse date: " + source, e);
            return null;
        }
    }
}
