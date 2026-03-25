package com.example.lecturemanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LecturesAdapter
        extends RecyclerView.Adapter<LecturesAdapter.LectureViewHolder> {

    private ArrayList<Lecture> lectures;
    private Map<String, Group> groupsMap;
    private Map<String, Lecturer> lecturersMap;

    OnLectureClickListener clickListener;
    private OnLectureLongClickListener longClickListener;
    DatabaseReference groupsRef , lecturersRef;
    private Context context;

    public interface OnLectureClickListener {
        void onLectureClick(Lecture lecture);
    }

    public interface OnLectureLongClickListener {
        void onLectureLongClick(Lecture lecture);
    }
    public LecturesAdapter(Context context,
                            ArrayList<Lecture> lectures,
                           OnLectureClickListener clickListener,
                           OnLectureLongClickListener longClickListener ,
                           DatabaseReference groupsRef,
                           DatabaseReference lecturersRef) {
        this.lectures = lectures;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.groupsRef = groupsRef;
        this.context = context;
        this.lecturersRef = lecturersRef;

        groupsMap = new HashMap<>();
        groupsRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot groupSnapshot) {
                for (DataSnapshot groupSnap: groupSnapshot.getChildren()) {
                    Group group = groupSnap.getValue(Group.class);
                    if (group == null) continue;
                    group.setId(groupSnap.getKey());
                    groupsMap.put(group.getId(), group);
            }
                notifyDataSetChanged();
        }
    });

        lecturersMap = new HashMap<>();
        lecturersRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot lecturerSnapshot) {
                for (DataSnapshot lecturerSnap: lecturerSnapshot.getChildren()) {
                    Lecturer lecturer = lecturerSnap.getValue(Lecturer.class);
                    if (lecturer == null) continue;
                    lecturer.setId(lecturerSnap.getKey());
                    lecturersMap.put(lecturer.getId(), lecturer);
                }
                notifyDataSetChanged();
            }
        });

    }

    @NonNull
    @Override
    public LectureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lecture, parent, false);
        return new LectureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LectureViewHolder holder, int position) {
        final Lecture lecture = lectures.get(position);

        Group g = groupsMap.get(lecture.getGroupId());
        if (g != null) {
            holder.tvGroupName.setText(g.getName());
        } else {
            holder.tvGroupName.setText("קבוצה לא נמצאה");
        }

        Lecturer l = lecturersMap.get(lecture.getLecturerId());
        if (l != null) {
            holder.tvLecturer.setText(l.getName());
        } else {
            holder.tvLecturer.setText("מרצה לא נמצא");
        }


        holder.tvTitle.setText(lecture.getTitle());

        Date lectureDate = lecture.getDate();

        if (lectureDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.tvDate.setText(sdf.format(lectureDate));
        } else {
            holder.tvDate.setText("ללא תאריך");
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onLectureClick(lecture);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onLectureLongClick(lecture);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return lectures.size();
    }

    static class LectureViewHolder extends RecyclerView.ViewHolder {

        TextView tvGroupName, tvTitle, tvLecturer, tvDate;

        public LectureViewHolder(@NonNull View itemView) {
            super(itemView);

            tvGroupName =itemView.findViewById(R.id.tvLectureGroup);
            tvTitle = itemView.findViewById(R.id.tvLectureTitle);
            tvLecturer = itemView.findViewById(R.id.tvLecturer);
            tvDate = itemView.findViewById(R.id.tvDate);

        }
    }
}

