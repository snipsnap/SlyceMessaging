package it.slyce.messaging.listeners;

import android.net.Uri;

/**
 * Created by matthewpage on 6/21/16.
 */
public interface UserSendsMessageListener {
    public void onUserSendsTextMessage(String text);
    public void onUserSendsMediaMessage();
}
