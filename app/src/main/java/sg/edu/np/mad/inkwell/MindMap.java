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

    private FrameLayout mindMapContainer;
    private ImageButton addNodeButton, addConnectionButton;
    private List<NodeView> nodes;
    private float touchX, touchY;
    private NodeView selectedNode;
    private boolean isMovingNode;

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

        // addConnectionButton.setOnClickListener(v -> drawConnections());
        addNodeButton.setOnClickListener(v -> addNode());

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

    // create new node
    private void addNode() {
        // Calculate the center of the screen
        int centerX = mindMapContainer.getWidth() / 2;
        int centerY = mindMapContainer.getHeight() / 2;

        // Create a new NodeView at the center of the screen
        NodeView node = new NodeView(this, "Node " + (nodes.size() + 1), centerX, centerY);

        // Set layout parameters to wrap the content
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        // Add the node to the mind map container
        mindMapContainer.addView(node, params);

        // Use ViewTreeObserver to adjust the position after the layout is complete
        node.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to prevent repeated calls
                node.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Adjust the node's position to the center
                int adjustedLeftMargin = centerX - node.getWidth() / 2;
                int adjustedTopMargin = centerY - node.getHeight() / 2;

                FrameLayout.LayoutParams updatedParams = (FrameLayout.LayoutParams) node.getLayoutParams();
                updatedParams.leftMargin = adjustedLeftMargin;
                updatedParams.topMargin = adjustedTopMargin;

                // Apply the new layout parameters
                node.setLayoutParams(updatedParams);

                // Log to confirm the node has been positioned
                Log.d(TAG, "Node positioned at: " + adjustedLeftMargin + ", " + adjustedTopMargin);
            }
        });

        nodes.add(node);

        Log.d(TAG, "addNode: added");
    }


    // move node around by dragging
    private void moveNode(float x, float y) {
        if (selectedNode != null) {
            selectedNode.setPosX(x);
            selectedNode.setPosY(y);
        }
    }

    // get the node by position
    private NodeView getNodeAtPosition(float x, float y) {
        for (NodeView node : nodes) {
            float nodeLeft = node.getPosX();
            float nodeRight = nodeLeft + node.getWidth();
            float nodeTop = node.getPosY();
            float nodeBottom = nodeTop + node.getHeight();

            if (x >= nodeLeft && x <= nodeRight && y >= nodeTop && y <= nodeBottom) {
                return node;
            }
        }
        return null;
    }

    // connect nodes
    private void drawConnections() {
        mindMapContainer.removeAllViews();
        for (NodeView node : nodes) {
            mindMapContainer.addView(node);
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
