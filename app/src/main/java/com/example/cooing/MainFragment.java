package com.example.cooing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private MemoAdapter memoAdapter;
    private TextView coupleDateTextView;
    private Map<String, List<String>> monthlyMemoMap; // Map to store memos by month
    private ImageView profileImage; // 내 캐릭터 이미지를 표시할 ImageView
    private ImageView partnerImageView; // 상대방 캐릭터 이미지를 표시할 ImageView
    private String member_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // arguments로부터 member_id 가져오기
        if (getArguments() != null) {
            member_id = getArguments().getString("member_id");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);

        calendarView = rootview.findViewById(R.id.calendarView);
        recyclerView = rootview.findViewById(R.id.recyclerView);
        profileImage = rootview.findViewById(R.id.profileImage); // 내 캐릭터 ImageView 초기화
        partnerImageView = rootview.findViewById(R.id.partnerImageView); // 상대방 캐릭터 ImageView 초기화
        coupleDateTextView = rootview.findViewById(R.id.coupleDateTextView);


        monthlyMemoMap = new HashMap<>();
        memoAdapter = new MemoAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(memoAdapter);
        // 캐릭터와 커플 날짜를 동시에 가져오기
        new GetCharactersTask().execute(member_id);  // 캐릭터 데이터를 가져오는 AsyncTask
        new GetCoupleDateTask().execute(member_id);   // 커플 날짜를 가져오는 AsyncTask
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

    private class GetCharactersTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String member_id = params[0];
            String response = "";
            try {
                Log.d("GetNicknameTask", "Fetching nickname for member_id: " + member_id);

                URL url = new URL("http://cooing.dothome.co.kr/main_item.php?member_id=" + member_id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                Log.d("GetNicknameTask", "Response Code: " + responseCode);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                response = stringBuilder.toString();
                reader.close();
                Log.d("GetNicknameTask", "Response: " + response);
            } catch (Exception e) {
                Log.e("GetNicknameTask", "Error fetching nickname", e);
            }
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.isEmpty()) {
                Log.e("GetCharactersTask", "Received empty response");
                Toast.makeText(getContext(), "Failed to load characters.", Toast.LENGTH_SHORT).show();
                return; // Early exit if the response is empty
            }

            try {
                JSONObject jsonObject = new JSONObject(result);

                // 내 캐릭터 이미지 URL 가져오기
                String profileImageUrl = jsonObject.optString("profileImage", null);
                // 상대방 캐릭터 이미지 URL 가져오기
                String partnerImageUrl = jsonObject.optString("partnerImageView", null);

                // 내 캐릭터 이미지 설정
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(profileImage.getContext())
                            .load(profileImageUrl)
                            .placeholder(R.drawable.baseline_person_24) // 기본 이미지 (로딩 중)
                            .into(profileImage);
                } else {
                    profileImage.setImageResource(R.drawable.baseline_person_24); // 기본 이미지 설정
                }

                // 상대방 캐릭터 이미지 설정
                if (partnerImageUrl != null && !partnerImageUrl.isEmpty()) {
                    Glide.with(partnerImageView.getContext())
                            .load(partnerImageUrl)
                            .placeholder(R.drawable.baseline_person_24) // 기본 이미지 (로딩 중)
                            .into(partnerImageView);
                } else {
                    partnerImageView.setImageResource(R.drawable.baseline_person_24); // 기본 이미지 설정
                }

            } catch (JSONException e) {
                Log.e("GetCharactersTask", "Error parsing JSON", e);
                // JSON 파싱 중 에러 발생 시 기본 이미지 설정
                profileImage.setImageResource(R.drawable.baseline_person_24); // 기본 이미지 설정
                partnerImageView.setImageResource(R.drawable.baseline_person_24); // 기본 이미지 설정
            }
        }
    }
    private class GetCoupleDateTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String member_id = params[0];
            String response = "";
            try {
                URL url = new URL("http://cooing.dothome.co.kr/get_couple_date.php?member_id=" + member_id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    response = stringBuilder.toString();
                    reader.close();
                }
            } catch (Exception e) {
                Log.e("GetCoupleDateTask", "Error fetching couple date", e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || result.isEmpty()) {
                Log.e("GetCoupleDateTask", "Received empty response");
                coupleDateTextView.setText("Failed to load couple date.");
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                String coupleDate = jsonObject.optString("days", "??");
                coupleDateTextView.setText("쿠잉한지 " + coupleDate +"일 째"); // 텍스트 설정
            } catch (JSONException e) {
                Log.e("GetCoupleDateTask", "Error parsing JSON", e);
                coupleDateTextView.setText("Failed to load couple date.");
            }
        }
    }
}