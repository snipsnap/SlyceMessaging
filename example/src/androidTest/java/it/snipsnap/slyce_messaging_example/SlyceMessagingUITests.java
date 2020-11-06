package it.snipsnap.slyce_messaging_example;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.recyclerview.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import it.slyce.messaging.SlyceMessagingFragment;
import it.slyce.messaging.message.MessageSource;
import it.slyce.messaging.message.TextMessage;
import it.slyce.messaging.message.messageItem.MessageRecyclerAdapter;

/**
 * @Author Matthew Page
 * @Date 7/18/16
 */

@RunWith(AndroidJUnit4.class)
public class SlyceMessagingUITests extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mActivity;
    private SlyceMessagingFragment mFragment;
    private RecyclerView mRecyclerView;
    private MessageRecyclerAdapter mAdapter;

    public SlyceMessagingUITests() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
        mFragment = (SlyceMessagingFragment) mActivity.getFragmentManager().findFragmentById(R.id.fragment_for_slyce_messaging);
        mRecyclerView = (RecyclerView) mFragment.getView().findViewById(R.id.slyce_messaging_recycler_view);
        mAdapter = (MessageRecyclerAdapter) mRecyclerView.getAdapter();
    }

    @Test
    public void checkNumberOfMessages() {
        assertTrue(mAdapter.getItemCount() == 51);
    }

    @Test
    public void checkIsAtBottom() {
        assertFalse(mRecyclerView.canScrollVertically(1));
    }

    @Test
    public void addMessageButStillAtBottom() {
        TextMessage textMessage = new TextMessage();
        textMessage.setText("hi");
        textMessage.setSource(MessageSource.LOCAL_USER);
        textMessage.setDate(new Date().getTime());
        textMessage.setUserId("ueoanhotneutnhaeuo");
        textMessage.setAvatarUrl("");
        mFragment.addNewMessage(textMessage);
        checkIsAtBottom();
    }

    @Test
    public void addManyMessageButStillAtBottom() {
        for (int i = 0; i < 10; i++) {
            TextMessage textMessage = new TextMessage();
            textMessage.setText("hi");
            textMessage.setSource(MessageSource.LOCAL_USER);
            textMessage.setDate(new Date().getTime());
            textMessage.setUserId("ueoanhotneutnhaeuo");
            textMessage.setAvatarUrl("");
            mFragment.addNewMessage(textMessage);
        }
        checkIsAtBottom();
    }
}
