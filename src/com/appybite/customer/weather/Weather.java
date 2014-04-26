package com.appybite.customer.weather;

public class Weather {
	private static final String LogDavid = "OpenWeather";

	// coord
	private double latitude, longitude;

	// sys
	private String country;
	private long sunrise, sunset;

	// weather
	private int weather_id;
	private String weather_main, weather_description, weather_icon;

	// main
	private double temp, temp_max, temp_min, preassure, sea_level, grnd_level,
			humidity;

	// wind
	private double wind_speed, wind_degrees;

	// rain
	private double rain_threehours;

	// snow
	private double snow_threehours;

	// clouds
	private double clouds_all;

	private long data_receiving;
	private String city_id, city_name, code;

	// METODOS

	public String toString() {
		return "" + (float) Conversor.kelvinToCelsius(temp) + "\n" + city_name
				+ "\n" + Conversor.dateToString(data_receiving);
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public long getSunrise() {
		return sunrise;
	}

	public void setSunrise(long sunrise) {
		this.sunrise = sunrise;
	}

	public long getSunset() {
		return sunset;
	}

	public void setSunset(long sunset) {
		this.sunset = sunset;
	}

	public int getWeather_id() {
		return weather_id;
	}

	public void setWeather_id(int weather_id) {
		this.weather_id = weather_id;
	}

	public String getWeather_main() {
		return weather_main;
	}

	public void setWeather_main(String weather_main) {
		this.weather_main = weather_main;
	}

	public String getWeather_description() {
		return weather_description;
	}

	public void setWeather_description(String weather_description) {
		this.weather_description = weather_description;
	}

	public String getWeather_icon() {
		return weather_icon;
	}

	public void setWeather_icon(String weather_icon) {
		this.weather_icon = weather_icon;
	}

	public double getTemp() {
		return temp;
	}

	public void setTemp(double temp) {
		this.temp = temp;
	}

	public double getTemp_max() {
		return temp_max;
	}

	public void setTemp_max(double temp_max) {
		this.temp_max = temp_max;
	}

	public double getTemp_min() {
		return temp_min;
	}

	public void setTemp_min(double temp_min) {
		this.temp_min = temp_min;
	}

	public double getPreassure() {
		return preassure;
	}

	public void setPreassure(double preassure) {
		this.preassure = preassure;
	}

	public double getSea_level() {
		return sea_level;
	}

	public void setSea_level(double sea_level) {
		this.sea_level = sea_level;
	}

	public double getGrnd_level() {
		return grnd_level;
	}

	public void setGrnd_level(double grnd_level) {
		this.grnd_level = grnd_level;
	}

	public double getHumidity() {
		return humidity;
	}

	public void setHumidity(double humidity) {
		this.humidity = humidity;
	}

	public double getWind_speed() {
		return wind_speed;
	}

	public void setWind_speed(double wind_speed) {
		this.wind_speed = wind_speed;
	}

	public double getWind_degrees() {
		return wind_degrees;
	}

	public void setWind_degrees(double wind_degrees) {
		this.wind_degrees = wind_degrees;
	}

	public double getRain_threehours() {
		return rain_threehours;
	}

	public void setRain_threehours(double rain_threehours) {
		this.rain_threehours = rain_threehours;
	}

	public double getSnow_threehours() {
		return snow_threehours;
	}

	public void setSnow_threehours(double snow_threehours) {
		this.snow_threehours = snow_threehours;
	}

	public double getClouds_all() {
		return clouds_all;
	}

	public void setClouds_all(double clouds_all) {
		this.clouds_all = clouds_all;
	}

	public long getData_receiving() {
		return data_receiving;
	}

	public void setData_receiving(long data_receiving) {
		this.data_receiving = data_receiving;
	}

	public String getCity_id() {
		return city_id;
	}

	public void setCity_id(String city_id) {
		this.city_id = city_id;
	}

	public String getCity_name() {
		return city_name;
	}

	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}