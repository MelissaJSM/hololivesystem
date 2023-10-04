package com.melissa.mikochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<Chat> chatList;


    private EditText chatText;
    private Button sendButton;

    private DatabaseReference messageData;
    private DatabaseReference QAdata;
    private DatabaseReference memberListData;
    private DatabaseReference Systemdata;

    private DatabaseReference Tokendata;

    private DatabaseReference Commanddata;

    private DatabaseReference SpeakingData;

    private DatabaseReference gpsData;

    private DatabaseReference iftttData;

    private DatabaseReference naviData;

    int first_count = 10;

    int member_count = 0;

    int ifttt_count = 0;
    int qa_count = 0;

    int SQ_count = 0;
    int SA_count = 0;

    int Tk_count = 0;

    int CDQ_count = 0;
    int CDA_count = 0;

    int SK_count = 0;

    int memberChange = Constants.member_changeDisable;

    String ifttt_key = "여기에 필요한 ifttt 키를 입력";

    //닉네임은 최초 기본상태를 저장하기 위함
    //usersharedpref 사용하여 어플 상태를 저장하길 바람.
    private String nickname = "사쿠라 미코";
    int member_id = 4; //0 : 시스템 루시아 발렌타인
    //3 : 사쿠라 미코
    //? : 시라카미 후부키

    // 이두개는 추후에 nickname = memeber_array.get(member_id)를 이용하여 설정 할 것이다.

    int system_mode = Constants.system_disable;


    int message_lock = Constants.lock_disable;

    int lockCount = Constants.lock_10min;

    int editNumber = 0; // 대화 수정용

    double save_latitude = 0.0; // 전송된 휴대폰의 현재위치
    double save_longtitude = 0.0;


    Double des_lat = 0.0; // 목적지의 위치
    Double des_long = 0.0;


    String add_edit_Questionmsg = null;
    String add_edit_Answermsg = null;

    String save_address = null;

    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "구글 파이어베이스 개인 서버 토큰키 입력";

    ArrayList<String> array_answer = new ArrayList<String>(); // 여기에 숫자배열 넣는다.
    ArrayList<String> array_question = new ArrayList<String>();
    ArrayList<String> array_member = new ArrayList<String>();

    ArrayList<String> array_ifttt = new ArrayList<String>();

    ArrayList<String> array_system_question = new ArrayList<String>();

    ArrayList<String> array_system_answer = new ArrayList<String>();

    ArrayList<String> array_token = new ArrayList<String>();

    ArrayList<String> array_stream_list = new ArrayList<String>();

    ArrayList<String> array_command_question = new ArrayList<String>();
    ArrayList<String> array_command_answer = new ArrayList<String>();

    ArrayList<String> array_speaking = new ArrayList<String>();

    ArrayList<String> array_POI_name = new ArrayList<String>();
    ArrayList<String> array_POI_address = new ArrayList<String>();
    ArrayList<String> array_POI_latitude = new ArrayList<String>();
    ArrayList<String> array_POI_longtitude = new ArrayList<String>();

    int[] speaking_rare = {Constants.member_miko, Constants.member_peko, Constants.member_iroha}; // 특정 발음 멤버

    int[] respect_speak = {Constants.member_sora, Constants.member_azki, Constants.member_mel, Constants.member_fubuki, Constants.member_aqua, Constants.member_choko, Constants.member_mio, Constants.member_watame, Constants.member_lamy, Constants.member_koyori, Constants.member_fauna, Constants.member_nerissa, Constants.member_ao, Constants.member_raden};


    Handler lock_Handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chatText = findViewById(R.id.chatText);
        sendButton = findViewById(R.id.sendButton);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //리사이클러뷰에 어댑터 적용
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList, nickname);
        recyclerView.setAdapter(adapter);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        memberListData = database.getReference("member");
        messageData = database.getReference("message");
        QAdata = database.getReference("hololive");
        Systemdata = database.getReference("system");
        Tokendata = database.getReference("token");
        Commanddata = database.getReference("command");
        SpeakingData = database.getReference("speaking");
        gpsData = database.getReference("gps");
        iftttData = database.getReference("ifttt");
        naviData = database.getReference("navi");
        //내 토큰 조회 시스템
        loadMyToken();


        //메시지 차일드로 로드해놔서 로드 멈추는 핸들러 필요함.
        first_load();


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //입력창에 메시지를 입력 후 버튼클릭했을 때
                String msg = chatText.getText().toString();

                if (msg != null) {
                    String sendName;
                    sendName = array_member.get(member_id);
                    //메세지 전송
                    send_Message(sendName, msg);


                    send_Push(msg);
                }
            }

        });

        //backupData.removeValue();

        //현재 디버그 보내는 작업 중임
        //sendDebug();

        serverDataLoad();

        messageDataLoad();


        // 시스템 호출 명령어 -> 루시아 소환 -> 시스템 수정명령어 -> 루시아 대답 -> 처리 ->> 루시아 처리완료 대답             ---> 시스템 대화 종료 -> 다시 원위치로
    }

    private void loadMyToken() {
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
    }

    private void sendDebug() {

        //질문, 답 푸시


        /*
        QAdata.child("호쇼 마린").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("나같은 jk는 이해 할 수 없는 말이야!");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("자 그럼 모두 출항~!!");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("새벽에 선장을 깨우다니 항해중 문제가 생긴거야?");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("아호이! 잠은 잘 잤어?");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("벌써 오후라니 생각보다 시간 빠른걸?");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("아호이! 오늘하루 고생 많았어!");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("키미땃쥐~ 선장이 방송중이야 구경와~");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("Ahoy! 홀로라이브 3기생 호쇼해적단 선장 호쇼마린입니다~!");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("이런 나랑 결혼하게 만들어주지 당장 도장찍어라");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("소녀에게 그런말은 심한말이야 알겠지?");


        QAdata.child("호쇼 마린").child("question").push().setValue("오하요");
        QAdata.child("호쇼 마린").child("question").push().setValue("Data LoadError(\"null\")");
        QAdata.child("호쇼 마린").child("question").push().setValue("Member Changed(\"goodbye\")");
        QAdata.child("호쇼 마린").child("question").push().setValue("안녕");
        QAdata.child("호쇼 마린").child("question").push().setValue("Data Overlapping(\"hello\")");
        QAdata.child("호쇼 마린").child("question").push().setValue("Data Overlapping(\"hello\")");
        QAdata.child("호쇼 마린").child("question").push().setValue("Data Overlapping(\"hello\")");
        QAdata.child("호쇼 마린").child("question").push().setValue("Data Overlapping(\"hello\")");
        QAdata.child("호쇼 마린").child("question").push().setValue("누구");
        QAdata.child("호쇼 마린").child("question").push().setValue("할머니");
        QAdata.child("호쇼 마린").child("question").push().setValue("Data LoadError(\"lock off\")");

         */





        /*
        QAdata.child("루시아 발렌타인").child("question").push().setValue("오하요");
        QAdata.child("토키노 소라").child("question").push().setValue("오하요");
        QAdata.child("로보코").child("question").push().setValue("오하요");
        QAdata.child("AZKi").child("question").push().setValue("오하요");
        QAdata.child("사쿠라 미코").child("question").push().setValue("오하요");
        QAdata.child("호시마치 스이세이").child("question").push().setValue("오하요");
        QAdata.child("요조라 멜").child("question").push().setValue("오하요");
        QAdata.child("아키 로젠탈").child("question").push().setValue("오하요");
        QAdata.child("아카이 하아토").child("question").push().setValue("오하요");
        QAdata.child("시라카미 후부키").child("question").push().setValue("오하요");
        QAdata.child("나츠이로 마츠리").child("question").push().setValue("오하요");
        QAdata.child("미나토 아쿠아").child("question").push().setValue("오하요");
        QAdata.child("무라사키 시온").child("question").push().setValue("오하요");
        QAdata.child("나키리 아야메").child("question").push().setValue("오하요");
        QAdata.child("유즈키 초코").child("question").push().setValue("오하요");
        QAdata.child("오오조라 스바루").child("question").push().setValue("오하요");
        QAdata.child("오오카미 미오").child("question").push().setValue("오하요");
        QAdata.child("네코마타 오카유").child("question").push().setValue("오하요");
        QAdata.child("이누가미 코로네").child("question").push().setValue("오하요");
        QAdata.child("우사다 페코라").child("question").push().setValue("오하요");
        QAdata.child("시라누이 후레아").child("question").push().setValue("오하요");
        QAdata.child("시로가네 노엘").child("question").push().setValue("오하요");
        QAdata.child("호쇼 마린").child("question").push().setValue("오하요");
        QAdata.child("아마네 카나타").child("question").push().setValue("오하요");
        QAdata.child("츠노마키 와타메").child("question").push().setValue("오하요");
        QAdata.child("토코야미 토와").child("question").push().setValue("오하요");
        QAdata.child("히메모리 루나").child("question").push().setValue("오하요");
        QAdata.child("유키하나 라미").child("question").push().setValue("오하요");
        QAdata.child("모모스즈 네네").child("question").push().setValue("오하요");
        QAdata.child("시시로 보탄").child("question").push().setValue("오하요");
        QAdata.child("오마루 폴카").child("question").push().setValue("오하요");
        QAdata.child("라플라스 다크니스").child("question").push().setValue("오하요");
        QAdata.child("타카네 루이").child("question").push().setValue("오하요");
        QAdata.child("하쿠이 코요리").child("question").push().setValue("오하요");
        QAdata.child("사카마타 클로에").child("question").push().setValue("오하요");
        QAdata.child("카자마 이로하").child("question").push().setValue("오하요");
        QAdata.child("모리 칼리오페").child("question").push().setValue("오하요");
        QAdata.child("타카나시 키아라").child("question").push().setValue("오하요");
        QAdata.child("니노마에 이나니스").child("question").push().setValue("오하요");
        QAdata.child("가우르 구라").child("question").push().setValue("오하요");
        QAdata.child("왓슨 아멜리아").child("question").push().setValue("오하요");
        QAdata.child("IRyS").child("question").push().setValue("오하요");
        QAdata.child("세레스 파우나").child("question").push().setValue("오하요");
        QAdata.child("오로 크로니").child("question").push().setValue("오하요");
        QAdata.child("나나시 무메이").child("question").push().setValue("오하요");
        QAdata.child("하코스 벨즈").child("question").push().setValue("오하요");
        QAdata.child("아윤다 리스").child("question").push().setValue("오하요");
        QAdata.child("무나 호시노바").child("question").push().setValue("오하요");
        QAdata.child("아이라니 이오피프틴").child("question").push().setValue("오하요");
        QAdata.child("쿠레이지 올리").child("question").push().setValue("오하요");
        QAdata.child("아냐 멜핏사").child("question").push().setValue("오하요");
        QAdata.child("파볼리아 레이네").child("question").push().setValue("오하요");
        QAdata.child("베스티아 제타").child("question").push().setValue("오하요");
        QAdata.child("카엘라 코발스키아").child("question").push().setValue("오하요");
        QAdata.child("코보 카나에루").child("question").push().setValue("오하요");
        QAdata.child("시오리 노벨라").child("question").push().setValue("오하요");
        QAdata.child("코세키 비쥬").child("question").push().setValue("오하요");
        QAdata.child("네리사 레이븐크로프트").child("question").push().setValue("오하요");
        QAdata.child("후와모코").child("question").push().setValue("오하요");
        QAdata.child("히오도시 아오").child("question").push().setValue("오하요");
        QAdata.child("오토노세 카나데").child("question").push().setValue("오하요");
        QAdata.child("이치조 리리카").child("question").push().setValue("오하요");
        QAdata.child("주우후테이 라덴").child("question").push().setValue("오하요");
        QAdata.child("토도로키 하지메").child("question").push().setValue("오하요");


        QAdata.child("루시아 발렌타인").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("토키노 소라").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("로보코").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("AZKi").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("사쿠라 미코").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("호시마치 스이세이").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("요조라 멜").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("아키 로젠탈").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("아카이 하아토").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("시라카미 후부키").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("나츠이로 마츠리").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("미나토 아쿠아").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("무라사키 시온").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("나키리 아야메").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("유즈키 초코").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("오오조라 스바루").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("오오카미 미오").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("네코마타 오카유").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("이누가미 코로네").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("우사다 페코라").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("시라누이 후레아").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("시로가네 노엘").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("호쇼 마린").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("아마네 카나타").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("츠노마키 와타메").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("토코야미 토와").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("히메모리 루나").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("유키하나 라미").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("모모스즈 네네").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("시시로 보탄").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("오마루 폴카").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("라플라스 다크니스").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("타카네 루이").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("하쿠이 코요리").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("사카마타 클로에").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("카자마 이로하").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("모리 칼리오페").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("타카나시 키아라").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("니노마에 이나니스").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("가우르 구라").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("왓슨 아멜리아").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("IRyS").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("세레스 파우나").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("오로 크로니").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("나나시 무메이").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("하코스 벨즈").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("아윤다 리스").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("무나 호시노바").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("아이라니 이오피프틴").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("쿠레이지 올리").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("아냐 멜핏사").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("파볼리아 레이네").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("베스티아 제타").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("카엘라 코발스키아").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("코보 카나에루").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("시오리 노벨라").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("코세키 비쥬").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("네리사 레이븐크로프트").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("후와모코").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("히오도시 아오").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("오토노세 카나데").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("이치조 리리카").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("주우후테이 라덴").child("awnser").push().setValue("Data LoadError(\"null\")");
        QAdata.child("토도로키 하지메").child("awnser").push().setValue("Data LoadError(\"null\")");

         */






        /*

        //멤버리스트 푸시

        memberListData.push().setValue("루시아 발렌타인");
        memberListData.push().setValue("토키노 소라");
        memberListData.push().setValue("로보코");
        memberListData.push().setValue("AZKi");
        memberListData.push().setValue("사쿠라 미코");
        memberListData.push().setValue("호시마치 스이세이");
        memberListData.push().setValue("요조라 멜");
        memberListData.push().setValue("아키 로젠탈");
        memberListData.push().setValue("아카이 하아토");
        memberListData.push().setValue("시라카미 후부키");
        memberListData.push().setValue("나츠이로 마츠리");
        memberListData.push().setValue("미나토 아쿠아");
        memberListData.push().setValue("무라사키 시온");
        memberListData.push().setValue("나키리 아야메");
        memberListData.push().setValue("유즈키 초코");
        memberListData.push().setValue("오오조라 스바루");
        memberListData.push().setValue("오오카미 미오");
        memberListData.push().setValue("네코마타 오카유");
        memberListData.push().setValue("이누가미 코로네");
        memberListData.push().setValue("우사다 페코라");
        memberListData.push().setValue("시라누이 후레아");
        memberListData.push().setValue("시로가네 노엘");
        memberListData.push().setValue("호쇼 마린");
        memberListData.push().setValue("아마네 카나타");
        memberListData.push().setValue("츠노마키 와타메");
        memberListData.push().setValue("토코야미 토와");
        memberListData.push().setValue("히메모리 루나");
        memberListData.push().setValue("유키하나 라미");
        memberListData.push().setValue("모모스즈 네네");
        memberListData.push().setValue("시시로 보탄");
        memberListData.push().setValue("오마루 폴카");
        memberListData.push().setValue("라플라스 다크니스");
        memberListData.push().setValue("타카네 루이");
        memberListData.push().setValue("하쿠이 코요리");
        memberListData.push().setValue("사카마타 클로에");
        memberListData.push().setValue("카자마 이로하");
        memberListData.push().setValue("모리 칼리오페");
        memberListData.push().setValue("타카나시 키아라");
        memberListData.push().setValue("니노마에 이나니스");
        memberListData.push().setValue("가우르 구라");
        memberListData.push().setValue("왓슨 아멜리아");
        memberListData.push().setValue("IRyS");
        memberListData.push().setValue("세레스 파우나");
        memberListData.push().setValue("오로 크로니");
        memberListData.push().setValue("나나시 무메이");
        memberListData.push().setValue("하코스 벨즈");
        memberListData.push().setValue("아윤다 리스");
        memberListData.push().setValue("무나 호시노바");
        memberListData.push().setValue("아이라니 이오피프틴");
        memberListData.push().setValue("쿠레이지 올리");
        memberListData.push().setValue("아냐 멜핏사");
        memberListData.push().setValue("파볼리아 레이네");
        memberListData.push().setValue("베스티아 제타");
        memberListData.push().setValue("카엘라 코발스키아");
        memberListData.push().setValue("코보 카나에루");
        memberListData.push().setValue("시오리 노벨라");
        memberListData.push().setValue("코세키 비쥬");
        memberListData.push().setValue("네리사 레이븐크로프트");
        memberListData.push().setValue("후와모코");
        memberListData.push().setValue("히오도시 아오");
        memberListData.push().setValue("오토노세 카나데");
        memberListData.push().setValue("이치조 리리카");
        memberListData.push().setValue("주우후테이 라덴");
        memberListData.push().setValue("토도로키 하지메");



        //토큰 데이터

        Tokendata.push().setValue("Data LoadError");
        Tokendata.push().setValue("UCp6993wxpyDPHUpavwDFqgg");
        Tokendata.push().setValue("UCDqI2jOz0weumE8s7paEk6g");
        Tokendata.push().setValue("UC0TXe_LYZ4scaW2XMyi5_kw");
        Tokendata.push().setValue("UC-hM6YJuNYVAmUWxeIr9FeA");
        Tokendata.push().setValue("UC5CwaMl1eIgY8h02uZw7u8A");
        Tokendata.push().setValue("UCD8HOxPs4Xvsm8H0ZxXGiBw");
        Tokendata.push().setValue("UCFTLzh12_nrtzqBPsTCqenA");
        Tokendata.push().setValue("UC1CfXB_kRs3C-zaeTG3oGyg");
        Tokendata.push().setValue("UCdn5BQ06XqgXoAxIhbqw5Rg");
        Tokendata.push().setValue("UCQ0UDLQCjY0rmuxCDE38FGg");
        Tokendata.push().setValue("UC1opHUrw8rvnsadT-iGp7Cg");
        Tokendata.push().setValue("UCXTpFs_3PqI41qX2d9tL2Rw");
        Tokendata.push().setValue("UC7fk0CB07ly8oSl0aqKkqFg");
        Tokendata.push().setValue("UC1suqwovbL1kzsoaZgFZLKg");
        Tokendata.push().setValue("UCvzGlP9oQwU--Y0r9id_jnA");
        Tokendata.push().setValue("UCp-5t9SrOQwXMU7iIjQfARg");
        Tokendata.push().setValue("UCvaTdHTWBGv3MKj3KVqJVCw");
        Tokendata.push().setValue("UChAnqc_AY5_I3Px5dig3X1Q");
        Tokendata.push().setValue("UC1DCedRgGHBdm81E1llLhOQ");
        Tokendata.push().setValue("UCvInZx9h3jC2JzsIzoOebWg");
        Tokendata.push().setValue("UCdyqAaZDKHXg4Ahi7VENThQ");
        Tokendata.push().setValue("UCCzUftO8KOVkV4wQG1vkUvg");
        Tokendata.push().setValue("UCZlDXzGoo7d44bwdNObFacg");
        Tokendata.push().setValue("UCqm3BQLlJfvkTsX_hvm0UmA");
        Tokendata.push().setValue("UC1uv2Oq6kNxgATlCiez59hw");
        Tokendata.push().setValue("UCa9Y57gfeY0Zro_noHRVrnw");
        Tokendata.push().setValue("UCFKOVgVbGmX65RxO3EtH3iw");
        Tokendata.push().setValue("UCAWSyEs_Io8MtpY3m-zqILA");
        Tokendata.push().setValue("UCUKD-uaobj9jiqB-VXt71mA");
        Tokendata.push().setValue("UCK9V2B22uJYu3N7eR_BT9QA");
        Tokendata.push().setValue("UCENwRMx5Yh42zWpzURebzTw");
        Tokendata.push().setValue("UCs9_O1tRPMQTHQ-N_L6FU2g");
        Tokendata.push().setValue("UC6eWCld0KwmyHFbAqK3V-Rw");
        Tokendata.push().setValue("UCIBY1ollUsauvVi4hW4cumw");
        Tokendata.push().setValue("UC_vMYWcDjmfdpH6r4TTn1MQ");
        Tokendata.push().setValue("UCL_qhgtOy0dy1Agp8vkySQg");
        Tokendata.push().setValue("UCHsx4Hqa-1ORjQTh9TYDhww");
        Tokendata.push().setValue("UCMwGHR0BTZuLsmjY_NT5Pwg");
        Tokendata.push().setValue("UCoSrY_IQQVpmIRZ9Xf-y93g");
        Tokendata.push().setValue("UCyl1z3jo3XHR1riLFKG5UAg");
        Tokendata.push().setValue("UC8rcEBzJSleTkf_-agPM20g");
        Tokendata.push().setValue("UCO_aKKYxn4tvrqPjcTzZ6EQ");
        Tokendata.push().setValue("UCmbs8T6MWqUHP1tIQvSgKrg");
        Tokendata.push().setValue("UC3n5uGu18FoCy23ggWWp8tA");
        Tokendata.push().setValue("UCgmPnx-EEeOrZSg5Tiw7ZRQ");
        Tokendata.push().setValue("UCOyYb1c43VlX9rc_lT6NKQw");
        Tokendata.push().setValue("UCP0BspO_AMEe3aQqqpo89Dg");
        Tokendata.push().setValue("UCAoy6rzhSf4ydcYjJw3WoVg");
        Tokendata.push().setValue("UCYz_5n-uDuChHtLo7My1HnQ");
        Tokendata.push().setValue("UC727SQYUvx5pDDGQpTICNWg");
        Tokendata.push().setValue("UChgTyjG-pdNvxxhdsXfHQ5Q");
        Tokendata.push().setValue("UCTvHWSfBZgtxE4sILOaurIQ");
        Tokendata.push().setValue("UCZLZ8Jjx_RN2CXloOmgTHVg");
        Tokendata.push().setValue("UCjLEmnpCNeisMxy134KPwWw");
        Tokendata.push().setValue("UCgnfPPb9JI3e9A4cXHnWbyg");
        Tokendata.push().setValue("UC9p_lqQ0FEDz327Vgf5JwqA");
        Tokendata.push().setValue("UC_sFNM0z0MWm9A6WlKPuMMg");
        Tokendata.push().setValue("UCt9H_RpQzhxzlyBxFqrdHqA");
        Tokendata.push().setValue("UCMGfV7TVTmHhEErVJg1oHBQ");
        Tokendata.push().setValue("UCWQtYtq9EOB4-I5P-3fh8lA");
        Tokendata.push().setValue("UCtyWhCj3AqKh2dXctLkDtng");
        Tokendata.push().setValue("UCdXAk5MpyLD8594lm_OvtGQ");
        Tokendata.push().setValue("UC1iA6_NT4mtAcIII6ygrvCw");




         */


        //말투 리스트

        /*
        SpeakingData.push().setValue("루시아 발렌타인");
        SpeakingData.push().setValue("토키노 소라");
        SpeakingData.push().setValue("로보코");
        SpeakingData.push().setValue("AZKi");
        SpeakingData.push().setValue("니에");
        SpeakingData.push().setValue("호시마치 스이세이");
        SpeakingData.push().setValue("요조라 멜");
        SpeakingData.push().setValue("아키 로젠탈");
        SpeakingData.push().setValue("아카이 하아토");
        SpeakingData.push().setValue("시라카미 후부키");
        SpeakingData.push().setValue("나츠이로 마츠리");
        SpeakingData.push().setValue("미나토 아쿠아");
        SpeakingData.push().setValue("무라사키 시온");
        SpeakingData.push().setValue("나키리 아야메");
        SpeakingData.push().setValue("유즈키 초코");
        SpeakingData.push().setValue("오오조라 스바루");
        SpeakingData.push().setValue("오오카미 미오");
        SpeakingData.push().setValue("네코마타 오카유");
        SpeakingData.push().setValue("이누가미 코로네");
        SpeakingData.push().setValue("페코");
        SpeakingData.push().setValue("시라누이 후레아");
        SpeakingData.push().setValue("시로가네 노엘");
        SpeakingData.push().setValue("호쇼 마린");
        SpeakingData.push().setValue("아마네 카나타");
        SpeakingData.push().setValue("츠노마키 와타메");
        SpeakingData.push().setValue("토코야미 토와");
        SpeakingData.push().setValue("히메모리 루나");
        SpeakingData.push().setValue("유키하나 라미");
        SpeakingData.push().setValue("모모스즈 네네");
        SpeakingData.push().setValue("시시로 보탄");
        SpeakingData.push().setValue("오마루 폴카");
        SpeakingData.push().setValue("라플라스 다크니스");
        SpeakingData.push().setValue("타카네 루이");
        SpeakingData.push().setValue("하쿠이 코요리");
        SpeakingData.push().setValue("사카마타 클로에");
        SpeakingData.push().setValue("고자루");
        SpeakingData.push().setValue("모리 칼리오페");
        SpeakingData.push().setValue("타카나시 키아라");
        SpeakingData.push().setValue("니노마에 이나니스");
        SpeakingData.push().setValue("가우르 구라");
        SpeakingData.push().setValue("왓슨 아멜리아");
        SpeakingData.push().setValue("IRyS");
        SpeakingData.push().setValue("세레스 파우나");
        SpeakingData.push().setValue("오로 크로니");
        SpeakingData.push().setValue("나나시 무메이");
        SpeakingData.push().setValue("하코스 벨즈");
        SpeakingData.push().setValue("아윤다 리스");
        SpeakingData.push().setValue("무나 호시노바");
        SpeakingData.push().setValue("아이라니 이오피프틴");
        SpeakingData.push().setValue("쿠레이지 올리");
        SpeakingData.push().setValue("아냐 멜핏사");
        SpeakingData.push().setValue("파볼리아 레이네");
        SpeakingData.push().setValue("베스티아 제타");
        SpeakingData.push().setValue("카엘라 코발스키아");
        SpeakingData.push().setValue("코보 카나에루");
        */



        // 첫번째 : 친근함
        // 세번째 : 반말
        // 네번째 : 니에

        //오류는 제일 첫번째에 등록해놓자.
        /*
        Commanddata.child("question").push().setValue("command error");
        Commanddata.child("question").push().setValue("command error");
        Commanddata.child("question").push().setValue("command error");
        Commanddata.child("question").push().setValue("문 열어");
        Commanddata.child("question").push().setValue("문 열어2");
        Commanddata.child("question").push().setValue("문 열어3");
        Commanddata.child("question").push().setValue("문 닫아");
        Commanddata.child("question").push().setValue("문 닫아2");
        Commanddata.child("question").push().setValue("문 닫아3");

         */



        /*
        Commanddata.child("awnser").push().setValue("말씀하신 동작에 문제가 생겼나봐요. 점검이 필요해 보여요.");
        Commanddata.child("awnser").push().setValue("뭔가 잘못된거같아. 너가 확인해봐.");
        Commanddata.child("awnser").push().setValue("동작에 오류가 발생한거 같으니 확인해보는 ");

        Commanddata.child("awnser").push().setValue("자동차 문을 열어드릴게요. 닫을때도 저를 불러주세요!");
        Commanddata.child("awnser").push().setValue("차 문 열어줄게.");
        Commanddata.child("awnser").push().setValue("자동차 문을 여는");

        Commanddata.child("awnser").push().setValue("자동차 문을 닫아드릴게요. 열때도 저를 불러주세요!");
        Commanddata.child("awnser").push().setValue("차 문 닫아줄게.");
        Commanddata.child("awnser").push().setValue("자동차 문을 닫는");


         */


        Commanddata.child("question").push().setValue("네비 시작");
        Commanddata.child("question").push().setValue("네비 시작2");
        Commanddata.child("question").push().setValue("네비 시작3");

        Commanddata.child("awnser").push().setValue("네비게이션을 원격으로 켜놓을게요! 이제부터 주소 입력이 가능해요!");
        Commanddata.child("awnser").push().setValue("네비게이션을 원격으로 켜놨어. 주소 입력이 가능할꺼야.");
        Commanddata.child("awnser").push().setValue("네비를 원격으로 켜놨으니 확인해보는");

        Commanddata.child("question").push().setValue("네비 종료");
        Commanddata.child("question").push().setValue("네비 종료2");
        Commanddata.child("question").push().setValue("네비 종료3");

        Commanddata.child("awnser").push().setValue("네비게이션을 원격으로 껐어요. 모든 네비기능이 종료되었어요~");
        Commanddata.child("awnser").push().setValue("네비게이션을 원격으로 껐으니 모든 네비기능이 종료되었을꺼야.");
        Commanddata.child("awnser").push().setValue("네비게이션을 원격으로 꺼놨으니 확인해보는");




        //Commanddata.child("awnser").push().setValue("시간이 소요 될 듯 싶어요. 가신다면 천천히 가셔야 해요!");
        //Commanddata.child("awnser").push().setValue("정도 걸리네 갈꺼야? 그럼 조심히 갔다와");
        //Commanddata.child("awnser").push().setValue("시간이 걸릴듯 하니 조심히 갔다오는");


        //Systemdata.child("question").push().setValue("즐겨찾기 추가");
        //Systemdata.child("question").push().setValue("즐겨찾기 추가 2");
        //Systemdata.child("question").push().setValue("즐겨찾기 삭제");
        //Systemdata.child("question").push().setValue("즐겨찾기 삭제2");
        //Systemdata.child("question").push().setValue("취소");

        //Systemdata.child("awnser").push().setValue("즐겨찾기에 추가하실 주소를 알려주시면 리스트를 알려드리겠습니다.");
        //Systemdata.child("awnser").push().setValue("즐겨찾기에 추가되었습니다.");
        //Systemdata.child("awnser").push().setValue("즐겨찾기에 등록되어 있는 리스트에요. 삭제하실 내용을 숫자로 알려주세요.");
        //Systemdata.child("awnser").push().setValue("삭제 되었습니다.");

        //QAdata.child("호시마치 스이세이").child("awnser").push().setValue("사쿠라 미코");
        //QAdata.child("아카이 하아토").child("awnser").push().setValue("사쿠라 미코");
        //QAdata.child("시라카미 후부키").child("awnser").push().setValue("이번만 봐줄게요. 알겠죠?");

        //QAdata.child("사쿠라 미코").child("question").push().setValue("차단 해제");
        //QAdata.child("사쿠라 미코").child("awnser").push().setValue("담엔 안 봐준다 니에.");

        //iftttData.push().setValue("car_open");
        //iftttData.push().setValue("car_close");
        //iftttData.push().setValue("car_load");
        //iftttData.push().setValue("door_open");

    }

    private void first_load() {
        Handler loading_Handler = new Handler();
        loading_Handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (first_count <= 0) {
                    first_count = 99;
                    System.out.println("카운트 멈췄음 그리고 first_count 값 : " + first_count);
                    return;
                }
                System.out.println("현재 접속 카운트 값 : " + first_count);

                first_count--;

                loading_Handler.postDelayed(this, 1000);
            }
        }, 0);

    }

    private void serverDataLoad() {
        memberListData.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("멤버 DB 데이터 추가됨");
                //서버에 저장된 데이터 불러오기
                array_member.clear();
                member_count = 0;
                for (DataSnapshot memberchild : snapshot.getChildren()) {
                    array_member.add(String.valueOf(memberchild.getValue()));
                    System.out.println("멤버 리스트 호출중 " + array_member.get(member_count));
                    member_count++;
                }


                QAdata.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        array_answer.clear();
                        array_question.clear();

                        System.out.println("QA DB 데이터 추가됨");
                        //서버에 저장된 데이터 불러오기
                        for (DataSnapshot Qchild : snapshot.child(array_member.get(member_id)).child("awnser").getChildren()) { // 이름으로 소환했네 이제보니
                            array_answer.add(String.valueOf(Qchild.getValue()));
                            System.out.println("A 호출중 : " + array_answer.get(qa_count));
                            qa_count++;

                        }

                        qa_count = 0;

                        for (DataSnapshot Achild : snapshot.child(array_member.get(member_id)).child("question").getChildren()) {
                            array_question.add(String.valueOf(Achild.getValue()));
                            System.out.println("Q 호출중 : " + array_question.get(qa_count));
                            qa_count++;
                        }
                        qa_count = 0;

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.out.println("백업 DB 처리중 오류");
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("백업 DB 처리중 오류");
            }
        });

        //시스템 데이터 소환 작업.
        Systemdata.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("시스템 DB 데이터 추가됨");
                //서버에 저장된 데이터 불러오기
                array_system_answer.clear();
                array_system_question.clear();
                SQ_count = 0;
                SA_count = 0;

                for (DataSnapshot SQchild : snapshot.child("question").getChildren()) {
                    array_system_question.add(String.valueOf(SQchild.getValue()));
                    System.out.println("시스템 질문 리스트 호출중 : " + array_system_question.get(SQ_count));
                    SQ_count++;
                }

                for (DataSnapshot SAchild : snapshot.child("awnser").getChildren()) {
                    array_system_answer.add(String.valueOf(SAchild.getValue()));
                    System.out.println("시스템 응답 리스트 호출중 : " + array_system_answer.get(SA_count));
                    SA_count++;
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("시스템 데이터 처리중 오류");
            }
        });


        //유튜브 토큰 데이터 소환작업
        Tokendata.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("토큰 DB 데이터 추가됨");
                //서버에 저장된 데이터 불러오기
                array_token.clear();
                Tk_count = 0;


                for (DataSnapshot TKchild : snapshot.getChildren()) {
                    array_token.add(String.valueOf(TKchild.getValue()));
                    System.out.println("토큰 리스트 호출중 : " + array_token.get(Tk_count));
                    Tk_count++;
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("시스템 데이터 처리중 오류");
            }
        });

        //커맨드 데이터 소환 작업.
        Commanddata.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("커맨드 DB 데이터 추가됨");
                //서버에 저장된 데이터 불러오기
                array_command_answer.clear();
                array_command_question.clear();
                CDQ_count = 0;
                CDA_count = 0;

                for (DataSnapshot CDQchild : snapshot.child("question").getChildren()) {
                    array_command_question.add(String.valueOf(CDQchild.getValue()));
                    System.out.println("커맨드 질문 리스트 호출중 : " + array_command_question.get(CDQ_count));
                    CDQ_count++;
                }

                for (DataSnapshot CDAchild : snapshot.child("awnser").getChildren()) {
                    array_command_answer.add(String.valueOf(CDAchild.getValue()));
                    System.out.println("커맨드 응답 리스트 호출중 : " + array_command_answer.get(CDA_count));
                    CDA_count++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("커맨드 QA 데이터 처리중 오류");
            }
        });

        //말투 데이터 소환
        SpeakingData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("말투 DB 데이터 추가됨");
                //서버에 저장된 데이터 불러오기
                array_speaking.clear();
                SK_count = 0;

                for (DataSnapshot SKchild : snapshot.getChildren()) {
                    array_speaking.add(String.valueOf(SKchild.getValue()));
                    System.out.println("말투 리스트 호출중 : " + array_speaking.get(SK_count));
                    SK_count++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("말투 데이터 처리중 오류");
            }
        });

        //ifttt 데이터 소환
        iftttData.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("ifttt DB 데이터 추가됨");
                //서버에 저장된 데이터 불러오기
                array_ifttt.clear();
                ifttt_count = 0;
                for (DataSnapshot iftttchild : snapshot.getChildren()) {
                    array_ifttt.add(String.valueOf(iftttchild.getValue()));
                    System.out.println("ifttt 리스트 호출중 " + array_ifttt.get(ifttt_count));
                    ifttt_count++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("백업 DB 처리중 오류");
            }
        });

        //gps 데이터 소환
        gpsData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                System.out.println("이게 왜 진입하는지 확인이 안되네");

                String poi_address = null;

                Double poi_latitude = 0.0;
                Double poi_longtitude = 0.0;

                for (DataSnapshot Latchild : snapshot.child("latitude").getChildren()) { // 이름으로 소환했네 이제보니
                    poi_latitude = Double.parseDouble(String.valueOf(Latchild.getValue()));
                }

                for (DataSnapshot Latchild : snapshot.child("longtitude").getChildren()) { // 이름으로 소환했네 이제보니
                    poi_longtitude = Double.parseDouble(String.valueOf(Latchild.getValue()));
                }

                for (DataSnapshot Latchild : snapshot.child("address").getChildren()) { // 이름으로 소환했네 이제보니
                    poi_address = String.valueOf(Latchild.getValue());
                }

                if (poi_latitude == 0.0 || poi_longtitude == 0.0 && system_mode != Constants.command_poi) {
                    System.out.println("0값 쓰레기값 나와서 캔슬");
                }
                else {

                    System.out.println("latitude : " + poi_latitude + "\nlongtitude : " + poi_longtitude + "\naddress : " + poi_address);
                    save_latitude = poi_latitude;
                    save_longtitude = poi_longtitude;
                    save_address = poi_address;
                    //poi 동작시킨다.
                    //여기에 답변리스트 보내도록 하면된다.
                    gpsData.removeValue();
                    System.out.println("데이터 삭제 완료");
                }


            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("백업 DB 처리중 오류");
            }
        });

    }

    private void bootMsgLoad(int bootChangeHello) {

        QAdata.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                array_answer.clear();
                array_question.clear();

                System.out.println("QA DB 데이터 추가됨");
                //서버에 저장된 데이터 불러오기
                for (DataSnapshot Qchild : snapshot.child(array_member.get(member_id)).child("awnser").getChildren()) { // 이름으로 소환했네 이제보니
                    array_answer.add(String.valueOf(Qchild.getValue()));
                    System.out.println("A 호출중 : " + array_answer.get(qa_count));
                    qa_count++;

                }

                qa_count = 0;

                for (DataSnapshot Achild : snapshot.child(array_member.get(member_id)).child("question").getChildren()) {
                    array_question.add(String.valueOf(Achild.getValue()));
                    System.out.println("Q 호출중 : " + array_question.get(qa_count));
                    qa_count++;
                }
                qa_count = 0;

                //멤버 바뀐 이후 안녕인사하는곳
                if (bootChangeHello == Constants.member_changeEnable) {
                    System.out.println("왜 일로 자꾸 들어와지는거지??");
                    System.out.println("멤버 체인지의 값을 체크한다. : " + memberChange);
                    String name = array_member.get(member_id);
                    String msg = array_answer.get(Constants.answer_who);
                    send_Message(name, msg);
                    send_Push(msg);
                    memberChange = Constants.member_changeDisable;
                }
                else if (bootChangeHello == Constants.question_boot_hello) {
                    Date nowDate = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH", Locale.KOREAN);
                    int toDayHH = Integer.parseInt(simpleDateFormat.format(nowDate));
                    System.out.println("현재 시간 (멤버 교체후) : " + toDayHH);
                    System.out.println("어레이 계산(멤버 교체후) : " + (toDayHH / 6 + 3));
                    String name = array_member.get(member_id);
                    String msg = array_answer.get(toDayHH / 6 + 3); // 시간에 따른 데이터 최적화
                    send_Message(name, msg);
                    send_Push(msg);

                    //스레드 처리를 하겠습니다
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            youtube_hello_check(member_id);
                        }
                    }).start();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("백업 DB 처리중 오류");
            }
        });
    }


    private void messageDataLoad() {
        //데이터들을 추가, 변경, 제거, 이동, 취소
        messageData.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                System.out.println("DB 데이터 추가됨");


                //어댑터에 DTO추가
                Chat chat = snapshot.getValue(Chat.class);
                ((ChatAdapter) adapter).addChat(chat);


                if (first_count == 99) {
                    //기본 모드
                    if (system_mode == Constants.system_disable) {
                        System.out.println(" 이후는 채팅의 데이터 내용 : " + chat.getMsg());

                        String name;
                        String msg;

                        //기본 대화 추적
                        for (int list = 0; list < array_question.size(); list++) {
                            if (chat.getMsg().contains(array_question.get(list)) && chat.getName().contains("멜리사J") && message_lock == Constants.lock_disable && system_mode == Constants.system_disable) {

                                System.out.println("받는데 성공했다");
                                name = array_member.get(member_id);


                                //안녕 했을때 시간 판단하여 인사하는 시스템
                                if (list == Constants.question_hello || list == Constants.question_boot_hello) {

                                    if (list == Constants.question_boot_hello) {
                                        while (true) {
                                            //여기에 멤버리스트 이용하여 랜덤변수를 넣도록 한다.
                                            Random random = new Random(); // 랜덤 객체 생성
                                            random.setSeed(System.currentTimeMillis());
                                            member_id = random.nextInt(64); // 0부터 갯수임. 1,2 2개 이런식으로
                                            name = array_member.get(member_id);


                                            System.out.println("랜덤 함수 생성 : " + member_id);
                                            if (member_id != 0) {
                                                break;
                                            }
                                        }

                                        bootMsgLoad(list);
                                        return;

                                    }
                                    else {
                                        Date nowDate = new Date();
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH", Locale.KOREAN);
                                        int toDayHH = Integer.parseInt(simpleDateFormat.format(nowDate));
                                        System.out.println("현재 시간 : " + toDayHH);
                                        System.out.println("어레이 계산 : " + (toDayHH / 6 + 3));
                                        msg = array_answer.get(toDayHH / 6 + 3); // 시간에 따른 데이터 최적화

                                        //스레드 처리를 하겠습니다
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                youtube_hello_check(member_id);
                                            }
                                        }).start();
                                    }


                                }

                                else if (list == Constants.question_Fword) {
                                    //심한말을 해버렸다.
                                    msg = array_answer.get(list);
                                    message_lock = Constants.lock_enable;

                                    message_lock_count();

                                }

                                else  {
                                    msg = array_answer.get(list);
                                }


                                //메세지와 푸시 보내는 장소
                                send_Message(name, msg);
                                send_Push(msg);
                                return;
                            }
                        }

                        //커맨드 대화 추적
                        for (int list = 0; list < array_command_question.size(); list++) {
                            if (chat.getMsg().contains(array_command_question.get(list)) && chat.getName().contains("멜리사J") && message_lock == Constants.lock_disable && system_mode == Constants.system_disable) {

                                System.out.println("받는데 성공했다");


                                if (list == Constants.command_streaming) { // 스트리밍 확인 모드
                                    system_mode = Constants.system_streaming;
                                    int memberSpeaking = chatting_speaking(member_id);
                                    name = array_member.get(member_id);
                                    System.out.println("현재 멤버 id 값은 : " + member_id);
                                    msg = array_command_answer.get(list + memberSpeaking) + array_speaking.get(member_id);
                                    //메세지와 푸시 보내는 장소
                                    send_Message(name, msg);
                                    send_Push(msg);
                                    return;
                                }


                                else if (list == Constants.command_poi) { // poi 커맨드


                                    int memberSpeaking = chatting_speaking(member_id);
                                    name = array_member.get(member_id);
                                    System.out.println("현재 멤버 id 값은1 : " + name);
                                    msg = array_command_answer.get(list + memberSpeaking) + array_speaking.get(member_id);


                                    //gps 데이터 사전로드
                                    system_mode = Constants.command_poi;
                                    gpsDataLoad(msg);

                                    return;
                                }

                                //ifttt 진입
                                else if (list >= Constants.command_iftttStart && list <= Constants.command_iftttEnd) {
                                    int iftttCommand = (list - 17) / 3;
                                    String iftttString = array_ifttt.get((iftttCommand));
                                    name = array_member.get(member_id);
                                    System.out.println("현재 멤버 id 값은 : " + member_id);
                                    network_trans(iftttString, list, name);
                                    return;
                                }

                                else if (list == Constants.command_naviStart) { // 네비 시작 시 대화

                                    int memberSpeaking = chatting_speaking(member_id);
                                    name = array_member.get(member_id);
                                    System.out.println("현재 멤버 id 값은 : " + member_id);
                                    msg = array_command_answer.get(list + memberSpeaking) + array_speaking.get(member_id);
                                    send_Message(name, msg);
                                    send_Push(msg);
                                    return;
                                }

                                else if (list == Constants.command_naviStop) { // 네비 시작 시 대화

                                    int memberSpeaking = chatting_speaking(member_id);
                                    name = array_member.get(member_id);
                                    System.out.println("현재 멤버 id 값은 : " + member_id);
                                    msg = array_command_answer.get(list + memberSpeaking) + array_speaking.get(member_id);
                                    send_Message(name, msg);
                                    send_Push(msg);
                                    return;
                                }



                            }
                        }

                    }


                    //시스템에 진입
                    for (int list = 0; list < array_system_question.size(); list++) {
                        if (chat.getMsg().contains(array_system_question.get(list)) && chat.getName().contains("멜리사J")) {

                            String name;
                            String msg;

                            //취소 ( 시스템, 일반 메뉴 둘다 )
                            if (chat.getMsg().contains(array_system_question.get(Constants.system_cancle)) && chat.getName().contains("멜리사J") && system_mode != Constants.system_enable) {

                                if (system_mode == Constants.command_poiResult || system_mode == Constants.system_streaming) {
                                    system_mode = Constants.system_disable;

                                    save_latitude = 0.0;
                                    save_longtitude = 0.0;

                                    des_lat = 0.0;
                                    des_long = 0.0;
                                    save_address = null;

                                    int memberSpeaking = chatting_speaking(member_id);

                                    msg = array_command_answer.get(Constants.command_cancle + memberSpeaking) + array_speaking.get(member_id);
                                    name = array_member.get(list); // 루시아 발렌타인


                                }
                                else {

                                    system_mode = Constants.system_enable;
                                    //가능한 모든 데이터를 전부 초기화 시킨다.
                                    editNumber = 0;
                                    add_edit_Answermsg = null;
                                    add_edit_Questionmsg = null;

                                    name = array_member.get(Constants.member_system); // 루시아 발렌타인
                                    msg = array_system_answer.get(list); // 취소
                                }

                                //메시지를 파이어베이스에 보냄.
                                send_Message(name, msg);
                                return; // 리턴 넣어야 for문 반복이 줄어든다.

                            }


                            //방송 상태 확인 모드
                            else if (chat.getMsg().contains(array_system_question.get(Constants.system_streaming)) && chat.getName().contains("멜리사J") && system_mode == Constants.system_enable) {

                                system_mode = Constants.system_streaming;
                                name = array_member.get(Constants.member_system); // 루시아 발렌타인
                                msg = array_system_answer.get(list); // 멤버 체인지 // 이거 리스트로 바꿔도 동작할텐데??
                                //메시지를 파이어베이스에 보냄.
                                send_Message(name, msg);
                                return; // 리턴 넣어야 for문 반복이 줄어든다.
                            }

                            //멤버 체인지 모드
                            else if (chat.getMsg().contains(array_system_question.get(Constants.system_memberChange)) && chat.getName().contains("멜리사J") && system_mode == Constants.system_enable) {
                                system_mode = Constants.system_memberChange;
                                //가능한 멤버리스트 표시 기능
                                //당연히 질문도 보내야겠지?
                                String merge_member = String.join("\n", array_member);
                                name = array_member.get(Constants.member_system); // 루시아 발렌타인
                                msg = array_system_answer.get(list) + "\n\n" + merge_member; // 멤버 체인지 // 이거 리스트로 바꿔도 동작할텐데??
                                //메시지를 파이어베이스에 보냄.
                                send_Message(name, msg);
                                return; // 리턴 넣어야 for문 반복이 줄어든다.
                            }

                            // 시스템 락 해제 모드 진입.
                            else if (chat.getMsg().contains(array_system_question.get(Constants.system_lockOff)) && chat.getName().contains("멜리사J")) {
                                System.out.println("시스템 락 해제 진입.");
                                system_mode = Constants.system_enable;
                                name = array_member.get(Constants.member_system); // 루시아 발렌타인
                                msg = array_system_answer.get(list);
                                //메시지를 파이어베이스에 보냄.
                                send_Message(name, msg);

                                name = array_member.get(member_id); // 루시아 발렌타인
                                msg = array_answer.get(Constants.answer_lockOff);
                                //메시지를 파이어베이스에 보냄.
                                send_Message(name, msg);

                                lock_Handler.removeMessages(0);
                                lock_Handler.removeCallbacksAndMessages(null);
                                lockCount = Constants.lock_10min;
                                message_lock = Constants.lock_disable;
                                return;
                            }


                            //대화 추가 모드 진입
                            else if (chat.getMsg().contains(array_system_question.get(Constants.system_talkAdd_question)) && chat.getName().contains("멜리사J") && system_mode == Constants.system_enable) {
                                System.out.println("대화 추가 모드 진입.");
                                system_mode = Constants.system_talkAdd_question;
                                name = array_member.get(Constants.member_system); // 루시아 발렌타인
                                msg = array_system_answer.get(list) + "\n" + array_member.get(member_id); // 이거 리스트로 바꿔도 동작할텐데??
                                //메시지를 파이어베이스에 보냄.
                                send_Message(name, msg);
                                return;
                            }

                            //대화 삭제 모드 진입
                            else if (chat.getMsg().contains(array_system_question.get(Constants.system_talkDelete_question)) && chat.getName().contains("멜리사J") && system_mode == Constants.system_enable) {
                                System.out.println("대화 삭제 모드 진입.");
                                system_mode = Constants.system_talkDelete_question;

                                ArrayList<String> array_system_talkDelete = new ArrayList<String>();

                                for (int qalist = 0; qalist < array_question.size(); qalist++) {
                                    array_system_talkDelete.add(qalist + ". Q : " + array_question.get(qalist) + "\n       A : " + array_answer.get(qalist) + "\n");
                                }

                                String merge_QA = String.join("\n", array_system_talkDelete);

                                // 여기에 이제 리스트 따른 기능 넣어야함. for문 권장

                                name = array_member.get(Constants.member_system); // 루시아 발렌타인
                                msg = array_system_answer.get(list) + array_member.get(member_id) + "\n\n" + merge_QA;
                                //메시지를 파이어베이스에 보냄.
                                send_Message(name, msg);
                                return;
                            }

                            //대화 수정 모드 진입
                            else if (chat.getMsg().contains(array_system_question.get(Constants.system_talkEdit)) && chat.getName().contains("멜리사J") && system_mode == Constants.system_enable) {
                                System.out.println("대화 삭제 모드 진입.");
                                system_mode = Constants.system_talkEdit;

                                ArrayList<String> array_system_talkDelete = new ArrayList<String>();

                                for (int qalist = 0; qalist < array_question.size(); qalist++) {
                                    array_system_talkDelete.add(qalist + ". Q : " + array_question.get(qalist) + "\n       A : " + array_answer.get(qalist) + "\n");
                                }

                                String merge_QA = String.join("\n", array_system_talkDelete);

                                // 여기에 이제 리스트 따른 기능 넣어야함. for문 권장

                                name = array_member.get(Constants.member_system); // 루시아 발렌타인
                                msg = array_system_answer.get(list) + array_member.get(member_id) + "\n\n" + merge_QA;
                                //메시지를 파이어베이스에 보냄.
                                send_Message(name, msg);
                                return;
                            }


                            //시스템 모드 종료
                            else if (chat.getMsg().contains(array_system_question.get(Constants.system_disable)) && chat.getName().contains("멜리사J") && system_mode == Constants.system_enable) {
                                System.out.println("홀로라이브 모드로 나감.");
                                system_mode = Constants.system_disable;
                                name = array_member.get(Constants.member_system); // 루시아 발렌타인
                                msg = array_system_answer.get(list); // 이거 리스트로 바꿔도 동작할텐데??
                                //메시지를 파이어베이스에 보냄.
                                send_Message(name, msg);
                                return;
                            }

                            // 시스템 모드 초기 진입 안내
                            else if (chat.getMsg().contains(array_system_question.get(Constants.system_enable)) && chat.getName().contains("멜리사J")) {
                                System.out.println("시스템 모드로 진입");
                                system_mode = Constants.system_enable;
                                name = array_member.get(Constants.member_system); // 루시아 발렌타인
                                msg = array_system_answer.get(list);
                                //메시지를 파이어베이스에 보냄.
                                send_Message(name, msg);
                                return;
                            }


                        }


                    }


                    answer_error(chat);

                    //멤버 교체 리스트 메뉴에 진입
                    member_change(chat);

                    talk_add(chat);

                    talk_delete(chat);

                    talk_edit(chat);

                    streaming_check(chat);

                    command_POI(chat);

                    navi_mode(chat);


                }

                else {
                    System.out.println("이전 채팅 데이터 내용 : " + chat.getMsg());
                    first_count = 1;
                }


                recyclerView.scrollToPosition(chatList.size() - 1);
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


    }

    private void gpsDataLoad(String msg) {
        //주소 작업 해야겠지?

        System.out.println("현재 멤버 id 값은2 : " + array_member.get(member_id));
        while (true) {
            if (save_address != null && save_latitude != 0.0 && save_longtitude != 0.0) {
                address_data_push(save_address, save_latitude, save_longtitude, array_member.get(member_id), msg);
                break;
            }
        }


    }


    private void send_Message(String name, String msg) {
        Chat messageChat = new Chat();
        messageChat.setName(name);
        messageChat.setMsg(msg);
        //메시지를 파이어베이스에 보냄.
        messageData.push().setValue(messageChat);
        chatText.setText("");
    }


    private void send_Push(String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // FMC 메시지 생성 start
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", msg);
                    notification.put("title", array_member.get(member_id));
                    root.put("notification", notification);
                    root.put("to", "스마트폰 고유 토큰 키 입력"); // 이걸건드리는거다 ㅅㅂ
                    // FMC 메시지 생성 end

                    URL Url = new URL(FCM_MESSAGE_URL);
                    HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.addRequestProperty("Authorization", "key=" + SERVER_KEY);
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Content-type", "application/json");
                    OutputStream os = conn.getOutputStream();
                    os.write(root.toString().getBytes("utf-8"));
                    os.flush();
                    conn.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void message_lock_count() {

        lock_Handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                lockCount--;
                System.out.println("락카운트 남은 시간 : " + lockCount);

                if (lockCount <= 0) {
                    lock_Handler.removeMessages(0);
                    lock_Handler.removeCallbacksAndMessages(null);
                    lockCount = Constants.lock_10min;
                    message_lock = Constants.lock_disable;
                    return;
                }
                lock_Handler.postDelayed(this, 1000);
            }
        }, 0);
    }

    private void member_change(Chat chat) {
        for (int member = 0; member < array_member.size(); member++) {
            if (system_mode == Constants.system_memberChange && chat.getMsg().contains(array_member.get(member)) && chat.getName().contains("멜리사J")) {
                System.out.println("멤버가 교체됩니다.");
                String name;
                String msg;

                name = array_member.get(Constants.member_system);
                msg = array_system_answer.get(4) + "\n" + array_member.get(member_id) + " -> " + array_member.get(member);
                send_Message(name, msg);

                name = array_member.get(member_id);
                msg = array_answer.get(Constants.answer_goodbye); // 1 : goodbye
                send_Message(name, msg);
                send_Push(msg);

                member_id = member; //멤버 교체 성공
                memberChange = Constants.member_changeEnable;
                bootMsgLoad(Constants.member_changeEnable);

                system_mode = Constants.system_enable;
                return;
            }
        }
    }

    private void answer_error(Chat chat) {
        if (chat.getName().contains("멜리사J")) {
            String name;
            String msg;
            if (system_mode == Constants.system_enable) { // 시스템 모드를 하나만 쓸수 있는 상태가아님.
                // 이해 불가능한 무언가를 적어버림.
                System.out.println("시스템에서 뭔말인지 이해못했다.");
                name = array_member.get(Constants.member_system);
                msg = array_system_answer.get(Constants.system_error);

                //메시지를 파이어베이스에 보냄.
                send_Message(name, msg);
                system_mode = Constants.system_enable;


            }

            else if (message_lock == Constants.lock_disable && system_mode == Constants.system_disable) { // 락이 안걸렸으니 대화가 가능하다.
                System.out.println("뭔말인지 이해못했다.");

                name = array_member.get(member_id);
                msg = array_answer.get(Constants.answer_error);

                //메시지를 파이어베이스에 보냄.
                send_Message(name, msg);
                send_Push(msg);
                //메시지를 푸시로 보냄
                send_Push(array_answer.get(Constants.answer_error));
            }

            else if (message_lock == Constants.lock_enable) { // 심한말해서 락걸림
                name = array_member.get(Constants.member_system);
                msg = array_system_answer.get(Constants.system_lock) + "\n앞으로 " + lockCount / 60 + "분 " + lockCount % 60 + "초 동안 대화가 불가능 해요.";

                //메시지를 파이어베이스에 보냄.
                send_Message(name, msg);
                send_Push(msg);
            }


        }
    }

    private void talk_add(Chat chat) {
        String name;
        String msg;

        if (system_mode == Constants.system_talkAdd_question && chat.getName().contains("멜리사J")) {
            System.out.println("질문 등록하는 부분 입니다.");

            add_edit_Questionmsg = chat.getMsg();

            name = array_member.get(Constants.member_system);
            msg = array_system_answer.get(Constants.system_talkAdd_answer) + array_member.get(member_id);
            send_Message(name, msg);

            system_mode = Constants.system_talkAdd_answer;

        }

        else if (system_mode == Constants.system_talkAdd_answer && chat.getName().contains("멜리사J")) {
            System.out.println("답변 등록하는 부분입니다.");

            add_edit_Answermsg = chat.getMsg();

            // 여기에 질문 대답 추가하는 어레이 추가 하고 데이터 삭제와 재갱신 요청함.

            array_question.add(add_edit_Questionmsg);
            array_answer.add(add_edit_Answermsg);

            QAdata.child(array_member.get(member_id)).removeValue();

            for (int list = 0; list < array_question.size(); list++) {
                QAdata.child(array_member.get(member_id)).child("question").push().setValue(array_question.get(list));
                QAdata.child(array_member.get(member_id)).child("awnser").push().setValue(array_answer.get(list));
            }


            serverDataLoad();


            name = array_member.get(Constants.member_system);
            msg = array_system_answer.get(Constants.system_talkAdd_ok);
            send_Message(name, msg);

            add_edit_Answermsg = null;
            add_edit_Questionmsg = null;

            system_mode = Constants.system_enable;

        }
    }

    private void talk_delete(Chat chat) {
        if (system_mode == Constants.system_talkDelete_question && chat.getName().contains("멜리사J")) {

            String name;
            String msg;

            int removeNumber = Integer.parseInt(chat.getMsg());

            if (removeNumber <= Constants.system_importantTalk) {
                // 시스템 대화라 차단해야함.
                name = array_member.get(Constants.member_system);
                msg = array_system_answer.get(Constants.system_talkDelete_denied);
                send_Message(name, msg);
            }
            else {

                array_question.remove(removeNumber);
                array_answer.remove(removeNumber);

                QAdata.child(array_member.get(member_id)).removeValue();

                for (int list = 0; list < array_question.size(); list++) {
                    QAdata.child(array_member.get(member_id)).child("question").push().setValue(array_question.get(list));
                    QAdata.child(array_member.get(member_id)).child("awnser").push().setValue(array_answer.get(list));
                }

                serverDataLoad();

                name = array_member.get(Constants.member_system);
                msg = array_system_answer.get(Constants.system_talkDelete_answer);
                send_Message(name, msg);

                system_mode = Constants.system_enable;
            }

        }
    }

    private void talk_edit(Chat chat) {
        String name;
        String msg;
        if (system_mode == Constants.system_talkEdit && chat.getName().contains("멜리사J")) {

            int EditNumber = Integer.parseInt(chat.getMsg());

            if (EditNumber <= Constants.system_importantTalk) { // 번호가 너무 작아서 들어가는 장소
                // 시스템 대화라 차단해야함.
                name = array_member.get(Constants.member_system);
                msg = array_system_answer.get(Constants.system_talkDelete_denied);

            }
            else { // 질문 적으라는 시스템 구성
                system_mode = Constants.system_talkEdit_question;
                editNumber = EditNumber;

                name = array_member.get(Constants.member_system);
                msg = array_system_answer.get(Constants.system_talkEdit_question);
            }
            send_Message(name, msg);
        }
        else if (system_mode == Constants.system_talkEdit_question && chat.getName().contains("멜리사J")) { // 질문 적는 구간
            system_mode = Constants.system_talkEdit_answer;
            add_edit_Questionmsg = chat.getMsg();
            name = array_member.get(Constants.member_system);
            msg = array_system_answer.get(Constants.system_talkEdit_answer);
            send_Message(name, msg);
        }
        else if (system_mode == Constants.system_talkEdit_answer && chat.getName().contains("멜리사J")) { // 답변 적는 구간

            add_edit_Answermsg = chat.getMsg();

            array_question.set(editNumber, add_edit_Questionmsg);
            array_answer.set(editNumber, add_edit_Answermsg);

            QAdata.child(array_member.get(member_id)).removeValue();

            for (int list = 0; list < array_question.size(); list++) {
                QAdata.child(array_member.get(member_id)).child("question").push().setValue(array_question.get(list));
                QAdata.child(array_member.get(member_id)).child("awnser").push().setValue(array_answer.get(list));
            }

            serverDataLoad();

            name = array_member.get(Constants.member_system);
            msg = array_system_answer.get(Constants.system_talkEdit_complete);
            send_Message(name, msg);

            system_mode = Constants.system_enable;
            editNumber = 0;
            add_edit_Answermsg = null;
            add_edit_Questionmsg = null;
        }
    }

    private void streaming_check(Chat chat) {

        if (system_mode == Constants.system_streaming && chat.getName().contains("멜리사J")) {
            String streaming = chat.getMsg();
            System.out.println("성공적으로 스트리밍 체크에 들어옴");

            array_stream_list.clear();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String name;
                    String msg;

                    for (int list = 0; list < array_member.size(); list++) {
                        if (streaming.contains(array_member.get(list))) {
                            //해당 요청 사항으로만 진행한다.
                            youtube_stream_check(list);
                        }
                    }


                    if (streaming.contains("전체")) {


                        //시스템이 아닌 커맨드 스트리밍
                        name = array_member.get(member_id);
                        int memberSpeaking = chatting_speaking(member_id);
                        msg = array_command_answer.get(3 + memberSpeaking) + array_speaking.get(member_id);


                        send_Message(name, msg);
                        send_Push(msg);


                        array_stream_list.add("<홀로JP 0기생>\n");
                        // for문으로 진행한다.


                        for (int member = 1; member < array_member.size(); member++) {
                            name = array_member.get(member_id);
                            if (member == 6) {
                                array_stream_list.add("\n\n<홀로JP 1기생>\n");

                                msg = "<홀로JP 0기생> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }
                            else if (member == 11) {
                                array_stream_list.add("\n\n<홀로JP 2기생>\n");


                                msg = "<홀로JP 1기생> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 16) {
                                array_stream_list.add("\n\n<홀로JP 게이머즈>\n");

                                msg = "<홀로JP 2기생> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 19) {
                                array_stream_list.add("\n\n<홀로JP 3기생 - Hololive Fantasy>\n");

                                msg = "<홀로JP 게이머즈> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 23) {
                                array_stream_list.add("\n\n<홀로JP 4기생 - holoForce>\n");

                                msg = "<홀로JP 3기생 - Hololive Fantasy> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 27) {
                                array_stream_list.add("\n\n<홀로JP 5기생 - holoFive>\n");

                                msg = "<홀로JP 4기생 - holoForce> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 31) {
                                array_stream_list.add("\n\n<홀로JP 비밀결사 - holoX>\n");

                                msg = "<홀로JP 5기생 - holoFive> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }


                            else if (member == 36) {
                                array_stream_list.add("\n\n<홀로EN : Myth>\n");

                                msg = "<홀로JP 비밀결사 - holoX> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 41) {
                                array_stream_list.add("\n\n<Project : HOPE>\n");

                                msg = "<홀로EN : Myth> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 42) {
                                array_stream_list.add("\n\n<홀로EN : Council>\n");

                                msg = "<Project : HOPE> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }


                            else if (member == 46) {
                                array_stream_list.add("\n\n<홀로 ID : 1기생>\n");

                                msg = "<홀로EN : Council> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 49) {
                                array_stream_list.add("\n\n<홀로 ID : 2기생>\n");

                                msg = "<홀로 ID : 1기생> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 52) {
                                array_stream_list.add("\n\n<홀로ID : 3기생>\n");

                                msg = "<홀로 ID : 2기생> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 55) {
                                array_stream_list.add("\n\n<홀로EN : ADVENT>\n");
                                msg = "<홀로 ID : 3기생> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == 59) {
                                array_stream_list.add("\n\n<홀로 DEV_IS>\n");
                                msg = "<홀로 EN : ADVENT> 로딩 완료" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            else if (member == array_member.size() - 1) {
                                msg = "<홀로 DEV_IS> 로딩 완료 및 정리중" + " (" + 100 * member / array_member.size() + "%)";
                                send_Message(name, msg);
                            }

                            youtube_stream_check(member);
                        }

                    }

                    String merge_member = String.join("\n", array_stream_list);


                    name = array_member.get(member_id);
                    int memberSpeaking = chatting_speaking(member_id);
                    msg = array_command_answer.get(6 + memberSpeaking) + array_speaking.get(member_id) + "\n\n\n" + merge_member;


                    system_mode = Constants.system_disable;


                    send_Message(name, msg);
                    send_Push(msg);

                }
            }).start();
        }
    }

    private void command_POI(Chat chat) {

        if (system_mode == Constants.command_poiResult && chat.getName().contains("멜리사J")) {

            int POINumber = Integer.parseInt(chat.getMsg());

            //poinumber 이용해서 스레드 처리해서 전송 하면된다 니에.

            new Thread(() -> {


                des_lat = Double.parseDouble(array_POI_latitude.get(POINumber));
                des_long = Double.parseDouble(array_POI_longtitude.get(POINumber));


                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\"tollgateFareOption\":16,\"roadType\":32,\"directionOption\":1,\"endX\":\"" + des_long + "\",\"endY\":\"" + des_lat + "\",\"endRpFlag\":\"G\",\"reqCoordType\":\"WGS84GEO\",\"startX\":\"" + save_longtitude + "\",\"startY\":\"" + save_latitude + "\",\"uncetaintyP\":1,\"uncetaintyA\":1,\"uncetaintyAP\":1,\"carType\":0,\"gpsInfoList\":\"126.939376564495,37.470947057194365,120430,20,50,5,2,12,1_126.939376564495,37.470947057194365,120430,20,50,5,2,12,1\",\"detailPosFlag\":\"2\",\"resCoordType\":\"WGS84GEO\",\"sort\":\"index\",\"totalValue\":2}");
                Request request = new Request.Builder().url("https://apis.openapi.sk.com/tmap/routes?version=1&callback=function").post(body).addHeader("accept", "application/json").addHeader("content-type", "application/json").addHeader("appKey", "티맵 api 전용 키 입력").build();

                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String message = null;
                try {
                    message = response.body().string();
                } catch (IOException e) {
                    System.out.println("리스폰스 값 자체가 null 화 되어버림2");
                    return;
                }
                System.out.println("네비 파싱전 메시지 : " + message);


                poi_parse(message, POINumber);


            }).start();


        }
    }

    private void poi_parse(String message, int POINumber) {

        String msg;
        String name;

        JSONObject original_JSON;
        try {
            original_JSON = new JSONObject(message); //전체 배열 가져오기 중복 없음!
            System.out.println("original_JSON 정리 : " + original_JSON);
        } catch (NullPointerException | JSONException e) {
            System.out.println("original_JSON 에서 null 값 발생으로 인한 리턴");
            return;
        }

        JSONArray matchedPoints_JSON = original_JSON.optJSONArray("features"); // 전체 배열 다음 다음 배열

        //배열 검증용?
        for (int i = 0; i < matchedPoints_JSON.length(); i++) {
            JSONObject array_json = matchedPoints_JSON.optJSONObject(i); // 전체 배열 다음 다음 배열


            JSONObject properties = array_json.optJSONObject("properties"); // 전체 배열 중 resultdata 아래 있는 데이터 가져오기) 중복 없음!

            int totaltime = Integer.parseInt(properties.optString("totalTime", ""));

            name = array_member.get(member_id);

            int memberSpeaking = chatting_speaking(member_id);

            if (totaltime / 3600 == 0) {
                msg = array_POI_name.get(POINumber) + " 까지 " + totaltime / 60 + "분 " + array_command_answer.get(Constants.command_poiResult + memberSpeaking) + array_speaking.get(member_id) + "?";
            }
            else if (totaltime % 3600 / 60 == 0) {
                msg = array_POI_name.get(POINumber) + " 까지 " + totaltime / 3600 + "시간 " + array_command_answer.get(Constants.command_poiResult + memberSpeaking) + array_speaking.get(member_id) + "?";
            }
            else {
                msg = array_POI_name.get(POINumber) + " 까지 " + totaltime / 3600 + "시간 " + totaltime % 3600 / 60 + "분 " + array_command_answer.get(Constants.command_poiResult + memberSpeaking) + array_speaking.get(member_id) + "?";
            }

            system_mode = Constants.command_navi_question;

            send_Message(name, msg);
            send_Push(msg);


        }
    }

    private void youtube_stream_check(int stream_member) {


        System.out.println("여기 들어온거 맞지?");
        String channelid = array_token.get(stream_member); // REPLACE WITH YOUR CHANNEL ID

        try {
            URL url = new URL("https://www.youtube.com/channel/" + channelid);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            if (response.toString().contains("hqdefault_live.jpg")) {
                array_stream_list.add(array_member.get(stream_member) + " : 방송 중");
                System.out.println("방송중 확인완료");
            }
            else {
                array_stream_list.add(array_member.get(stream_member) + " : 방송 종료");
                System.out.println("방송아님 확인완료");
            }
        } catch (IOException e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }

    private void youtube_hello_check(int hello_member) {
        System.out.println("여기 들어온거 맞지?");
        String channelid = array_token.get(hello_member); // REPLACE WITH YOUR CHANNEL ID

        String name = array_member.get(hello_member);
        String msg = array_answer.get(Constants.answer_helloStreaming);


        try {
            URL url = new URL("https://www.youtube.com/channel/" + channelid);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            if (response.toString().contains("hqdefault_live.jpg")) {

                send_Message(name, msg);
                send_Push(msg);
                System.out.println("방송중 확인완료");
            }
            else {
                System.out.println("방송아님 확인완료");
            }
        } catch (IOException e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }

    public void address_data_push(String address, Double latitude, Double longtitude, String member_name, String msg) {

        try {
            address = URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        // 여기에 주표 파싱용 필요함.
        String finalAddress = address;

        new Thread(new Runnable() {
            @Override
            public void run() {

                //long , lat 순으로 적어야한다.
                //쉼표 변환은 %2C , | 변환은 %7C를 사용한다.

                OkHttpClient navi_client = new OkHttpClient();
                Request poi_request = new Request.Builder().url("https://apis.openapi.sk.com/tmap/pois?version=1&searchKeyword=" + finalAddress + "&searchType=all&searchtypCd=A&centerLon=" + longtitude + "&centerLat=" + latitude + "&reqCoordType=WGS84GEO&resCoordType=WGS84GEO&radius=0&page=1&count=10&multiPoint=N&poiGroupYn=N").get().addHeader("Accept", "application/json").addHeader("appKey", "티맵 전용 api 키 입력").build();
                System.out.println("왜 에러가 갑자기 생겼는지 조사 시작한다 : " + "https://apis.openapi.sk.com/tmap/pois?version=1&searchKeyword=" + finalAddress + "&searchType=all&searchtypCd=A&centerLon=" + longtitude + "&centerLat=" + latitude + "&reqCoordType=WGS84GEO&resCoordType=WGS84GEO&radius=0&page=1&count=10&multiPoint=N&poiGroupYn=N");

                Response poi_response = null;
                try {
                    poi_response = navi_client.newCall(poi_request).execute();
                } catch (IOException e) {
                    System.out.println("리스폰스 값 자체가 null 화 되어버림1"); // 이게 진짜임
                    return;
                }

                String poi_message = null;
                try {
                    poi_message = poi_response.body().string();
                } catch (IOException e) {
                    System.out.println("리스폰스 값 자체가 null 화 되어버림2");
                    return;
                }
                System.out.println("네비 파싱전 메시지 : " + poi_message);

                String poi_finalMessage = poi_message;

                try {
                    System.out.println("현재 멤버 id 값은3 : " + member_name);
                    JSONParse_POI(poi_finalMessage, member_name, msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void JSONParse_POI(String finalMessage, String member_name, String msg) throws JSONException {
        JSONObject original_JSON;


        try {
            original_JSON = new JSONObject(finalMessage); //전체 배열 가져오기 중복 없음!
            System.out.println("original_JSON 정리 : " + original_JSON);
        } catch (NullPointerException | JSONException e) {
            System.out.println("original_JSON 에서 null 값 발생으로 인한 리턴");
            return;
        }

        JSONObject searchPoiInfo = original_JSON.optJSONObject("searchPoiInfo"); // 전체 배열 중 resultdata 아래 있는 데이터 가져오기) 중복 없음!

        if (original_JSON == null) { // null 값 검증
            System.out.println("original_JSON 0값 발생했음 ");
            return;
        }

        JSONObject pois = searchPoiInfo.optJSONObject("pois"); // 전체 배열 중 resultdata 아래 있는 데이터 가져오기) 중복 없음!

        if (searchPoiInfo == null) { // null 값 검증
            System.out.println("searchPoiInfo 0값 발생했음 ");
            return;
        }

        JSONArray poi = pois.optJSONArray("poi"); // 전체 배열 다음 다음 배열

        if (poi == null) { // null 값 검증
            System.out.println("poi 0값 발생했음 ");
            return;
        }


        String name[] = new String[poi.length()];
        String upperAddrName[] = new String[poi.length()];
        String middleAddrName[] = new String[poi.length()];
        String lowerAddrName[] = new String[poi.length()];
        String totalAddrName[] = new String[poi.length()];
        String frontLat[] = new String[poi.length()];
        String frontLon[] = new String[poi.length()];


        array_POI_name.clear();
        array_POI_address.clear();
        array_POI_latitude.clear();
        array_POI_longtitude.clear();

        for (int i = 0; i < poi.length(); i++) {
            System.out.println(i + " 배열의 값 : " + poi);
            JSONObject array_json = poi.optJSONObject(i); // 배열로했던 장소중 I 의 배열을 참조
            name[i] = array_json.getString("name"); //좌표값 찾기
            upperAddrName[i] = array_json.optString("upperAddrName", "."); //좌표값 찾기
            middleAddrName[i] = array_json.optString("middleAddrName", "."); //좌표값 찾기
            lowerAddrName[i] = array_json.optString("lowerAddrName", "."); //좌표값 찾기
            frontLat[i] = array_json.optString("frontLat", "0");
            frontLon[i] = array_json.optString("frontLon", "0");
            totalAddrName[i] = (upperAddrName[i] + " " + middleAddrName[i] + " " + lowerAddrName[i]);

            System.out.println("중요 주소만 표시 : " + name[i]);
            System.out.println("모든 주소만 표시 : " + totalAddrName[i]);
            System.out.println("해당 좌표 : " + frontLat[i] + " " + frontLon[i]);


            array_POI_name.add(name[i]);
            array_POI_address.add(totalAddrName[i]);
            array_POI_latitude.add(frontLat[i]);
            array_POI_longtitude.add(frontLon[i]);
        }

        //머지 작업이 필요해요.

        String mergeList = msg + "\n";

        for (int merge = 0; merge < array_POI_name.size(); merge++) {
            mergeList = mergeList + "\n" + merge + ". " + array_POI_name.get(merge) + "\n" + array_POI_address.get(merge) + "\n";
        }

        System.out.println("최종적으로 도달하는 멤버의 네임은? : " + member_name);
        send_Message(member_name, mergeList);
        send_Push(mergeList);

        system_mode = Constants.command_poiResult;


    }

    private void navi_mode(Chat chat) {
        String msg = null;

        if (system_mode == Constants.command_navi_question && chat.getName().contains("멜리사J")) {

            for (int list = 0; list < array_command_question.size(); list++) {

                if (list == Constants.command_navi_yes) {
                    // 네비 전송 시작

                    //save_lat이아니라 다른 변수를 따로만드는걸 추천할게 23-09-06
                    naviData.child("latitude").push().setValue(des_lat);
                    naviData.child("longtitude").push().setValue(des_long);
                    int memberSpeaking = chatting_speaking(member_id);

                    msg = array_command_answer.get(Constants.command_navi_yes + memberSpeaking) + array_speaking.get(member_id);
                    save_latitude = 0.0;
                    save_longtitude = 0.0;
                    des_lat = 0.0;
                    des_long = 0.0;
                    save_address = null;
                    system_mode = Constants.system_disable;

                    String name = array_member.get(member_id); // 루시아 발렌타인

                    send_Message(name, msg);
                    send_Push(msg);

                    naviData.removeValue();

                    return;
                }
                else if (list == Constants.command_navi_no) {
                    int memberSpeaking = chatting_speaking(member_id);
                    msg = array_command_answer.get(Constants.command_navi_no + memberSpeaking) + array_speaking.get(member_id);
                    // 네비 전송 취소
                    save_latitude = 0.0;
                    save_longtitude = 0.0;
                    des_lat = 0.0;
                    des_long = 0.0;
                    save_address = null;
                    system_mode = Constants.system_disable;

                    String name = array_member.get(member_id); // 루시아 발렌타인

                    send_Message(name, msg);
                    send_Push(msg);

                    naviData.removeValue();

                    return;
                }

            }

        }

    }


    private void network_trans(String iftttString, int commandAnswer, String name) {


        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String url = "https://maker.ifttt.com/trigger/" + iftttString + "/json/with/key/" + ifttt_key;

                    System.out.println(url);


                    System.out.println("url 입력 전송중");

                    // OkHttp 클라이언트 객체 생성
                    OkHttpClient client = new OkHttpClient();

                    // GET 요청 객체 생성
                    Request.Builder builder = new Request.Builder().url(url).get();
                    Request request = builder.build();

                    // OkHttp 클라이언트로 GET 요청 객체 전송
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        // 응답 받아서 처리
                        ResponseBody body = response.body();
                        if (body != null) {
                            System.out.println("Response:" + body.string()); // 대답 받는 부위

                            //성공적으로 받았으면 여기에 입력한다.
                            //여기도 캐릭터 3특성 추가해야함.
                            String msg;

                            int memberSpeaking = chatting_speaking(member_id);
                            msg = array_command_answer.get(commandAnswer + memberSpeaking) + array_speaking.get(member_id);

                            send_Message(name, msg);
                            send_Push(msg);
                            //send_Push(array_answer.get(commandAnswer));
                            system_mode = Constants.system_disable;
                        }
                    }

                    else {
                        //성공적으로 못 받았으면 여기에 입력한다.
                        System.err.println("Error Occurred");
                        String msg;


                        int memberSpeaking = chatting_speaking(member_id);

                        msg = array_command_answer.get(15 + memberSpeaking) + array_speaking.get(member_id);

                        send_Message(name, msg);
                        send_Push(msg);
                        system_mode = Constants.system_disable;

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("이번엔 주소가 잘못되었나본데");
                    system_mode = Constants.system_disable;

                }
            }
        }).start();


    }

    private int chatting_speaking(int member_id) {

        for (int check = 0; check < speaking_rare.length; check++) {
            if (speaking_rare[check] == member_id) { // 특수 아이디면 리턴
                return 2;
            }
        }

        for (int check = 0; check < respect_speak.length; check++) {
            if (respect_speak[check] == member_id) { // 존댓말 아이디면 리턴
                return 0;
            }
        }

        //반말 캐릭인경우 for문 추후 처리해야함.
        return 1;


    }


}