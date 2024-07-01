package sg.edu.np.mad.inkwell;

import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import android.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

public class TimetableActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private LinearLayout slidingPanel;
    private LinearLayout updatePanel;
    private Spinner categorySpinner, updateCategorySpinner;
    private ArrayAdapter<String> adapter;
    private List<String> categoryList;
    private boolean isPanelShown = false;
    private View backgroundOverlay;
    private ImageButton addNewBtn, leftBtn, rightBtn;
    private TextView tvDate;
    private CardView startTime, endTime, startDate, endDate;
    private TextView tvStartTime,tvEndTime;
    private int startHour, startMinute, endHour, endMinute;
    private int startYear, startMonth, startDayOfMonth;
    private int endYear, endMonth, endDayOfMonth;
    private TextView tvStartDate, tvEndDate, updateEventName, updateLocation, updateStartTime;
    private TextView updateStartDate, updateEndTime, updateEndDate,xEvents;
    private EditText etToDo, etLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // finds drawer and nav view before setting listener
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

        // init all ids
        addNewBtn = findViewById(R.id.addNewTaskbtn);
        slidingPanel = findViewById(R.id.slidingPanel);
        updatePanel = findViewById(R.id.updatePanel);
        backgroundOverlay = findViewById(R.id.backgroundOverlay);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        tvDate = findViewById(R.id.tvDate);
        TextView btnClear = findViewById(R.id.btnClear);
        etToDo = findViewById(R.id.etToDo);
        etLocation = findViewById(R.id.etLocation);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvEndTime = findViewById(R.id.tvEndTime);
        TextView btnSave = findViewById(R.id.btnSave);
        leftBtn = findViewById(R.id.leftArrow);
        rightBtn = findViewById(R.id.rightArrow);
        xEvents = findViewById(R.id.x_events);
        updateEventName = findViewById(R.id.updateEventName);
        updateLocation = findViewById(R.id.updateLocation);
        updateStartTime = findViewById(R.id.updateTvStartTime);
        updateStartDate = findViewById(R.id.updateTvStartDate);
        updateEndTime = findViewById(R.id.updateTvEndTime);
        updateEndDate = findViewById(R.id.updateTvEndDate);
        updateCategorySpinner = findViewById(R.id.updateCategorySpinner);

        // set up calendar data
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String currentTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        Date date = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(date);
        String today = dateFormat2.format(date);

        // init Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        ArrayList<TimetableData> eventList = new ArrayList<>();

        // init category list
        categoryList = new ArrayList<>();
        categoryList.add("Class");
        categoryList.add("Meeting");

        if (currentUser != null) {
            String userId = currentUser.getUid();
            initCategories(db, userId);
            createEvents(db, userId);

            // get data from Firestore
            fetchEventData(db, userId, eventList,today);

            // get categories from Firestore and populate categoryList
            fetchCategoriesFromFirestore(categoryList);

            // get time
            tvStartTime.setText(currentTime);
            tvEndTime.setText(currentTime);

            startYear = endYear = calendar.get(Calendar.YEAR);
            startMonth = endMonth = calendar.get(Calendar.MONTH);
            startDayOfMonth = endDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            tvStartDate.setText(formatDate(startYear, startMonth, startDayOfMonth));
            tvEndDate.setText(formatDate(endYear, endMonth, endDayOfMonth));

            tvDate.setText(currentDate);

            // open sldingPanel if add new btn clicked
            addNewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleSlidingPanel();
                }
            });

            // close sliding panel if bg overlay clicked
            backgroundOverlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideSlidingPanel();
                    hideUpdatePanel();
                }
            });

            // show startTimePicker dialog
            startTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePickerDialog(true);
                }
            });

            // show startDatePicker dialog
            startDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(true);
                }
            });

            // show endTimePicker dialog
            endTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showTimePickerDialog(false);
                }
            });

            // show endDatePicker dialog
            endDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(false);
                }
            });

            updateStartTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTimePickerDialog(true);
                }
            });

            // show startDatePicker dialog
            updateStartDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(true);
                }
            });

            // show endTimePicker dialog
            updateEndTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showTimePickerDialog(false);
                }
            });

            // show endDatePicker dialog
            updateEndDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDatePickerDialog(false);
                }
            });

            // left button to navigate to yest schedule
            leftBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Decrement the calendar by one day
                    calendar.add(Calendar.DAY_OF_MONTH, -1);

                    // format the previous day date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                    String previousDay = dateFormat.format(calendar.getTime());

                    // update tvDate
                    tvDate.setText(previousDay);

                    // format date for filtering
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String prevDay = dateFormat2.format(calendar.getTime());
                    Log.d("PREV DAY",prevDay);

                    // filter the event list
                    fetchEventData(db, userId, eventList,prevDay);
                }
            });

            // right button to navigate to tmr schedule
            rightBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // increment the calendar by one day
                    calendar.add(Calendar.DAY_OF_MONTH, 1);

                    // format the previous day date
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
                    String tmr = dateFormat.format(calendar.getTime());

                    // update tvDate with the formatted previous day date
                    tvDate.setText(tmr);

                    // format date for filtering
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String nextDay = dateFormat2.format(calendar.getTime());

                    Log.d("NEXT DAY",nextDay);

                    // set up recyclerView for next day
                    fetchEventData(db, userId, eventList,nextDay);
                }
            });

            // clear all data inputted onClick
            btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearInputData();
                }
            });

            // save new event data to db
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!etToDo.getText().toString().isEmpty()) {
                        if (!etLocation.getText().toString().isEmpty()) {
                            try {
                                Map<String, Object> eventData = new HashMap<>();
                                eventData.put("eventName", etToDo.getText().toString());
                                eventData.put("eventLocation", etLocation.getText().toString());
                                eventData.put("startTime", tvStartTime.getText().toString());
                                eventData.put("startDate", tvStartDate.getText().toString());
                                eventData.put("endTime", tvEndTime.getText().toString());
                                eventData.put("endDate", tvEndDate.getText().toString());
                                eventData.put("category", categorySpinner.getSelectedItem().toString());

                                TimetableData data = new TimetableData(etToDo.getText().toString(), etLocation.getText().toString(),
                                        tvStartTime.getText().toString(), tvStartDate.getText().toString(), tvEndTime.getText().toString(),
                                        tvEndDate.getText().toString(), categorySpinner.getSelectedItem().toString());

                                db.collection("users").document(userId).collection("timetableEvents").add(eventData);
                                eventList.add(data);

                                // close sliding panel and remove data for next use
                                hideSlidingPanel();
                                clearInputData();

                                // set up reyclerView
                                filter(eventList, today);

                                Toast toast = new Toast(TimetableActivity.this);
                                toast.setDuration(Toast.LENGTH_SHORT);

                                LayoutInflater layoutInflater = (LayoutInflater) TimetableActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View view = layoutInflater.inflate(R.layout.toast_added, null);
                                toast.setView(view);
                                toast.show();

                                Toast.makeText(TimetableActivity.this, "Data saved successfully.", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e("SaveButtonClick", "Error saving data: " + e.getMessage(), e);

                                Toast.makeText(TimetableActivity.this, "Error saving data. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(TimetableActivity.this, "Add Location!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(TimetableActivity.this, "Add Event Name!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // get selected item from spinner onCLick
            updateCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedCategory = parent.getItemAtPosition(position).toString();
                    if (selectedCategory.equals("Add New Option")) {
                        showAddOptionPopup();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedCategory = parent.getItemAtPosition(position).toString();
                    if (selectedCategory.equals("Add New Option")) {
                        showAddOptionPopup();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            adapter.notifyDataSetChanged();
        }
    }

    // get event data from db
    private void fetchEventData(FirebaseFirestore db, String userId, ArrayList<TimetableData> eventList, String date) {
        db.collection("users").document(userId).collection("timetableEvents")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            eventList.clear(); // Clear existing data
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // get all data
                                String docId = document.getId();
                                String name = document.getString("eventName");
                                String loc = document.getString("eventLocation");
                                String startTime = document.getString("startTime");
                                String endTime = document.getString("endTime");
                                String startDate = document.getString("startDate");
                                String endDate = document.getString("endDate");
                                String category = document.getString("category");
                                if (name != null && name != "" && startDate.equals(date)) {
                                    TimetableData data = new TimetableData(name, loc, startTime, startDate, endTime, endDate, category);
                                    data.setDocumentId(docId);
                                    eventList.add(data);
                                }
                            }
                        }

                        // get size of event list
                        String eventText = "You have " + eventList.size() + " events today";
                        xEvents.setText(eventText);

                        // set up recyclerView
                        filter(eventList,date);
                    }
                });
    }

    // get categories from db
    private void fetchCategoriesFromFirestore(List<String> catList) {
        categorySpinner = findViewById(R.id.categorySpinner);
        updateCategorySpinner = findViewById(R.id.updateCategorySpinner);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        updateCategorySpinner.setAdapter(adapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).collection("categories")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String category = document.getString("name");
                                    if (category != null && !catList.contains(category)) {
                                        catList.add(category);
                                    }
                                }
                                // add "Add New Option" at the end of the list
                                catList.add("Add New Option");
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.w("Firestore", "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
    }

    // show add category pop up onClick
    private void showAddOptionPopup() {
        // init data needed
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_new_cat_popup, null);
        builder.setView(dialogView);

        final EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        final LinearLayout colorLayout = dialogView.findViewById(R.id.colorLayout);
        Button btnAddCategory = dialogView.findViewById(R.id.btnAddCategory);

        final AlertDialog dialog = builder.create();

        // add color buttons dynamically
        int[] colors = {R.color.pastelCoral, R.color.pastelBlue, R.color.pastelGreen, R.color.pastelPurple, R.color.pastelYellow, R.color.pastelOrange};
        for (final int color : colors) {
            Button colorButton = new Button(this);
            colorButton.setBackgroundColor(ContextCompat.getColor(this, color));
            colorButton.setTag(color);
            colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(true);
                    for (int i = 0; i < colorLayout.getChildCount(); i++) {
                        View child = colorLayout.getChildAt(i);
                        if (child != v) {
                            child.setSelected(false);
                        }
                    }
                }
            });

            // set up colour button size
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.color_button_size),
                    getResources().getDimensionPixelSize(R.dimen.color_button_size)
            );
            params.setMargins(10, 10, 10, 10);
            colorButton.setLayoutParams(params);

            // add a circular background programmatically
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(ContextCompat.getColor(this, color));
            colorButton.setBackground(shape);

            // add colour button to colour layout
            colorLayout.addView(colorButton);

            colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isSelected = v.isSelected();
                    v.setSelected(!isSelected);

                    if (isSelected) {
                        // restore original size
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
                        params.width = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                        params.height = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                        v.setLayoutParams(params);
                    } else {
                        // enlarge button when selected
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
                        params.width = getResources().getDimensionPixelSize(R.dimen.color_button_selected_size);
                        params.height = getResources().getDimensionPixelSize(R.dimen.color_button_selected_size);
                        v.setLayoutParams(params);
                    }

                    for (int i = 0; i < colorLayout.getChildCount(); i++) {
                        View child = colorLayout.getChildAt(i);
                        if (child != v) {
                            child.setSelected(false);
                            // restore original size for unselected buttons
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
                            params.width = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                            params.height = getResources().getDimensionPixelSize(R.dimen.color_button_size);
                            child.setLayoutParams(params);
                        }
                    }
                }
            });
        }

        // add new category to collection onClick
        btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get user input data
                String categoryName = etCategoryName.getText().toString().trim();
                int selectedColor = -1;
                for (int i = 0; i < colorLayout.getChildCount(); i++) {
                    View child = colorLayout.getChildAt(i);
                    if (child.isSelected()) {
                        selectedColor = (int) child.getTag();
                        break;
                    }
                }

                if (!categoryName.isEmpty() && selectedColor != -1 && userId != null) {
                    // get selected color as a hex string
                    String hexColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(TimetableActivity.this, selectedColor)));

                    // add category to Firestore
                    addCategoryToFirestore(userId, categoryName, hexColor);
                    dialog.dismiss(); // close the dialog after adding the category
                } else {
                    // Show error message
                    if (categoryName.isEmpty()) {
                        etCategoryName.setError("Category name required");
                    }
                    if (selectedColor == -1) {
                        Toast.makeText(TimetableActivity.this, "Select colour!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dialog.show();
    }

    // show data of specific item onClick
    public void onItemClick(TimetableData item, String userId, String documentId, FirebaseFirestore db, ArrayList<TimetableData> eventList, String date) {
        toggleUpdatePanel();
        TextView updateBtn = findViewById(R.id.btnUpdate);
        TextView delBtn = findViewById(R.id.btnDelete);

        updateBtn.setText(R.string.save);
        delBtn.setText(R.string.bottom_sheet_delete_button);
        updateEventName.setText(item.getName());
        updateLocation.setText(item.getLocation());
        updateStartTime.setText(item.getStartTime());
        updateStartDate.setText(item.getStartDate());
        updateEndTime.setText(item.getEndTime());
        updateEndDate.setText(item.getEndDate());

        // delete data from db
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInputData();
                hideUpdatePanel();
                deleteEventData(userId, documentId);
                fetchEventData(db, userId, eventList, date);
            }
        });

        // update date to db
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> eventData = new HashMap<>();
                eventData.put("eventName", updateEventName.getText().toString());
                eventData.put("eventLocation", updateLocation.getText().toString());
                eventData.put("startTime", updateStartTime.getText().toString());
                eventData.put("startDate", updateStartDate.getText().toString());
                eventData.put("endTime", updateEndTime.getText().toString());
                eventData.put("endDate", updateEndDate.getText().toString());
                eventData.put("category", updateCategorySpinner.getSelectedItem().toString());

                hideUpdatePanel();
                updateTimetableEvent(documentId, eventData, db, userId, eventList, date);
                fetchEventData(db, userId, eventList, date);
            }
        });
    }

    // add new category to db
    private void addCategoryToFirestore(String userId, String categoryName, String colorHex) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // create a new category object
        Map<String, Object> category = new HashMap<>();
        category.put("name", categoryName);
        category.put("color", colorHex);

        // cdd the category to Firestore under the user's collection
        db.collection("users").document(userId).collection("categories").document(categoryName)
                .set(category)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(TimetableActivity.this, "Category added", Toast.LENGTH_SHORT).show();

                        // update the spinner with the new category
                        categoryList.add(categoryList.size() - 1, categoryName); // Add before "Add New Option"
                        adapter.notifyDataSetChanged();

                        // set the spinner to the newly added category
                        categorySpinner.setSelection(categoryList.size() - 2); // Select the new category
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TimetableActivity.this, "Error adding category", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // show timepicker dialog
    private void showTimePickerDialog(final boolean isStartTime) {
        int hour = isStartTime ? startHour : endHour;
        int minute = isStartTime ? startMinute : endMinute;

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                // if true, change startTime text
                if (isStartTime) {
                    startHour = selectedHour;
                    startMinute = selectedMinute;
                    tvStartTime.setText(String.format("%02d:%02d", startHour, startMinute));
                    updateStartTime.setText(String.format("%02d:%02d", startHour, startMinute));
                } // if false, change endTime text
                else {
                    endHour = selectedHour;
                    endMinute = selectedMinute;
                    tvEndTime.setText(String.format("%02d:%02d", endHour, endMinute));
                    updateEndTime.setText(String.format("%02d:%02d", endHour, endMinute));
                }
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    // show datepicker dialog
    private void showDatePickerDialog(final boolean isStartDate) {
        int year = isStartDate ? startYear : endYear;
        int month = isStartDate ? startMonth : endMonth;
        int dayOfMonth = isStartDate ? startDayOfMonth : endDayOfMonth;

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // if true, change tvStartDate
                        if (isStartDate) {
                            startYear = year;
                            startMonth = monthOfYear;
                            startDayOfMonth = dayOfMonth;
                            tvStartDate.setText(formatDate(startYear, startMonth, startDayOfMonth));
                            updateStartDate.setText(formatDate(startYear, startMonth, startDayOfMonth));
                        } // if false, change tvEndDate
                        else {
                            endYear = year;
                            endMonth = monthOfYear;
                            endDayOfMonth = dayOfMonth;
                            tvEndDate.setText(formatDate(endYear, endMonth, endDayOfMonth));
                            updateEndDate.setText(formatDate(endYear, endMonth, endDayOfMonth));
                        }
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    // use to format date in dd-mm-yyyy
    private String formatDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    // panel slides up
    private void toggleSlidingPanel() {
        if (!isPanelShown) {
            // Slide up animation
            Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
            slidingPanel.startAnimation(slideUp);
            slidingPanel.setVisibility(View.VISIBLE);
            backgroundOverlay.setVisibility(View.VISIBLE);
            isPanelShown = true;
        }
    }
    private void toggleUpdatePanel() {
        if (!isPanelShown) {
            // Slide up animation
            Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
            updatePanel.startAnimation(slideUp);
            updatePanel.setVisibility(View.VISIBLE);
            backgroundOverlay.setVisibility(View.VISIBLE);
            isPanelShown = true;
        }
    }

    // panel slides down
    private void hideSlidingPanel() {
        // Slide down animation
        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        slidingPanel.startAnimation(slideDown);
        slidingPanel.setVisibility(View.GONE);
        backgroundOverlay.setVisibility(View.GONE);
        isPanelShown = false;
        clearInputData();
    }
    private void hideUpdatePanel() {
        // Slide down animation
        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        updatePanel.startAnimation(slideDown);
        updatePanel.setVisibility(View.GONE);
        backgroundOverlay.setVisibility(View.GONE);
        isPanelShown = false;
        clearInputData();
    }


    // clear all input data from add new event
    private void clearInputData() {
        TimeZone singaporeTimeZone = TimeZone.getTimeZone("Asia/Singapore");
        EditText etToDo = findViewById(R.id.etToDo);
        EditText etLocation = findViewById(R.id.etLocation);
        Calendar calendar = Calendar.getInstance(singaporeTimeZone);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        Date date = calendar.getTime();
        String currentTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(date);

        etToDo.setText("");
        etLocation.setText("");
        tvStartTime.setText(currentTime);
        tvEndTime.setText(currentTime);
        tvStartDate.setText(currentDate);
        tvEndDate.setText(currentDate);
    }

    // add the collections category to specific user
    private void initCategories(FirebaseFirestore db, String userId) {
        // Reference the categories collection under the user's document
        CollectionReference categoriesCollection = db.collection("users").document(userId).collection("categories");

        // Check if the "Class" category exists
        categoriesCollection.document("Class").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().exists()) {
                            Map<String, Object> classCategory = new HashMap<>();
                            classCategory.put("name", "Class");
                            classCategory.put("color", "#ffc6ff");

                            categoriesCollection.document("Class").set(classCategory)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Firestore", "Class category added successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Firestore", "Error adding class category", e);
                                        }
                                    });
                        } else {
                            Log.d("Firestore", "Class category already exists");
                        }
                    }
                });

        // Check if the "Meeting" category exists
        categoriesCollection.document("Meeting").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().exists()) {
                            Map<String, Object> meetingCategory = new HashMap<>();
                            meetingCategory.put("name", "Meeting");
                            meetingCategory.put("color", "#ffc09f");

                            categoriesCollection.document("Meeting").set(meetingCategory)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Firestore", "Meeting category added successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Firestore", "Error adding meeting category", e);
                                        }
                                    });
                        } else {
                            Log.d("Firestore", "Meeting category already exists");
                        }
                    }
                });
    }

    // create events collection
    private void createEvents(FirebaseFirestore db, String userId) {
        // Reference the events collection under the user's document
        CollectionReference eventsCollection = db.collection("users").document(userId).collection("timetableEvents");

        // Check if the events collection already exists
        eventsCollection.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                // Events collection doesn't exist, create it with fields
                                Map<String, Object> eventData = new HashMap<>();
                                eventData.put("eventName", "");
                                eventData.put("eventLocation","");
                                eventData.put("startTime", "");
                                eventData.put("endTime", "");
                                eventData.put("category", "");

                                // Create the events collection with a dummy document and fields
                                eventsCollection.document("0").set(eventData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Firestore", "Empty events collection created for user: " + userId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Firestore", "Error creating empty events collection", e);
                                            }
                                        });
                            } else {
                                // Events collection already exists, check if it's empty and log a message
                                eventsCollection.limit(1).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if (task.getResult().isEmpty()) {
                                                        Log.d("Firestore", "Events collection already exists but is empty for user: " + userId);
                                                    } else {
                                                        Log.d("Firestore", "Events collection already exists and is not empty for user: " + userId);
                                                    }
                                                } else {
                                                    Log.e("Firestore", "Error checking events collection", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.e("Firestore", "Error checking events collection", task.getException());
                        }
                    }
                });
    }

    // set up recyclerView
    private void recyclerView(ArrayList<TimetableData> eventList, ArrayList<TimetableData> event) {
        Animation slideInLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_left);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        TimetableAdapter adapter = new TimetableAdapter(eventList, event, this);

        // snsure the layout manager is set to enable scrolling
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // set item animator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // set the adapter
        recyclerView.setAdapter(adapter);

        // Notify the adapter of data changes
        recyclerView.startAnimation(slideInLeft);
        adapter.notifyDataSetChanged();
    }

    // filter data to bring out only current date
    private void filter(ArrayList<TimetableData> eventList, String date) {
        ArrayList<TimetableData> filterList = new ArrayList<>();
        for (TimetableData data : eventList) {
            if (data.getStartDate() != null && data.getStartDate().equals(date)) {
                filterList.add(data);
            }
        }

        // get size of event list
        String eventText = "You have " + filterList.size() + " events today";
        xEvents.setText(eventText);

        // Sort the filterList based on startTime and endTime
        Collections.sort(eventList, new Comparator<TimetableData>() {
            @Override
            public int compare(TimetableData data1, TimetableData data2) {

                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                try {
                    Date startTime1 = format.parse(data1.getStartTime());
                    Date startTime2 = format.parse(data2.getStartTime());
                    // Compare the startTime of the two objects
                    return startTime1.compareTo(startTime2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return 0;
            }
        });

        recyclerView(eventList, filterList);
    }

    // delete event data
    private void deleteEventData(String userId, String documentId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("users")
                .document(userId)
                .collection("timetableEvents")
                .document(documentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Document successfully deleted
                        // You can perform any additional actions here, such as updating UI or showing a toast
                        Toast.makeText(getApplicationContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors that occurred during deletion
                        Log.e("DeleteEvent", "Error deleting event: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error deleting event", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // update event data
    public void updateTimetableEvent(String documentId, Map<String, Object> newData, FirebaseFirestore db, String userId, ArrayList<TimetableData> eventsList, String date) {
        db.collection("users").document(userId).collection("timetableEvents").document(documentId)
                .update(newData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fetchEventData(db, userId, eventsList, date);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TimetableAdapter", "Error updating document", e);
                        // Handle error, e.g., show a message to the user
                    }
                });
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