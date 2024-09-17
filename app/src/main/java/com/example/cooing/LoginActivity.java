package com.example.cooing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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
                    // 로그인 성공 시 MainActivity로 이동
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // 현재 로그인 액티비티 종료
                }
            }
        });

        // 회원가입 버튼 클릭 리스너
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 화면으로 이동
                // 예를 들어, SignUpActivity로 이동

            }
        });
    }
}

