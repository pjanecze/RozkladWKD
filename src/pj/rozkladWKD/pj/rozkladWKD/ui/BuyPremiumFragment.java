package pj.rozkladWKD.pj.rozkladWKD.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import pj.rozkladWKD.R;
import pj.rozkladWKD.RozkladWKD;

/**
 * Created with IntelliJ IDEA.
 * User: pawel
 * Date: 19.05.12
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public class BuyPremiumFragment extends SherlockDialogFragment implements CompoundButton.OnCheckedChangeListener {
    private CheckBox dontShow;

    SharedPreferences settings;

    public BuyPremiumFragment() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher_wkd);
        builder.setTitle(R.string.premium_title);
        builder.setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Intent i1 = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.premium_url)));
                startActivity(i1);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_buy_premium, null);
        dontShow = (CheckBox) view.findViewById(R.id.dont_show_again);
        dontShow.setOnCheckedChangeListener(this);
        builder.setView(view);

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return builder.create();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogTheme);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(compoundButton == dontShow) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(RozkladWKD.SHOW_PREMIUM_DIALOG, !b);
            editor.commit();
        }
    }
}
