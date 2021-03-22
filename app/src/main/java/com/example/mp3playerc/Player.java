package com.example.mp3playerc;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Player extends Fragment implements View.OnClickListener{

    private ImageView ivAlbum;
    private TextView tvArtist, tvTitle, tvCurrentTime, tvDuration;
    private SeekBar seekBar;
    private ImageButton ibPlay,ibPrevious, ibNext, ibLike;


    private MainActivity mainActivity;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    private int index;
    private MusicData musicData = new MusicData();
    private ArrayList<MusicData> likeArrayList = new ArrayList<>();
    private MusicAdapter musicAdapter;

    private boolean nowPlaying = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_player_main_view, container, false);

        // 뷰 아이디
        findViewByIdFunc(view);

        // 어댑터 가져옴
        musicAdapter = mainActivity.getMusicAdapter_like();
        // 좋아요 리스트 가져오기
        likeArrayList = mainActivity.getMusicLikeArrayList();

        musicAdapter.setMusicList(likeArrayList);

        seekBarChangeMethod();


        return view;
    }

    private void findViewByIdFunc(View view) {

        ivAlbum = view.findViewById(R.id.ivAlbum);
        tvArtist = view.findViewById(R.id.tvArtist);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        tvDuration = view.findViewById(R.id.tvDuration);
        seekBar = view.findViewById(R.id.seekBar);
        ibPlay = view.findViewById(R.id.ibPlay);
        ibPrevious = view.findViewById(R.id.ibPrevious);
        ibNext = view.findViewById(R.id.ibNext);
        ibLike = view.findViewById(R.id.ibLike);

        ibPlay.setOnClickListener(this);
        ibPrevious.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        ibLike.setOnClickListener(this);
    }

    //버튼 클릭 이벤트처리 함수
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.ibPlay :
                if(nowPlaying == true){
                    mediaPlayer.pause();
                    ibPlay.setImageResource(R.drawable.play);
//                    ibPlay.setActivated(false);
                    nowPlaying = false;
                }else{
                    mediaPlayer.start();
                    ibPlay.setImageResource(R.drawable.pause_button);
                    nowPlaying = true;
                    setSeekBarThread();
                }
                break;
            case R.id.ibPrevious :
                mediaPlayer.stop();
                mediaPlayer.reset();
                try {
                    if(index == 0){
                        index = mainActivity.getMusicDataArrayList().size();
                    }
                    index--;
                    setPlayerData(index, true);

                } catch (Exception e) {
                    Log.d("ubPrevious",e.getMessage());
                }
                break;
            case R.id.ibNext :
                try {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    if(index == mainActivity.getMusicDataArrayList().size()-1){
                        index= -1;
                    }
                    index++;
                    setPlayerData(index, true);

                } catch (Exception e) {
                    Log.d("ibNext",e.getMessage());
                }
                break;
            case R.id.ibLike :

                if(ibLike.isActivated()){
                    ibLike.setActivated(false);
                    ibLike.setImageResource(R.drawable.heart);
                    musicData.setLiked(0);
                    likeArrayList.remove(musicData);
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(mainActivity, "좋아요목록에서 삭제되었습니다", Toast.LENGTH_SHORT).show();

                }else{
                    ibLike.setImageResource(R.drawable.full_heart);
                    ibLike.setActivated(true);
                    musicData.setLiked(1);
                    likeArrayList.add(musicData);
                    musicAdapter.notifyDataSetChanged();
                    Toast.makeText(mainActivity, "좋아요목록에 추가되었습니다", Toast.LENGTH_SHORT).show();
                }

                break;
            default:break;
        }
    }


    //시크바 변경에 관한 함수
    private void seekBarChangeMethod() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // 사용자가 움직였을시, seekbar 이동
                if(b){
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    //시크바 스레드 에 관한 함수
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setSeekBarThread(){
        Thread thread = new Thread(new Runnable() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

            @Override
            public void run() {
                while(mediaPlayer.isPlaying()){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    try {
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvCurrentTime.setText(simpleDateFormat.format(mediaPlayer.getCurrentPosition()));
                            }
                        });

                    }catch (Exception e){
                    }
                    SystemClock.sleep(100);
                }
            }
        });
        thread.start();
    }

    // 플레이어 화면 처리
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setPlayerData(int pos, boolean flag) {
        index = pos;

        mediaPlayer.stop();
        mediaPlayer.reset();

        MusicAdapter musicAdapter = new MusicAdapter(mainActivity);

        if(flag){
            musicData = mainActivity.getMusicDataArrayList().get(pos);

        }else{
            musicData = mainActivity.getMusicLikeArrayList().get(pos);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

        tvTitle.setText(musicData.getTitle());
        tvArtist.setText(musicData.getArtist());
        tvDuration.setText(simpleDateFormat.format(Integer.parseInt(musicData.getDuration())));

        if(musicData.getLiked() == 1){
            ibLike.setActivated(true);
        }else{
            ibLike.setActivated(false);
        }

        // 앨범 이미지 세팅
        Bitmap albumImg = musicAdapter.getAlbumImg(mainActivity, Integer.parseInt(musicData.getAlbumArt()), 200);
        if(albumImg != null){
            ivAlbum.setImageBitmap(albumImg);
        }else{
            ivAlbum.setImageResource(R.drawable.album_default);
        }

        // 음악 재생
        Uri musicURI = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,musicData.getId());
        try {
            mediaPlayer.setDataSource(mainActivity, musicURI);
            mediaPlayer.prepare();
            mediaPlayer.start();
            ibPlay.setImageResource(R.drawable.pause_button);
            nowPlaying = true;
            seekBar.setProgress(0);
            seekBar.setMax(Integer.parseInt(musicData.getDuration()));
//            ibPlay.setActivated(true);

            setSeekBarThread();

            // 재생완료 리스너
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    ibNext.callOnClick();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
