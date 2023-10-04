package com.melissa.mikochat;


import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import static com.melissa.mikochat.MainActivity.array_link;
import static com.melissa.mikochat.MainActivity.array_member;
import static com.melissa.mikochat.MainActivity.array_url;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private List<Chat> chatList;
    private String name;

    int link =0;


    public ChatAdapter(List<Chat> chatData, String name){
        //MainActivity.java에서 받은 데이터들을 저장
        chatList = chatData;
        this.name = name;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView nameText;
        public TextView msgText;
        public LinearLayout msgLinear;
        public ImageView img_melissa;
        public ImageView img_miko;



        public View rootView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.nameText);
            msgText = itemView.findViewById(R.id.msgText);
            msgLinear = itemView.findViewById(R.id.msgLinear);

            img_melissa = itemView.findViewById(R.id.img_melissa);
            img_miko = itemView.findViewById(R.id.img_miko);

            rootView = itemView;

            itemView.setEnabled(true);
            itemView.setClickable(true);
        }
    }


    @NonNull
    @Override
    public ChatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflation 과정
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item,parent,false);

        MyViewHolder myViewHolder = new MyViewHolder(linearLayout);




        return myViewHolder;
    }

    //각 뷰의 기능 설정
    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.MyViewHolder holder, int position) {

        Chat chat = chatList.get(position);

        holder.nameText.setText(chat.getName());
        holder.msgText.setText(chat.getMsg());







        for (link = 0 ; link<array_link.size() ; link++) {

            // 현재 구현이 완료되었음
            Linkify.TransformFilter mTransform = new Linkify.TransformFilter() {
                @Override
                public String transformUrl(Matcher match, String url) {
                    return array_url.get(link);
                }
            };


            Linkify.addLinks(holder.msgText, array_link.get(link), "", null, mTransform);
        }
        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        //여기서 작업 하네....
        System.out.println("추가된 데이터 : :" +chat.getMsg());
        System.out.println("추가된 이름 : :" +chat.getName());

        TypedArray typedArray_illust;

        typedArray_illust = holder.itemView.getContext().getResources().obtainTypedArray(R.array.png_illust); //chara_select가 배열 번호가된다.

        for(int member_list = 0 ; member_list<array_member.size() ; member_list++)
        if(chat.getName().contains(array_member.get(member_list))){
            System.out.println("이미지 변경에 진입성공");
            holder.img_miko.setImageResource(typedArray_illust.getResourceId(member_list,0));

        }

        if(chat.getName().equals(this.name)){
            //사용자가 저장된 이름과 같을 시 오른쪽으로 정렬
            holder.nameText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            holder.msgText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

            holder.msgLinear.setGravity(Gravity.RIGHT);
            holder.img_miko.setVisibility(View.GONE);
            holder.img_melissa.setVisibility(View.VISIBLE);
        } else {
            //아닐 시 왼쪽 정렬
            holder.nameText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            holder.msgText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            holder.msgLinear.setGravity(Gravity.LEFT);
            holder.img_melissa.setVisibility(View.GONE);
            holder.img_miko.setVisibility(View.VISIBLE);
        }


    }

    //메시지아이템 갯수세기
    @Override
    public int getItemCount() {
        return chatList == null ? 0: chatList.size();
    }

    //메시지아이템의 추가 및 적용
    public void addChat(Chat chat){
        chatList.add(chat);
        notifyItemInserted(chatList.size()-1);
    }
}
