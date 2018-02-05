package org.horaapps.leafpic.timeline;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.horaapps.leafpic.R;
import org.horaapps.leafpic.data.Media;
import org.horaapps.leafpic.data.sort.SortingOrder;
import org.horaapps.leafpic.timeline.data.TimelineHeaderModel;
import org.horaapps.leafpic.timeline.data.TimelineItem;
import org.horaapps.liz.ThemeHelper;
import org.horaapps.liz.ThemedAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static org.horaapps.leafpic.timeline.ViewHolder.TimelineHeaderViewHolder;
import static org.horaapps.leafpic.timeline.ViewHolder.TimelineMediaViewHolder;
import static org.horaapps.leafpic.timeline.ViewHolder.TimelineViewHolder;

/**
 * Adapter for showing Timeline.
 */
public class TimelineAdapter extends ThemedAdapter<TimelineViewHolder> {

    private List<TimelineItem> timelineItems;
    private static Drawable mediaPlaceholder;

    private final PublishSubject<Integer> onClickSubject = PublishSubject.create();
    private SortingOrder sortingOrder;
    private GroupingMode groupingMode;

    public TimelineAdapter(@NonNull Context context) {
        super(context);
        timelineItems = new ArrayList<>();

        this.sortingOrder = SortingOrder.DESCENDING;
    }

    public ArrayList<Media> getMedia() {
        return null;
    }

    public boolean clearSelected() {
        return true;
    }

    /**
     * Set the grouping mode (DAY, WEEK, MONTH, YEAR) of the Timeline.
     */
    public void setGroupingMode(@NonNull GroupingMode groupingMode) {
        this.groupingMode = groupingMode;
        notifyDataSetChanged();
    }

    /**
     * Set the sorting order (ASCENDING, DESCENDING) of the Timeline.
     */
    public void setSortingOrder(@NonNull SortingOrder sortingOrder) {
        this.sortingOrder = sortingOrder;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder.TimelineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (viewType == TimelineItem.TYPE_HEADER) {
            return new TimelineHeaderViewHolder(LayoutInflater.from(context).inflate(
                    R.layout.view_timeline_header,
                    parent,
                    false));

        } else if (viewType == TimelineItem.TYPE_MEDIA) {
            return new TimelineMediaViewHolder(LayoutInflater.from(context).inflate(
                    R.layout.card_photo,
                    parent,
                    false),
                    ThemeHelper.getPlaceHolder(context));
        }
        return null;
    }

    public void setGridLayoutManager(GridLayoutManager gridLayoutManager) {
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                TimelineItem timelineItem = getItem(position);
                if (timelineItem.getTimelineType() == TimelineItem.TYPE_HEADER) return 4;
                return 1;
            }
        });
    }

    private void clearAll() {
        timelineItems.clear();
        notifyDataSetChanged();
    }

    public boolean selecting() {
        return false;
    }

    public Observable<Integer> getClicks() {
        return onClickSubject;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getTimelineType();
    }

    @NonNull
    private TimelineItem getItem(int position) {
        return timelineItems.get(position);
    }

    @Override
    public void onBindViewHolder(TimelineViewHolder viewHolder, int position) {
        TimelineItem timelineItem = getItem(position);

        if (viewHolder instanceof TimelineHeaderViewHolder) {
            TimelineHeaderViewHolder headerViewHolder = (TimelineHeaderViewHolder) viewHolder;
            headerViewHolder.bind((TimelineHeaderModel) timelineItem);

        } else if (viewHolder instanceof TimelineMediaViewHolder) {
            TimelineMediaViewHolder mediaHolder = (TimelineMediaViewHolder) viewHolder;
            mediaHolder.bind((Media) timelineItem);
        }
    }

    public void setMedia(@NonNull List<Media> mediaList) {
        clearAll();
        timelineItems = getTimelineItems(mediaList);
        notifyDataSetChanged();
    }

    /**
     * Get the list of Timeline Items to show.
     * Internally adds the headers to the list.
     *
     * @param mediaList The list of media items to show.
     * @return A list with headers to be inflated for Timeline.
     */
    private List<TimelineItem> getTimelineItems(@NonNull List<Media> mediaList) {
        // Preprocessing - Add headers in the list of media
        // TODO: Think of ways to optimise / improve this logic

        List<TimelineItem> timelineItemList = new ArrayList<>();

        int headersAdded = 0;
        Calendar currentDate = null;
        for (int position = 0; position < mediaList.size(); position++) {
            Calendar mediaDate = new GregorianCalendar();
            mediaDate.setTimeInMillis(mediaList.get(position).getDateModified());
            if (currentDate == null || !groupingMode.isInGroup(currentDate, mediaDate)) {
                currentDate = mediaDate;
                TimelineHeaderModel timelineHeaderModel = new TimelineHeaderModel(mediaDate);
                timelineItemList.add(position + headersAdded, timelineHeaderModel);
                headersAdded++;
            }

            timelineItemList.add(mediaList.get(position));
        }
        return timelineItemList;
    }

    @Override
    public int getItemCount() {
        return timelineItems.size();
    }
}