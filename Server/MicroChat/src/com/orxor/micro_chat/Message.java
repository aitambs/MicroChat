package com.orxor.micro_chat;

public class Message implements Comparable<Message>{
    private long id;
    private String from;
    private String content;
    private String extraContent;
    private boolean isPhoto;

    public Message(String from, String content, boolean isPhoto) {
        this.from = from;
        this.content = content;
        this.isPhoto = isPhoto;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExtraContent() {
        return extraContent;
    }

    public void setExtraContent(String extraContent) {
        this.extraContent = extraContent;
    }

    public boolean isPhoto() {
        return isPhoto;
    }

    public void setPhoto(boolean photo) {
        isPhoto = photo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof Message){
            Message other = (Message)obj;
            return id == other.id && from.equals(other.from)
                    && content.equals(other.content)
                    && (extraContent==null ? other.extraContent==null : extraContent.equals(other.extraContent))
                    && isPhoto==other.isPhoto;
        } else return false;
    }

    @Override
    public int compareTo(Message o) {
        return Long.compare(id,o.id);
    }
}
