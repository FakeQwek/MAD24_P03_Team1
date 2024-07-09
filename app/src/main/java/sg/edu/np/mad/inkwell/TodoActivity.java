package sg.edu.np.mad.inkwell;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TodoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables

    // currentTodoId keeps track of the ids that have already been assigned
    private int currentTodoId;

    public static String currentStatus = "todo";

    private ArrayList<Todo> todos;

    private int todoCount;

    int hour = 0;

    int minute = 0;

    int year;

    int month;

    int day;

    Date date;

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<Todo> allTodos, ArrayList<Todo> todos) {
        RecyclerView recyclerView = findViewById(R.id.todoRecyclerView);
        TodoAdapter adapter = new TodoAdapter(allTodos, todos, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Method to filter items already in the recycler view
    private void filter(ArrayList<Todo> allTodos, String status, String query) {
        ArrayList<Todo> filterList = new ArrayList<>();
        for (Todo todo : allTodos){
            if(todo.getTodoStatus().equals(status) && todo.getTodoTitle().toLowerCase().contains(query)) {
                filterList.add(todo);
            }
        }
        todos = filterList;
        recyclerView(allTodos, filterList);
    }

    // Create notification channel for sending notifications
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Todo";
            String description = "todo";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("Todo", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

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

        createNotificationChannel();

        ArrayList<Todo> allTodos = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.todoRecyclerView);

        TextView todoCounter = findViewById(R.id.todoCounter);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Read from firebase and create todos on create
        db.collection("users").document(currentFirebaseUserUid).collection("todos")
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
                            if (Integer.parseInt(dc.getDocument().getId()) > currentTodoId) {
                                currentTodoId = Integer.parseInt(dc.getDocument().getId());
                            }
                            String docTodoUid = String.valueOf(dc.getDocument().getData().get("uid"));
                            if (dc.getType() == DocumentChange.Type.ADDED && docTodoUid.equals(currentFirebaseUserUid)) {
                                Todo todo = new Todo(dc.getDocument().getData().get("title").toString(), Integer.parseInt(dc.getDocument().getId()), dc.getDocument().getData().get("description").toString(), dc.getDocument().getData().get("dateTime").toString(), dc.getDocument().getData().get("status").toString());
                                if (dc.getDocument().getData().get("status").toString().equals("todo")) {
                                    todoCount++;
                                }
                                todoCounter.setText(String.format(getResources().getString(R.string.todo_counter), todoCount));

                                allTodos.add(todo);
                                filter(allTodos, "todo", "");
                            }
                            else if (dc.getType() == DocumentChange.Type.REMOVED && docTodoUid.equals(currentFirebaseUserUid)) {
                                if (dc.getDocument().getData().get("status").toString().equals("todo")) {
                                    todoCount--;
                                }
                                todoCounter.setText(String.format(getResources().getString(R.string.todo_counter), todoCount));
                            }
                        }
                    }
                });

        ImageButton addTodoButton = findViewById(R.id.addTodoButton);

        // Brings up a menu to create a todo
        addTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(TodoActivity.this);
                View view = LayoutInflater.from(TodoActivity.this).inflate(R.layout.add_todo_bottom_sheet, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                TextInputEditText titleEditText = view.findViewById(R.id.titleEditText);
                TextInputEditText descriptionEditText = view.findViewById(R.id.descriptionEditText);

                Button timePickerButton = view.findViewById(R.id.timePickerButton);

                timePickerButton.setText("00:00");

                // Brings up a time picker
                timePickerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                            {
                                hour = selectedHour;
                                minute = selectedMinute;
                                timePickerButton.setText(String.format(Locale.getDefault(), "%02d:%02d",hour, minute));
                            }
                        };

                        TimePickerDialog timePickerDialog = new TimePickerDialog(TodoActivity.this, onTimeSetListener, hour, minute, true);

                        timePickerDialog.show();
                    }
                });

                Button datePickerButton = view.findViewById(R.id.datePickerButton);

                Calendar calendar = Calendar.getInstance();

                datePickerButton.setText(String.format(Locale.getDefault(), "%02d/%02d/%02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));

                // Brings up a date picker
                datePickerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Calendar calendar = Calendar.getInstance();

                        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                year = selectedYear;
                                month = selectedMonth + 1;
                                day = selectedDay;

                                datePickerButton.setText(String.format(Locale.getDefault(), "%02d/%02d/%02d", day, month, year));
                            }
                        };

                        DatePickerDialog datePickerDialog = new DatePickerDialog(TodoActivity.this, onDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                        datePickerDialog.show();
                    }
                });

                Button createTodoButton = view.findViewById(R.id.createTodoButton);

                // Adds a todo to firebase
                createTodoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> todoData = new HashMap<>();
                        todoData.put("title", titleEditText.getText().toString());
                        todoData.put("description", descriptionEditText.getText().toString());
                        todoData.put("dateTime", simpleDateFormat.format(Calendar.getInstance().getTime()));
                        todoData.put("status", "todo");
                        todoData.put("uid", currentFirebaseUserUid);
                        db.collection("users").document(currentFirebaseUserUid).collection("todos").document(String.valueOf(currentTodoId + 1)).set(todoData);
                        bottomSheetDialog.dismiss();

                        Intent intent = new Intent(TodoActivity.this, TodoBroadcast.class);
                        Date date = new Date();
                        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(date));
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(TodoActivity.this, id, intent, PendingIntent.FLAG_IMMUTABLE);
                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                        try {
                            date = simpleDateFormat.parse(datePickerButton.getText().toString());
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                        long time = hour * 3600000 + minute * 60000;

                        if (date.getTime() + time > Calendar.getInstance().getTimeInMillis()) {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime() + time, pendingIntent);
                        }

                        Toast toast = new Toast(TodoActivity.this);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        LayoutInflater layoutInflater = (LayoutInflater) TodoActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View view = layoutInflater.inflate(R.layout.toast_added, null);
                        toast.setView(view);
                        toast.show();
                    }
                });

                Button cancelButton = view.findViewById(R.id.cancelButton);

                // Cancels the process
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });

        Button todoButton = findViewById(R.id.todoButton);

        // Set recycler view to only show todos with status todo
        todoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStatus = "todo";

                filter(allTodos, "todo", "");
            }
        });

        Button inProgressButton = findViewById(R.id.inProgressButton);

        // Set recycler view to only show todos with status inProgress
        inProgressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStatus = "inProgress";

                filter(allTodos, "inProgress", "");
            }
        });

        Button doneButton = findViewById(R.id.doneButton);

        // Set recycler view to only show todos with status done
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStatus = "done";

                filter(allTodos, "done", "");
            }
        });

        SearchView searchView = findViewById(R.id.searchView);

        // Search the items in recycler view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(allTodos, currentStatus, query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(allTodos, currentStatus, newText);
                return false;
            }
        });

        // Allows for recycler view items to be swiped
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Removes the item from the recycler view and deletes its data from firebase
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                Todo todo = todos.get(position);
                db.collection("users").document(currentFirebaseUserUid).collection("todos").document(String.valueOf(todo.todoId)).delete();
                allTodos.remove(todo);
                todos.remove(todo);
                recyclerView.getAdapter().notifyItemRemoved(position);
                Toast toast = new Toast(TodoActivity.this);
                toast.setDuration(Toast.LENGTH_SHORT);
                LayoutInflater layoutInflater = (LayoutInflater) TodoActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.toast_deleted, null);
                toast.setView(view);
                toast.show();
            }

            // Only swipes the item away if 80% of it is off the screen
            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.80f;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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