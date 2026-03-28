package com.example.lecturemanager;

import android.os.Bundle;

import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;

import java.util.Date;

public class LectureDetailsActivity extends AppCompatActivity  {

    private String lectureId;
    private Date date;
    private DatabaseReference groupRef, lecturerRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lecture_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView tvLecturerName = findViewById(R.id.tvLecturerName);
        TextView tvLectureGroup = findViewById(R.id.tvLectureGroup);
        TextView tvLectureTitle = findViewById(R.id.tvLectureTitle);
        TextView tvLectureDate = findViewById(R.id.tvLectureDate);


        String lecturerId = getIntent().getStringExtra("lecturerId");
        String groupId = getIntent().getStringExtra("groupId");
        String title = getIntent().getStringExtra("lectureTitle");
        long d = getIntent().getLongExtra("lectureDate" , -1);
        if (d != -1) {
            date = new Date(d);
        }
        lectureId = getIntent().getStringExtra("lectureId");

        groupRef = DBRefs.groupsRef;
        lecturerRef = DBRefs.lecturersRef;

        groupRef.child(groupId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    Group group = snapshot.getValue(Group.class);

                    if (group != null) {
                        String groupName = group.getName();
                        tvLectureGroup.setText(groupName);
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "שגיאה: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });

        lecturerRef.child(lecturerId)
                .get()
                .addOnSuccessListener(snapshot -> {

                            Lecturer lecturer = snapshot.getValue(Lecturer.class);
                            if (lecturer != null) {
                                String lecturerName = lecturer.getName();
                                tvLecturerName.setText(lecturerName);
                            }

                        })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "שגיאה: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });

        tvLectureTitle.setText(title);
        tvLectureDate.setText(date.toString());
    }


}