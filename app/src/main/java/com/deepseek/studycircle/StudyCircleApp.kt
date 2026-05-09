package com.deepseek.studycircle

import android.app.Application
import io.agora.chat.ChatClient
import io.agora.chat.ChatOptions

class StudyCircleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initAgoraChat()
    }

    private fun initAgoraChat() {
        val options = ChatOptions()
        // Format: AppKey = OrgName#AppName. For Agora Chat, it's often your App ID if not using AppKey.
        // However, Agora Chat usually requires an AppKey from the console.
        // If you don't have one, we'll try to use the App ID as a placeholder or check your console.
        options.appKey = "6280dfc1f4b24b44ba9631652c98069f" // Replace with your actual AppKey from Agora Console
        options.autoLogin = true

        ChatClient.getInstance().init(this, options)
        ChatClient.getInstance().setDebugMode(BuildConfig.DEBUG)
    }
}
