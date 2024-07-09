package sg.edu.np.mad.inkwell;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;

public class File {
    // File class attributes
    public String title;

    public String body;

    public int id;

    public String type;

    public DocumentReference docRef;

    public Date dateCreated;

    public Date dateUpdated;

    // File class get methods
    public String getTitle() { return this.title; }

    public String getBody() { return this.body; }

    public int getId() { return this.id; }

    public String getType() { return this.type; }

    public DocumentReference getDocRef() { return this.docRef; }

    // File class set methods
    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getDateCreated() { return this.dateCreated; }

    public Date getDateUpdated() { return this.dateUpdated; }

    public void setDateUpdated(Date dateUpdated) { this.dateUpdated = dateUpdated; }

    // File class constructor
    public File(String title, String body, int id, String type, DocumentReference docRef, Date dateCreated, Date dateUpdated) {
        this.title = title;
        this.body = body;
        this.id = id;
        this.type = type;
        this.docRef = docRef;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }
}
