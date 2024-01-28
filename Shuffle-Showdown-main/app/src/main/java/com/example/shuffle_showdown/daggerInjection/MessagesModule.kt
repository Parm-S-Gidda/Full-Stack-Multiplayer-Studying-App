package com.example.shuffle_showdown.daggerInjection


import com.example.shuffle_showdown.messages.MessagesViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object MessagesModule  {
    @Provides
    @Singleton
    fun provideMessagesViewModel(): MessagesViewModel {
        return MessagesViewModel()
    }

}