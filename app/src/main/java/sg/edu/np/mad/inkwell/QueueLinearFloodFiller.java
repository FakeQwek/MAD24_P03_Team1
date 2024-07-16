package sg.edu.np.mad.inkwell;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import java.util.LinkedList;
import java.util.Queue;

public class QueueLinearFloodFiller {
    private Bitmap bitmap;
    private int targetColor;
    private int replacementColor;
    private int tolerance = 0; // Tolerance for color difference

    public QueueLinearFloodFiller(Bitmap bitmap, int targetColor, int replacementColor) {
        this.bitmap = bitmap;
        this.targetColor = targetColor;
        this.replacementColor = replacementColor;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public void floodFill(int x, int y) {
        if (targetColor == replacementColor) return;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int startColor = bitmap.getPixel(x, y);

        if (Math.abs(Color.red(startColor) - Color.red(replacementColor)) <= tolerance &&
                Math.abs(Color.green(startColor) - Color.green(replacementColor)) <= tolerance &&
                Math.abs(Color.blue(startColor) - Color.blue(replacementColor)) <= tolerance) {
            return; // The color is similar to the target color, no need to flood fill
        }

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            int px = p.x;
            int py = p.y;

            if (px < 0 || px >= width || py < 0 || py >= height) continue;

            int pixelColor = bitmap.getPixel(px, py);
            if (isColorSimilar(pixelColor, startColor)) {
                bitmap.setPixel(px, py, replacementColor);
                queue.add(new Point(px + 1, py));
                queue.add(new Point(px - 1, py));
                queue.add(new Point(px, py + 1));
                queue.add(new Point(px, py - 1));
            }
        }
    }

    private boolean isColorSimilar(int color1, int color2) {
        return Math.abs(Color.red(color1) - Color.red(color2)) <= tolerance &&
                Math.abs(Color.green(color1) - Color.green(color2)) <= tolerance &&
                Math.abs(Color.blue(color1) - Color.blue(color2)) <= tolerance;
    }
}