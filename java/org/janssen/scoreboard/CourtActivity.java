package org.janssen.scoreboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.janssen.scoreboard.model.Server;
import org.janssen.scoreboard.model.types.RoomType;

/**
 * Created by stephan on 18/08/13.
 */
public class CourtActivity extends WifiControlActivity {

    private int selectedCourt;

    @Override
    public void onCreate(Bundle icicle) {


        super.onCreate(icicle);

        setContentView(R.layout.activity_court);

        validateWifi();

        Spinner courtsSpinner = (Spinner) findViewById(R.id.court);
        courtsSpinner.setSelection(0);

        ArrayAdapter<CharSequence> adapterMinutesSpinner
                = ArrayAdapter.createFromResource(this, R.array.courts, android.R.layout.simple_spinner_item);
        adapterMinutesSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        courtsSpinner.setAdapter(adapterMinutesSpinner);
        courtsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedCourt = pos;
                Server.setCourt(selectedCourt);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void startLogin(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.putExtra(Constants.COURT, selectedCourt);
        startActivity(intent);
    }
}
