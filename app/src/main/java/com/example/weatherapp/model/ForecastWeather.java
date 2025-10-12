package com.example.weatherapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ForecastWeather {
    @SerializedName("cod")
    private String cod;

    @SerializedName("message")
    private int message;

    @SerializedName("cnt")
    private int cnt;

    @SerializedName("list")
    private List<ForecastItem> list;

    @SerializedName("city")
    private City city;

    // Getters and setters
    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public int getMessage() {
        return message;
    }

    public void setMessage(int message) {
        this.message = message;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public List<ForecastItem> getList() {
        return list;
    }

    public void setList(List<ForecastItem> list) {
        this.list = list;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    // Nested classes
    public static class ForecastItem {
        @SerializedName("dt")
        private long dt;

        @SerializedName("main")
        private Main main;

        @SerializedName("weather")
        private List<Weather> weather;

        @SerializedName("clouds")
        private Clouds clouds;

        @SerializedName("wind")
        private Wind wind;

        @SerializedName("visibility")
        private int visibility;

        @SerializedName("pop")
        private double pop;

        @SerializedName("sys")
        private Sys sys;

        @SerializedName("dt_txt")
        private String dt_txt;

        // Getters and setters
        public long getDt() {
            return dt;
        }

        public void setDt(long dt) {
            this.dt = dt;
        }

        public Main getMain() {
            return main;
        }

        public void setMain(Main main) {
            this.main = main;
        }

        public List<Weather> getWeather() {
            return weather;
        }

        public void setWeather(List<Weather> weather) {
            this.weather = weather;
        }

        public Clouds getClouds() {
            return clouds;
        }

        public void setClouds(Clouds clouds) {
            this.clouds = clouds;
        }

        public Wind getWind() {
            return wind;
        }

        public void setWind(Wind wind) {
            this.wind = wind;
        }

        public int getVisibility() {
            return visibility;
        }

        public void setVisibility(int visibility) {
            this.visibility = visibility;
        }

        public double getPop() {
            return pop;
        }

        public void setPop(double pop) {
            this.pop = pop;
        }

        public Sys getSys() {
            return sys;
        }

        public void setSys(Sys sys) {
            this.sys = sys;
        }

        public String getDt_txt() {
            return dt_txt;
        }

        public void setDt_txt(String dt_txt) {
            this.dt_txt = dt_txt;
        }

        // Nested classes for ForecastItem
        public static class Main {
            @SerializedName("temp")
            private double temp;

            @SerializedName("feels_like")
            private double feels_like;

            @SerializedName("temp_min")
            private double temp_min;

            @SerializedName("temp_max")
            private double temp_max;

            @SerializedName("pressure")
            private int pressure;

            @SerializedName("sea_level")
            private int sea_level;

            @SerializedName("grnd_level")
            private int grnd_level;

            @SerializedName("humidity")
            private int humidity;

            @SerializedName("temp_kf")
            private double temp_kf;

            // Getters and setters
            public double getTemp() {
                return temp;
            }

            public void setTemp(double temp) {
                this.temp = temp;
            }

            public double getFeels_like() {
                return feels_like;
            }

            public void setFeels_like(double feels_like) {
                this.feels_like = feels_like;
            }

            public double getTemp_min() {
                return temp_min;
            }

            public void setTemp_min(double temp_min) {
                this.temp_min = temp_min;
            }

            public double getTemp_max() {
                return temp_max;
            }

            public void setTemp_max(double temp_max) {
                this.temp_max = temp_max;
            }

            public int getPressure() {
                return pressure;
            }

            public void setPressure(int pressure) {
                this.pressure = pressure;
            }

            public int getSea_level() {
                return sea_level;
            }

            public void setSea_level(int sea_level) {
                this.sea_level = sea_level;
            }

            public int getGrnd_level() {
                return grnd_level;
            }

            public void setGrnd_level(int grnd_level) {
                this.grnd_level = grnd_level;
            }

            public int getHumidity() {
                return humidity;
            }

            public void setHumidity(int humidity) {
                this.humidity = humidity;
            }

            public double getTemp_kf() {
                return temp_kf;
            }

            public void setTemp_kf(double temp_kf) {
                this.temp_kf = temp_kf;
            }
        }

        public static class Weather {
            @SerializedName("id")
            private int id;

            @SerializedName("main")
            private String main;

            @SerializedName("description")
            private String description;

            @SerializedName("icon")
            private String icon;

            // Getters and setters
            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getMain() {
                return main;
            }

            public void setMain(String main) {
                this.main = main;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }
        }

        public static class Clouds {
            @SerializedName("all")
            private int all;

            // Getters and setters
            public int getAll() {
                return all;
            }

            public void setAll(int all) {
                this.all = all;
            }
        }

        public static class Wind {
            @SerializedName("speed")
            private double speed;

            @SerializedName("deg")
            private int deg;

            @SerializedName("gust")
            private double gust;

            // Getters and setters
            public double getSpeed() {
                return speed;
            }

            public void setSpeed(double speed) {
                this.speed = speed;
            }

            public int getDeg() {
                return deg;
            }

            public void setDeg(int deg) {
                this.deg = deg;
            }

            public double getGust() {
                return gust;
            }

            public void setGust(double gust) {
                this.gust = gust;
            }
        }

        public static class Sys {
            @SerializedName("pod")
            private String pod;

            // Getters and setters
            public String getPod() {
                return pod;
            }

            public void setPod(String pod) {
                this.pod = pod;
            }
        }
    }

    public static class City {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("coord")
        private Coord coord;

        @SerializedName("country")
        private String country;

        @SerializedName("population")
        private int population;

        @SerializedName("timezone")
        private int timezone;

        @SerializedName("sunrise")
        private long sunrise;

        @SerializedName("sunset")
        private long sunset;

        // Getters and setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Coord getCoord() {
            return coord;
        }

        public void setCoord(Coord coord) {
            this.coord = coord;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public int getPopulation() {
            return population;
        }

        public void setPopulation(int population) {
            this.population = population;
        }

        public int getTimezone() {
            return timezone;
        }

        public void setTimezone(int timezone) {
            this.timezone = timezone;
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

        public static class Coord {
            @SerializedName("lat")
            private double lat;

            @SerializedName("lon")
            private double lon;

            // Getters and setters
            public double getLat() {
                return lat;
            }

            public void setLat(double lat) {
                this.lat = lat;
            }

            public double getLon() {
                return lon;
            }

            public void setLon(double lon) {
                this.lon = lon;
            }
        }
    }
}