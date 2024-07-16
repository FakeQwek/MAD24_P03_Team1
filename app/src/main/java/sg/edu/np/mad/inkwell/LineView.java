package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class LineView extends View {

    private NodeView startNode, endNode;
    private Paint paint;

    public LineView(Context context, NodeView startNode, NodeView endNode) {
        super(context);
        this.startNode = startNode;
        this.endNode = endNode;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(android.graphics.Color.BLACK);
        paint.setStrokeWidth(5);

        startNode.setOnPositionChangedListener(nodeView -> invalidate());
        endNode.setOnPositionChangedListener(nodeView -> invalidate());
    }

    public void updatePosition() {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float startX = startNode.getPosX() + startNode.getTextWidth() / 2;
        float startY = startNode.getPosY() + startNode.getTextHeight() / 2;
        float endX = endNode.getPosX() + endNode.getTextWidth() / 2;
        float endY = endNode.getPosY() + endNode.getTextHeight() / 2;

        canvas.drawLine(startX, startY, endX, endY, paint);
    }
}
