package sg.edu.np.mad.inkwell;

import android.graphics.Bitmap;

public class CommunityNote {
    public String id;

    public String title;

    public String body;

    public String email;

    public String uid;

    public Bitmap bitmap;

    public String getId() { return this.id; }

    public String getTitle() { return this.title; }

    public String getBody() { return this.body; }

    public String getEmail() { return this.email; }

    public String getUid() { return this.uid; }

    public Bitmap getBitmap() { return this.bitmap; }

    public CommunityNote(String id, String title, String body, String email, String uid, Bitmap bitmap) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.email = email;
        this.uid = uid;
        this.bitmap = bitmap;
    }
}
