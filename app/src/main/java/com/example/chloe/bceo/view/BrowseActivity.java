package com.example.hsianglin.bceoapp.view.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hsianglin.bceoapp.R;


public class BrowseActivity extends AppCompatActivity {

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.androider_01,
            R.drawable.androider_02,
            R.drawable.androider_03,
            R.drawable.androider_04,
            R.drawable.androider_05,
            R.drawable.androider_06,
            R.drawable.androider_07,
            R.drawable.androider_08,
            R.drawable.androider_09,
//            R.drawable.androider_10,
//            R.drawable.androider_11,
//            R.drawable.androider_12,
//            R.drawable.androider_13,
//            R.drawable.androider_14,
//            R.drawable.androider_15,
//            R.drawable.androider_16
    };

    public class MyAdapter extends BaseAdapter {

        private Context mContext;

        public MyAdapter(Context c) {
            // TODO Auto-generated constructor stub
            mContext = c;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mThumbIds.length;
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return mThumbIds[arg0];
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            View grid;

            if(convertView==null){
                grid = new View(mContext);
                LayoutInflater inflater=getLayoutInflater();
                grid=inflater.inflate(R.layout.fragment_grid_item, parent, false);
            }else{
                grid = (View)convertView;
            }

            ImageView imageView = (ImageView)grid.findViewById(R.id.imagepart);
            TextView textView = (TextView)grid.findViewById(R.id.textpart);
            imageView.setImageResource(mThumbIds[position]);
            textView.setText(String.valueOf(position));

            return grid;
        }

    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new MyAdapter(this));
        gridview.setOnItemClickListener(new ItemClickListener());

    }
    public class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent myIntent = new Intent(view.getContext(), ProductActivity.class);
            startActivityForResult(myIntent, 0);
//            UserSubmissionLog userSubmissionLogs= new UserSubmissionLog(position);
//            System.out.println("Position "+position);
        }
    }
}

