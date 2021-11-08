package edu.lehigh.cse216.rok224;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.navigation.ui.AppBarConfiguration;
import edu.lehigh.cse216.rok224.databinding.ActivityMainBinding;
import android.util.Log;
import androidx.recyclerview.widget.*;
import android.widget.Toast;

import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.toolbox.*;
import com.android.volley.*;
import android.content.Intent;

import java.util.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    /**
     * mData holds the data we get from Volley
     */
    ArrayList<Datum> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://www.cse.lehigh.edu/~spear/5k.json";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                populateListFromVolley(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("rok224", "That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent i = new Intent(getApplicationContext(), SecondActivity.class);
            i.putExtra("label_contents", "CSE216 is the best");
            startActivityForResult(i, 789); // 789 is the number that will come back to us
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
        return true;
    }

    private void populateListFromVolley(String response){
        try {
            JSONArray json= new JSONArray(response);
            for (int i = 0; i < json.length(); ++i) {
                int num = json.getJSONObject(i).getInt("num");
                String str = json.getJSONObject(i).getString("str");
                mData.add(new Datum(num, str));
            }
        } catch (final JSONException e) {
            Log.d("rok224", "Error parsing JSON file: " + e.getMessage());
            return;
        }
        Log.d("rok224", "Successfully parsed JSON file.");
        RecyclerView rv = (RecyclerView) findViewById(R.id.datum_list_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ItemListAdapter adapter = new ItemListAdapter(this, mData);
        rv.setAdapter(adapter);
        adapter.setClickListener(new ItemListAdapter.ClickListener() {
            @Override
            public void onClick(Datum d) {
                Toast.makeText(MainActivity.this, d.mIndex + " --> " + d.mText, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == 789) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the "extra" string of data
                Toast.makeText(MainActivity.this, data.getStringExtra("result"), Toast.LENGTH_LONG).show();
            }
        }
    }

}

