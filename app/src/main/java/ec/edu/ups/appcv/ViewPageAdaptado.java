package ec.edu.ups.appcv;


import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class ViewPageAdaptado extends PagerAdapter {

    Context context;

    ArrayList<String> imagenes = new ArrayList<>();

    LayoutInflater mlayoutInfl;
    private VideoView videoView;
    private ImageView control_btn;
    private int playOrNot=0;
            public ViewPageAdaptado(Context context, ArrayList imagenes){

            this.context= context;
            this.imagenes = imagenes;

            mlayoutInfl= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            }


    @Override
    public int getCount() {
        return imagenes.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view== ((FrameLayout) object);
    }

    @Override
    public void  destroyItem( ViewGroup container, int position,  Object object) {
       container.removeView((FrameLayout) object) ;
    }


    @NonNull
    @Override
    public Object  instantiateItem(@NonNull ViewGroup container, int position) {
        Uri imgUri = Uri.parse("file://"+imagenes.get(position));

        Log.w("ViewPageAdaptado", "Out:"+ imgUri.getPath());

        String[] arrStr=imagenes.get(position).split(".j", 2);

        if(arrStr.length==2){
            //creamos el layout
            View itemView = mlayoutInfl.inflate(R.layout.item, container, false );
            ImageView image_page=(ImageView) itemView.findViewById(R.id.image_page);
//posicion index in imageArray

            image_page.setImageURI(imgUri);
            Objects.requireNonNull(container).addView(itemView);

            return  itemView;
        }else {
            View video_itemV= mlayoutInfl.inflate(R.layout.video_item,container,false);
            videoView= video_itemV.findViewById(R.id.videoview);
            control_btn = video_itemV.findViewById(R.id.control_boton);

            MediaController mediaController= new MediaController(this.context);
            mediaController.setAnchorView(videoView);


            videoView.setVideoURI(imgUri);
            videoView.requestFocus();

            control_btn.setImageResource(R.drawable.play_boton);
            videoView.start();
            playOrNot=1;


            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    control_btn.setImageResource(R.drawable.replay_boton);
                    playOrNot=0;
                }
            });
            control_btn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                        control_btn.setColorFilter(Color.DKGRAY);
                        return true;
                    }
                    if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                        control_btn.setColorFilter(Color.WHITE);
                        if (playOrNot==0){

                            control_btn.setImageResource(R.drawable.pause_boton);
                            playOrNot=1;
                            videoView.start();
                        }else {
                            control_btn.setImageResource(R.drawable.play_boton);
                            playOrNot=0;
                            videoView.pause();
                        }
                    }
                    return false;
                }
            });
            Objects.requireNonNull(container).addView(video_itemV);
            return video_itemV;
        }
    }
}
