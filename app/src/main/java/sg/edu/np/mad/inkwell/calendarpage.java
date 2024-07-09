package sg.edu.np.mad.inkwell;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;


public class calendarpage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private CalendarView calendarView;
    private TextView eventDescriptionTextView;
    private EditText dateEditText;
    private Button datePickerButton;

    private RecyclerView eventsRecyclerView;
    private EventsAdapter eventsAdapter;

    private List<String> eventsList;
    private FirebaseFirestore db;
    private String userId;
    public static final String CHANNEL_ID = "EventNotificationChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendarpage);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Finds nav bar drawer and nav view before setting listener
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Sets listener to allows for closing and opening of the navbar
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        calendarView = findViewById(R.id.calendarView);
        eventDescriptionTextView = findViewById(R.id.eventDescription);
        dateEditText = findViewById(R.id.dateEditText);
        datePickerButton = findViewById(R.id.datePickerButton);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsList = new ArrayList<>();
        eventsAdapter = new EventsAdapter(eventsList, this::editDelete);
        eventsRecyclerView.setAdapter(eventsAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();


        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {    userId = currentUser.getUid();
        } else {    Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            finish();}
        //userId = "8dvh5X3c5sYMOOhHhvv9w5ZvL2n2"; //temporary testing placeholder



        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                displayEventsForDate(date);
                eventsList(date);
            }
        });

        datePickerButton.setOnClickListener(v -> showDatePickerDialog());

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event Notification";
            String description = "Notification channel for event reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void displayEventsForDate(String date) {
        db.collection("users")
                .document(userId)
                .collection("events").document(date).get()
                .addOnSuccessListener(documentSnapshot -> {
                    eventsList.clear();
                    if (documentSnapshot.exists()) {
                        List<String> events = (List<String>) documentSnapshot.get("events");
                        if (events != null) {
                            eventsList.addAll(events);
                        }
                    }
                    eventsAdapter.notifyDataSetChanged();
                    eventDescriptionTextView.setText("Events on " + date + ":");
                })
                .addOnFailureListener(e -> Toast.makeText(calendarpage.this, "Failed to retrieve events: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        dateEditText.setText(selectedDate);
                        displayEventsForDate(selectedDate);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void editDelete(int position) {
        String date = dateEditText.getText().toString();
        if (!date.isEmpty()) {
            eventsList(date, position);
        }
    }

    private void eventsList(final String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Click on event to edit or delete.");

        db.collection("users")
                .document(userId)
                .collection("events").document(date).get()

                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> events = (List<String>) documentSnapshot.get("events");
                            if (events != null && !events.isEmpty()) {
                                String[] eventsArray = events.toArray(new String[0]);
                                builder.setItems(eventsArray, (dialog, which) -> editDelete(date, which));
                            } else {
                                builder.setMessage("No events for this day.");
                            }
                        } else {
                            builder.setMessage("No events for this day.");
                        }

                        builder.setPositiveButton("Create Event", (dialog, which) -> addEvent(date));
                        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
                        builder.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(calendarpage.this, "Failed to retrieve events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        builder.setMessage("Failed to retrieve events.").setPositiveButton("Create Event", (dialog, which) -> addEvent(date));
                        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
                        builder.show();
                    }
                });
    }

    private void editDelete(final String date, final int eventIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit or Delete Event");

        builder.setPositiveButton("Edit", (dialog, which) -> editEvent(date, eventIndex));

        builder.setNegativeButton("Delete", (dialog, which) -> {
            db.collection("users")
                    .document(userId)
                    .collection("events").document(date).get()

                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                List<String> events = (List<String>) documentSnapshot.get("events");
                                if (events != null && eventIndex < events.size()) {
                                    events.remove(eventIndex);

                                    db.collection("users")
                                            .document(userId)
                                            .collection("events")
                                            .document(date)
                                            .update("events", events)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    displayEventsForDate(date);
                                                    Toast.makeText(calendarpage.this, "Event Deleted", Toast.LENGTH_SHORT).show();
                                                    showNotification("Event Deleted", "Your event has been deleted successfully.");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    Toast.makeText(calendarpage.this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(calendarpage.this, "Failed to retrieve events for deletion: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void eventsList(final String date, final int eventIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Click on event to edit or delete.");

        db.collection("users")
                .document(userId)
                .collection("events").document(date).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> events = (List<String>) documentSnapshot.get("events");
                        if (events != null && !events.isEmpty()) {
                            builder.setItems(events.toArray(new String[0]), (dialog, which) -> editDeleteEvent(date, which));
                        } else {
                            builder.setMessage("No events for this day.");
                        }
                    } else {
                        builder.setMessage("No events for this day.");
                    }

                    builder.setPositiveButton("Create Event", (dialog, which) -> addEvent(date));
                    builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
                    builder.show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(calendarpage.this, "Failed to retrieve events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    builder.setMessage("Failed to retrieve events.").setPositiveButton("Create Event", (dialog, which) -> addEvent(date));
                    builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
                    builder.show();
                });
    }

    private void editDeleteEvent(final String date, final int eventIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit or Delete Event");

        builder.setPositiveButton("Edit", (dialog, which) -> editEvent(date, eventIndex));

        builder.setNegativeButton("Delete", (dialog, which) -> {
            db.collection("users")
                    .document(userId)
                    .collection("events").document(date).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> events = (List<String>) documentSnapshot.get("events");
                            if (events != null && eventIndex < events.size()) {
                                events.remove(eventIndex);

                                db.collection("users")
                                        .document(userId)
                                        .collection("events")
                                        .document(date)
                                        .update("events", events)
                                        .addOnSuccessListener(aVoid -> {
                                            displayEventsForDate(date);
                                            Toast.makeText(calendarpage.this, "Event Deleted", Toast.LENGTH_SHORT).show();
                                            showNotification("Event Deleted", "Your event has been deleted successfully.");
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(calendarpage.this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(calendarpage.this, "Failed to retrieve events for deletion: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void editEvent(final String date, final int eventIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Event on " + date);

        final EditText input = new EditText(this);
        db.collection("users")
                .document(userId)
                .collection("events").document(date).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            List<String> events = (List<String>) documentSnapshot.get("events");
                            if (events != null && eventIndex < events.size()) {
                                input.setText(events.get(eventIndex));
                            }
                        }
                    }
                });

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String eventDescription = input.getText().toString();
            if (!eventDescription.isEmpty()) {
                db.collection("users")
                        .document(userId)
                        .collection("events").document(date).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    List<String> events = (List<String>) documentSnapshot.get("events");
                                    if (events != null && eventIndex < events.size()) {
                                        events.set(eventIndex, eventDescription);
                                        db.collection("users")
                                                .document(userId)
                                                .collection("events").document(date)
                                                .update("events", events)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        displayEventsForDate(date);
                                                        setAlarmForEvent(date);
                                                        Toast.makeText(calendarpage.this, "Event Edited", Toast.LENGTH_SHORT).show();
                                                        showNotification("Event Edited", "Your event has been edited successfully.");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        Toast.makeText(calendarpage.this, "Failed to edit event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }
                            }
                        });
            } else {
                Toast.makeText(calendarpage.this, "Event Description Cannot Be Empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addEvent(final String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Event on " + date);

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String eventDescription = input.getText().toString();
            if (!eventDescription.isEmpty()) {
                db.collection("users")
                        .document(userId)
                        .collection("events").document(date).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                List<String> events = new ArrayList<>();
                                if (documentSnapshot.exists()) {
                                    events = (List<String>) documentSnapshot.get("events");
                                    if (events == null) {
                                        events = new ArrayList<>();
                                    }
                                }
                                events.add(eventDescription);
                                db.collection("users")
                                        .document(userId)
                                        .collection("events").document(date)
                                        .set(Collections.singletonMap("events", events), SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                displayEventsForDate(date);
                                                setAlarmForEvent(date);
                                                Toast.makeText(calendarpage.this, "Event Saved", Toast.LENGTH_SHORT).show();
                                                showNotification("Event Created", "Your event has been created successfully.");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception e) {
                                                Toast.makeText(calendarpage.this, "Failed to save event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
            } else {
                Toast.makeText(calendarpage.this, "Event Description Cannot Be Empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void setAlarmForEvent(String date) {
        String[] dateParts = date.split("/");
        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1;
        int year = Integer.parseInt(dateParts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0); // Notify at 12:00 AM on the event day

        Intent intent = new Intent(this, EventAlarmReceiver.class);
        intent.putExtra("event_date", date);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        // Show notification immediately after setting the alarm
        showNotification("Event Reminder Set", "A reminder has been set for your event on " + date);
    }

    private void showNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.plus)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);
        return true;

    }
}