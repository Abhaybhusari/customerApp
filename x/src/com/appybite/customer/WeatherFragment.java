package com.appybite.customer;

import java.text.DateFormat;
import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appybite.customer.weather.Conversor;
import com.appybite.customer.weather.Weather;
import com.yj.commonlib.screen.LayoutLib;
import com.yj.commonlib.screen.PRJFUNC;

public class WeatherFragment extends Fragment {

	private Weather weather;
	
	private ImageView ivWeatherImage, ivWeatherIcon;
	private TextView tvCity, tvCountry, tvDate, tvTemp, tvWind, tvLastUpdate;
	private Button btUpdate;
	
	public WeatherFragment()
	{
	}

	public void setWeather(Weather weather) {
		
		this.weather = weather;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frag_weather, container, false);

		updateLCD(v);

		// - update position
		if (!PRJFUNC.DEFAULT_SCREEN) {
			scaleView(v);
		}
		
		loadWeather();
	
		return v;
	}
	
	private void updateLCD(View v) {
		
		if (PRJFUNC.mGrp == null) {
			PRJFUNC.resetGraphValue(getActivity());
		}

		ivWeatherImage = (ImageView)v.findViewById(R.id.ivWeatherImage); 
		ivWeatherIcon = (ImageView)v.findViewById(R.id.ivWeatherIcon);
		tvCity = (TextView)v.findViewById(R.id.tvCity);
		tvCountry = (TextView)v.findViewById(R.id.tvCountry);
		tvDate = (TextView)v.findViewById(R.id.tvDate);
		tvTemp = (TextView)v.findViewById(R.id.tvTemp);
		tvWind = (TextView)v.findViewById(R.id.tvWind);
		tvLastUpdate = (TextView)v.findViewById(R.id.tvLastUpdate);
		btUpdate = (Button)v.findViewById(R.id.btUpdate);
		btUpdate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				((MainActivity)getActivity()).loadWeather();
			}
		});
	}
	
	private void scaleView(View v) {

		if (PRJFUNC.mGrp == null) {
			return;
		}
	
		PRJFUNC.mGrp.relayoutView(ivWeatherIcon, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.relayoutView(tvCity, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvCity);
		PRJFUNC.mGrp.relayoutView(tvCountry, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvCountry);
		PRJFUNC.mGrp.relayoutView(tvDate, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvDate);
		PRJFUNC.mGrp.relayoutView(tvTemp, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvTemp);
		PRJFUNC.mGrp.relayoutView(tvWind, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvWind);
		PRJFUNC.mGrp.relayoutView(tvLastUpdate, LayoutLib.LP_RelativeLayout);
		PRJFUNC.mGrp.setTextViewFontScale(tvLastUpdate);
		PRJFUNC.mGrp.setButtonFontScale(btUpdate);
	}

	public void loadWeather() {
		
			if (weather != null && weather.getData_receiving() != 0) {
				long weatherId = weather.getWeather_id();
				tvCity.setText(weather.getCity_name());
				tvCountry.setText(weather.getWeather_main());
				tvTemp.setText((int) Conversor.roundNoDecimals(Conversor.kelvinToCelsius(weather.getTemp()))+ " ℃");
				DateFormat df = DateFormat.getDateInstance();
				tvDate.setText(df.format(Calendar.getInstance().getTime()));
				tvWind.setText(weather.getWeather_main() + "(" + String.valueOf((int)weather.getHumidity()) + "%" + " / " + weather.getWind_speed() + "ms)");
				tvLastUpdate.setText("Last update: " + Conversor.dateToString(weather.getData_receiving()));
				
				if (weatherId >= 200 && weatherId <= 232) {
					ivWeatherImage.setBackgroundResource(R.drawable.weather_img_thunderstorm);
					ivWeatherIcon.setBackgroundResource(R.drawable.weather_icon_thunderstorm_day);
					tvTemp.setTextColor(getResources().getColor(R.color.White));
					tvCity.setTextColor(getResources().getColor(R.color.White));
					tvCountry.setTextColor(getResources().getColor(R.color.White));
					tvWind.setTextColor(getResources().getColor(R.color.White));
					tvLastUpdate.setTextColor(getResources().getColor(R.color.White));
				}
				if (weatherId >= 300 && weatherId <= 321) {
					ivWeatherImage.setBackgroundResource(R.drawable.weather_img_rain);
					ivWeatherIcon.setBackgroundResource(R.drawable.weather_icon_rain_day);
					tvTemp.setTextColor(getResources().getColor(R.color.White));
					tvCity.setTextColor(getResources().getColor(R.color.White));
					tvCountry.setTextColor(getResources().getColor(R.color.White));
					tvWind.setTextColor(getResources().getColor(R.color.White));
					tvLastUpdate.setTextColor(getResources().getColor(R.color.White));
				}
				if (weatherId >= 500 && weatherId <= 522) {
					ivWeatherImage.setBackgroundResource(R.drawable.weather_img_rain);
					ivWeatherIcon.setBackgroundResource(R.drawable.weather_icon_rain_day);
					tvTemp.setTextColor(getResources().getColor(R.color.White));
					tvCity.setTextColor(getResources().getColor(R.color.White));
					tvCountry.setTextColor(getResources().getColor(R.color.White));
					tvWind.setTextColor(getResources().getColor(R.color.White));
					tvLastUpdate.setTextColor(getResources().getColor(R.color.White));
				}
				if (weatherId >= 600 && weatherId <= 621) {
					ivWeatherImage.setBackgroundResource(R.drawable.weather_img_snow);
					ivWeatherIcon.setBackgroundResource(R.drawable.weather_icon_snow_day);
					tvTemp.setTextColor(getResources().getColor(R.color.Aqua));
					tvCity.setTextColor(getResources().getColor(R.color.Aqua));
					tvCountry.setTextColor(getResources().getColor(R.color.Aqua));
					tvWind.setTextColor(getResources().getColor(R.color.Aqua));
					tvLastUpdate.setTextColor(getResources().getColor(R.color.Aqua));
				}
				if (weatherId >= 700 && weatherId <= 741) {
					ivWeatherImage.setBackgroundResource(R.drawable.weather_img_mist);
					ivWeatherIcon.setBackgroundResource(R.drawable.weather_icon_mist_day);
					tvTemp.setTextColor(getResources().getColor(R.color.White));
					tvCity.setTextColor(getResources().getColor(R.color.White));
					tvCountry.setTextColor(getResources().getColor(R.color.White));
					tvWind.setTextColor(getResources().getColor(R.color.White));
					tvLastUpdate.setTextColor(getResources().getColor(R.color.White));
				}
				if (weatherId == 800) {
					ivWeatherImage.setBackgroundResource(R.drawable.weather_img_clear);
					ivWeatherIcon.setBackgroundResource(R.drawable.weather_icon_clear_day);
					tvTemp.setTextColor(getResources().getColor(R.color.Black));
					tvCity.setTextColor(getResources().getColor(R.color.Black));
					tvCountry.setTextColor(getResources().getColor(R.color.Black));
					tvWind.setTextColor(getResources().getColor(R.color.Black));
					tvLastUpdate.setTextColor(getResources().getColor(R.color.Black));
				}
				if (weatherId == 801) {
					ivWeatherImage.setBackgroundResource(R.drawable.weather_img_scattered_clouds);
					ivWeatherIcon.setBackgroundResource(R.drawable.weather_icon_scattered_clouds_day);
					tvTemp.setTextColor(getResources().getColor(R.color.Black));
					tvCity.setTextColor(getResources().getColor(R.color.Black));
					tvCountry.setTextColor(getResources().getColor(R.color.Black));
					tvWind.setTextColor(getResources().getColor(R.color.Black));
					tvLastUpdate.setTextColor(getResources().getColor(R.color.Black));
				}
				if (weatherId == 802) {
					ivWeatherImage.setBackgroundResource(R.drawable.weather_img_scattered_clouds);
					ivWeatherIcon.setBackgroundResource(R.drawable.weather_icon_scattered_clouds_day);
					tvTemp.setTextColor(getResources().getColor(R.color.Black));
					tvCity.setTextColor(getResources().getColor(R.color.Black));
					tvCountry.setTextColor(getResources().getColor(R.color.Black));
					tvWind.setTextColor(getResources().getColor(R.color.Black));
					tvLastUpdate.setTextColor(getResources().getColor(R.color.Black));
				}
				if (weatherId == 803) {
					ivWeatherImage.setBackgroundResource(R.drawable.weather_img_broken_clouds);
					ivWeatherIcon.setBackgroundResource(R.drawable.weather_icon_broken_clouds_day);
					tvTemp.setTextColor(getResources().getColor(R.color.Gray));
					tvCity.setTextColor(getResources().getColor(R.color.Gray));
					tvCountry.setTextColor(getResources().getColor(R.color.Gray));
					tvWind.setTextColor(getResources().getColor(R.color.Gray));
					tvLastUpdate.setTextColor(getResources().getColor(R.color.Gray));
				}
				if (weatherId == 804) {
					ivWeatherImage.setBackgroundResource(R.drawable.weather_img_broken_clouds);
					ivWeatherIcon.setBackgroundResource(R.drawable.weather_icon_broken_clouds_day);
					tvTemp.setTextColor(getResources().getColor(R.color.Gray));
					tvCity.setTextColor(getResources().getColor(R.color.Gray));
					tvCountry.setTextColor(getResources().getColor(R.color.Gray));
					tvWind.setTextColor(getResources().getColor(R.color.Gray));
					tvLastUpdate.setTextColor(getResources().getColor(R.color.Gray));
				}
			} else {
				tvCity.setText("Unknown");
				tvCountry.setText("");
				tvDate.setText("");
				tvTemp.setText("");
				tvWind.setText("");
				tvLastUpdate.setText("Update Failed. Try again later.");
				ivWeatherImage.setBackgroundColor(getResources().getColor(R.color.Transparent));
				ivWeatherIcon.setBackgroundColor(getResources().getColor(R.color.Transparent));
			}
	}
}
