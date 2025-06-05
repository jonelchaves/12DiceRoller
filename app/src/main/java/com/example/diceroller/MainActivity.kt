package com.example.diceroller
import android.media.AudioAttributes // Import for SoundPool
import android.media.SoundPool       // Import SoundPool
import androidx.compose.runtime.LaunchedEffect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
// MaterialTheme and Surface are good to have for theming, remove if not actively styling with them yet
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect // Import this
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle // Import this
import androidx.lifecycle.LifecycleEventObserver // Import this
import androidx.lifecycle.compose.LocalLifecycleOwner // THE CORRECT ONE!
import com.example.diceroller.ui.theme.DiceRollerTheme
import kotlin.random.Random // For (1..6).random() more explicitly

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiceRollerTheme { // Assuming DiceRollerTheme is defined
                DiceRollerApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiceRollerApp() {
    // Renamed for clarity to reflect it includes shake, image click, and button
    DiceRollerWithShakeImageAndButton(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )
}

// ... (other imports remain the same)

@Composable
fun DiceRollerWithShakeImageAndButton(modifier: Modifier = Modifier) {
    var result by remember { mutableStateOf(Random.nextInt(1, 7)) }
    val imageInteractionSource = remember { MutableInteractionSource() }

    // Define context and lifecycleOwner EARLIER
    val context = LocalContext.current // Moved UP
    val lifecycleOwner = LocalLifecycleOwner.current // Moved UP

    val imageResource = when (result) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        6 -> R.drawable.dice_6
        7 -> R.drawable.dice_7
        8 -> R.drawable.dice_8
        9 -> R.drawable.dice_9
        10 -> R.drawable.dice_10
        11 -> R.drawable.dice_11
        else -> R.drawable.dice_12
    }

    // --- SoundPool Setup ---
    val soundPool = remember {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setAudioAttributes(attributes)
            .setMaxStreams(1)
            .build()
    }
    var soundId by remember { mutableStateOf(0) }
    var soundLoaded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { // Key is Unit to run once
        // 'context' is now available here
        soundId = soundPool.load(context, R.raw.dice_roll, 1) // Replace R.raw.dice_roll
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (sampleId == soundId && status == 0) {
                soundLoaded = true
            }
        }
        onDispose {
            soundPool.release()
        }
    }

    LaunchedEffect(result, soundLoaded) {
        if (soundLoaded) {
            // Optional: Add a check here if you want to avoid playing on initial load
            // For example, by checking if this is the first time `result` is being processed
            // after `soundLoaded` became true.
            // if (/* not initial load or initial result */) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            // }
        }
    }
    // --- End SoundPool Setup ---


    // --- Shake Detection Setup ---
    // 'context' and 'lifecycleOwner' are available here as well
    DisposableEffect(lifecycleOwner) {
        val shakeDetector = ShakeDetector(context) {
            result = Random.nextInt(1, 13)
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                shakeDetector.start()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                shakeDetector.stop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            shakeDetector.stop()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // --- End Shake Detection Setup ---


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageResource),
            contentDescription = result.toString(),
            modifier = Modifier
                .clickable(
                    interactionSource = imageInteractionSource,
                    indication = null,
                    onClick = {
                        result = Random.nextInt(1, 13)
                    }
                )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            result = Random.nextInt(1, 13)
        }) {
            Text(stringResource(R.string.roll))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.tap_or_shake_to_roll),
            fontSize = 16.sp
        )
    }
}