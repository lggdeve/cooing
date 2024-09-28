package com.example.cooing;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private EditText idText;
    private EditText pwText;
    private Button loginButton;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // XML 파일 이름이 맞는지 확인

        // UI 요소 초기화
        idText = findViewById(R.id.id_text);
        pwText = findViewById(R.id.pw_text);
        loginButton = findViewById(R.id.login_button);
        signUpButton = findViewById(R.id.sign_up_button);

        // 로그인 버튼 클릭 리스너
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idText.getText().toString().trim();
                String password = pwText.getText().toString().trim();

                if (id.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // 로그인 처리 로직 추가 (서버와 통신 등)
                    new LoginTask().execute(id, password);
                }
            }
        });

        // 회원가입 버튼 클릭 리스너
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpDialogFragment signUpDialog = new SignUpDialogFragment();
                signUpDialog.show(getSupportFragmentManager(), "SignUpDialog");
            }
        });

        // 로그인 화면에 메시지 표시
        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    // 로그인 처리 AsyncTask
    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String id = params[0];
            String password = params[1];
            String response = "";

            try {
                URL url = new URL("http://cooing.dothome.co.kr/login.php"); // PHP 파일 URL
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);

                String postData = "member_id=" + id + "&member_password=" + password;

                // 데이터 전송
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                // 응답 받기
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // JSON 응답 처리
                JSONObject jsonResponse = new JSONObject(result);
                String message = jsonResponse.getString("message");

                if (message.equals("로그인 성공")) {
                    // 로그인 성공 시 회원 ID를 MainActivity로 전달
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("member_id",idText.getText().toString()); // ID 전달
                    startActivity(intent); // MainActivity로 이동

                    finish(); // 현재 로그인 액티비티 종료
                } else {
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(LoginActivity.this, "응답 처리 오류", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
