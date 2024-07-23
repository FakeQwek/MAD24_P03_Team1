package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ScaleGestureDetector;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MindMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final float MIN_ZOOM = 0.6f;
    private static final float MAX_ZOOM = 1.2f;
    private static final float MAX_PAN_LEFT = 0f;
    private static final float MAX_PAN_RIGHT = 6000f;
    private static final float MAX_PAN_TOP = 0f;
    private static final float MAX_PAN_BOTTOM = 6000f;

    private ZoomLayout mindMapContainer;
    private ImageButton addNodeButton, addConnectionButton;
    private List<NodeView> nodes;
    private List<LineView> lines;
    private float touchX, touchY;
    private NodeView selectedNode;
    private boolean isMovingNode;
    private boolean isPanning;
    private NodeView titleNode;
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mind_map);

        // Sets toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        mindMapContainer = findViewById(R.id.mindMapContainer);
        addNodeButton = findViewById(R.id.addNodeButton);
        addConnectionButton = findViewById(R.id.addConnectionButton);
        nodes = new ArrayList<>();
        lines = new ArrayList<>();

        initializeTitleNode();

        addNodeButton.setOnClickListener(v -> addChildNode());
        addConnectionButton.setOnClickListener(v -> addSiblingNode());

        // Handle all touch events for nodes in the mind map
        mindMapContainer.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(MindMapActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    float x = e.getX();
                    float y = e.getY();
                    NodeView nodeAtPosition = getNodeAtPosition(x, y);
                    if (nodeAtPosition != null) {
                        nodeAtPosition.showEditDialog();
                    }
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        NodeView nodeAtPosition = getNodeAtPosition(x, y);
                        if (nodeAtPosition != null) {
                            setSelectedNode(nodeAtPosition);
                            bringNodeToFront(nodeAtPosition);
                        } else {
                            if (selectedNode != null) {
                                selectedNode.setSelected(false);
                                selectedNode.invalidate();
                                selectedNode = null;
                            }
                            isPanning = true;
                        }

                        touchX = x;
                        touchY = y;
                        isMovingNode = (selectedNode != null);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (isMovingNode && selectedNode != null) {
                            moveNode(x, y);
                        } else if (isPanning) {
                            panMindMap(x, y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        isMovingNode = false;
                        isPanning = false;
                        break;
                }
                return true;
            }
        });

        // Initialize ScaleGestureDetector for zooming
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();

                // set scale only between min and max zoom
                scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));

                // update child node scales and redraw lines
                for (int i = 0; i < mindMapContainer.getChildCount(); i++) {
                    View child = mindMapContainer.getChildAt(i);
                    if (child instanceof NodeView) {
                        child.setScaleX(scaleFactor);
                        child.setScaleY(scaleFactor);
                    } else if (child instanceof LineView) {
                        ((LineView) child).invalidate();
                    }
                }

                return true;
            }
        });
    }

    // enable mindmap to be panned around
    private void panMindMap(float x, float y) {
        float dx = x - touchX;
        float dy = y - touchY;

        boolean withinXBounds = true;
        boolean withinYBounds = true;

        for (NodeView node : nodes) {
            float newX = node.getPosX() + dx;
            float newY = node.getPosY() + dy;

            // check if the new position is within the allowed bounds
            if (newX < MAX_PAN_LEFT || newX > MAX_PAN_RIGHT) {
                withinXBounds = false;
            }
            if (newY < MAX_PAN_TOP || newY > MAX_PAN_BOTTOM) {
                withinYBounds = false;
            }
        }

        // apply panning only if the new positions are within bounds
        if (withinXBounds && withinYBounds) {
            for (NodeView node : nodes) {
                node.setPosX(node.getPosX() + dx);
                node.setPosY(node.getPosY() + dy);
                node.updateRect();
                node.invalidate();
            }

            for (LineView line : lines) {
                line.invalidate();
            }

            touchX = x;
            touchY = y;
        }
    }

    // init title node in centre of screen
    private void initializeTitleNode() {
        titleNode = new NodeView(this, "My New MindMap", 0, 0);
        titleNode.setTextSize(60);

        ZoomLayout.LayoutParams params = new ZoomLayout.LayoutParams(
                ZoomLayout.LayoutParams.WRAP_CONTENT,
                ZoomLayout.LayoutParams.WRAP_CONTENT
        );

        mindMapContainer.addView(titleNode, params);

        mindMapContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mindMapContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                float centerX = (mindMapContainer.getWidth() - titleNode.getTextWidth()) / 2;
                float centerY = (mindMapContainer.getHeight() - titleNode.getTextHeight()) / 2;

                titleNode.setPosX(centerX);
                titleNode.setPosY(centerY);
                titleNode.updateRect();
                titleNode.invalidate();
            }
        });

        nodes.add(titleNode);
    }

    // add child node
    private void addChildNode() {
        NodeView parentNode = selectedNode != null ? selectedNode : titleNode;
        NodeView childNode = new NodeView(this, "Child Node " + (nodes.size() + 1), parentNode.getPosX() + 200, parentNode.getPosY() + 200);
        addNodeToContainer(childNode);
        addConnectionLine(parentNode, childNode);

        setSelectedNode(childNode);
    }

    // add sibling node
    private void addSiblingNode() {
        NodeView parentNode = selectedNode != null ? selectedNode : titleNode;
        NodeView siblingNode = new NodeView(this, "Sibling Node " + (nodes.size() + 1), parentNode.getPosX() + 200, parentNode.getPosY() + 200);
        addNodeToContainer(siblingNode);
        addConnectionLine(parentNode, siblingNode);

        setSelectedNode(siblingNode);
    }

    // add connection line b/w nodes
    private void addConnectionLine(NodeView startNode, NodeView endNode) {
        LineView lineView = new LineView(this, startNode, endNode);
        lines.add(lineView);
        mindMapContainer.addView(lineView);
    }

    // add node to mindmap
    private void addNodeToContainer(NodeView node) {
        ZoomLayout.LayoutParams params = new ZoomLayout.LayoutParams(
                ZoomLayout.LayoutParams.WRAP_CONTENT,
                ZoomLayout.LayoutParams.WRAP_CONTENT
        );

        node.setScaleX(scaleFactor);
        node.setScaleY(scaleFactor);

        mindMapContainer.addView(node, params);
        node.updateRect();
        nodes.add(node);
    }

    // get node at touched position
    private NodeView getNodeAtPosition(float x, float y) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            NodeView node = nodes.get(i);
            if (x >= node.getPosX() && x <= node.getPosX() + node.getWidth() &&
                    y >= node.getPosY() && y <= node.getPosY() + node.getHeight()) {
                return node;
            }
        }
        return null;
    }

    // bring node to the front
    private void bringNodeToFront(NodeView node) {
        mindMapContainer.removeView(node);
        mindMapContainer.addView(node);
        node.bringToFront();
    }

    // move node on drag
    private void moveNode(float x, float y) {
        if (selectedNode != null) {
            float dx = x - touchX;
            float dy = y - touchY;
            selectedNode.setPosX(selectedNode.getPosX() + dx);
            selectedNode.setPosY(selectedNode.getPosY() + dy);
            selectedNode.updateRect();
            selectedNode.invalidate();
            touchX = x;
            touchY = y;

            for (LineView line : lines) {
                if (line.isConnectedTo(selectedNode)) {
                    line.invalidate();
                }
            }
        }
    }

    // set touched node as selected
    private void setSelectedNode(NodeView node) {
        if (selectedNode != null) {
            selectedNode.setSelected(false);
            selectedNode.invalidate();
        }

        selectedNode = node;
        if (selectedNode != null) {
            selectedNode.setSelected(true);
            selectedNode.invalidate();
        }
    }

    // remove node
    public void removeNode(NodeView node) {
        if (nodes.contains(node)) {
            for (LineView line : new ArrayList<>(lines)) {
                if (line.isConnectedTo(node)) {
                    lines.remove(line);
                    mindMapContainer.removeView(line);
                }
            }

            nodes.remove(node);
            mindMapContainer.removeView(node);

            setSelectedNode(titleNode);
        }
    }

    // navigation
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);
        return true;
    }
}
