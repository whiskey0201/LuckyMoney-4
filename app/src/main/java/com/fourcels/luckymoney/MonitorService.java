package com.fourcels.luckymoney;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;


public class MonitorService extends AccessibilityService {
    private ArrayList<AccessibilityNodeInfo> mNodeInfoList = new ArrayList<AccessibilityNodeInfo>();

    private boolean mLuckyMoneyOpened = true;

    private final String HONGBAO_TEXT_KEY = "[微信红包]";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            System.out.println("TYPE_NOTIFICATION_STATE_CHANGED");
            List<CharSequence> texts = event.getText();

            if(!texts.isEmpty()){
                for(CharSequence t: texts) {
                    if(t.toString().contains(HONGBAO_TEXT_KEY)){
                        openNotify(event);
                        mLuckyMoneyOpened = false;
                        break;
                    }
                }
            }

        }

        if(mLuckyMoneyOpened) {
            System.out.println("there is no new lucky money.");
            return;
        }

        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            System.out.println("TYPE_WINDOW_CONTENT_CHANGED");
            lingHongbao();
        }


        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            System.out.println("TYPE_WINDOW_STATE_CHANGED");
            openHongbao(event);

        }
    }

    private void openHongbao(AccessibilityEvent event){
        String className = event.getClassName().toString();
        if(className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")){
            chaiHongbao();
            mLuckyMoneyOpened = true;
        } else if(className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")){
            //goHome();
            mLuckyMoneyOpened = true;
        }
    }


    private void openNotify(AccessibilityEvent event){
        System.out.println("openNotify");
        if(event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)){
            return;
        }

        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try{
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    private void chaiHongbao(){
        System.out.println("chaiHongbao");
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null){
            System.out.println("rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("Open");

        if(list.size() == 0) {
            list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");
        }

        for(AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private  void  lingHongbao(){
        System.out.println("linHongbao");
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null){
            System.out.println("rootWindow为空");
            return;
        }

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");

        if(!list.isEmpty()){
            for(int i = list.size()-1; i>=0; i--) {
                AccessibilityNodeInfo parent = list.get(i);
                while (parent != null && !parent.isClickable()){
                    parent = parent.getParent();
                }
                System.out.println("-->领取红包:" + parent);
                if(parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        }

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    @Override
    public void onInterrupt() {

    }
}
