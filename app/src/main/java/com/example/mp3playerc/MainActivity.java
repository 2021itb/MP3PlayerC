package com.example.mp3playerc;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MusicAdapter.OnItemClickListener{

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerViewLeft;
    private RecyclerView recyclerViewRight;

    private LinearLayoutManager linearLayoutManager;
    private LinearLayoutManager linearLayoutManagerLike;
    private MusicAdapter musicAdapter;
    private MusicAdapter musicAdapterLike;

    private Button btnCloseLeft, btnCloseRight;

    private MusicDBHelper musicDBHelper;


    private ArrayList<MusicData> musicDataArrayList = new ArrayList<>();

    private ArrayList<MusicData> musicLikeArrayList = new ArrayList<>();


    private Fragment player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //sd카드의 외부접근권한 설정
        requestPermissionsFunc();

        //싱글톤방식의 musicDBHelper 객체 화면에 가져오기
        musicDBHelper = MusicDBHelper.getInstance(getApplicationContext());

        //아이디 찾기 함수
        findViewByIdFunc();

        // 어댑터 생성
        musicAdapter = new MusicAdapter(getApplicationContext());
        musicAdapterLike = new MusicAdapter(getApplicationContext());

        // 리니어레이아웃 매니저
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManagerLike = new LinearLayoutManager(getApplicationContext());

        // 왼쪽 플레이리스트 recyclerView에 어댑터와 매니저 세팅
        recyclerViewLeft.setAdapter(musicAdapter);
        recyclerViewLeft.setLayoutManager(linearLayoutManager);

        //오른쪽 좋아요 recyclerView에 어뎁터와 매니저 세팅
        recyclerViewRight.setAdapter(musicAdapterLike);
        recyclerViewRight.setLayoutManager(linearLayoutManagerLike);

        // 플레이리스트 가져오기
        musicDataArrayList = musicDBHelper.compareArrayList();

        // 플레이리스트 DB에 삽입
        insertDB(musicDataArrayList);

        // 어댑터에 데이터 세팅
        recyclerViewListUpdate(musicDataArrayList);
        likeRecyclerViewListUpdate(getLikeList());

        // 프래그먼트 지정
        replaceFrag();

        // recyclerview 클릭 이벤트
        musicAdapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(View v, int pos) {
                // 플레이어 화면 처리
                ((Player)player).setPlayerData(pos,true);
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        // recyclerviewLike 클릭 이벤트
        musicAdapterLike.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(View v, int pos) {
                // 플레이어 화면 처리
                ((Player)player).setPlayerData(pos,false);
                drawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });

        //왼쪽 플레이리스트 닫기 버튼 이벤트
        btnCloseLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        //오른쪽 좋아요리스트 닫기 버튼 이벤트
        btnCloseRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.closeDrawer(Gravity.RIGHT);
            }
        });


    }

    //sdcard에 대한 외부접근권한 설정
    private void requestPermissionsFunc() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MODE_PRIVATE);
    }

    // View 아이디 연결
    private void findViewByIdFunc() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        recyclerViewLeft = (RecyclerView) findViewById(R.id.recyclerViewLeft);
        recyclerViewRight = (RecyclerView) findViewById(R.id.recyclerViewRight);
        btnCloseLeft = (Button) findViewById(R.id.btnCloseLeft);
        btnCloseRight = (Button) findViewById(R.id.btnCloseRight);

    }


    // DB에 mp3 삽입
    private void insertDB(ArrayList<MusicData> arrayList){

        boolean returnValue = musicDBHelper.insertMusicDataToDB(arrayList);

        if(returnValue){
            Toast.makeText(getApplicationContext(), "삽입 성공", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "삽입 실패", Toast.LENGTH_SHORT).show();
        }

    }

    // 좋아요 리스트 가져오기
    private ArrayList<MusicData> getLikeList(){

        musicLikeArrayList = musicDBHelper.saveLikeList();

        if(musicLikeArrayList.isEmpty()){
            Toast.makeText(getApplicationContext(), "가져오기 실패", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "가져오기 성공", Toast.LENGTH_SHORT).show();
        }

        return musicLikeArrayList;
    }


    // 어댑터에 데이터 세팅
    private void recyclerViewListUpdate(ArrayList<MusicData> arrayList){

        // 어댑터에 데이터리스트 세팅
        musicAdapter.setMusicList(arrayList);

        // recyclerViewLeft에 어댑터 세팅
        recyclerViewLeft.setAdapter(musicAdapter);
        musicAdapter.notifyDataSetChanged();
    }

    // like 어댑터 데이터 세팅
    private void likeRecyclerViewListUpdate(ArrayList<MusicData> arrayList){

        // 어댑터에 데이터리스트 세팅
        musicAdapterLike.setMusicList(arrayList);

        // recyclerViewRight에 어댑터 세팅
        recyclerViewRight.setAdapter(musicAdapterLike);
        musicAdapterLike.notifyDataSetChanged();
    }

    // 프래그먼트 지정
    private void replaceFrag() {
        player = new Player();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.replace(R.id.frameLayout, player);
        ft.commit();
    }

    @Override
    public void onItemClick(View v, int pos) {}

    public ArrayList<MusicData> getMusicDataArrayList() {
        return musicDataArrayList;
    }

    public MusicAdapter getMusicAdapter_like() {
        return musicAdapterLike;
    }

    public ArrayList<MusicData> getMusicLikeArrayList() {
        return musicLikeArrayList;
    }

    @Override
    protected void onStop() {
        super.onStop();

        boolean returnValue = musicDBHelper.updateMusicDataToDB(musicLikeArrayList);
//        boolean returnValue2 = musicDBHelper.updateMusicDataToDB(musicLikeArrayList);

        if(returnValue){
            Toast.makeText(getApplicationContext(), "업데이트 성공", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "업데이트 실패", Toast.LENGTH_SHORT).show();
        }
    }
}