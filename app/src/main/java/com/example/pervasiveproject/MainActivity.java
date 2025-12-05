package com.example.pervasiveproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ImageView imgLock;
    private TextView tvStatus;
    private Button btnOpenDoor;
    private Button btnAddUser;
    private boolean isDoorOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        imgLock = findViewById(R.id.img_lock);
        tvStatus = findViewById(R.id.tv_status);
        btnOpenDoor = findViewById(R.id.btn_open_door);
        btnAddUser = findViewById(R.id.btn_add_user);

        // Set up edge-to-edge (fix for root layout)
        getWindow().getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsetsCompat compatInsets = WindowInsetsCompat.toWindowInsetsCompat(insets);
            int paddingTop = compatInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, paddingTop, 0, 0); // Adjust padding for status bar
            return insets;
        });

        // Open Door Button
        btnOpenDoor.setOnClickListener(v -> {
            if (isDoorOpen) {
                lockDoor();
            } else {
                unlockDoor();
            }
        });

        // Add New User Button (opens Keypad)


        // Menu button in toolbar (assuming ID from XML)
        findViewById(R.id.btn_menu).setOnClickListener(v -> {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
            // Add menu logic later
        });

        // Initial state
        lockDoor();
    }

    private void unlockDoor() {
        isDoorOpen = true;
        imgLock.setImageResource(R.drawable.ic_lock_open);
        tvStatus.setText(R.string.door_unlocked);
        tvStatus.setTextColor(getColor(R.color.green));
        btnOpenDoor.setText(R.string.close_door);
        addLog("Admin", "Door opened");
    }

    private void lockDoor() {
        isDoorOpen = false;
        imgLock.setImageResource(R.drawable.ic_lock_closed);
        tvStatus.setText(R.string.door_locked);
        tvStatus.setTextColor(getColor(R.color.red));
        btnOpenDoor.setText(R.string.open_door);
        addLog("System", "Door locked");
    }

    private void addLog(String name, String action) {
        // In real app: add to RecyclerView adapter
        // Here: just show toast for demo
        Toast.makeText(this, name + " - " + action + " at " + getCurrentTime(), Toast.LENGTH_SHORT).show();
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}