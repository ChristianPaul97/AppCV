package ec.edu.ups.appcv;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Objects;


public class imageClassification {

    private Interpreter interpreter;
    private int INPUT_SIZE;
    private int PIXEL_SIZE=3;
    private int IMAGE_MEAN=0;
    private float IMAGE_STD=255.0f;

    //Inicializo GPU interpreter

    private GpuDelegate gpuDelegate;
    private int height=0;
    private int width=0;

    //Definir 2 colores red y green
    private Scalar red = new Scalar(255,0,0,50);
    private  Scalar green = new Scalar(0,255,0,50);

    imageClassification(AssetManager assetManager, String modelPath, int inputSize) throws IOException{
        INPUT_SIZE=inputSize;

        Interpreter.Options options=new Interpreter.Options();
        //GPU .........
       // GpuDelegate gpuDelegate= new GpuDelegate();
        // options.addDelegate(gpuDelegate);
        options.setNumThreads(6);
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath), options);
         }

         public Mat recognizeImage(Mat mat_image){

              Mat rotate_mat_image=new Mat();
             Core.flip(mat_image.t(),rotate_mat_image,1);

             //define height y width
             height=rotate_mat_image.height();
             width=rotate_mat_image.width();

             //Now we will draw a rectangle of size (400,400)
             Rect roi_cropped = new Rect((width-400)/2, (height-400)/2,400,400);

             Mat cropped_image=new Mat(rotate_mat_image,roi_cropped);


             //Convert Mat image to bitmat image
             Bitmap bitmap=null;
             bitmap=Bitmap.createBitmap(cropped_image.cols(),cropped_image.rows(),Bitmap.Config.ARGB_8888);
             Utils.matToBitmap(cropped_image,bitmap);



             //resize bitmap image
             Bitmap scaledBitmap= Bitmap.createScaledBitmap(bitmap, INPUT_SIZE,INPUT_SIZE, false);


             ByteBuffer byteBuffer= convertBitmapToByteBuffer(scaledBitmap);
                //creamos el input y output INTERPRETER

             float[][] output= new float[1][1];
             Object[] out = new Object[1];
             out[0]=output;

            Object[] input = new Object[1];
            input[0]= byteBuffer;


            //now pass int through interpreter
             interpreter.run(byteBuffer,output);

             Log.d("imageClassification", "Out"+ Arrays.deepToString(output));

             //select
            float val_prediction= (float) Array.get(Array.get(output,0),0);

            if (val_prediction>0.4){
                Imgproc.putText(rotate_mat_image,"Tiene Cancer",new Point(((width-400)/2+30), 80),3,1,red,2);

                Imgproc.rectangle(rotate_mat_image,new Point((width-400)/2,(height-400)/2),new Point((width+400)/2,(height+400)/2),red,2);
            }
            else {
                Imgproc.putText(rotate_mat_image,"No tiene cancer",new Point(((width-400)/2+30), 80),3,1,green,2);

                Imgproc.rectangle(rotate_mat_image,new Point((width-400)/2,(height-400)/2),new Point((width+400)/2,(height+400)/2),green,2);
            }



                //Rotate image by -90 degree
             Core.flip(rotate_mat_image.t(),rotate_mat_image,0);
            return rotate_mat_image;
         }

         private ByteBuffer convertBitmapToByteBuffer(Bitmap scaledBitmap){
            ByteBuffer byteBuffer;
             byteBuffer= ByteBuffer.allocateDirect(4*INPUT_SIZE*INPUT_SIZE*PIXEL_SIZE);
             byteBuffer.order(ByteOrder.nativeOrder());
             int[] intValues = new int [INPUT_SIZE*INPUT_SIZE];

             scaledBitmap.getPixels(intValues, 0, scaledBitmap.getWidth(),0,0,scaledBitmap.getWidth(),scaledBitmap.getHeight());

             int pixel= 0;
             for (int i=0;i<INPUT_SIZE;++i){
                 for (int j=0;j<INPUT_SIZE;++j){
                     final int val= intValues[pixel++];

                     byteBuffer.putFloat((((val>>16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                     byteBuffer.putFloat((((val>>8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                     byteBuffer.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                 }
             }
             return byteBuffer;
         }

        private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException{
            AssetFileDescriptor assetFileDescriptor= assetManager.openFd(modelPath);
            FileInputStream inputStream= new FileInputStream(assetFileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffSet = assetFileDescriptor.getStartOffset();
            long declaredLength = assetFileDescriptor.getLength();

            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffSet, declaredLength);
    }


}
