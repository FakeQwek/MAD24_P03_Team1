package sg.edu.np.mad.inkwell;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

public class NodeView extends View {

    private String text;
    private float posX, posY;
    private Paint paint;
    private RectF rect;
    private GestureDetector gestureDetector;
    private AlertDialog editDialog;

    private float lastTouchX, lastTouchY;
    private boolean isMoving = false;

    public NodeView(Context context, String text, float posX, float posY) {
        super(context);
        this.text = text;
        this.posX = posX;
        this.posY = posY;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(40);

        // Set up GestureDetector for double-click detection
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showEditDialog();
                return true;
            }
        });
    }

    private void showEditDialog() {
        final EditText editText = new EditText(getContext());
        editText.setText(text);

        editDialog = new AlertDialog.Builder(getContext())
                .setTitle("Edit Text")
                .setView(editText)
                .setPositiveButton("OK", (dialog, which) -> {
                    text = editText.getText().toString();
                    invalidate(); // Redraw view with updated text
                })
                .setNegativeButton("Cancel", null)
                .create();

        editDialog.show();
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle touch events for double-click detection and dragging
        gestureDetector.onTouchEvent(event);

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if the touch is within the bounds of the NodeView
                if (touchX >= posX && touchX <= posX + rect.width() &&
                        touchY >= posY && touchY <= posY + rect.height()) {
                    lastTouchX = touchX;
                    lastTouchY = touchY;
                    isMoving = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMoving) {
                    float dx = touchX - lastTouchX;
                    float dy = touchY - lastTouchY;
                    posX += dx;
                    posY += dy;
                    lastTouchX = touchX;
                    lastTouchY = touchY;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                isMoving = false;
                break;
        }

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Calculate the rect bounds including padding
        int padding = 40;
        rect = new RectF(posX, posY, posX + getTextWidth() + 2 * padding, posY + getTextHeight() + 2 * padding);
    }

    private float getTextWidth() {
        return paint.measureText(text);
    }

    private float getTextHeight() {
        return paint.descent() - paint.ascent();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int padding = 40;
        int cornerRadius = 20; // Adjust corner radius as needed

        // Calculate the position of the rounded rectangle based on text position
        float rectLeft = posX;
        float rectTop = posY;
        float rectRight = posX + getTextWidth() + 2 * padding;
        float rectBottom = posY + getTextHeight() + 2 * padding;

        // Draw rounded rectangle
        paint.setColor(Color.BLUE);
        canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, cornerRadius, cornerRadius, paint);

        // Draw text centered inside the rounded rectangle
        paint.setColor(Color.WHITE);
        canvas.drawText(text, posX + padding, posY + padding + getTextHeight(), paint);
    }

}
