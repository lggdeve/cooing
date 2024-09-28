package com.example.cooing;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Random;

public class SettingFragment extends Fragment {

    private TextView generatedCodeTextView;
    private EditText partnerCodeEditText;
    private TextView startDateTextView;
    private String member_id; // member_id를 저장할 변수

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
        ViewGroup rootview = (ViewGroup) inflater.inflate(R.layout.fragment_setting, container, false);

        generatedCodeTextView = rootview.findViewById(R.id.generated_code_textview);
        partnerCodeEditText = rootview.findViewById(R.id.partner_code_edittext);
        Button generateCodeButton = rootview.findViewById(R.id.generate_code_button);
        Button connectButton = rootview.findViewById(R.id.connect_button);
        startDateTextView = rootview.findViewById(R.id.start_date_textview);

        // "사랑을 시작한 날짜" 클릭 리스너 설정
        startDateTextView.setOnClickListener(v -> showDatePickerDialog());

        // 랜덤 코드 생성 버튼 클릭 리스너
        generateCodeButton.setOnClickListener(v -> {
            String couple_id = generateRandomCode();
            generatedCodeTextView.setText("생성된 코드: " + couple_id);
            // PHP 서버에 랜덤 코드와 member_id 전송
            new SendCodeTask().execute(couple_id, member_id);
            // 버튼을 비활성화하여 한 번만 누를 수 있게 설정
            generateCodeButton.setEnabled(false);
        });

        // 연결하기 버튼 클릭 리스너
        connectButton.setOnClickListener(v -> {
            String partner_id = partnerCodeEditText.getText().toString();


            // 파트너 코드가 비어있지 않은지 확인
            if (!partner_id.isEmpty()) {
                // SendConnectCodeTask를 실행하여 파트너 코드와 member_id 전송
                new SendConnectCodeTask(this).execute(partner_id, member_id);
            } else {
                Toast.makeText(getActivity(), "파트너 코드를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        return rootview;
    }

    private void showDatePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.date_picker_dialog, null);
        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);

        // DatePicker 초기화
        datePicker.setCalendarViewShown(false); // 캘린더 뷰 숨기기
        datePicker.setSpinnersShown(true); // 스피너 형태로 표시

        // 현재 날짜로 초기화
        Calendar calendar = Calendar.getInstance();
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);

        builder.setView(dialogView)
                .setTitle("날짜 선택")
                .setPositiveButton("저장", (dialog, id) -> {
                    // DatePicker에서 선택한 날짜를 가져오기
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth(); // 0부터 시작하므로 +1 필요
                    int day = datePicker.getDayOfMonth();

                    // 선택한 날짜를 TextView에 설정
                    startDateTextView.setText(String.format("%d-%02d-%02d", year, month + 1, day));
                    Toast.makeText(getActivity(), "선택된 날짜: " + startDateTextView.getText(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        int length = 7 + random.nextInt(2); // 7 또는 8자
        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    private class SendCodeTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String couple_id = params[0];
            String member_id = params[1];
            String response = "";

            try {
                // URL 설정
                String urlString = "http://cooing.dothome.co.kr/couple_code.php"; // PHP 파일의 URL

                // HttpURLConnection을 사용하여 POST 요청 설정
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // POST 데이터 작성
                String postData = "couple_id=" + couple_id + "&member_id=" + member_id;

                // POST 데이터 전송
                try (OutputStream os = conn.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                    writer.write(postData);
                    writer.flush();
                }

                // 서버의 응답 받기
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    responseBuilder.append(line);
                }
                in.close();

                response = responseBuilder.toString();
                Log.d("Response", response);
            } catch (Exception e) {
                Log.e("Error", "Exception occurred", e);
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // 서버 응답 처리
            if (result != null && !result.isEmpty()) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String message = jsonResponse.getString("message");
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    if (jsonResponse.has("couple_id")) {
                        String couple_id = jsonResponse.getString("couple_id");
                        generatedCodeTextView.setText("커플 아이디: " + couple_id);
                    }
                } catch (JSONException e) {
                    Log.e("JSONError", "Failed to parse JSON", e);
                    Toast.makeText(getActivity(), "응답 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "서버 응답이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class SendConnectCodeTask extends AsyncTask<String, Void, String> {
        private WeakReference<Fragment> fragmentReference;

        public SendConnectCodeTask(Fragment fragment) {
            this.fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected String doInBackground(String... params) {
            String partner_id = params[0];
            String member_id = params[1];
            String response = "";

            try {
                // URL 설정
                String urlString = "http://cooing.dothome.co.kr/couple_connection.php";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // POST 데이터 작성 (URL 인코딩)
                String postData = "couple_id=" + URLEncoder.encode(partner_id, "UTF-8") +
                        "&member_id=" + URLEncoder.encode(member_id, "UTF-8");

                // POST 데이터 전송
                try (OutputStream os = conn.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                    writer.write(postData);
                    writer.flush();
                }

                // 응답 코드 확인
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 서버의 응답 받기
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    in.close();
                    response = responseBuilder.toString();
                    Log.d("Response", response);
                } else {
                    Log.e("HTTPError", "Server returned non-OK code: " + responseCode);
                    response = "Error: Server returned non-OK code: " + responseCode; // 에러 메시지 처리
                }
            } catch (Exception e) {
                Log.e("Error", "Exception occurred", e);
                response = "Exception: " + e.getMessage(); // 예외 메시지 처리
            }

            return response;
        }


    @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Fragment fragment = fragmentReference.get();
            if (fragment != null && result != null && !result.isEmpty()) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    String message = jsonResponse.optString("message", ""); // 기본값 추가
                    Toast.makeText(fragment.getActivity(), message, Toast.LENGTH_SHORT).show();

                    // 추가적인 처리: 연결 성공 여부 확인
                    if (jsonResponse.has("success")) {
                        boolean success = jsonResponse.getBoolean("success");
                        if (success) {
                            Toast.makeText(fragment.getActivity(), "파트너와 연결되었습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = jsonResponse.optString("error", "알 수 없는 오류 발생");
                            Toast.makeText(fragment.getActivity(), "연결 실패: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    Log.e("JSONError", "Failed to parse JSON: " + result, e);
                    Toast.makeText(fragment.getActivity(), "응답 처리 중 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("UnexpectedError", "Unexpected error: " + e.getMessage(), e);
                    Toast.makeText(fragment.getActivity(), "예상치 못한 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (fragment != null) {
                    Toast.makeText(fragment.getActivity(), "서버 응답이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}


