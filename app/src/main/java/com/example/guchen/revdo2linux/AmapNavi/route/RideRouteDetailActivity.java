package com.example.guchen.revdo2linux.AmapNavi.route;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.WalkPath;
import com.example.guchen.revdo2linux.AmapNavi.util.AMapUtil;
import com.example.guchen.revdo2linux.R;

public class RideRouteDetailActivity extends AppCompatActivity {
    private RidePath mRidePath;
    private WalkPath mWalkPath;
    private TextView mTitle,mTitleWalkRoute;
    private ListView mRideSegmentList;
    private RideSegmentListAdapter mRideSegmentListAdapter;
    private int iRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);

        getIntentData();
        mTitle = (TextView) findViewById(R.id.title_center);
            mTitle.setText("骑行路线详情"); //标题说明
            mTitleWalkRoute = (TextView) findViewById(R.id.firstline);//总路程和时间
            String dur = AMapUtil.getFriendlyTime((int) mRidePath.getDuration());
            String dis = AMapUtil
                    .getFriendlyLength((int) mRidePath.getDistance());
            mTitleWalkRoute.setText(dur + "(" + dis + ")");
            mRideSegmentList = (ListView) findViewById(R.id.bus_segment_list);
            mRideSegmentListAdapter = new RideSegmentListAdapter(
                    this.getApplicationContext(), mRidePath.getSteps());
            mRideSegmentList.setAdapter(mRideSegmentListAdapter);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mRidePath = intent.getParcelableExtra("ride_path");
    }

    public void onBackClick(View view) {
        this.finish();
    }
}
