package com.example.guchen.revdo2linux.AmapNavi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.example.guchen.revdo2linux.AmapNavi.overlay.DrivingRouteOverlay;
import com.example.guchen.revdo2linux.AmapNavi.overlay.RideRouteOverlay;
import com.example.guchen.revdo2linux.AmapNavi.overlay.WalkRouteOverlay;
import com.example.guchen.revdo2linux.AmapNavi.route.DriveRouteDetailActivity;
import com.example.guchen.revdo2linux.AmapNavi.route.RideRouteDetailActivity;
import com.example.guchen.revdo2linux.AmapNavi.route.WalkRouteDetailActivity;
import com.example.guchen.revdo2linux.AmapNavi.util.AMapUtil;
import com.example.guchen.revdo2linux.AmapNavi.util.ToastUtil;
import com.example.guchen.revdo2linux.MyApplication;
import com.example.guchen.revdo2linux.R;

import static com.example.guchen.revdo2linux.R.color.blue;
import static com.example.guchen.revdo2linux.R.color.grey;

public class RoutePlanActivity extends AppCompatActivity implements RouteSearch.OnRouteSearchListener {
    private LatLonPoint mStartPoint = null;
    private LatLonPoint mEndPoint = null;
    private Button buttonCar;
    private Button buttonBike;
    private Button buttonWalk;
    private Button buttonStartNavi;
    private TextView textViewRouteInfo,textViewDetail;
    Double elatitude = null;
    Double elongitude = null;
    int iRoute=0; //传递给导航的路径方式，0代表骑行，1代表驾车，2代表步行

    private RouteSearch mRouteSearch;
    private RideRouteResult mRideRouteResult;
    private WalkRouteResult mWalkRouteResult;
    private DriveRouteResult mDriveRouteResult;

    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_plan);

        //获得起点的坐标
        MyApplication myApplication=(MyApplication) getApplication();//也可以用getApplicationContext();
        myApplication.get_Start_Point(); //正式发起导航前，再次定位当前的位置
        Double slatitude = myApplication.slatitude;
        Double slongitude = myApplication.slongitude;
        if((slatitude==null)||(slongitude==null))
            Toast.makeText(this,"无法获取当前位置", Toast.LENGTH_SHORT).show();
        else
            mStartPoint = new LatLonPoint(slatitude, slongitude);

        try{
            //获得终点的坐标
            Bundle bundle = this.getIntent().getExtras();
            elatitude = bundle.getDouble("elatitude");
            elongitude = bundle.getDouble("elongitude");
            mEndPoint = new LatLonPoint(elatitude, elongitude);
        }catch (Exception e){
            e.printStackTrace();
        }

        //显示地图
        MapView mapView = (MapView) findViewById(R.id.navi_route_plan);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();

        //路线搜索初始化设定
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint,mEndPoint);

        buttonCar=(Button)findViewById(R.id.buttonCar);
        buttonBike=(Button)findViewById(R.id.buttonBike);
        buttonWalk=(Button)findViewById(R.id.buttonWalk);
        buttonStartNavi=(Button)findViewById(R.id.buttonStartNavi);
        buttonCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iRoute=1;
                // 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
                RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null, null, "");
                mRouteSearch.calculateDriveRouteAsyn(query); //发起驾车规划路径计算
                buttonCar.setBackgroundColor(getResources().getColor(blue));
                buttonBike.setBackgroundColor(getResources().getColor(grey));
                buttonWalk.setBackgroundColor(getResources().getColor(grey));
            }
        });
        buttonBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iRoute=0;
                RouteSearch.RideRouteQuery query = new RouteSearch.RideRouteQuery(fromAndTo, RouteSearch.RidingDefault);
                mRouteSearch.calculateRideRouteAsyn(query); //发起骑行规划路径计算
                buttonBike.setBackgroundColor(getResources().getColor(blue));
                buttonCar.setBackgroundColor(getResources().getColor(grey));
                buttonWalk.setBackgroundColor(getResources().getColor(grey));
            }
        });
        buttonWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iRoute=2;
                RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
                mRouteSearch.calculateWalkRouteAsyn(query); //发起步行行规划路径计算
                buttonWalk.setBackgroundColor(getResources().getColor(blue));
                buttonBike.setBackgroundColor(getResources().getColor(grey));
                buttonCar.setBackgroundColor(getResources().getColor(grey));
            }
        });
        buttonStartNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Intent intent=new Intent(RoutePlanActivity.this,NaviActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putDouble("elatitude",elatitude);
                    bundle.putDouble("elongitude", elongitude);
                    bundle.putInt("iRoute",iRoute);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        textViewRouteInfo=(TextView)findViewById(R.id.textViewRouteInfo);
        textViewDetail=(TextView)findViewById(R.id.textViewDetail);

        //默认进入路径规划界面时，使用骑行模式
        RouteSearch.RideRouteQuery query = new RouteSearch.RideRouteQuery(fromAndTo, RouteSearch.RidingDefault);
        mRouteSearch.calculateRideRouteAsyn(query); //发起骑行规划路径计算
        show_Local_Point(); //显示当前位置定位绿点
        buttonBike.setBackgroundColor(getResources().getColor(blue));
        buttonCar.setBackgroundColor(getResources().getColor(grey));
        buttonWalk.setBackgroundColor(getResources().getColor(grey));
    }

    public void show_Local_Point(){
        //显示定位蓝点,会自动显示到当前的城市（不管上面的坐标设到哪里）
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        //myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW); //只定位一次
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
        Log.w("xxx","onDriveRouteSearched");
        //dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物

        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mDriveRouteResult = result;
                    final DrivePath drivePath = mDriveRouteResult.getPaths()
                            .get(0);
                    //添加驾车路线
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            this, aMap, drivePath,
                            mDriveRouteResult.getStartPos(),
                            mDriveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                    drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    //mBottomLayout.setVisibility(View.VISIBLE);

                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    Log.w("xxx","distance="+dis);
                    Log.w("xxx","duration="+dur);
                    String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
                    textViewRouteInfo.setText("导航模式：驾车"+"  "+des);
                    Log.w("xxx",des);
                    show_Local_Point(); //显示当前位置定位绿点

                    textViewDetail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(RoutePlanActivity.this,DriveRouteDetailActivity.class);
                            intent.putExtra("drive_path", drivePath);
                            intent.putExtra("drive_result", mDriveRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (result != null && result.getPaths() == null) {
                   ToastUtil.show(this, R.string.no_result);
                }

            } else {
                ToastUtil.show(this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }

    }

    @Override
    public void onRideRouteSearched(RideRouteResult result, int errorCode) {
        Log.w("xxx","onRideRouteSearched");
        //解析result获取算路结果，可参考官方demo
        //dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mRideRouteResult = result;
                    final RidePath ridePath = mRideRouteResult.getPaths()
                            .get(0);
                    //添加步行路线
                    RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(
                            this, aMap, ridePath,
                            mRideRouteResult.getStartPos(),
                            mRideRouteResult.getTargetPos());
                    rideRouteOverlay.removeFromMap();
                    rideRouteOverlay.addToMap();
                    rideRouteOverlay.zoomToSpan();
                    //mBottomLayout.setVisibility(View.VISIBLE);

                    int dis = (int) ridePath.getDistance();//总距离，单位米
                    int dur = (int) ridePath.getDuration();//总里程，单位秒
                    Log.w("xxx","distance="+dis);
                    Log.w("xxx","duration="+dur);
                    String des = AMapUtil.getFriendlyTime(dur)+"("+ AMapUtil.getFriendlyLength(dis)+")";
                    textViewRouteInfo.setText("导航模式：骑行"+"  "+des);
                    Log.w("xxx",des);
                    show_Local_Point(); //显示当前位置定位绿点
                    textViewDetail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(RoutePlanActivity.this,RideRouteDetailActivity.class);
                            intent.putExtra("ride_path", ridePath);
                            intent.putExtra("ride_result", mRideRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(this, R.string.no_result);
                }
            } else {
                ToastUtil.show(this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }

    }

    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        Log.w("xxx","onWalkRouteSearched");
        //dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mWalkRouteResult = result;
                    final WalkPath walkPath = mWalkRouteResult.getPaths()
                            .get(0);
                    //添加步行路线
                    WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                            this, aMap, walkPath,
                            mWalkRouteResult.getStartPos(),
                            mWalkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();
                    //mBottomLayout.setVisibility(View.VISIBLE);

                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    Log.w("xxx","distance="+dis);
                    Log.w("xxx","duration="+dur);
                    String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
                    textViewRouteInfo.setText("导航模式：步行"+"  "+des);
                    Log.w("xxx",des);
                    show_Local_Point(); //显示当前位置定位绿点

                    textViewDetail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(RoutePlanActivity.this,WalkRouteDetailActivity.class);
                            intent.putExtra("walk_path", walkPath);
                            intent.putExtra("walk_result", mWalkRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (result != null && result.getPaths() == null) {
                  ToastUtil.show(this, R.string.no_result);
                }
            } else {
                ToastUtil.show(this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }
}
