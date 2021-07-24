package shell.app;

import android.content.Context;

import com.mindrex.android.appsupport.AlertDialog;
import com.mindrex.shell.R;

final class RootRequiredDialog {
    public static void show(Context context) {
        AlertDialog.show(context,
                context.getResources().getString(R.string.access_denied_message),
                context.getResources().getString(R.string.root_required_message),
                AlertDialog.dismissButton);
    }
}