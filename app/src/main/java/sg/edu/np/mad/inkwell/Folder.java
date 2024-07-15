package sg.edu.np.mad.inkwell;

import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;
import java.util.Date;

public class Folder {
    // Folder class attributes
    public String title;

    public String body;

    public int id;

    public String type;

    public CollectionReference colRef;

    public Date dateCreated;

    public Date dateUpdated;

    public String bookmarkColour;

    // Folder class get methods
    public String getTitle() { return this.title; }

    public String getBody() { return this.body; }

    public int getId() { return this.id; }

    public String getType() { return this.type; }

    public CollectionReference getColRef() { return this.colRef; }

    public Date getDateCreated() { return this.dateCreated; }

    public Date getDateUpdated() { return this.dateUpdated; }

    public String getBookmarkColour() { return this.bookmarkColour; }

    public void setDateUpdated(Date dateUpdated) { this.dateUpdated = dateUpdated; }

    public void setBookmarkColour(String bookmarkColour) { this.bookmarkColour = bookmarkColour; }

    // Folder class constructor
    public Folder(String title, String body, int id, String type, CollectionReference colRef, Date dateCreated, Date dateUpdated, String bookmarkColour) {
        this.title = title;
        this.body = body;
        this.id = id;
        this.type = type;
        this.colRef = colRef;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
        this.bookmarkColour = bookmarkColour;
    }

}
