package dz.cabinet.app

import android.app.Application
import dz.cabinet.data.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CabinetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CabinetApplication)
            modules(dataModule)
        }
    }
}
