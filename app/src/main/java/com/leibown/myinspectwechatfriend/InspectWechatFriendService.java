package com.leibown.myinspectwechatfriend;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.leibown.myinspectwechatfriend.PerformClickUtils.performClick;

public class InspectWechatFriendService extends AccessibilityService {

    public static final int GROUP_COUNT = 39;//群组成员个数

    private String class_launcherui = "com.tencent.mm.ui.LauncherUI";
    private String class_SelectContactUI = "com.tencent.mm.ui.contact.SelectContactUI";
    private String class_ChattingUI = "com.tencent.mm.ui.chatting.ChattingUI";
    private String class_ChatroomInfoUI = "com.tencent.mm.plugin.chatroom.ui.ChatroomInfoUI";


    private String luncherUI_add_id = "com.tencent.mm:id/et";//主页面+号ID
    private String luncherUI_StartGroupChat_id = "com.tencent.mm:id/eu";//发起群聊ID

    private static final String SelectContactUI_checkbox_id = "com.tencent.mm:id/p9";//发起群聊界面 CheckBox  ID
    private static final String SelectContactUI_sure_btn_id = "com.tencent.mm:id/fx";//发起群聊界面 确定按钮ID
    private static final String SelectContactUI_listview_id = "com.tencent.mm:id/eb";//发起群聊界面ListView ID
    private static final String SelectContactUI_user_name_id = "com.tencent.mm:id/m5";//发起群聊界面每个Item里面人名ID
    private static final String SelectContactUI_sort_id = "com.tencent.mm:id/a9q";//发起群聊界面每个Item里面sort ID


    private String ChattingUI_prompt_box_id = "com.tencent.mm:id/ib";//群聊界面提示框ID
    private String ChatroomInfoUI_listview_id = "android:id/list";//聊天信息界面最外层ListView  ID


    private List<String> userNames = new ArrayList<>();
    public static HashSet<String> sortItems = new HashSet<>();
    public static HashSet<String> deleteList = new HashSet<>();

    public static boolean hasComplete = false;
    public static boolean allComplete = false; //一次检查通过

    private boolean canChecked;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //如果手机当前界面的窗口发送变化
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String className = event.getClassName().toString();
            Log.e("className:", className);
            if (!allComplete) {
                if (className.equals(class_launcherui)) {
                    PerformClickUtils.findTextAndClick(this, "更多功能按钮");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PerformClickUtils.findTextAndClick(this, "发起群聊");
                } else if (className.equals(class_SelectContactUI)) {
                    canChecked = true;
                    createGroup();
                } else if (className.equals(class_ChattingUI)) {
                    getDeleteFriend();
                } else if (className.equals(class_ChatroomInfoUI)) {
                    deleteGroup();
                }
            } else {
                Log.e("leibown:", "总好友数：" + userNames.size() + "___被删好友数：" + deleteList.size());
                userNames.clear();
                deleteList.clear();
                sortItems.clear();
                startActivity(new Intent(this, DeleteFriendListActivity.class));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void getDeleteFriend() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        //提示框
        List<AccessibilityNodeInfo> promptBoxs = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(ChattingUI_prompt_box_id);
        for (AccessibilityNodeInfo nodeInfo : promptBoxs) {
            if (nodeInfo != null && nodeInfo.getText() != null && nodeInfo.getText().toString().contains("你无法邀请未添加你为好友的用户进去群聊，请先向")) {
                String str = nodeInfo.getText().toString();
                str = str.replace("你无法邀请未添加你为好友的用户进去群聊，请先向", "");
                str = str.replace("发送朋友验证申请。对方通过验证后，才能加入群聊。", "");
                String[] arr = str.split("、");
                deleteList.addAll(Arrays.asList(arr));
                Preferences.saveDeleteFriends(this);

                Toast.makeText(this, "僵尸粉数量:" + deleteList.size(), Toast.LENGTH_SHORT).show();
                break;
            }
        }
        PerformClickUtils.findTextAndClick(this, "聊天信息");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void createGroup() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> listview = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(SelectContactUI_listview_id);
        int count = 0;
        if (listview != null) {
            while (canChecked) {
                List<AccessibilityNodeInfo> checkboxList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(SelectContactUI_checkbox_id);
                List<AccessibilityNodeInfo> sortList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(SelectContactUI_sort_id);

                for (AccessibilityNodeInfo nodeInfo : sortList) {
                    if (nodeInfo != null && nodeInfo.getText() != null) {
                        sortItems.add(nodeInfo.getText().toString());
                    }
                }
                Log.e("COLLECTION INFO! ", " " + sortItems.size() + "  " + listview.get(0).getCollectionInfo().getRowCount() + "  " + userNames.size());
                for (AccessibilityNodeInfo info : checkboxList) {
                    String nickName = info.getParent().findAccessibilityNodeInfosByViewId(SelectContactUI_user_name_id).get(0).getText().toString();
                    Rect rect = new Rect();
                    info.getParent().getBoundsInParent(rect);
                    String s = rect.toShortString();
                    if (!userNames.contains(nickName)) {
                        userNames.add(nickName);
                        performClick(info);
                        count++;
                        if (count >= GROUP_COUNT || userNames.size() >= listview.get(0).getCollectionInfo().getRowCount() - sortItems.size() - 3) {

                            List<AccessibilityNodeInfo> createButtons = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(SelectContactUI_sure_btn_id);
                            if (!createButtons.isEmpty()) {
                                performClick(createButtons.get(0));
                            }

                            if (userNames.size() >= listview.get(0).getCollectionInfo().getRowCount() - sortItems.size() - 3) {
                                hasComplete = true;
                                Log.e("leibown", "111111111111111111111");
                            }
                            return;
                        }
                    }
                }
                listview.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 退出群组步骤
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void deleteGroup() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(ChatroomInfoUI_listview_id);
        if (!nodeInfoList.isEmpty()) {

            nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            PerformClickUtils.findTextAndClick(this, "删除并退出");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            PerformClickUtils.findTextAndClick(this, "删除并退出");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            if(Utils.getVersion(this).equals(WECHAT_VERSION_27)){
//                PerformClickUtils.findTextAndClick(this,"确定");
//            }else{
            if (hasComplete) {
                allComplete = true;
                Log.e("leibown", "2222222222222222222222222");
            }
            PerformClickUtils.findTextAndClick(this, "离开群聊");
//            PerformClickUtils.findTextAndClick(this, "确定");
//            }
        }
    }

    @Override
    public void onInterrupt() {
        canChecked = false;
        Toast.makeText(this, "_检测好友服务被中断啦_", Toast.LENGTH_LONG).show();
    }


}
