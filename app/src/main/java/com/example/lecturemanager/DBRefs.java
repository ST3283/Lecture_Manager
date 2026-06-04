package com.example.lecturemanager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DBRefs {
    public static final String DB_URL = "https://lecture-manager-356ad-default-rtdb.europe-west1.firebasedatabase.app/";
    public static final String LECTURERS = "lecturers";
    public static final String LECTURES = "lectures";
    public static final String GROUPS = "groups";
    public static final String USERS = "users";

    public static final String LECTURER_ID = "lecturerId";
    public static final String LECTURE_ID = "lectureId";
    public static final String GROUP_ID = "groupId";
    public static final String USER_ID = "userId";

    public static final FirebaseDatabase dbref = FirebaseDatabase.getInstance(DB_URL);

    public static final DatabaseReference lecturersRef = dbref.getReference(LECTURERS);
    public static final DatabaseReference lecturesRef = dbref.getReference(LECTURES);
    public static final DatabaseReference groupsRef = dbref.getReference(GROUPS);
    public static final DatabaseReference usersRef = dbref.getReference(USERS);

}
