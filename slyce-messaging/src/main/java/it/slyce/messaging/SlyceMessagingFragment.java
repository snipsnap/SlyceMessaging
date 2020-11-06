package it.slyce.messaging;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.slyce.messaging.listeners.LoadMoreMessagesListener;
import it.slyce.messaging.listeners.UserClicksAvatarPictureListener;
import it.slyce.messaging.listeners.UserSendsMessageListener;
import it.slyce.messaging.message.Message;
import it.slyce.messaging.message.MessageSource;
import it.slyce.messaging.message.SpinnerMessage;
import it.slyce.messaging.message.TextMessage;
import it.slyce.messaging.message.messageItem.MessageItem;
import it.slyce.messaging.message.messageItem.MessageRecyclerAdapter;
import it.slyce.messaging.utils.CustomSettings;
import it.slyce.messaging.utils.DateUtils;
import it.slyce.messaging.utils.Refresher;
import it.slyce.messaging.utils.ScrollUtils;
import it.slyce.messaging.utils.asyncTasks.AddNewMessageTask;
import it.slyce.messaging.utils.asyncTasks.ReplaceMessagesTask;
import it.slyce.messaging.view.ViewUtils;

/**
 * Created by John C. Hunchar on 1/12/16.
 */
public class SlyceMessagingFragment extends Fragment implements OnClickListener {

    private static final int START_RELOADING_DATA_AT_SCROLL_VALUE = 5000; // TODO: maybe change this? make it customizable?

    private EditText mEntryField;
    private LinearLayoutManager mLinearLayoutManager;
    private List<Message> mMessages;
    private List<MessageItem> mMessageItems;
    private MessageRecyclerAdapter mRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private View rootView;

    private LoadMoreMessagesListener loadMoreMessagesListener;
    private UserSendsMessageListener listener;
    private CustomSettings customSettings;
    private Refresher mRefresher;

    private String defaultAvatarUrl;
    private String defaultDisplayName;
    private String defaultUserId;
    private int startHereWhenUpdate;
    private long recentUpdatedTime;
    private boolean moreMessagesExist;

    public void setPictureButtonVisible(final boolean bool) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView imageView = (ImageView) rootView.findViewById(R.id.slyce_messaging_image_view_snap);
                    imageView.setVisibility(bool ? View.VISIBLE : View.GONE);
                }
            });
    }

    private void addSpinner() {
        mMessages.add(0, new SpinnerMessage());
        replaceMessages(mMessages, -1);
    }

    private void removeSpinner() {
        if (mMessages.get(0) instanceof SpinnerMessage) {
            mMessages.remove(0);
            mMessageItems.remove(0);
            mRecyclerAdapter.notifyItemRemoved(0);
        }
    }

    public void setMoreMessagesExist(boolean moreMessagesExist) {
        if (this.moreMessagesExist == moreMessagesExist)
            return;
        this.moreMessagesExist = moreMessagesExist;
        if (moreMessagesExist)
            addSpinner();
        else
            removeSpinner();
        loadMoreMessagesIfNecessary();
    }

    public void setLoadMoreMessagesListener(LoadMoreMessagesListener loadMoreMessagesListener) {
        this.loadMoreMessagesListener = loadMoreMessagesListener;
        loadMoreMessagesIfNecessary();
    }

    public void setUserClicksAvatarPictureListener(UserClicksAvatarPictureListener userClicksAvatarPictureListener) {
        this.customSettings.userClicksAvatarPictureListener = userClicksAvatarPictureListener;
    }

    public void setDefaultAvatarUrl(String defaultAvatarUrl) {
        this.defaultAvatarUrl = defaultAvatarUrl;
    }

    public void setDefaultDisplayName(String defaultDisplayName) {
        this.defaultDisplayName = defaultDisplayName;
    }

    public void setDefaultUserId(String defaultUserId) {
        this.defaultUserId = defaultUserId;
    }

    public void setStyle(int style) {
        TypedArray ta = getActivity().obtainStyledAttributes(style, R.styleable.SlyceMessagingTheme);
        this.customSettings.backgroudColor = ta.getColor(R.styleable.SlyceMessagingTheme_backgroundColor, Color.GRAY);
        rootView.setBackgroundColor(this.customSettings.backgroudColor); // the background color
        this.customSettings.timestampColor = ta.getColor(R.styleable.SlyceMessagingTheme_timestampTextColor, Color.BLACK);
        this.customSettings.externalBubbleTextColor = ta.getColor(R.styleable.SlyceMessagingTheme_externalBubbleTextColor, Color.WHITE);
        this.customSettings.externalBubbleBackgroundColor = ta.getColor(R.styleable.SlyceMessagingTheme_externalBubbleBackground, Color.WHITE);
        this.customSettings.localBubbleBackgroundColor = ta.getColor(R.styleable.SlyceMessagingTheme_localBubbleBackground, Color.WHITE);
        this.customSettings.localBubbleTextColor = ta.getColor(R.styleable.SlyceMessagingTheme_localBubbleTextColor, Color.WHITE);
        this.customSettings.snackbarBackground = ta.getColor(R.styleable.SlyceMessagingTheme_snackbarBackground, Color.WHITE);
        this.customSettings.snackbarButtonColor = ta.getColor(R.styleable.SlyceMessagingTheme_snackbarButtonColor, Color.WHITE);
        this.customSettings.snackbarTitleColor = ta.getColor(R.styleable.SlyceMessagingTheme_snackbarTitleColor, Color.WHITE);
    }

    public void addNewMessages(List<Message> messages) {
        mMessages.addAll(messages);
        new AddNewMessageTask(messages, mMessageItems, mRecyclerAdapter, mRecyclerView, getActivity().getApplicationContext(), customSettings).execute();
    }

    public void addNewMessage(Message message) {
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        addNewMessages(messages);
    }

    public void setOnSendMessageListener(UserSendsMessageListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.fragment_slyce_messaging, null);
        this.rootView = v;
        this.customSettings = new CustomSettings();

        // Setup views
        mEntryField = (EditText) rootView.findViewById(R.id.slyce_messaging_edit_text_entry_field);
        ImageView mSendButton = (ImageView) rootView.findViewById(R.id.slyce_messaging_image_view_send);
        ImageView mSnapButton = (ImageView) rootView.findViewById(R.id.slyce_messaging_image_view_snap);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.slyce_messaging_recycler_view);

        // Add interfaces
        mSendButton.setOnClickListener(this);
        mSnapButton.setOnClickListener(this);

        // Init variables for recycler view
        mMessages = new ArrayList<>();
        mMessageItems = new ArrayList<>();
        mRecyclerAdapter = new MessageRecyclerAdapter(mMessageItems, customSettings);
        mLinearLayoutManager = new LinearLayoutManager(this.getActivity().getApplicationContext()){
            @Override
            public boolean canScrollVertically() {
                return !mRefresher.isRefreshing();
            }

            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                }
            }
        };
        mLinearLayoutManager.setStackFromEnd(true);

        // Setup recycler view
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return mRefresher.isRefreshing();
                    }
                }
        );

        startUpdateTimestampsThread();
        startHereWhenUpdate = 0;
        recentUpdatedTime = 0;
        mRefresher = new Refresher(false);
        setStyle(R.style.MyTheme);

        loadMoreMessagesIfNecessary();
        startLoadMoreMessagesListener();

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 232);

        return rootView;
    }

    private void startUpdateTimestampsThread() {
        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (int i = startHereWhenUpdate; i < mMessages.size() && i < mMessageItems.size(); i++) {
                    try {
                        MessageItem messageItem = mMessageItems.get(i);
                        Message message = messageItem.getMessage();
                        if (DateUtils.dateNeedsUpdated(getActivity(), message.getDate(), messageItem.getDate())) {
                            messageItem.updateDate(getActivity(), message.getDate());
                            updateTimestampAtValue(i);
                        } else if (i == startHereWhenUpdate) {
                            i++;
                        }
                    } catch (RuntimeException exception) {
                        Log.d("debug", exception.getMessage());
                        exception.printStackTrace();
                    }
                }
            }
        }, 0, 62, TimeUnit.SECONDS);
    }

    private void startLoadMoreMessagesListener() {
        if (Build.VERSION.SDK_INT >= 23)
            mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    loadMoreMessagesIfNecessary();
                }
            });
        else
            mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    loadMoreMessagesIfNecessary();
                }
            });
    }

    private void loadMoreMessagesIfNecessary() {
        if (shouldReloadData()) {
            recentUpdatedTime = new Date().getTime();
            loadMoreMessages();
        }
    }

    private void loadMoreMessages() {
        new AsyncTask<Void, Void, Void>() {
            private boolean spinnerExists;
            private List<Message> messages;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mRefresher.setIsRefreshing(true);
                spinnerExists = moreMessagesExist && mMessages.get(0) instanceof SpinnerMessage;
                if (spinnerExists) {
                    mMessages.remove(0);
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                messages = loadMoreMessagesListener.loadMoreMessages();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                int upTo = messages.size();
                for (int i = messages.size() - 1; i >= 0; i--) {
                    Message message = messages.get(i);
                    mMessages.add(0, message);
                }
                if (spinnerExists && moreMessagesExist)
                    mMessages.add(0, new SpinnerMessage());
                mRefresher.setIsRefreshing(false);
                replaceMessages(mMessages, upTo);
            }
        }.execute();
    }

    public void replaceMessages(List<Message> messages) {
        replaceMessages(messages, -1);
    }

    private void replaceMessages(List<Message> messages, int upTo) {
        if (getActivity() != null) {
            new ReplaceMessagesTask(messages, mMessageItems, mRecyclerAdapter, getActivity().getApplicationContext(), mRefresher, upTo).execute();
        }
    }

    private boolean shouldReloadData() {
        int scrollOffset = mRecyclerView.computeVerticalScrollOffset();
        if (loadMoreMessagesListener == null || !moreMessagesExist) {
            return false;
        } else {
            return scrollOffset < START_RELOADING_DATA_AT_SCROLL_VALUE &&
                    recentUpdatedTime + 1000 < new Date().getTime();
        }
    }

    private void updateTimestampAtValue(final int i) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRecyclerAdapter.notifyItemChanged(i);
                }
            });
        }
    }

    private File file;
    private Uri outputFileUri;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.slyce_messaging_image_view_send) {
            sendUserTextMessage();
        } else if (v.getId() == R.id.slyce_messaging_image_view_snap) {
            if (listener != null) listener.onUserSendsMediaMessage();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 232 || (data == null && this.file.exists())) {
            return;
        }
        try {
            if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
                
            }
        } catch (RuntimeException exception) {
            Log.d("debug", exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void sendUserTextMessage() {
        String text = ViewUtils.getStringFromEditText(mEntryField);
        if (TextUtils.isEmpty(text))
            return;
        mEntryField.setText("");

        // Build messageData object
        TextMessage message = new TextMessage();
        message.setDate(System.currentTimeMillis());
        message.setAvatarUrl(defaultAvatarUrl);
        message.setSource(MessageSource.LOCAL_USER);
        message.setDisplayName(defaultDisplayName);
        message.setText(text);
        message.setUserId(defaultUserId);
        addNewMessage(message);

        ScrollUtils.scrollToBottomAfterDelay(mRecyclerView, mRecyclerAdapter);
        if (listener != null)
            listener.onUserSendsTextMessage(message.getText());
    }
}
