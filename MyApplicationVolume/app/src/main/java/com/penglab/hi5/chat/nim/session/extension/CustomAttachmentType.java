package com.penglab.hi5.chat.nim.session.extension;

/**
 * Created by zhoujianghua on 2015/4/9.
 */
public interface CustomAttachmentType {
    // 多端统一
    int Guess = 1;//邀请消息
    int SnapChat = 2;//阅后即焚
    int Sticker = 3;//贴图
    int RTS = 4;//白板的发起结束消息
    int RedPacket = 5;
    int OpenedRedPacket = 6;
    int MultiRetweet = 15;//多条消息合并转发
    int Invite = 17;
}
