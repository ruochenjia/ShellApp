package shell.app;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.mindrex.shell.R;

final class RootRequiredDialog {
    public static void show(Context context) {
        final AlertDialog.Builder a = new AlertDialog.Builder(context);
        a.setTitle(R.string.access_denied_message);
        a.setMessage(R.string.root_required_message);
        a.setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss());
        a.show();
    }
}