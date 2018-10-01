/*
 * Copyright (C) 2015 Daniel Schaal <daniel@schaal.email>
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

package email.schaal.ocreader.database.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * RealmObject representing a feed Item.
 */
@RealmClass
public class Item implements RealmModel, Insertable, Parcelable {
    private static final String TAG = Item.class.getName();

    @PrimaryKey
    private long id;
    public final static String ID = "id";

    private String guid;

    private String guidHash;
    public final static String GUID_HASH = "guidHash";

    @Nullable
    private String url;

    private String title;
    public final static String TITLE = "title";

    private String author;

    private Date pubDate;
    public static final String PUB_DATE = "pubDate";

    private Date updatedAt;
    public static final String UPDATED_AT = "updatedAt";

    private String body;
    public static final String BODY = "body";

    private String enclosureMime;
    private String enclosureLink;

    private Feed feed;
    public final static String FEED = "feed";

    private long feedId;
    public final static String FEED_ID = "feedId";

    private boolean unread;
    public final static String UNREAD = "unread";

    private boolean unreadChanged = false;
    public final static String UNREAD_CHANGED = "unreadChanged";

    private boolean starred;
    public static final String STARRED = "starred";

    private boolean starredChanged = false;
    public final static String STARRED_CHANGED = "starredChanged";

    private long lastModified;
    public static final String LAST_MODIFIED = "lastModified";

    /** @since 8.4.0 **/
    @Index
    private String fingerprint;
    public static final String FINGERPRINT = "fingerprint";

    @Index
    private String contentHash;
    public static final String CONTENT_HASH = "contentHash";

    @Index
    private boolean active;
    public static final String ACTIVE = "active";

    /**
     * Required by realm
     */
    public Item() {
    }

    public long getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    public String getGuidHash() {
        return guidHash;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getBody() {
        return body;
    }

    public String getEnclosureMime() {
        return enclosureMime;
    }

    public String getEnclosureLink() {
        return enclosureLink;
    }

    public long getFeedId() {
        return feedId;
    }

    public Feed getFeed() {
        return feed;
    }

    private void setFeed(Feed feed) {
        this.feed = feed;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        if(RealmObject.isManaged(this) && this.unread != unread) {
            unreadChanged = !unreadChanged;
            feed.incrementUnreadCount(unread ? 1 : -1);
        }
        this.unread = unread;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        if(RealmObject.isManaged(this) && this.starred != starred) {
            starredChanged = !starredChanged;
            feed.incrementStarredCount(starred ? 1 : -1);
        }
        this.starred = starred;
    }

    public boolean isUnreadChanged() {
        return unreadChanged;
    }

    public void setUnreadChanged(boolean unreadChanged) {
        this.unreadChanged = unreadChanged;
    }

    public boolean isStarredChanged() {
        return starredChanged;
    }

    public void setStarredChanged(boolean starredChanged) {
        this.starredChanged = starredChanged;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getContentHash() {
        return contentHash;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void play(Context context) {
        if(getEnclosureLink() != null) {
            Intent playIntent = new Intent(Intent.ACTION_VIEW);
            playIntent.setData(Uri.parse(getEnclosureLink()));
            context.startActivity(playIntent);
        }
    }

    @Override
    public void insert(Realm realm) {
        if (getTitle() == null) {
            // Reduced item
            final Item fullItem = realm.where(Item.class).equalTo(Item.CONTENT_HASH, getContentHash()).findFirst();
            if (fullItem != null) {
                fullItem.setUnread(isUnread());
                fullItem.setStarred(isStarred());
            } else {
                Log.w(TAG, "Full item is not available");
            }
        } else {
            // new full item
            setFeed(Feed.getOrCreate(realm, getFeedId()));
            realm.insertOrUpdate(this);
        }

    }

    @Override
    public void delete(Realm realm) {
        RealmObject.deleteFromRealm(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.url);
        dest.writeString(this.title);
        dest.writeString(this.author);
        dest.writeLong(this.pubDate != null ? this.pubDate.getTime() : -1);
        dest.writeLong(this.updatedAt != null ? this.updatedAt.getTime() : -1);
        dest.writeString(this.body);
        dest.writeString(this.enclosureLink);
        dest.writeLong(this.feedId);
        dest.writeParcelable(this.feed, flags);
        dest.writeByte(this.unread ? (byte) 1 : (byte) 0);
        dest.writeByte(this.starred ? (byte) 1 : (byte) 0);
        dest.writeLong(this.lastModified);
    }

    protected Item(Parcel in) {
        this.id = in.readLong();
        this.url = in.readString();
        this.title = in.readString();
        this.author = in.readString();
        long tmpPubDate = in.readLong();
        this.pubDate = tmpPubDate == -1 ? null : new Date(tmpPubDate);
        long tmpUpdatedAt = in.readLong();
        this.updatedAt = tmpUpdatedAt == -1 ? null : new Date(tmpUpdatedAt);
        this.body = in.readString();
        this.enclosureLink = in.readString();
        this.feedId = in.readLong();
        this.feed = in.readParcelable(Feed.class.getClassLoader());
        this.unread = in.readByte() != 0;
        this.starred = in.readByte() != 0;
        this.lastModified = in.readLong();
    }

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    private Item(Builder builder) {
        this.id = builder.id;
        this.url = builder.url;
        this.title = builder.title;
        this.author = builder.author;
        this.pubDate = builder.pubDate;

        if(builder.updatedAt != null)
            this.updatedAt = builder.updatedAt;
        else
            this.updatedAt = pubDate;

        this.body = builder.body;
        this.enclosureLink = builder.enclosureLink;
        this.enclosureMime = builder.enclosureMime;
        this.feedId = builder.feedId;
        this.feed = builder.feed;
        this.unread = builder.unread;
        this.starred = builder.starred;
        this.unreadChanged = builder.unreadChanged;
        this.starredChanged = builder.starredChanged;
        this.lastModified = builder.lastModified;
        this.contentHash = builder.contentHash;
        this.guid = builder.guid;
        this.guidHash = builder.guidHash;
        this.fingerprint = builder.fingerprint;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private long id = -1;
        private String url;
        private String title;
        private String author;
        private Date pubDate;
        private Date updatedAt;
        private String body;
        private String enclosureLink;
        private long feedId;
        private Feed feed;
        private boolean unread;
        private boolean starred;
        private boolean unreadChanged;
        private boolean starredChanged;
        private long lastModified;
        private String contentHash;
        private String enclosureMime;
        private String fingerprint;
        private String guid;
        private String guidHash;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public Builder setPubDate(Date pubDate) {
            this.pubDate = pubDate;
            return this;
        }

        public Builder setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setEnclosureLink(String enclosureLink) {
            this.enclosureLink = enclosureLink;
            return this;
        }

        public Builder setFeedId(long feedId) {
            this.feedId = feedId;
            return this;
        }

        public Builder setFeed(Feed feed) {
            this.feed = feed;
            return this;
        }

        public Builder setUnread(boolean unread) {
            this.unread = unread;
            return this;
        }

        public Builder setStarred(boolean starred) {
            this.starred = starred;
            return this;
        }

        public Builder setUnreadChanged(boolean unreadChanged) {
            this.unreadChanged = unreadChanged;
            return this;
        }

        public Builder setStarredChanged(boolean starredChanged) {
            this.starredChanged = starredChanged;
            return this;
        }

        public Builder setLastModified(long lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder setContentHash(String contentHash) {
            this.contentHash = contentHash;
            return this;
        }

        public Builder setEnclosureMime(String enclosureMime) {
            this.enclosureMime = enclosureMime;
            return this;
        }

        public Builder setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
            return this;
        }

        public Builder setGuid(String guid) {
            this.guid = guid;
            return this;
        }

        public Builder setGuidHash(String guidHash) {
            this.guidHash = guidHash;
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }
}
