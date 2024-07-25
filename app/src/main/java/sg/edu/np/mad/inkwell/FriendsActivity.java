package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FriendsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    String currentFirebaseUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    FirebaseStorage storage = FirebaseStorage.getInstance();

    StorageReference storageRef = storage.getReference();

    public static String selectedFriendId;

    public static String selectedFriendUid;

    public static String selectedFriendEmail;

    ArrayList<Friend> friendList;

    private void recyclerView(ArrayList<Friend> friendList) {
        RecyclerView recyclerView = findViewById(R.id.friendRecyclerView);
        FriendAdapter adapter = new FriendAdapter(friendList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

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

        friendList = new ArrayList<>();

        ImageButton addFriendButton = findViewById(R.id.addFriendButton);

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FriendsActivity.this);
                View view = LayoutInflater.from(FriendsActivity.this).inflate(R.layout.add_friend_bottom_sheet, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                TextInputEditText friendEditText = view.findViewById(R.id.friendEditText);

                Button doneButton = view.findViewById(R.id.doneButton);

                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.collection("users")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                if (document.getData().get("email") != null) {
                                                    if (document.getData().get("email").toString().equals(friendEditText.getText().toString())) {
                                                        Map<String, Object> newFriend = new HashMap<>();
                                                        newFriend.put("friendEmail", friendEditText.getText().toString());
                                                        newFriend.put("uid", currentFirebaseUserUid);

                                                        db.collection("users").document(currentFirebaseUserUid).collection("friends").document(document.getData().get("uid").toString()).set(newFriend);

                                                        Map<String, Object> newFriend2 = new HashMap<>();
                                                        newFriend2.put("friendEmail", currentFirebaseUserEmail);
                                                        newFriend2.put("uid", document.getData().get("uid").toString());

                                                        db.collection("users").document(document.getData().get("uid").toString()).collection("friends").document(currentFirebaseUserUid).set(newFriend2);

                                                        Map<String, Object> newMessage = new HashMap<>();
                                                        newMessage.put("message", "");
                                                        newMessage.put("uid", currentFirebaseUserUid);
                                                        newMessage.put("type", "none");

                                                        db.collection("users").document(currentFirebaseUserUid).collection("friends").document(document.getData().get("uid").toString()).collection("messages").document("0").set(newMessage);
                                                        db.collection("users").document(document.getData().get("uid").toString()).collection("friends").document(currentFirebaseUserUid).collection("messages").document("0").set(newMessage);

                                                        Toast toast = new Toast(FriendsActivity.this);
                                                        toast.setDuration(Toast.LENGTH_SHORT);
                                                        LayoutInflater layoutInflater = (LayoutInflater) FriendsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                        View view = layoutInflater.inflate(R.layout.toast_added, null);
                                                        TextView toastMessage = view.findViewById(R.id.toastMessage);
                                                        toastMessage.setText("Friend Added");
                                                        toast.setView(view);
                                                        toast.show();
                                                    }
                                                }
                                            }
                                        } else {
                                            Log.d("testing", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });

                        bottomSheetDialog.dismiss();
                    }
                });

                Button cancelButton = view.findViewById(R.id.cancelButton);

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        db.collection("users").document(currentFirebaseUserUid).collection("friends")
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
                            String docFriendUid = String.valueOf(dc.getDocument().getData().get("uid"));
                            if (dc.getType() == DocumentChange.Type.ADDED && docFriendUid.equals(currentFirebaseUserUid)) {
                                StorageReference imageRef = storageRef.child("users/" + dc.getDocument().getId() + "/profile.jpg");

                                long ONE_MEGABYTE = 1024 * 1024;

                                imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        Friend friend = new Friend(dc.getDocument().getId(), dc.getDocument().getId(), dc.getDocument().getData().get("friendEmail").toString(), bitmap);
                                        friendList.add(friend);
                                        recyclerView(friendList);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Friend friend = new Friend(dc.getDocument().getId(), dc.getDocument().getId(), dc.getDocument().getData().get("friendEmail").toString(), null);
                                        friendList.add(friend);
                                        recyclerView(friendList);
                                    }
                                });
                            }
                            else if (dc.getType() == DocumentChange.Type.REMOVED && docFriendUid.equals(currentFirebaseUserUid)) {

                            }
                        }
                    }
                });
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