package com.apress.gerber.gps_clock;

import android.app.Service;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import android.media.MediaPlayer;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MyLocationStyle.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationSource,AMapLocationListener {
    MapView mMapView = null;
    AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mapLocationClient;
    private AMapLocationClientOption mapLocationOption;
    Button b1;
    Button b2;
    double amapLat = 0 ,amapLon = 0;
    double goalLat = 0,goalLon = 0;
    LatLng amapLatLng ,goalLatLng = null;
    boolean isFirstset = false;
    boolean viFirst = false;
    protected MediaPlayer mp=new MediaPlayer();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = (Button)findViewById(R.id.button);
        b2 = (Button)findViewById(R.id.button2);
        b1.setOnClickListener(new b1ClickListener());
        b2.setOnClickListener(new b2ClickListener());

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写

        mp=MediaPlayer.create(this,R.raw.hudiequanbian);

        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        aMap.setLocationSource(this);
        aMap.setMyLocationEnabled(true);
        aMap.setMyLocationType(AMap.MAP_TYPE_NORMAL);
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.setTrafficEnabled(true);//开启交通图
        //aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);//跟随模式，随时定位
        //aMap.setMyLocationType(AMap.MAP_TYPE_SATELLITE);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.getUiSettings().setCompassEnabled(true);//指南针
        aMap.getUiSettings().setScaleControlsEnabled(true);//比例尺

    }



    class b1ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    double latitude = latLng.latitude;
                    double longitude = latLng.longitude;
                    goalLat = latitude;
                    goalLon = longitude;

                    LatLng point = new LatLng(latitude,longitude);
                    aMap.addMarker(new MarkerOptions().position(point));
                   // LatLng point2 = new LatLng(amapLat,amapLon);
                    goalLatLng = point;
                   // amapLatLng = point2;
                    isFirstset = true;
                    viFirst = true;

                }
            });
        }
    }
    class b2ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            aMap.clear();
            if(mp!=null){
                mp.stop();
            }
            viFirst = false;
            Toast.makeText(getApplicationContext(),"取消成功",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void activate(OnLocationChangedListener listener){
        mListener = listener;
        if(mapLocationClient == null){
            //初始化定位
            mapLocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mapLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mapLocationClient.setLocationListener(this);//处理位置改变
            //设置是否只定位一次
            mapLocationOption.setOnceLocation(false);
            mapLocationOption.setOnceLocationLatest(false);
            //设置为高精度模式
            mapLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置是否返回地址信息（默认返回地址信息）
            mapLocationOption.setNeedAddress(true);
            //设置定位时间间隔 毫秒
            mapLocationOption.setInterval(1000);
            //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            mapLocationOption.setHttpTimeOut(20000);
            //设置定位参数
            mapLocationClient.setLocationOption(mapLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（默认间隔支持为2000ms，最低1000ms）（已设置），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除

            mapLocationClient.startLocation();//启动定位


        }
    }

    @Override
    public void deactivate(){
        mListener = null;
        if(mapLocationClient != null){
            mapLocationClient.stopLocation();
            mapLocationClient.onDestroy();
        }
        mapLocationClient = null;
    }
    @Override
    public void onLocationChanged(AMapLocation aMapLocation){
        if((mListener != null)&&(aMapLocation != null)){
            mListener.onLocationChanged(aMapLocation);//显示系统蓝点
            if(aMapLocation.getErrorCode() == 0){
                aMapLocation.getLocationType();
                aMapLocation.getLatitude();//获取纬度
                amapLat = aMapLocation.getLatitude();
                aMapLocation.getLongitude();//获取经度
                amapLon = aMapLocation.getLongitude();
                aMapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm;ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间

                LatLng point2 = new LatLng(amapLat,amapLon);
                //goalLatLng = point;
                amapLatLng = point2;
                float distance = AMapUtils.calculateLineDistance(amapLatLng,goalLatLng);
                if(isFirstset == true) {
                    Toast.makeText(getApplicationContext(), "当前距离" + distance + "\n设置成功", Toast.LENGTH_LONG).show();
                    isFirstset = false;
                }
                if(distance <=500&&viFirst==true){
                    Vibrator vb = (Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
                    vb.vibrate(5000);
                    mp.start();
                }

             /*   //显示位置信息
                StringBuffer buffer = new StringBuffer();
                buffer.append(aMapLocation.getCountry()+""+aMapLocation.getProvince()+""+aMapLocation.getCity()+
                        ""+aMapLocation.getDistrict()+""+aMapLocation.getStreet()+""+aMapLocation.getStreetNum());
                Toast.makeText(getApplicationContext(),buffer.toString(), Toast.LENGTH_LONG).show();
             */

            }else{
                String errText = "定位失败，" + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if(null != mapLocationClient){
            mapLocationClient.onDestroy();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

}
