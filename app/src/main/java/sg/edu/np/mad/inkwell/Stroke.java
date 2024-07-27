package sg.edu.np.mad.inkwell;

import android.graphics.Path;

public class Stroke {

    public final int color;
    public final int strokeWidth;
    public final Path path;

    public Stroke(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}