package ca.etsmtl.gti785.peer.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import ca.etsmtl.gti785.peer.fragment.FilesFragment;
import ca.etsmtl.gti785.peer.fragment.PeersFragment;
import ca.etsmtl.gti785.peer.fragment.ServerFragment;
import ca.etsmtl.gti785.peer.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FloatingActionButton fab;
    private MenuItem previousMenuItem;

    private PeersFragment peersFragment;
    private ServerFragment serverFragment;
    private FilesFragment filesFragment;

    private int nextPeerId = Menu.FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

                SubMenu submenu = navigationView.getMenu().findItem(R.id.nav_peers_submenu).getSubMenu();

                MenuItem item = submenu.add(Menu.NONE, nextPeerId, Menu.NONE, "Peer " + nextPeerId);
                item.setIcon(R.drawable.ic_phone_android_black_24dp);

                nextPeerId++;
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
            fab.show();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, peersFragment).commit();
        } else if (id == R.id.nav_status) {
            setTitle(R.string.activity_status_title);
            fab.hide();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, serverFragment).commit();
        } else if (id == R.id.nav_files) {
            setTitle(R.string.activity_files_title);
            fab.hide();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, filesFragment).commit();
        } else {
            setTitle(item.getTitle());
            fab.hide();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
