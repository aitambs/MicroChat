package com.orxor.micro_chat;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ShowPhoto extends DialogFragment {

    ImageView iv_large_photo;
    Button btn_close;
    String photoName;

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_photo,container,false);
        btn_close =  view.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        iv_large_photo= view.findViewById(R.id.iv_large_photo);
        ServerConnection serverConnection = new ServerConnection();
        serverConnection.setActualPostExecute(new OnActualPostExecute() {
            @Override
            public void onPostExecute(ServerResponse response) {
                if (response != null && response.isSuccess()) {
                    byte[] bytes = Base64.decode(response.getResponse(),Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    iv_large_photo.setImageBitmap(bitmap);
                } else if (response !=null && !response.isSuccess()){
                    dismiss();
                    Toast.makeText(btn_close.getContext(), response.getResponse(), Toast.LENGTH_LONG).show();
                } else {
                    dismiss();
                }
            }
        });
        ServerAction action =new ServerAction(ServerAction.ACTION_GET_PHOTO,
                new Message(ChatActivity.currentUser.getUserName(),photoName,false), ChatActivity.currentUser);
        serverConnection.execute(action);
        return view;
    }
}
