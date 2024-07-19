package sg.edu.np.mad.inkwell;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MindMap extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // init
    private FrameLayout mindMapContainer;
    private ImageButton addNodeButton, addConnectionButton;
    private List<NodeView> nodes;
    private List<LineView> lines;
    private float touchX, touchY;
    private NodeView selectedNode;
    private boolean isMovingNode;
    private NodeView titleNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mind_map);

        // Sets toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Finds drawer and nav view before setting listener
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
            private GestureDetector gestureDetector = new GestureDetector(MindMap.this, new GestureDetector.SimpleOnGestureListener() {
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
                gestureDetector.onTouchEvent(event);
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Select new node if touched
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
                        }

                        // Initialize touch position and movement state
                        touchX = x;
                        touchY = y;
                        isMovingNode = (selectedNode != null);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (isMovingNode && selectedNode != null) {
                            moveNode(x, y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        isMovingNode = false;
                        break;
                }
                return true;
            }
        });
    }

    // add title node to middle of screen
    private void initializeTitleNode() {
        titleNode = new NodeView(this, "My New MindMap", 0, 0);
        titleNode.setTextSize(60);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        mindMapContainer.addView(titleNode, params);

        mindMapContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mindMapContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Calculate the center position for the title node
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

        // Set the new node as the selected node
        setSelectedNode(childNode);
    }

    // add sibling node
    private void addSiblingNode() {
        NodeView parentNode = selectedNode != null ? selectedNode : titleNode;
        NodeView siblingNode = new NodeView(this, "Sibling Node " + (nodes.size() + 1), parentNode.getPosX() + 200, parentNode.getPosY() + 200);
        addNodeToContainer(siblingNode);
        addConnectionLine(parentNode, siblingNode);

        // Set the new node as the selected node
        setSelectedNode(siblingNode);
    }

    // Add connection line
    private void addConnectionLine(NodeView startNode, NodeView endNode) {
        LineView lineView = new LineView(this, startNode, endNode);
        lines.add(lineView);
        mindMapContainer.addView(lineView);
    }

    // add node to container
    private void addNodeToContainer(NodeView node) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        mindMapContainer.addView(node, params);
        node.updateRect();
        nodes.add(node);

    }

    // get the node by position
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

    // bring the selected node to front
    private void bringNodeToFront(NodeView node) {
        mindMapContainer.removeView(node);
        mindMapContainer.addView(node);
        node.bringToFront();
    }

    // move node by dragging
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

            // Update lines connected to this node
            for (LineView line : lines) {
                line.updatePosition();
            }
        }
    }

    // set selected node
    private void setSelectedNode(NodeView node) {
        if (selectedNode != null) {
            selectedNode.setSelected(false); // Deselect previous selected node
            selectedNode.invalidate(); // Redraw to reset color
        }

        selectedNode = node;
        if (selectedNode != null) {
            selectedNode.setSelected(true); // Set as selected
            selectedNode.invalidate(); // Redraw to apply selection color
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
