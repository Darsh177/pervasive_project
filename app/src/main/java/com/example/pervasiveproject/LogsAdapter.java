package com.example.pervasiveproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {

    private final List<LogItem> logs;

    public LogsAdapter(List<LogItem> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new LogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogItem item = logs.get(position);

        String nameTime = "Code: " + item.getCode() + " • " + item.getFormattedTime();
        holder.tvNameTime.setText(nameTime);

        String res = item.getResult();
        if (res == null) res = "";
        holder.tvExtra.setText(res);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {

        TextView tvNameTime;
        TextView tvExtra;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameTime = itemView.findViewById(R.id.tv_name_time);
            tvExtra = itemView.findViewById(R.id.tv_extra);
        }
    }
}
