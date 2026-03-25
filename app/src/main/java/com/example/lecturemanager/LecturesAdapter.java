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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LecturesAdapter
        extends RecyclerView.Adapter<LecturesAdapter.LectureViewHolder> {

    private ArrayList<Lecture> lectures;
    private Map<String, Group> groupsMap;
     OnLectureClickListener clickListener;
    private OnLectureLongClickListener longClickListener;
    DatabaseReference groupsRef;
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
                           DatabaseReference groupsRef) {
        this.lectures = lectures;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.groupsRef = groupsRef;
        this.context = context;

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

        holder.tvTitle.setText(lecture.getTitle());
        holder.tvTitle.setText(lecture.getTitle());
//        holder.tvDate.setText(lecture.getDate()); //TODO Date

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

