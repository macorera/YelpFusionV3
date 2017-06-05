package com.yelp.app.yelpfusionv3.home;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.yelp.app.yelpfusionv3.R;
import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.connection.YelpFusionApiFactory;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.SearchResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;


public class HomeFragment extends Fragment implements OnMapReadyCallback {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleMap mMap;
    public Context context;
    ImageButton btn_home;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        context = getActivity().getApplicationContext();
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        fragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(40.705311, -74.2581883);
        CameraUpdate cameraPosition = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.moveCamera(cameraPosition);

        new AsyncCaller(latLng,context,googleMap).execute();



    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            try {
                Intent intent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                .build(getActivity());
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException e) {
                // TODO: Handle the error.
                Toast.makeText(getActivity().getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            } catch (GooglePlayServicesNotAvailableException e) {
                // TODO: Handle the error.
                Toast.makeText(getActivity().getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode ==getActivity().RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity().getApplicationContext(), data);
                CameraUpdate cameraPosition = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15);
                mMap.clear();
                new AsyncCaller(place.getLatLng(),context,mMap).execute();

                mMap.moveCamera(cameraPosition);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity().getApplicationContext(), data);



            } else if (resultCode == getActivity().RESULT_CANCELED) {

            }
        }
    }


    private class AsyncCaller extends AsyncTask<Void, Business, Boolean>
    {
        private final Context mCntxt;
        private final GoogleMap mGMap;
        private final  Map<Marker, Business> imageStringMapMarker;
        private final  LatLng location;

        public AsyncCaller(LatLng location,Context mCntxt, GoogleMap mGMap) {
            this.mCntxt = mCntxt;
            this.mGMap = mGMap;
            this.imageStringMapMarker=new HashMap<Marker, Business>();
            this.location=location;
        }

        private void drawMarker(final GoogleMap gmap, Business business)
        {


            final Marker marker=gmap.addMarker(new MarkerOptions()
                    .position(new LatLng(business.getCoordinates().getLatitude(),business.getCoordinates().getLongitude()))
                    .title(business.getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker)));


            imageStringMapMarker.put(marker,business);

            gmap.setInfoWindowAdapter(new CustomWindowAdapter(getActivity().getLayoutInflater(),
                    imageStringMapMarker, context));

            gmap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(final Marker mark) {


                    mark.showInfoWindow();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mark.showInfoWindow();
                            int zoom = (int)gmap.getCameraPosition().zoom;
                            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(mark.getPosition().latitude + (double)90/Math.pow(2, zoom), mark.getPosition().longitude), zoom);
                            gmap.animateCamera(cu);

                        }
                    }, 100);

                    return true;
                }
            });




        }

        ProgressDialog pdLoading = new ProgressDialog(getActivity());


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }
        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                YelpFusionApiFactory apiFactory = new YelpFusionApiFactory();
                YelpFusionApi yelpFusionApi = apiFactory.createAPI("G71qMX8xqmVqcKII_LhETQ", "260ffs8wtZ7Ws2X6YgLaMN11vSwRJF3iOIONokHTpL8uD5IwuJbPG4tTLSXHr1ha");
                Map<String, String> yelpParams = new HashMap<>();
                yelpParams.put("term", "restaurants");
                yelpParams.put("latitude", location.latitude+"");
                yelpParams.put("longitude",location.longitude+"");
                Call<SearchResponse> call = yelpFusionApi.getBusinessSearch(yelpParams);
                SearchResponse searchResponse = call.execute().body();
                if (searchResponse.getTotal()==0)
                {
                    return false;
                }
                ArrayList<Business> businesses = searchResponse.getBusinesses();


                for (Business business:businesses){

                    publishProgress(business);

                }


            } catch (Exception e) {
                Toast.makeText(mCntxt,e.toString(),Toast.LENGTH_SHORT).show();
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            pdLoading.dismiss();
        }

        @Override
        protected void onProgressUpdate(Business... values) {
            super.onProgressUpdate(values);
            drawMarker(this.mGMap,values[0]);


        }
    }

    class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater mInflater;
        Map<Marker, Business> imageStringMapMarker;
        Context context;

        public CustomWindowAdapter(LayoutInflater i,  Map<Marker, Business> imageStringMapMarker2, Context context ){
            this.mInflater = i;
            this.imageStringMapMarker = imageStringMapMarker2;
            this.context=context;
        }



        @Override
        public View getInfoContents(final Marker marker) {

            return null;

        }

        @Override
        public View getInfoWindow(Marker marker) {
            // TODO Auto-generated method stub
            Business business=imageStringMapMarker.get(marker);
            View myContentView = mInflater.inflate(
                    R.layout.custom_infowindow, null);

            try {

                TextView tvTitle = ((TextView) myContentView
                        .findViewById(R.id.tvIwLocationName));
                tvTitle.setText(business.getName().toString());

                TextView tvAddress = ((TextView) myContentView
                        .findViewById(R.id.tvIwAddress));
                StringBuilder builder = new StringBuilder();
                for (String details : business.getLocation().getDisplayAddress()) {
                    builder.append(details + ", ");
                }

                tvAddress.setText(builder.toString());


                TextView tvPhone = ((TextView) myContentView
                        .findViewById(R.id.tvIwTelephone));
                tvPhone.setText(business.getDisplayPhone());

                ImageView ivThumbnail = (ImageView) myContentView.findViewById(R.id.ivIwLocationAvatar);
                Picasso.with(context).load(business.getImageUrl()).placeholder(R.mipmap.ic_business_avatar).resize(256,256).into(ivThumbnail);

                RatingBar rbRatingBar= (RatingBar) myContentView.findViewById(R.id.rbIwratingbar);
                rbRatingBar.setRating(business.getReviewCount());

            }
            catch (Exception e){
                Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
            }



            return myContentView;
        }
    }

}
