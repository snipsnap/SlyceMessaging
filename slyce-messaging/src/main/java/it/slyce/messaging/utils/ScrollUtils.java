package it.slyce.messaging.utils;

import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScrollUtils {
    private static final int SMOOTH_SCROLL_IF_THIS_MUCH = 100000;

    public static void scrollToTopAfterDelay(RecyclerView mRecyclerView, RecyclerView.Adapter mRecyclerAdapter) {
        scrollToPositionAfterDelay(0, mRecyclerView, mRecyclerAdapter);
    }

    public static void scrollToBottomAfterDelay(RecyclerView mRecyclerView, RecyclerView.Adapter mRecyclerAdapter) {
        scrollToPositionAfterDelay(mRecyclerAdapter.getItemCount() - 1, mRecyclerView, mRecyclerAdapter);
    }

    public static void scrollToPositionAfterDelay(final int position, final RecyclerView mRecyclerView, final RecyclerView.Adapter mRecyclerAdapter) {
        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        scheduleTaskExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                scrollToPosition(position+1, mRecyclerView, mRecyclerAdapter);
            }
        }, 150, TimeUnit.MILLISECONDS);
    }

    public static void scrollToPosition(int position, RecyclerView mRecyclerView, RecyclerView.Adapter mRecyclerAdapter) {
        int offsetOfScroll = getOffsetOfGoal(position, mRecyclerView, mRecyclerAdapter);
        if (offsetOfScroll < SMOOTH_SCROLL_IF_THIS_MUCH) {
            mRecyclerView.smoothScrollToPosition(position);
        } else {
            mRecyclerView.scrollToPosition(position);
        }
    }

    private static int getOffsetOfGoal(int position, RecyclerView mRecyclerView, RecyclerView.Adapter mRecyclerAdapter) {
        int scrollPosition = mRecyclerView.computeVerticalScrollOffset();
        int goalPosition;
        if (position == 0) { // we are at the top
            goalPosition = 0;
        } else if (position == mRecyclerAdapter.getItemCount() - 1) {
            goalPosition = mRecyclerView.computeVerticalScrollRange();
        } else {
            goalPosition = 100000000; // this will cause scrollToPosition() to be called, which we want
        }
        return Math.abs(scrollPosition - goalPosition);
    }
}
