package com.orxor.micro_chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatActivity extends Activity {

    public static final int PHOTO_REQUEST_CODE = 123;
    public static final String MESSAGES_JSON_FILE = "messages.json";
    static User currentUser;
    private EditText etMessage;
    private Button btnSend, btnPhoto;
    private ListView listView;
    public static Boolean isActive = false;
    private BroadcastReceiver myBroadcastReceiver;
    private List<Message> messages = new ArrayList<>();
    private ArrayAdapter<Message> arrayAdapter;
    public boolean refresh =false;
    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.USER)){
            currentUser=intent.getParcelableExtra(MainActivity.USER);
        } else {
            SharedPreferences preferences=getSharedPreferences(MainActivity.SETTINGS,MODE_PRIVATE);
            if(preferences.contains(MainActivity.USER)) {
                currentUser = new Gson().fromJson(preferences.getString(MainActivity.USER, ""), User.class);
            } else exit();

        }
        setContentView(R.layout.activity_chat);
        etMessage=findViewById(R.id.et_message);
        btnSend=findViewById(R.id.btn_send);
        btnPhoto=findViewById(R.id.btn_photo);
        listView=findViewById(R.id.lv_chat);

        arrayAdapter = new MessagesAdapter(ChatActivity.this,messages);
        listView.setAdapter(arrayAdapter);

        FirebaseMessaging.getInstance().subscribeToTopic("MicroChat")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(ChatActivity.this, "Failed to Register to FCM!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive=true;
        IntentFilter intentFilter = new IntentFilter("MicroChat_new_message");
        myBroadcastReceiver=new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver,intentFilter);
        final File messegesBackup = new File(getFilesDir(),MESSAGES_JSON_FILE);
        InputStreamReader reader=null;
        if (messages.isEmpty()) {
            Gson gson = new Gson();
            try {
                reader = new InputStreamReader(new FileInputStream(messegesBackup), StandardCharsets.UTF_8);
                ArrayList<Message> temp;
                temp = gson.fromJson(reader, new TypeToken<ArrayList<Message>>() {
                }.getType());
                messages.addAll(temp);
                arrayAdapter.notifyDataSetChanged();
            } catch (FileNotFoundException e) {
                Log.w("ChatActivity", "No messages Backup!");
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        listView.smoothScrollToPosition(messages.size());
        Intent intent = getIntent();
        refresh=intent.hasExtra("refresh");
        if (refresh){
            refresh = false;
            String from = intent.getStringExtra("refresh");
            intent.removeExtra("refresh");
            Log.e("ChatActivity", from);
            ServerAction action = new ServerAction(ServerAction.ACTION_REFRESH_MESSAGES,
                    new Message(currentUser.getUserName(),from,false),currentUser);
            ServerConnection connection = new ServerConnection();
            connection.setActualPostExecute(new OnActualPostExecute() {
                @Override
                public void onPostExecute(ServerResponse response) {
                    Gson gson = new Gson();

                    List<Message> newMessages = null;
                    newMessages = gson.fromJson(response.getResponse(),new TypeToken<List<Message>>(){}.getType());
                    if (newMessages != null && !newMessages.isEmpty()) {
                        for (Message newMessage : newMessages) {
                            if (!messages.contains(newMessage)) messages.add(newMessage);
                        }
                        Collections.sort(messages);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            });
            connection.execute(action);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive=false;
        unregisterReceiver(myBroadcastReceiver);
        saveMessages();
    }

    private void saveMessages() {
        Gson gson = new Gson();
        File messegesBackup = new File(getFilesDir(), MESSAGES_JSON_FILE);
        OutputStreamWriter writer = null;
        try {
            writer=new OutputStreamWriter(new FileOutputStream(messegesBackup), StandardCharsets.UTF_8);
            writer.write(gson.toJson(messages));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                exit();
        }
        return true;
    }

    private void exit() {
        getSharedPreferences(MainActivity.SETTINGS,MODE_PRIVATE).edit().remove(MainActivity.USER).apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void sendMessage(View view) {
        String content=etMessage.getText().toString();
        etMessage.setText("");
        if (content.isEmpty()) return;
        btnSend.setEnabled(false);
        btnPhoto.setEnabled(false);
        Message message = new Message(currentUser.getUserName(),content,false);
        ServerAction action = new ServerAction(ServerAction.ACTION_SEND_MESSAGE,message,currentUser);
        ServerConnection connection = new ServerConnection();
        connection.setActualPostExecute(new OnActualPostExecute() {
            @Override
            public void onPostExecute(ServerResponse response) {
                btnSend.setEnabled(true);
                btnPhoto.setEnabled(true);
                if (response !=null && response.isSuccess()){
                    Toast.makeText(ChatActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                } else if (response !=null && !response.isSuccess()) {
                    Toast.makeText(ChatActivity.this, response.getResponse(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ChatActivity.this, "Server did not respond", Toast.LENGTH_SHORT).show();
                }
            }
        });
        connection.execute(action);
    }

    public void takePhoto(View view) {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(storageDir, "photo.jpg");
        photoPath = file.getAbsolutePath();
        Uri photoUri = FileProvider.getUriForFile(this, "com.orxor.micro_chat.fileprovider", file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PHOTO_REQUEST_CODE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            sendPhoto();
        }
    }

    private void sendPhoto() {
        btnSend.setEnabled(false);
        btnPhoto.setEnabled(false);

        int orientation = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(photoPath);
            int ori = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,1);
            if (ori == ExifInterface.ORIENTATION_ROTATE_90) orientation=90;
            else if (ori == ExifInterface.ORIENTATION_ROTATE_180) orientation=180;
            else if (ori == ExifInterface.ORIENTATION_ROTATE_270) orientation=270;
        } catch (IOException e) {
            e.printStackTrace();
        }


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap large = BitmapFactory.decodeFile(photoPath,options);

        Matrix matrix = new Matrix();
        if (orientation!=0) matrix.postRotate(orientation);
        Bitmap large_corrected = Bitmap.createBitmap(large,0,0,large.getWidth(),large.getHeight(),matrix,true);
        byteArrayOutputStream = new ByteArrayOutputStream();
        large_corrected.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        large.recycle();
        large_corrected.recycle();
        String largeString = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);
        int photoWidth = options.outWidth;
        int photoHeight = options.outHeight;
        options.inSampleSize=Math.max(photoWidth / 128, photoHeight / 128);;
        large_corrected.recycle();
        byteArrayOutputStream.reset();
        Bitmap thumbnail = BitmapFactory.decodeFile(photoPath,options);
        Bitmap thumbnail_corrected = Bitmap.createBitmap(thumbnail,0,0,thumbnail.getWidth(),thumbnail.getHeight(),matrix,true);
        thumbnail_corrected.compress(Bitmap.CompressFormat.JPEG,20,byteArrayOutputStream);
        String content = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);
        byteArrayOutputStream.reset();
        thumbnail.recycle();
        thumbnail_corrected.recycle();
        Message message = new Message(currentUser.getUserName(),content,true);
        message.setExtraContent(largeString);
        ServerAction action = new ServerAction(ServerAction.ACTION_SEND_MESSAGE,message,currentUser);
        ServerConnection connection = new ServerConnection();
        connection.setActualPostExecute(new OnActualPostExecute() {
            @Override
            public void onPostExecute(ServerResponse response) {
                btnSend.setEnabled(true);
                btnPhoto.setEnabled(true);
                if (response !=null && response.isSuccess()){
                    Toast.makeText(ChatActivity.this, "Sent", Toast.LENGTH_SHORT).show();
                } else if (response !=null && !response.isSuccess()) {
                    Toast.makeText(ChatActivity.this, response.getResponse(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ChatActivity.this, "Server did not respond", Toast.LENGTH_SHORT).show();
                }
            }
        });
        connection.execute(action);
    }

    class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Gson gson = new Gson();
            Message message = gson.fromJson(intent.getStringExtra("message"),Message.class);
            if (!messages.contains(message)) {
                messages.add(message);
                Collections.sort(messages);
                arrayAdapter.notifyDataSetChanged();
                saveMessages();
            }
        }
    }
}
