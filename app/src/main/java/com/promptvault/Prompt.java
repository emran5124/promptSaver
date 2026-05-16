package com.promptvault;

import com.google.gson.annotations.SerializedName;

public class Prompt {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("category")
    private String category;

    @SerializedName("tags")
    private String tags;

    @SerializedName("aiModel")
    private String aiModel;

    @SerializedName("createdAt")
    private long createdAt;

    @SerializedName("updatedAt")
    private long updatedAt;

    @SerializedName("isFavorite")
    private boolean isFavorite;

    // Constructor for new prompts
    public Prompt(String title, String content, String category, String tags, String aiModel) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.aiModel = aiModel;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isFavorite = false;
    }

    // Full constructor for JSON import
    public Prompt(String id, String title, String content, String category, String tags,
                  String aiModel, long createdAt, long updatedAt, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.aiModel = aiModel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isFavorite = isFavorite;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public String getTags() { return tags; }
    public String getAiModel() { return aiModel; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public boolean isFavorite() { return isFavorite; }

    public void setTitle(String title) { this.title = title; this.updatedAt = System.currentTimeMillis(); }
    public void setContent(String content) { this.content = content; this.updatedAt = System.currentTimeMillis(); }
    public void setCategory(String category) { this.category = category; }
    public void setTags(String tags) { this.tags = tags; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
