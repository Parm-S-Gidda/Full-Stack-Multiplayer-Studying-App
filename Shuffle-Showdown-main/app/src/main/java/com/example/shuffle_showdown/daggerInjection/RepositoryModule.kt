package com.example.shuffle_showdown.daggerInjection

import com.example.shuffle_showdown.account.AccountRepository
import com.example.shuffle_showdown.cueCard.CueCardRepository
import com.example.shuffle_showdown.customAccount.CustomAccountRepository
import com.example.shuffle_showdown.history.HistoryRepository
import com.example.shuffle_showdown.ui.decks.DecksRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
 Please refer to FirebaseModule.kt comment
 This module exists for it to work with @Inject constructor,
 found in CueCardViewModel initialization, while also having
 a single instance of the repository across the application
 */

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCueCardRepository(database: FirebaseFirestore): CueCardRepository {
        return CueCardRepository(database)
    }

    @Provides
    @Singleton
    fun provideAccountRepository(auth: FirebaseAuth): AccountRepository {
        return AccountRepository(auth)
    }

    @Provides
    @Singleton
    fun provideCustomAccountRepository(database: FirebaseFirestore): CustomAccountRepository {
        return CustomAccountRepository(database)
    }

    @Provides
    @Singleton
    fun provideDeckRepository(database: FirebaseFirestore): DecksRepository {
        return DecksRepository(database)
    }

    @Provides
    @Singleton
    fun provideHistoryRepository(database: FirebaseFirestore): HistoryRepository {
        return HistoryRepository(database)
    }


}