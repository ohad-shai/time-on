package com.ohadshai.timeon.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ohadshai.timeon.R;
import com.ohadshai.timeon.entities.HeaderListItem;
import com.ohadshai.timeon.entities.ProjectWorker;
import com.ohadshai.timeon.utils.ListItem;
import com.ohadshai.timeon.utils.SelectionHelper;
import com.ohadshai.timeon.utils.TimeSpan;
import com.ohadshai.timeon.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.ohadshai.timeon.utils.SelectionHelper.SELECTED;

/**
 * Represents a project workers adapter for RecyclerView.
 * Created by Ohad on 11/20/2016.
 */
public class ProjectWorkersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //region Private Members

    /**
     * Holds the activity owner of the adapter.
     */
    private Activity _activity;

    /**
     * Holds the list of project workers to adapt.
     */
    private List<ListItem> _items;

    /**
     * Holds the list of all the project workers.
     */
    private ArrayList<ProjectWorker> _workers;

    //endregion

    //region C'tors

    /**
     * C'tor
     * Initializes a new instance of a project workers adapter for RecyclerView.
     *
     * @param activity The activity owner of the adapter.
     * @param items    The list of items to adapt.
     * @param workers  The list of all the project workers.
     */
    public ProjectWorkersAdapter(List<ListItem> items, ArrayList<ProjectWorker> workers, Activity activity) {
        this._activity = activity;
        this._items = items;
        this._workers = workers;
    }

    //endregion

    //region Adapter Events

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == ListItem.TYPE_HEADER) {
            View itemView = inflater.inflate(R.layout.item_header_list_item, parent, false);
            return new HeaderViewHolder(itemView);
        } else {
            View itemView = inflater.inflate(R.layout.item_project_worker, parent, false);
            return new WorkerViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int type = getItemViewType(position);
        if (type == ListItem.TYPE_HEADER)
            ((HeaderViewHolder) viewHolder).bindViewHolder((HeaderListItem) _items.get(position)); // Gets the header, and binds it to the view holder.
        else
            ((WorkerViewHolder) viewHolder).bindViewHolder((ProjectWorker) _items.get(position)); // Gets the project worker, and binds it to the view holder.
    }

    @Override
    public int getItemCount() {
        return _items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return _items.get(position).getType();
    }

    //endregion

    //region Public API

    /**
     * Gets the context of the projects adapter.
     *
     * @return Returns the context of the projects adapter.
     */
    public Context getContext() {
        return this._activity.getApplicationContext();
    }

    //endregion

    //region Inner Classes

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        //region Private Members

        /**
         * Holds the header list item object of the current position.
         */
        private HeaderListItem header;

        private TextView txtHeaderDate;

        //endregion

        HeaderViewHolder(final View itemView) {
            super(itemView);

            txtHeaderDate = (TextView) itemView.findViewById(R.id.txtHeaderDate);

        }

        //region Default Methods

        /**
         * Binds a {@link HeaderListItem} object to the view holder.
         *
         * @param header The header object to bind.
         */
        void bindViewHolder(HeaderListItem header) {
            this.header = header; // Sets the header object to the view holder local.

            // Sets item views, based on the views and the data model:

            txtHeaderDate.setText(Utils.Dates.display(header.getDate(), _activity.getApplicationContext()));
        }

        //endregion

    }

    private class WorkerViewHolder extends RecyclerView.ViewHolder implements SelectionHelper.SelectionItemController<ProjectWorker> {

        //region Private Members

        /**
         * Holds the project worker object of the current position item.
         */
        private ProjectWorker worker;

        private CardView cardWorker;
        private LinearLayout _llWorkerFrame;
        private TextView lblWorkerStart;
        private TextView lblWorkerTrack;
        private TextView lblWorkerEnd;
        private TextView lblSessionState;

        private FrameLayout _flSelection;
        private CheckBox _chkSelection;

        //endregion

        WorkerViewHolder(final View itemView) {
            super(itemView);

            cardWorker = (CardView) itemView.findViewById(R.id.cardWorker);
            cardWorker.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Checks if not already in selection mode:
                    if (!getSelectionHelper().isInSelectionMode()) {
                        getSelectionHelper().enterSelectionMode(); // Enters the selection mode.
                        itemSelection(true); // Selects this item that started the selection mode.
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            _llWorkerFrame = (LinearLayout) itemView.findViewById(R.id.llWorkerFrame);

            lblWorkerStart = (TextView) itemView.findViewById(R.id.lblWorkerStart);

            lblWorkerTrack = (TextView) itemView.findViewById(R.id.lblWorkerTrack);

            lblWorkerEnd = (TextView) itemView.findViewById(R.id.lblWorkerEnd);

            lblSessionState = (TextView) itemView.findViewById(R.id.lblSessionState);
            lblSessionState.setVisibility(View.GONE);

            _flSelection = (FrameLayout) itemView.findViewById(R.id.flSelection);
            _flSelection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _chkSelection.performClick();
                }
            });

            _chkSelection = new CheckBox(_activity);
            _chkSelection.setChecked(false);
            _chkSelection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (_chkSelection.isChecked())
                        itemSelection(true);
                    else
                        itemSelection(false);
                }
            });

        }

        //region Selection Events

        @Override
        public SelectionHelper<ProjectWorker> getSelectionHelper() {
            return ((SelectionHelper.SelectionController<ProjectWorker>) _activity).getSelectionHelper();
        }

        @Override
        public void drawSelection(int state) {
            if (state == SELECTED) {
                _chkSelection.setChecked(true);
                _llWorkerFrame.setBackgroundResource(R.drawable.selected_radial_background_style);
                _flSelection.setVisibility(View.VISIBLE);
            } else if (state == SelectionHelper.UNSELECTED) {
                _chkSelection.setChecked(false);
                _llWorkerFrame.setBackgroundColor(Color.WHITE);
                _flSelection.setVisibility(View.VISIBLE);
            } else if (state == SelectionHelper.GONE) {
                _llWorkerFrame.setBackgroundColor(Color.WHITE);
                _flSelection.setVisibility(View.GONE);
            } else {
                throw new IllegalArgumentException("State is invalid.");
            }
        }

        @Override
        public void bindSelection() {
            // Checks if currently in the selection mode:
            if (getSelectionHelper() != null && getSelectionHelper().isInSelectionMode()) {
                // Checks if the item is selected in the selection list:
                if (getSelectionHelper().isItemSelected(worker))
                    this.drawSelection(SELECTED);
                else
                    this.drawSelection(SelectionHelper.UNSELECTED);
            } else {
                this.drawSelection(SelectionHelper.GONE);
            }
        }

        @Override
        public void itemSelection(boolean isSelected) {
            if (isSelected) {
                drawSelection(SELECTED);
                getSelectionHelper().itemSelection(getTypePosition(getLayoutPosition()), true);
            } else {
                drawSelection(SelectionHelper.UNSELECTED);
                getSelectionHelper().itemSelection(getTypePosition(getLayoutPosition()), false);
            }
        }

        //endregion

        //region Default Methods

        /**
         * Binds a ProjectWorker object to the view holder.
         *
         * @param worker The project worker object to bind.
         */
        @SuppressLint("SetTextI18n")
        void bindViewHolder(ProjectWorker worker) {
            this.worker = worker; // Sets the project worker object to the view holder local.

            // Sets item views, based on the views and the data model:

            // Checks if the session is an edit track worker or not:
            if (worker.getTrackEdit() != null) {
                lblWorkerStart.setVisibility(View.GONE);
                lblWorkerEnd.setVisibility(View.GONE);
                lblSessionState.setVisibility(View.VISIBLE);

                // Checks if the time is added or removed:
                if (worker.getTrackEdit().getTotalMilliseconds() > 0)
                    lblWorkerTrack.setText("+ " + worker.getTrackEdit().toString());
                else
                    lblWorkerTrack.setText(worker.getTrackEdit().toString());
            } else {
                // Just a regular session:
                lblWorkerStart.setVisibility(View.VISIBLE);
                lblWorkerEnd.setVisibility(View.VISIBLE);
                lblSessionState.setVisibility(View.GONE);

                lblWorkerStart.setText(String.format("%1$tI:%1$tM %1$Tp", worker.getStart()));

                // Checks if there's an end time:
                if (worker.getEnd() != null) {
                    TimeSpan track = new TimeSpan(worker.getEnd().getTimeInMillis() - worker.getStart().getTimeInMillis());
                    lblWorkerTrack.setText(track.toString());

                    lblWorkerEnd.setText(String.format("%1$tI:%1$tM %1$Tp", worker.getEnd()));
                } else {
                    lblWorkerTrack.setText("-");

                    lblWorkerEnd.setText("-");
                }
            }

            this.bindSelection();
        }

        /**
         * Gets the position of the item in the list of the type.
         *
         * @param position The position of the item in the container list.
         * @return Returns the position of the item in the list of the type.
         */
        int getTypePosition(int position) {
            return _workers.indexOf((ProjectWorker) _items.get(position));
        }

        //endregion

    }

    //endregion

}
