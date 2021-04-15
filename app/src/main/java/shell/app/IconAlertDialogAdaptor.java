package shell.app;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.Arrays;

public final class IconAlertDialogAdaptor extends BaseAdapter {
    private final Item[] items;

    public IconAlertDialogAdaptor(Item[] i) {
        this.items = i;
    }

    @Override
    public final int getCount() {
        return items.length;
    }

    @Override
    public final Object getItem(int position) {
        return items[position];
    }

    @Override
    public final long getItemId(int position) {
        return 0;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.select_dialog_item, parent, false);
        final AppCompatTextView a = convertView.findViewById(android.R.id.text1);
        a.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        final Item b = items[position];
        if (b.label != null) a.setText(b.label);
        else a.setText(a.getResources().getString(b.text));
        a.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);
        final int i = (int) (5 * convertView.getResources().getDisplayMetrics().density + 0.5f);
        a.setCompoundDrawablePadding(i);
        return convertView;
    }

    public static final class Item {
        public final String label;
        public final int icon;
        public final int text;

        @SuppressWarnings("unused")
        public Item(String s, int d) {
            this.label = s;
            this.icon = d;
            this.text = 0x80000000;
        }

        public Item(int i, int d) {
            this.label = null;
            this.icon = d;
            this.text = i;
        }

        @NonNull
        @Override
        public final String toString() {
            return "Item{" +
                    "label='" + label + '\'' +
                    ", icon=" + icon +
                    ", text=" + text +
                    '}';
        }
    }

    @NonNull
    @Override
    public final String toString() {
        return "IconAlertDialogAdaptor{" +
                "items=" + Arrays.toString(items) +
                '}';
    }
}