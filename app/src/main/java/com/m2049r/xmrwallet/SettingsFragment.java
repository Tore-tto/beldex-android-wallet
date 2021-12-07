package com.m2049r.xmrwallet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.m2049r.xmrwallet.data.NodeInfo;
import com.m2049r.xmrwallet.dialog.AboutFragment;
import com.m2049r.xmrwallet.dialog.PrivacyFragment;
import com.m2049r.xmrwallet.layout.NodeInfoAdapter;
import com.m2049r.xmrwallet.layout.WalletInfoAdapter;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.util.Helper;
import com.m2049r.xmrwallet.util.LocaleHelper;
import com.m2049r.xmrwallet.util.NodePinger;
import com.m2049r.xmrwallet.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

public class SettingsFragment extends Fragment implements WalletInfoAdapter.OnInteractionListener,
        View.OnClickListener {

    private boolean isFabOpen = false;

    private SettingsFragment.Listener activityCallback;

    // Container Activity must implement this interface
    public interface Listener {
        File getStorageRoot();

        boolean onWalletSelected(String wallet, boolean streetmode);

        void onWalletDetails(String wallet);

        void onWalletRename(String name);

        void onWalletBackup(String name);

        void onWalletRestore();

        void onWalletDelete(String walletName);

        void onWalletDeleteCache(String walletName);

        void onAddWallet(String type);

        void onNodePrefs();

        void showNet();

        void setToolbarButton(int type);

        void setTitle(String title);

        void setNode(NodeInfo node);

        NodeInfo getNode();

        Set<NodeInfo> getFavouriteNodes();

        Set<NodeInfo> getOrPopulateFavourites();

        boolean hasLedger();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SettingsFragment.Listener) {
            this.activityCallback = (SettingsFragment.Listener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onPause() {
        Timber.d("onPause()");
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume() %s", activityCallback.getFavouriteNodes().size());
        activityCallback.setTitle("Settings");
        activityCallback.setToolbarButton(Toolbar.BUTTON_BACK);
        activityCallback.showNet();
        pingSelectedNode();
    }

    public void pingSelectedNode() {
        new SettingsFragment.AsyncFindBestNode().execute(SettingsFragment.AsyncFindBestNode.PING_SELECTED);
    }

    private NodeInfo autoselect(Set<NodeInfo> nodes) {
        if (nodes.isEmpty()) return null;
        NodePinger.execute(nodes, null);
        List<NodeInfo> nodeList = new ArrayList<>(nodes);
        Collections.sort(nodeList, NodeInfo.BestNodeComparator);
        return nodeList.get(0);
    }

    private class AsyncFindBestNode extends AsyncTask<Integer, Void, NodeInfo> {
        final static int PING_SELECTED = 0;
        final static int FIND_BEST = 1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //pbNode.setVisibility(View.VISIBLE);
            //llNode.setVisibility(View.INVISIBLE);
        }

        @Override
        protected NodeInfo doInBackground(Integer... params) {
            Set<NodeInfo> favourites = activityCallback.getOrPopulateFavourites();
            NodeInfo selectedNode;
            if (params[0] == FIND_BEST) {
                selectedNode = autoselect(favourites);
            } else if (params[0] == PING_SELECTED) {
                selectedNode = activityCallback.getNode();
                if (!activityCallback.getFavouriteNodes().contains(selectedNode))
                    selectedNode = null; // it's not in the favourites (any longer)
                if (selectedNode == null)
                    for (NodeInfo node : favourites) {
                        if (node.isSelected()) {
                            selectedNode = node;
                            break;
                        }
                    }
                if (selectedNode == null) { // autoselect
                    selectedNode = autoselect(favourites);
                } else
                    selectedNode.testRpcService();
            } else throw new IllegalStateException();
            if ((selectedNode != null) && selectedNode.isValid()) {
                activityCallback.setNode(selectedNode);
                return selectedNode;
            } else {
                activityCallback.setNode(null);
                return null;
            }
        }

        @Override
        protected void onPostExecute(NodeInfo result) {
            if (!isAdded()) return;
            //pbNode.setVisibility(View.INVISIBLE);
            //llNode.setVisibility(View.VISIBLE);
            if (result != null) {
                Timber.d("found a good node %s", result.toString());
                showNode(result);
            } else {
                //tvNodeName.setText(getResources().getText(R.string.node_create_hint));
               // tvNodeName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
               // tvNodeAddress.setText(null);
                //tvNodeAddress.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onCancelled(NodeInfo result) { //TODO: cancel this on exit from fragment
            Timber.d("cancelled with %s", result);
        }
    }

    private void showNode(NodeInfo nodeInfo) {
        //tvNodeName.setText(nodeInfo.getName());
        //tvNodeName.setCompoundDrawablesWithIntrinsicBounds(NodeInfoAdapter.getPingIcon(nodeInfo), 0, 0, 0);
        //Helper.showTimeDifference(tvNodeAddress, nodeInfo.getTimestamp());
        //tvNodeAddress.setVisibility(View.VISIBLE);
    }

    private void startNodePrefs() {
        activityCallback.onNodePrefs();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    CardView languageCardView;
    CardView aboutCardView;
    CardView privacyPolicyCardView;
    CardView nodeCardView;
    CardView importWalletCardView;
    CardView viewOnlyCardView;
    CardView addressAndPrivateKeyCardView;
    CardView mnemonicsKeyCardView;
    CardView createWalletCardView;
    CardView telegramCardView;
    CardView emailCardView;
    CardView githubCardView;
    CardView twitterCardView;
    CardView termsandconditionsCardView;
    CardView logoutCardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        languageCardView = view.findViewById(R.id.activity_settings_language_cardView);
        aboutCardView = view.findViewById(R.id.activity_settings_about_cardView);
        privacyPolicyCardView = view.findViewById(R.id.activity_settings_privacy_policy_cardView);
        nodeCardView = view.findViewById(R.id.activity_settings_node_cardView);
        importWalletCardView = view.findViewById(R.id.activity_settings_import_wallet_cardView);
        viewOnlyCardView = view.findViewById(R.id.activity_settings_restore_view_only_cardView);
        addressAndPrivateKeyCardView = view.findViewById(R.id.activity_settings_restore_private_key_cardView);
        mnemonicsKeyCardView = view.findViewById(R.id.activity_settings_restore_mnemonics_key_cardView);
        createWalletCardView = view.findViewById(R.id.activity_settings_create_wallet_cardView);
        telegramCardView = view.findViewById(R.id.activity_settings_telegram_cardView);
        emailCardView = view.findViewById(R.id.activity_settings_gmail_cardView);
        githubCardView = view.findViewById(R.id.activity_settings_github_cardView);
        twitterCardView = view.findViewById(R.id.activity_settings_twitter_cardView);
        termsandconditionsCardView = view.findViewById(R.id.activity_settings_terms_and_condition_cardView);
        logoutCardView = view.findViewById(R.id.activity_settings_logout_cardView);

        createWalletCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                activityCallback.onAddWallet(GenerateFragment.TYPE_NEW);
            }
        });

        mnemonicsKeyCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                activityCallback.onAddWallet(GenerateFragment.TYPE_SEED);
            }
        });

        addressAndPrivateKeyCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                activityCallback.onAddWallet(GenerateFragment.TYPE_KEY);
            }
        });

        viewOnlyCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                activityCallback.onAddWallet(GenerateFragment.TYPE_VIEWONLY);
            }
        });

        importWalletCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                activityCallback.onWalletRestore();
            }
        });
        nodeCardView.setOnClickListener(v -> startNodePrefs());
        //nodeCardView.setOnClickListener(v -> findBestNode());
        languageCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChangeLocale();
            }
        });
        aboutCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SettingsActivity)getActivity()).startAboutFragment();
                //AboutFragment.display(getFragmentManager());
            }
        });
        privacyPolicyCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SettingsActivity)getActivity()).startPrivacyPolicyFragment();
                //PrivacyFragment.display(getFragmentManager());
            }
        });
        telegramCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://t.me/beldexcoin"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        emailCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_SEND);
                String[] recipients={"support@beldex.io"};
                intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                intent.putExtra(Intent.EXTRA_SUBJECT,"");
                intent.putExtra(Intent.EXTRA_CC,"support@beldex.io");
                intent.setType("text/html");
                intent.setPackage("com.google.android.gm");
                startActivity(Intent.createChooser(intent, "Send mail"));

            }
        });
        githubCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Uri uri = Uri.parse("https://github.com/Beldex-Coin"); // missing 'http://' will cause crashed
                 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                 startActivity(intent);

            }
        });
        twitterCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uri = Uri.parse("https://twitter.com/BeldexCoin"); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        termsandconditionsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SettingsActivity)getActivity()).startTermsandConditionsFragment();
            }
        });

        logoutCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SettingsActivity)getActivity()).logoutApp();
            }
        });

        return view;
    }

    public boolean isFabOpen() {
        return isFabOpen;
    }

    /*public void animateFAB() {
        if (isFabOpen) { // close the fab
            //fabScreen.setClickable(false);
            //fabScreen.startAnimation(fab_close_screen);
            //fab.startAnimation(rotate_backward);
            if (fabLedgerL.getVisibility() == View.VISIBLE) {
                fabLedgerL.startAnimation(fab_close);
                fabLedger.setClickable(false);
            } else {
                fabNewL.startAnimation(fab_close);
                fabNew.setClickable(false);
                fabViewL.startAnimation(fab_close);
                fabView.setClickable(false);
                fabKeyL.startAnimation(fab_close);
                fabKey.setClickable(false);
                fabSeedL.startAnimation(fab_close);
                fabSeed.setClickable(false);
            }
            isFabOpen = false;
        } else { // open the fab
            //fabScreen.setClickable(true);
            //fabScreen.startAnimation(fab_open_screen);
            //fab.startAnimation(rotate_forward);
            if (activityCallback.hasLedger()) {
                fabLedgerL.setVisibility(View.VISIBLE);
                fabNewL.setVisibility(View.GONE);
                fabViewL.setVisibility(View.GONE);
                fabKeyL.setVisibility(View.GONE);
                fabSeedL.setVisibility(View.GONE);

                fabLedgerL.startAnimation(fab_open);
                fabLedger.setClickable(true);
            } else {
                fabLedgerL.setVisibility(View.GONE);
                fabNewL.setVisibility(View.VISIBLE);
                fabViewL.setVisibility(View.VISIBLE);
                fabKeyL.setVisibility(View.VISIBLE);
                fabSeedL.setVisibility(View.VISIBLE);

                fabNewL.startAnimation(fab_open);
                fabNew.setClickable(true);
                fabViewL.startAnimation(fab_open);
                fabView.setClickable(true);
                fabKeyL.startAnimation(fab_open);
                fabKey.setClickable(true);
                fabSeedL.startAnimation(fab_open);
                fabSeed.setClickable(true);
            }
            isFabOpen = true;
        }
    }*/

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        Timber.d("onClick %d/%d", id, R.id.fabLedger);
        if (id == R.id.fab) {
            //animateFAB();
        } else if (id == R.id.fabNew) {
            //fabScreen.setVisibility(View.INVISIBLE);
            isFabOpen = false;
            activityCallback.onAddWallet(GenerateFragment.TYPE_NEW);
        } else if (id == R.id.fabView) {
            //animateFAB();
            activityCallback.onAddWallet(GenerateFragment.TYPE_VIEWONLY);
        } else if (id == R.id.fabKey) {
            //animateFAB();
            activityCallback.onAddWallet(GenerateFragment.TYPE_KEY);
        } else if (id == R.id.fabSeed) {
            //animateFAB();
            activityCallback.onAddWallet(GenerateFragment.TYPE_SEED);
        } else if (id == R.id.fabLedger) {
            Timber.d("FAB_LEDGER");
            //animateFAB();
            activityCallback.onAddWallet(GenerateFragment.TYPE_LEDGER);
        } else if (id == R.id.fabScreen) {
            //animateFAB();
        }
    }

    // Wallet touched
    @Override
    public void onInteraction(final View view, final WalletManager.WalletInfo infoItem) {
        openWallet(infoItem.getName(), false);
    }
    private void openWallet(String name, boolean streetmode) {
        activityCallback.onWalletSelected(name, streetmode);
    }

    private void showInfo(@NonNull String name) {
        activityCallback.onWalletDetails(name);
    }

    @Override
    public boolean onContextInteraction(MenuItem item, WalletManager.WalletInfo listItem) {
        final int id = item.getItemId();
        if (id == R.id.action_streetmode) {
            openWallet(listItem.getName(), true);
        } else if (id == R.id.action_info) {
            showInfo(listItem.getName());
        } else if (id == R.id.action_rename) {
            activityCallback.onWalletRename(listItem.getName());
        } /*else if (id == R.id.action_backup) {
            activityCallback.onWalletBackup(listItem.getName());
        } */else if (id == R.id.action_archive) {
            activityCallback.onWalletDelete(listItem.getName());
        }/* else if (id == R.id.action_deletecache) {
            activityCallback.onWalletDeleteCache(listItem.getName());
        }*/ else {
            return super.onContextItemSelected(item);
        }
        return true;
    }

    public void onChangeLocale() {
        final ArrayList<Locale> availableLocales = LocaleHelper.getAvailableLocales(getActivity());
        Collections.sort(availableLocales, (locale1, locale2) -> {
            String localeString1 = LocaleHelper.getDisplayName(locale1, true);
            String localeString2 = LocaleHelper.getDisplayName(locale2, true);
            return localeString1.compareTo(localeString2);
        });

        String[] localeDisplayNames = new String[1 + availableLocales.size()];
        localeDisplayNames[0] = getString(R.string.language_system_default);
        for (int i = 1; i < localeDisplayNames.length; i++) {
            localeDisplayNames[i] = LocaleHelper.getDisplayName(availableLocales.get(i - 1), true);
        }

        int currentLocaleIndex = 0;
        String currentLocaleTag = LocaleHelper.getPreferredLanguageTag(getActivity());
        if (!currentLocaleTag.isEmpty()) {
            Locale currentLocale = Locale.forLanguageTag(currentLocaleTag);
            String currentLocaleName = LocaleHelper.getDisplayName(currentLocale, true);
            currentLocaleIndex = Arrays.asList(localeDisplayNames).indexOf(currentLocaleName);
            if (currentLocaleIndex < 0) currentLocaleIndex = 0;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.LanguageAlertDialogBox);
        builder.setTitle(getString(R.string.menu_language));
        builder.setSingleChoiceItems(localeDisplayNames, currentLocaleIndex, (dialog, i) -> {
            dialog.dismiss();

            LocaleHelper.setAndSaveLocale(getActivity(),
                    (i == 0) ? "" : availableLocales.get(i - 1).toLanguageTag());
            startActivity(getActivity().getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        });
        builder.show();
    }
}