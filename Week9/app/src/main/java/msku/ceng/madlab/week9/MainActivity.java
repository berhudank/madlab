package msku.ceng.madlab.week9;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    EditText txtURL;
    ImageView imgView;
    Button btnDownload;
    private static int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSONS_STORAGE = {android.Manifest.permission.READ_EXTERNAL_STORAGE ,android.Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtURL = findViewById(R.id.txtURL);
        imgView = findViewById(R.id.imgView);
        btnDownload = findViewById(R.id.btnDownload);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permission != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                }
//                String fileName = "temp.jpg";
//                String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + fileName;
//                downloadFile(txtURL.getText().toString(), imagePath);
//                preview(imagePath);
                else{
//                    DownloadTask backgroundTask = new DownloadTask();
//                    String[] urls = new String[1];
//                    urls[0] = txtURL.getText().toString();
//                    backgroundTask.execute(urls);
                    Thread backgroundThread = new Thread(new DownloadRunnable(txtURL.getText().toString()));
                    backgroundThread.start();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
//            String fileName = "temp.jpg";
//            String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + fileName;
//            downloadFile(txtURL.getText().toString(), imagePath);
//            preview(imagePath);
            DownloadTask backgroundTask = new DownloadTask();
            String[] urls = new String[1];
            urls[0] = txtURL.getText().toString();
            backgroundTask.execute(urls);
        }
        else{
            Toast.makeText(this,"External storage permission is not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void preview(String imagePath){
        Bitmap image = BitmapFactory.decodeFile(imagePath);
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        int rescaledWidth = 400;
        int rescaledHeight = (int) ((imageHeight * rescaledWidth) / imageWidth);

        Bitmap bitmap = Bitmap.createScaledBitmap(image, rescaledWidth, rescaledHeight, false);
        imgView.setImageBitmap(bitmap);
    }

    public void downloadFile(String url, String imagePath) {
        try {
            URL strURL = new URL(url);
            URLConnection connection = strURL.openConnection();
            connection.connect();

            InputStream inputStream = new BufferedInputStream(strURL.openStream(), 8192);
            OutputStream outputStream = new FileOutputStream(imagePath);

            byte[] data = new byte[1024];
            int count;

            while ((count = inputStream.read(data)) != -1){
                outputStream.write(data, 0, count);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Bitmap rescaleBitmap(String imagePath){
        Bitmap image = BitmapFactory.decodeFile(imagePath);
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        int rescaledWidth = 400;
        int rescaledHeight = (int) ((imageHeight * rescaledWidth) / imageWidth);

        Bitmap bitmap = Bitmap.createScaledBitmap(image, rescaledWidth, rescaledHeight, false);
        return bitmap;
    }

    class DownloadTask extends AsyncTask<String, Integer, Bitmap>{
        ProgressDialog progressDialog;

        @Override protected void onPreExecute() { super.onPreExecute(); progressDialog = new ProgressDialog(MainActivity.this); progressDialog.setMax(100); progressDialog.setIndeterminate(false); progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); progressDialog.setTitle("Downloading"); progressDialog.setMessage("Please wait.."); progressDialog.show();}

        @Override
        protected Bitmap doInBackground(String... urls) {
            String fileName = "temp.jpg";
            String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            downloadFile(urls[0], imagePath + "/" + fileName);

            return rescaleBitmap(imagePath + "/" + fileName);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imgView.setImageBitmap(bitmap);
            progressDialog.dismiss();
        }

        protected void onProgressUpdate(Integer... progress) { progressDialog.setProgress(progress[0]); }


    }

    class DownloadRunnable implements Runnable{
        String url;

        public DownloadRunnable(String url){
            this.url = url;
        }

        @Override
        public void run() {
            String fileName = "temp.jpg";
            String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
            downloadFile(url, imagePath + "/" + fileName);
            Bitmap bitmap = rescaleBitmap(imagePath + "/" + fileName);

            runOnUiThread(new UpdateBitmap(bitmap));
        }

        class UpdateBitmap implements Runnable{
            Bitmap bitmap;
            public UpdateBitmap(Bitmap bitmap){
                this.bitmap = bitmap;
            }
            @Override
            public void run() {
                imgView.setImageBitmap(bitmap);
            }
        }
    }
}