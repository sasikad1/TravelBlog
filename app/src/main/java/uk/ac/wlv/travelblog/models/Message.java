package uk.ac.wlv.travelblog.models;

public class Message {
    private int id;
    private int userId;
    private String title;
    private String content;
    private String imagePath;
    private String createdDate;
    private String updatedDate;

    public Message() {}

    public Message(int id, int userId, String title, String content,
                   String imagePath, String createdDate, String updatedDate) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.imagePath = imagePath;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImagePath() { return imagePath; }
    public String getCreatedDate() { return createdDate; }
    public String getUpdatedDate() { return updatedDate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
    public void setUpdatedDate(String updatedDate) { this.updatedDate = updatedDate; }
}