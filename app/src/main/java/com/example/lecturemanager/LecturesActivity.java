package com.example.lecturemanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LecturesActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView rvLectures;
    private FloatingActionButton fabAddLecture;
    DatabaseReference groupsRef , lecturersRef;
    private Toolbar toolbar;
    private EditText  etLectureTitle  , etLectureDate;
    private Spinner spLectureGroup;
    private Spinner spLecturerName;
    private Button btnAddLecture;
    private AlertDialog addLectureDialog;
    private LecturesAdapter adapter;
    private ArrayList<Lecture> lectures = new ArrayList<>();
    private Calendar selectedDateTime = Calendar.getInstance();
    private DatabaseReference lecturesRef;
    private com.google.firebase.database.ValueEventListener lecturesListener;
    private ArrayList<Group> groups = new ArrayList<>();
    private ArrayList<Lecturer> lecturers = new ArrayList<>();
    private boolean groupsLoaded = false;
    private boolean lecturersLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lectures);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lecturesRef = FirebaseDatabase.getInstance().getReference("lectures");
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        lecturersRef = FirebaseDatabase.getInstance().getReference("lecturers");

        rvLectures = findViewById(R.id.rvLectures);
        rvLectures.setLayoutManager(new LinearLayoutManager(this));

        fabAddLecture = findViewById(R.id.fabAddLecture);
        fabAddLecture.setOnClickListener(this);
        fabAddLecture.setEnabled(false);





        adapter = new LecturesAdapter(this, lectures,
                new LecturesAdapter.OnLectureClickListener() {
                    @Override
                    public void onLectureClick(Lecture lecture) {
                        openLectureDeatails(lecture);                    }
                },
                new LecturesAdapter.OnLectureLongClickListener() {
                    @Override
                    public void onLectureLongClick(Lecture lecture) {
                        showEditLectureDialog(lecture);
                    }
                },
                groupsRef);
        rvLectures.setAdapter(adapter);

        groupsRef.get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot snapshot) {

                        groups.clear();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            Group group = child.getValue(Group.class);
                            if (group != null) {
                                group.setId(child.getKey());
                                groups.add(group);
                            }
                        }

                        groupsLoaded = true;

                        Toast.makeText(LecturesActivity.this,
                                "groups loaded size = " + groups.size(),
                                Toast.LENGTH_LONG).show();

                        checkIfReady();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LecturesActivity.this,
                            "groups failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
        lecturersRef.get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot snapshot) {

                        lecturers.clear();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            Lecturer lecturer = child.getValue(Lecturer.class);
                            if (lecturer != null) {
                                lecturer.setId(child.getKey());
                                lecturers.add(lecturer);
                            }
                        }

                        lecturersLoaded = true;

                        Toast.makeText(LecturesActivity.this,
                                "lecturers loaded size = " + lecturers.size(),
                                Toast.LENGTH_LONG).show();

                        checkIfReady();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LecturesActivity.this,
                            "lecturers failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
        loadLectures();
    }
    private void checkIfReady() {
        Toast.makeText(this,
                "groupsLoaded=" + groupsLoaded + " lecturersLoaded=" + lecturersLoaded,
                Toast.LENGTH_LONG).show();

        if (groupsLoaded && lecturersLoaded) {
            fabAddLecture.setEnabled(true);
        }
    }



    private void openLectureDeatails(Lecture lecture) {
        Intent intent = new Intent(this, LectureDetailsActivity.class)
                .putExtra("lecturerId",lecture.getLecturerId())
                .putExtra("groupId",lecture.getGroupId())
                .putExtra("lectureTitle",lecture.getTitle())
                .putExtra("lectureDate", lecture.getDate())
                .putExtra("lectureId",lecture.getLectureId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_lecturers) {
            Intent intent = new Intent(this, LecturersActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.menu_lectures) {
            Intent intent = new Intent(this, LecturesActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.menu_groups) {
            Intent intent = new Intent(this, GroupsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabAddLecture) {
            if (!groupsLoaded || !lecturersLoaded) {
                Toast.makeText(this, "הקבוצות או המרצים עדיין בטעינה", Toast.LENGTH_SHORT).show();
                return;
            }

            showAddLectureDialog();
        }
            else if(v.getId() == R.id.btnAddLecture){
               handleAddLecture();
            }
    }



    private void showAddLectureDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_lecture, null);
        builder.setView(view);

        Toast.makeText(this,
                "groups: " + groups.size() + " lecturers: " + lecturers.size(),
                Toast.LENGTH_LONG).show();

        spLectureGroup = view.findViewById(R.id.spLectureGroup);
        spLecturerName = view.findViewById(R.id.spLecturerName);
        etLectureTitle = view.findViewById(R.id.etLectureTitle);
        etLectureDate = view.findViewById(R.id.etLectureDate);

        ArrayList<String> groupNames = new ArrayList<>();
        for (Group group : groups) {
            groupNames.add(group.getName());
        }

        ArrayList<String> lecturerNames = new ArrayList<>();
        for (Lecturer lecturer : lecturers) {
            lecturerNames.add(lecturer.getName());
        }

        ArrayAdapter<String> groupsAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                groupNames
        );
        groupsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLectureGroup.setAdapter(groupsAdapter);

        ArrayAdapter<String> lecturersAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                lecturerNames
        );
        lecturersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLecturerName.setAdapter(lecturersAdapter);

        etLectureDate.setOnClickListener(v -> showDateTimePicker());

        btnAddLecture = view.findViewById(R.id.btnAddLecture);
        btnAddLecture.setOnClickListener(this);

        addLectureDialog = builder.create();
        addLectureDialog.show();
    }
    public void handleAddLecture() {

        // במקום group ו-lecturerName, נשאר רק עם title
        String title = etLectureTitle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // בדיקה שהרשימות נטענו
        if (groups.isEmpty() || lecturers.isEmpty()) {
            Toast.makeText(this, "קבוצות או מרצים לא נטענו", Toast.LENGTH_SHORT).show();
            return;
        }

        // מקבלים את הבחירה מה-Spinner
        int groupPosition = spLectureGroup.getSelectedItemPosition();
        int lecturerPosition = spLecturerName.getSelectedItemPosition();

        Group selectedGroup = groups.get(groupPosition);
        Lecturer selectedLecturer = lecturers.get(lecturerPosition);

        long timestamp = selectedDateTime.getTimeInMillis();

        String lectureId = FirebaseDatabase.getInstance()
                .getReference("lectures")
                .push()
                .getKey();

        Lecture lecture = new Lecture();
        lecture.setLectureId(lectureId);
        lecture.setTitle(title);

        // 🔹 השינוי החשוב – עובדים עם IDs
        lecture.setGroupId(selectedGroup.getId());
        lecture.setLecturerId(selectedLecturer.getId());

        lecture.setDate(new Date(timestamp));

        FirebaseDatabase.getInstance()
                .getReference("lectures")
                .child(lectureId)
                .setValue(lecture)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "הרצאה נוספה בהצלחה", Toast.LENGTH_LONG).show();
                    addLectureDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void showEditLectureDialog(Lecture lecture) {
    }
    private void showDateTimePicker() {

        int year = selectedDateTime.get(Calendar.YEAR); // //Current date used to put as defult
        int month = selectedDateTime.get(Calendar.MONTH);
        int day = selectedDateTime.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    selectedDateTime.set(Calendar.YEAR, selectedYear);
                    selectedDateTime.set(Calendar.MONTH, selectedMonth);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, selectedDay);

                    showTimePicker();
                },
                year, month, day
        );

        datePickerDialog.show();
    }
    private void showTimePicker() {

        int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY); //Current time, used to put as defult
        int minute = selectedDateTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {

                    selectedDateTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                    selectedDateTime.set(Calendar.MINUTE, selectedMinute);

                    updateDateField();
                },
                hour, minute, true
        );

        timePickerDialog.show();
    }
    private void updateDateField() {

        SimpleDateFormat sdf =
                new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        etLectureDate.setText(sdf.format(selectedDateTime.getTime()));
    }
    private void loadLectures() {
        lecturesRef.get().addOnSuccessListener(snapshot -> {
            lectures.clear();

            for (DataSnapshot child : snapshot.getChildren()) {
                Lecture lecture = child.getValue(Lecture.class);
                if (lecture == null) continue;

                // חשוב: אם יש לך setLectureId — נשים את ה-key כדי שתוכל לערוך/למחוק
                lecture.setLectureId(child.getKey());

                lectures.add(lecture);
            }

            adapter.notifyDataSetChanged();
        });
    }
}