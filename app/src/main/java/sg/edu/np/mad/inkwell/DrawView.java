package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawView extends View {

    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path mPath;
    private Paint mPaint;
    private ArrayList<Stroke> paths = new ArrayList<>();
    private int currentColor;
    private float strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAlpha(0xff);
        mPaint.setColor(Color.BLACK); // Default color to black
        mPaint.setStrokeWidth(0.5f); // Default stroke width
    }

    public void init(int height, int width) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);  // Set initial background color
    }

    public void setColor(int color) {
        currentColor = color;
        mPaint.setColor(color);
    }

    public void setStrokeWidth(float width) {
        strokeWidth = width;
        mPaint.setStrokeWidth(width);
    }

    public void undo() {
        if (paths.size() > 0) {
            paths.remove(paths.size() - 1);
            redraw();
        }
    }

    public Bitmap save() {
        return mBitmap;
    }

    public void clear() {
        paths.clear();  // Clear all paths
        mCanvas.drawColor(Color.WHITE);  // Clear the canvas by filling it with white color
        invalidate();  // Refresh the view
    }

    private void redraw() {
        mCanvas.drawColor(Color.WHITE);  // Clear the canvas
        for (Stroke fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mCanvas.drawPath(fp.path, mPaint);
        }
        invalidate();  // Refresh the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        for (Stroke fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mCanvas.drawPath(fp.path, mPaint);
        }
    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        Stroke fp = new Stroke(currentColor, (int) strokeWidth, mPath);
        paths.add(fp);
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    private static class Stroke {
        int color;
        int strokeWidth;
        Path path;

        Stroke(int color, int strokeWidth, Path path) {
            this.color = color;
            this.strokeWidth = strokeWidth;
            this.path = path;
        }
    }
}