package com.angelqinwen.chatpractice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity
{

    private ListView messageList;
    private ImageButton send;
    private WebSocket webSocket;
    private MessageAdapter adapter;
    private RelativeLayout activity_main;


    //Update Emoji...
    //Change origin EditText to EmojiconEditText in order to display emojis in TextView.

    //private EditText messageBox;
    private EmojiconEditText messageBox;
    private ImageView emojiButton;
    private EmojIconActions emojIconActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = findViewById(R.id.activity_main);

        messageList = findViewById(R.id.messageList);
        messageBox = findViewById(R.id.messageBox);
        send = findViewById(R.id.send);
        emojiButton = findViewById(R.id.emoji_button);
        //emojIconActions constructor have four parameters which is Context, RootView, EmojiconEditText and ImageView.
        emojIconActions = new EmojIconActions(getApplicationContext(), activity_main, messageBox, emojiButton);
        emojIconActions.ShowEmojIcon();

        instantiateWebSocket();

        adapter = new MessageAdapter();
        messageList.setAdapter(adapter);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageBox.getText().toString();

                if(!message.isEmpty())
                {
                    webSocket.send(message);
                    messageBox.setText("");

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("message", message);
                        jsonObject.put("byServer", false);

                        adapter.addItem(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });





    }


    //Create a method called instantiateWebSocket, and it will instantiate an OkHttpClient and create a request object.
    private void instantiateWebSocket()
    {
        //Create an okhttp client object
        OkHttpClient client = new OkHttpClient();
        //Build a okhttp request
        Request request = new Request.Builder().url("wss://echo.websocket.org").build();
        SocketListener socketListener = new SocketListener(this);
        //Create a websocket object
        webSocket = client.newWebSocket(request,socketListener);



    }
    //websocketListener object used to create a connection with server
    public class SocketListener extends WebSocketListener
    {
        public MainActivity activity;
        //Create a constructor that take the main activity class
        public SocketListener(MainActivity activity){
            this.activity = activity;
        }

        //call when the connection establishes with the server
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Connection Established!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //call when there is new message(two types: byteString or String)
        @Override
        public void onMessage(WebSocket webSocket, final String text)
        {
            super.onMessage(webSocket, text);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("message", text);
                        jsonObject.put("byServer", true);


                        adapter.addItem(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

//        @Override
//        public void onMessage(WebSocket webSocket, ByteString bytes) {
//            super.onMessage(webSocket, bytes);
//        }

        //call before the connection was closed
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
        }

        //call when the connection close it
        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
        }

        //call when some error occur
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
        }
    }

    public class MessageAdapter extends BaseAdapter
    {

        List<JSONObject> messagesList = new ArrayList<>();

        //return number of item in the list
        @Override
        public int getCount() {
            return messagesList.size();
        }


        //Get the data item at the specified position.
        @Override
        public Object getItem(int i) {
            return messagesList.get(i);
        }

        //Get the id of the item at the specified position.
        @Override
        public long getItemId(int i) {
            return i;
        }



        //Get a View that displays the data at the specified position in the data set.
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null)
            {
                view = getLayoutInflater().inflate(R.layout.message_list_item, viewGroup, false);

            }

            TextView sentMessage = view.findViewById(R.id.sentMessage);
            TextView receiveMessage = view.findViewById(R.id.receiveMessage);

            JSONObject item = messagesList.get(i);

            try {
                if (item.getBoolean("byServer")) {
                    receiveMessage.setVisibility(View.VISIBLE);
                    receiveMessage.setText(item.getString("message"));
                    sentMessage.setVisibility(View.INVISIBLE);
                } else {
                    sentMessage.setVisibility(View.VISIBLE);
                    sentMessage.setText(item.getString("message"));
                    receiveMessage.setVisibility(View.INVISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return view;
        }

        void  addItem(JSONObject item){
            messagesList.add(item);
            //to update the message list
            notifyDataSetChanged();
        }
    }
}
