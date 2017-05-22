package com.example.guchen.revdo2linux.AmapNavi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.guchen.revdo2linux.AmapNavi.overlay.PoiOverlay;
import com.example.guchen.revdo2linux.AmapNavi.util.AMapUtil;
import com.example.guchen.revdo2linux.AmapNavi.util.ToastUtil;
import com.example.guchen.revdo2linux.MyApplication;
import com.example.guchen.revdo2linux.R;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements GeocodeSearch.OnGeocodeSearchListener,
        PoiSearch.OnPoiSearchListener ,TextWatcher,Inputtips.InputtipsListener {
    // 定义北京市经纬度坐标（此处以北京坐标为例）
    //private LatLng centerBJPoint= new LatLng(39.904989,116.405285);
    // 定义上海市经纬度坐标
    private AMap aMap;
    private LatLng centerBJPoint= null;
    private Button button;
    private AutoCompleteTextView editText;
    private TextView textViewLocal;
    private ImageView imageView;
    private ProgressDialog progDialog = null;// 搜索时进度条
    public static Handler handlerLocal;
    GeocodeSearch geocodeSearch;
    GeocodeAddress address=null;
    private PoiSearch.Query query;// Poi查询条件类
    private String keyWord = "宣颐家园";// 要输入的poi搜索关键字
    private PoiSearch poiSearch;// POI搜索
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiResult poiResult; // poi返回的结果
    private MyApplication myApplication;
    private SharedPreferences mSharedPreferences=null;
    SharedPreferences.Editor edit=null;
    int num;
    private ListView listSearch;
    private TextView textViewClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //记录搜索历史记录
        mSharedPreferences = getSharedPreferences("test", MODE_PRIVATE);//仅本程序可访问，生成test.xml
        edit = mSharedPreferences.edit();
        num=mSharedPreferences.getInt("num",0);

        myApplication=(MyApplication) getApplication();//也可以用getApplicationContext();
        myApplication.get_Start_Point();

        MapView mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();
        //通过指定坐标，显示指定的城市
        //aMap.moveCamera(CameraUpdateFactory.changeLatLng(centerBJPoint));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        //显示定位蓝点,会自动显示到当前的城市（不管上面的坐标设到哪里）
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        //myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        //myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW); //只定位一次
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        //绘制marker
        Marker marker = aMap.addMarker(new MarkerOptions()
                .position(new LatLng(39.986919,116.353369))
                //.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.marker)))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("学院桥")
                .draggable(true));

        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);

        textViewClear=(TextView)findViewById(R.id.textViewClear);
        listSearch = (ListView) findViewById(R.id.listSearch);
        editText=(AutoCompleteTextView)findViewById(R.id.editText);
        editText.addTextChangedListener(this);// 添加文本输入框监听事件
        //第一次进入程序时隐藏输入法
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("xxx", "editText onClick()");
                dis_search_history();
            }
        });

        textViewClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear_search_history();
                listSearch.setVisibility(View.GONE);
                textViewClear.setVisibility(View.GONE);
            }
        });

        button=(Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_search_history(); //将搜索记录加入历史表中
                GeocodeQuery query = new GeocodeQuery(editText.getText().toString(), "010");
                geocodeSearch.getFromLocationNameAsyn(query);
            }
        });

        textViewLocal=(TextView)findViewById(R.id.textViewLocal);
        handlerLocal= new Handler(){
            //或使用dispatchleMessage(),因为dispatchMessage()会调用handleMessage()
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 222:// 服务器消息
                        textViewLocal.setText((String)msg.obj);
                        break;
                    case 333:// 扫描完毕消息
                    default: break;
                }}
        };

        imageView=(ImageView)findViewById(R.id.imageLocation);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获得起点的坐标
                MyApplication myApplication=(MyApplication) getApplication();//也可以用getApplicationContext();
                myApplication.get_Start_Point(); //正式发起导航前，再次定位当前的位置
                Double slatitude = myApplication.slatitude;
                Double slongitude = myApplication.slongitude;
                if((slatitude==null)||(slongitude==null))
                    Toast.makeText(MapActivity.this,"无法获取当前位置,请重试",Toast.LENGTH_SHORT).show();
                else {
                    centerBJPoint = new LatLng(slatitude, slongitude);
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(centerBJPoint));
                }
            }
        });


        //doSearchQuery(); //显示keyword指定的所有poi相关点
    }

    void dis_search_history(){
        if (TextUtils.isEmpty(editText.getText()))  //输入框为空时触发历史记录显示
        {
            num=mSharedPreferences.getInt("num",0);
            Log.w("xxx","num="+num);
            if(num>0) {  //有数据显示
                textViewClear.setVisibility(View.VISIBLE);
                listSearch.setAdapter(new ArrayAdapter<String>(MapActivity.this,
                        android.R.layout.simple_list_item_1, query_search_history()));
                listSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    //setTitle("你点击了第" + arg2 + "行");
                    editText.setText(mSharedPreferences.getString("data"+(num-arg2), "null"));
                }
            });
                listSearch.setVisibility(View.VISIBLE);
            }
        }else {
            listSearch.setVisibility(View.GONE);
            textViewClear.setVisibility(View.GONE);
        }
    }

    void add_search_history(){
        if (!TextUtils.isEmpty(editText.getText()))  //输入框不为空时加入历史记录
        {
            num=mSharedPreferences.getInt("num",0);
            edit.putInt("num", num+1);
            edit.putString("data"+(num+1), editText.getText().toString());
            edit.commit(); //更新数据
        }
    }

    void clear_search_history(){
        edit.clear();
        edit.putInt("num", 0);
        edit.commit();
    }

    List<String> query_search_history(){
        List<String> listString = new ArrayList<String>();
        num=mSharedPreferences.getInt("num",0);
        if((0<num)&&(num<11)) {
            for(int i=0;i<num;i++){
                listString.add(mSharedPreferences.getString("data"+(num-i), "null"));
                Log.w("xxx",mSharedPreferences.getString("data"+(num-i), "null"));
            }
        }else if(num>10){
            for(int i=0;i<10;i++) {
                listString.add(mSharedPreferences.getString("data" + (num - i), "null"));
                Log.w("xxx",mSharedPreferences.getString("data"+(num-i), "null"));
            }
        }
        return listString;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim(); //s代表输入框中输入的字符
        if (!AMapUtil.IsEmptyOrNullString(newText)) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, myApplication.city); //InputtipsQuery是AMAP的API
            Inputtips inputTips = new Inputtips(this, inputquery);
            inputTips.setInputtipsListener(this);//触发onGetInputtips回调函数，从下的异步查询结果中得到查询数据
            inputTips.requestInputtipsAsyn();    //发起异步查询，触发onPoiSearched回调函数
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        dis_search_history();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" + keyWord);
        progDialog.show();
    }

    // 开始进行poi搜索
    protected void doSearchQuery() {
        showProgressDialog();// 显示进度框
        currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", "北京市");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this); //会触发onPoiSearched回调函数
        poiSearch.searchPOIAsyn(); //异步搜索
    }


    /**

     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        // 隐藏搜索进度对话框
        if (progDialog != null) {
            progDialog.dismiss();
        }
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        aMap.clear();// 清理之前的图标
                        //使用PoiOverlay图层函数，绘制Maker标志
                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);  //找不到时返回一些推荐城市信息
                    } else {
                        ToastUtil.show(this, R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }

    }


    @Override
    public void onPoiItemSearched(PoiItem item, int rCode) {
        // TODO Auto-generated method stub
    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(this, infomation);

    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        //解析result获取坐标信息
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                address = result.getGeocodeAddressList().get(0);
                /*
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        AMapUtil.convertToLatLng(address.getLatLonPoint()), 15));
                geoMarker.setPosition(AMapUtil.convertToLatLng(address
                        .getLatLonPoint()));
                        */
                String addressName = "经纬度值:" + address.getLatLonPoint() + "\n位置描述:"
                        + address.getFormatAddress();
                Log.w("xxx",addressName);

                try{
                    Intent intent=new Intent(MapActivity.this,RoutePlanActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putDouble("elatitude",address.getLatLonPoint().getLatitude());
                    bundle.putDouble("elongitude", address.getLatLonPoint().getLongitude());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                ToastUtil.show(MapActivity.this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

    /**
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {

    }

    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());
            }
            ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                    getApplicationContext(),
                    R.layout.route_inputs, listString);
            editText.setAdapter(aAdapter);  //设置autocomplete匹配字段
            aAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

}
