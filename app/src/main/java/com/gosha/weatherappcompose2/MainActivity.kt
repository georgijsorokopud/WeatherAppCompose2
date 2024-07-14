package com.gosha.weatherappcompose2

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.gosha.weatherappcompose2.Screen.DialogSearch
import com.gosha.weatherappcompose2.Screen.MainCard
import com.gosha.weatherappcompose2.Screen.TabLayout
import com.gosha.weatherappcompose2.data.WeatherModel
import com.gosha.weatherappcompose2.ui.theme.WeatherAppCompose2Theme
import org.json.JSONObject

const val API_KEY = "00f97d8d3d2b4b9bb08131717241306"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppCompose2Theme {
                // Состояние для списка дней
                val daysList = remember {
                    mutableStateOf(listOf<WeatherModel>())
                }
                // Состояние для диалога поиска
                val dialogState = remember {
                    mutableStateOf(false)
                }
                // Состояние для текущего дня
                val currentDay = remember {
                    mutableStateOf(
                        WeatherModel(
                            "",
                            "",
                            "10.0",
                            "",
                            "",
                            "10.0",
                            "10.0",
                            "",
                        )
                    )
                }
                // Показ диалога поиска, если состояние dialogState истинно
                if (dialogState.value) {
                    DialogSearch(dialogState, onSubmit = {
                        getData(it, this, daysList, currentDay)
                    })
                }
                // Получение данных о погоде для Лондона при запуске
                getData("London", this, daysList, currentDay)
                Box(modifier = Modifier.fillMaxSize())
                Column {
                    // Главная карточка с текущей погодой
                    MainCard(currentDay, onClickSync = {
                        getData("белгород", this@MainActivity, daysList, currentDay)
                    }, onClickSearch = {
                        dialogState.value = true
                    })
                    // Таблица с прогнозом по часам и дням
                    TabLayout(daysList, currentDay)
                }
            }
        }
    }
}

// Функция для получения данных о погоде с сервера
private fun getData(
    city: String,
    context: Context,
    daysList: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>
) {
    // URL для запроса с параметрами
    val url = "https://api.weatherapi.com/v1/forecast.json" +
            "?key=$API_KEY" +
            "&q=$city" +
            "&days=3" +
            "&aqi=no&alerts=no"
    // Создание очереди запросов
    val queue = Volley.newRequestQueue(context)
    // Создание запроса на получение данных о погоде
    val sRequest = StringRequest(
        Request.Method.GET,
        url,
        { response ->
            // Обработка успешного ответа
            val list = getWeatherByDays(response)
            currentDay.value = list[0]
            daysList.value = list
        },
        {
            // Логирование ошибки в случае неудачи
            Log.d("MyLog", "error: $it")
        }
    )
    // Добавление запроса в очередь
    queue.add(sRequest)
}

// Функция для парсинга данных о погоде из JSON ответа
private fun getWeatherByDays(response: String): List<WeatherModel> {
    if (response.isEmpty()) return listOf()
    val list = ArrayList<WeatherModel>()
    val mainObject = JSONObject(response)
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    // Парсинг данных для каждого дня
    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                item.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString()
            )
        )
    }
    // Обновление данных для текущего дня
    list[0] = list[0].copy(
        time = mainObject.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObject.getJSONObject("current").getString("temp_c"),
    )
    return list
}
