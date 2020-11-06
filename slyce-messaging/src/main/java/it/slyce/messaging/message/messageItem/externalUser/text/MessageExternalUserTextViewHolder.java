package it.slyce.messaging.message.messageItem.externalUser.text;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import it.slyce.messaging.R;
import it.slyce.messaging.utils.CustomSettings;
import it.slyce.messaging.message.messageItem.master.text.MessageTextViewHolder;

/**
 * Created by John C. Hunchar on 5/12/16.
 */
public class MessageExternalUserTextViewHolder extends MessageTextViewHolder {

    public MessageExternalUserTextViewHolder(View itemView, final CustomSettings customSettings) {
        super(itemView, customSettings);

        avatar = (ImageView) itemView.findViewById(R.id.message_scout_text_image_view_avatar);
        carrot = (ImageView) itemView.findViewById(R.id.message_scout_text_image_view_carrot);
        text = (TextView) itemView.findViewById(R.id.message_scout_text_text_view_text);
        timestamp = (TextView) itemView.findViewById(R.id.message_scout_text_text_view_timestamp);
        avatarContainer = (ViewGroup) itemView.findViewById(R.id.message_scout_text_image_view_avatar_group);
        initials = (TextView) itemView.findViewById(R.id.message_scout_text_text_view_initials);
        bubble = (FrameLayout) itemView.findViewById(R.id.message_scout_text_view_group_bubble);

        Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.shape_rounded_rectangle_white);
        // Drawable drawable = itemView.getContext().getDrawable();
        drawable.setColorFilter(customSettings.externalBubbleBackgroundColor, PorterDuff.Mode.SRC_ATOP);
        bubble.setBackground(drawable);
        carrot.setColorFilter(customSettings.externalBubbleBackgroundColor);
        text.setTextColor(customSettings.externalBubbleTextColor);
        timestamp.setTextColor(customSettings.timestampColor);
    }
}
