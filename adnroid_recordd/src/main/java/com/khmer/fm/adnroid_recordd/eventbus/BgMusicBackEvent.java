package com.khmer.fm.adnroid_recordd.eventbus;

import com.khmer.fm.adnroid_recordd.record.bean.BgMusicBean;

public class BgMusicBackEvent {
    private int id;
    private BgMusicBean bgMusicBean;

    public BgMusicBackEvent() {
    }

    public BgMusicBackEvent(BgMusicBean bgMusicBean) {
        this.bgMusicBean = bgMusicBean;
    }

    public BgMusicBackEvent(int id, BgMusicBean bgMusicBean) {
        this.id = id;
        this.bgMusicBean = bgMusicBean;
    }

    public BgMusicBean getBgMusicBean() {
        return bgMusicBean;
    }

    public void setBgMusicBean(BgMusicBean bgMusicBean) {
        this.bgMusicBean = bgMusicBean;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
