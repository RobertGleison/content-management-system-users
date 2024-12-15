package com.example.netflixplus.entities;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


public class MediaResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String title;
    private String description;
    private String genre;
    private Integer year;
    private String publisher;
    private Integer duration;
    private Map<String, String> bucketPaths;
    private LocalDateTime uploadTimestamp;
    private String filePath = null;

    public MediaResponseDTO() {
    }

    public MediaResponseDTO(UUID id, String title, String description, String genre, Integer year, String publisher, Integer duration, Map<String, String> bucketPaths, LocalDateTime uploadTimestamp, byte[] thumbnail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.year = year;
        this.publisher = publisher;
        this.duration = duration;
        this.bucketPaths = bucketPaths;
        this.uploadTimestamp = uploadTimestamp;
    }

    public MediaResponseDTO(UUID id, String title, String description, String genre, Integer year, String publisher, Integer duration, Map<String, String> bucketPaths, LocalDateTime uploadTimestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.year = year;
        this.publisher = publisher;
        this.duration = duration;
        this.bucketPaths = bucketPaths;
        this.uploadTimestamp = uploadTimestamp;
    }

    public String getId() {
        return id.toString();
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }


    public Map<String, String> getBucketPaths(){
        return  bucketPaths;
    }

    public String getDescription() {
        return description;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getGenre() {
        return genre;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}


