package it.slyce.messaging.message.messageItem;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.slyce.messaging.R;
import it.slyce.messaging.message.messageItem.externalUser.media.MessageExternalUserMediaViewHolder;
import it.slyce.messaging.message.messageItem.externalUser.text.MessageExternalUserTextViewHolder;
import it.slyce.messaging.message.messageItem.general.generalOptions.MessageGeneralOptionsViewHolder;
import it.slyce.messaging.message.messageItem.internalUser.media.MessageInternalUserViewHolder;
import it.slyce.messaging.message.messageItem.internalUser.text.MessageInternalUserTextViewHolder;
import it.slyce.messaging.message.messageItem.general.generalText.MessageGeneralTextViewHolder;
import it.slyce.messaging.message.messageItem.spinner.SpinnerViewHolder;
import it.slyce.messaging.utils.CustomSettings;

/**
 * Created by John C. Hunchar on 5/12/16.
 */
public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private static final String TAG = MessageRecyclerAdapter.class.getName();

    private List<MessageItem> mMessageItems;

    private CustomSettings customSettings;

    public MessageRecyclerAdapter(List<MessageItem> messageItems, CustomSettings customSettings) {
        mMessageItems = messageItems;
        this.customSettings = customSettings;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        MessageViewHolder viewHolder = null;

        MessageItemType messageItemType = MessageItemType.values()[viewType];
        switch (messageItemType) {


            case INCOMING_MEDIA:
                View scoutMediaView = inflater.inflate(R.layout.item_message_external_media, parent, false);
                viewHolder = new MessageExternalUserMediaViewHolder(scoutMediaView, customSettings);
                break;

            case INCOMING_TEXT:
                View scoutTextView = inflater.inflate(R.layout.item_message_external_text, parent, false);
                viewHolder = new MessageExternalUserTextViewHolder(scoutTextView, customSettings);
                break;

            case OUTGOING_MEDIA:
                View userMediaView = inflater.inflate(R.layout.item_message_user_media, parent, false);
                viewHolder = new MessageInternalUserViewHolder(userMediaView, customSettings);
                break;

            case OUTGOING_TEXT:
                View userTextView = inflater.inflate(R.layout.item_message_user_text, parent, false);
                viewHolder = new MessageInternalUserTextViewHolder(userTextView, customSettings);
                break;

            case SPINNER:
                View spinnerView = inflater.inflate(R.layout.item_spinner, parent, false);
                viewHolder = new SpinnerViewHolder(spinnerView, customSettings);
                break;

            case GENERAL_TEXT:
                View generalTextView = inflater.inflate(R.layout.item_message_general_text, parent, false);
                viewHolder = new MessageGeneralTextViewHolder(generalTextView, customSettings);
                break;

            case GENERAL_OPTIONS:
                View generalOptionsView = inflater.inflate(R.layout.item_message_general_options, parent, false);
                viewHolder = new MessageGeneralOptionsViewHolder(generalOptionsView, customSettings);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder messageViewHolder, int position) {
        if (messageViewHolder == null) {
            return;
        }

        // Build the item
        MessageItem messageItem = getMessageItemByPosition(position);
        if (messageItem != null) {
            messageItem.buildMessageItem(messageViewHolder);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageItems != null ? mMessageItems.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {

        // Get the item type
        Integer itemType = getMessageItemType(position);
        if (itemType != null) {
            return itemType;
        }

        return super.getItemViewType(position);
    }

    private MessageItem getMessageItemByPosition(int position) {
        if (mMessageItems != null && !mMessageItems.isEmpty()) {
            if (position >= 0 && position < mMessageItems.size()) {
                MessageItem messageItem = mMessageItems.get(position);
                if (messageItem != null) {
                    return messageItem;
                }
            }
        }

        return null;
    }

    private Integer getMessageItemType(int position) {
        MessageItem messageItem = getMessageItemByPosition(position);
        if (messageItem != null) {
            return messageItem.getMessageItemTypeOrdinal();
        }

        return null;
    }

    public void updateMessageItemDataList(List<MessageItem> messageItems) {
        mMessageItems = messageItems;
        notifyDataSetChanged();
    }
}
