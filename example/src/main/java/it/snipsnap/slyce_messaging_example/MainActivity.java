package it.snipsnap.slyce_messaging_example;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.slyce.messaging.SlyceMessagingFragment;
import it.slyce.messaging.listeners.LoadMoreMessagesListener;
import it.slyce.messaging.listeners.OnOptionSelectedListener;
import it.slyce.messaging.listeners.UserSendsMessageListener;
import it.slyce.messaging.message.GeneralOptionsMessage;
import it.slyce.messaging.message.MediaMessage;
import it.slyce.messaging.message.Message;
import it.slyce.messaging.message.MessageSource;
import it.slyce.messaging.message.TextMessage;

public class MainActivity extends AppCompatActivity {

    private static String[] latin = {
            "Vestibulum dignissim enim a mauris malesuada fermentum. Vivamus tristique consequat turpis, pellentesque.",
            "Quisque nulla leo, venenatis ut augue nec, dictum gravida nibh. Donec augue nisi, volutpat nec libero.",
            "Cras varius risus a magna egestas.",
            "Mauris tristique est eget massa mattis iaculis. Aenean sed purus tempus, vestibulum ante eget, vulputate mi. Pellentesque hendrerit luctus tempus. Cras feugiat orci.",
            "Morbi ullamcorper, sapien mattis viverra facilisis, nisi urna sagittis nisi, at luctus lectus elit.",
            "Phasellus porttitor fermentum neque. In semper, libero id mollis.",
            "Praesent fermentum hendrerit leo, ac rutrum ipsum vestibulum at. Curabitur pellentesque augue.",
            "Mauris finibus mi commodo molestie placerat. Curabitur aliquam metus vitae erat vehicula ultricies. Sed non quam nunc.",
            "Praesent vel velit at turpis vestibulum eleifend ac vehicula leo. Nunc lacinia tellus eget ipsum consequat fermentum. Nam purus erat, mollis sed ullamcorper nec, efficitur.",
            "Suspendisse volutpat enim eros, et."
    };

    private static String[] urls = {
            "http://en.l4c.me/fullsize/googleplex-mountain-view-california-1242979177.jpg",
            "http://entropymag.org/wp-content/uploads/2014/10/outer-space-wallpaper-pictures.jpg",
            "http://www.bolwell.com/wp-content/uploads/2013/09/bolwell-metal-fabrication-raw-material.jpg",
            "http://www.bytscomputers.com/wp-content/uploads/2013/12/pc.jpg",
            "https://content.edmc.edu/assets/modules/ContentWebParts/AI/Locations/New-York-City/startpage-masthead-slide.jpg"
    };

    private volatile static int n = 0;

    private static Message getRandomMessage() {
        n++;
        Message message;
        if (Math.random() < 1.1) {
            TextMessage textMessage = new TextMessage();
            textMessage.setText(n + ""); // +  ": " + latin[(int) (Math.random() * 10)]);
            message = textMessage;
        } else {
            MediaMessage mediaMessage = new MediaMessage();
            mediaMessage.setUrl(urls[(int)(Math.random() * 5)]);
            message = mediaMessage;
        }
        message.setDate(new Date().getTime());
        if (Math.random() > 0.5) {
            message.setAvatarUrl("https://lh3.googleusercontent.com/-Y86IN-vEObo/AAAAAAAAAAI/AAAAAAAKyAM/6bec6LqLXXA/s0-c-k-no-ns/photo.jpg");
            message.setUserId("LP");
            message.setSource(MessageSource.EXTERNAL_USER);
        } else {
            message.setAvatarUrl("https://lh3.googleusercontent.com/-Y86IN-vEObo/AAAAAAAAAAI/AAAAAAAKyAM/6bec6LqLXXA/s0-c-k-no-ns/photo.jpg");
            message.setUserId("MP");
            message.setSource(MessageSource.LOCAL_USER);
        }
        return message;
    }

    SlyceMessagingFragment slyceMessagingFragment;

    private boolean hasLoadedMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(it.snipsnap.slyce_messaging_example.R.layout.activity_main);

        hasLoadedMore = false;

        slyceMessagingFragment = (SlyceMessagingFragment) getFragmentManager().findFragmentById(R.id.fragment_for_slyce_messaging);
        slyceMessagingFragment.setDefaultAvatarUrl("https://lh3.googleusercontent.com/-Y86IN-vEObo/AAAAAAAAAAI/AAAAAAAKyAM/6bec6LqLXXA/s0-c-k-no-ns/photo.jpg");
        slyceMessagingFragment.setDefaultDisplayName("Matthew Page");
        slyceMessagingFragment.setDefaultUserId("uhtnaeohnuoenhaeuonthhntouaetnheuontheuo");

        slyceMessagingFragment.setOnSendMessageListener(new UserSendsMessageListener() {
            @Override
            public void onUserSendsTextMessage(String text) {
                Log.d("inf", "******************************** " + text);
            }

            @Override
            public void onUserSendsMediaMessage() {
                Log.d("inf", "******************************** ");
            }
        });

        slyceMessagingFragment.setLoadMoreMessagesListener(new LoadMoreMessagesListener() {
            @Override
            public List<Message> loadMoreMessages() {
                Log.d("info", "loadMoreMessages()");

                if (!hasLoadedMore) {
                    hasLoadedMore = true;
                    ArrayList<Message> messages = new ArrayList<>();
                    GeneralOptionsMessage generalTextMessage = new GeneralOptionsMessage();
                    generalTextMessage.setTitle("Started group");
                    generalTextMessage.setFinalText("Accepted");
                    generalTextMessage.setOptions(new String[]{"Accept", "Reject"});
                    generalTextMessage.setOnOptionSelectedListener(new OnOptionSelectedListener() {
                        @Override
                        public String onOptionSelected(int optionSelected) {
                            if (optionSelected == 0) {
                                return "Accepted";
                            } else {
                                return "Rejected";
                            }
                        }
                    });
                    messages.add(generalTextMessage);
                    for (int i = 0; i < 50; i++)
                        messages.add(getRandomMessage());
                    messages.add(generalTextMessage);
                    Log.d("info", "loadMoreMessages() returns");
                    return messages;
                } else {
                    slyceMessagingFragment.setMoreMessagesExist(false);
                    return new ArrayList<>();
                }
            }
        });

        slyceMessagingFragment.setMoreMessagesExist(true);


        try {
            Thread.sleep(1000 * 3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                TextMessage textMessage = new TextMessage();
                textMessage.setText("Another message...");
                textMessage.setAvatarUrl("https://lh3.googleusercontent.com/-Y86IN-vEObo/AAAAAAAAAAI/AAAAAAAKyAM/6bec6LqLXXA/s0-c-k-no-ns/photo.jpg");
                textMessage.setDisplayName("Gary Johnson");
                textMessage.setUserId("LP");
                textMessage.setDate(new Date().getTime());
                textMessage.setSource(MessageSource.EXTERNAL_USER);
                slyceMessagingFragment.addNewMessage(textMessage);
            }
        }, 3, 3, TimeUnit.SECONDS);
    }


}
