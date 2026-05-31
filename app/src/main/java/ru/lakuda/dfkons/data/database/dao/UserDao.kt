package ru.lakuda.dfkons.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.lakuda.dfkons.data.database.entities.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("""
        SELECT * FROM users 
        WHERE (email = :loginOrEmail OR username = :loginOrEmail)
        AND passwordHash = :passwordHash
        LIMIT 1
    """)
    suspend fun login(
        loginOrEmail: String,
        passwordHash: String
    ): UserEntity?

    @Query("""
        SELECT * FROM users
        WHERE email = :email
        OR username = :username
        LIMIT 1
    """)
    suspend fun checkUserExists(
        email: String,
        username: String
    ): UserEntity?
}