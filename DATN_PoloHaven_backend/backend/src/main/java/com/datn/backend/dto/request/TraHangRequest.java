package com.datn.backend.dto.request;

public class TraHangRequest {
    private String lyDo;
    private java.util.List<String> mediaUrls; // URLs (returned from upload endpoint) for images/videos

    // Constructors
    public TraHangRequest() {}

    public TraHangRequest(String lyDo) {
        this.lyDo = lyDo;
    }

    public TraHangRequest(String lyDo, java.util.List<String> mediaUrls) {
        this.lyDo = lyDo;
        this.mediaUrls = mediaUrls;
    }

    // Getters and Setters
    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public java.util.List<String> getMediaUrls() {
        return mediaUrls;
    }

    public void setMediaUrls(java.util.List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }
}
