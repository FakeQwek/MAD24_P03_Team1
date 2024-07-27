package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private int currentMessageId;

    ArrayList<Message> messageList;

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<Message> messageList) {
        messageList.sort(Comparator.comparingInt(Message::getId));
        RecyclerView recyclerView = findViewById(R.id.messageRecyclerView);
        MessageAdapter adapter = new MessageAdapter(messageList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Sets toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Finds drawer and nav view before setting listener
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

        messageList = new ArrayList<>();

        TextInputEditText messageEditText = findViewById(R.id.messageEditText);

        ImageButton sendButton = findViewById(R.id.sendButton);

        // send message content in the input text box when user clicks this button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messageEditText.getText().toString().isEmpty()) {
                    currentMessageId += 1;

                    Map<String, Object> newMessage = new HashMap<>();
                    newMessage.put("message", messageEditText.getText().toString());
                    newMessage.put("uid", currentFirebaseUserUid);
                    newMessage.put("type", "sent");

                    db.collection("users").document(currentFirebaseUserUid).collection("friends").document(String.valueOf(FriendsActivity.selectedFriendId)).collection("messages").document(String.valueOf(currentMessageId)).set(newMessage);

                    Map<String, Object> newMessage2 = new HashMap<>();
                    newMessage2.put("message", messageEditText.getText().toString());
                    newMessage2.put("uid", FriendsActivity.selectedFriendUid);
                    newMessage2.put("type", "received");

                    db.collection("users").document(FriendsActivity.selectedFriendUid).collection("friends").document(currentFirebaseUserUid).collection("messages").document(String.valueOf(currentMessageId)).set(newMessage2);

                    messageEditText.setText("");
                }
            }
        });

        TextView friendEmail = findViewById(R.id.friendEmail);

        friendEmail.setText(FriendsActivity.selectedFriendEmail);
    }

    protected void onStart() {
        super.onStart();

        // get data of messages from firebase
        db.collection("users").document(currentFirebaseUserUid).collection("friends").document(String.valueOf(FriendsActivity.selectedFriendId)).collection("messages")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("testing", "listen:error", e);
                            return;
                        }

                        // Adds items to recycler view on create and everytime new data is added to firebase
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (Integer.parseInt(dc.getDocument().getId()) > currentMessageId) {
                                currentMessageId = Integer.parseInt(dc.getDocument().getId());
                            }
                            String docMessageUid = String.valueOf(dc.getDocument().getData().get("uid"));
                            String docMessageType = String.valueOf(dc.getDocument().getData().get("type"));
                            if (dc.getType() == DocumentChange.Type.ADDED && docMessageUid.equals(currentFirebaseUserUid) && (docMessageType.equals("sent") || docMessageType.equals("received"))) {
                                Message message = new Message(Integer.parseInt(dc.getDocument().getId()), dc.getDocument().getData().get("message").toString(), dc.getDocument().getData().get("type").toString());
                                messageList.add(message);
                                recyclerView(messageList);
                            }
                            else if (dc.getType() == DocumentChange.Type.REMOVED && docMessageUid.equals(currentFirebaseUserUid)) {

                            }
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Allows movement between activities upon clicking
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);

        return true;
    }
}