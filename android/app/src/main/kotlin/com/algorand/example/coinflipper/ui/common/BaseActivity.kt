package com.algorand.example.coinflipper.ui.common

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorand.example.coinflipper.R

open class BaseActivity: AppCompatActivity() {
    var passphraseTextField: String = ""

    @Composable
    fun AlgorandButton(resourceId: Int, stringResourceId: Int) {
        Button(
            onClick = {
                if (onClick(resourceId)) {
                    Modifier.alpha(1.0f)
                } else {
                    Modifier.alpha(0.3f)
                }
            },
            colors = ButtonDefaults.buttonColors(colorResource(R.color.teal_700)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .width(190.dp)
                .height(50.dp)

        )
        {
            Text(
                stringResource(stringResourceId),
                style = MaterialTheme.typography.button,
                color = Color.White
            )
        }
    }

    @Composable
    fun AlgorandDivider() {
        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier
                .width(300.dp))
    }

    @Composable
    fun PassphraseField(label: String, textData: String) {
        var textInput by remember { mutableStateOf(textData) }
        OutlinedTextField(
            value = textInput,
            textStyle = TextStyle.Default.copy(fontSize = 16.sp),
            onValueChange = { textInput = it; passphraseTextField = it },
            label = {
                Text(
                    text = label,
                    color = Color.Black
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray,
                disabledTextColor = Color.Gray,
                textColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 30.dp)
        )
    }

    open fun onClick(resourceId: Int): Boolean { return true }
}