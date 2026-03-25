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
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class GroupsActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String DB_URL = "https://lecture-manager-356ad-default-rtdb.europe-west1.firebasedatabase.app/";

    private RecyclerView rvGroups;
    private GroupsAdapter adapter;
    DatabaseReference groupsRef;
    private FloatingActionButton fabAdd;
    private EditText etGroupName, etGroupDescription;
    private Button btnAddGroup;
    private AlertDialog addGroupDialog;
    private ArrayList<Group> groups;
    private Toolbar toolbar;

    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        rvGroups = findViewById(R.id.rvGroups);
        rvGroups.setLayoutManager(new LinearLayoutManager(this));

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(this);

        groups = new ArrayList<>();

        groupsRef = FirebaseDatabase.getInstance(DB_URL).getReference("groups");


        adapter = new GroupsAdapter(this, groups, groupsRef, new GroupsAdapter.OnGroupClickListener() {
            @Override
            public void onGroupClick(Group group) {
                openGroupDetails(group);
            }
        }
        );
        rvGroups.setAdapter(adapter);

        // 🔹 מאזין בזמן אמת בלי Lambda
        listenToGroupsRealtime();
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


    // ===================== OnClick אחד בלבד =====================
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.fabAdd) {
            showAddGroupDialog();
        } else if (id == R.id.btnAddGroup) {
            handleAddGroup();
        }
    }

    // ===================== Dialog =====================
    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_group, null);
        builder.setView(view);

        etGroupName = view.findViewById(R.id.etGroupName);
        etGroupDescription = view.findViewById(R.id.etGroupDescription);

        btnAddGroup = view.findViewById(R.id.btnAddGroup);
        btnAddGroup.setOnClickListener(this);

        addGroupDialog = builder.create();
        addGroupDialog.show();
    }

    // ===================== הוספת קבוצה =====================
    private void handleAddGroup() {
        String name = etGroupName.getText().toString().trim();
        String description = etGroupDescription.getText().toString().trim();

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = groupsRef.push().getKey();
        Group group = new Group(id, name, description);

        groupsRef.child(id).setValue(group)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "קבוצה נוספה בהצלחה", Toast.LENGTH_LONG).show();
                    // אין צורך לקרוא loadGroups() – ה-SnapshotListener יעדכן אוטומטית
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        // close the dialog
        addGroupDialog.dismiss();
    }

    // ===================== מאזין בזמן אמת =====================
    private void listenToGroupsRealtime() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groups.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Group group = child.getValue(Group.class);
                    group.setId(child.getKey()); // חשוב!
                    groups.add(group);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        // FOR LECTURES ACTIVITY
//        ArrayList<Group> ggroups = new ArrayList<>();
//        groupsRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//            @Override
//            public void onSuccess(DataSnapshot snapshot) {
//                for (DataSnapshot child : snapshot.getChildren()) {
//                    Group group = child.getValue(Group.class);
//                    group.setId(child.getKey()); // חשוב!
//                    ggroups.add(group);
//                }
//            }
//        });

    }

    private void openGroupDetails(Group group) {
        Intent intent = new Intent(this, GroupDetailsActivity.class);
        intent.putExtra("groupId", group.getId());
        intent.putExtra("groupName", group.getName());
        intent.putExtra("groupDescription", group.getDescription());
        startActivity(intent);
        }
    }

