package com.melissa.mikochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Chat> chatList;
    private String nickname = "멜리사J";

    private EditText chatText;
    private ImageButton sendButton;

    private DatabaseReference messageData;

    private DatabaseReference memberListData;

    private DatabaseReference Urldata;

    private DatabaseReference gpsData;

    private DatabaseReference controllerData;

    static ArrayList<String> array_member = new ArrayList<String>();


    static ArrayList<String> array_url = new ArrayList<String>();

    ArrayList<String> array_streamingMember = new ArrayList<String>();

    static ArrayList<Pattern> array_link = new ArrayList<Pattern>();

    static int pushMember = 0;

    int URL_count = 0;


    double latitude;
    double longtitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatText = findViewById(R.id.chatText);
        sendButton = findViewById(R.id.sendButton);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        int i = 10;
        int array[] = {1, 2, 3, 4, 5, 6, 7, 8, 9};


        //토큰 정보 가져오는 장소
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    System.out.println("Fetching FCM registration token failed" + task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();

                // Log and toast
                String msg = getString(R.string.msg_token_fmt, token);
                System.out.println("토큰의 정보" + msg);
                //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //입력창에 메시지를 입력 후 버튼클릭했을 때
                String msg = chatText.getText().toString();

                if (msg.contains(Constants.command_poi)) {
                    //여기에 음... 좌표값 전송 해야겠다.
                    int idx = msg.indexOf("까지");
                    String address = (msg.substring(0, idx));
                    System.out.println("제대로 잘렸나 함 보자 : " + address);
                    gps_now_location_check(address);
                }

                //hud 컨트롤에 들어갈 내용
                else if(msg.contains(Constants.command_debugNavi) || msg.contains(Constants.command_naviOff) || msg.contains(Constants.command_naviOn) ||msg.contains(Constants.command_moveHome) || msg.contains(Constants.command_moveNavi)){

                    System.out.println("네비 시작 및 종료 전송");
                    controllerData.child("launcher").push().setValue(msg);
                }


                if (msg != null) {
                    Chat chat = new Chat();
                    chat.setName(nickname);
                    chat.setMsg(msg);

                    //메시지를 파이어베이스에 보냄.
                    messageData.push().setValue(chat);

                    chatText.setText("");

                }
            }

        });
        //리사이클러뷰에 어댑터 적용
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList, nickname);
        recyclerView.setAdapter(adapter);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messageData = database.getReference("message");
        memberListData = database.getReference("member");
        Urldata = database.getReference("token");
        gpsData = database.getReference("gps");
        controllerData = database.getReference("controller");


        serverDataLoad();


        firstLoad();


    }

    private void serverDataLoad() {


        //데이터들을 추가, 변경, 제거, 이동, 취소
        messageData.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                System.out.println("DB 데이터 추가됨");
                //어댑터에 DTO추가
                Chat chat = snapshot.getValue(Chat.class);
                ((ChatAdapter) adapter).addChat(chat);
                recyclerView.scrollToPosition(chatList.size() - 1);

                for (int member = 0; member < array_member.size(); member++) {
                    if (chat.getName().contains(array_member.get(member))) {
                        pushMember = member;
                    }
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                System.out.println("DB 데이터 변화됨");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                System.out.println("DB 데이터 제거됨");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                System.out.println("DB 위치 변경됨");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("DB 처리중 오류");
            }
        });


        memberListData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("멤버 DB 데이터 추가됨");
                //서버에 저장된 데이터 불러오기
                array_member.clear();
                int member_count = 0;
                for (DataSnapshot memberchild : snapshot.getChildren()) {
                    array_member.add(String.valueOf(memberchild.getValue()));
                    System.out.println("멤버 리스트 호출중 " + array_member.get(member_count));
                    member_count++;

                }

                //멤버리스트 조회 완료 후 토큰 작업
                Urldata.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        System.out.println("토큰 DB 데이터 추가됨");
                        //서버에 저장된 데이터 불러오기
                        array_url.clear();
                        URL_count = 0;


                        for (DataSnapshot URLchild : snapshot.getChildren()) {
                            String token = String.valueOf(URLchild.getValue());
                            array_url.add("https://www.youtube.com/channel/" + token);
                            System.out.println("토큰 리스트 호출중 : " + array_url.get(URL_count));
                            URL_count++;
                        }

                        // 여기에 병합 데이터 2개 추가.
                        array_streamingMember.clear();
                        for (int member = 0; member < array_member.size(); member++) {
                            // 방송중 적힌 String 값을 수집.
                            array_streamingMember.add(array_member.get(member) + " : 방송 중");
                            System.out.println("방송중 리스트 호출중 : " + array_streamingMember.get(member));
                        }

                        array_link.clear();
                        for (int merge = 0; merge < array_member.size(); merge++) {
                            array_link.add(Pattern.compile(array_streamingMember.get(merge)));

                            System.out.println("머지 리스트 호출중 : " + array_streamingMember.get(merge));
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.out.println("시스템 데이터 처리중 오류");
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("백업 DB 처리중 오류");
            }
        });


    }

    private void firstLoad() {
        Chat chat = new Chat();
        chat.setName(nickname);
        chat.setMsg("오하요");

        //메시지를 파이어베이스에 보냄.
        messageData.push().setValue(chat);

        chatText.setText("");
    }


    @SuppressLint("MissingPermission")
    public void gps_now_location_check(String address) {
        //현재 위도와 경도 좌표 수신
        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        try {
            latitude = location.getLatitude();
            longtitude = location.getLongitude();
        } catch (NullPointerException e) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            latitude = location.getLatitude();
            longtitude = location.getLongitude();
            System.out.println(" 네트워크 값으로 체인지 ");
        }
        System.out.println("위도와 경도 : " + latitude + ", " + longtitude); // 값이 비어있는경우 0.0 이 들어오는거 확인 완료.


        gpsData.child("address").push().setValue(address);
        gpsData.child("latitude").push().setValue(latitude);
        gpsData.child("longtitude").push().setValue(longtitude);


    }




}