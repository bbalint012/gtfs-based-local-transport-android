package hu.unideb.bus.asynctask;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Optional;

import hu.unideb.api.responses.Route;
import hu.unideb.bus.R;
import hu.unideb.bus.apicall.AgencyCall;
import hu.unideb.bus.apicall.RouteCall;
import hu.unideb.bus.apicall.StopCall;
import hu.unideb.bus.app.App;
import hu.unideb.bus.room.BusRepository;
import hu.unideb.bus.room.model.RouteEntity;
import hu.unideb.bus.room.model.RouteStopJoin;
import hu.unideb.bus.room.model.StopEntity;
import hu.unideb.bus.utils.SharedPrefKey;
import hu.unideb.bus.utils.SharedPrefUtils;
import hu.unideb.bus.utils.Utils;

public class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
    private final String TAG = this.getClass().getSimpleName();
    private final Context context;
    private BusRepository repo;

    public PopulateDbAsyncTask(Context context) {
        this.context = context;
        this.repo = BusRepository.getInstance(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        saveDefaultLocation();

        List<Route> routes = new RouteCall().getRoutesForAgency();
        if (routes != null) {
            for (Route route : routes) {
                Log.d(TAG, "insert..." + route.getShortName());
                repo.insertRoute(new RouteEntity(route));

                List<StopEntity> stopsForRoute = new StopCall().getStopsForRoute(route.getId());
                for (StopEntity stop : stopsForRoute) {
                    repo.insertStop(stop);
                    repo.insertRouteStopJoin(new RouteStopJoin(route.getId(), stop.getId()));
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        refreshMapFragment();
    }

    private void saveDefaultLocation() {
        final Location location = new AgencyCall().getAgencyCoverageArea();
        SharedPrefUtils.saveString(context, SharedPrefKey.DEFAULT_LATITUDE.getKey(), String.valueOf(location.getLatitude()));
        SharedPrefUtils.saveString(context, SharedPrefKey.DEFAULT_LONGITUDE.getKey(), String.valueOf(location.getLongitude()));
    }

    private void refreshMapFragment() {
        SharedPrefUtils.saveBoolean(App.getAppContext(), SharedPrefKey.IS_DB_POPULATED.getKey(), true);
        //TODO: refresh fragment
        Utils.showToast(App.getAppContext(), App.getAppContext().getResources().getString(R.string.downloadOK), Toast.LENGTH_SHORT);
    }
}