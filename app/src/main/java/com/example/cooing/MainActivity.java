package com.example.cooing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    private Fragment mainFragment;
    private Fragment mapFragment;
    private Fragment questionFragment;
    private Fragment avatarFragment;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 프래그먼트 매니저와 각 프래그먼트 초기화
        fragmentManager = getSupportFragmentManager();
        mainFragment = new MainFragment();
        mapFragment = new MapFragment();
        questionFragment = new QuestionFragment();
        avatarFragment = new AvatarFragment();

        // DrawerLayout과 네비게이션 뷰 초기화
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // 헤더 레이아웃 설정 (사용자 이름과 아이콘 설정)
        View headerView = navigationView.getHeaderView(0);  // 헤더의 첫 번째 항목 가져오기
        TextView profileName = headerView.findViewById(R.id.profile_name);
        ImageView profileImage = headerView.findViewById(R.id.profile_image);

        // 프로필 섹션에 사용자 이름과 이미지 설정
        profileName.setText("UserName");
        profileImage.setImageResource(R.drawable.baseline_person_24);

        // 앱이 처음 시작될 때 홈 프래그먼트 표시
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.menu_frame_layout, mainFragment)
                    .commitAllowingStateLoss();
        }

        // BottomNavigationView 설정
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_bottom);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                // 각 메뉴 아이템에 맞는 프래그먼트 표시
                if (item.getItemId() == R.id.nav_home) {
                    fragmentTransaction.replace(R.id.menu_frame_layout, mainFragment)
                            .commitAllowingStateLoss();
                } else if (item.getItemId() == R.id.nav_map) {
                    fragmentTransaction.replace(R.id.menu_frame_layout, mapFragment)
                            .commitAllowingStateLoss();
                } else if (item.getItemId() == R.id.nav_question) {
                    fragmentTransaction.replace(R.id.menu_frame_layout, questionFragment)
                            .commitAllowingStateLoss();
                } else if (item.getItemId() == R.id.nav_avatar) {
                    fragmentTransaction.replace(R.id.menu_frame_layout, avatarFragment)
                            .commitAllowingStateLoss();
                }
                return true;
            }
        });

        // NavigationView 설정 (사이드 메뉴)
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // 사이드 메뉴에서 선택한 항목에 맞는 프래그먼트 설정
                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = mainFragment;
                } else if (item.getItemId() == R.id.nav_map) {
                    selectedFragment = mapFragment;
                } else if (item.getItemId() == R.id.nav_question) {
                    selectedFragment = questionFragment;
                } else if (item.getItemId() == R.id.nav_avatar) {
                    selectedFragment = avatarFragment;
                }

                // 선택된 프래그먼트로 화면 교체
                if (selectedFragment != null) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.menu_frame_layout, selectedFragment)
                            .commitAllowingStateLoss();
                    drawerLayout.closeDrawer(GravityCompat.START); // 메뉴 닫기
                }
                return true;
            }
        });

        // 툴바 설정
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.baseline_menu_24); // 메뉴 아이콘 설정

        // 툴바에서 네비게이션 아이콘 클릭 처리
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START); // 메뉴 열려 있을 때 닫기
            } else {
                drawerLayout.openDrawer(GravityCompat.START); // 메뉴 닫혀 있을 때 열기
            }
        });

        // 터치 이벤트로 메뉴 닫기 (옵션)
        findViewById(R.id.drawer_layout).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START); // 메뉴 닫기
                    return true;
                }
            }
            return false;
        });
    }

    // 네비게이션 버튼 클릭 이벤트 처리
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START); // 네비게이션 메뉴 닫기
            } else {
                drawerLayout.openDrawer(GravityCompat.START); // 네비게이션 메뉴 열기
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
