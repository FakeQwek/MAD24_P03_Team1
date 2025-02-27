package sg.edu.np.mad.inkwell;

public class Book {
    private String id;
    private String title;
    private String author;
    private String status; // "To Read", "Reading", "Completed"
    private String notes;
    private String coverUrl;
    private int totalPages;
    private int currentPage;

    public Book() { }

    public Book(String id, String title, String author, String status, String notes, String coverUrl, int totalPages, int currentPage) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = status;
        this.notes = notes;
        this.coverUrl = coverUrl;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getProgress() {
        return totalPages == 0 ? 0 : (currentPage * 100) / totalPages;
    }
}
