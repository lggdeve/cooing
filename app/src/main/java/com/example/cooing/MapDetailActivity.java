package com.example.cooing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MapDetailFragment extends Fragment {

    private EditText editTextTitle;
    private EditText editTextContent;
    private static final String SERVER_URL = "http://cooing.dothome.co.kr/write_update.php"; // 서버 URL

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 프래그먼트 레이아웃 설정
        View view = inflater.inflate(R.layout.map_detail, container, false);

        // UI 요소 초기화
        editTextTitle = view.findViewById(R.id.editTextTitle_mapDetail);
        editTextContent = view.findViewById(R.id.editTextContent_mapDetail);
        ImageButton backButton = view.findViewById(R.id.backButton_mapDetail);
        Button uploadButton = view.findViewById(R.id.uploadButton_mapDetail);

        // 뒤로가기 버튼 클릭 리스너 설정
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // 업로드 버튼 클릭 리스너 설정
        uploadButton.setOnClickListener(v -> uploadData());

        return view;
    }

    // 데이터 업로드 메서드
    private void uploadData() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        // 요청 큐 생성
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        // POST 요청 생성
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SERVER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(requireContext(), "Data uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(requireContext(), "Error uploading data", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("content", content);
                return params;
            }
        };

        // 요청 큐에 요청 추가
        requestQueue.add(stringRequest);
    }
}
