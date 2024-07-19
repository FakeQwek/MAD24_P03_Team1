package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class NodeView extends View {

    private String text;
    private float posX, posY;
    private Paint paint;
    private RectF rect;
    private GestureDetector gestureDetector;
    private boolean isSelected = false;
    private static NodeView selectedNode = null;
    private OnPositionChangedListener positionChangedListener;
    private List<NodeView> childNodes = new ArrayList<>();


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

        setWillNotDraw(false);
        updateRect();
    }

    // show edit dialog
    public void showEditDialog() {
        final EditText editText = new EditText(getContext());
        editText.setText(text);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Text")
                .setView(editText)
                .setPositiveButton("OK", (dialog, which) -> {
                    text = editText.getText().toString();
                    updateRect();
                    requestLayout();
                    invalidate();
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Delete", (dialog, which) -> {
                    showDeleteConfirmationDialog();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Node")
                .setMessage("Are you sure you want to delete this node?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Notify MindMap to delete this node
                    if (getContext() instanceof MindMap) {
                        ((MindMap) getContext()).removeNode(NodeView.this);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
        updateRect();
        notifyPositionChanged();
        requestLayout(); // Request layout update to adjust position
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
        updateRect();
        notifyPositionChanged();
        requestLayout(); // Request layout update to adjust position
    }

    public String getText() {
        return text;
    }

    public void setText(String editedText) {
        this.text = editedText;
        updateRect();
        requestLayout(); // Request layout update to adjust size
        invalidate();
    }

    public void setTextSize(float size) {
        paint.setTextSize(size);
        updateRect();
        requestLayout(); // Request layout update to adjust size
        invalidate();
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        invalidate();
    }

    public void toggleSelection() {
        if (isSelected) {
            return;
        }

        // Deselect the previously selected node
        if (selectedNode != null && selectedNode != this) {
            selectedNode.setSelected(false);
        }

        // Select the current node
        isSelected = true;
        selectedNode = this;

        invalidate();
    }

    public List<NodeView> getChildNodes() {
        return childNodes;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Measure width and height based on content
        int width = (int) (getTextWidth() + 80);
        int height = (int) (getTextHeight() + 80);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateRect();
    }

    public void updateRect() {
        int padding = 40;
        rect = new RectF(posX, posY, posX + getTextWidth() + 2 * padding, posY + getTextHeight() + 2 * padding);
        setX(posX);
        setY(posY);
    }

    public float getTextWidth() {
        return paint.measureText(text);
    }

    public float getTextHeight() {
        return paint.descent() - paint.ascent();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("NodeView", "Width: " + getWidth() + ", Height: " + getHeight());

        int padding = 40;
        int cornerRadius = 20;

        // Calculate the position of the rounded rectangle based on text position
        float rectLeft = 0;
        float rectTop = 0;
        float rectRight = getWidth();
        float rectBottom = getHeight();

        // Make node rounded
        paint.setColor(isSelected ? Color.YELLOW : Color.BLUE); // Change color if selected
        canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, cornerRadius, cornerRadius, paint);

        // Draw text centered inside the rounded rectangle
        paint.setColor(isSelected ? Color.BLACK : Color.WHITE);
        canvas.drawText(text, padding, padding - paint.ascent(), paint);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}
