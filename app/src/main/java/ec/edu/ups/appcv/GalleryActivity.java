package ec.edu.ups.appcv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

        ViewPager viewPager;
        ArrayList<String> filePath= new ArrayList<>();
    //importamos ViewPageAdaptado
    ViewPageAdaptado viewPageAdaptado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/ImagePro");

        createFileArray(folder);

        viewPager=(ViewPager) findViewById(R.id.viewPagerMain);
        viewPageAdaptado = new ViewPageAdaptado(GalleryActivity.this, filePath);

        viewPager.setAdapter(viewPageAdaptado);

    }

    private void createFileArray(File folder){

        File listFile [] = folder.listFiles();

        if(listFile !=null){
            for (int i= 0; i<listFile.length;i++){
                filePath.add(listFile[i].getAbsolutePath());
            }
        }



    }


}