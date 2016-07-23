package ca.etsmtl.gti785.peer.activity;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.gson.JsonSyntaxException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ca.etsmtl.gti785.lib.entity.EventEntity;
import ca.etsmtl.gti785.lib.entity.FileEntity;
import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.service.PeerService;
import ca.etsmtl.gti785.lib.service.PeerService.PeerServiceBinder;
import ca.etsmtl.gti785.lib.service.PeerService.PeerServiceListener;
import ca.etsmtl.gti785.peer.fragment.FilesFragment;
import ca.etsmtl.gti785.peer.fragment.FilesFragment.FilesFragmentListener;
import ca.etsmtl.gti785.peer.fragment.PeerFilesFragment;
import ca.etsmtl.gti785.peer.fragment.PeerFilesFragment.PeerFilesFragmentListener;
import ca.etsmtl.gti785.peer.fragment.PeersFragment;
import ca.etsmtl.gti785.peer.fragment.PeersFragment.PeersFragmentListener;
import ca.etsmtl.gti785.peer.fragment.ServerFragment;
import ca.etsmtl.gti785.peer.fragment.ServerFragment.ServerFragmentListener;
import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.util.EditTextPreferenceDialog;
import ca.etsmtl.gti785.peer.util.UriUtil;

// FIXME: Remove unneeded listeners
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FilesFragmentListener, PeerFilesFragmentListener, PeersFragmentListener, ServerFragmentListener {

    private static final int DIRECTORY_REQUEST_CODE = 123;
    private static final int PERMISSIONS_REQUEST_CODE = 456;

    private PeerService service;
//    private PeerEntity selfPeer;

    private RelativeLayout contentLayout;
    private FloatingActionButton addFab;
    private FloatingActionButton editFab;
    private NavigationView navigationView;
    private MenuItem previousMenuItem;

    private PeersFragment peersFragment;
    private ServerFragment serverFragment;
    private FilesFragment filesFragment;

    // Bidirectional map between Menu ItemId and PeerEntity UUID
    private Map<Integer, UUID> mapItemToPeer = new HashMap<>();
    private Map<UUID, Integer> mapPeerToItem = new HashMap<>();

    private boolean actionDirectoryVisible = false;
    private int nextPeerId = Menu.FIRST;
    private boolean bound = false;

//    private PeerRepository peerRepository = new PeerRepository(); // FIXME

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("MainActivity", "onServiceConnected");

            PeerServiceBinder binder = (PeerServiceBinder) iBinder;
            service = binder.getService();
            service.setListener(listener);

            // TODO: Check if path has changed while we were gone.
            // TODO: Same for the name

            peersFragment.updateDataSet(service.getPeerRepository());
            serverFragment.updateDataSet(service.getSelfPeerEntity());
            filesFragment.updateDataSet(service.getFileRepository());

            service.getPeerHive().sync();

            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("MainActivity", "onServiceDisconnected");

            service.setListener(null);

            bound = false;
        }
    };

    private PeerServiceListener listener = new PeerServiceListener() {
        @Override
        public void onServerStart(PeerEntity peerEntity) {

        }

        @Override
        public void onServerError(String message) {

        }

        @Override
        public void onPeerConnection(PeerEntity peerEntity) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("MainActivity", "onPeerConnection");
                    peersFragment.updateDataSet(service.getPeerRepository());
                }
            });
        }

        @Override
        public void onPeerDisplayNameUpdate(final PeerEntity peerEntity) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("MainActivity", "onPeerDisplayNameUpdate");
                    peersFragment.updateDataSet(service.getPeerRepository());

                    SubMenu submenu = navigationView.getMenu().findItem(R.id.nav_peers_submenu).getSubMenu();
                    Integer itemId = mapPeerToItem.get(peerEntity.getUUID());

                    MenuItem item;
                    if (itemId != null) {
                        item = submenu.findItem(itemId);
                        item.setTitle(peerEntity.getDisplayName());
                    }

                    // TODO: Call setTitle() if we are currently on that peer screen
                }
            });
        }

        @Override
        public void onPeerLocationUpdate(PeerEntity peerEntity) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("MainActivity", "onPeerLocationUpdate");
                    peersFragment.updateDataSet(service.getPeerRepository());
                }
            });
        }

        @Override
        public void onPeerUpdate(PeerEntity peerEntity) {

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String serverDirectory = prefs.getString(getString(R.string.pref_server_directory_key), null);
        String serverName = prefs.getString(getString(R.string.pref_server_name_key), null);

        if (serverDirectory == null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.pref_server_directory_key), Environment.getExternalStorageDirectory().getPath());
            editor.apply();

            serverDirectory = prefs.getString(getString(R.string.pref_server_directory_key), Environment.getExternalStorageDirectory().getPath());
        }

        if (serverName == null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.pref_server_name_key), getString(R.string.pref_server_name_default));
            editor.apply();

            serverName = prefs.getString(getString(R.string.pref_server_name_key), getString(R.string.pref_server_name_default));
        }

        // Used for displaying Snackbar
        contentLayout = (RelativeLayout) findViewById(R.id.main_content_layout);

        addFab = (FloatingActionButton) findViewById(R.id.add_fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
            }
        });

        editFab = (FloatingActionButton) findViewById(R.id.edit_fab);
        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditTextPreferenceDialog dialog = new EditTextPreferenceDialog(getActivity(),
                        R.string.pref_server_name_key, R.string.pref_server_name_default);

                dialog.setListner(new EditTextPreferenceDialog.OnValueChangeListner() {
                    @Override
                    public void onValueChange(String value) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String name = prefs.getString(getString(R.string.pref_server_name_key), getString(R.string.pref_server_name_default));

                        // TODO: Do something when service is null
                        if (service != null) {
                            EventEntity event = new EventEntity(EventEntity.Type.DISPLAY_NAME_UPDATE);
                            event.getParams().setDisplayName(name);

                            service.getSelfPeerEntity().setDisplayName(name);
                            service.getQueueRepository().putAll(event);

                            serverFragment.updateDataSet(service.getSelfPeerEntity());
                        }
                    }
                });
                dialog.showDialog();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, PeerService.class);
            intent.putExtra(PeerService.EXTRA_DIRECTORY_PATH, serverDirectory);
            intent.putExtra(PeerService.EXTRA_PEER_NAME, serverName);
            startService(intent);
        }

        // Initializing fixed fragments
        peersFragment = PeersFragment.newInstance();
        serverFragment = ServerFragment.newInstance();
        filesFragment = FilesFragment.newInstance();

        // Perform initial setup by triggering a navigation change
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_peers));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        } else {
            Intent intent = new Intent(this, PeerService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (bound) {
            unbindService(connection);
            bound = false;
            service.setListener(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(this, PeerService.class);
        stopService(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        menu.findItem(R.id.action_directory).setVisible(actionDirectoryVisible);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        } else
        if (id == R.id.action_directory) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, DIRECTORY_REQUEST_CODE);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setCheckable(true);
        item.setChecked(true);

        if (previousMenuItem != null) {
            previousMenuItem.setChecked(false);
        }
        previousMenuItem = item;

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_peers) {
            setTitle(R.string.activity_peers_title);
            setActionDirectoryVisible(false);
            editFab.hide();
            addFab.show();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, peersFragment).commit();
        } else if (id == R.id.nav_status) {
            setTitle(R.string.activity_status_title);
            setActionDirectoryVisible(false);
            addFab.hide();
            editFab.show();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, serverFragment).commit();
        } else if (id == R.id.nav_files) {
            setTitle(R.string.activity_files_title);
            setActionDirectoryVisible(true);
            addFab.hide();
            editFab.hide();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, filesFragment).commit();
        } else {
            UUID uuid = mapItemToPeer.get(id);

            if (uuid != null) {
                // TODO: Do something when service is null
                if (service != null) {
                    PeerEntity peer = service.getPeerRepository().get(uuid);

                    setTitle(item.getTitle());
                    setActionDirectoryVisible(false);
                    addFab.hide();
                    editFab.hide();

                    PeerFilesFragment peerFilesFragment = PeerFilesFragment.newInstance(peer);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, peerFilesFragment).commit();
                }
            }

            Log.d("MainActivity", "onNavigationItemSelected:" + item.getItemId());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("MainActivity", "onActivityResult");

        if (requestCode == DIRECTORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String path = UriUtil.getDocumentPath(getActivity(), data.getData());

                if (path == null) {
                    Snackbar.make(contentLayout, R.string.snackbar_directory_error, Snackbar.LENGTH_LONG).show();
                } else {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(getString(R.string.pref_server_directory_key), path);
                    editor.apply();

                    if (service == null) {
                        Snackbar.make(contentLayout, R.string.snackbar_service_error, Snackbar.LENGTH_LONG).show();
                    } else {
                        service.getFileRepository().removeAll();
                        service.getFileRepository().addAll(path);

                        filesFragment.updateDataSet(service.getFileRepository());
                    }
                }
            }
        } else if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            try {
                PeerEntity peer = PeerEntity.decode(scanResult.getContents());

                if (service == null) {
                    Snackbar.make(contentLayout, R.string.snackbar_service_error, Snackbar.LENGTH_LONG).show();
                } else {
                    if (service.getPeerRepository().addOrUpdate(peer)) {
//                        service.getPeerHive().sync(); // TODO
                        service.getPeerHive().spawnWorker(peer);
                    }

                    peersFragment.updateDataSet(service.getPeerRepository());

                    // Update display name
                    SubMenu submenu = navigationView.getMenu().findItem(R.id.nav_peers_submenu).getSubMenu();
                    Integer itemId = mapPeerToItem.get(peer.getUUID());

                    MenuItem item;
                    if (itemId != null) {
                        item = submenu.findItem(itemId);
                        item.setTitle(peer.getDisplayName());
                    }

                    // TODO: Call setTitle() if we are currently on that peer screen
                }

            } catch (JsonSyntaxException e) {
                Snackbar.make(contentLayout, R.string.snackbar_peer_error, Snackbar.LENGTH_LONG).show(); // FIXME
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay!
                recreate();
            }
        }
    }

    public void setActionDirectoryVisible(boolean actionDirectoryVisible) {
        this.actionDirectoryVisible = actionDirectoryVisible;

        invalidateOptionsMenu();
    }

    @Override
    public void onPeerEntityClick(PeerEntity peerEntity) {
        // TODO: Move this code to the button press listener
        SubMenu submenu = navigationView.getMenu().findItem(R.id.nav_peers_submenu).getSubMenu();
        Integer itemId = mapPeerToItem.get(peerEntity.getUUID());

        MenuItem item;
        if (itemId == null) {
            itemId = nextPeerId++;

            item = submenu.add(Menu.NONE, itemId, Menu.NONE, peerEntity.getDisplayName());
            item.setIcon(R.drawable.ic_phone_android_black_24dp);

            mapItemToPeer.put(itemId, peerEntity.getUUID());
            mapPeerToItem.put(peerEntity.getUUID(), itemId);
        } else {
            item = submenu.findItem(itemId);
            item.setTitle(peerEntity.getDisplayName());
        }

        // Switch to that fragment
        onNavigationItemSelected(item); // FIXME
    }

    @Override
    public void onPeerEntityDismiss(PeerEntity peer) {
        // TODO: Do something when service is null
        if (service != null) {
            service.getPeerHive().killWorker(peer);
            service.getPeerRepository().remove(peer);

            peersFragment.updateDataSet(service.getPeerRepository());
        }
    }

    @Override
    public void onDownloadImageClick(FileEntity file, String host) {
        Uri uri = Uri.parse("http://" + host + "/api/v1/file/" + file.getUuid().toString());

        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.getName());
        dm.enqueue(request);
    }

//    @Override
//    public PeerService getPeerService() {
//        return service;
//    }

//    @Override
//    public PeerEntity getSelfPeerEntity() {
//        return selfPeer;
//    }

    public Activity getActivity() {
        return this;
    }
}
