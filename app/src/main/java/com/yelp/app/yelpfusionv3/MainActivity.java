package com.yelp.app.yelpfusionv3;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.yelp.app.yelpfusionv3.dashboard.Dashboard;
import com.yelp.app.yelpfusionv3.home.HomeFragment;
import com.yelp.app.yelpfusionv3.notification.Notification;

public class MainActivity extends AppCompatActivity {


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment=null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment=new HomeFragment();
                    changeFragment(fragment);
                    return true;
                case R.id.navigation_dashboard:
                    fragment=new Dashboard();
                    changeFragment(fragment);
                    return true;
                case R.id.navigation_notifications:
                    fragment=new Notification();
                    changeFragment(fragment);
                    return true;
            }



            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

       changeFragment(new HomeFragment());


    }

    private void changeFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment).commit();
    }

}
