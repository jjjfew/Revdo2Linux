package com.example.guchen.revdo2linux.AmapNavi;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviGuide;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLink;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.example.guchen.revdo2linux.MyApplication;
import com.example.guchen.revdo2linux.R;

import java.util.ArrayList;
import java.util.List;

public class NaviActivity extends AppCompatActivity implements AMapNaviListener, AMapNaviViewListener {
    private int iRoute=0 ; //0代表骑行，1代表驾车，2代表步行

    private AMapNaviPath mAMapNaviPath;
    private List<AMapNaviStep> steps;
    private List<AMapNaviLink> links;
    private List<AMapNaviGuide> guides;

    private AMapNaviView mAMapNaviView;
    private AMapNavi mAMapNavi;
    private TTSController mTtsManager;
    //算路终点坐标
    protected NaviLatLng mEndLatlng = null;
    //算路起点坐标
    protected NaviLatLng mStartLatlng = null;
    //存储算路起点的列表
    protected final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    //存储算路终点的列表
    protected final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();

    TextView textViewRemain,getTextViewNext,textViewCurrentRoad,textViewNextRoad;
    ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);

        //获得起点的坐标
        MyApplication myApplication=(MyApplication) getApplication();//也可以用getApplicationContext();
        myApplication.get_Start_Point(); //正式发起导航前，再次定位当前的位置
        Double slatitude = myApplication.slatitude;
        Double slongitude = myApplication.slongitude;
        if((slatitude==null)||(slongitude==null))
            Toast.makeText(this,"无法获取当前位置", Toast.LENGTH_SHORT).show();
        else
        mStartLatlng = new NaviLatLng(slatitude, slongitude);

        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);

        //实例化语音引擎
        mTtsManager = TTSController.getInstance(getApplicationContext());
        mTtsManager.init();

        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);
        mAMapNavi.addAMapNaviListener(mTtsManager);

        //设置模拟导航的行车速度
        mAMapNavi.setEmulatorNaviSpeed(75);

                try{
                //获得终点的坐标
                Bundle bundle = this.getIntent().getExtras();
                Double elatitude = bundle.getDouble("elatitude");
                Double elongitude = bundle.getDouble("elongitude");
                    mEndLatlng = new NaviLatLng(elatitude, elongitude);
                //将起始点坐标加入list
                sList.add(mStartLatlng);
                eList.add(mEndLatlng);
                    iRoute=bundle.getInt("iRoute");  //获取导航方式
                }catch (Exception e){
                e.printStackTrace();
                }

        //设置子布局本身的属性
        LayoutInflater infalter = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutDialog = infalter.inflate(R.layout.activity_navi_dialog, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutDialog.setLayoutParams(lp);
        //设置子布局相对于父布局的属性
        RelativeLayout rl=(RelativeLayout)findViewById(R.id.activity_navi);
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);//与父容器的左侧对齐
        lp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);//与父容器的下侧对齐
        lp2.bottomMargin=120;
        rl.addView(layoutDialog,lp2);

        textViewRemain = (TextView) layoutDialog.findViewById(R.id.textViewRemain);
        getTextViewNext= (TextView) layoutDialog.findViewById(R.id.textViewNext);
        textViewCurrentRoad= (TextView) layoutDialog.findViewById(R.id.textViewCurrentRoad);
        textViewNextRoad= (TextView) layoutDialog.findViewById(R.id.textViewNextRoad);
        imgView=(ImageView)layoutDialog.findViewById(R.id.imageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAMapNaviView.onResume();
        Log.w("xxx","onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAMapNaviView.onPause();
        Log.w("xxx","onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAMapNaviView.onDestroy();
        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();
        mTtsManager.stopSpeaking();//必须先停止speaking，否则下次会没有声音
        mTtsManager.destroy();
        Log.w("xxx","onDestroy");
    }

    //路线规划
    @Override
    public void onInitNaviSuccess() {
        /**
         * 方法:
         *   int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute);
         * 参数:
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         * 说明:
         *      以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         * 注意:
         *      不走高速与高速优先不能同时为true
         *      高速优先与避免收费不能同时为true
         */

        if(iRoute==0) {
            //骑行
            mAMapNavi.calculateRideRoute(mStartLatlng,mEndLatlng);
        }else if(iRoute==1) {
            //驾车
            int strategy = 0;
            try {
                strategy = mAMapNavi.strategyConvert(true, false, false, false, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mAMapNavi.setCarNumber("京", "DFZ588"); //设置车牌号，用于限行算路,同时会改变当前位置设定到路上，发出语音（不在路边没声音）
            //驾车路径计算
            mAMapNavi.calculateDriveRoute(sList, eList, null, strategy);
        }else if(iRoute==2) {
            //步行
            mAMapNavi.calculateWalkRoute(mStartLatlng,mEndLatlng);
        }
    }

    //开始导航
    @Override
    public void onCalculateRouteSuccess() {
        //如果什么都不加，会显示整体的路线
        //开始模拟导航
        mAMapNavi.startNavi(NaviType.EMULATOR);
        //开始实时导航
        //mAMapNavi.startNavi(NaviType.GPS);

//
        //显示全程的概况
        guides = mAMapNavi.getNaviGuideList();
        //详情
        mAMapNaviPath = mAMapNavi.getNaviPath();
        steps = mAMapNaviPath.getSteps();
        Toast.makeText(this, "共有："+steps.size()+" steps", Toast.LENGTH_SHORT).show();
/*
        for (int i = 0; i < steps.size() - 1; i++) {
            //guide step相生相惜，指的是大导航段
            AMapNaviGuide guide = guides.get(i);  //有问题，会闪退

            Log.w("wlx", "AMapNaviGuide 路线经纬度:" + guide.getCoord() + "");
            Log.w("wlx", "AMapNaviGuide 路线名:" + guide.getName() + "");
            Log.w("wlx", "AMapNaviGuide 路线长:" + guide.getLength() + "m");
            Log.w("wlx", "AMapNaviGuide 路线耗时:" + guide.getTime() + "s");
            Log.w("wlx", "AMapNaviGuide 路线IconType" + guide.getIconType() + "");
            AMapNaviStep step = steps.get(i);
            Log.w("wlx", "AMapNaviStep 距离:" + step.getLength() + "m" + " " + "耗时:" + step.getTime() + "s");
            Log.w("wlx", "AMapNaviStep 红绿灯个数:" + step.getTrafficLightNumber());

            //link指的是大导航段中的小导航段
            links = step.getLinks();
            for (AMapNaviLink link : links) {
                //请看com.amap.api.navi.enums.RoadClass，以及帮助文档
                Log.w("wlx", "AMapNaviLink 道路名:" + link.getRoadName() + " " + "道路等级:" + link.getRoadClass());
                //请看com.amap.api.navi.enums.RoadType，以及帮助文档
                Log.w("wlx", "AMapNaviLink 道路类型:" + link.getRoadType());

            }
        }
        */
    }

    @Override
    public void onInitNaviFailure() {
        Toast.makeText(this, "init navi Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartNavi(int type) {
        //开始导航回调
    }

    @Override
    public void onTrafficStatusUpdate() {
        //
    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {
        //当前位置回调
    }

    @Override
    public void onGetNavigationText(int type, String text) {
        //播报类型和播报文字回调
    }

    @Override
    public void onEndEmulatorNavi() {
        //结束模拟导航
    }

    @Override
    public void onArriveDestination() {
        //到达目的地
    }

    @Override
    public void onCalculateRouteFailure(int errorInfo) {
        //路线计算失败
        Log.e("dm", "--------------------------------------------");
        //Log.i("dm", "路线计算失败：错误码=" + errorInfo + ",Error Message= " + ErrorInfo.getError(errorInfo));
        Log.i("dm", "错误码详细链接见：http://lbs.amap.com/api/android-navi-sdk/guide/tools/errorcode/");
        Log.e("dm", "--------------------------------------------");
        //Toast.makeText(this, "errorInfo：" + errorInfo + ",Message：" + ErrorInfo.getError(errorInfo), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReCalculateRouteForYaw() {
        //偏航后重新计算路线回调
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        //拥堵后重新计算路线回调
    }

    @Override
    public void onArrivedWayPoint(int wayID) {
        //到达途径点
    }

    @Override
    public void onGpsOpenStatus(boolean enabled) {
        //GPS开关状态回调
    }

    @Override
    public void onNaviSetting() {
        //底部导航设置点击回调
    }

    @Override
    public void onNaviMapMode(int isLock) {
        //地图的模式，锁屏或锁车
    }

    //点击导航界面左下角的停止导航后的动作
    @Override
    public void onNaviCancel() {
        mTtsManager.stopSpeaking();
        finish();
    }


    @Override
    public void onNaviTurnClick() {
        //转弯view的点击回调
    }

    @Override
    public void onNextRoadClick() {
        //下一个道路View点击回调
    }


    @Override
    public void onScanViewButtonClick() {
        //全览按钮点击回调
    }

    @Deprecated
    @Override
    public void onNaviInfoUpdated(AMapNaviInfo naviInfo) {
        //过时
    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapCameraInfos) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] amapServiceAreaInfos) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviinfo) {
        //导航过程中的信息更新，请看NaviInfo的具体说明
        Log.w("xxx","onNaviInfoUpdate()........... ");
        int currentStep = naviinfo.getCurStep();
        int currentLink = naviinfo.getCurLink();
        Log.w("xxx", "当前Step index:" + currentStep + "当前Link index:" + currentLink);
        Log.w("xxx","next Icontype: "+naviinfo.getIconType());
        Log.w("xxx","next distance: "+naviinfo.getCurStepRetainDistance()+""+"m");
        Log.w("xxx","remain distance: "+naviinfo.getPathRetainDistance()+""+"m");
        Log.w("xxx","remain time: "+naviinfo.getPathRetainTime()+""+"s");
        textViewRemain.setText("剩余："+naviinfo.getPathRetainDistance()/1000+""+"km "+
                naviinfo.getPathRetainTime()/60+""+"分钟 "+ naviinfo.getCurrentSpeed()+"m/s");
        getTextViewNext.setText(naviinfo.getCurStepRetainDistance()+""+"m后 "+naviinfo.getIconType());
        textViewCurrentRoad.setText("当前："+naviinfo.getCurrentRoadName());
        textViewNextRoad.setText("进入："+naviinfo.getNextRoadName());
        String iconName="navicon"+naviinfo.getIconType();
        imgView.setImageDrawable(getResources().getDrawable(getNavid(iconName)));
        //naviinfo.setIconType(naviinfo.getIconType());
        //getCurrentSpeed()
    }

    //获取导航转向的的图标R.drawable.id
    public int getNavid(String name) {
        Resources res = getResources();
        return res.getIdentifier(name, "drawable", getPackageName());
    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {
        //已过时
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
        //已过时
    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        //显示转弯回调
    }

    @Override
    public void hideCross() {
        //隐藏转弯回调
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {
        //显示车道信息

    }

    @Override
    public void hideLaneInfo() {
        //隐藏车道信息
    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        //多路径算路成功回调
    }

    @Override
    public void notifyParallelRoad(int i) {
        if (i == 0) {
            Toast.makeText(this, "当前在主辅路过渡", Toast.LENGTH_SHORT).show();
            Log.d("wlx", "当前在主辅路过渡");
            return;
        }
        if (i == 1) {
            Toast.makeText(this, "当前在主路", Toast.LENGTH_SHORT).show();

            Log.d("wlx", "当前在主路");
            return;
        }
        if (i == 2) {
            Toast.makeText(this, "当前在辅路", Toast.LENGTH_SHORT).show();

            Log.d("wlx", "当前在辅路");
        }
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        //更新交通设施信息
    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
        //更新巡航模式的统计信息
    }


    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
        //更新巡航模式的拥堵信息
    }

    @Override
    public void onPlayRing(int i) {

    }


    @Override
    public void onLockMap(boolean isLock) {
        //锁地图状态发生变化时回调
    }

    @Override
    public void onNaviViewLoaded() {
        Log.d("wlx", "导航页面加载成功");
        Log.d("wlx", "请不要使用AMapNaviView.getMap().setOnMapLoadedListener();会overwrite导航SDK内部画线逻辑");
    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }
}
