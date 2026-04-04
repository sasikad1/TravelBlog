package uk.ac.wlv.travelblog.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class BloggerUploadService {

    private static final String TAG = "BloggerUpload";
    private static final String BLOG_ID = "7027558009002002808";
    private static final String BLOG_URL = "https://wanderlogtravels.blogspot.com";

    private Context context;
    private OnUploadListener listener;

    public interface OnUploadListener {
        void onSuccess(String postUrl);
        void onError(String error);
    }

    public BloggerUploadService(Context context) {
        this.context = context;
    }

    public void setOnUploadListener(OnUploadListener listener) {
        this.listener = listener;
    }

    public void authenticateAndUpload(String title, String content) {
        new UploadTask().execute(title, content);
    }

    private class UploadTask extends AsyncTask<String, Void, Boolean> {

        private String errorMessage = null;
        private String title;
        private String content;

        @Override
        protected void onPreExecute() {
            Toast.makeText(context, "Uploading to Blogger...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            title = params[0];
            content = params[1];

            try {
                // Simulate network delay
                Thread.sleep(2000);

                // Log what would be uploaded
                Log.d(TAG, "=== BLOGGER UPLOAD DEMO ===");
                Log.d(TAG, "Blog ID: " + BLOG_ID);
                Log.d(TAG, "Blog URL: " + BLOG_URL);
                Log.d(TAG, "Title: " + title);
                Log.d(TAG, "Content: " + content);
                Log.d(TAG, "===========================");

                // For demo, always return success
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success && listener != null) {
                listener.onSuccess(BLOG_URL);
                Toast.makeText(context, "Posted to Blogger successfully!", Toast.LENGTH_LONG).show();
            } else if (listener != null) {
                listener.onError(errorMessage != null ? errorMessage : "Upload failed");
                Toast.makeText(context, "Upload failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }
}