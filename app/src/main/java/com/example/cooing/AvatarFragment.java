package com.example.cooing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AvatarFragment extends Fragment {

    private ImageView imageViewBoy, imageViewGirl;
    private String selectedImage;
    private String member_id; // member_id 저장할 변수

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bundle에서 전달된 member_id 가져오기
        if (getArguments() != null) {
            member_id = getArguments().getString("member_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_avatar, container, false);

        imageViewBoy = rootView.findViewById(R.id.imageViewBoy);
        imageViewGirl = rootView.findViewById(R.id.imageViewGirl);

        loadImages(); // 이미지를 불러옵니다.

        imageViewBoy.setOnClickListener(view -> selectImage("M_1"));
        imageViewGirl.setOnClickListener(view -> selectImage("F_1"));

        return rootView;
    }

    private void loadImages() {
        String url = "http://cooing.dothome.co.kr/load_item.php"; // PHP 스크립트 URL

        // Volley를 사용하여 서버에 요청
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.has("characters")) {
                            JSONArray characters = response.getJSONArray("characters");
                            // 이미지 URL을 Glide로 로드
                            if (characters.length() > 0) {
                                JSONObject boyCharacter = characters.getJSONObject(0);
                                String boyImageUrl = boyCharacter.getString("image_url");
                                Glide.with(this).load(boyImageUrl).into(imageViewBoy);
                            }
                            if (characters.length() > 1) {
                                JSONObject girlCharacter = characters.getJSONObject(1);
                                String girlImageUrl = girlCharacter.getString("image_url");
                                Glide.with(this).load(girlImageUrl).into(imageViewGirl);
                            }
                        } else {
                            Toast.makeText(getContext(), response.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "서버 요청 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void selectImage(String item_id) {
        selectedImage = item_id;

        // 이미 선택된 member_id를 사용하여 저장
        saveSelectedImage(member_id, selectedImage);
    }

    private void saveSelectedImage(String member_id, String item_id) {
        String url = "http://cooing.dothome.co.kr/choice_item.php?member_id=" + member_id + "&item_id=" + item_id;

        // Volley를 사용하여 서버에 요청
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // 응답 처리
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("success")) {
                            Toast.makeText(getContext(), "이미지가 성공적으로 저장되었습니다!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "이미지 저장 실패: " + jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "서버 요청 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(stringRequest);
    }
}
