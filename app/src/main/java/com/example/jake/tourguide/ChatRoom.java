package com.example.jake.tourguide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import static java.lang.Math.toIntExact;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatRoom extends AppCompatActivity {

    EditText usernameField;
    EditText newMessage;
    String username;
    Button enterButton;
    Button goToMap;
    String placeID;
    String People;

    Button fab;
    ListView list_of_messages;
    String mess;

    DatabaseReference ref;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    Date datetime = new Date();

    ArrayAdapter<String> adapter;
    ArrayList<String> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        Bundle extras = getIntent().getExtras();
        placeID = extras.getString("Place ID");
        People = extras.getString("People");

        list_of_messages = (ListView) findViewById(R.id.list_of_messages);

        listItems = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(ChatRoom.this,
                android.R.layout.simple_list_item_1,
                listItems);
        list_of_messages.setAdapter(adapter);


        usernameField = (EditText) findViewById(R.id.editText);
        enterButton = (Button) findViewById(R.id.button2);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameField.getText().toString();
                Toast.makeText(getApplicationContext(), username, Toast.LENGTH_LONG).show();
                ref = FirebaseDatabase.getInstance().getReference();
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> count = dataSnapshot.child("Chat Room: " + placeID).getChildren();
                        for (DataSnapshot snapshot : count) {
                            String val = snapshot.getValue().toString();
                            /*Iterable<DataSnapshot> count2 = snapshot.getChildren();
                            for (DataSnapshot snapshot1 : count2) {
                                datetime = (Date) snapshot1.getValue();
                            }*/

                            listItems.add(snapshot.getKey()+" said: "+val);

                            adapter.notifyDataSetChanged();
                            //Log.e(ChatRoom.class.getName(),snapshot.getValue().toString());
                        }

                        //Log.e(ChatRoom.class.getSimpleName(), dataSnapshot.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                usernameField.setVisibility(View.GONE);
                enterButton.setVisibility(View.GONE);
                goToMap.setVisibility(View.VISIBLE);
                newMessage = (EditText) findViewById(R.id.newmessage);
                fab = (Button) findViewById(R.id.fab);
                newMessage.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);

                int initialDelay = 0;
                int delay = 5;
                scheduler.scheduleWithFixedDelay(runnable, initialDelay, delay, TimeUnit.SECONDS);
            }
        });

        fab = (Button) findViewById(R.id.fab) ;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //chat = FirebaseDatabase.getInstance("Chat Room: " + placeID);
                newMessage = (EditText) findViewById(R.id.newmessage);
                mess= newMessage.getText().toString();
                writeNewMessage(username, mess, ref, placeID);
                newMessage.setText(null);
            }
        });

        goToMap = (Button) findViewById(R.id.goToMap);
        goToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduler.shutdown();
                Intent i = new Intent(ChatRoom.this, MapsActivity.class);
                startActivity(i);
                database = FirebaseDatabase.getInstance();
                databaseReference = database.getReference(placeID);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String value = dataSnapshot.getValue(String.class);

                            databaseReference.setValue(Integer.toString(Integer.parseInt(value)-1));



                    }



                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Toast.makeText(getApplicationContext(), "Error is " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void writeNewMessage (String user, String message, DatabaseReference ref, final String placeID) {
        //ChatMessage message1 = new ChatMessage(user, message);
        Map<String, Object> map = new HashMap<>();
        map.put(user, (Object) message);
        Date date = new Date();
        ref.child("Chat Room: " + placeID).child(user).setValue(message);

        refreshTask(ref, placeID, user, message);
        /*listItems.add(user+" said: "+message);
        adapter.notifyDataSetChanged();*/
    }

    private void refreshTask (DatabaseReference ref, final String placeID, final String user, String message) {
        ref = FirebaseDatabase.getInstance().getReference();
        listItems = new ArrayList<String>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> count = dataSnapshot.child("Chat Room: " + placeID).getChildren();
                for (DataSnapshot snapshot : count) {
                        String val = snapshot.getValue().toString();
                        listItems.add(snapshot.getKey() + " said: " + val);


                    //Log.e(ChatRoom.class.getName(),snapshot.getValue().toString());
                }
                adapter = new ArrayAdapter<String>(ChatRoom.this,
                        android.R.layout.simple_list_item_1,
                        listItems);
                list_of_messages.setAdapter(adapter);

                //Log.e(ChatRoom.class.getSimpleName(), dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshTask(ref, placeID, username, mess);
        }
    };

    @Override
    protected void onDestroy() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(placeID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                value = Integer.toString(Integer.parseInt(value)-1);
                ref.setValue(value);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
