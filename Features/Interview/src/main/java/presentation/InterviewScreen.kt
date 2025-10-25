package presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import usecases.RecordVideoUseCase


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WelcomeScreen(viewModel: InterviewScreenViewModel){


    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val internetPermissionState = rememberPermissionState(Manifest.permission.INTERNET)


    if (cameraPermissionState.status.isGranted && audioPermissionState.status.isGranted && internetPermissionState.status.isGranted){
        InterviewScreen(viewModel)
    }
    else{
        Column(modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
            .widthIn(max = 480.dp)
            .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally){
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
            Text(textToShow, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                cameraPermissionState.launchPermissionRequest()
                audioPermissionState.launchPermissionRequest()
                internetPermissionState.launchPermissionRequest()
            }) {
                Text("Предоставить разрешение")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun InterviewScreen(viewModel: InterviewScreenViewModel){

    val context: Context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.setContext(context)
        viewModel.startVideoPreview(lifecycleOwner, previewView?: return@LaunchedEffect)

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Center
    ) {

        Box(modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .align(alignment = Alignment.CenterHorizontally)
            ) {
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        previewView = this
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

        }

        Spacer(
            modifier = Modifier.height(200.dp)
        )


        //TODO: заглушка аватара
        Card(modifier = Modifier.padding(16.dp)){
            Text(
                text = "\uD83E\uDD16 эйчарчик ",
                modifier = Modifier.padding(16.dp)
            )
        }
        Log.d("InterviewScreen", "questions: ${uiState.interviewInfo.questions.toString()}")
        val currentQuestion = uiState.interviewInfo.questions.getOrNull(uiState.currentQuestionIndex)



        if(uiState.isInterviewStarted){
            Card {
                if (uiState.isInterviewFinished){
                    Text("")
                }
                else{
                    Text(
                        text = currentQuestion?: ""
                    )
                }

            }
        }
        else if (uiState.isInterviewFinished){
            Card {
                Text(
                    text = "На этом всё!"
                )
            }
        }
        else{
            Card {
                Text(
                    text = "Пожалуйста, приготовьтесь!"
                )
            }
        }



        Spacer(
            modifier = Modifier
                .height(40.dp)
        )

        if (!uiState.isInterviewStarted && !uiState.isInterviewFinished){
            Button(
                onClick = {
                    viewModel.startInterview(lifecycleOwner, previewView?: return@Button)
                }
            ) {
                Text(text = "Начать интервью")
            }
        }

        else if (!uiState.isInterviewFinished && uiState.isInterviewStarted){
            TextButton(
                onClick = {
                    if (uiState.currentQuestionIndex+1==uiState.interviewInfo.questions.count()){
                        //последний вопрос
                        viewModel.stopInterview()
                    }
                    else{
                        viewModel.nextQuestion()
                    }
                },
                modifier = Modifier,
            ) {

                if(uiState.isInterviewFinished){
                    Text("Интервью окончено!")
                    //viewModel.sendInterviewToAi(uiState.interviewInfo)
                }
                else if(uiState.currentQuestionIndex+1 == uiState.interviewInfo.questions.count()){
                    Text("Закончить интервью!")
                }
                else{
                    Text(text = "Следующий вопрос!")
                }
            }
        }

        else{
                Text(
                    text = "На этом всё!"
                )
                if (uiState.testFinalScore == -1){
                    Text("Пожалуйста, не закрывайте приложение\nРезультаты загружаются...", textAlign = TextAlign.Center)
                }
                else{
                    Text("Ваш итоговый балл: ${uiState.testFinalScore}", textAlign = TextAlign.Center)
            }
        }
    }

}



@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun preview(){
    MaterialTheme {
    }
}