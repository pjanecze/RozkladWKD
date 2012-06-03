package pj.rozkladWKD.pj.rozkladWKD.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockDialogFragment;
import pj.rozkladWKD.R;
import pj.rozkladWKD.RozkladWKD;

/**
 * Created with IntelliJ IDEA.
 * User: paweljaneczek
 * Date: 31.05.2012
 * Time: 07:45
 * To change this template use File | Settings | File Templates.
 */
public class SetUsernameFragment extends SherlockDialogFragment{
    public interface ISetUsernameListener {
        public void onSetUsernamePositive(String username);
        public void onSetUsernameNegative();
    }


    private EditText username;

    SharedPreferences settings;

    public SetUsernameFragment() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher_wkd);
        builder.setTitle(R.string.set_username_title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String usernameText = username.getText().toString();
                if(TextUtils.isEmpty(usernameText)) {
                    username.setError(getString(R.string.fields_cannot_be_empty));
                    return;
                }
                if(usernameText.length() >30) {
                    username.setError(getString(R.string.field_value_too_long));
                    return;
                }
                ISetUsernameListener listener = (ISetUsernameListener) getActivity();
                listener.onSetUsernamePositive(usernameText);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ISetUsernameListener listener = (ISetUsernameListener) getActivity();
                listener.onSetUsernameNegative();
                dialogInterface.dismiss();
            }
        });

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_username, null);
        username = (EditText) view.findViewById(R.id.username);
        builder.setView(view);

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return builder.create();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogTheme);
    }

}
