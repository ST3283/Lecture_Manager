package com.example.lecturemanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.List;
import java.util.Locale;

public class GroupDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    String groupId;
    FloatingActionButton fabAddLecture;
    DatabaseReference lectureRef , groupRef , lecturerRef;
    private  LecturesAdapter adapter;
    private EditText  etLectureTitle  , etLectureDate;
    private Spinner spLecturerName;
    private Calendar selectedDateTime = Calendar.getInstance();
    private android.app.AlertDialog addLectureDialog;
    private  Button btnAddLecture;
    private ArrayList<Lecturer> lecturers = new ArrayList<>();


    List<Lecture> groupLectures = new ArrayList<Lecture>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        TextView tvName = findViewById(R.id.tvGroupName);
        TextView tvDescription = findViewById(R.id.tvGroupDescription);

        groupId = getIntent().getStringExtra("groupId");
        String name = getIntent().getStringExtra("groupName");
        String description = getIntent().getStringExtra("groupDescription");

        tvName.setText(name);
        tvDescription.setText(description);

      fabAddLecture = findViewById(R.id.fabAddLecture);
      fabAddLecture.setOnClickListener(this);

        ArrayList<Lecture> lectures = new ArrayList<>();

        lecturerRef = FirebaseDatabase.getInstance().getReference("lecturers");

        lecturerRef.get()
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


                        Toast.makeText(GroupDetailsActivity.this,
                                "lecturers loaded size = " + lecturers.size(),
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(GroupDetailsActivity.this,
                            "lecturers failed: "  ,
                            Toast.LENGTH_LONG).show();
                });


        groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId);
        // TODO display group details
        // TODO find lectures for this group
        //TODO display rcLectures
        lectureRef = FirebaseDatabase.getInstance().getReference("lectures");
        lectureRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot lectureSnapshot) {
                for (DataSnapshot lecSnap: lectureSnapshot.getChildren()) {
                    Lecture lec = lecSnap.getValue(Lecture.class);
                    if (lec.getGroupId().equals(groupId)) {
                        lectures.add(lec);
                    }
                }
                // adapter.notifyDatasetChange
            }
        });
        adapter = new LecturesAdapter(this,
                lectures,
                // לחיצה רגילה
                new LecturesAdapter.OnLectureClickListener() {
                    @Override
                    public void onLectureClick(Lecture lecture) {
                        handleLectureClick(lecture);
                    }
                },
                // לחיצה ארוכה
                new LecturesAdapter.OnLectureLongClickListener() {
                    @Override
                    public void onLectureLongClick(Lecture lecture) {
                        handleLectureLongClick(lecture);
                    }
                },
        groupRef,
                lectureRef);

        RecyclerView rvLectures = findViewById(R.id.rvLectures);
        rvLectures.setLayoutManager(new LinearLayoutManager(this));
        rvLectures.setAdapter(adapter);

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
                        Toast.makeText(GroupDetailsActivity.this, "הרצאה נמחקה", Toast.LENGTH_SHORT).show();
                        // ה-SnapshotListener ב-Activity יעדכן את הרשימה אוטומטית
                    }
                })
                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupDetailsActivity.this, "שגיאה במחיקה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleLectureClick(Lecture lecture) {
        Intent intent = new Intent(this, LectureDetailsActivity.class);
        intent.putExtra("lectureId", lecture.getLectureId());
        startActivity(intent);
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabAddLecture){
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
                 " lecturers: " + lecturers.size(),
                Toast.LENGTH_LONG).show();

        //TODO put the group name of the group you are in automatically
        spLecturerName = view.findViewById(R.id.spLecturerName);
        etLectureTitle = view.findViewById(R.id.etLectureTitle);
        etLectureDate = view.findViewById(R.id.etLectureDate);



        ArrayList<String> lecturerNames = new ArrayList<>();
        for (Lecturer lecturer : lecturers) {
            lecturerNames.add(lecturer.getName());
        }

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
        if ( lecturers.isEmpty()) {
            Toast.makeText(this, "קבוצות או מרצים לא נטענו", Toast.LENGTH_SHORT).show();
            return;
        }

        // מקבלים את הבחירה מה-Spinner

        int lecturerPosition = spLecturerName.getSelectedItemPosition();


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
        lecture.setGroupId(groupId);
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
}
