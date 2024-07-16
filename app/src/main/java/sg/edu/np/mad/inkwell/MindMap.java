package sg.edu.np.mad.inkwell;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
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

        initializeTitleNode();

        addNodeButton.setOnClickListener(v -> addChildNode());
        addConnectionButton.setOnClickListener(v -> addSiblingNode());

        // get nodes at position and move if dragged
        mindMapContainer.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    selectedNode = getNodeAtPosition(event.getX(), event.getY());
                    if (selectedNode != null) {
                        touchX = event.getX();
                        touchY = event.getY();
                        isMovingNode = true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isMovingNode && selectedNode != null) {
                        moveNode(event.getX(), event.getY());
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isMovingNode = false;
                    break;
            }
            return true;
        });
    }

    // add title node to middle of screen
    private void initializeTitleNode() {
        // create the title node
        titleNode = new NodeView(this, "My New MindMap", -1000, -1000);

        // set text size to be larger
        titleNode.setTextSize(60);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        // add title node to container
        mindMapContainer.addView(titleNode, params);

        mindMapContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mindMapContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Calculate the center position for the title node
                float centerX = (mindMapContainer.getWidth() - titleNode.getTextWidth()) / 2;
                float centerY = (mindMapContainer.getHeight() - titleNode.getTextHeight()) / 2;

                // Update the title node's position and bounds
                titleNode.setPosX(centerX);
                titleNode.setPosY(centerY);
                titleNode.updateRect();
                titleNode.invalidate();
            }
        });

        // add title node to list of nodes
        nodes.add(titleNode);
    }

    // add child node
    private void addChildNode() {
        if (titleNode != null) {
            NodeView childNode = new NodeView(this, "Child Node " + (nodes.size() + 1), titleNode.getPosX() + 200, titleNode.getPosY() + 200);
            addNodeToContainer(childNode);
        }
    }

    // add sibling node
    private void addSiblingNode() {
        if (titleNode != null) {
            NodeView siblingNode = new NodeView(this, "Sibling Node " + (nodes.size() + 1), titleNode.getPosX() + 200, titleNode.getPosY() - 200);
            addNodeToContainer(siblingNode);
        }
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

        Log.d(TAG, "Node added: " + node.getText());
    }

    // get the node by position
    private NodeView getNodeAtPosition(float x, float y) {
        for (NodeView node : nodes) {
            if (x >= node.getPosX() && x <= node.getPosX() + node.getWidth() &&
                    y >= node.getPosY() && y <= node.getPosY() + node.getHeight()) {
                return node;
            }
        }
        return null;
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
