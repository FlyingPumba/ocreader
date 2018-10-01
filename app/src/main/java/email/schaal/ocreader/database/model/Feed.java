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
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import email.schaal.ocreader.R;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * RealmObject representing a Feed
 */
@SuppressWarnings("unused")
@RealmClass
public class Feed implements RealmModel, TreeItem, Insertable, Parcelable {
    @PrimaryKey
    private long id;

    private String url;
    private String name;
    private String link;
    private String faviconLink;
    public static final String FAVICON_LINK = "faviconLink";

    private Date added;
    private Long folderId;
    public static final String FOLDER_ID = "folderId";

    @Nullable
    private Folder folder;
    public static final String FOLDER = "folder";

    private int unreadCount;
    public static final String UNREAD_COUNT = "unreadCount";

    /**
     * Not part of the JSON response, calculated in-app
     */
    private int starredCount;
    public static final String STARRED_COUNT = "starredCount";

    /**
     * @since 5.1.0
     */
    private int ordering;
    /**
     * @since 6.0.3
     */
    private boolean pinned;
    public static final String PINNED = "pinned";

    /**
     * @since 8.6.0
     */
    private int updateErrorCount;
    public static final String UPDATE_ERROR_COUNT = "updateErrorCount";

    /**
     * @since 8.6.0
     */
    private String lastUpdateError;
    public static final String LAST_UPDATE_ERROR = "lastUpdateError";

    public Feed() {
    }

    public Feed(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFaviconLink() {
        return faviconLink;
    }

    public void setFaviconLink(String faviconLink) {
        this.faviconLink = faviconLink;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCount(Realm realm) {
        return getUnreadCount();
    }

    @Override
    public boolean canLoadMore() {
        return true;
    }

    @Override
    public List<Feed> getFeeds(Realm realm, boolean onlyUnread) {
        return Collections.singletonList(this);
    }

    @Override
    public List<Item> getItems(Realm realm, boolean onlyUnread) {
        final RealmQuery<Item> query = realm.where(Item.class).equalTo(Item.FEED_ID, id);
        if(onlyUnread)
            query.equalTo(Item.UNREAD, true);
        return query.findAll();
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void incrementUnreadCount(int increment) {
        unreadCount += increment;
    }

    public int getStarredCount() {
        return starredCount;
    }

    public void setStarredCount(int starredCount) {
        this.starredCount = starredCount;
    }

    public void incrementStarredCount(int increment) {
        starredCount += increment;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public int getUpdateErrorCount() {
        return updateErrorCount;
    }

    public void setUpdateErrorCount(int updateErrorCount) {
        this.updateErrorCount = updateErrorCount;
    }

    public String getLastUpdateError() {
        return lastUpdateError;
    }

    public void setLastUpdateError(String lastUpdateError) {
        this.lastUpdateError = lastUpdateError;
    }

    public void setFolder(@Nullable Folder folder) {
        if(folder != null)
            this.folderId = folder.getId();
        else
            this.folderId = 0L;
        this.folder = folder;
    }

    @Nullable
    public Folder getFolder() {
        return folder;
    }

    public String getFolderTitle(Context context) {
        if(folder == null) {
            if(folderId == 0)
                return context.getString(R.string.root_folder);
            else
                return null;
        } else
            return folder.getName();
    }

    public boolean isConsideredFailed() {
        return updateErrorCount >= 50;
    }

    @Override
    public void insert(Realm realm) {
        if(getName() != null) {
            setFolder(Folder.getOrCreate(realm, folderId));
            realm.insertOrUpdate(this);
        }
    }

    @Override
    public void delete(Realm realm) {
        realm.where(Item.class).equalTo(Item.FEED_ID, getId()).findAll().deleteAllFromRealm();
        RealmObject.deleteFromRealm(this);
    }

    @Nullable
    public static Feed get(Realm realm, long id) {
        return realm.where(Feed.class).equalTo(Feed.ID, id).findFirst();
    }

    /**
     * Return the feed with id feedId, or insert a new (temporary) feed into the database.
     * @param realm Database to operate on
     * @param feedId id of the feed
     * @return Feed with id feedId (either from the database or a newly created one)
     */
    public static Feed getOrCreate(Realm realm, long feedId) {
        Feed feed = Feed.get(realm, feedId);
        if(feed == null) {
            feed = realm.createObject(Feed.class, feedId);
        }
        return feed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.url);
        dest.writeString(this.name);
        dest.writeString(this.link);
        dest.writeString(this.faviconLink);
        dest.writeLong(this.added != null ? this.added.getTime() : -1);
        dest.writeValue(this.folderId);
        dest.writeParcelable(this.folder, flags);
        dest.writeInt(this.unreadCount);
        dest.writeInt(this.starredCount);
        dest.writeInt(this.ordering);
        dest.writeByte(this.pinned ? (byte) 1 : (byte) 0);
        dest.writeInt(this.updateErrorCount);
        dest.writeString(this.lastUpdateError);
    }

    protected Feed(Parcel in) {
        this.id = in.readLong();
        this.url = in.readString();
        this.name = in.readString();
        this.link = in.readString();
        this.faviconLink = in.readString();
        long tmpAdded = in.readLong();
        this.added = tmpAdded == -1 ? null : new Date(tmpAdded);
        this.folderId = (Long) in.readValue(Long.class.getClassLoader());
        this.folder = in.readParcelable(Folder.class.getClassLoader());
        this.unreadCount = in.readInt();
        this.starredCount = in.readInt();
        this.ordering = in.readInt();
        this.pinned = in.readByte() != 0;
        this.updateErrorCount = in.readInt();
        this.lastUpdateError = in.readString();
    }

    public static final Parcelable.Creator<Feed> CREATOR = new Parcelable.Creator<Feed>() {
        @Override
        public Feed createFromParcel(Parcel source) {
            return new Feed(source);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };
}
