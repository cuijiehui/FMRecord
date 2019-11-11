package com.khmer.fm.adnroid_recordd.eventbus;

public class EditTextEventbus {
    private String content;

    public EditTextEventbus(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
