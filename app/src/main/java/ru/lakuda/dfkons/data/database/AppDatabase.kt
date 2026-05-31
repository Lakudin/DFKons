package ru.lakuda.dfkons.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import ru.lakuda.dfkons.data.database.converters.Converters
import ru.lakuda.dfkons.data.database.dao.TransactionDao
import ru.lakuda.dfkons.data.database.dao.UserDao
import ru.lakuda.dfkons.data.database.entities.TransactionEntity
import ru.lakuda.dfkons.data.database.entities.UserEntity


@Database(
    entities = [UserEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}