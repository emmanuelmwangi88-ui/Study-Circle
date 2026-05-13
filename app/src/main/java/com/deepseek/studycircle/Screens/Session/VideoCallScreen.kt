package com.deepseek.studycircle.Screens.Session

import android.Manifest
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.deepseek.studycircle.BuildConfig
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.navigation.ROUTE_WHITEBOARD
import io.agora.rtc2.ChannelMediaOptions
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
    val remoteUids = remember { mutableStateListOf<Int>() }
    var rtcEngine by remember { mutableStateOf<RtcEngine?>(null) }
    var hasPermissions by remember { mutableStateOf(false) }

    val allSessions = userViewModel.allSessions
    val allGroups by userViewModel.allStudyGroups.collectAsState()
    val userData by userViewModel.userData
    
    val session = remember(allSessions, sessionId) { allSessions.find { it.id == sessionId } }
    val group = remember(allGroups, sessionId) { allGroups.find { it.id.toString() == sessionId } }

    val callTitle = session?.title ?: group?.name ?: "Study Call"

    val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap.values.all { it }) {
            hasPermissions = true
        } else {
            Toast.makeText(context, "Permissions required for video call", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }

    DisposableEffect(hasPermissions) {
        if (!hasPermissions) return@DisposableEffect onDispose { }

        val appId = BuildConfig.AGORA_APP_ID
        if (appId.isNullOrBlank() || appId == "null") {
            Toast.makeText(context, "Agora App ID is not configured", Toast.LENGTH_LONG).show()
        }

        try {
            val config = RtcEngineConfig().apply {
                mContext = context
                mAppId = appId
                mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                        // Successfully joined
                    }
                    override fun onUserJoined(uid: Int, elapsed: Int) {
                        if (!remoteUids.contains(uid)) {
                            remoteUids.add(uid)
                        }
                    }
                    override fun onUserOffline(uid: Int, reason: Int) {
                        remoteUids.remove(uid)
                    }
                    override fun onError(err: Int) {
                        super.onError(err)
                    }
                }
            }

            val engine = RtcEngine.create(config)
            engine.enableVideo()
            engine.startPreview()
            
            val options = ChannelMediaOptions()
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            options.publishCameraTrack = true
            options.publishMicrophoneTrack = true
            
            val token = if (BuildConfig.AGORA_TOKEN == "007eJxTYFDXYI2pe+F6KfqgcuBS4fXVaSVPJBq5Hi3vnpoXtnlry0MFBjMjC4OUtGTDNJMkI5MkE5OkREszY0MzU6NkSwsDM8u0OFHmrIZARoYgoY9MjAyMDCxADOIzgUlmMMkCJlkZiktKUyoZGAAgAiCA" || BuildConfig.AGORA_TOKEN == "null") null else BuildConfig.AGORA_TOKEN
            engine.joinChannel(token, sessionId, 0, options)
            
            rtcEngine = engine
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        onDispose {
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A))) {
        // Remote Videos
        if (remoteUids.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (remoteUids.size <= 1) 1 else 2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp)
            ) {
                items(remoteUids) { uid ->
                    Box(modifier = Modifier.fillMaxWidth().aspectRatio(if (remoteUids.size <= 1) 0.6f else 1f)) {
                        AndroidView(
                            factory = { ctx ->
                                SurfaceView(ctx).apply {
                                    rtcEngine?.setupRemoteVideo(VideoCanvas(this, VideoCanvas.RENDER_MODE_HIDDEN, uid))
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(8.dp).align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = "Learner $uid",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Waiting for peers to join...", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }

        // Call Title Tag
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Text(
                text = callTitle,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 14.sp
            )
        }

        // Local Preview (Picture-in-Picture)
        Card(
            modifier = Modifier.size(120.dp, 180.dp).align(Alignment.TopEnd).padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
                if (!isVideoOff && hasPermissions) {
                    AndroidView(
                        factory = { ctx ->
                            SurfaceView(ctx).apply {
                                rtcEngine?.setupLocalVideo(VideoCanvas(this, VideoCanvas.RENDER_MODE_HIDDEN, 0))
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val imageUrl = if (userData?.imageUri?.isNotEmpty() == true) userData?.imageUri ?: "" else "https://cdn-icons-png.flaticon.com/512/3135/3135715.png"
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                    Icon(Icons.Default.VideocamOff, null, modifier = Modifier.align(Alignment.Center), tint = Color.White)
                }
            }
        }

        // Bottom Controls
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).fillMaxWidth(),
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
                    modifier = Modifier.size(48.dp).background(if (isMuted) Color.Red else Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(if (isMuted) Icons.Default.MicOff else Icons.Default.Mic, null, tint = Color.White)
                }

                IconButton(
                    onClick = { 
                        isVideoOff = !isVideoOff
                        rtcEngine?.muteLocalVideoStream(isVideoOff)
                    },
                    modifier = Modifier.size(48.dp).background(if (isVideoOff) Color.Red else Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam, null, tint = Color.White)
                }

                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(56.dp).background(Color.Red, CircleShape)
                ) {
                    Icon(Icons.Default.CallEnd, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}
