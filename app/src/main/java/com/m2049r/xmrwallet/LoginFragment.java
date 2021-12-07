/*
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.m2049r.xmrwallet;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.m2049r.xmrwallet.util.Helper.STALE_NODE_HOURS;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.m2049r.xmrwallet.data.NodeInfo;
import com.m2049r.xmrwallet.dialog.ProgressDialog;
import com.m2049r.xmrwallet.layout.NodeInfoAdapter;
import com.m2049r.xmrwallet.layout.WalletInfoAdapter;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.util.Helper;
import com.m2049r.xmrwallet.util.KeyStoreHelper;
import com.m2049r.xmrwallet.util.NodePinger;
import com.m2049r.xmrwallet.util.Notice;
import com.m2049r.xmrwallet.util.ThemeHelper;
import com.m2049r.xmrwallet.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import timber.log.Timber;
import android.view.View.*;

public class LoginFragment extends Fragment implements WalletInfoAdapter.OnInteractionListener,
        View.OnClickListener {

    private AppUpdateManager appUpdateManager;
    private static final int IMMEDIATE_APP_UPDATE_REQ_CODE = 124;

    private WalletInfoAdapter adapter;

    private final List<WalletManager.WalletInfo> walletList = new ArrayList<>();

    private View tvGuntherSays;
    private ImageView ivGunther;
    private TextView tvNodeName;
    private TextView tvNodeAddress;
    private View pbNode;
    private View llNode;
    private LinearLayout walletActionButtonLinearLayout;
    private Button createWalletButton;
    private Button restoreWalletButton;
    //private ImageView ivGuntherLogo;
    //private TextView tvGuntherText;

    private Listener activityCallback;
    //ProgressDialog progressDialog;

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
        if (context instanceof Listener) {
            this.activityCallback = (Listener) context;
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
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            Timber.d("onResume-->(1)");
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    Timber.d("onResume-->(2)");
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            AppUpdateType.IMMEDIATE,
                                            getActivity(),
                                            IMMEDIATE_APP_UPDATE_REQ_CODE);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
        Timber.d("onResume() %s", activityCallback.getFavouriteNodes().size());
        activityCallback.setTitle(null);
        activityCallback.setToolbarButton(Toolbar.BUTTON_CREDITS);
        activityCallback.showNet();
        pingSelectedNode();
    }

    //-->
    private void checkUpdate() {

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        Timber.d("Else Start");
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            Timber.d("Else Start--> %s", appUpdateInfo.updateAvailability());
            Timber.d("Else Start---> %s", UpdateAvailability.UPDATE_AVAILABLE);
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                Timber.d("If");
                startUpdateFlow(appUpdateInfo);
            } else if  (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                Timber.d("Else If");
                startUpdateFlow(appUpdateInfo);
            }else{
                Timber.d("Else");
            }
        });
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, getActivity(), IMMEDIATE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Update canceled by user! Result Code: " + resultCode, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_OK) {
                Toast.makeText(getActivity(), "Update success! Result Code: " + resultCode, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Update Failed! Result Code: " + resultCode, Toast.LENGTH_LONG).show();
                checkUpdate();
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Timber.d("onCreateView");
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        appUpdateManager = AppUpdateManagerFactory.create(getActivity());
        checkUpdate();

        tvGuntherSays = view.findViewById(R.id.tvGuntherSays);
        ivGunther = view.findViewById(R.id.ivGunther);
        //ivGuntherLogo = view.findViewById(R.id.ivGuntherLogo);
        //tvGuntherText = view.findViewById(R.id.tvGuntherText);
        //fabScreen = view.findViewById(R.id.fabScreen);
        //fab = view.findViewById(R.id.fab);
        //fabNew = view.findViewById(R.id.fabNew);
        //fabView = view.findViewById(R.id.fabView);
        //fabKey = view.findViewById(R.id.fabKey);
        //fabSeed = view.findViewById(R.id.fabSeed);
        //fabLedger = view.findViewById(R.id.fabLedger);

        //fabNewL = view.findViewById(R.id.fabNewL);
        //fabViewL = view.findViewById(R.id.fabViewL);
        //fabKeyL = view.findViewById(R.id.fabKeyL);
        //fabSeedL = view.findViewById(R.id.fabSeedL);
        //fabLedgerL = view.findViewById(R.id.fabLedgerL);

        fab_pulse = AnimationUtils.loadAnimation(getContext(), R.anim.fab_pulse);
        fab_open_screen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open_screen);
        fab_close_screen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close_screen);
        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_backward);
        //fab.setOnClickListener(this);
        //fabNew.setOnClickListener(this);
        //fabView.setOnClickListener(this);
        //fabKey.setOnClickListener(this);
        //fabSeed.setOnClickListener(this);
        //fabLedger.setOnClickListener(this);
        //fabScreen.setOnClickListener(this);

        RecyclerView recyclerView = view.findViewById(R.id.list);
        registerForContextMenu(recyclerView);
        this.adapter = new WalletInfoAdapter(getActivity(), this);
        recyclerView.setAdapter(adapter);

        //ViewGroup llNotice = view.findViewById(R.id.llNotice);
        //Notice.showAll(llNotice, ".*_login");

        pbNode = view.findViewById(R.id.pbNode);
        //progressDialog = new ProgressDialog(getContext());
        llNode = view.findViewById(R.id.llNode);
        llNode.setOnClickListener(v -> startNodePrefs());
        tvNodeName = view.findViewById(R.id.tvNodeName);
        tvNodeAddress = view.findViewById(R.id.tvNodeAddress);
        walletActionButtonLinearLayout = view.findViewById(R.id.walletActionButtonLinearLayout);
        createWalletButton = view.findViewById(R.id.createWalletButton);
        restoreWalletButton = view.findViewById(R.id.restoreWalletButton);
        createWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFabOpen=false;
                activityCallback.onAddWallet(GenerateFragment.TYPE_NEW);            }
        });

        restoreWalletButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               /* animateFAB();
                activityCallback.onAddWallet(GenerateFragment.TYPE_SEED);*/
                showDialog(getActivity(),"Restore Wallet Using","");
            }
        });
        view.findViewById(R.id.ibRenew).setOnClickListener(v -> findBestNode());

        Helper.hideKeyboard(getActivity());

        loadList();

        return view;
    }
    public void showDialog(Activity activity,String title, CharSequence message) {
        //AlertDialog.Builder builder = new MaterialAlertDialogBuilder(activity);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity,R.style.backgroundColor);

        if (title != null) builder.setTitle(title);

        builder.setMessage(message);
        builder.setPositiveButton("25 Word Seed", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                animateFAB();
                activityCallback.onAddWallet(GenerateFragment.TYPE_SEED);
            } });
        builder.setNegativeButton("Address and Private Key", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                animateFAB();
                activityCallback.onAddWallet(GenerateFragment.TYPE_KEY);
            } });
        builder.show();
    }

    // Callbacks from WalletInfoAdapter

    // Wallet touched
    @Override
    public void onInteraction(final View view, final WalletManager.WalletInfo infoItem) {
        openWallet(infoItem.getName(), false);
    }

    private void openWallet(String name, boolean streetmode) {
        activityCallback.onWalletSelected(name, streetmode);
    }

    // Method to show Progress bar
    private void showProgressDialogWithTitle(String substring) {
        //Without this user can hide loader by tapping outside screen
        //progressDialog.setCancelable(false);
        //progressDialog.setMessage(substring);
        //progressDialog.show();
    }

    // Method to hide/ dismiss Progress bar
    private void hideProgressDialogWithTitle() {
        //progressDialog.dismiss();
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
        } */ else if (id == R.id.action_archive) {
            activityCallback.onWalletDelete(listItem.getName());
        }/* else if (id == R.id.action_deletecache) {
            activityCallback.onWalletDeleteCache(listItem.getName());
        }*/ else {
            return super.onContextItemSelected(item);
        }
        return true;
    }

    public void loadList() {
        Timber.d("loadList()");
        WalletManager mgr = WalletManager.getInstance();
        walletList.clear();
        walletList.addAll(mgr.findWallets(activityCallback.getStorageRoot()));
        adapter.setInfos(walletList);

        // deal with Gunther & FAB animation
        if (walletList.isEmpty()) {
            //fab.startAnimation(fab_pulse);
            if (ivGunther.getDrawable() == null) {
                //ivGunther.setImageResource(R.drawable.ic_emptygunther);
                ivGunther.setImageResource(R.drawable.ic_empty_wallet_image);
                tvGuntherSays.setVisibility(View.VISIBLE);
                walletActionButtonLinearLayout.setVisibility(View.VISIBLE);
                //ivGuntherLogo.setVisibility(View.VISIBLE);
                //tvGuntherText.setVisibility(View.VISIBLE);
            }
        } else {
            //fab.clearAnimation();
            if (ivGunther.getDrawable() != null) {
                ivGunther.setImageDrawable(null);
            }
            tvGuntherSays.setVisibility(View.GONE);
            walletActionButtonLinearLayout.setVisibility(View.GONE);
            //ivGuntherLogo.setVisibility(View.GONE);
            //tvGuntherText.setVisibility(View.GONE);
        }

        // remove information of non-existent wallet
        Set<String> removedWallets = getActivity()
                .getSharedPreferences(KeyStoreHelper.SecurityConstants.WALLET_PASS_PREFS_NAME, Context.MODE_PRIVATE)
                .getAll().keySet();
        for (WalletManager.WalletInfo s : walletList) {
            removedWallets.remove(s.getName());
        }
        for (String name : removedWallets) {
            KeyStoreHelper.removeWalletUserPass(getActivity(), name);
        }
    }

    private void showInfo(@NonNull String name) {
        activityCallback.onWalletDetails(name);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private boolean isFabOpen = false;
    //private FloatingActionButton fab, fabNew, fabView, fabKey, fabSeed, fabLedger;
    //private FrameLayout fabScreen;
    //private RelativeLayout fabNewL, fabViewL, fabKeyL, fabSeedL, fabLedgerL;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward, fab_open_screen, fab_close_screen;
    private Animation fab_pulse;

    public boolean isFabOpen() {
        return isFabOpen;
    }

    public void animateFAB() {
        if (isFabOpen) { // close the fab
            //fabScreen.setClickable(false);
            //fabScreen.startAnimation(fab_close_screen);
            //fab.startAnimation(rotate_backward);
            /*if (fabLedgerL.getVisibility() == View.VISIBLE) {
                fabLedgerL.startAnimation(fab_close);
                //fabLedger.setClickable(false);
            } else {
                fabNewL.startAnimation(fab_close);
                //fabNew.setClickable(false);
                fabViewL.startAnimation(fab_close);
                //fabView.setClickable(false);
                fabKeyL.startAnimation(fab_close);
                //fabKey.setClickable(false);
                fabSeedL.startAnimation(fab_close);
                //fabSeed.setClickable(false);
            }*/
            isFabOpen = false;
        } else { // open the fab
            //fabScreen.setClickable(true);
            //fabScreen.startAnimation(fab_open_screen);
            //fab.startAnimation(rotate_forward);
            /*if (activityCallback.hasLedger()) {
                fabLedgerL.setVisibility(View.VISIBLE);
                fabNewL.setVisibility(View.GONE);
                fabViewL.setVisibility(View.GONE);
                fabKeyL.setVisibility(View.GONE);
                fabSeedL.setVisibility(View.GONE);

                fabLedgerL.startAnimation(fab_open);
                //fabLedger.setClickable(true);
            } else {
                fabLedgerL.setVisibility(View.GONE);
                fabNewL.setVisibility(View.VISIBLE);
                fabViewL.setVisibility(View.VISIBLE);
                fabKeyL.setVisibility(View.VISIBLE);
                fabSeedL.setVisibility(View.VISIBLE);

                fabNewL.startAnimation(fab_open);
               // fabNew.setClickable(true);
                fabViewL.startAnimation(fab_open);
                //fabView.setClickable(true);
                fabKeyL.startAnimation(fab_open);
                //fabKey.setClickable(true);
                fabSeedL.startAnimation(fab_open);
                //fabSeed.setClickable(true);
            }*/
            isFabOpen = true;
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        Timber.d("onClick %d/%d", id, R.id.fabLedger);
        if (id == R.id.fab) {
            animateFAB();
        } else if (id == R.id.fabNew) {
            //fabScreen.setVisibility(View.INVISIBLE);
            isFabOpen = false;
            activityCallback.onAddWallet(GenerateFragment.TYPE_NEW);
        } else if (id == R.id.fabView) {
            animateFAB();
            activityCallback.onAddWallet(GenerateFragment.TYPE_VIEWONLY);
        } else if (id == R.id.fabKey) {
            animateFAB();
            activityCallback.onAddWallet(GenerateFragment.TYPE_KEY);
        } else if (id == R.id.fabSeed) {
            animateFAB();
            activityCallback.onAddWallet(GenerateFragment.TYPE_SEED);
        } else if (id == R.id.fabLedger) {
            Timber.d("FAB_LEDGER");
            animateFAB();
            activityCallback.onAddWallet(GenerateFragment.TYPE_LEDGER);
        } /*else if (id == R.id.fabScreen) {
            animateFAB();
        }*/
    }

    public void findBestNode() {
        new AsyncFindBestNode().execute(AsyncFindBestNode.FIND_BEST);
    }

    public void pingSelectedNode() {
        new AsyncFindBestNode().execute(AsyncFindBestNode.PING_SELECTED);
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
            pbNode.setVisibility(View.VISIBLE);
            //showProgressDialogWithTitle("Connecting to Remote Node");
            llNode.setVisibility(View.INVISIBLE);
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
                Timber.d("Testing-->12");
                activityCallback.setNode(selectedNode);
                return selectedNode;
            } else {
                Timber.d("Testing-->13");
                activityCallback.setNode(null);
                return null;
            }
        }

        @Override
        protected void onPostExecute(NodeInfo result) {
            if (!isAdded()) return;
            pbNode.setVisibility(View.INVISIBLE);
            //hideProgressDialogWithTitle();
            llNode.setVisibility(View.VISIBLE);
            if (result != null) {
                Timber.d("found a good node %s", result.toString());
                final Context ctx = tvNodeAddress.getContext();
                final long now = Calendar.getInstance().getTimeInMillis() / 1000;
                final long secs = (now - result.getTimestamp());
                final long mins = secs / 60; // in minutes
                final long hours = mins / 60;
                final long days = hours / 24;
                String msg;
                if (mins < 2) {
                    msg = ctx.getString(R.string.node_updated_now, secs);
                } else if (hours < 2) {
                    msg = ctx.getString(R.string.node_updated_mins, mins);
                } else if (days < 2) {
                    msg = ctx.getString(R.string.node_updated_hours, hours);
                } else {
                    msg = ctx.getString(R.string.node_updated_days, days);
                }
                Toast.makeText(getContext(), result.getName() + " connected\n" + msg, Toast.LENGTH_SHORT).show();
                showNode(result);
            } else {
                tvNodeName.setText(getResources().getText(R.string.node_create_hint));
                tvNodeName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                tvNodeAddress.setText(null);
                tvNodeAddress.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onCancelled(NodeInfo result) { //TODO: cancel this on exit from fragment
            Timber.d("cancelled with %s", result);
        }
    }

    private void showNode(NodeInfo nodeInfo) {
        tvNodeName.setText(nodeInfo.getName());
        tvNodeName.setCompoundDrawablesWithIntrinsicBounds(NodeInfoAdapter.getPingIcon(nodeInfo), 0, 0, 0);
        Helper.showTimeDifference(tvNodeAddress, nodeInfo.getTimestamp());
        tvNodeAddress.setVisibility(View.VISIBLE);
    }

    private void startNodePrefs() {
        activityCallback.onNodePrefs();
    }
}
