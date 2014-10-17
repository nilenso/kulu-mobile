package nilenso.com.kulu_mobile;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.readystatesoftware.simpl3r.UploadIterruptedException;
import com.readystatesoftware.simpl3r.Uploader;
import com.readystatesoftware.simpl3r.Uploader.UploadProgressListener;

import java.io.File;
import java.io.IOException;

public class InvoiceUploadService extends IntentService {
    public static final String ARG_FILE_PATH = "file_path";

    public static final String UPLOAD_STATE_CHANGED_ACTION = "com.kulu_mobile.UPLOAD_STATE_CHANGED_ACTION";
    public static final String UPLOAD_CANCELLED_ACTION = "com.kulu_mobile.UPLOAD_CANCELLED_ACTION";
    public static final String UPLOAD_FINISHED_ACTION = "com.kulu_mobile.UPLOAD_FINISHED_ACTION";

    public static final String S3KEY_EXTRA = "com.kulu_mobile.s3key";
    public static final String S3LOCATION_EXTRA = "com.kulu_mobile.s3location";
    public static final String FILEUPLOADED_EXTRA = "com.kulu_mobile.filetoremove";
    public static final String PERCENT_EXTRA = "com.kulu_mobile.percent";
    public static final String MSG_EXTRA = "com.kulu_mobile.msg";

    private AmazonS3Client s3Client;
    private Uploader uploader;

    private NotificationManager nm;
    private static final int NOTIFY_ID_UPLOAD = 4815;

    public InvoiceUploadService() {
        super("invoice-upload-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        s3Client = new AmazonS3Client(
                new BasicAWSCredentials(getString(R.string.kulu_s3_access_key_id),
                        getString(R.string.kulu_s3_secret_access_key)));
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        IntentFilter f = new IntentFilter();
        f.addAction(UPLOAD_CANCELLED_ACTION);
        registerReceiver(uploadCancelReceiver, f);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String filePath = intent.getStringExtra(ARG_FILE_PATH);

        File fileToUpload = new File(filePath);

        final String s3ObjectKey = FileUtils.getLastPartOfFile(filePath);

        String s3BucketName = getString(R.string.kulu_s3_tmp_bucket);

        final String msg = "Uploading " + s3ObjectKey + "...";

        // create a new uploader for this file
        uploader = new Uploader(this, s3Client, s3BucketName, s3ObjectKey, fileToUpload);

        // listen for progress updates and broadcast/notify them appropriately
        uploader.setProgressListener(new UploadProgressListener() {
            @Override
            public void progressChanged(ProgressEvent progressEvent,
                                        long bytesUploaded, int percentUploaded) {
                Notification notification = buildNotification(msg, percentUploaded);
                nm.notify(NOTIFY_ID_UPLOAD, notification);
                broadcastState(s3ObjectKey, percentUploaded, msg);
            }
        });

        // broadcast/notify that our upload is starting
        Notification notification = buildNotification(msg, 0);
        nm.notify(NOTIFY_ID_UPLOAD, notification);
        broadcastState(s3ObjectKey, 0, msg);

        String s3Location = null;

        try {
            s3Location = uploader.start();
        } catch (UploadIterruptedException uie) {
            broadcastState(s3ObjectKey, -1, "Upload was interrupted");
        } catch (Exception e) {
            e.printStackTrace();
            broadcastState(s3ObjectKey, -1, "Error: " + e.getMessage());
        }

        try {
            uploadInvoice(s3Location);
            broadcastState(s3ObjectKey, -1, "File successfully uploaded to " + s3Location);
            nm.notify(NOTIFY_ID_UPLOAD, buildNotification("Upload finished", 100));
            broadcastFinished(s3Location, fileToUpload.toString());
        } catch (IOException e) {
            broadcastState(s3ObjectKey, -1, "Upload couldn't be finished as connection to Kulu Backend failed");
            nm.notify(NOTIFY_ID_UPLOAD + 1, buildNotification("Upload couldn't be finished as the connection to the backend failed.", -1));
        }
    }

    @Override
    public void onDestroy() {
        nm.cancel(NOTIFY_ID_UPLOAD);
        unregisterReceiver(uploadCancelReceiver);

        super.onDestroy();
    }

    private void broadcastState(String s3key, int percent, String msg) {
        Bundle b = new Bundle();
        b.putString(S3KEY_EXTRA, s3key);
        b.putInt(PERCENT_EXTRA, percent);
        b.putString(MSG_EXTRA, msg);

        broadcast(UPLOAD_STATE_CHANGED_ACTION, b);
    }

    private void broadcastFinished(String s3Location, String fileUploaded) {
        Bundle b = new Bundle();
        b.putString(S3LOCATION_EXTRA, s3Location);
        b.putString(FILEUPLOADED_EXTRA, fileUploaded);

        broadcast(UPLOAD_FINISHED_ACTION, b);
    }

    private void broadcast(String intentContent, Bundle b) {
        Intent intent = new Intent(intentContent);
        intent.putExtras(b);
        sendBroadcast(intent);
    }

    private BroadcastReceiver uploadCancelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (uploader != null) {
                uploader.interrupt();
            }
        }
    };

    private Notification buildNotification(String msg, int progress) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setTicker(msg);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setOngoing(true);
        builder.setProgress(100, progress, false);
        builder.setStyle(new Notification.BigTextStyle().bigText(msg));
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(contentIntent);

        return builder.build();
    }

    private void uploadInvoice(String s3Location) throws IOException {
        KuluBackend backend = new KuluBackend();
        backend.createInvoice(getString(R.string.kulu_backend_service_url), s3Location);
    }
}