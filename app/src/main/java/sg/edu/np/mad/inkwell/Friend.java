package sg.edu.np.mad.inkwell;

import android.graphics.Bitmap;

public class Friend {
    public int id;

    public String uid;

    public String email;

    public Bitmap bitmap;

    public int getId() { return this.id; }

    public String getUid() { return this.uid; }

    public String getEmail() { return this.email; }

    public Bitmap getBitmap() { return this.bitmap; }

    public Friend(int id, String uid, String email, Bitmap bitmap) {
        this.id = id;
        this.uid = uid;
        this.email = email;
        this.bitmap = bitmap;
    }
}
