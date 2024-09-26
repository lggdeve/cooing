package com.example.cooing;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Random;

public class SettingFragment extends Fragment {

    private TextView generatedCodeTextView;
    private EditText partnerCodeEditText;
    private TextView startDateTextView;

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
            String randomCode = generateRandomCode();
            generatedCodeTextView.setText("생성된 코드: " + randomCode);

            // 버튼을 비활성화하여 한 번만 누를 수 있게 설정
            generateCodeButton.setEnabled(false);
        });


        // 연결하기 버튼 클릭 리스너 (여기서 연결 로직 추가)
        connectButton.setOnClickListener(v -> {
            String partnerCode = partnerCodeEditText.getText().toString();
            // 연결 로직 추가
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
}
