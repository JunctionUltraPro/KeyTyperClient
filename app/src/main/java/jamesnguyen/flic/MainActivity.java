package jamesnguyen.flic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import io.flic.lib.FlicButton;
import io.flic.lib.FlicButtonCallback;
import io.flic.lib.FlicButtonCallbackFlags;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;

public class MainActivity extends AppCompatActivity {

    private final String appID = "3bd37cbc-70c1-4d4a-9b73-f6fab65b6a0e";
    private final String appSecret = "3ce6fc73-2163-4b00-9dd8-a7ed7fea9599";
    private final String appName = "Slide controller";
    private String single_click_macro, double_click_macro, hold_macro;

    private FlicManager manager;

    private FlicButtonCallback singleOrDoubleOrHoldCallback = new FlicButtonCallback() {
        @Override
        public void onButtonSingleOrDoubleClickOrHold(FlicButton button, boolean wasQueued
                , int timeDiff, boolean isSingleClick, boolean isDoubleClick, boolean isHold) {
            if (isSingleClick) {
                doOnSingleClick();
            } else if (isDoubleClick) {
                doOnDoubleClick();
            } else if (isHold) {
                doOnHold();
            }
        }
    };

    private void setButtonCallback(FlicButton button) {
        button.removeAllFlicButtonCallbacks();
        button.addFlicButtonCallback(singleOrDoubleOrHoldCallback);
        button.setFlicButtonCallbackFlags(FlicButtonCallbackFlags.CLICK_OR_DOUBLE_CLICK_OR_HOLD);
        button.setActiveMode(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File f = new File(
                "/data/data/" + getPackageName() + "/shared_prefs/jamesnguyen.flic.app.xml");
        if (f.exists()) {
            readCache(); // Read cache
        }

        FlicManager.setAppCredentials(appID, appSecret, appName);

        FlicManager.getInstance(this, new FlicManagerInitializedCallback() {

            @Override
            public void onInitialized(FlicManager manager) {
                MainActivity.this.manager = manager;

                List<FlicButton> buttons = manager.getKnownButtons();
                for (FlicButton button : buttons) {
                    String status = null;
                    switch (button.getConnectionStatus()) {
                        case FlicButton.BUTTON_DISCONNECTED:
                            status = "disconnected";
                            break;
                        case FlicButton.BUTTON_CONNECTION_STARTED:
                            status = "connection started";
                            break;
                        case FlicButton.BUTTON_CONNECTION_COMPLETED:
                            status = "connection completed";
                            break;
                    }
                    Log.d("MainActivity", "Found an existing button: " + button + ", status: " + status);
                    setButtonCallback(button);
                }
            }
        });

        final Button grabButton = (Button) findViewById(R.id.grab_button);
        grabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.initiateGrabButton(MainActivity.this);
            }
        });

        final Button connectButton = (Button) findViewById(R.id.connect_button);
        final EditText HOST = (EditText) findViewById(R.id.host_ip);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ServerHandler.getInstance().getHostIP().isEmpty() ||
                        !HOST.getText().toString().equalsIgnoreCase(ServerHandler.getInstance().getHostIP())) {
                    ServerHandler.getInstance().setHostIP(HOST.getText().toString());

                    Log.d("Connection", "Yes!?");
                    new Thread(ServerHandler.getInstance()).start();
                    makeToast("Connection Succeed");
                } else {
                    makeToast("Connection either exist or failed");
                }
            }
        });

        final Button syntaxButton = (Button) findViewById(R.id.syntax_button);
        syntaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent e = new Intent();
                e.setClass(getBaseContext(), SyntaxDisplayActivity.class);
                startActivity(e);
            }
        });
    }

    @Override
    protected void onDestroy() {
        FlicManager.destroyInstance();
        writeCache();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FlicButton button = manager.completeGrabButton(requestCode, resultCode, data);
        if (button != null) {
            Log.d("MainActivity", "Got a button: " + button);
            setButtonCallback(button);
        }
    }

    private void doOnSingleClick() {
        final EditText input1 = (EditText) findViewById(R.id.input1);
        if (!input1.getText().toString().isEmpty()) {
            single_click_macro = input1.getText().toString();
            StreamHandler.getInstance().writeOutputStream(ServerHandler.getInstance().getCURRENT_SOCKET()
                    , single_click_macro);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Single clicked", Toast.LENGTH_SHORT).show();
                    TextView current_single_click_macro = (TextView) findViewById(R.id.current_single_macro);
                    current_single_click_macro.setText("Current single click macro: " + single_click_macro);
                }
            });
        } else {
            if (!single_click_macro.isEmpty()) {
                StreamHandler.getInstance().writeOutputStream(ServerHandler.getInstance().getCURRENT_SOCKET()
                        , single_click_macro);
                makeToast("Single Clicked - Cache");

            } else {
                makeToast("Input Empty");
            }
        }
    }

    private void doOnDoubleClick() {
        final EditText input2 = (EditText) findViewById(R.id.input2);
        if (!input2.getText().toString().isEmpty()) {
            double_click_macro = input2.getText().toString();
            StreamHandler.getInstance().writeOutputStream(ServerHandler.getInstance().getCURRENT_SOCKET()
                    , double_click_macro);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Double clicked", Toast.LENGTH_SHORT).show();

                    TextView current_double_macro = (TextView) findViewById(R.id.current_double_macro);
                    current_double_macro.setText("Current double click macro: " + double_click_macro);
                }
            });
        } else {
            if (!double_click_macro.isEmpty()) {
                StreamHandler.getInstance().writeOutputStream(ServerHandler.getInstance().getCURRENT_SOCKET()
                        , double_click_macro);
                makeToast("Double Clicked - Cache");
            } else {
                makeToast("Input Empty");
            }
        }
    }

    private void doOnHold() {
        final EditText input3 = (EditText) findViewById(R.id.input3);
        if (!input3.getText().toString().isEmpty()) {
            hold_macro = input3.getText().toString();
            StreamHandler.getInstance().writeOutputStream(ServerHandler.getInstance().getCURRENT_SOCKET()
                    , hold_macro);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Held", Toast.LENGTH_SHORT).show();
                    TextView current_hold_macro = (TextView) findViewById(R.id.current_hold_macro);
                    current_hold_macro.setText("Current hold macro: " + hold_macro);
                }
            });
        } else {
            if (!hold_macro.isEmpty()) {
                StreamHandler.getInstance().writeOutputStream(ServerHandler.getInstance().getCURRENT_SOCKET()
                        , hold_macro);
                makeToast("Held - Cache");
            } else {
                makeToast("Input Empty");
            }
        }
    }

    private void makeToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeCache() {

        SharedPreferences cache = this.getSharedPreferences(
                "jamesnguyen.flic.app", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = cache.edit();
        editor.putString("Single_Click_Macro", single_click_macro);
        editor.putString("Double_Click_Macro", double_click_macro);
        editor.putString("Hold_Macro", hold_macro);
        editor.apply();

    }

    @SuppressWarnings("unchecked")
    private void readCache() {
        final TextView current_single_click_macro = (TextView) findViewById(R.id.current_single_macro);
        final TextView current_double_click_macro = (TextView) findViewById(R.id.current_double_macro);
        final TextView current_hold_macro = (TextView) findViewById(R.id.current_hold_macro);

        SharedPreferences cache = getSharedPreferences("jamesnguyen.flic.app", Context.MODE_PRIVATE);

        single_click_macro = cache.getString("Single_Click_Macro", "");
        double_click_macro = cache.getString("Double_Click_Macro", "");
        hold_macro = cache.getString("Hold_Macro", "");
        current_single_click_macro.setText("Current macro: " + single_click_macro);
        current_double_click_macro.setText("Current macro: " + double_click_macro);
        current_hold_macro.setText("Current macro: " + hold_macro);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                current_single_click_macro.setText("Current macro: " + single_click_macro);
                current_double_click_macro.setText("Current macro: " + double_click_macro);
                current_hold_macro.setText("Current macro: " + hold_macro);
                Toast.makeText(MainActivity.this, single_click_macro, Toast.LENGTH_SHORT).show();
            }
        });


    }
}
