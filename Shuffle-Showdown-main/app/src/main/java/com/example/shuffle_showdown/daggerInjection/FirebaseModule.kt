package com.example.shuffle_showdown.daggerInjection

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
 We use Hilt for Dagger Injections
 I.e. So we can have a single instance (singleton) of the database
 throughout the application without the need of excess code

 Please note, we need to add '@AndroidEntryPoint' to every activity or fragment!!!
 Otherwise, app will crash
 Also add '@HiltViewModel' and the @Inject constructor to every ViewModel
 */

@InstallIn(SingletonComponent::class)
@Module
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFireStoreInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthenticationInstance(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

}