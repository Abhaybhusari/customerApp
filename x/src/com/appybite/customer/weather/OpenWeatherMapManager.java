package com.appybite.customer.weather;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class OpenWeatherMapManager {

	private static final String LogDavid = "OpenWeather";

	private static String list = "list";

	private static String coordinates = "coord";
	private static String system = "sys";
	private static String weather = "weather";
	private static String main = "main";
	private static String wind = "wind";
	private static String rain = "rain";
	private static String snow = "snow";
	private static String clouds = "clouds";

	private static String latitude = "lat";
	private static String longitude = "lon";

	private static String country = "country";
	private static String sunrise = "sunrise";
	private static String sunset = "sunset";

	private static String weather_id = "id";
	private static String weather_main = "main";
	private static String weather_description = "description";
	private static String weather_icon = "icon";

	private static String temp = "temp";
	private static String temp_max = "temp_max";
	private static String temp_min = "temp_min";
	private static String pressure = "pressure";
	private static String sea_level = "sea_level";
	private static String grnd_level = "grnd_level";
	private static String humidity = "humidity";

	private static String wind_speed = "speed";
	private static String wind_degrees = "deg";

	private static String rain_threehours = "3h";

	private static String snow_threehours = "3h";

	private static String clouds_all = "all";

	private static String data_receiving = "dt";
	private static String city_id = "id";
	private static String city_name = "name";
	private static String city_country = "country";
	private static String city = "city";
	private static String code = "cod";

	public static Weather[] jsonToForecast(JSONObject datos_object) {

		try {
			int cnt;
			String cityCode, cityId, cityName, cityCountry;
			double latitude, longitude;

			cnt = datos_object.getInt("cnt");
			cityCode = datos_object.getString(code);

			if (datos_object.has(OpenWeatherMapManager.city)) {
				JSONObject cityOb = datos_object
						.getJSONObject(OpenWeatherMapManager.city);
				cityId = cityOb.getString(city_id);
				cityName = cityOb.getString(city_name);
				cityCountry = cityOb.getString(city_country);

				if (cityOb.has(OpenWeatherMapManager.coordinates)) {
					JSONObject coordOb = cityOb.getJSONObject(coordinates);
					latitude = coordOb
							.getDouble(OpenWeatherMapManager.latitude);
					longitude = coordOb
							.getDouble(OpenWeatherMapManager.longitude);
				} else {
					return null;
				}
				if (datos_object.has(OpenWeatherMapManager.list)) {
					Weather[] forecast = new Weather[cnt];
					JSONArray listAr = datos_object
							.getJSONArray(OpenWeatherMapManager.list);
					JSONObject listOb;
					Weather weather;
					for (int n = 0; n < cnt; n++) {
						listOb = listAr.getJSONObject(n);

						weather = new Weather();
						
						//COMMON
						weather.setCity_id(cityId);
						weather.setLatitude(latitude);
						weather.setLongitude(longitude);
						weather.setCountry(cityCountry);
						weather.setCode(cityCode);
						weather.setCity_name(cityName);

						
						weather.setData_receiving(listOb
								.getLong(data_receiving));
						
						//CLOUDS						
						JSONObject cloudsOb = listOb.getJSONObject(clouds);
						weather.setClouds_all(cloudsOb.getDouble(clouds_all));
						
						
						//WEATHER
						if (listOb.has(OpenWeatherMapManager.weather)) {
							JSONArray weatherAr = listOb
									.getJSONArray(OpenWeatherMapManager.weather);
							JSONObject weatherOb = weatherAr.getJSONObject(0);
							weather.setWeather_id(weatherOb.getInt(weather_id));
							weather.setWeather_main(weatherOb.getString(weather_main));
							weather.setWeather_description(weatherOb
									.getString(weather_description));
							weather.setWeather_icon(weatherOb.getString(weather_icon));
						} else {
							Log.d(LogDavid, "jsonToForecast: NoWeather");
						}
						
						// MAIN
						JSONObject mainOb = listOb.getJSONObject(main);
						weather.setTemp(mainOb.getDouble(temp));
						weather.setPreassure(mainOb.getDouble(pressure));
						weather.setHumidity(mainOb.getDouble(humidity));

						if (mainOb.has(temp_max))
							weather.setTemp_max(mainOb.getDouble(temp_max));
						else
							weather.setTemp_max(0);
						if (mainOb.has(temp_min))
							weather.setTemp_min(mainOb.getDouble(temp_min));
						else
							weather.setTemp_min(0);
						if (mainOb.has(sea_level))
							weather.setSea_level(mainOb.getDouble(sea_level));
						else
							weather.setSea_level(0);
						if (mainOb.has(grnd_level))
							weather.setGrnd_level(mainOb.getDouble(grnd_level));
						else
							weather.setGrnd_level(0);

						//RAIN
						if (listOb.has(rain)) {
							JSONObject rainOb = listOb.getJSONObject(rain);
							if(rainOb.has(rain_threehours)) {
								weather.setRain_threehours(rainOb.getDouble(rain_threehours));
								Log.d(LogDavid, "RAIN: " + rainOb.getDouble(rain_threehours));
							}
						} else {
							Log.d(LogDavid, "jsonToWeather: NoRain");
						}
						
						//SNOW
						if (listOb.has(snow)) {
							JSONObject rainOb = listOb.getJSONObject(snow);
							if(rainOb.has(snow_threehours))
								weather.setRain_threehours(rainOb.getDouble(snow_threehours));
						} else {
							Log.d(LogDavid, "jsonToWeather: NoSnow");
						}
						
						//WIND
						JSONObject windOb = listOb.getJSONObject(wind);
						weather.setWind_speed(windOb.getDouble(wind_speed));
						weather.setWind_degrees(windOb.getDouble(wind_degrees));
						
						forecast[n] = weather;
					}
					return forecast;
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			Log.e(LogDavid, "jsonToForecast: " + e.toString());
			return null;
		}
	}

	public static Weather jsonToWeather2(JSONObject datos_object) {
		try {
			Weather weather = new Weather();

			weather.setCode(datos_object.getString(code));

			if (datos_object.has("list")) {
				JSONArray listAr = datos_object.getJSONArray("list");
				JSONObject listOb = listAr.getJSONObject(0);

				weather.setCity_name(listOb.getString(city_name));

				JSONObject coordOb = listOb.getJSONObject(coordinates);
				weather.setLatitude(coordOb.getDouble(latitude));
				weather.setLongitude(coordOb.getDouble(longitude));

				JSONObject mainOb = listOb.getJSONObject(main);
				weather.setTemp(mainOb.getDouble(temp));
				weather.setPreassure(mainOb.getDouble(pressure));
				weather.setHumidity(mainOb.getDouble(humidity));

				if (mainOb.has(temp_max))
					weather.setTemp_max(mainOb.getDouble(temp_max));
				else
					weather.setTemp_max(0);
				if (mainOb.has(temp_min))
					weather.setTemp_min(mainOb.getDouble(temp_min));
				else
					weather.setTemp_min(0);
				if (mainOb.has(sea_level))
					weather.setSea_level(mainOb.getDouble(sea_level));
				else
					weather.setSea_level(0);
				if (mainOb.has(grnd_level))
					weather.setGrnd_level(mainOb.getDouble(grnd_level));
				else
					weather.setGrnd_level(0);

				weather.setData_receiving(listOb.getLong(data_receiving));

				JSONObject windOb = listOb.getJSONObject(wind);
				weather.setWind_speed(windOb.getDouble(wind_speed));
				weather.setWind_degrees(windOb.getDouble(wind_degrees));

				JSONObject cloudsOb = listOb.getJSONObject(clouds);
				weather.setClouds_all(cloudsOb.getDouble(clouds_all));

				if (listOb.has(OpenWeatherMapManager.weather)) {
					JSONArray weatherAr = listOb
							.getJSONArray(OpenWeatherMapManager.weather);
					JSONObject weatherOb = weatherAr.getJSONObject(0);
					weather.setWeather_id(weatherOb.getInt(weather_id));
					weather.setWeather_main(weatherOb.getString(weather_main));
					weather.setWeather_description(weatherOb
							.getString(weather_description));
					weather.setWeather_icon(weatherOb.getString(weather_icon));
				} else {
					Log.d(LogDavid, "jsonToWeather2: NoWeather");
				}
			}
			return weather;
		} catch (Exception e) {
			Log.e(LogDavid, "jsonToWeather2: " + e.toString());
			return null;
		}
	}

	public static Weather jsonToWeather(JSONObject datos_object) {
		try {
			Weather weather = new Weather();

			JSONObject coordOb = datos_object.getJSONObject(coordinates);
			weather.setLatitude(coordOb.getDouble(latitude));
			weather.setLongitude(coordOb.getDouble(longitude));

			JSONObject sysOb = datos_object.getJSONObject(system);
			weather.setCountry(sysOb.getString(country));
			weather.setSunrise(sysOb.getLong(sunrise));
			weather.setSunset(sysOb.getLong(sunset));

			if (datos_object.has(OpenWeatherMapManager.weather)) {
				JSONArray weatherAr = datos_object
						.getJSONArray(OpenWeatherMapManager.weather);
				JSONObject weatherOb = weatherAr.getJSONObject(0);
				weather.setWeather_id(weatherOb.getInt(weather_id));
				weather.setWeather_main(weatherOb.getString(weather_main));
				weather.setWeather_description(weatherOb
						.getString(weather_description));
				weather.setWeather_icon(weatherOb.getString(weather_icon));
			} else {
				Log.d(LogDavid, "jsonToWeather: NoWeather");
			}

			JSONObject mainOb = datos_object.getJSONObject(main);
			weather.setTemp(mainOb.getDouble(temp));
			weather.setPreassure(mainOb.getDouble(pressure));
			weather.setHumidity(mainOb.getDouble(humidity));

			if (mainOb.has(temp_max))
				weather.setTemp_max(mainOb.getDouble(temp_max));
			else
				weather.setTemp_max(0);
			if (mainOb.has(temp_min))
				weather.setTemp_min(mainOb.getDouble(temp_min));
			else
				weather.setTemp_min(0);
			if (mainOb.has(sea_level))
				weather.setSea_level(mainOb.getDouble(sea_level));
			else
				weather.setSea_level(0);
			if (mainOb.has(grnd_level))
				weather.setGrnd_level(mainOb.getDouble(grnd_level));
			else
				weather.setGrnd_level(0);

			JSONObject windOb = datos_object.getJSONObject(wind);
			weather.setWind_speed(windOb.getDouble(wind_speed));
			weather.setWind_degrees(windOb.getDouble(wind_degrees));

			if (datos_object.has(rain)) {
				JSONObject rainOb = datos_object.getJSONObject(rain);
				if(rainOb.has(rain_threehours)) {
					weather.setRain_threehours(rainOb.getDouble(rain_threehours));
					Log.d(LogDavid, "RAIN: " + rainOb.getDouble(rain_threehours));
				}
			} else {
				Log.d(LogDavid, "jsonToWeather: NoRain");
			}

			if (datos_object.has(snow)) {
				JSONObject rainOb = datos_object.getJSONObject(snow);
				if(rainOb.has(snow_threehours)) {
					weather.setRain_threehours(rainOb.getDouble(snow_threehours));
				}
			} else {
				Log.d(LogDavid, "jsonToWeather: NoSnow");
			}

			JSONObject cloudsOb = datos_object.getJSONObject(clouds);
			weather.setClouds_all(cloudsOb.getDouble(clouds_all));

			weather.setData_receiving(datos_object.getLong(data_receiving));
			weather.setCity_id(datos_object.getString(city_id));
			weather.setCity_name(datos_object.getString(city_name));
			weather.setCode(datos_object.getString(code));

			return weather;

		} catch (Exception e) {
			Log.e(LogDavid, "jsonToWeather: " + e.toString());
			return null;
		}
	}

}
