package com.example.tonezone.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TokenDao {

    @Update
    suspend fun update(token: Token)

    @Insert
    fun insert(token: Token)

    @Query("delete from token_table")
    suspend fun clear()

    @Query("select * from token_table limit 1")
    fun getToken(): LiveData<Token>

}