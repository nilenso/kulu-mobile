package nilenso.com.kulu_mobile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity {
    private ImageView newClickedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button uploadInvoice = (Button) findViewById(R.id.new_upload_button);
        newClickedImage = (ImageView) findViewById(R.id.uploadedImage);

        uploadInvoice.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = getOutputImage();
            } catch (IOException ex) {
                Log.w("IOException", "Problem in saving the file" + ex.getMessage());
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile.getPath());
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    private File getOutputImage() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) +
                File.separator +
                "IMG_" + timeStamp + ".jpg");

       return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(),
                        "New image added.", Toast.LENGTH_SHORT)
                        .show();

                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                newClickedImage.setImageBitmap(imageBitmap);

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "No new images added.", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Failed to capture image.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}