package ec.edu.ups.appcv;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.Manifest;
import android.os.Environment;

import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.CameraActivity;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.opencv.android.JavaCameraView;

public class MainActivity extends CameraActivity  {
    private JavaCameraView mCameraView;
    private int mCameraId = CameraBridgeViewBase.CAMERA_ID_BACK;
    CameraBridgeViewBase camaraBridgeViewBase;
    private boolean mIsCameraColor;
    //Camara Flip boton
    private ImageView flip_camara;
    /////
 private String list;
    private int mCamaraId=0;
    private TextView mFpsTextView;

    private Bitmap bit;
    private Bitmap bit1;
    private long mFrameCount = 0;
    private long mStartTime = 0;
    private Mat lastFrame;
    private Mat lFrame;
    private Button mSwitchCameraButton;
    //lamado del takebutton
    private  ImageView take_icon;
    private int take_imagen=0;

    private ImageView gallery_icon;

    private ImageView filtro_icon;

    private ListView set_filtro;
    private int res_list=0;
    private String fill;
    Camera mCamera;

    private MediaRecorder recorder;

    private ImageView video_camara;
    private int video_foto=0;
    private int take_video=0;
    private int mHeight=0;
    private int mWidth=0;

    private enum FilterType {
        NONE, GRAYSCALE, HISTOGRAM_EQUALIZATION
    }

    private FilterType currentFilter = FilterType.NONE;
    public static void merge(List<Mat> mv, Mat dst){

    }
    // Used to load the 'appcv' library on application startup.
    static {
        System.loadLibrary("appcv");
    }

    //private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int permisoCamara=0;
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                ==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.RECORD_AUDIO},permisoCamara);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
        ==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA},permisoCamara);
        }
        //
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},permisoCamara);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},permisoCamara);
        }


/////////////////FPSSSSSSSSSSSSSSSSSSSSSS
        setContentView(R.layout.activity_main);
        mFpsTextView = findViewById(R.id.fps_textview);

        //Permiso de la camara
        getPermiso();
        mIsCameraColor = true;


        camaraBridgeViewBase = findViewById(R.id.camaraView);
        flip_camara=findViewById(R.id.flip_camara);
        flip_camara.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    flip_camara.setColorFilter(Color.DKGRAY);
                    return  true;
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    flip_camara.setColorFilter(Color.WHITE);
                    swapCamera();
                    return true;
                }
                return false;
            }
        });



    gallery_icon= findViewById(R.id.gallery_icon);
    gallery_icon.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                gallery_icon.setColorFilter(Color.DKGRAY);
                return  true;
            }
            if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                gallery_icon.setColorFilter(Color.WHITE);
                startActivity(new Intent(MainActivity.this, GalleryActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
            }
            return false;
        }
    });


    ArrayList<String> resoluArray = new ArrayList<>();
    resoluArray.add("Detector");
    resoluArray.add("Escala");
    resoluArray.add("Histograma");
    resoluArray.add("Desenfoque");
    resoluArray.add("Contornos");
    resoluArray.add("MejorContraste");
    resoluArray.add("DetectarColor");
    resoluArray.add("DetectorMovimiento");
    resoluArray.add("DetectorCirculo");
    resoluArray.add("DetectarBorde");


    filtro_icon= findViewById(R.id.filtro_icon);
    set_filtro= findViewById(R.id.set_filtro);

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(this, R.layout.resolution_item,
                R.id.textView, resoluArray);
    String null_array []= {};
ArrayAdapter<String> null_arrayAd=new ArrayAdapter<>(this, R.layout.resolution_item,
        R.id.textView, resoluArray);
///////
 //set_filtro.setAdapter(null_arrayAd);

        filtro_icon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    filtro_icon.setColorFilter(Color.DKGRAY);
                    return true;
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    filtro_icon.setColorFilter(Color.WHITE);
                    if(res_list==0){
                        set_filtro.setAdapter(arrayAdapter);
                        res_list=1;
                    }else{
                        set_filtro.setAdapter(null_arrayAd);
                        res_list=0;
                    }
                    return true;
                }
                return false;
            }
        });



set_filtro.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

       String filt =  resoluArray.get(i);

       MainActivity.this.fill = filt;

        set_filtro.setAdapter(null_arrayAd);
        res_list=0;
    }
});
        recorder=new MediaRecorder();
        video_camara= findViewById(R.id.video_camara);
        video_camara.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                video_camara.setColorFilter(Color.DKGRAY);
                return true;
            }
            if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                video_camara.setColorFilter(Color.WHITE);
                if(video_foto==0){
                    //Video
                    take_icon.setImageResource(R.drawable.circulo_icon);
                    take_icon.setColorFilter(Color.WHITE);
                    video_foto=1;
                }else {
                    //Fotos
                    take_icon.setImageResource(R.drawable.take_icon);
                    video_foto=0;
                }
                return true;
            }

                return false;
            }
        });

        take_icon= findViewById(R.id.take_icon);
        take_icon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    if(video_foto==0){

                        if(take_imagen==0){
                            take_icon.setColorFilter(Color.DKGRAY);
                        }

                    }
                    return  true;
                }
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                   if(video_foto==1){
                        if (take_video==0){
                            try {
                                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath()+"/ImagePro");

                                boolean success=true;
                                if(!folder.exists()){
                                    success=folder.mkdir();
                                }
                                take_icon.setImageResource(R.drawable.circulo_icon);
                                take_icon.setColorFilter(Color.RED);
                                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
                                CamcorderProfile camcorderProfile= CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                                recorder.setProfile(camcorderProfile);

                                SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                                String current_Date= sdf.format(new Date());
                                String filename= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath()+"/ImagePro"+current_Date+".mp4";
                                recorder.setOutputFile(filename);
                                recorder.setVideoSize(mHeight,mWidth);

                                recorder.prepare();
                                camaraBridgeViewBase.setRecorder(recorder);
                                recorder.start();
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                            take_video=1;
                        }else{
                            take_icon.setImageResource(R.drawable.circulo_icon);
                            take_icon.setColorFilter(Color.WHITE);
                            camaraBridgeViewBase.setRecorder(null);
                            recorder.stop();
                            //1 segundo
                            try {
                                Thread.sleep(1000);
                            }catch (InterruptedException e){
                                throw new RuntimeException(e);
                            }
                            take_video=0;
                       }
                   }else {
                       take_icon.setColorFilter(Color.WHITE);
                       if(take_imagen==0){
                           take_imagen=1;
                       }else{
                           take_imagen=0;
                       }
                   }
                    return true;
                }
                return false;
            }
        });




        camaraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {




            @Override
            public void onCameraViewStarted(int width, int height) {
            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                lastFrame = inputFrame.rgba();
                lFrame = inputFrame.gray();
                if (mCameraId==1) {
                    Core.flip(lastFrame, lastFrame,-1);
                    Core.flip(lFrame, lFrame,-1);
                    //intentado metodo c++

                }

                Mat clon = new Mat();
                bit= Bitmap.createBitmap(lastFrame.cols(),lastFrame.rows(),Bitmap.Config.ARGB_8888);
                bit1= Bitmap.createBitmap(lastFrame.cols(),lastFrame.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(lastFrame,bit);
                    if(fill=="Escala"){
                        aEscalaGrises(bit, bit1);
                        Utils.bitmapToMat(bit1,clon);
                    lastFrame = clon.clone();
                }else if (fill=="Histograma"){
                aHistogramaE(bit,bit1);
                Utils.bitmapToMat(bit1,clon);
                lastFrame =clon.clone();
                }else if (fill=="Dectector"){
                    Dectector(bit,bit1);
                    Utils.bitmapToMat(bit1,clon);
                    lastFrame =clon.clone();
                }else if (fill=="Desenfoque"){
                    aplicarDesenfoque(bit,bit1);
                    Utils.bitmapToMat(bit1,clon);
                    lastFrame =clon.clone();
                }else if (fill=="Contornos"){
                    detectarContornos(bit,bit1);
                    Utils.bitmapToMat(bit1,clon);
                    lastFrame =clon.clone();
                }else if (fill=="MejorContraste"){
                    mejorarContraste(bit,bit1);
                    Utils.bitmapToMat(bit1,clon);
                    lastFrame =clon.clone();
                }else if (fill=="DetectarColor"){
                    detectarColor(bit,bit1);
                    Utils.bitmapToMat(bit1,clon);
                    lastFrame =clon.clone();
                }else if (fill=="DetectorMovimiento"){
                    detectarMovimiento(bit,bit1);
                    Utils.bitmapToMat(bit1,clon);
                    lastFrame =clon.clone();
                }else if (fill=="DetectorCirculo"){
                    detectarCirculo(bit,bit1);
                    Utils.bitmapToMat(bit1,clon);
                    lastFrame =clon.clone();
                }else if (fill=="DetectarBorde"){
                    detectarBordesLaplaciano(bit,bit1);
                    Utils.bitmapToMat(bit1,clon);
                    lastFrame =clon.clone();
                }

                take_imagen= take_icon_funcion_gray(take_imagen,lastFrame);

                mHeight=lastFrame.height();
                mWidth=lastFrame.width();

                // Calcular FPS
                calculateFPS();
                return lastFrame;
            }
        });

        if(OpenCVLoader.initDebug()){
            camaraBridgeViewBase.enableView();
        }
    }

    private int take_icon_funcion_rgb(int take_imagen, Mat lastFrame){
      if(take_imagen==1){
          Mat save_mat= new Mat();

          Core.flip(lastFrame.t(),save_mat,1);

          Imgproc.cvtColor(save_mat,save_mat,Imgproc.COLOR_RGBA2BGRA);

          File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath()+"/ImagePro");

          boolean success=true;
          if(!folder.exists()){
              success=folder.mkdir();
          }

          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
          String diaActual= sdf.format(new Date());
          String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath()+"/ImagePro/"+ diaActual+".jpg";

          Imgcodecs.imwrite(fileName,save_mat);
          take_imagen=0;
      }


        return take_imagen;
    }

    private int take_icon_funcion_gray(int take_imagen, Mat lFrame){
        if(take_imagen==1){
            Mat save_mat= new Mat();

            Core.flip(lFrame.t(),save_mat,1);



            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath()+"/ImagePro");

            boolean success=true;
            if(!folder.exists()){
                success=folder.mkdir();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String diaActual= sdf.format(new Date());
            String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath()+"/ImagePro/"+ diaActual+".jpg";

            Imgcodecs.imwrite(fileName,save_mat);
            take_imagen=0;
        }


        return take_imagen;
    }

    private void swapCamera() {
        mCameraId= mCameraId^1;

        camaraBridgeViewBase.disableView();
        camaraBridgeViewBase.setCameraIndex(mCameraId);
        camaraBridgeViewBase.enableView();



    }


    @Override
    protected void onResume() {
        super.onResume();
        camaraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camaraBridgeViewBase.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camaraBridgeViewBase.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(camaraBridgeViewBase);
    }

    /**
     * A native method that is implemented by the 'appcv' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();


    void  getPermiso(){
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            getPermiso();
        }
    }
    public void onDetectarMovimientoClick(View view) {
        mIsCameraColor = !mIsCameraColor;
    }


    private void switchCamera() {
        mCameraId = (mCameraId == CameraBridgeViewBase.CAMERA_ID_BACK) ?
                CameraBridgeViewBase.CAMERA_ID_FRONT :
                CameraBridgeViewBase.CAMERA_ID_BACK;
        mCameraView.disableView();
        mCameraView.setCameraIndex(mCameraId);
        mCameraView.enableView();
    }

    private Mat applyHistogramEqualization(Mat inputFrame) {
        Mat equalizedFrame = new Mat();
        Mat grayFrame = new Mat();

        // Convertir la imagen a escala de grises
        Imgproc.cvtColor(inputFrame, grayFrame, Imgproc.COLOR_RGBA2GRAY);

        // Aplicar ecualización de histograma
        Imgproc.equalizeHist(grayFrame, equalizedFrame);

        // Convertir la imagen de vuelta a RGB si es necesario
        if (mIsCameraColor) {
            Imgproc.cvtColor(equalizedFrame, equalizedFrame, Imgproc.COLOR_GRAY2RGBA);
        }

        return equalizedFrame;
    }
    private Mat applyCLAHE(Mat inputFrame) {
        Mat claheFrame = new Mat();
        Mat grayFrame = new Mat();

        // Convertir la imagen a escala de grises
        Imgproc.cvtColor(inputFrame, grayFrame, Imgproc.COLOR_RGBA2GRAY);

        // Aplicar CLAHE
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(4.0);
        clahe.apply(grayFrame, claheFrame);

        // Convertir la imagen de vuelta a RGB si es necesario
        if (mIsCameraColor) {
            Imgproc.cvtColor(claheFrame, claheFrame, Imgproc.COLOR_GRAY2RGBA);
        }

        return claheFrame;
    }

    public static Mat enhanceContrast(Mat inputImage) {
        // Convertir la imagen a escala de grises
        Mat grayImage = new Mat();
        Imgproc.cvtColor(inputImage, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Calcular la media y desviación estándar locales
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stdDev = new MatOfDouble();
        Core.meanStdDev(grayImage, mean, stdDev);

        double meanValue = mean.get(0, 0)[0];
        double stdDevValue = stdDev.get(0, 0)[0];

        // Ajustar el contraste adaptativamente
        double alpha = 1.5; // factor de ajuste de contraste
        double beta = 0.5 * stdDevValue / meanValue; // factor de ajuste de brillo
        Mat enhancedImage = new Mat();
        grayImage.convertTo(enhancedImage, CvType.CV_8UC1, alpha, beta);

        // Convertir la imagen de vuelta a color si es necesario
        Mat outputImage;
        if (inputImage.channels() == 1) {
            outputImage = enhancedImage;
        } else {
            outputImage = new Mat();
            Imgproc.cvtColor(enhancedImage, outputImage, Imgproc.COLOR_GRAY2BGR);
        }

        return outputImage;
    }
///Con c++ native=lib met1
    private Mat detectContours(Mat inputFrame) {
        Mat grayFrame = new Mat();
        Mat contoursFrame = new Mat();

        // Convertir la imagen a escala de grises
        Imgproc.cvtColor(inputFrame, grayFrame, Imgproc.COLOR_RGBA2GRAY);

        // Aplicar umbral binario
        Imgproc.threshold(grayFrame, grayFrame, 100, 255, Imgproc.THRESH_BINARY);

        // Encontrar contornos
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(grayFrame, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Dibujar contornos en la imagen original
        Imgproc.drawContours(inputFrame, contours, -1, new Scalar(0, 255, 0), 3);

        return inputFrame;
    }

    private void calculateFPS() {
        if (mStartTime == 0) {
            mStartTime = System.currentTimeMillis();
        }

        mFrameCount++;
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - mStartTime;

        if (elapsedTime >= 1000) {
            double fps = mFrameCount / (elapsedTime / 1000.0);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //FPSSSSSSSSSSSSSSSSSSs
                    TextView fpsTextView = findViewById(R.id.fps_textview);
                    fpsTextView.setText(String.format("FPS: %.2f", fps));
                }
            });

            mFrameCount = 0;
            mStartTime = currentTime;
        }
    }

    public native void aEscalaGrises(Bitmap bit, Bitmap bit1);

    public native void aHistogramaE(Bitmap bit, Bitmap bit1);

    public native void aplicarDesenfoque(Bitmap bit, Bitmap bit1);

    public native void Dectector(Bitmap addGray, Bitmap addRGBA);
    public native void detectarContornos(Bitmap addGray, Bitmap addRGBA);

    public native void mejorarContraste(Bitmap addGray, Bitmap addRGBA);

    public native void detectarColor(Bitmap addGray, Bitmap addRGBA);

    public native void detectarMovimiento(Bitmap addGray, Bitmap addRGBA);

    public native void detectarCirculo(Bitmap addGray, Bitmap addRGBA);

    public native void detectarBordesLaplaciano(Bitmap addGray, Bitmap addRGBA);

    //public native void detectarRostros(Bitmap addGray, Bitmap addRGBA);


    }


