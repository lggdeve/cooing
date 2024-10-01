package com.example.cooing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AnswerActivity extends AppCompatActivity {

    private TextView questionTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wirte_request);


        ImageButton answerbackbutton = findViewById(R.id.back_button_answer);
        ImageButton answersavebutton = findViewById(R.id.save_button_answer);

        answerbackbutton.setOnClickListener(v -> onBackPressed());

        questionTextView = findViewById(R.id.question_answer);

        Intent intent = getIntent();
        String question = intent.getStringExtra("question");

        questionTextView.setText(question);
    }
}
