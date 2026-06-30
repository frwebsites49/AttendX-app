package com.college.attendx.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.attendx.R
import kotlinx.coroutines.delay

/**
 * Brutalist splash screen.
 *
 * LOGO OPTION:
 * By default this shows a plain black square placeholder with no image
 * (so the project compiles even if you haven't added a logo file yet).
 * To use your own logo:
 *   1. Drop your image into app/src/main/res/drawable/ (e.g. logo.png)
 *   2. Change the line below:
 *        val logoResId: Int? = null
 *      to:
 *        val logoResId: Int? = R.drawable.logo
 *   (swap "logo" for whatever you actually named the file, no extension)
 *
 * Progress fills 0 -> 100% over ~1.6s, then waits exactly 1 extra second
 * at 100% before calling onAnimationComplete().
 */
@Composable
fun SplashScreenContent(
    onAnimationComplete: () -> Unit
) {
    // ---- SET YOUR LOGO HERE ----
    // null = shows the plain "A" placeholder block below.
    // R.drawable.your_file_name = shows your actual logo image.
    val logoResId: Int? = R.drawable.logo
    // -----------------------------

    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Fill 0 -> 1.0 over roughly 1.6 seconds (80 steps * 20ms)
        while (progress < 1f) {
            progress += 0.0125f
            delay(20)
        }
        progress = 1f

        // Sit at 100% for exactly 1 second before finishing, as requested.
        delay(1000)

        onAnimationComplete()
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing),
        label = "splashProgress"
    )

    val brutalBlack = Color(0xFF000000)
    val brutalYellow = Color(0xFFFFD500)
    val brutalBorder = Color(0xFF2A2A2A)
    val brutalGray = Color(0xFF9E9E9E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brutalBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // ---- Logo ----
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(0.dp)) // sharp corners, brutalist
                    .background(Color.Transparent)
                    .run {
                        if (logoResId == null) {
                            this.background(brutalYellow)
                        } else {
                            this
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (logoResId != null) {
                    Image(
                        painter = painterResource(id = logoResId),
                        contentDescription = "App Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder shown until a real logo is wired in - see
                    // the logoResId comment block above this composable.
                    Text(
                        text = "A",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = brutalBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ---- App name ----
            Text(
                text = "ATTENDX",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(4.dp)
                    .background(brutalYellow)
            )

            Spacer(modifier = Modifier.height(56.dp))

            // ---- Progress bar ----
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(8.dp)
                    .background(brutalBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(brutalYellow)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = brutalGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}