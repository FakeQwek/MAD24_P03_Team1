package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Stack;  // Make sure this import is included

public class DrawView extends View {

    private Path path;
    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;
    private Stack<Bitmap> undoStack = new Stack<>();
    private Stack<Bitmap> redoStack = new Stack<>();
    private int currentColor;  // Added this line to define currentColor
    private boolean isFillModeOn = false;  // Add a flag for fill mode

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        currentColor = Color.BLACK;  // Initialize currentColor to default color
    }

    public void setColor(int color) {
        currentColor = color;  // Update currentColor when color is set
        paint.setColor(color);
    }

    public void setStrokeWidth(float width) {
        paint.setStrokeWidth(width);
    }

    public void init(int height, int width) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawPath(path, paint);
    }

    private void startTouch(float x, float y) {
        path.moveTo(x, y);
    }

    private void moveTouch(float x, float y) {
        path.lineTo(x, y);
    }

    private void upTouch() {
        // Draw the path with the current paint mode
        if (paint.getXfermode() != null) {
            // If eraser mode is on, the path is cleared
            canvas.drawPath(path, paint);
        } else {
            // Otherwise, draw the path as normal
            canvas.drawPath(path, paint);
        }
        path.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                saveStateForUndo();
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                if (isFillModeOn) {
                    int targetColor = bitmap.getPixel((int) x, (int) y);
                    QueueLinearFloodFiller filler = new QueueLinearFloodFiller(bitmap, targetColor, currentColor);
                    filler.setTolerance(10);  // Set tolerance value as needed
                    filler.floodFill((int) x, (int) y);
                    invalidate();
                }
                invalidate();
                break;
        }
        return true;
    }

    public void clear() {
        path.reset();
        saveStateForUndo();
        canvas.drawColor(Color.WHITE);
        invalidate();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            saveStateForRedo();
            bitmap = undoStack.pop();
            canvas.setBitmap(bitmap);
            invalidate();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            saveStateForUndo();
            bitmap = redoStack.pop();
            canvas.setBitmap(bitmap);
            invalidate();
        }
    }

    private void saveStateForUndo() {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap);
        undoStack.push(newBitmap);
    }

    private void saveStateForRedo() {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap);
        redoStack.push(newBitmap);
    }

    public Bitmap save() {
        return bitmap;
    }

    public void setEraserMode(boolean isEraserOn) {
        if (isEraserOn) {
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            paint.setColor(Color.TRANSPARENT);  // Set the eraser color to transparent
        } else {
            paint.setXfermode(null);
            paint.setColor(currentColor);  // Reset color to the current drawing color
        }
    }

    public void setFillMode(boolean isFillModeOn) {
        this.isFillModeOn = isFillModeOn;
    }
}