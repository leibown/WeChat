package com.leibown.myinspectwechatfriend;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class DeleteFriendListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_friend_list);
        ListView listView = (ListView) findViewById(R.id.listview);
        TextView textView = (TextView) findViewById(R.id.desc);

        List<String> list = Preferences.getDeleteFriends(this);
        textView.setText("被删好友数量:"+list.size());

        listView.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list));
    }
}
