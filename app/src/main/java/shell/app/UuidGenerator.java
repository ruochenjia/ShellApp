package shell.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.mindrex.shell.R;

import java.util.UUID;

public final class UuidGenerator extends ActivityFramework {
    private AppCompatTextView uuidDisplay;

    public UuidGenerator() {
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uuid_generator);
        uuidDisplay = findViewById(R.id.uuidDisplay);
        uuidDisplay.setText(UUID.randomUUID().toString());
        findViewById(R.id.generateRandomUuid).setOnClickListener(view -> uuidDisplay.setText(UUID.randomUUID().toString()));
        findViewById(R.id.copyToClipboard).setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getResources().getString(R.string.generated_uuid_label), uuidDisplay.getText().toString());
            clipboard.setPrimaryClip(clip);
        });
        findViewById(R.id.copyToClipboardWithoutDashes).setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getResources().getString(R.string.generated_uuid_label), uuidDisplay.getText().toString().replace("-", ""));
            clipboard.setPrimaryClip(clip);
        });
    }

    @Override
    public final void onPause() {
        getContentView().setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public final void onResume() {
        super.onResume();
        getContentView().setVisibility(View.VISIBLE);
    }
}