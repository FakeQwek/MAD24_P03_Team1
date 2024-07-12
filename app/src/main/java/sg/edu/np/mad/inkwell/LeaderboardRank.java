package sg.edu.np.mad.inkwell;

import android.graphics.Bitmap;

public class LeaderboardRank {
    public String id;

    public String email;

    public int points;

    public Bitmap bitmap;

    public String getId() { return this.id; }

    public String getEmail() { return this.email; }

    public int getPoints() { return this.points; }

    public Bitmap getBitmap() { return this.bitmap; }

    public LeaderboardRank(String id, String email, int points, Bitmap bitmap) {
        this.id = id;
        this.email = email;
        this.points = points;
        this.bitmap = bitmap;
    }
}
