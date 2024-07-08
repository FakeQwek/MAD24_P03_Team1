package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
    private ImageButton addNodeButton;
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
        nodes = new ArrayList<>();

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
        // create a new NodeView at the touch position
        NodeView node = new NodeView(this, "Node " + (nodes.size() + 1), touchX, touchY);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = (int) touchX;
        params.topMargin = (int) touchY;
        mindMapContainer.addView(node, params);
        nodes.add(node);
    }

    // move node around by dragging
    private void moveNode(float x, float y) {
        if (selectedNode != null) {
            selectedNode.setPosX(x);
            selectedNode.setPosY(y);
            drawConnections();
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
