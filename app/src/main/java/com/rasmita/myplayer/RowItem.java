package com.rasmita.myplayer;

public class RowItem {

    private int imageId;
    private String title;
    private String desc;

    public RowItem(Integer image, String title, String description) {
        this.imageId = image;
        this.title = title;
        this.desc = description;

    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
