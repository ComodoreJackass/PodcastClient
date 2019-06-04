package hr.tvz.android.podcastclient.Model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import java.util.UUID;

import hr.tvz.android.podcastclient.MyDatabase;

@Table(database = MyDatabase.class)
public class Result {

    @Column
    @PrimaryKey
    private String id;
    @Column
    private String rss;
    @Column
    private String audio;
    @Column
    private String image;
    @Column
    private String thumbnail;
    @Column
    private String title_original;
    @Column
    private String description_original;

    @Column
    public String description;
    @Column
    public Boolean mExpanded;
    @Column
    public Boolean mBookmarked;


    public void reduceDescription() {
        if (description_original.length() > 60) {
            description_original = description_original.substring(0, 60) + "...";
        }
        mExpanded = false;
    }

    public void restoreDescription() {
        description_original = description;
        mExpanded = true;
    }

    public String getRss() {
        return rss;
    }

    public String getAudio() {
        return audio;
    }

    public String getImage() {
        return image;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getTitle_original() {
        return title_original;
    }

    public String getDescription_original() {
        return description_original;
    }


    public void setRss(String rss) {
        this.rss = rss;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setTitle_original(String title_original) {
        this.title_original = title_original;
    }

    public void setDescription_original(String description_original) {
        this.description_original = description_original;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getmExpanded() {
        return mExpanded;
    }

    public void setmExpanded(Boolean mExpanded) {
        this.mExpanded = mExpanded;
    }

    public Boolean getmBookmarked() {
        return mBookmarked;
    }

    public void setmBookmarked(Boolean mBookmarked) {
        this.mBookmarked = mBookmarked;
    }
}
