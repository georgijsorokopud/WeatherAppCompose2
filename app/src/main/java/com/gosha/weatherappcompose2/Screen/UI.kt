package com.gosha.weatherappcompose2.Screen

import android.app.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gosha.weatherappcompose2.data.WeatherModel

@Composable
fun MainList(list: List<WeatherModel>, currentDay: MutableState<WeatherModel>) {
    // Создаем LazyColumn для отображения списка элементов
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Для каждого элемента списка вызываем ListItem
        itemsIndexed(
            list
        ) { _, item ->
            ListItem(item, currentDay)
        }
    }
}

// Компонент для отображения одного элемента списка
@Composable
fun ListItem(item: WeatherModel, currentDay: MutableState<WeatherModel>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp)
            .clickable {
                // Обновляем текущий день при клике на элемент списка
                if (item.hours.isEmpty()) return@clickable
                currentDay.value = item
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF87CEEB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(5.dp),
    ) {
        // Горизонтальный ряд для размещения информации о погоде
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF87CEEB)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Колонка с временем и условием погоды
            Column(
                modifier = Modifier.padding(start = 5.dp, top = 5.dp, bottom = 5.dp)
            ) {
                Text(
                    text = item.time,
                    color = Color.White,
                )
                Text(
                    text = item.condition,
                    color = Color.White
                )
            }
            // Текст с текущей или максимальной/минимальной температурой
            Text(
                text = item.currentTemp.ifEmpty { "${item.maxTemp}°C/${item.minTemp}°C" },
                color = Color.White,
                style = TextStyle(fontSize = 25.sp)
            )
            // Асинхронное изображение иконки погоды
            AsyncImage(
                model = "https:" + item.icon,
                contentDescription = "im2",
                modifier = Modifier.size(35.dp)
            )
        }
    }
}

@Composable
fun DialogSearch(dialogState: MutableState<Boolean>, onSubmit: (String) -> Unit) {
    val dialogText = remember {
        mutableStateOf("")
    }
    // Создаем диалоговое окно для поиска города
    AlertDialog(onDismissRequest = {
        dialogState.value = false
    },
        confirmButton = {
            // Кнопка подтверждения
            TextButton(onClick = {
                onSubmit(dialogText.value)
                dialogState.value = false
            }) {
                Text(text = "Ok")
            }
        },
        dismissButton = {
            // Кнопка отмены
            TextButton(onClick = {
                dialogState.value = false
            }) {
                Text(text = "Cancel")
            }
        },
        title = {
            // Колонка с текстовым полем для ввода названия города
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Введите название города:")
                TextField(value = dialogText.value, onValueChange = {
                    dialogText.value = it
                })
            }
        }
    )
}
