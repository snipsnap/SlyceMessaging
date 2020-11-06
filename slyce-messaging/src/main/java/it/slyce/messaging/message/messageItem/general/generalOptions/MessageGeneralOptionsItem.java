package it.slyce.messaging.message.messageItem.general.generalOptions;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import it.slyce.messaging.R;
import it.slyce.messaging.message.GeneralOptionsMessage;
import it.slyce.messaging.message.MessageSource;
import it.slyce.messaging.message.messageItem.MessageItem;
import it.slyce.messaging.message.messageItem.MessageItemType;
import it.slyce.messaging.message.messageItem.MessageViewHolder;

public class MessageGeneralOptionsItem extends MessageItem {
    private Context context;

    public MessageGeneralOptionsItem(GeneralOptionsMessage generalOptionsMessage, Context context) {
        super(generalOptionsMessage);
        this.context = context;
    }

    @Override
    public void buildMessageItem(MessageViewHolder messageViewHolder) {
        final MessageGeneralOptionsViewHolder viewHolder = (MessageGeneralOptionsViewHolder) messageViewHolder;
        final GeneralOptionsMessage generalTextMessage = (GeneralOptionsMessage) message;
        if (!generalTextMessage.isSelected()) {
            viewHolder.titleTextView.setText(generalTextMessage.getTitle());
            viewHolder.optionsLinearLayout.removeAllViews();
            for (int i = 0; i < generalTextMessage.getOptions().length; i++) {
                String option = generalTextMessage.getOptions()[i];
                Button button = new Button(context);
                button.setText(option);
                button.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.background_gray_light), PorterDuff.Mode.MULTIPLY);
                button.setTextColor(Color.BLUE);
                final int finalI = i;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewHolder.optionsLinearLayout.removeAllViews();
                        generalTextMessage.setSelected();
                        String text = generalTextMessage.getOnOptionSelectedListener().onOptionSelected(finalI);
                        viewHolder.titleTextView.setText(text);
                    }
                });
                viewHolder.optionsLinearLayout.addView(button);
            }
        } else {
            viewHolder.titleTextView.setText(generalTextMessage.getFinalText());
            viewHolder.optionsLinearLayout.removeAllViews();
        }
    }

    @Override
    public MessageItemType getMessageItemType() {
        return MessageItemType.GENERAL_OPTIONS;
    }

    @Override
    public MessageSource getMessageSource() {
        return MessageSource.GENERAL;
    }
}
