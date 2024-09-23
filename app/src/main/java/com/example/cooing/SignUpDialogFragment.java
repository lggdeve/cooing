package com.example.cooing;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SignUpDialogFragment extends DialogFragment {

    private EditText signUpIdText, signUpPwText, confirmPwText, nameText, phoneText, birthDateText;
    private Button checkIdButton, confirmPwButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // 레이아웃 인플레이트
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.sign_up_dialog, null);

        // UI 구성 요소 초기화
        signUpIdText = view.findViewById(R.id.sign_up_id_text);
        signUpPwText = view.findViewById(R.id.sign_up_pw_text);
        confirmPwText = view.findViewById(R.id.confirm_pw_text);
        nameText = view.findViewById(R.id.name_text);
        phoneText = view.findViewById(R.id.phone_text);
        birthDateText = view.findViewById(R.id.birth_date_text);

        checkIdButton = view.findViewById(R.id.check_id_button);
        confirmPwButton = view.findViewById(R.id.confirm_pw_button);

        // 생년월일 입력 시 DatePickerDialog를 띄움
        birthDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // ID 중복 확인 버튼 리스너 설정
        checkIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIdAvailability();
            }
        });

        // 비밀번호 확인 버튼 리스너 설정
        confirmPwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPasswordMatch();
            }
        });

        // 다이얼로그 설정
        builder.setView(view)
                .setTitle("회원가입")
                .setPositiveButton("가입", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        handleSignUp();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SignUpDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    // 생년월일 선택을 위한 DatePickerDialog
    private void showDatePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.date_picker_dialog, null);
        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);

        // DatePicker 초기화 (캘린더뷰 숨기고 스피너만 보여줌)
        datePicker.setCalendarViewShown(false);
        datePicker.setSpinnersShown(true);

        builder.setView(dialogView)
                .setTitle("생년월일 선택")
                .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int year = datePicker.getYear();
                        int month = datePicker.getMonth() + 1; // 0부터 시작하므로 1 더해줌
                        int day = datePicker.getDayOfMonth();
                        birthDateText.setText(String.format("%d-%02d-%02d", year, month, day));
                        Toast.makeText(getActivity(), "생년월일이 설정되었습니다: " + birthDateText.getText(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }

    // ID 중복 확인 로직
    private void checkIdAvailability() {
        String id = signUpIdText.getText().toString().trim();
        if (TextUtils.isEmpty(id)) {
            Toast.makeText(getActivity(), "ID를 입력하세요.", Toast.LENGTH_SHORT).show();
        } else {
            // 서버와 통신하여 ID 중복 확인 로직 필요
            Toast.makeText(getActivity(), "ID 사용 가능!", Toast.LENGTH_SHORT).show();
        }
    }

    // 비밀번호 일치 확인 로직
    private void checkPasswordMatch() {
        String password = signUpPwText.getText().toString().trim();
        String confirmPassword = confirmPwText.getText().toString().trim();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "비밀번호 일치!", Toast.LENGTH_SHORT).show();
        }
    }

    // 회원가입 처리 로직
    private void handleSignUp() {
        String signUpId = signUpIdText.getText().toString().trim();
        String signUpPassword = signUpPwText.getText().toString().trim();
        String name = nameText.getText().toString().trim();
        String phone = phoneText.getText().toString().trim();
        String birthDate = birthDateText.getText().toString().trim();

        if (TextUtils.isEmpty(signUpId) || TextUtils.isEmpty(signUpPassword) || TextUtils.isEmpty(name)
                || TextUtils.isEmpty(phone) || TextUtils.isEmpty(birthDate)) {
            Toast.makeText(getActivity(), "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
        } else {
            // 서버와 통신하여 회원가입 처리 로직
            new SignUpTask().execute(signUpId, signUpPassword, name, birthDate, phone);
            // 여기에 다음 액티비티로 이동하는 코드 추가
            Intent intent = new Intent(getActivity(), CoupleCodeActivity.class); // NextActivity는 다음 액티비티의 이름
            startActivity(intent);
        }
    }

    private class SignUpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://cooing.dothome.co.kr/sign_up.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                conn.setDoOutput(true);

                // POST 데이터 생성
                String postData = "member_id=" + URLEncoder.encode(params[0], "UTF-8")
                        + "&member_password=" + URLEncoder.encode(params[1], "UTF-8")
                        + "&name=" + URLEncoder.encode(params[2], "UTF-8")
                        + "&birth=" + URLEncoder.encode(params[3], "UTF-8")
                        + "&phonenumber=" + URLEncoder.encode(params[4], "UTF-8");
                Log.d("SignUpTask", "doInBackground started");
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
                Log.e("SignUpTask", "Exception in doInBackground", e);
                return null;
            }
        }
    }
}