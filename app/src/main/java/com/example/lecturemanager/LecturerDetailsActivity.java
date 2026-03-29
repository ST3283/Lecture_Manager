package com.example.lecturemanager;


import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LecturerDetailsActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private final static String DB_URL = "https://lecture-manager-356ad-default-rtdb.europe-west1.firebasedatabase.app/";
    private TextView tvName, tvEmail, tvPhone;
    private String lecturerId;
    private RecyclerView rvLecturerLectures;
    private EditText etLectureTitle, etLectureDate;
    private Spinner spLecturerName, spLectureGroup;
    private Button btnAddLecture;
    private AlertDialog addLectureDialog;
    private LecturesAdapter adapter;
    private Calendar selectedDateTime = Calendar.getInstance();

    private DatabaseReference lecturerRef, groupsRef, lectureRef, lecturersRef;
    private FloatingActionButton fabAddLecture;
    private ArrayList<Group> groups = new ArrayList<>();
    private ArrayList<Lecturer> lecturers = new ArrayList<>();
    private ArrayList<Lecture> lectures = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_details);

        requestNotificationPermission();

        lecturerId = getIntent().getStringExtra("lecturerId");

        lecturerRef = FirebaseDatabase.getInstance(DB_URL).getReference("lecturers").child(lecturerId);
        lectureRef = FirebaseDatabase.getInstance(DB_URL).getReference("lectures");
        groupsRef = FirebaseDatabase.getInstance(DB_URL).getReference("groups");
        lecturersRef = FirebaseDatabase.getInstance(DB_URL).getReference("lecturers");


        tvName = findViewById(R.id.tvLecturerName);
        tvEmail = findViewById(R.id.tvLecturerEmail);
        tvPhone = findViewById(R.id.tvLecturerPhone);
        rvLecturerLectures = findViewById(R.id.rvLecturerLectures);

        // מקבל את המרצה מה Intent
        String lecturerId = getIntent().getStringExtra("lecturerId");
        String lecturerName = getIntent().getStringExtra("lecturerName");
        String lecturerEmail = getIntent().getStringExtra("lecturerEmail");
        String lecturerPhone = getIntent().getStringExtra("lecturerPhone");

        tvName.setText(lecturerName);
        tvEmail.setText(lecturerEmail);
        tvPhone.setText(lecturerPhone);

        tvName.setOnLongClickListener(this);
        tvEmail.setOnLongClickListener(this);
        tvPhone.setOnLongClickListener(this);


        lecturerRef.get()
                .addOnSuccessListener(snapshot -> {
                    lecturers.clear();

                    Lecturer lecturer = snapshot.getValue(Lecturer.class);
                    if (lecturer != null) {
                        lecturer.setId(snapshot.getKey());
                        lecturers.add(lecturer);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LecturerDetailsActivity.this,
                            "lecturer failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });


        groupsRef.get()
                .addOnSuccessListener(snapshot -> {
                    groups.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Group group = child.getValue(Group.class);
                        if (group != null) {
                            group.setId(child.getKey());
                            groups.add(group);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LecturerDetailsActivity.this,
                            "group failed: ",
                            Toast.LENGTH_LONG).show();
                });


        adapter = new LecturesAdapter(this,
                lectures,
                // לחיצה רגילה כניסה למידע על הרצאה
                new LecturesAdapter.OnLectureClickListener() {
                    @Override
                    public void onLectureClick(Lecture lecture) {
                        handleLectureClick(lecture);
                    }
                },
                // לחיצה ארוכה מחיקה
                new LecturesAdapter.OnLectureLongClickListener() {
                    @Override
                    public void onLectureLongClick(Lecture lecture) {
                        handleLectureLongClick(lecture);
                    }
                },
                groupsRef,
                lecturersRef);
        rvLecturerLectures.setLayoutManager(new LinearLayoutManager(this));
        rvLecturerLectures.setAdapter(adapter);

        loadLecturerLectures();


//add a lecture buttun
        fabAddLecture = findViewById(R.id.fabAddLecture);
        fabAddLecture.setOnClickListener(this);
    }

    private void loadLecturerLectures() {
        lectureRef.orderByChild("lecturerId").equalTo(lecturerId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot snapshot) {
                        lectures.clear();

                        for (DataSnapshot lecSnap : snapshot.getChildren()) {
                            Lecture lec = lecSnap.getValue(Lecture.class);

                            if (lec != null) {
                                lec.setLectureId(lecSnap.getKey());
                                lectures.add(lec);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LecturerDetailsActivity.this,
                            "שגיאה:" + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabAddLecture) {
            showAddLectureDialog();
        } else if (v.getId() == R.id.btnAddLecture) {
            handleAddLecture();
        }
    }

    private void showAddLectureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_lecture, null);
        builder.setView(view);


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

        for (int i = 0; i < lecturers.size(); i++) { //in order to choose automationically the lecturer
            if (lecturers.get(i).getId().equals(lecturerId)) {
                spLecturerName.setSelection(i);
                break;
            }
        }

        spLecturerName.setEnabled(false);//shuts down the spinner so you can't change it
        spLecturerName.setClickable(false);

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
        if (groups.isEmpty()) {
            Toast.makeText(this, "קבוצות לא נטענו", Toast.LENGTH_SHORT).show();
            return;
        }

        // מקבלים את הבחירה מה-Spinner
        int groupPosition = spLectureGroup.getSelectedItemPosition();

        Group selectedGroup = groups.get(groupPosition);

        String lectureId = FirebaseDatabase.getInstance(DB_URL)
                .getReference("lectures")
                .push()
                .getKey();

        Lecture lecture = new Lecture();
        lecture.setLectureId(lectureId);
        lecture.setTitle(title);

        // 🔹 השינוי החשוב – עובדים עם IDs
        lecture.setGroupId(selectedGroup.getId());
        lecture.setLecturerId(lecturerId);

        long timestamp = selectedDateTime.getTimeInMillis();
        lecture.setDate(new Date(timestamp));

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("lectures")
                .child(lectureId)
                .setValue(lecture)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "הרצאה נוספה בהצלחה", Toast.LENGTH_LONG).show();
                    addLectureDialog.dismiss();

                    loadLecturerLectures();//update the rvLecture

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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


    private void handleLectureLongClick(Lecture lecture) {
        new AlertDialog.Builder(this)
                .setTitle("מחיקה")
                .setMessage("למחוק את ההרצאה?")
                .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteLecture(lecture);
                    }
                })
                .setNegativeButton("לא", null)
                .show();

    }

    private void deleteLecture(Lecture lecture) {
        lectureRef.child(lecture.getLectureId()).removeValue()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(LecturerDetailsActivity.this, "הרצאה נמחקה", Toast.LENGTH_SHORT).show();
                        // ה-SnapshotListener ב-Activity יעדכן את הרשימה אוטומטית
                    }
                })
                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LecturerDetailsActivity.this, "שגיאה במחיקה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleLectureClick(Lecture lecture) {
        Intent intent = new Intent(this, LectureDetailsActivity.class);
        intent.putExtra("lectureId", lecture.getLectureId());
        intent.putExtra("lecturerId", lecture.getLecturerId());
        intent.putExtra("groupId", lecture.getGroupId());
        intent.putExtra("lectureTitle", lecture.getTitle());
        intent.putExtra("lectureDate", lecture.getDate().getTime());
        startActivity(intent);
    }

    //when long press edit or delete lecturer
    @Override
    public boolean onLongClick(View v) {

        if (v.getId() == R.id.tvLecturerName
                || v.getId() == R.id.tvLecturerEmail
                || v.getId() == R.id.tvLecturerPhone) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            View view = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_add_lecturer, null); // חשוב: layout של מרצה

            builder.setView(view);

            final EditText etName = view.findViewById(R.id.etLecturerName);
            final EditText etEmail = view.findViewById(R.id.etLecturerEmail);
            final EditText etPhone = view.findViewById(R.id.etLecturerPhone);

            Button btnSave = view.findViewById(R.id.btnAddLecturer);
            Button btnDelete = view.findViewById(R.id.btnDelete);


            // הכנסת ערכים קיימים
            etName.setText(tvName.getText().toString());
            etEmail.setText(tvEmail.getText().toString());
            etPhone.setText(tvPhone.getText().toString());

            btnSave.setText("שמור");
            btnDelete.setText("מחק");

            final AlertDialog dialog = builder.create();
            dialog.show();

            btnSave.setOnClickListener(v1 -> {

                String newName = etName.getText().toString().trim();
                String newEmail = etEmail.getText().toString().trim();
                String newPhone = etPhone.getText().toString().trim();

                if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
                    Toast.makeText(LecturerDetailsActivity.this,
                            "נא למלא את כל השדות",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("name", newName);
                updates.put("email", newEmail);
                updates.put("phone", newPhone);

                DBRefs.lecturersRef.child(lecturerId)
                        .updateChildren(updates)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(LecturerDetailsActivity.this,
                                    "מרצה עודכן",
                                    Toast.LENGTH_SHORT).show();

                            // עדכון ה־UI
                            tvName.setText(newName);
                            tvEmail.setText(newEmail);
                            tvPhone.setText(newPhone);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(LecturerDetailsActivity.this,
                                    "שגיאה בעדכון: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });

                dialog.dismiss();
            });
            btnDelete.setOnClickListener(v1 -> {
                lecturersRef.child(lecturerId).removeValue()
                        .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LecturerDetailsActivity.this, "מרצה נמחק", Toast.LENGTH_SHORT).show();
                                // ה-SnapshotListener ב-Activity יעדכן את הרשימה אוטומטית
                            }
                        })
                        .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LecturerDetailsActivity.this, "שגיאה במחיקה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                dialog.dismiss();
            });
        }
        return true;
    }
}