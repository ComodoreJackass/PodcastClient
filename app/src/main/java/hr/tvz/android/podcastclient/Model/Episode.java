package hr.tvz.android.podcastclient.Model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import hr.tvz.android.podcastclient.MyDatabase;

@Table(database = MyDatabase.class)
public class Episode {
    @Column
    @PrimaryKey
    private String id;
    @Column
    private String audio;
    @Column
    private String image;
    @Column
    private String title;
    @Column
    private String thumbnail;
    @Column
    private String description;
    @Column
    private String rss;
    @Column
    private int audio_length_sec;


    @Column
    public String descriptionOriginal;
    @Column
    public Boolean mExpanded;
    @Column
    public Boolean mPlaying;


    public void reduceDescription() {
        if (description.length() > 60) {
            description = description.substring(0, 60) + "...";
        }
        mExpanded = false;
    }

    public void restoreDescription() {
        description = descriptionOriginal;
        mExpanded = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRss() {
        return rss;
    }

    public void setRss(String rss) {
        this.rss = rss;
    }

    public int getAudio_length_sec() {
        return audio_length_sec;
    }

    public void setAudio_length_sec(int audio_length_sec) {
        this.audio_length_sec = audio_length_sec;
    }
}
