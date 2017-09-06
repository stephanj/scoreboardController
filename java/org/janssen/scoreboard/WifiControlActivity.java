package org.janssen.scoreboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

import org.janssen.scoreboard.comms.NetworkUtilities;
import org.janssen.scoreboard.model.Server;

/**
 * The Wifi controller.
 *
 * Created by stephan on 25/01/14.
 */
public class WifiControlActivity extends FragmentActivity {

    public static final String SCORE = "score";     // Part of the SSID name needed by the app to work

    public void validateWifi() {
        final WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        if (wifi != null) {
            if (!wifi.isWifiEnabled()) {
                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setTitle(getString(R.string.waarschuwing));
                dialog.setMessage(getString(R.string.zetWifiAan));
                dialog.setCancelable(false);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        //
                    }
                });
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.show();
                return;
            }

            WifiInfo connectionInfo = wifi.getConnectionInfo();

            if (connectionInfo != null &&
                !connectionInfo.getSSID().toLowerCase().contains(SCORE)) {
                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setTitle(getString(R.string.waarschuwing));
                dialog.setMessage(getString(R.string.verkeerdeWifi));
                dialog.setCancelable(false);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        //
                    }
                });
                dialog.setIcon(android.R.drawable.ic_dialog_alert);
                dialog.show();
            }
        }
    }

    public void validateWifiAndServerComms() {
        validateWifi();

        if (Server.getIp() != null) {
            PingServerTask pingServerTask = new PingServerTask();
            pingServerTask.execute();
        }
    }

    public void onPingResult(String result) {
        if (result == null ||
            !result.equalsIgnoreCase("OK")) {
            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setTitle(getString(R.string.error));
            dialog.setMessage(getString(R.string.geenComms) + Server.getCourt() + getString(R.string.herstart));
            dialog.setCancelable(false);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int buttonId) {
                    //
                }
            });
            dialog.setIcon(android.R.drawable.ic_dialog_alert);
            dialog.show();
        }
    }

    /**
     * Represents an asynchronous task used to authenticate a user against the
     * SampleSync Service
     */
    private class PingServerTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // We do the actual work of authenticating the user
            // in the NetworkUtilities class.
            try {
                return NetworkUtilities.pingServer();
            } catch (Exception ex) {
                return ex.toString();
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            // On a successful authentication, call back into the Activity to
            // communicate the authToken (or null for an error).
            onPingResult(result);
        }
    }
}
