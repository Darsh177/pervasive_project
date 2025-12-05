package com.example.pervasiveproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imgLock;
    private TextView tvStatus;
    private Button btnOpenDoor;
    private Button btnAddUser;

    private RecyclerView rvLogs;
    private LogsAdapter logsAdapter;
    private final List<LogItem> logItems = new ArrayList<>();

    private FirebaseDatabase database;
    private DatabaseReference doorCommandRef;
    private DatabaseReference accessLogsRef;
    private DatabaseReference accessCodesRef;

    private boolean isDoorOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        imgLock = findViewById(R.id.img_lock);
        tvStatus = findViewById(R.id.tv_status);
        btnOpenDoor = findViewById(R.id.btn_open_door);
        btnAddUser = findViewById(R.id.btn_add_user);
        rvLogs = findViewById(R.id.rv_logs);

        getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsetsCompat compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets);
            int paddingTop = compatInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, paddingTop, 0, 0);
            return insets;
        });

        findViewById(R.id.btn_menu).setOnClickListener(v ->
                Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        );

        initFirebase();

        setupLogsRecycler();

        listenForLogs();

        btnOpenDoor.setOnClickListener(v -> sendRemoteUnlock());

        btnAddUser.setOnClickListener(v -> showAddUserDialog());

        lockDoorUi();
    }


    private void initFirebase() {
        database = FirebaseDatabase.getInstance(
                "https://smart-door-6e61f-default-rtdb.firebaseio.com"
        );

        doorCommandRef = database.getReference("door").child("command");
        accessLogsRef = database.getReference("access_logs");
        accessCodesRef = database.getReference("access_codes");
    }


    private void setupLogsRecycler() {
        logsAdapter = new LogsAdapter(logItems);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        rvLogs.setAdapter(logsAdapter);
    }

    private void listenForLogs() {
        accessLogsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                LogItem item = parseLogSnapshot(snapshot);
                if (item != null) {
                    logItems.add(0, item);
                    logsAdapter.notifyItemInserted(0);
                    rvLogs.scrollToPosition(0);
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Error loading logs: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private LogItem parseLogSnapshot(DataSnapshot snapshot) {
        String code = snapshot.child("code").getValue(String.class);
        String result = snapshot.child("result").getValue(String.class);
        Long ts = snapshot.child("timestamp").getValue(Long.class);

        if (code == null) code = "";
        if (result == null) result = "";
        if (ts == null) ts = System.currentTimeMillis() / 1000L; // fallback

        return new LogItem(code, result, ts);
    }


    private void sendRemoteUnlock() {
        doorCommandRef.setValue("UNLOCK")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(MainActivity.this,
                            "Remote unlock command sent",
                            Toast.LENGTH_SHORT).show();
                    unlockDoorUiTemporary();
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this,
                        "Failed to send command: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    private void unlockDoorUi() {
        isDoorOpen = true;
        imgLock.setImageResource(R.drawable.ic_lock_open);
        tvStatus.setText(R.string.door_unlocked);
        tvStatus.setTextColor(getColor(R.color.green));
        btnOpenDoor.setText(R.string.close_door);
    }

    private void lockDoorUi() {
        isDoorOpen = false;
        imgLock.setImageResource(R.drawable.ic_lock_closed);
        tvStatus.setText(R.string.door_locked);
        tvStatus.setTextColor(getColor(R.color.red));
        btnOpenDoor.setText(R.string.open_door);
    }

    private void unlockDoorUiTemporary() {
        unlockDoorUi();
        addLocalToastLog("Admin", "Remote unlock command sent");

        btnOpenDoor.postDelayed(this::lockDoorUi, 4000);
    }

    private void addLocalToastLog(String name, String action) {
        Toast.makeText(this,
                name + " - " + action + " at " + getCurrentTime(),
                Toast.LENGTH_SHORT).show();
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }


    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add / Update Access Code");

        final android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        final android.widget.EditText etCode = new android.widget.EditText(this);
        etCode.setHint("Code (e.g. 121)");
        etCode.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        final SwitchMaterial switchAllowed = new SwitchMaterial(this);
        switchAllowed.setText("Allowed?");

        layout.addView(etCode);
        layout.addView(switchAllowed);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String code = etCode.getText().toString().trim();
            boolean allowed = switchAllowed.isChecked();

            if (code.isEmpty()) {
                Toast.makeText(MainActivity.this,
                        "Code cannot be empty",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            saveAccessCode(code, allowed);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void saveAccessCode(String code, boolean allowed) {
        accessCodesRef.child(code).setValue(allowed)
                .addOnSuccessListener(unused -> Toast.makeText(MainActivity.this,
                        "Code " + code + " saved. allowed=" + allowed,
                        Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this,
                        "Failed to save code: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }
}
