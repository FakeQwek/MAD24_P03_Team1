package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class LineView extends View {
    private NodeView startNode;
    private NodeView endNode;
    private Paint paint;

    public LineView(Context context, NodeView startNode, NodeView endNode) {
        super(context);
        this.startNode = startNode;
        this.endNode = endNode;
        paint = new Paint();
        paint.setColor(0xFF000000); // Black color for the line
        paint.setStrokeWidth(5);
        setWillNotDraw(false);
    }

    public NodeView getStartNode() {
        return startNode;
    }

    public NodeView getEndNode() {
        return endNode;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float startX = startNode.getX() + startNode.getWidth() / 2;
        float startY = startNode.getY() + startNode.getHeight() / 2;
        float endX = endNode.getX() + endNode.getWidth() / 2;
        float endY = endNode.getY() + endNode.getHeight() / 2;
        canvas.drawLine(startX, startY, endX, endY, paint);
    }

    public boolean isConnectedTo(NodeView node) {
        return startNode == node || endNode == node;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("startNodePosX", startNode.getPosX());
        map.put("startNodePosY", startNode.getPosY());
        map.put("endNodePosX", endNode.getPosX());
        map.put("endNodePosY", endNode.getPosY());
        return map;
    }
}
