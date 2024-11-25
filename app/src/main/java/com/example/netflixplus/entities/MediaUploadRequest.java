package com.example.netflixplus.entities;


public class MediaUploadRequest {
    private String title;
    private String description;
    private String genre;
    private Integer year;
    private String publisher;
    private Integer duration;

    public MediaUploadRequest(String title, String description, String genre, Integer year, String publisher, Integer duration) {
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.year = year;
        this.publisher = publisher;
        this.duration = duration;
    }
}
