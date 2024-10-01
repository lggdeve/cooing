package com.example.cooing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class QuestionFragment extends Fragment {
    private RecyclerView recyclerView;
    private QuestionAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootview = (ViewGroup)inflater.inflate(R.layout.fragment_question,
                container, false);

        recyclerView = rootview.findViewById(R.id.question_list); // RecyclerView 초기화
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // LayoutManager 설정


        List<String> items = new ArrayList<>();

        adapter = new QuestionAdapter();
        recyclerView.setAdapter(adapter);

        return rootview;
    }
}
