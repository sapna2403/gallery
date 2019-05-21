package com.example.twistmobile.gallery;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int WRITE_REQUEST_PERMISSION_CODE = 101;

    RecyclerView rv;
    ImageGridAdapter adapter;
    TextView txt_deny;
    Button btn_Allow;

    List<String> imagelist = new ArrayList<>();

    private static String TAG = "PermissionDemo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new ImageGridAdapter(this, imagelist);
        txt_deny = (TextView) findViewById(R.id.txt_deny);
        btn_Allow = (Button) findViewById(R.id.btn_allow);

        rv.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
        rv.setAdapter(adapter);

        btn_Allow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePermissionErrorMessage();
                makePermissionRequest();
            }
        });

        hidePermissionErrorMessage();

        if (hasSDCardPermission()) {
            openImagePicker();
        } else {
            makePermissionRequest();
        }
    }

    private boolean hasSDCardPermission() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            ClipData mClipData = data.getClipData();
            for (int i = 0; i < mClipData.getItemCount(); i++) {
                ClipData.Item item = mClipData.getItemAt(i);
                Uri selectedimage = item.getUri();
                String[] filePathColoum = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedimage, filePathColoum, null, null, null);
                while (cursor.moveToNext()) {
                    //cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColoum[0]);
                    String picturePath = cursor.getString(columnIndex);
                    System.out.println("picturepath:" + picturePath);
                    imagelist.add(picturePath);
                }
                cursor.close();
            }
            adapter.notifyDataSetChanged();
        }
    }

    protected void makePermissionRequest() {
        ActivityCompat.requestPermissions(this,
                PERMISSIONS,
                WRITE_REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQUEST_PERMISSION_CODE: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user");
                    showPermissionErrorMessage();
                } else {
                    Log.i(TAG, "Permission has been granted by user");
                    openImagePicker();
                }
                return;
            }
        }
    }

    private void showPermissionErrorMessage() {
        btn_Allow.setVisibility(View.VISIBLE);
        txt_deny.setVisibility(View.VISIBLE);
    }

    private void hidePermissionErrorMessage() {
        btn_Allow.setVisibility(View.GONE);
        txt_deny.setVisibility(View.GONE);
    }
}

