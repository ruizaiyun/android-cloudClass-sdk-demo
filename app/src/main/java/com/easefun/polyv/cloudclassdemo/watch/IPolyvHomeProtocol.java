package com.easefun.polyv.cloudclassdemo.watch;

import android.view.ViewGroup;

import com.easefun.polyv.cloudclass.chat.PolyvChatManager;

/**
 * @author df
 * @create 2018/9/27
 * @Describe
 */
public interface IPolyvHomeProtocol {
    void sendDanmu(CharSequence content);

    //当前直播的场次Id
    String getSessionId();

    ViewGroup getImageViewerContainer();

    ViewGroup getChatEditContainer();

    PolyvChatManager getChatManager();
}
