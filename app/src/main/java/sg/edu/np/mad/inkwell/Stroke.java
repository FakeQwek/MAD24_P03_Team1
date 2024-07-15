package sg.edu.np.mad.inkwell;

import android.graphics.Path;

public class Stroke {

    public final int color;          // Make final as color does not change
    public final int strokeWidth;   // Make final as strokeWidth does not change
    public final Path path;         // Make final as path does not change

    public Stroke(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}