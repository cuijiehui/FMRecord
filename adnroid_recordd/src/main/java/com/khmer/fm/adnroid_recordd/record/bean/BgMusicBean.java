package com.khmer.fm.adnroid_recordd.record.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author : created by cui on 2019/10/10 11:46
 * 接受rn传递过来的bean
 */
public class BgMusicBean implements Parcelable {
    private String cover;
    private String khmerTitle;
    private String title;
    private String id;
    private String path;
    private String localUrl;
    private float duration;
    private String englishTitle;
    private String author ;

    public BgMusicBean() {
    }

    public BgMusicBean(String cover, String khmerTitle, String title, String id, String path, String localUrl, float duration, String englishTitle, String author) {
        this.cover = cover;
        this.khmerTitle = khmerTitle;
        this.title = title;
        this.id = id;
        this.path = path;
        this.localUrl = localUrl;
        this.duration = duration;
        this.englishTitle = englishTitle;
        this.author = author;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getKhmerTitle() {
        return khmerTitle;
    }

    public void setKhmerTitle(String khmerTitle) {
        this.khmerTitle = khmerTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public void setEnglishTitle(String englishTitle) {
        this.englishTitle = englishTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.cover);
        dest.writeString(this.khmerTitle);
        dest.writeString(this.title);
        dest.writeString(this.id);
        dest.writeString(this.path);
        dest.writeString(this.localUrl);
        dest.writeFloat(this.duration);
        dest.writeString(this.englishTitle);
        dest.writeString(this.author);
    }

    protected BgMusicBean(Parcel in) {
        this.cover = in.readString();
        this.khmerTitle = in.readString();
        this.title = in.readString();
        this.id = in.readString();
        this.path = in.readString();
        this.localUrl = in.readString();
        this.duration = in.readFloat();
        this.englishTitle = in.readString();
        this.author = in.readString();
    }

    public static final Creator<BgMusicBean> CREATOR = new Creator<BgMusicBean>() {
        @Override
        public BgMusicBean createFromParcel(Parcel source) {
            return new BgMusicBean(source);
        }

        @Override
        public BgMusicBean[] newArray(int size) {
            return new BgMusicBean[size];
        }
    };

    @Override
    public String toString() {
        return "BgMusicBean{" +
                "cover='" + cover + '\'' +
                ", khmerTitle='" + khmerTitle + '\'' +
                ", title='" + title + '\'' +
                ", id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", localUrl='" + localUrl + '\'' +
                ", duration=" + duration +
                ", englishTitle='" + englishTitle + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}
