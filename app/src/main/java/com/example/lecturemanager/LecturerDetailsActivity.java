package com.example.lecturemanager;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class LecturerDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvName, tvEmail, tvPhone;
    private String lecturerId ;
    private RecyclerView rvLecturerLectures;
    private ArrayList<Lecture> lectures;
    private LecturesAdapter adapter;
    private Lecturer lecturer;
    private Calendar calendar;
    private DatabaseReference lecturerRef , groupRef , lectureRef ;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_details);

        requestNotificationPermission();

        lecturerId = getIntent().getStringExtra("lecturerId");

        lecturerRef = FirebaseDatabase.getInstance().getReference("lecturers").child(lecturerId);
        lectureRef = FirebaseDatabase.getInstance().getReference("lectures");
        groupRef = FirebaseDatabase.getInstance().getReference("groups");


        tvName = findViewById(R.id.tvLecturerName);
        tvEmail = findViewById(R.id.tvLecturerEmail);
        tvPhone = findViewById(R.id.tvLecturerPhone);
        rvLecturerLectures = findViewById(R.id.rvLecturerLectures);

        // מקבל את המרצה מה Intent
        String lecturerId =  getIntent().getStringExtra("lecturerId");
        String lecturerName =  getIntent().getStringExtra("lecturerName");
        String lecturerEmail =  getIntent().getStringExtra("lecturerEmail");
        String lecturerPhone = getIntent().getStringExtra("lecturerPhone");

        tvName.setText(lecturerName);
        tvEmail.setText(lecturerEmail);
        tvPhone.setText(lecturerPhone);


        // RecyclerView
        lectures = new ArrayList<>();
        adapter = new LecturesAdapter(this,
                lectures,
                // לחיצה רגילה כניסה למידע על הרצאה
                new LecturesAdapter.OnLectureClickListener() {
                    @Override
                    public void onLectureClick(Lecture lecture) {
                        handleLectureClick(lecture);
                    }
                },
                // לחיצה ארוכה עריכה
                new LecturesAdapter.OnLectureLongClickListener() {
                    @Override
                    public void onLectureLongClick(Lecture lecture) {
                        handleLectureLongClick(lecture);
                    }
                },
                groupRef);
        rvLecturerLectures.setLayoutManager(new LinearLayoutManager(this));
        rvLecturerLectures.setAdapter(adapter);



        // ניתן להוסיף FAB או כפתור במסך להוספת הרצאה חדשה

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(this);
    }

    // ----------- טוען הרצאות של המרצה בלבד ----------------
    private void loadLectures() {

        adapter.notifyDataSetChanged();
    }

    // ----------- הוספה / עריכה של הרצאה ----------------
//    private void showAddEditLectureDialog(final Lecture lectureToEdit) {
//
//        // 1️⃣ יצירת דיאלוג AlertDialog עם layout של הוספה/עריכה
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_lecture, null);
//        builder.setView(view);
//
//        // 2️⃣ קישור רכיבי הדיאלוג
//        final EditText etName = view.findViewById(R.id.etLecturerName);
//        final EditText etDate = view.findViewById(R.id.etLectureDate);
//        final Button btnAdd = view.findViewById(R.id.btnAddLecture);
//
//        // 3️⃣ אתחול Calendar
//        calendar = Calendar.getInstance();
//
//        // 4️⃣ אם זו עריכה – נעדכן את השדות עם נתוני ההרצאה הקיימת
//        if (lectureToEdit != null) {
//            etName.setText(lectureToEdit.getLecturerName());
////            etDate.setText(lectureToEdit.getDate());
//            btnAdd.setText("עדכן"); // כפתור משנה טקסט
//        }
//
//        // 5️⃣ יצירת ה־AlertDialog בפועל
//        final AlertDialog dialog = builder.create();
//        dialog.show();
//
//        // 6️⃣ DatePicker – פתיחת לוח שנה בעת לחיצה על שדה התאריך
//        etDate.setOnClickListener(v -> {
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//
//            DatePickerDialog datePickerDialog = new DatePickerDialog(
//                    LecturerDetailsActivity.this,
//                    new DatePickerDialog.OnDateSetListener() {
//                        @Override
//                        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
//                            // עדכון ה־Calendar עם התאריך שנבחר
//                            calendar.set(selectedYear, selectedMonth, selectedDay);
//
//                            // יצירת מחרוזת תאריך להצגה
//                            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
//                            etDate.setText(date);
//                        }
//                    },
//                    year,
//                    month,
//                    day
//            );
//
//            datePickerDialog.show();
//        });
//
//        // 7️⃣ כפתור הוספה / עדכון
//        btnAdd.setOnClickListener(v -> {
//            String name = etName.getText().toString().trim();
//            String date = etDate.getText().toString().trim();
//
//            // בדיקה אם השדות מלאים
//            if (name.isEmpty() || date.isEmpty()) {
//                Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // 🔹 אם זו הוספה חדשה
//            if (lectureToEdit == null) {
//                Lecture lecture = new Lecture();
//                lecture.setLecturerName(name);
////                lecture.setDate(date);
//                lecture.setLecturerId(lecturer.getId());
//                lecture.setGroupId(groupId);
//
//                DocumentReference docRef = db.collection("lectures").document();
//                lecture.setId(docRef.getId());
//
//                docRef.set(lecture)
//                        .addOnSuccessListener(aVoid -> {
//                            Toast.makeText(this, "ההרצאה נוספה", Toast.LENGTH_SHORT).show();
//                            dialog.dismiss();
//
//                            // 🔔 תזכורת להרצאה
//                            long reminderTime = calendar.getTimeInMillis();
//                            scheduleLectureReminder(lecture.getLecturerName(), reminderTime);
//                        });
//
//                // 🔹 אם זו עריכה של הרצאה קיימת
//            } else {
//                lectureToEdit.setLecturerName(name);
////                lectureToEdit.setDate(date);
//
//                db.collection("lectures")
//                        .document(lectureToEdit.getId())
//                        .set(lectureToEdit)
//                        .addOnSuccessListener(aVoid -> {
//                            Toast.makeText(this, "ההרצאה עודכנה", Toast.LENGTH_SHORT).show();
//                            dialog.dismiss();
//
//                            // 🔔 עדכון תזכורת להרצאה
//                            long reminderTime = calendar.getTimeInMillis();
//                            scheduleLectureReminder(lectureToEdit.getLecturerName(), reminderTime);
//                        });
//            }
//        });
//    }
//
//
//    // ----------- מחיקת הרצאה ----------------
//    private void showDeleteLectureDialog(final Lecture lecture) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("מחיקת הרצאה");
//        builder.setMessage("האם אתה בטוח שברצונך למחוק את ההרצאה?");
//
//        builder.setPositiveButton("מחק", (dialog, which) -> {
//            db.collection("lectures")
//                    .document(lecture.getId())
//                    .delete()
//                    .addOnSuccessListener(aVoid ->
//                            Toast.makeText(LecturerDetailsActivity.this, "ההרצאה נמחקה", Toast.LENGTH_SHORT).show()
//                    );
//        });
//
//        builder.setNegativeButton("ביטול", null);
//        builder.show();
//    }
//
//    private void scheduleLectureReminder(String lectureName, long timeInMillis) {
//        Intent intent = new Intent(this, LectureReminderReceiver.class);
//        intent.putExtra("lectureName", lectureName);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                this,
//                (int) System.currentTimeMillis(),
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        AlarmManager alarmManager =
//                (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//
//        alarmManager.setExact(
//                AlarmManager.RTC_WAKEUP,
//                timeInMillis,
//                pendingIntent
//        );
//    }
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
        if (v.getId() == R.id.fabAddLecture){
            showAddLectureDialog(lecturerId);
        }
    }

    private void handleAddLecture() {
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
        startActivity(intent);
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

                DatePickerDialog dpd = new DatePickerDialog(LecturerDetailsActivity.this,
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
    }
}

