package com.deepseek.studycircle.Screens.Session

import android.Manifest
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.deepseek.studycircle.data.UserViewModel
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

@Composable
fun VideoCallScreen(
    navController: NavHostController,
    sessionId: String,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOff by remember { mutableStateOf(false) }
    var remoteUid by remember { mutableIntStateOf(0) }
    var rtcEngine by remember { mutableStateOf<RtcEngine?>(null) }
    
    val session = userViewModel.allSessions.find { it.id == sessionId }
    val userData = userViewModel.userData.value

    // Permission Handling
    val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.all { it }
        if (!areGranted) {
            Toast.makeText(context, "Permissions required for video call", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }

    // Agora Engine Lifecycle
    DisposableEffect(Unit) {
        val config = RtcEngineConfig()
        config.mContext = context
        config.mAppId = "44446c65636b446565705365656b3132" // Placeholder / Example ID
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                // Local user joined
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                remoteUid = uid
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                if (remoteUid == uid) remoteUid = 0
            }
        }

        try {
            val engine = RtcEngine.create(config)
            engine.enableVideo()
            engine.startPreview()
            engine.joinChannel(null, sessionId, null, 0)
            rtcEngine = engine
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A))) {
        // Main Video (Remote User)
        if (remoteUid != 0) {
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).apply {
                        rtcEngine?.setupRemoteVideo(
                            VideoCanvas(this, VideoCanvas.RENDER_MODE_HIDDEN, remoteUid)
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder for Peer
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = "",
                    contentDescription = "Peer Video",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Waiting for others to join...",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Overlay for Peer Name
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Text(
                text = session?.student ?: "Peer",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 14.sp
            )
        }

        // Self Video Preview (Small overlay)
        Card(
            modifier = Modifier
                .size(120.dp, 180.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                if (!isVideoOff) {
                    AndroidView(
                        factory = { ctx ->
                            SurfaceView(ctx).apply {
                                rtcEngine?.setupLocalVideo(
                                    VideoCanvas(this, VideoCanvas.RENDER_MODE_HIDDEN, 0)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val imageUrl = if (userData?.imageUri?.isNotEmpty() == true) userData.imageUri else "https://cdn-icons-png.flaticon.com/512/3135/3135715.png"
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "My Avatar",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                    Icon(
                        Icons.Default.VideocamOff,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).align(Alignment.Center),
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(32.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        isMuted = !isMuted
                        rtcEngine?.muteLocalAudioStream(isMuted)
                    },
                    modifier = Modifier.background(if (isMuted) Color.Red else Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, null, tint = Color.White)
                }

                IconButton(
                    onClick = { 
                        isVideoOff = !isVideoOff
                        rtcEngine?.muteLocalVideoStream(isVideoOff)
                    },
                    modifier = Modifier.background(if (isVideoOff) Color.Red else Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam, null, tint = Color.White)
                }

                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(56.dp).background(Color.Red, CircleShape)
                ) {
                    Icon(Icons.Default.CallEnd, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }

                IconButton(
                    onClick = { /* Chat logic can be added here */ },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = Color.White)
                }

                IconButton(
                    onClick = { /* Participants list */ },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.People, null, tint = Color.White)
                }
            }
        }
    }
}
