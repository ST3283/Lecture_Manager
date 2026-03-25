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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LecturersAdapter extends RecyclerView.Adapter<LecturersAdapter.LecturerViewHolder> {

    // רשימת המרצים שמוצגת

    private LecturersAdapter.OnLecturerClickListener listener;
    private Context context;
    private ArrayList<Lecturer> lecturers;
    private DatabaseReference lecturersRef;
    public interface OnLecturerClickListener {
        void onLecturerClick(Lecturer lecturer);
    }
    public LecturersAdapter(Context context, ArrayList<Lecturer> lecturers, DatabaseReference lecturersRef , LecturersAdapter.OnLecturerClickListener listener) {
        this.context = context;
        this.lecturers = lecturers;
        this.lecturersRef = lecturersRef;
        this.listener = listener;
    }


    @NonNull
    @Override
    public LecturersAdapter.LecturerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_lecturer, parent, false);
        return new LecturersAdapter.LecturerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LecturersAdapter.LecturerViewHolder holder, int position) {


        final Lecturer lecturer = lecturers.get(position);

        holder.tvLecturerName.setText(lecturer.getName());
        holder.tvLecturerEmail.setText(lecturer.getEmail());
        holder.tvLecturerPhone.setText(lecturer.getPhone());

        // ================== מחיקה ==================
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lecturersRef.child(lecturer.getId()).removeValue()
                        .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "מרצה נמחק", Toast.LENGTH_SHORT).show();
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
                        .inflate(R.layout.dialog_add_lecturer, null);
                builder.setView(view);

                final EditText etName = view.findViewById(R.id.etLecturerName);
                final EditText etEmail = view.findViewById(R.id.etLecturerEmail);
                final EditText etPhone = view.findViewById(R.id.etLecturerPhone);
                Button btnSave = view.findViewById(R.id.btnAddLecturer);

                etName.setText(lecturer.getName());
                etEmail.setText(lecturer.getEmail());
                etPhone.setText(lecturer.getPhone());
                btnSave.setText("שמור");

                final AlertDialog dialog = builder.create();
                dialog.show();

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newName = etName.getText().toString().trim();
                        String newEmail = etEmail.getText().toString().trim();
                        String newPhone = etPhone.getText().toString().trim();


                        if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
                            Toast.makeText(context, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("name", newName);
                        updates.put("email", newEmail);
                        updates.put("phone", newPhone);

                        lecturersRef.child(lecturer.getId()).updateChildren(updates)
                                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "מרצה עודכן", Toast.LENGTH_SHORT).show();

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
                listener.onLecturerClick(lecturer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lecturers.size();
    }

    // ================== ViewHolder ==================
    public static class LecturerViewHolder extends RecyclerView.ViewHolder {

        TextView tvLecturerName, tvLecturerEmail , tvLecturerPhone;
        Button btnEdit, btnDelete;

        public LecturerViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLecturerName = itemView.findViewById(R.id.tvLecturerName);
            tvLecturerEmail = itemView.findViewById(R.id.tvLecturerEmail);
            tvLecturerPhone = itemView.findViewById(R.id.tvLecturerPhone);
            btnEdit = itemView.findViewById(R.id.btnEditLecturer);
            btnDelete = itemView.findViewById(R.id.btnDeleteLecturer);
        }
    }
}
