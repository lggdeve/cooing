package com.example.cooing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private MemoAdapter memoAdapter;
    private Map<String, List<String>> monthlyMemoMap; // Map to store memos by month

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);

        calendarView = rootview.findViewById(R.id.calendarView);
        recyclerView = rootview.findViewById(R.id.recyclerView);

        monthlyMemoMap = new HashMap<>();
        memoAdapter = new MemoAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(memoAdapter);

        loadMemos();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                showPopup(year, month, dayOfMonth);
            }
        });

        return rootview;
    }

    private void showPopup(int year, int month, int dayOfMonth) {
        final EditText editText = new EditText(getContext());
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("메모 작성");
        dialogBuilder.setMessage(String.format("%d년 %d월 %d일 메모", year, month + 1, dayOfMonth));
        dialogBuilder.setView(editText);

        dialogBuilder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String memo = editText.getText().toString();
                if (!memo.isEmpty()) {
                    saveMemo(memo, year, month, dayOfMonth);
                    Toast.makeText(getContext(), "메모가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "메모를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void saveMemo(String memo, int year, int month, int dayOfMonth) {
        String fileName = year + "_" + (month + 1) + "_" + dayOfMonth + ".txt";
        try {
            FileOutputStream fos = getContext().openFileOutput(fileName, getContext().MODE_PRIVATE);
            fos.write(memo.getBytes("UTF-8"));  // UTF-8 인코딩으로 저장
            fos.close();

            // Update the map and notify the adapter
            String monthKey = year + "_" + (month + 1);
            if (!monthlyMemoMap.containsKey(monthKey)) {
                monthlyMemoMap.put(monthKey, new ArrayList<>());
            }
            monthlyMemoMap.get(monthKey).add(memo);
            updateRecyclerView(monthKey); // Update RecyclerView with the new data

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMemos() {
        File filesDir = getContext().getFilesDir();
        File[] files = filesDir.listFiles();
        if (files != null) {
            monthlyMemoMap.clear();  // Clear the existing map
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[(int) file.length()];
                        fis.read(buffer);
                        fis.close();
                        String memo = new String(buffer, "UTF-8");  // UTF-8 인코딩으로 읽기

                        // Extract year and month from the file name
                        String[] fileNameParts = file.getName().split("_");
                        if (fileNameParts.length == 3) {
                            String yearMonthKey = fileNameParts[0] + "_" + fileNameParts[1];
                            if (!monthlyMemoMap.containsKey(yearMonthKey)) {
                                monthlyMemoMap.put(yearMonthKey, new ArrayList<>());
                            }
                            monthlyMemoMap.get(yearMonthKey).add(memo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            // Optionally, update RecyclerView with the memos for the current month or another default selection
            if (!monthlyMemoMap.isEmpty()) {
                updateRecyclerView((String) monthlyMemoMap.keySet().toArray()[0]);
            }
        }
    }

    private void updateRecyclerView(String monthKey) {
        if (monthlyMemoMap.containsKey(monthKey)) {
            memoAdapter.updateData(monthlyMemoMap.get(monthKey));
        } else {
            memoAdapter.updateData(new ArrayList<>());
        }
    }

}
