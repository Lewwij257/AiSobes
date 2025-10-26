//package presentation
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Context
//import android.os.Build
//import android.util.Log
//import androidx.annotation.RequiresApi
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.widthIn
//import androidx.compose.foundation.layout.wrapContentSize
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import com.google.accompanist.permissions.ExperimentalPermissionsApi
//import com.google.accompanist.permissions.isGranted
//import com.google.accompanist.permissions.rememberPermissionState
//import com.google.accompanist.permissions.shouldShowRationale
//import usecases.RecordVideoUseCase
//
//
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun WelcomeScreen(viewModel: InterviewScreenViewModel){
//
//
//    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
//    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
//    val internetPermissionState = rememberPermissionState(Manifest.permission.INTERNET)
//
//
//    if (cameraPermissionState.status.isGranted && audioPermissionState.status.isGranted && internetPermissionState.status.isGranted){
//        InterviewScreen(viewModel)
//    }
//    else{
//        Column(modifier = Modifier
//            .fillMaxSize()
//            .wrapContentSize()
//            .widthIn(max = 480.dp)
//            .padding(20.dp),
//            horizontalAlignment = Alignment.CenterHorizontally){
//            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
//                // If the user has denied the permission but the rationale can be shown,
//                // then gently explain why the app requires this permission
//                "Пожалуйста, предоставьте разрешение на доступ к камере устройства" + "\n" +
//                        "Честное слово, мы не будем следить за вами" + "\n" +
//                        "(мы не знаем как это делать)"
//            } else {
//                // If it's the first time the user lands on this feature, or the user
//                // doesn't want to be asked again for this permission, explain that the
//                // permission is required
//                "Для работы приложения нужен доступ к камере " +
//                        "Пожалуйста, нажмите ниже чтобы всё заработало"
//            }
//            Text(textToShow, textAlign = TextAlign.Center)
//            Spacer(Modifier.height(16.dp))
//            Button(onClick = {
//                cameraPermissionState.launchPermissionRequest()
//                audioPermissionState.launchPermissionRequest()
//                internetPermissionState.launchPermissionRequest()
//            }) {
//                Text("Предоставить разрешение")
//            }
//        }
//    }
//}
//
//@RequiresApi(Build.VERSION_CODES.Q)
//@Composable
//fun InterviewScreen(viewModel: InterviewScreenViewModel){
//
//    val context: Context = LocalContext.current
//    val uiState by viewModel.uiState.collectAsState()
//    var previewView by remember { mutableStateOf<PreviewView?>(null) }
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    LaunchedEffect(Unit) {
//        viewModel.setContext(context)
//        viewModel.startVideoPreview(lifecycleOwner, previewView?: return@LaunchedEffect)
//
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        //verticalArrangement = Arrangement.Center
//    ) {
//
//        Box(modifier = Modifier
//            .height(200.dp)
//            .fillMaxWidth()
//            .align(alignment = Alignment.CenterHorizontally)
//            ) {
//            AndroidView(
//                factory = { context ->
//                    PreviewView(context).apply {
//                        previewView = this
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//            )
//
//        }
//
//        Spacer(
//            modifier = Modifier.height(200.dp)
//        )
//
//
//        //TODO: заглушка аватара
//        Card(modifier = Modifier.padding(16.dp)){
//            Text(
//                text = "\uD83E\uDD16 эйчарчик ",
//                modifier = Modifier.padding(16.dp)
//            )
//        }
//        Log.d("InterviewScreen", "questions: ${uiState.interviewInfo.questions.toString()}")
//        val currentQuestion = uiState.interviewInfo.questions.getOrNull(uiState.currentQuestionIndex)
//
//
//
//        if(uiState.isInterviewStarted){
//            Card {
//                if (uiState.isInterviewFinished){
//                    Text("")
//                }
//                else{
//                    Text(
//                        text = currentQuestion?: ""
//                    )
//                }
//
//            }
//        }
//        else if (uiState.isInterviewFinished){
//            Card {
//                Text(
//                    text = "На этом всё!"
//                )
//            }
//        }
//        else{
//            Card {
//                Text(
//                    text = "Пожалуйста, приготовьтесь!"
//                )
//            }
//        }
//
//
//
//        Spacer(
//            modifier = Modifier
//                .height(40.dp)
//        )
//
//        if (!uiState.isInterviewStarted && !uiState.isInterviewFinished){
//            Button(
//                onClick = {
//                    viewModel.startInterview(lifecycleOwner, previewView?: return@Button)
//                }
//            ) {
//                Text(text = "Начать интервью")
//            }
//        }
//
//        else if (!uiState.isInterviewFinished && uiState.isInterviewStarted){
//            TextButton(
//                onClick = {
//                    if (uiState.currentQuestionIndex+1==uiState.interviewInfo.questions.count()){
//                        //последний вопрос
//                        viewModel.stopInterview()
//                    }
//                    else{
//                        viewModel.nextQuestion()
//                    }
//                },
//                modifier = Modifier,
//            ) {
//
//                if(uiState.isInterviewFinished){
//                    Text("Интервью окончено!")
//                    //viewModel.sendInterviewToAi(uiState.interviewInfo)
//                }
//                else if(uiState.currentQuestionIndex+1 == uiState.interviewInfo.questions.count()){
//                    Text("Закончить интервью!")
//                }
//                else{
//                    Text(text = "Следующий вопрос!")
//                }
//            }
//        }
//
//        else{
//                Text(
//                    text = "На этом всё!"
//                )
//                if (uiState.testFinalScore == -1){
//                    Text("Пожалуйста, не закрывайте приложение\nРезультаты загружаются...", textAlign = TextAlign.Center)
//                }
//                else{
//                    Text("Ваш итоговый балл: ${uiState.testFinalScore}", textAlign = TextAlign.Center)
//            }
//        }
//    }
//
//}
//
//
//
//@SuppressLint("ViewModelConstructorInComposable")
//@Preview
//@Composable
//fun preview(){
//    MaterialTheme {
//    }
//}

package presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import usecases.RecordVideoUseCase

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WelcomeScreen(viewModel: InterviewScreenViewModel) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val internetPermissionState = rememberPermissionState(Manifest.permission.INTERNET)

    if (cameraPermissionState.status.isGranted && audioPermissionState.status.isGranted && internetPermissionState.status.isGranted) {
        InterviewScreen(viewModel)
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
                    .widthIn(max = 480.dp)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Permission icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    colorScheme.primary,
                                    colorScheme.primaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                    // If the user has denied the permission but the rationale can be shown,
                    // then gently explain why the app requires this permission
                    "Пожалуйста, предоставьте разрешение на доступ к камере устройства" + "\n" +
                            "Честное слово, мы не будем следить за вами" + "\n" +
                            "(мы не знаем как это делать)"
                } else {
                    // If it's the first time the user lands on this feature, or the user
                    // doesn't want to be asked again for this permission, explain that the
                    // permission is required
                    "Для работы приложения нужен доступ к камере " +
                            "Пожалуйста, нажмите ниже чтобы всё заработало"
                }

                Text(
                    text = textToShow,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onBackground,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        cameraPermissionState.launchPermissionRequest()
                        audioPermissionState.launchPermissionRequest()
                        internetPermissionState.launchPermissionRequest()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Предоставить разрешение",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun InterviewScreen(viewModel: InterviewScreenViewModel) {
    val context: Context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.setContext(context)
        viewModel.startVideoPreview(lifecycleOwner, previewView ?: return@LaunchedEffect)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Camera Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                previewView = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )
                }
            }

            // HR Avatar Card
            Card(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83E\uDD16",
                        fontSize = 32.sp,
                        color = colorScheme.onPrimaryContainer
                    )
                }
            }

            Log.d("InterviewScreen", "questions: ${uiState.interviewInfo.questions.toString()}")
            val currentQuestion = uiState.interviewInfo.questions.getOrNull(uiState.currentQuestionIndex)

            // Question/Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isInterviewStarted) {
                        if (uiState.isInterviewFinished) {
                            Text(
                                text = "Интервью завершено!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colorScheme.primary,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = currentQuestion ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                        }
                    } else if (uiState.isInterviewFinished) {
                        Text(
                            text = "На этом всё!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colorScheme.primary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Пожалуйста, приготовьтесь!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Action Buttons
            if (!uiState.isInterviewStarted && !uiState.isInterviewFinished) {
                Button(
                    onClick = {
                        viewModel.startInterview(lifecycleOwner, previewView ?: return@Button)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Начать интервью",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (!uiState.isInterviewFinished && uiState.isInterviewStarted) {
                TextButton(
                    onClick = {
                        if (uiState.currentQuestionIndex + 1 == uiState.interviewInfo.questions.count()) {
                            //последний вопрос
                            viewModel.stopInterview()
                        } else {
                            viewModel.nextQuestion()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    colorScheme.secondary,
                                    colorScheme.secondaryContainer
                                )
                            )
                        ),
                ) {
                    if (uiState.isInterviewFinished) {
                        Text(
                            "Интервью окончено!",
                            color = colorScheme.onSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        //viewModel.sendInterviewToAi(uiState.interviewInfo)
                    } else if (uiState.currentQuestionIndex + 1 == uiState.interviewInfo.questions.count()) {
                        Text(
                            "Закончить интервью!",
                            color = colorScheme.onSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Следующий вопрос!",
                                color = colorScheme.onSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.padding(start = 8.dp),
                                tint = colorScheme.onSecondary
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "На этом всё!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (uiState.testFinalScore == -1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Пожалуйста, не закрывайте приложение\nРезультаты загружаются...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Ваш итоговый балл: ${uiState.testFinalScore}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = colorScheme.primary,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun preview() {
    MaterialTheme {
        // Preview content can be added here if needed
    }
}