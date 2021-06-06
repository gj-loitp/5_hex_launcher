package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.Result

class LauncherViewModel(app: Application): AndroidViewModel(app) {
    val apps: LiveData<Result<List<AppInfo>>> = AppInfoLiveData.get()
}