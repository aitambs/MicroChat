package com.orxor.micro_chat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MessagesAdapter extends ArrayAdapter<Message> {
    Activity activity;
    List<Message> messages;

    public MessagesAdapter(Activity activity, List<Message> messages) {
        super(activity, R.layout.chat_message,messages);
        this.activity=activity;
        this.messages=messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view==null){
            LayoutInflater inflater = activity.getLayoutInflater();
            view=inflater.inflate(R.layout.chat_message,parent,false);
            holder=new ViewHolder();
            holder.tvFrom=view.findViewById(R.id.from);
            holder.tvMessage=view.findViewById(R.id.message);
            holder.ivPhoto=view.findViewById(R.id.photo);
            holder.llContainer=view.findViewById(R.id.ll_container);
            view.setTag(holder);
        } else {
            holder=(ViewHolder)view.getTag();
        }
        Message message = messages.get(position);

        if (message.isPhoto()){
            holder.tvFrom.setText(message.getFrom());
            holder.tvMessage.setText("");
            holder.ivPhoto.setVisibility(View.VISIBLE);
            byte[] bytes = Base64.decode(message.getContent(),Base64.NO_WRAP);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            holder.ivPhoto.setImageBitmap(bitmap);
            holder.ivPhoto.setTag(message.getExtraContent());
            holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowPhoto showPhoto = new ShowPhoto();
                    showPhoto.setPhotoName((String)v.getTag());
                    showPhoto.show(activity.getFragmentManager(),"LargePhoto");
                }
            });
        } else {
            holder.tvFrom.setText(message.getFrom());
            holder.tvMessage.setText(message.getContent());
            holder.ivPhoto.setVisibility(View.GONE);
            holder.ivPhoto.setOnClickListener(null);
        }

        if (message.getFrom().equals(ChatActivity.currentUser.getUserName())){
            holder.llContainer.setBackgroundResource(R.color.colorPrimary);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.llContainer.getLayoutParams();
            params.gravity=Gravity.END;
            holder.llContainer.setLayoutParams(params);
        } else {
            holder.llContainer.setBackgroundResource(android.R.color.white);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.llContainer.getLayoutParams();
            params.gravity=Gravity.START;
            holder.llContainer.setLayoutParams(params);
        }
        return view;
    }

    static class ViewHolder{
        public TextView tvFrom,tvMessage;
        public ImageView ivPhoto;
        public LinearLayout llContainer;
    }
}
