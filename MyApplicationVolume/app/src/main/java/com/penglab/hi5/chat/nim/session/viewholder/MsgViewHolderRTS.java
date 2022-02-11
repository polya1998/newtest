package com.penglab.hi5.chat.nim.session.viewholder;

import android.widget.TextView;

import com.penglab.hi5.chat.nim.session.extension.RTSAttachment;
import com.penglab.hi5.R;
import com.netease.nim.uikit.business.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

public class MsgViewHolderRTS extends MsgViewHolderBase {

    private TextView textView;

    public MsgViewHolderRTS(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    public int getContentResId() {
        return R.layout.nim_message_item_rts;
    }

    @Override
    public void inflateContentView() {
        textView = (TextView) view.findViewById(R.id.rts_text);
    }

    @Override
    public void bindContentView() {
        RTSAttachment attachment = (RTSAttachment) message.getAttachment();
        textView.setText(attachment.getContent());
    }
}

