package jamesnguyen.flic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.flic.lib.FlicButton;
import io.flic.lib.FlicButtonCallback;
import io.flic.lib.FlicButtonCallbackFlags;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;

public class MainActivity extends AppCompatActivity {

    private String appID = "3bd37cbc-70c1-4d4a-9b73-f6fab65b6a0e";
    private String appSecret = "3ce6fc73-2163-4b00-9dd8-a7ed7fea9599";
    private String appName = "Slide controller";

    private FlicManager manager;

    private FlicButtonCallback buttonCallback = new FlicButtonCallback() {
        @Override
        public void onButtonSingleOrDoubleClick(FlicButton button, boolean wasQueued, int timeDiff, boolean isSingleClick, boolean isDoubleClick) {
            if (isSingleClick) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StreamHandler.getInstance().writeOutputStream(ServerHandler.getInstance().getCURRENT_SOCKET()
                                , "VK_RIGHT");
                        Toast.makeText(MainActivity.this, "Single clicked, next slide", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            if (isDoubleClick) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StreamHandler.getInstance().writeOutputStream(ServerHandler.getInstance().getCURRENT_SOCKET()
                                , "VK_LEFT");
                        Toast.makeText(MainActivity.this, "Double clicked, previous slide", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    private void setButtonCallback(FlicButton button) {
        button.removeAllFlicButtonCallbacks();
        button.addFlicButtonCallback(buttonCallback);
        button.setFlicButtonCallbackFlags(FlicButtonCallbackFlags.CLICK_OR_DOUBLE_CLICK);
        button.setActiveMode(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                //ServerHandler.getInstance().setHostIP(HOST.getText().toString());
                //Log.d("Connection", "Yes!?");
                new Thread(ServerHandler.getInstance()).start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Connection Established", Toast.LENGTH_SHORT).show();
                    }
                });
                HOST.getText().clear();
            }
        });
    }

    @Override
    protected void onDestroy() {
        FlicManager.destroyInstance();
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
}
