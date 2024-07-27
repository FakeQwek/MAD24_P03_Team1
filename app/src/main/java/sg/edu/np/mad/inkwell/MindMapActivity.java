package sg.edu.np.mad.inkwell;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MindMapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final float MIN_ZOOM = 0.6f;
    private static final float MAX_ZOOM = 1.2f;
    private static final float MAX_PAN_LEFT = 0f;
    private static final float MAX_PAN_RIGHT = 6000f;
    private static final float MAX_PAN_TOP = 0f;
    private static final float MAX_PAN_BOTTOM = 6000f;

    private ZoomLayout mindMapContainer;
    private ImageButton addNodeButton, addNewMindmap;
    private List<NodeView> nodes;
    private List<LineView> lines;
    private float touchX, touchY;
    private NodeView selectedNode;
    private boolean isMovingNode;
    private boolean isPanning;
    private NodeView titleNode;
    private List<String> mindMapIds = new ArrayList<>();
    private int currentIndex = -1;
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;

    private FirebaseFirestore db;
    FirebaseUser currentUser;
    private String currentMindMapId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mind_map);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set up drawer and navigation view
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            initMindMap(db, currentUser.getUid());
        }

        mindMapContainer = findViewById(R.id.mindMapContainer);
        addNodeButton = findViewById(R.id.addNodeButton);
        addNewMindmap = findViewById(R.id.addNewMindMap);
        nodes = new ArrayList<>();
        lines = new ArrayList<>();

        addNodeButton.setOnClickListener(v -> addChildNode());
        addNewMindmap.setOnClickListener(v -> {
            // Clear the current mind map
            mindMapContainer.removeAllViews();
            nodes.clear();
            lines.clear();

            // Initialize a new mind map
            initializeTitleNode();
            selectedNode = null;
            currentMindMapId = null; // Reset mind map ID for new mind map

            if (currentUser != null) {
                saveMindMap(db, currentUser.getUid()); // Save new mind map with a new ID
            }
        });

        // Initialize ScaleGestureDetector for zooming
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
                updateViewScales();
                return true;
            }
        });

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
                    if (currentUser != null) {
                        saveMindMap(db, currentUser.getUid()); // Save mind map on edit
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

        fetchAllTitleNodes(currentUser.getUid());
    }

    private void fetchAllTitleNodes(String userId) {
        CollectionReference mindMapCollection = db.collection("users").document(userId).collection("mindmaps");

        mindMapCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<String> titleNodes = new ArrayList<>();
                        List<String> mindMapIds = new ArrayList<>();

                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Map<String, Object> mindMapData = doc.getData();
                            if (mindMapData != null) {
                                List<Map<String, Object>> savedNodes = (List<Map<String, Object>>) mindMapData.get("nodes");
                                if (savedNodes != null && !savedNodes.isEmpty()) {
                                    Map<String, Object> firstNodeData = savedNodes.get(0);
                                    String text = (String) firstNodeData.get("text");
                                    titleNodes.add(text);
                                    mindMapIds.add(doc.getId());

                                    Log.d(TAG, "MindMap ID: " + doc.getId() + " 0th Node Text: " + text);
                                }
                            }
                        }

                        // Pass both lists to the recyclerView method or wherever needed
                        recyclerView(titleNodes, mindMapIds);
                        search(titleNodes, mindMapIds);
                        navigationBar();
                    } else {
                        Log.d(TAG, "No mind maps found for the user.");
                    }
                } else {
                    Log.d(TAG, "Error fetching mind maps: ", task.getException());
                }
            }
        });
    }

    // Update node and line scales after zoom
    private void updateViewScales() {
        for (int i = 0; i < mindMapContainer.getChildCount(); i++) {
            View child = mindMapContainer.getChildAt(i);
            if (child instanceof NodeView) {
                child.setScaleX(scaleFactor);
                child.setScaleY(scaleFactor);
            } else if (child instanceof LineView) {
                ((LineView) child).invalidate();
            }
        }
    }

    // Enable mind map to be panned around
    private void panMindMap(float x, float y) {
        float dx = x - touchX;
        float dy = y - touchY;

        boolean withinXBounds = true;
        boolean withinYBounds = true;

        for (NodeView node : nodes) {
            float newX = node.getPosX() + dx;
            float newY = node.getPosY() + dy;

            if (newX < MAX_PAN_LEFT || newX > MAX_PAN_RIGHT) {
                withinXBounds = false;
            }
            if (newY < MAX_PAN_TOP || newY > MAX_PAN_BOTTOM) {
                withinYBounds = false;
            }
        }

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

    // Initialize title node in center of screen
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
        setSelectedNode(titleNode);
    }

    // Add a new child node
    private void addChildNode() {
        NodeView parentNode = selectedNode != null ? selectedNode : titleNode;
        NodeView childNode = new NodeView(this, "Child Node " + (nodes.size() + 1), parentNode.getPosX() + 200, parentNode.getPosY() + 200);
        childNode.setScale(scaleFactor);
        addNodeToContainer(childNode);
        addConnectionLine(parentNode, childNode);

        setSelectedNode(childNode);

        if (currentUser != null) {
            saveMindMap(db, currentUser.getUid());
        }
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

    // Move the currently selected node
    private void moveNode(float x, float y) {
        if (selectedNode == null) return;

        selectedNode.setPosX(selectedNode.getPosX() + (x - touchX));
        selectedNode.setPosY(selectedNode.getPosY() + (y - touchY));
        selectedNode.updateRect();
        selectedNode.invalidate();

        for (LineView line : lines) {
            line.invalidate();
        }

        touchX = x;
        touchY = y;

        if (currentUser != null) {
            saveMindMap(db, currentUser.getUid());
        }
    }

    // Set selected node and bring to front
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

            if (currentUser != null) {
                saveMindMap(db, currentUser.getUid());
            }
        }
    }

    // Bring node view to front
    private void bringNodeToFront(NodeView node) {
        mindMapContainer.bringChildToFront(node);
    }

    // Get node at a specific position
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


    // Initialize the mind map from Firebase
    private void initMindMap(FirebaseFirestore db, String userId) {
        CollectionReference mindMapCollection = db.collection("users").document(userId).collection("mindmaps");

        mindMapCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        currentMindMapId = doc.getId();
                        loadMindMap(db, userId, currentMindMapId);
                    } else {
                        // Create new mind map if none exists
                        initializeTitleNode();
                        saveMindMap(db, userId);
                    }
                } else {
                    Log.d(TAG, "Error getting mind maps: ", task.getException());
                }
            }
        });
    }

    private void loadMindMap(FirebaseFirestore db, String userId, String mindMapId) {
        DocumentReference mindMapRef = db.collection("users").document(userId).collection("mindmaps").document(mindMapId);
        mindMapRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> mindMapData = documentSnapshot.getData();
                    if (mindMapData != null) {
                        // Process and update nodes
                        List<Map<String, Object>> savedNodes = (List<Map<String, Object>>) mindMapData.get("nodes");
                        for (int i = 0; i < savedNodes.size(); i++) {
                            Map<String, Object> nodeData = savedNodes.get(i);
                            String text = (String) nodeData.get("text");
                            float posX = ((Number) nodeData.get("posX")).floatValue();
                            float posY = ((Number) nodeData.get("posY")).floatValue();
                            int color = ((Number) nodeData.get("color")).intValue(); // Retrieve color

                            NodeView node = new NodeView(MindMapActivity.this, text, posX, posY);
                            ZoomLayout.LayoutParams params = new ZoomLayout.LayoutParams(
                                    ZoomLayout.LayoutParams.WRAP_CONTENT,
                                    ZoomLayout.LayoutParams.WRAP_CONTENT
                            );
                            mindMapContainer.addView(node, params);
                            node.updateRect();
                            node.invalidate();
                            nodes.add(node);

                            // set title node text
                            if (i == 0) {
                                node.setTextSize(60);
                                setSelectedNode(node);
                            }

                            // set node colour
                            node.setColor(color);
                        }

                        // Process and update lines
                        List<Map<String, Object>> savedLines = (List<Map<String, Object>>) mindMapData.get("lines");
                        for (Map<String, Object> lineData : savedLines) {
                            int startNodeIndex = ((Number) lineData.get("startNodeIndex")).intValue();
                            int endNodeIndex = ((Number) lineData.get("endNodeIndex")).intValue();
                            NodeView startNode = nodes.get(startNodeIndex);
                            NodeView endNode = nodes.get(endNodeIndex);
                            LineView line = new LineView(MindMapActivity.this, startNode, endNode);
                            lines.add(line);
                            mindMapContainer.addView(line);
                        }

                        // Update the current mind map ID
                        currentMindMapId = mindMapId;
                    }
                } else {
                    Log.d(TAG, "No such mind map!");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error loading mind map: ", e);
            }
        });
    }

    // Save the current mind map to Firebase
    public void saveMindMap(FirebaseFirestore db, String userId) {
        CollectionReference mindMapCollection = db.collection("users").document(userId).collection("mindmaps");

        if (currentMindMapId == null) {
            // Create a new document if no current ID
            DocumentReference newMindMapDoc = mindMapCollection.document();
            currentMindMapId = newMindMapDoc.getId();
        }

        DocumentReference mindMapRef = mindMapCollection.document(currentMindMapId);

        Map<String, Object> mindMapData = new HashMap<>();
        List<Map<String, Object>> nodeList = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            NodeView node = nodes.get(i);
            node.setIndex(i);
            Map<String, Object> nodeData = node.toMap();
            nodeList.add(nodeData);
        }

        mindMapData.put("nodes", nodeList);

        List<Map<String, Object>> lineList = new ArrayList<>();
        for (LineView line : lines) {
            Map<String, Object> lineData = new HashMap<>();
            lineData.put("startNodeIndex", line.getStartNode().getIndex());
            lineData.put("endNodeIndex", line.getEndNode().getIndex());
            lineList.add(lineData);
        }

        mindMapData.put("lines", lineList);

        mindMapRef.set(mindMapData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Mind map successfully saved!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error saving mind map: ", e);
                    }
                });
        fetchAllTitleNodes(userId);
    }

    // Navigation bar handling
    private void navigationBar() {
        SearchView searchView = findViewById(R.id.searchView);

        if (searchView != null) {
            searchView.setVisibility(View.VISIBLE);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        menu.findItem(R.id.nav_home).setVisible(false);
        menu.findItem(R.id.nav_notes).setVisible(false);
        menu.findItem(R.id.nav_mind_map).setVisible(false);
        menu.findItem(R.id.nav_todos).setVisible(false);
        menu.findItem(R.id.nav_flashcards).setVisible(false);
        menu.findItem(R.id.nav_calendar).setVisible(false);
        menu.findItem(R.id.nav_timetable).setVisible(false);
        menu.findItem(R.id.nav_settings).setVisible(false);
        menu.findItem(R.id.nav_profile).setVisible(false);
        menu.findItem(R.id.nav_logout).setVisible(false);
        menu.findItem(R.id.nav_friends).setVisible(false);
        menu.findItem(R.id.nav_community).setVisible(false);
        menu.findItem(R.id.nav_calculator).setVisible(false);
        menu.findItem(R.id.nav_essay).setVisible(false);
        menu.findItem(R.id.nav_drawing).setVisible(false);

        ImageButton swapButton = findViewById(R.id.swapButton);

        if (swapButton != null) {
            swapButton.setVisibility(View.VISIBLE);

            swapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (menu.hasVisibleItems()) {
                        menu.findItem(R.id.nav_home).setVisible(false);
                        menu.findItem(R.id.nav_notes).setVisible(false);
                        menu.findItem(R.id.nav_mind_map).setVisible(false);
                        menu.findItem(R.id.nav_todos).setVisible(false);
                        menu.findItem(R.id.nav_flashcards).setVisible(false);
                        menu.findItem(R.id.nav_calendar).setVisible(false);
                        menu.findItem(R.id.nav_timetable).setVisible(false);
                        menu.findItem(R.id.nav_settings).setVisible(false);
                        menu.findItem(R.id.nav_profile).setVisible(false);
                        menu.findItem(R.id.nav_logout).setVisible(false);
                        menu.findItem(R.id.nav_friends).setVisible(false);
                        menu.findItem(R.id.nav_community).setVisible(false);
                        menu.findItem(R.id.nav_calculator).setVisible(false);
                        menu.findItem(R.id.nav_essay).setVisible(false);
                        menu.findItem(R.id.nav_drawing).setVisible(false);
                        searchView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        menu.findItem(R.id.nav_home).setVisible(true);
                        menu.findItem(R.id.nav_notes).setVisible(true);
                        menu.findItem(R.id.nav_mind_map).setVisible(true);
                        menu.findItem(R.id.nav_todos).setVisible(true);
                        menu.findItem(R.id.nav_flashcards).setVisible(true);
                        menu.findItem(R.id.nav_calendar).setVisible(true);
                        menu.findItem(R.id.nav_timetable).setVisible(true);
                        menu.findItem(R.id.nav_settings).setVisible(true);
                        menu.findItem(R.id.nav_profile).setVisible(true);
                        menu.findItem(R.id.nav_logout).setVisible(true);
                        menu.findItem(R.id.nav_friends).setVisible(true);
                        menu.findItem(R.id.nav_community).setVisible(true);
                        menu.findItem(R.id.nav_calculator).setVisible(true);
                        menu.findItem(R.id.nav_essay).setVisible(true);
                        menu.findItem(R.id.nav_drawing).setVisible(true);
                        searchView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void recyclerView(List<String> titles, List<String> ids) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MindMapAdapter adapter = new MindMapAdapter(this, titles, ids,
                id -> {
                    mindMapContainer.removeAllViews();
                    nodes.clear();
                    lines.clear();
                    loadMindMap(db, currentUser.getUid(), id);
                },
                id -> {
                    // Show confirmation dialog for deletion
                    new AlertDialog.Builder(MindMapActivity.this)
                            .setTitle("Delete Mind Map")
                            .setMessage("Are you sure you want to delete this mind map?")
                            .setPositiveButton("Delete", (dialog, which) -> deleteMindMap(id))
                            .setNegativeButton("Cancel", null)
                            .show();
                });
        recyclerView.setAdapter(adapter);
    }

    private void deleteMindMap(String mindMapId) {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).collection("mindmaps")
                    .document(mindMapId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MindMapActivity.this, "Mind map deleted", Toast.LENGTH_SHORT).show();
                        fetchAllTitleNodes(currentUser.getUid()); // Refresh the list
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MindMapActivity.this, "Error deleting mind map", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error deleting mind map: ", e);
                    });
        }
    }

    // ensure title node is not deleted
    public void handleTitleNodeDeletion(NodeView node) {
        new AlertDialog.Builder(this)
                .setTitle("Cannot Delete Node")
                .setMessage("The title node cannot be deleted!")
                .setPositiveButton("OK", null)
                .show();
    }

    // method to search the items in recycler view
    private void search(List<String> titles, List<String> ids) {
        SearchView searchView = findViewById(R.id.searchView);

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filter(titles, ids, query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filter(titles, ids, newText);
                    return false;
                }
            });
        }
    }

    // method to filter items already in the recycler view
    private void filter(List<String> titles, List<String> ids, String query) {
        ArrayList<String> filteredTitles = new ArrayList<>();
        ArrayList<String> filteredIds = new ArrayList<>();

        if (query.isEmpty()) {
            recyclerView(titles, ids);
        } else {
            for (int i = 0; i < titles.size(); i++) {
                String title = titles.get(i);
                if (title.toLowerCase().contains(query.toLowerCase())) {

                    filteredTitles.add(title);
                    filteredIds.add(ids.get(i));
                }
            }
            // Update RecyclerView with filtered data
            recyclerView(filteredTitles, filteredIds);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);
        return true;
    }
}
