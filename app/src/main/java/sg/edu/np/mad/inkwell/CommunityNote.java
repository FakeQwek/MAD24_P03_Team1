package sg.edu.np.mad.inkwell;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class CommunityNote {
    public String id;

    public String title;

    public String body;

    public String email;

    public String uid;

    public Bitmap bitmap;

    public String dateCreated;

    public ArrayList<String> likes = new ArrayList<>();

    public String getId() { return this.id; }

    public String getTitle() { return this.title; }

    public String getBody() { return this.body; }

    public String getEmail() { return this.email; }

    public ArrayList<String> getLikes() { return this.likes; }

    public String getUid() { return this.uid; }

    public Bitmap getBitmap() { return this.bitmap; }

    public String getDateCreated() { return this.dateCreated; }

    public void addLike(String uid) {
        this.likes.add(uid);
    }

    public CommunityNote(String id, String title, String body, String email, String uid, Bitmap bitmap, String dateCreated) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.email = email;
        this.uid = uid;
        this.bitmap = bitmap;
        this.dateCreated = dateCreated;
    }
}
