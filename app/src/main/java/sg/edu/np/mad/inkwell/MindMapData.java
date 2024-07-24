package sg.edu.np.mad.inkwell;

public class MindMapData {
    public class NodeData {
        private String text;
        private float posX;
        private float posY;
        private int color; // Store color as an integer

        public NodeData() { } // Required for Firestore

        public NodeData(String text, float posX, float posY, int color) {
            this.text = text;
            this.posX = posX;
            this.posY = posY;
            this.color = color;
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public float getPosX() { return posX; }
        public void setPosX(float posX) { this.posX = posX; }

        public float getPosY() { return posY; }
        public void setPosY(float posY) { this.posY = posY; }

        public int getColor() { return color; }
        public void setColor(int color) { this.color = color; }
    }

    public class LineData {
        private String startNodeId;
        private String endNodeId;

        public LineData() { }

        public LineData(String startNodeId, String endNodeId) {
            this.startNodeId = startNodeId;
            this.endNodeId = endNodeId;
        }

        public String getStartNodeId() { return startNodeId; }
        public void setStartNodeId(String startNodeId) { this.startNodeId = startNodeId; }

        public String getEndNodeId() { return endNodeId; }
        public void setEndNodeId(String endNodeId) { this.endNodeId = endNodeId; }
    }

}
