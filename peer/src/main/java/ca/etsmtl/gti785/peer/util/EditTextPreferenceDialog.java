package ca.etsmtl.gti785.peer.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
//import android.support.v7.app.AlertDialog; // FIXME: Using this broke colorAccent, see: https://code.google.com/p/android/issues/detail?id=194643
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import ca.etsmtl.gti785.peer.R;

/**
 * This custom PreferenceDialog is heavily inspired from
 * {@link android.preference.EditTextPreference EditTextPreference} and
 * {@link android.preference.DialogPreference DialogPreference}.
 * Seeing no way to manually invoke an EditTextPreference from outside a
 * {@link android.preference.PreferenceActivity PreferenceActivity},
 * I decided to reimplement the core logic from both of those classes,
 * which is to display a {@link android.app.Dialog Dialog} with an
 * {@link android.widget.EditText EditText} field, backed by a
 * {@link android.content.SharedPreferences SharedPreferences} key.
 *
 * @see <a href="https://developer.android.com/guide/topics/ui/settings.html">https://developer.android.com/guide/topics/ui/settings.html</a>
 */
public class EditTextPreferenceDialog implements DialogInterface.OnClickListener {

    private EditText editText;

    private Activity activity;
    private Dialog dialog;

    private String key;
    private String defValue;
    private String text;

    private OnValueChangeListner listner;

    public EditTextPreferenceDialog(Activity activity, int keyResId, int defValueResId) {
        this(activity, activity.getString(keyResId), activity.getString(defValueResId));
    }

    public EditTextPreferenceDialog(Activity activity, String key, String defValue) {
        this.activity = activity;
        this.key = key;
        this.defValue = defValue;

        editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setSelectAllOnFocus(true);
        editText.selectAll();

        text = restoreString();
    }

    private String restoreString() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString(key, defValue);
    }

    private void persistString(String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void showDialog() {
         if (dialog != null && dialog.isShowing()) {
             throw new IllegalStateException("Dialog already showing.");
         }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.pref_server_name_title)
            .setPositiveButton(R.string.dialog_ok, this)
            .setNegativeButton(R.string.dialog_cancel, this);

        View contentView = createDialogView();
        bindDialogView(contentView);
        builder.setView(contentView);

        dialog = builder.create();

        requestInputMethod(dialog);

        dialog.show();
    }

    private View createDialogView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        return inflater.inflate(R.layout.preference_dialog_edittext, null);
    }

    private void bindDialogView(View contentView) {
        editText.setText(getText());

        ViewGroup container = (ViewGroup) contentView.findViewById(R.id.edittext_container);
        if (container != null) {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void requestInputMethod(Dialog dialog) {
        Window window = dialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            setText(editText.getText().toString());

            if (listner != null) {
                listner.onValueChange(text);
            }
        }

        dialog.dismiss(); // We probably don't have to call this, but we are going to anyway
    }

    public Activity getActivity() {
        return activity;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;

        persistString(text);
    }

    public OnValueChangeListner getListner() {
        return listner;
    }

    public void setListner(OnValueChangeListner listner) {
        this.listner = listner;
    }

    public interface OnValueChangeListner {
        void onValueChange(String value);
    }
}
