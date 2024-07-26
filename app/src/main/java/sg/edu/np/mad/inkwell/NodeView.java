package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeView extends View {
    private int index;
    private String text;
    private float posX, posY;
    private Paint paint;
    private RectF rect;
    private GestureDetector gestureDetector;
    private boolean isSelected = false;
    private static NodeView selectedNode = null;
    private OnPositionChangedListener positionChangedListener;
    private List<NodeView> childNodes = new ArrayList<>();
    private float scale = 1.0f;
    private int nodeColor = Color.BLUE; // Default color

    public NodeView(Context context, String text, float posX, float posY) {
        super(context);
        this.text = text;
        this.posX = posX;
        this.posY = posY;
        this.index = -1;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(40);
        paint.setColor(nodeColor);

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

    public void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout dialogLayout = new LinearLayout(getContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(16, 16, 16, 16);

        final EditText editText = new EditText(getContext());
        editText.setText(text);
        dialogLayout.addView(editText);

        final LinearLayout colorLayout = new LinearLayout(getContext());
        colorLayout.setOrientation(LinearLayout.HORIZONTAL);
        dialogLayout.addView(colorLayout);

        int[] colors = {R.color.pastelCoral, R.color.pastelBlue, R.color.pastelGreen, R.color.pastelPurple, R.color.pastelYellow, R.color.pastelOrange};
        final int[] selectedColor = {R.color.pastelBlue};

        for (final int color : colors) {
            Button colorButton = new Button(getContext());
            colorButton.setBackgroundColor(ContextCompat.getColor(getContext(), color));
            colorButton.setTag(color);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.color_button_size),
                    getResources().getDimensionPixelSize(R.dimen.color_button_size)
            );
            params.setMargins(10, 10, 10, 10);
            colorButton.setLayoutParams(params);

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(ContextCompat.getColor(getContext(), color));
            colorButton.setBackground(shape);

            colorButton.setOnClickListener(v -> {
                selectedColor[0] = color;
                for (int i = 0; i < colorLayout.getChildCount(); i++) {
                    View child = colorLayout.getChildAt(i);
                    if (child == v) {
                        child.setSelected(true);
                        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) child.getLayoutParams();
                        params1.width = getResources().getDimensionPixelSize(R.dimen.color_button_selected_size);
                        params1.height = getResources().getDimensionPixelSize(R.dimen.color_button_selected_size);
                        child.setLayoutParams(params1);
                    } else {
                        child.setSelected(false);
                        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) child.getLayoutParams();
                        params2.width = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                        params2.height = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                        child.setLayoutParams(params2);
                    }
                }
            });

            colorLayout.addView(colorButton);
        }

        builder.setView(dialogLayout)
                .setPositiveButton("OK", (dialog, which) -> {
                    text = editText.getText().toString();
                    nodeColor = ContextCompat.getColor(getContext(), selectedColor[0]);
                    paint.setColor(nodeColor); // Update the paint color
                    updateRect();
                    requestLayout();
                    invalidate(); // Refresh the view with the new color
                    if (onNodeUpdateListener != null) {
                        onNodeUpdateListener.onNodeUpdate(); // Notify update
                    }
                    // Trigger saveMindMap
                    ((MindMapActivity) getContext()).saveMindMap(FirebaseFirestore.getInstance(), ((MindMapActivity) getContext()).currentUser.getUid());
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Delete", (dialog, which) -> showDeleteConfirmationDialog());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Node")
                .setMessage("Are you sure you want to delete this node? This action cannot be undone!")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (getContext() instanceof MindMapActivity) {
                        MindMapActivity activity = (MindMapActivity) getContext();
                        int nodeIndex = getIndex(); // Get the index of this node

                        if (nodeIndex == 0) {
                            // Handle special case for 0th node
                            activity.handleTitleNodeDeletion(NodeView.this);
                        } else {
                            activity.removeNode(NodeView.this);
                        }
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
        requestLayout();
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
        updateRect();
        notifyPositionChanged();
        requestLayout();
    }

    public String getText() {
        return text;
    }

    public void setText(String editedText) {
        this.text = editedText;
        updateRect();
        requestLayout();
        invalidate();
        ((MindMapActivity) getContext()).saveMindMap(FirebaseFirestore.getInstance(), ((MindMapActivity) getContext()).currentUser.getUid());
    }

    public void setTextSize(float size) {
        paint.setTextSize(size);
        updateRect();
        requestLayout();
        invalidate();
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        invalidate();
    }

    public void setScale(float scale) {
        this.scale = scale;

        setPosX(getPosX() * scale);
        setPosY(getPosY() * scale);

        setLayoutParams(new ViewGroup.LayoutParams(
                (int) (getWidth() * scale),
                (int) (getHeight() * scale)
        ));

        updateRect();
        requestLayout();
        invalidate();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void toggleSelection() {
        if (isSelected) {
            return;
        }

        if (selectedNode != null && selectedNode != this) {
            selectedNode.setSelected(false);
        }

        isSelected = true;
        selectedNode = this;

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (getTextWidth() + 80);
        int height = (int) (getTextHeight() + 80);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setScaleX(scale);
        setScaleY(scale);
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

        float rectLeft = 0;
        float rectTop = 0;
        float rectRight = getWidth();
        float rectBottom = getHeight();

        paint.setColor(isSelected ? Color.YELLOW : nodeColor); // Use nodeColor
        canvas.drawRoundRect(rectLeft, rectTop, rectRight, rectBottom, cornerRadius, cornerRadius, paint);

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

    private OnNodeUpdateListener onNodeUpdateListener;

    public void setOnNodeUpdateListener(OnNodeUpdateListener listener) {
        this.onNodeUpdateListener = listener;
    }

    public interface OnNodeUpdateListener {
        void onNodeUpdate();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("text", text);
        map.put("posX", posX);
        map.put("posY", posY);
        map.put("color", nodeColor);
        return map;
    }
}
