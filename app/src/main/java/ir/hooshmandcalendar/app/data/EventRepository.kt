package ir.hooshmandcalendar.app.data

import android.content.Context
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.data.db.EventDao
import ir.hooshmandcalendar.app.data.db.EventEntity
import kotlinx.coroutines.flow.Flow

/** لایه دسترسی به رویدادها */
class EventRepository(private val dao: EventDao) {

    fun observeAll(): Flow<List<EventEntity>> = dao.observeAll()

    fun search(query: String): Flow<List<EventEntity>> = dao.search(query)

    suspend fun getById(id: Long): EventEntity? = dao.getById(id)

    suspend fun upsert(event: EventEntity): Long = dao.insert(event)

    suspend fun delete(event: EventEntity) = dao.delete(event)

    suspend fun getAll(): List<EventEntity> = dao.getAll()

    /** رویدادهای یک روز با احتساب تکرارها */
    suspend fun eventsForDay(date: JalaliDate): List<EventEntity> {
        val candidates = dao.getForDayCandidates(date.epochDay, date.year)
        return candidates.filter { OccurrenceEngine.occursOn(it, date) }
    }

    /** رویدادهای دارای یادآور برای زمان‌بندی */
    suspend fun scheduledEvents(fromEpochDay: Long): List<EventEntity> =
        dao.getScheduled(fromEpochDay)

    companion object {
        @Volatile private var instance: EventRepository? = null

        fun get(context: Context): EventRepository =
            instance ?: synchronized(this) {
                instance ?: EventRepository(
                    ir.hooshmandcalendar.app.data.db.AppDatabase.get(context).eventDao()
                ).also { instance = it }
            }
    }
}
