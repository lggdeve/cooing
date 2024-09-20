package com.example.cooing;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.cooing.NewDetailsDialogFragment;

import java.util.Calendar;
import java.util.Random;

public class NewDetailsDialogFragment extends DialogFragment {

    private EditText nicknameText, dateText, codeText, enterCodeText;
    private Button generateCodeButton;
    private String generatedCode;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // 레이아웃 인플레이트
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_details_dialog, null);

        // UI 구성 요소 초기화
        nicknameText = view.findViewById(R.id.nickname_text);
        dateText = view.findViewById(R.id.date_text);
        codeText = view.findViewById(R.id.code_text);
        enterCodeText = view.findViewById(R.id.enter_code_text);
        generateCodeButton = view.findViewById(R.id.generate_code_button);

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

        builder.setView(view)
                .setTitle("추가 정보 설정")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 추가 정보 처리 로직
                        String nickname = nicknameText.getText().toString().trim();
                        String date = dateText.getText().toString().trim();
                        String enteredCode = enterCodeText.getText().toString().trim();

                        if (nickname.isEmpty() || date.isEmpty() || enteredCode.isEmpty()) {
                            Toast.makeText(getActivity(), "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
                        } else if (!enteredCode.equals(generatedCode)) {
                            Toast.makeText(getActivity(), "입력한 코드가 발급받은 코드와 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            // 추가 정보 처리 로직 (예: 서버에 전송, 데이터베이스에 저장 등)
                            Toast.makeText(getActivity(), "이제 쿠잉의 서비스를 사용해보세요!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewDetailsDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    private void showDatePickerDialog() {
        // AlertDialog 안에 DatePicker를 포함한 커스텀 다이얼로그 만들기
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.date_picker_dialog, null);
        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);

        // DatePicker 초기화
        datePicker.setCalendarViewShown(false); // 캘린더 뷰 숨기기
        datePicker.setSpinnersShown(true); // 스피너만 보여주기

        builder.setView(dialogView)
                .setTitle("날짜 선택")
                .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // DatePicker에서 선택한 날짜를 가져오기
                        int year = datePicker.getYear();
                        int month = datePicker.getMonth(); // 0부터 시작하므로 +1 필요
                        int day = datePicker.getDayOfMonth();

                        // 선택한 날짜를 EditText에 설정
                        dateText.setText(String.format("%d-%02d-%02d", year, month + 1, day));

                        // 날짜가 올바르게 설정되었는지 로그 확인
                        Toast.makeText(getActivity(), "날짜가 설정되었습니다: " + dateText.getText().toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }



    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder(8); // 코드 길이 설정

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }
}
