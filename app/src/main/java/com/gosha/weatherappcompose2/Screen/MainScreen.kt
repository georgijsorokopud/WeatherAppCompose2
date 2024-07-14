package com.gosha.weatherappcompose2.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.gosha.weatherappcompose2.R
import com.gosha.weatherappcompose2.data.WeatherModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MainCard(currentDay: MutableState<WeatherModel>, onClickSync: () -> Unit, onClickSearch: () -> Unit) {
    val customBlue = Color(0xFF87CEEB)

    Column(
        modifier = Modifier
            .padding(5.dp)
    ) {
        // Карточка с информацией о погоде
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(0.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(customBlue),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Горизонтальный ряд с временем и иконкой погоды
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                        text = currentDay.value.time,
                        style = TextStyle(fontSize = 15.sp),
                        color = Color.White,
                    )
                    // изображение иконки погоды
                    AsyncImage(
                        model = "https:${currentDay.value.icon}",
                        contentDescription = "im2",
                        modifier = Modifier.size(35.dp)
                    )
                }
                // Текст с названием города
                Text(
                    text = currentDay.value.city,
                    style = TextStyle(fontSize = 25.sp),
                    color = Color.White,
                )
                // Текст с текущей температурой
                Text(
                    text = if (currentDay.value.currentTemp.isNotEmpty())
                        currentDay.value.currentTemp.toFloat().toInt().toString() + "°C"
                    else "${
                        currentDay.value.maxTemp.toFloat().toInt()
                    }°C/${currentDay.value.minTemp.toFloat().toInt()}°C",
                    style = TextStyle(fontSize = 55.sp),
                    color = Color.White,
                )
                // Текст с условием погоды
                Text(
                    text = currentDay.value.condition,
                    style = TextStyle(fontSize = 16.sp),
                    color = Color.White,
                )
                // Горизонтальный ряд с кнопками поиска и синхронизации
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        onClickSearch.invoke()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "im3",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "${
                            currentDay.value.maxTemp.toFloat().toInt()
                        }°C/${currentDay.value.minTemp.toFloat().toInt()}°C",
                        style = TextStyle(fontSize = 16.sp),
                        color = Color.White,
                    )
                    IconButton(onClick = {
                        onClickSync.invoke()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = "im3",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(daysList: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>) {
    // Список вкладок
    val tabList = listOf("HOURS", "DAYS")
    // Состояние для управления страницами вкладок
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val tabIndex = pagerState.currentPage

    Column(
        modifier = Modifier
            .padding(start = 5.dp, end = 5.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {
        // Вкладки для переключения между часами и днями
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.background(Color(0xFF87CEEB)),
            contentColor = Color(0xFF87CEEB),
            indicator = { pos ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(pos[tabIndex])
                )
            },
        ) {
            tabList.forEachIndexed { index, text ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(text = text)
                    })
            }
        }
        // Горизонтальный пейджер для отображения содержимого вкладок
        HorizontalPager(
            count = tabList.size,
            state = pagerState,
            modifier = Modifier.weight(1.0f)
        ) { index ->
            val list = when (index) {
                0 -> getWeatherByHours(currentDay.value.hours)
                1 -> daysList.value
                else -> daysList.value
            }
            MainList(list, currentDay)
        }
    }
}

// Функция для получения прогноза погоды по часам
private fun getWeatherByHours(hours: String): List<WeatherModel> {
    if (hours.isEmpty()) return listOf()
    val hoursArray = JSONArray(hours)
    val list = ArrayList<WeatherModel>()
    for (i in 0 until hoursArray.length()) {
        val item = hoursArray[i] as JSONObject
        list.add(
            WeatherModel(
                "",
                item.getString("time"),
                item.getString("temp_c").toFloat().toInt().toString() + "°C",
                item.getJSONObject("condition").getString("text"),
                item.getJSONObject("condition").getString("icon"),
                "",
                "",
                "",
            )
        )
    }
    return list
}
