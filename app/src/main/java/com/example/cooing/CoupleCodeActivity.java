package com.example.cooing;

import android.app.AlertDialog;
import android.content.SharedPreferences; // 추가
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

public class CoupleCodeActivity extends AppCompatActivity {

    private EditText nicknameText, dateText, codeText, enterCodeText; // myIdEditText 추가
    private Button generateCodeButton, saveButton; // 저장 버튼 추가
    private String generatedCode;
    private  TextView myIdTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.couple_code); // 액티비티 레이아웃 설정

        // UI 구성 요소 초기화
        nicknameText = findViewById(R.id.nickname_text);
        dateText = findViewById(R.id.date_text);
        codeText = findViewById(R.id.code_text);
        enterCodeText = findViewById(R.id.enter_code_text);
        myIdTextview = findViewById(R.id.myid); // myIdEditText 초기화
        generateCodeButton = findViewById(R.id.generate_code_button);
        saveButton = findViewById(R.id.connection_button); // 저장 버튼 초기화

        // SharedPreferences에서 아이디 가져오기
        SharedPreferences sharedPref = getSharedPreferences("MyPref", MODE_PRIVATE);
        String userId = sharedPref.getString("user_id", null); // 기본값은 null

        // TextView 초기화
        TextView myIdTextView = findViewById(R.id.myid); // myIdTextView 초기화

        // 가져온 아이디가 null이 아닐 경우 TextView에 설정
        if (userId != null) {
            myIdTextView.setText(userId); // TextView에 아이디 설정
        }

        // 날짜 선택 버튼 클릭 리스너 설정
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // 코드 발급 버튼 클릭 리스너 설정
        generateCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatedCode = generateRandomCode();
                codeText.setText(generatedCode);
            }
        });

        // 저장 버튼 클릭 리스너
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = nicknameText.getText().toString().trim();
                String date = dateText.getText().toString().trim();
                String enteredCode = enterCodeText.getText().toString().trim();

                if (TextUtils.isEmpty(nickname) || TextUtils.isEmpty(date) || TextUtils.isEmpty(enteredCode)) {
                    Toast.makeText(CoupleCodeActivity.this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
                } else if (!enteredCode.equals(generatedCode)) {
                    Toast.makeText(CoupleCodeActivity.this, "입력한 코드가 발급받은 코드와 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    new SendCoupleDataTask().execute(nickname, date, generatedCode);
                }
            }
        });
    }

    private void showDatePickerDialog() {
        // DatePickerDialog를 포함한 AlertDialog 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.date_picker_dialog, null);
        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);

        // DatePicker 초기화
        datePicker.setCalendarViewShown(false); // 캘린더 뷰 숨기기
        datePicker.setSpinnersShown(true); // 스피너만 보여주기

        builder.setView(dialogView)
                .setTitle("날짜 선택")
                .setPositiveButton("저장", (dialog, id) -> {
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth(); // 0부터 시작하므로 +1 필요
                    int day = datePicker.getDayOfMonth();

                    // 선택한 날짜를 EditText에 설정
                    dateText.setText(String.format("%d-%02d-%02d", year, month + 1, day));
                    Toast.makeText(CoupleCodeActivity.this, "날짜가 설정되었습니다: " + dateText.getText().toString(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder(10); // 코드 길이 설정

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }

    // AsyncTask로 PHP 서버에 데이터 전송
    private class SendCoupleDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String nickname = params[0];
            String coupleDate = params[1];
            String coupleCode = params[2];

            try {
                URL url = new URL("http://cooing.dothome.co.kr/couple_code.php"); // 서버의 PHP 파일 URL로 변경 필요
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                conn.setDoOutput(true);

                // POST 데이터 생성
                String postData = "nickname=" + URLEncoder.encode(nickname, "UTF-8") +
                        "&couple_date=" + URLEncoder.encode(coupleDate, "UTF-8") +
                        "&couple_id=" + URLEncoder.encode(coupleCode, "UTF-8");

                // POST 데이터 전송
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                // 서버 응답 처리
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                conn.disconnect();

                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(CoupleCodeActivity.this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
