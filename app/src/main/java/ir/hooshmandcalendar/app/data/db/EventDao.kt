package ir.hooshmandcalendar.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY dayEpochDay ASC, startMinutes ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events")
    suspend fun getAll(): List<EventEntity>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query(
        "SELECT * FROM events WHERE dayEpochDay = :epochDay " +
            "OR (repeatType != 0 AND jYear <= :jy) ORDER BY startMinutes ASC"
    )
    suspend fun getForDayCandidates(epochDay: Long, jy: Int): List<EventEntity>

    @Query(
        "SELECT * FROM events WHERE title LIKE '%' || :query || '%' " +
            "OR description LIKE '%' || :query || '%' " +
            "ORDER BY dayEpochDay ASC, startMinutes ASC"
    )
    fun search(query: String): Flow<List<EventEntity>>

    @Query(
        "SELECT * FROM events WHERE reminderOffsetMinutes IS NOT NULL " +
            "AND (repeatType != 0 OR dayEpochDay >= :fromEpochDay) " +
            "ORDER BY dayEpochDay ASC"
    )
    suspend fun getScheduled(fromEpochDay: Long): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM events")
    suspend fun count(): Int
}
