package com.dedikurniawan.shc.enclouds;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.nbsp.materialfilepicker.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    Button btnEnkrip, btnDekrip, btnFile, btnUpload, btnExit;


    public EditText editInput;
    public EditText editKey;


    String filepath;

    private GoogleApiClient mGoogleApiClient;
    private DriveId mFileId;
    public DriveFile mFile;


    private String Folder_Name = "Enclouds";

    private static final String TAG = "drive-quickstart";
    private static final int REQUEST_CODE_RESOLUTION = 3;


   private static final String salt = "t784";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        }
        editInput = (EditText) findViewById(R.id.input);
        editKey = (EditText)findViewById(R.id.inputPassword);

        editInput.setEnabled(false);

        btnUpload = (Button)findViewById(R.id.btnUpload);
        btnDekrip = (Button) findViewById(R.id.btnDecrypt);
        btnEnkrip = (Button) findViewById(R.id.btnEncrypt);
        btnFile = (Button) findViewById(R.id.browseFile);
        btnExit = (Button) findViewById(R.id.btnExit);



        btnEnkrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    if(editKey.getText().toString().isEmpty()){
                        Notif();
                    }else {

                        encryptedfile(editInput.getText().toString(), editKey.getText().toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        });

        btnDekrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if(editKey.getText().toString().isEmpty()){
                        Notif();
                    }else {
                        decryptedfile(editInput.getText().toString(), editKey.getText().toString());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        });

        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             new MaterialFilePicker()
                     .withActivity(MainActivity.this)
                     .withRequestCode(1000)
                     .withHiddenFiles(true)
                     .start();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGoogleApiClient != null){
                    upload_to_drive();
                }else{
                    Log.e(TAG, "Tidak dapat terhubung ke Google Drive");
                }
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Exit();
            }
        });

    }
    private  void Notif(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Konfirmasi Kata Sandi!")
                .setMessage("Kata Sandi Tidak Boleh Kosong")
                .setNegativeButton("Tutup", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        alert.show();
    }
    private  void NotifEncrypt(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enkripsi")
                .setMessage("Berkas Berhasil di Enkripsi!")
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        alert.show();
    }
    private  void NotifDecrypt(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Dekripsi")
                .setMessage("Berkas Berhasil di Dekripsi!")
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        alert.show();
    }
    private void Exit(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Konfirmasi Keluar!")
                .setMessage("Anda Yakin Ingin Keluar Aplikasi ?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        alert.show();

    }

    public void encryptedfile(String path, String Pass) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException{
        FileInputStream fis = new FileInputStream(path);
        FileOutputStream fos = new FileOutputStream(path.concat(".enclouds"));
        byte[] key = (salt + editKey.getText().toString()).getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        SecretKeySpec sks = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, sks);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        int b;
        byte[] d = new byte[8];
        while((b = cis.read(d)) != -1){
            fos.write(d, 0, b);
        }
        fos.flush();
        fos.close();
        cis.close();
        editInput.setText(path +".enclouds");
        NotifEncrypt();
        Toast.makeText(this, "Berkas Berhasil Dienkripsi", Toast.LENGTH_LONG).show();

    }

    public void decryptedfile(String path, String Pass) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException{
        FileInputStream fis = new FileInputStream(path);
        FileOutputStream fos = new FileOutputStream(path.replace(".enclouds",""));
        byte[] key = (salt + editKey.getText().toString()).getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        SecretKeySpec sks = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sks);
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        int b;
        byte[] d = new byte[8];
        while((b = cis.read(d)) != -1){
            fos.write(d, 0, b);
        }
        fos.flush();
        fos.close();
        cis.close();
        Toast.makeText(this, "Berkas Berhasil Didekripsi", Toast.LENGTH_LONG).show();
        NotifDecrypt();
    }
    private void upload_to_drive(){

        check_folder_exist();
    }
    private void check_folder_exist(){
        Query query = new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, Folder_Name),
                Filters.eq(SearchableField.TRASHED, false))).build();
        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                if(!metadataBufferResult.getStatus().isSuccess()){
                    Log.e(TAG, "Tidak dapat membuat folder di root");
                }else {
                    boolean isFound = false;
                    for (Metadata m : metadataBufferResult.getMetadataBuffer()){
                        if(m.getTitle().equals(Folder_Name)){
                            Log.e(TAG, "Folder Tersedia");
                            isFound = true;
                            DriveId driveId = m.getDriveId();
                            create_file_in_folder(driveId);
                            break;
                        }
                    }
                    if(!isFound){
                        Log.i(TAG, "Folder Tidak Tersedia: Folder Dibuat");
                    }
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(Folder_Name).build();

                    Drive.DriveApi.getRootFolder(mGoogleApiClient)
                            .createFolder(mGoogleApiClient, changeSet)
                            .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                                @Override
                                public void onResult(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
                                    if(!driveFolderResult.getStatus().isSuccess()){
                                        Log.e(TAG, "Gagal Membuat Folder");
                                    }else
                                        Log.i(TAG, "Folder Berhasil Dibuat");
                                    DriveId driveId = driveFolderResult.getDriveFolder().getDriveId();
                                    create_file_in_folder(driveId);
                                }
                            });
                }
            }
        });
    }
    public void create_file_in_folder(final DriveId driveId){
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if(!driveContentsResult.getStatus().isSuccess()){
                    Log.e(TAG, "Gagal Membuat File");
                    return;
                }
                OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();

                Toast.makeText(MainActivity.this, "Proses Unggah ke Google Drive", Toast.LENGTH_LONG).show();

                final File thFile = new File(editInput.getText().toString());
                try {
                    FileInputStream fileInputStream = new FileInputStream(thFile);
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while((byteRead = fileInputStream.read(buffer)) != -1){
                        outputStream.write(buffer, 0, byteRead);
                    }
                }catch (IOException ie){
                    Log.i(TAG, "Tidak dapat membaca konten");
                }
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(thFile.getName())
                        .setMimeType("text/plain")
                        .setStarred(false)
                        .build();
                DriveFolder folder = driveId.asDriveFolder();
                folder.createFile(mGoogleApiClient, changeSet,driveContentsResult.getDriveContents())
                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                        if(!driveFileResult.getStatus().isSuccess()){
                            Log.e(TAG, "Gagal membuat konten");
                            return;
                        }
                        Log.v(TAG, "Membuat Konten : "+driveFileResult.getDriveFile().getDriveId());
                    }
                });
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1001:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Ijin DIterima!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Ijin Ditolak!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1000 && resultCode == RESULT_OK){
            filepath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            editInput.setText(filepath);

        }
        if(requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK){
            mGoogleApiClient.connect();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "Koneksi GoogleApiClient gagal: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(TAG, "API client terhubung.");

    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient ditolak");
    }

}
