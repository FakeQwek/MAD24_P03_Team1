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

        // set up GestureDetector for double-click detection
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showEditDialog();
                return true;
            }
        });
    }

    // create and display editTextDialog
    private void showEditDialog() {
        final EditText editText = new EditText(getContext());
        editText.setText(text);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Text")
                .setView(editText)
                .setPositiveButton("OK", (dialog, which) -> {
                    text = editText.getText().toString();
                    invalidate(); // Redraw view with updated text
                })
                .setNegativeButton("Cancel", null);

        editDialog = builder.create();
        editDialog.show();
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
        updateRect(); // Update rect bounds when posX changes
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
        updateRect();
    }

    public String getText() {
        return text;
    }

    public void setText(String editedText) {
        this.text = editedText;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // check if the touch is within the bounds of the NodeView
                if (touchX >= rect.left && touchX <= rect.right &&
                        touchY >= rect.top && touchY <= rect.bottom) {
                    lastTouchX = touchX;
                    lastTouchY = touchY;
                    isMoving = true;
                    return true;
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
                    updateRect(); //
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                isMoving = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    // if size rectangle changes, update
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateRect();
    }

    // ensure text fits within node no matter the length
    private void updateRect() {
        int padding = 40;
        rect = new RectF(posX, posY, posX + getTextWidth() + 2 * padding, posY + getTextHeight() + 2 * padding);
    }

    private float getTextWidth() {
        return paint.measureText(text);
    }

    private float getTextHeight() {
        return paint.descent() - paint.ascent();
    }

    // set up + create nodes
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int padding = 40;
        int cornerRadius = 20;

        // calc position of the rounded rectangle based on text position
        float rectLeft = posX;
        float rectTop = posY;
        float rectRight = posX + getTextWidth() + 2 * padding;
        float rectBottom = posY + getTextHeight() + 2 * padding;

        // make node rounded
        paint.setColor(Color.BLUE);
        canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, cornerRadius, cornerRadius, paint);

        // Draw text centered inside the rounded rectangle
        paint.setColor(Color.WHITE);
        canvas.drawText(text, posX + padding, posY + padding + getTextHeight(), paint);
    }
}
