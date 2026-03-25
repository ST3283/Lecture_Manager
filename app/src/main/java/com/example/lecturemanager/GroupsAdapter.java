package com.example.lecturemanager;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    private OnGroupClickListener listener;
    private Context context;
    private ArrayList<Group> groups;
    private DatabaseReference groupsRef;
    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }
    public GroupsAdapter(Context context, ArrayList<Group> groups, DatabaseReference groupsRef , OnGroupClickListener listener) {
        this.context = context;
        this.groups = groups;
        this.groupsRef = groupsRef;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {


        final Group group = groups.get(position);

        holder.tvGroupName.setText(group.getName());
        holder.tvGroupDescription.setText(group.getDescription());

        // ================== מחיקה ==================
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                groupsRef.child(group.getId()).removeValue()
                        .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "קבוצה נמחקה", Toast.LENGTH_SHORT).show();
                                // ה-SnapshotListener ב-Activity יעדכן את הרשימה אוטומטית
                            }
                        })
                        .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "שגיאה במחיקה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // ================== עריכה ==================
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                View view = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_add_group, null);
                builder.setView(view);

                final EditText etName = view.findViewById(R.id.etGroupName);
                final EditText etDescription = view.findViewById(R.id.etGroupDescription);
                Button btnSave = view.findViewById(R.id.btnAddGroup);

                etName.setText(group.getName());
                etDescription.setText(group.getDescription());
                btnSave.setText("שמור");

                final AlertDialog dialog = builder.create();
                dialog.show();

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newName = etName.getText().toString().trim();
                        String newDesc = etDescription.getText().toString().trim();

                        if (newName.isEmpty() || newDesc.isEmpty()) {
                            Toast.makeText(context, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("name", newName);
                        updates.put("description", newDesc);

                        groupsRef.child(group.getId()).updateChildren(updates)
                                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "קבוצה עודכנה", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new com.google.android.gms.tasks.OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "שגיאה בעדכון: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                        dialog.dismiss();
                    }
                });
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    // ================== ViewHolder ==================
    public static class GroupViewHolder extends RecyclerView.ViewHolder {

        TextView tvGroupName, tvGroupDescription;
        Button btnEdit, btnDelete;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvGroupDescription = itemView.findViewById(R.id.tvGroupDescription);
            btnEdit = itemView.findViewById(R.id.btnEditGroup);
            btnDelete = itemView.findViewById(R.id.btnDeleteGroup);
        }
    }
}
