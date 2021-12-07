package com.m2049r.xmrwallet.dialog;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.m2049r.xmrwallet.R;

public class TermsandConditionFragment extends Fragment {
    /* static final String TAG = "PrivacyFragment";

     public static PrivacyFragment newInstance() {
         return new PrivacyFragment();
     }

     public static void display(FragmentManager fm) {
         FragmentTransaction ft = fm.beginTransaction();
         Fragment prev = fm.findFragmentByTag(TAG);
         if (prev != null) {
             ft.remove(prev);
         }

         PrivacyFragment.newInstance().show(ft, TAG);
     }
 */
    TextView tvtermsandconditions;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_termsandconditons, container, false);
        tvtermsandconditions = view.findViewById(R.id.tvtermsandconditions);
        tvtermsandconditions.setText(Html.fromHtml(getString(R.string.terms_conditons)));
        return view;
    }

   /* @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_privacy_policy, null);

        ((TextView) view.findViewById(R.id.tvCredits)).setText(Html.fromHtml(getString(R.string.privacy_policy)));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.CommonAlertDialogBox)
                .setView(view)
                .setNegativeButton(R.string.about_close,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
        return builder.create();
    }*/
}
