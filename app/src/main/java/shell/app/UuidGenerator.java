package shell.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.mindrex.shell.R;
import com.mindrex.util.Sfx;

import java.util.UUID;

public final class UuidGenerator extends ActivityFramework {
    private AppCompatTextView uuidDisplay;

    public UuidGenerator() throws RuntimeException {
    }

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uuid_generator);
        final AppCompatButton[] b = new AppCompatButton[]{
                findViewById(R.id.generateRandomUuid),
                findViewById(R.id.copyToClipboard),
                findViewById(R.id.copyToClipboardWithoutDashes)
        };
        uuidDisplay = findViewById(R.id.uuidDisplay);

        Sfx.nonnull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        uuidDisplay.setText(UUID.randomUUID().toString());
        b[0].setOnClickListener(view -> {
            uuidDisplay.setText(null);
            uuidDisplay.setText(UUID.randomUUID().toString());
        });
        b[1].setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getResources().getString(R.string.generated_uuid_label), uuidDisplay.getText().toString());
            clipboard.setPrimaryClip(clip);
        });
        b[2].setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getResources().getString(R.string.generated_uuid_label), uuidDisplay.getText().toString().replace("-", ""));
            clipboard.setPrimaryClip(clip);
        });
    }

    @Override
    public void onPause() {
        getContentView().setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public final void onResume() {
        super.onResume();
        getContentView().setVisibility(View.VISIBLE);
    }

    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public final Context getContext() {
        return this;
    }

    @NonNull
    @Override
    public final String toString() {
        return "UuidGenerator{" +
                "uuidDisplay=" + uuidDisplay +
                '}';
    }
}
