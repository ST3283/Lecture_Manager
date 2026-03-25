package com.example.lecturemanager;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GroupDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    String groupId;
    FloatingActionButton fabAddLecture;
    DatabaseReference lectureRef , groupRef;
    private  LecturesAdapter adapter;

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



        groupRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId);

        // TODO display group details
        // TODO find lectures for this group
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
        // TODO link the lectures list to the adapter and the adapter to the recyclerview
        // TODO FAB should add a lecture
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
            showAddLectureDialog(groupId);
        }
    }

    private void showAddLectureDialog(final String groupId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_lecture, null);
        builder.setView(view);

        final EditText etTitle = (EditText) view.findViewById(R.id.etLectureTitle);
        final EditText etLecturer = (EditText) view.findViewById(R.id.etLecturerName);
        final EditText etDate = (EditText) view.findViewById(R.id.etLectureDate);
        final Button btnAdd = (Button) view.findViewById(R.id.btnAddLecture);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // DatePicker ללחיצה על שדה התאריך
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(GroupDetailsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                String dateStr = dayOfMonth + "/" + (month + 1) + "/" + year;
                                etDate.setText(dateStr);
                            }
                        }, year, month, day);
                dpd.show();
            }
        });

        // לחיצה על כפתור הוספה
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString().trim();
                String lecturerName = etLecturer.getText().toString().trim();
                String date = etDate.getText().toString().trim(); // TODO change to Date class

                if(title.isEmpty() || lecturerName.isEmpty() || date.isEmpty()) {
                    Toast.makeText(GroupDetailsActivity.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                    return;
                }

//                Lecture lecture = new Lecture(title, date, groupId, "", lecturerName);


            }
        });
    }

//    private void showEditLectureDialog(final Lecture lecture) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_lecture, null);
//        builder.setView(view);
//
//        final EditText etTitle = (EditText) view.findViewById(R.id.etLectureTitle);
//        final EditText etLecturer = (EditText) view.findViewById(R.id.etLecturerName);
//        final EditText etDate = (EditText) view.findViewById(R.id.etLectureDate);
//        final Button btnAdd = (Button) view.findViewById(R.id.btnAddLecture);
//
//        // מלא את השדות בערכים הקיימים
//        etTitle.setText(lecture.getTitle());
//        etLecturer.setText(lecture.getLecturerId()); //TODO get lecturer name from id
////        etDate.setText(lecture.getDate()); // TODO how do we display a date? (with access to the date picker)
//        btnAdd.setText("עדכן הרצאה");  // משנה את הכיתוב של הכפתור
//
//        final AlertDialog dialog = builder.create();
//        dialog.show();
//
//        // DatePicker
//        etDate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final Calendar c = Calendar.getInstance();
//                int year = c.get(Calendar.YEAR);
//                int month = c.get(Calendar.MONTH);
//                int day = c.get(Calendar.DAY_OF_MONTH);
//
//                DatePickerDialog dpd = new DatePickerDialog(GroupDetailsActivity.this,
//                        new DatePickerDialog.OnDateSetListener() {
//                            @Override
//                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                                c.set(year, month, dayOfMonth);
//                                Date d = c.getTime();
//
//                            }
//                        }, year, month, day);
//                dpd.show();
//            }
//        });
//
//        // עדכון הרצאה
//        btnAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String title = etTitle.getText().toString().trim();
//                String lecturerName = etLecturer.getText().toString().trim();
//                String date = etDate.getText().toString().trim();
//
//                if(title.isEmpty() || lecturerName.isEmpty() || date.isEmpty()) {
//                    Toast.makeText(GroupDetailsActivity.this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                // עדכון הערכים באובייקט Lecture
//                lecture.setTitle(title);
////                lecture.setDate(date); // TODO change to Date class
//
//                // עדכון ב-Firestore
//                FirebaseFirestore.getInstance()
//                        .collection("groups")
//                        .document(groupId)
//                        .collection("lectures")
//                        .document(lecture.getId())
//                        .set(lecture)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void unused) {
//                                Toast.makeText(GroupDetailsActivity.this, "הרצאה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
//                                dialog.dismiss();
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(GroupDetailsActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                            }
//                        });
//            }
//        });
//    }

//    private void showDeleteLectureDialog(final Lecture lecture) {
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("מחיקת הרצאה");
//        builder.setMessage("האם אתה בטוח שברצונך למחוק את ההרצאה?");
//
//        builder.setPositiveButton("מחק", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                FirebaseFirestore.getInstance()
//                        .collection("groups")
//                        .document(groupId)
//                        .collection("lectures")
//                        .document(lecture.getId())
//                        .delete()
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void unused) {
//                                Toast.makeText(GroupDetailsActivity.this,
//                                        "הרצאה נמחקה",
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(GroupDetailsActivity.this,
//                                        "שגיאה: " + e.getMessage(),
//                                        Toast.LENGTH_LONG).show();
//                            }
//                        });
//            }
//        });
//
//        builder.setNegativeButton("ביטול", null);
//        builder.show();
//    }



}
