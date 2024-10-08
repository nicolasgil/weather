package com.nicolas.weatherapp.data.datasources.remote

import android.util.Log
import com.nicolas.weatherapp.BuildConfig
import com.nicolas.weatherapp.data.datasources.remote.remotemodel.ForecastResponse
import com.nicolas.weatherapp.domain.models.Condition
import com.nicolas.weatherapp.domain.models.CurrentWeather
import com.nicolas.weatherapp.domain.models.Day
import com.nicolas.weatherapp.domain.models.Forecast
import com.nicolas.weatherapp.domain.models.ForecastDay
import com.nicolas.weatherapp.domain.models.Hour
import com.nicolas.weatherapp.domain.models.Location
import com.nicolas.weatherapp.domain.models.WeatherForecast
import com.nicolas.weatherapp.utils.WeatherForecastObject
import javax.inject.Inject

class LocationRemoteDataSourceImpl @Inject constructor(
    private val locationService: LocationService,
) : RemoteDataSource {

    private val apiKey = BuildConfig.WEATHER_API_KEY

    override suspend fun getAllLocations(query: String): List<Location> {

        return try {
            val response = locationService.searchLocations(apiKey = apiKey, query = query)
            response ?: emptyList()
        } catch (e: Exception) {
            Log.e("LocationRemoteData", "Error fetching remote locations", e)
            emptyList()
        }
    }

    override suspend fun getDetailsLocation(cityName: String): WeatherForecast {
        val defaultWeatherForecast = WeatherForecastObject.EMPTY

        return try {
            val response =
                locationService.getForecast(apiKey = apiKey, location = cityName, days = 3)
            response.toWeatherForecast() ?: defaultWeatherForecast
        } catch (e: Exception) {
            Log.e("LocationRemoteData", "Error fetching remote locations", e)
            defaultWeatherForecast
        }
    }

    private fun ForecastResponse.toWeatherForecast(): WeatherForecast {
        return WeatherForecast(
            location = Location(
                name = this.location.name,
                country = this.location.country
            ),
            current = CurrentWeather(
                temp_c = this.current.tempC,
                feelslike_c = this.current.feelsLikeC,
                wind_mph = this.current.windMph,
                humidity = this.current.humidity,
                condition = Condition(
                    text = this.current.condition.text,
                    icon = this.current.condition.icon
                )
            ),
            forecast = Forecast(
                forecastday = this.forecast.forecastday.map { forecastDay ->
                    ForecastDay(
                        date = forecastDay.date,
                        day = Day(
                            avgtemp_c = forecastDay.day.avgTempC,
                            condition = Condition(
                                text = forecastDay.day.condition.text,
                                icon = forecastDay.day.condition.icon
                            )
                        ),
                        hour = forecastDay.hour.map { hour ->
                            Hour(
                                time = hour.time,
                                temp_c = hour.tempC,
                                condition = Condition(
                                    text = hour.condition.text,
                                    icon = hour.condition.icon
                                )
                            )
                        }
                    )
                }
            )
        )
    }


}
