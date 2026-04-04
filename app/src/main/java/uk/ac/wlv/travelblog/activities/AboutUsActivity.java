package uk.ac.wlv.travelblog.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import uk.ac.wlv.travelblog.R;

public class AboutUsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvDeveloperName, tvStudentId, tvCourseCode, tvCourseInfo, tvThanks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupClickListeners();
        setData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvDeveloperName = findViewById(R.id.tvDeveloperName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvCourseCode = findViewById(R.id.tvCourseCode);
        tvCourseInfo = findViewById(R.id.tvCourseInfo);
        tvThanks = findViewById(R.id.tvThanks);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setData() {
        tvDeveloperName.setText("K A S Dilum");
        tvStudentId.setText("Student ID: 2587711");
        tvCourseCode.setText("Course: 6CS027");
        tvCourseInfo.setText("6CS027 - Secure Mobile Application Development\nUniversity of Wolverhampton");
        tvThanks.setText("Special thanks to our lecturer Praveen Chandramenon\nfor guidance and support throughout this project.\n\nThank you for using WanderLog! 🌍✈️");
    }
}