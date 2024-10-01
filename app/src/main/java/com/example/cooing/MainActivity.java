package com.example.cooing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout; // DrawerLayout 객체
    private FragmentManager fragmentManager; // 프래그먼트 매니저
    private Fragment mainFragment, mapFragment, questionFragment, avatarFragment, settingFragment; // 각 프래그먼트
    private BottomNavigationView bottomNavigationView; // 바텀 네비게이션뷰

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Intent로 전달된 member_id 값 가져오기
        String member_id = getIntent().getStringExtra("member_id");

        // DrawerLayout과 NavigationView 초기화
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // 네비게이션 헤더에 프로필 이름과 이미지 설정
        View headerView = navigationView.getHeaderView(0);
        TextView profileName = headerView.findViewById(R.id.profile_name);
        ImageView profileImage = headerView.findViewById(R.id.profile_image);

        // member_id를 사용해 닉네임과 image_url 가져오기 (AsyncTask 실행)
        if (member_id != null) {
            new GetNicknameTask(profileName, profileImage).execute(member_id);
        }

        // 프래그먼트 초기화
        fragmentManager = getSupportFragmentManager();
        mainFragment = new MainFragment();
        mapFragment = new MapFragment();
        questionFragment = new QuestionFragment();
        avatarFragment = new AvatarFragment();
        settingFragment = new SettingFragment();

        // member_id를 포함하는 Bundle 생성
        Bundle args = new Bundle();
        args.putString("member_id", member_id);

        // SettingFragment 설정
        SettingFragment settingFragment = new SettingFragment();
        settingFragment.setArguments(args); // member_id를 SettingFragment에 전달

        // AvatarFragment 설정
        AvatarFragment avatarFragment = new AvatarFragment();
        avatarFragment.setArguments(args); // member_id를 AvatarFragment에도 전달

        // MainFragment 설정
        MainFragment mainFragment = new MainFragment();
        mainFragment.setArguments(args); // member_id를 MainFragment에도 전달


        // 앱 시작 시 MainFragment를 화면에 표시
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.menu_frame_layout, mainFragment)
                    .commitAllowingStateLoss();
        }

        // BottomNavigationView 리스너 설정
        bottomNavigationView = findViewById(R.id.nav_bottom);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = mainFragment;
            } else if (item.getItemId() == R.id.nav_map) {
                selectedFragment = mapFragment;
            } else if (item.getItemId() == R.id.nav_question) {
                selectedFragment = questionFragment;
            } else if (item.getItemId() == R.id.nav_avatar) {
                selectedFragment = avatarFragment;
            }

            if (selectedFragment != null) {
                fragmentManager.beginTransaction()
                        .replace(R.id.menu_frame_layout, selectedFragment)
                        .commitAllowingStateLoss();
            }
            return true;
        });

        // NavigationView 아이템 클릭 시 프래그먼트 전환 및 바텀 네비게이션 숨김/표시
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = mainFragment;
                bottomNavigationView.setVisibility(View.VISIBLE); // 바텀 네비게이션 표시
            } else if (item.getItemId() == R.id.nav_map) {
                selectedFragment = mapFragment;
                bottomNavigationView.setVisibility(View.VISIBLE); // 바텀 네비게이션 표시
            } else if (item.getItemId() == R.id.nav_question) {
                selectedFragment = questionFragment;
                bottomNavigationView.setVisibility(View.VISIBLE); // 바텀 네비게이션 표시
            } else if (item.getItemId() == R.id.nav_avatar) {
                selectedFragment = avatarFragment;
                bottomNavigationView.setVisibility(View.VISIBLE); // 바텀 네비게이션 표시
            } else if (item.getItemId() == R.id.nav_setting) {
                selectedFragment = settingFragment;
                bottomNavigationView.setVisibility(View.GONE); // 바텀 네비게이션 숨기기
            }

            if (selectedFragment != null) {
                fragmentManager.beginTransaction()
                        .replace(R.id.menu_frame_layout, selectedFragment)
                        .commitAllowingStateLoss();
                drawerLayout.closeDrawer(GravityCompat.START); // 드로어 닫기
            }
            return true;
        });

        // 햄버거 버튼 대신 ImageView로 드로어 열기
        ImageView drawerToggleImage = findViewById(R.id.btn_hamburger);
        drawerToggleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
    }

    private class GetNicknameTask extends AsyncTask<String, Void, String> {
        private TextView changeName;
        private ImageView profileImage; // 이미지 뷰 추가

        public GetNicknameTask(TextView changeName, ImageView profileImage) {
            this.changeName = changeName;
            this.profileImage = profileImage; // 이미지 뷰 초기화
        }

        @Override
        protected String doInBackground(String... params) {
            String member_id = params[0];
            String response = "";
            try {
                Log.d("GetNicknameTask", "Fetching nickname for member_id: " + member_id);

                URL url = new URL("http://cooing.dothome.co.kr/get_user.php?member_id=" + member_id);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                Log.d("GetNicknameTask", "Response Code: " + responseCode);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                response = stringBuilder.toString();
                reader.close();
                Log.d("GetNicknameTask", "Response: " + response);
            } catch (Exception e) {
                Log.e("GetNicknameTask", "Error fetching nickname", e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String nickname = jsonObject.optString("nickname", null);
                String image_url = jsonObject.optString("image_url", null); // image_url 가져오기

                if (nickname != null) {
                    changeName.setText(nickname);
                    Log.d("GetNicknameTask", "Nickname fetched: " + nickname);
                } else {
                    changeName.setText("이름 없음"); // 또는 기본 텍스트로 설정
                    Log.d("GetNicknameTask", "Nickname not found, setting default.");
                }

                // image_url이 유효한 경우 이미지 설정
                if (image_url != null && !image_url.isEmpty()) {
                    Glide.with(profileImage.getContext())
                            .load(image_url) // item_url 대신 image_url 사용
                            .placeholder(R.drawable.baseline_person_24) // 기본 이미지 (로딩 중)
                            .into(profileImage);
                } else {
                    // 이미지 URL이 없으면 기본 이미지 설정
                    profileImage.setImageResource(R.drawable.baseline_person_24); // 기본 이미지 설정
                }
            } catch (JSONException e) {
                Log.e("GetNicknameTask", "Error parsing JSON", e);
                // JSON 파싱 중 에러 발생 시 기본값 설정
                changeName.setText("이름 없음");
                profileImage.setImageResource(R.drawable.baseline_person_24); // 기본 이미지 설정
            }
        }
    }



    // 뒤로 가기 버튼 처리
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
