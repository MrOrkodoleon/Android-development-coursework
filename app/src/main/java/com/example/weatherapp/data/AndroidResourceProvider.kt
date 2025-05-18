package com.example.weatherapp.data
import android.app.Application
import com.example.weatherapp.data.ResourceProvider
import javax.inject.Inject

class AndroidResourceProvider @Inject constructor(
    private val application: Application
) : ResourceProvider {
    override fun getString(resId: Int): String = application.getString(resId)
    override fun getString(resId: Int, vararg formatArgs: Any): String =
        application.getString(resId, *formatArgs)
}