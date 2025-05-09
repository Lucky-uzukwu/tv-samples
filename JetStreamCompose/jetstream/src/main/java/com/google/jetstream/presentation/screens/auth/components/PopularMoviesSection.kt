package com.google.jetstream.presentation.screens.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

@Composable
fun PopularMoviesSection(
) {
    Text(
        text = "Popular Movies",
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        // TODO: fix this , potential fix is to make the function an extension function with column scope
//        modifier = Modifier.align(Alignment.Start)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(8.dp))
            )
        }
    }
}