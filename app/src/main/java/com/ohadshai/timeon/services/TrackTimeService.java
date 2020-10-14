package com.ohadshai.timeon.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.ohadshai.timeon.entities.ProjectWorker;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * An {@link Service} for tracking time in a project worker.
 */
public class TrackTimeService extends Service {

    //region Private Members

    /**
     * Holds the binder for the TrackTimeService.
     */
    private IBinder _binder = new TimeTrackerBinder();

    /**
     * Holds an indicator if the service is destroyed or not (to inform the tracking thread).
     */
    private boolean _isDestroyed = false;

    /**
     * Holds the list of all the project workers registered to the service.
     */
    private ArrayList<ProjectWorker> _workers = new ArrayList<>();

    /**
     * Holds all the listeners for the workers registered to the service.
     */
    private ArrayList<TrackTimeService.TimeTrackerListener> _listeners = new ArrayList<>();

    //endregion

    //region Service Events

    @Override
    public void onCreate() {
        super.onCreate();

        new Task_TimeTracker().execute();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        _isDestroyed = true;
    }

    //endregion

    //region Public API

    /**
     * Indicates whether there is any project worker running or not.
     *
     * @return Returns true if there is any project worker running, otherwise false.
     */
    public boolean isAnyRunning() {
        return _workers.size() > 0;
    }

    /**
     * Registers a worker to the time tracker service.
     *
     * @param worker   The worker to register the service.
     * @param listener The listener for the worker.
     */
    public void registerWorker(ProjectWorker worker, TimeTrackerListener listener) {
        if (worker == null)
            throw new NullPointerException("worker");
        if (listener == null)
            throw new NullPointerException("listener");

        // Checks if the worker already exists:
        int index = _workers.indexOf(worker);
        if (index > -1) {
            // Worker already exists, so updates the listener for the worker:
            _listeners.set(index, listener);
        } else {
            // Worker not exists, so adds the worker:
            _workers.add(worker);
            _listeners.add(listener);
        }
    }

    /**
     * Unregisters a worker from the time tracker service.
     *
     * @param worker The worker to unregister from the service.
     */
    public void unregisterWorker(ProjectWorker worker) {
        if (worker == null)
            throw new NullPointerException("worker");

        if (!_workers.contains(worker))
            return;

        int index = _workers.indexOf(worker);
        _listeners.remove(index);
        _workers.remove(index);
    }

    //endregion

    //region Inner Classes

    /**
     * Represents a binder for the track time service.
     */
    public class TimeTrackerBinder extends Binder {
        public TrackTimeService getService() {
            return TrackTimeService.this;
        }
    }

    /**
     * Represents a task for tracking the time.
     */
    private class Task_TimeTracker extends AsyncTask<Void, ProjectWorker, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                while (!_isDestroyed) {
                    long current = Calendar.getInstance().getTimeInMillis();

                    // On each workers, checks if 1 second passed to update UI:
                    for (ProjectWorker worker : _workers) {
                        // Checks if a second is passed in the worker:
                        if (((current - worker.getStart().getTimeInMillis()) % 1000) < 200)
                            publishProgress(worker);
                    }

                    Thread.sleep(100); // Sleeps 100 milliseconds.
                }
                return null;
            } catch (InterruptedException e) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(ProjectWorker... values) {
            int index = _workers.indexOf(values[0]);
            TimeTrackerListener listener = null;

            if (index > -1)
                listener = _listeners.get(index);

            // Checks if there's a listener, and 1 second passed (for donkey users pressing a lot):
            if (listener != null)
                listener.onTrackUpdate(); // Fires the event to update the UI of the worker.
        }

    }

    /**
     * Represents the listener for the time tracker service.
     */
    public interface TimeTrackerListener {

        /**
         * Event occurs when a track update occurs, called every 1 second, (used to update UI).
         */
        void onTrackUpdate();

    }

    //endregion

}
