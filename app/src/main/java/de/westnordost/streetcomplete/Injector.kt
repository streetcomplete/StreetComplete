package de.westnordost.streetcomplete

object Injector {

    lateinit var applicationComponent: ApplicationComponent
        private set

    fun initializeApplicationComponent(app: StreetCompleteApplication?) {
        applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(app!!)) // not sure why it is necessary to add these all by hand, I must be doing something wrong
            .build()
    }
}
