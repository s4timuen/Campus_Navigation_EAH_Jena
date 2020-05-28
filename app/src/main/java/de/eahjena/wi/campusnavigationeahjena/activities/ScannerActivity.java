package de.eahjena.wi.campusnavigationeahjena.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private static final String TAG = "ScannerActivity"; //$NON-NLS

    //Permission codes
    private static final int REQUEST_CODE_CAMERA = 123;

    //Variables
    private ZXingScannerView mScannerView;
    private boolean mAutoFocus = true;
    private String destinationQRCode;
    String ownLocation;
    boolean skipScanner;
    ArrayList<String> availableRooms;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get extra from parent
        Intent intendScannerActivity = getIntent();
        destinationQRCode = intendScannerActivity.getStringExtra("destinationQRCode");
        ownLocation = intendScannerActivity.getStringExtra("ownLocation");
        skipScanner = intendScannerActivity.getBooleanExtra("skipScanner", false);
        availableRooms = intendScannerActivity.getStringArrayListExtra("availableRooms");

        if (skipScanner) {
            try {
                Intent intentNavigationActivity = new Intent(getApplicationContext(), NavigationActivity.class);
                intentNavigationActivity.putExtra("ownLocation", ownLocation);
                intentNavigationActivity.putExtra("destinationQRCode", destinationQRCode);
                startActivity(intentNavigationActivity);
            } catch (Exception e) {
                Log.e(TAG + " intend exception", String.valueOf(e));
            }
        }
        if (!skipScanner) {
            mScannerView = new ZXingScannerView(this);
            setContentView(mScannerView);
            mScannerView.setAutoFocus(mAutoFocus);

            //Check necessary permissions
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
            }
        }
    }

    @Override
    public void handleResult(Result rawResult) {

        ownLocation = rawResult.getText();
        ArrayList<String> availableRoomsQRCodes = new ArrayList<>();

        try {
            //Get list of all valid qr-codes
            for (int index = 0; index < availableRooms.size(); index++) {
                String roomToQRCode = availableRooms.get(index).replaceAll("\\.", "");
                availableRoomsQRCodes.add(roomToQRCode);
            }

            //Check if QR-Code is valid and intent
            if (availableRoomsQRCodes.contains(ownLocation)) {
                Intent intentNavigationActivity = new Intent(getApplicationContext(), NavigationActivity.class);
                intentNavigationActivity.putExtra("ownLocation", ownLocation);
                intentNavigationActivity.putExtra("destinationQRCode", destinationQRCode);
                startActivity(intentNavigationActivity);
            }
            if(!availableRoomsQRCodes.contains(ownLocation)) {
                mScannerView.stopCameraPreview();
                mScannerView.stopCamera();
                mScannerView.startCamera();
                mScannerView.resumeCameraPreview(this);
            }
        } catch (Exception e) {
            Log.e(TAG + " intend exception", String.valueOf(e));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!skipScanner) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                mScannerView.setResultHandler(this);
                mScannerView.startCamera();
                mScannerView.setAutoFocus(mAutoFocus);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!skipScanner) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                mScannerView.stopCamera();
            }
        }
    }
}
