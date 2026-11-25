package dz.cabinet.app

import android.app.Application
import dz.cabinet.data.AuthRepository
import dz.cabinet.data.dataModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CabinetApplication : Application() {
    private val authRepository: AuthRepository by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CabinetApplication)
            modules(dataModule)
        }
        authRepository.configureBootstrapAdmin(BuildConfig.ADMIN_BOOTSTRAP_PASSWORD)
    }
}
