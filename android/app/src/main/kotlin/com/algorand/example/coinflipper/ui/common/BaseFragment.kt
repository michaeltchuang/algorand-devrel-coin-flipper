package com.algorand.example.coinflipper.ui.common

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.algorand.example.coinflipper.R

open class BaseFragment : Fragment() {
    @Suppress("ComposableNaming")
    @Composable
    fun algorandFragmentButton(
        resourceId: Int,
        stringResourceId: Int,
    ) {
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
            modifier =
                Modifier
                    .width(190.dp)
                    .height(50.dp),
        ) {
            Text(
                stringResource(stringResourceId),
                style = MaterialTheme.typography.button,
                color = Color.White,
            )
        }
    }

    open fun onClick(resourceId: Int): Boolean {
        return true
    }
}
