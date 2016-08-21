package com.pajato.android.gamechat.chat.adapter;

import java.util.ArrayList;
import java.util.List;

import static com.pajato.android.gamechat.chat.adapter.DateHeaderItem.DateHeaderType.thisMonth;
import static com.pajato.android.gamechat.chat.adapter.DateHeaderItem.DateHeaderType.today;
import static com.pajato.android.gamechat.chat.adapter.DateHeaderItem.DateHeaderType.yesterday;

/**
 * Provide a POJO with some dummy data to use while developing the rooms screen.
 *
 * @author Paul Michael Reilly
 */
public class DummyData {

    public static List<RoomsListItem> getData() {
        List<RoomsListItem> list = new ArrayList<>();
        list.add(new RoomsListItem(new DateHeaderItem(today)));
        list.add(new RoomsListItem(new GroupListItem("0")));
        list.add(new RoomsListItem(new GroupListItem("1")));
        list.add(new RoomsListItem(new DateHeaderItem(yesterday)));
        list.add(new RoomsListItem(new GroupListItem("2")));
        list.add(new RoomsListItem(new GroupListItem("3")));
        list.add(new RoomsListItem(new GroupListItem("4")));
        list.add(new RoomsListItem(new GroupListItem("5")));
        list.add(new RoomsListItem(new DateHeaderItem(thisMonth)));
        list.add(new RoomsListItem(new GroupListItem("6")));
        list.add(new RoomsListItem(new GroupListItem("7")));

        return list;
    }

}
