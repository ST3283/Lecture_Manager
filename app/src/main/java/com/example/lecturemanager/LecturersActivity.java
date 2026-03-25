package com.example.lecturemanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LecturersActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String DB_URL = "https://lecture-manager-356ad-default-rtdb.europe-west1.firebasedatabase.app/";
    private Button btnAddLecturer;
    private FloatingActionButton fabAddLecturer;
    private RecyclerView rvLecturers;
    private LecturersAdapter adapter;
    private ArrayList<Lecturer> lecturerList;
    DatabaseReference lecturersRef;
    private EditText etLecturerName, etLecturerEmail , etLecturerPhone;
    private AlertDialog addLecturerDialog;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturers);

        toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabAddLecturer = findViewById(R.id.fabAddLecturer);
        fabAddLecturer.setOnClickListener(this);

         lecturersRef = FirebaseDatabase.getInstance(DB_URL).getReference("lecturers");



        rvLecturers = findViewById(R.id.rvLecturers);
        rvLecturers.setLayoutManager(new LinearLayoutManager(this));


        lecturerList = new ArrayList<>();

        adapter = new LecturersAdapter(this, lecturerList, lecturersRef, new LecturersAdapter.OnLecturerClickListener() {
            @Override
            public void onLecturerClick(Lecturer lecturer) {
                openLecturerDetails(lecturer);
            }
        }
        );

        rvLecturers.setAdapter(adapter);

        ListenToLecturerRealTime();

      //  loadLecturers();
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
        if (v.getId() == R.id.fabAddLecturer){
            showAddLecturerDialog();
        }
        else if (v.getId() == R.id.btnAddLecturer){
            handleAddLecturer();
        }
    }

    private void loadLecturers() {
        DatabaseReference lecturersRef =
                FirebaseDatabase.getInstance().getReference("lecturers");

        lecturersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lecturerList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Lecturer lecturer = child.getValue(Lecturer.class);
                    if (lecturer != null) {
                        lecturer.setId(child.getKey()); // חשוב!
                        lecturerList.add(lecturer);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LecturersActivity.this,
                        "שגיאה בטעינת המרצים",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void handleAddLecturer() {
        String name = etLecturerName.getText().toString().trim();
        String email = etLecturerEmail.getText().toString().trim();
        String phone = etLecturerPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = lecturersRef.push().getKey();
        Lecturer lecturer = new Lecturer(id, name, email , phone);

        lecturersRef.child(id).setValue(lecturer)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "מרצה נוסף בהצלחה", Toast.LENGTH_LONG).show();
                    // אין צורך לקרוא loadGroups() – ה-SnapshotListener יעדכן אוטומטית
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        // close the dialog
        addLecturerDialog.dismiss();
    }

    private  void ListenToLecturerRealTime() {

        lecturersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lecturerList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Lecturer lecturer = child.getValue(Lecturer.class);
                    if (lecturer != null) {
                        lecturer.setId(child.getKey()); // חובה
                        lecturerList.add(lecturer);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LecturersActivity.this,
                        "Failed to load lecturers",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void showAddLecturerDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_lecturer, null);
        builder.setView(view);

        etLecturerName = view.findViewById(R.id.etLecturerName);
        etLecturerEmail = view.findViewById(R.id.etLecturerEmail);
        etLecturerPhone = view.findViewById(R.id.etLecturerPhone);

        btnAddLecturer = view.findViewById(R.id.btnAddLecturer);
        btnAddLecturer.setOnClickListener(this);

        addLecturerDialog = builder.create();
        addLecturerDialog.show();
    }
    private void openLecturerDetails(Lecturer lecturer) {
        Intent intent = new Intent(this, LecturerDetailsActivity.class);
        intent.putExtra("lecturerId", lecturer.getId());
        intent.putExtra("lecturerName", lecturer.getName());
        intent.putExtra("lecturerEmail",  lecturer.getEmail());
        intent.putExtra("lecturerPhone",  lecturer.getPhone());
        startActivity(intent);



    }
}
