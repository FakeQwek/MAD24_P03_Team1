package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Stack;

public class DrawView extends View {

    private Path path;
    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;
    private Stack<Bitmap> undoStack = new Stack<>();
    private Stack<Bitmap> redoStack = new Stack<>();
    private int currentColor;
    private boolean isFillModeOn = false;
    private boolean isSelectionModeOn = false;

    private Rect selectionBox;
    private Paint selectionPaint;
    private Paint selectionFillPaint;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        currentColor = Color.BLACK;

        selectionBox = new Rect();
        selectionPaint = new Paint();
        selectionPaint.setColor(Color.RED);
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(5);
        selectionFillPaint = new Paint();
    }

    public void setColor(int color) {
        currentColor = color;
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

        if (isSelectionModeOn) {
            canvas.drawRect(selectionBox, selectionPaint);
            if (isFillModeOn) {
                canvas.drawRect(selectionBox, selectionFillPaint);
            }
        }
    }

    private void startTouch(float x, float y) {
        if (isSelectionModeOn) {
            selectionBox.set((int) x, (int) y, (int) x, (int) y);
        } else {
            path.moveTo(x, y);
        }
    }

    private void moveTouch(float x, float y) {
        if (isSelectionModeOn) {
            selectionBox.right = (int) x;
            selectionBox.bottom = (int) y;
        } else {
            path.lineTo(x, y);
        }
    }

    private void upTouch() {
        if (!isSelectionModeOn) {
            if (paint.getXfermode() != null) {
                canvas.drawPath(path, paint);
            } else {
                canvas.drawPath(path, paint);
            }
            path.reset();
        }
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
                if (isFillModeOn && !isSelectionModeOn) {
                    int targetColor = bitmap.getPixel((int) x, (int) y);
                    QueueLinearFloodFiller filler = new QueueLinearFloodFiller(bitmap, targetColor, currentColor);
                    filler.setTolerance(10);
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
            paint.setColor(Color.TRANSPARENT);
        } else {
            paint.setXfermode(null);
            paint.setColor(currentColor);
        }
    }

    public void setFillMode(boolean isFillModeOn) {
        this.isFillModeOn = isFillModeOn;
    }

    public void setSelectionMode(boolean selectionMode) {
        isSelectionModeOn = selectionMode;
        if (!selectionMode) {
            selectionBox.setEmpty();
        }
        invalidate();
    }

    public void deleteSelection() {
        if (selectionBox.isEmpty()) return;

        saveStateForUndo();
        Paint clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(selectionBox, clearPaint);
        selectionBox.setEmpty();
        invalidate();
    }

    public boolean isSelectionMode() {
        return isSelectionModeOn;
    }
}