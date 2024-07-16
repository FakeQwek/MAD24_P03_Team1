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
    private boolean isSelected = false;
    private static NodeView selectedNode = null;
    private OnPositionChangedListener positionChangedListener;

    public NodeView(Context context, String text, float posX, float posY) {
        super(context);
        this.text = text;
        this.posX = posX;
        this.posY = posY;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(40);

        // Set up GestureDetector for tap and double-tap detection
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showEditDialog();
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleSelection();
                return true;
            }
        });

        updateRect();
        setWillNotDraw(false);
    }

    // Create and display edit text dialog
    private void showEditDialog() {
        final EditText editText = new EditText(getContext());
        editText.setText(text);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Text")
                .setView(editText)
                .setPositiveButton("OK", (dialog, which) -> {
                    text = editText.getText().toString();
                    updateRect();
                    invalidate();
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
        updateRect();
        notifyPositionChanged();
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
        updateRect();
        notifyPositionChanged();
    }

    public String getText() {
        return text;
    }

    public void setText(String editedText) {
        this.text = editedText;
        updateRect();
        invalidate();
    }

    public void setTextSize(float size) {
        paint.setTextSize(size);
        updateRect();
        invalidate();
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        invalidate();
    }

    public void toggleSelection() {
        // Deselect the previously selected node (if any)
        if (selectedNode != null && selectedNode != this) {
            selectedNode.setSelected(false);
        }

        // Toggle selection for the current node
        isSelected = !isSelected;
        selectedNode = isSelected ? this : null;

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if the touch is within the bounds of the NodeView
                if (touchX >= rect.left && touchX <= rect.right &&
                        touchY >= rect.top && touchY <= rect.bottom) {
                    lastTouchX = touchX;
                    lastTouchY = touchY;
                    isMoving = true;
                    // Select this node when it's touched
                    toggleSelection();
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
                    updateRect();
                    invalidate();
                    // Notify position change listener
                    notifyPositionChanged();
                }
                break;
            case MotionEvent.ACTION_UP:
                isMoving = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    // Update rectangle bounds when the size changes
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateRect();
    }

    // Ensure the text fits within the node no matter the length
    public void updateRect() {
        int padding = 40;
        rect = new RectF(posX, posY, posX + getTextWidth() + 2 * padding, posY + getTextHeight() + 2 * padding);
    }

    public float getTextWidth() {
        return paint.measureText(text);
    }

    public float getTextHeight() {
        return paint.descent() - paint.ascent();
    }

    // Set up and create nodes
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int padding = 40;
        int cornerRadius = 20;

        // Calculate the position of the rounded rectangle based on text position
        float rectLeft = posX;
        float rectTop = posY;
        float rectRight = posX + getTextWidth() + 2 * padding;
        float rectBottom = posY + getTextHeight() + 2 * padding;

        // Make node rounded
        paint.setColor(isSelected ? Color.YELLOW : Color.BLUE); // Change color if selected
        canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, cornerRadius, cornerRadius, paint);

        // Draw text centered inside the rounded rectangle
        paint.setColor(Color.WHITE);
        canvas.drawText(text, posX + padding, posY + padding - paint.ascent(), paint);
    }

    private void notifyPositionChanged() {
        if (positionChangedListener != null) {
            positionChangedListener.onPositionChanged(this);
        }
    }

    public void setOnPositionChangedListener(OnPositionChangedListener listener) {
        this.positionChangedListener = listener;
    }

    public interface OnPositionChangedListener {
        void onPositionChanged(NodeView nodeView);
    }
}
