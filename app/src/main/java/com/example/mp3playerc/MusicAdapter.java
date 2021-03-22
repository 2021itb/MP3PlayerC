package com.example.mp3playerc;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

//뮤직어뎁터 필요 -> 안에 들어갈 ArrayList<MusicData>, content, 개수, 화면 레이아웃이 필요, 이벤트 걸 수 있으면 이벤트
//bind 시켜줘야하는 것을 뷰홀더가 필요하다. 뷰홀더: 화면에 있는 레이아웃을 갖고 있다가 매치시켜주는 역할
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.CustomViewHolder>{

    private Context context;
    private ArrayList<MusicData> musicList;

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null;

    public MusicAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item, viewGroup, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder customViewHolder, int position) {

        //앨범 이미지를 비트맵으로 만들기
        Bitmap albumImg = getAlbumImg(context, Integer.parseInt(musicList.get(position).getAlbumArt()), 200);
        if(albumImg != null){
            customViewHolder.albumArt.setImageBitmap(albumImg);
        }

        // recyclerviewer에 보여줘야할 정보 세팅
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        customViewHolder.title.setText(musicList.get(position).getTitle());
        customViewHolder.artist.setText(musicList.get(position).getArtist());
        customViewHolder.duration.setText(simpleDateFormat.format(Integer.parseInt(musicList.get(position).getDuration())));

    }

    @Override
    public int getItemCount() {
        return musicList != null ? musicList.size() : 0;
    }

    // 앨범아트 가져오는 함수
    public Bitmap getAlbumImg(Context context, int albumArt, int imgMaxSize){

        BitmapFactory.Options options = new BitmapFactory.Options();

        /*컨텐트 프로바이더(Content Provider)는 앱 간의 데이터 공유를 위해 사용됨.
        특정 앱이 다른 앱의 데이터를 직접 접근해서 사용할 수 없기 때문에
        무조건 컨텐트 프로바이더를 통해 다른 앱의 데이터를 사용해야만 한다.
        다른 앱의 데이터를 사용하고자 하는 앱에서는 URI를 이용하여 컨텐트 리졸버(Content Resolver)를 통해
        다른 앱의 컨텐트 프로바이더에게 데이터를 요청하게 되는데
        요청받은 컨텐트 프로바이더는 URI를 확인하고 내부에서 데이터를 꺼내어 컨텐트 리졸버에게 전달한다.
        */

        ContentResolver contentResolver = context.getContentResolver();

        // 앨범아트는 uri를 제공하지 않으므로, 별도로 생성.
        Uri uri = Uri.parse("content://media/external/audio/albumart/" + albumArt);
        if (uri != null){
            ParcelFileDescriptor fd = null;
            try{
                fd = contentResolver.openFileDescriptor(uri, "r");

                //true면 비트맵객체에 메모리를 할당하지 않아서 비트맵을 반환하지 않음.
                //다만 options fields는 값이 채워지기 때문에 Load 하려는 이미지의 크기를 포함한 정보들을 얻어올 수 있다.
                options.inJustDecodeBounds = true;

                int scale = 0;
                if(options.outHeight > imgMaxSize || options.outWidth > imgMaxSize){
                    scale = (int)Math.pow(2,(int) Math.round(Math.log(imgMaxSize / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
                }
                options.inJustDecodeBounds = false; // true면 비트맵을 만들지 않고 해당이미지의 가로, 세로, Mime type등의 정보만 가져옴
                options.inSampleSize = scale; // 이미지의 원본사이즈를 설정된 스케일로 축소

                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);

                if(bitmap != null){
                    // 정확하게 사이즈를 맞춤
                    if(options.outWidth != imgMaxSize || options.outHeight != imgMaxSize){
                        Bitmap tmp = Bitmap.createScaledBitmap(bitmap, imgMaxSize, imgMaxSize, true);
                        bitmap.recycle();
                        bitmap = tmp;
                    }
                }
                return bitmap;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    public interface OnItemClickListener
    {
        void onItemClick(View v, int pos);
    }

    // OnItemClickListener 객체 참조를 어댑터에 전달하는 메소드
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.mListener = listener;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView albumArt;
        TextView title;
        TextView artist;
        TextView duration;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);

            this.albumArt = itemView.findViewById(R.id.d_ivAlbum);
            this.title = itemView.findViewById(R.id.d_tvTitle);
            this.artist = itemView.findViewById(R.id.d_tvArtist);
            this.duration = itemView.findViewById(R.id.d_tvDuration);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if( pos != RecyclerView.NO_POSITION){

                        mListener.onItemClick(view,pos);
                    }
                }
            });
        }

    }

    public void setMusicList(ArrayList<MusicData> musicList) {
        this.musicList = musicList;
    }
}