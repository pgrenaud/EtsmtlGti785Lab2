package ca.etsmtl.gti785.peer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import ca.etsmtl.gti785.peer.fragment.FilesFragment;
import ca.etsmtl.gti785.peer.fragment.PeersFragment;
import ca.etsmtl.gti785.peer.fragment.ServerFragment;
import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.util.EditTextPreferenceDialog;
import ca.etsmtl.gti785.peer.util.UriUtil;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int DIRECTORY_REQUEST_CODE = 123;

    private RelativeLayout contentLayout;
    private FloatingActionButton addFab;
    private FloatingActionButton editFab;
    private MenuItem previousMenuItem;

    private PeersFragment peersFragment;
    private ServerFragment serverFragment;
    private FilesFragment filesFragment;

    private boolean actionDirectoryVisible = false;
    private int nextPeerId = Menu.FIRST;

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
        }

        if (serverName == null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.pref_server_name_key), getString(R.string.pref_server_name_default));
            editor.apply();
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

                dialog.setListener(new EditTextPreferenceDialog.OnValueChangeListener() {
                    @Override
                    public void onValueChange(String value) {
                        serverFragment.reloadStatus();
                    }
                });
                dialog.showDialog();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initializing fixed fragments
        peersFragment = PeersFragment.newInstance();
        serverFragment = ServerFragment.newInstance();
        filesFragment = FilesFragment.newInstance();

        // Perform initial setup by triggering a navigation change
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_peers));
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
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_directory) {
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
            setTitle(item.getTitle());
            setActionDirectoryVisible(false);
            addFab.hide();
            editFab.hide();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

                    filesFragment.reloadFiles();
                }
            }
        } else if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

//            scanResult.getContents() // TODO

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

            SubMenu submenu = navigationView.getMenu().findItem(R.id.nav_peers_submenu).getSubMenu();

            MenuItem item = submenu.add(Menu.NONE, nextPeerId, Menu.NONE, "Peer " + nextPeerId);
            item.setIcon(R.drawable.ic_phone_android_black_24dp);

            nextPeerId++;
        }
    }

    public Activity getActivity() {
        return this;
    }

    public void setActionDirectoryVisible(boolean actionDirectoryVisible) {
        this.actionDirectoryVisible = actionDirectoryVisible;

        invalidateOptionsMenu();
    }
}
