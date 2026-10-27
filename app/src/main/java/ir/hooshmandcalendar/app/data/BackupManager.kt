package ir.hooshmandcalendar.app.data

import android.content.Context
import android.net.Uri
import ir.hooshmandcalendar.app.core.JalaliDate
import ir.hooshmandcalendar.app.data.db.EventEntity
import ir.hooshmandcalendar.app.data.db.RepeatType
import org.json.JSONArray
import org.json.JSONObject

/**
 * پشتیبان‌گیری و بازیابی رویدادها به‌صورت فایل JSON.
 * خواندن/نوشتن از طریق Storage Access Framework انجام می‌شود و نیازی به مجوز حافظه ندارد.
 */
object BackupManager {

    private const val FORMAT_VERSION = 1

    fun buildJson(events: List<EventEntity>): String {
        val root = JSONObject()
        root.put("app", "HooshmandCalendar")
        root.put("formatVersion", FORMAT_VERSION)
        val arr = JSONArray()
        for (e in events) {
            val o = JSONObject()
            o.put("title", e.title)
            o.put("description", e.description)
            o.put("jYear", e.jYear)
            o.put("jMonth", e.jMonth)
            o.put("jDay", e.jDay)
            o.put("dayEpochDay", e.dayEpochDay)
            o.put("allDay", e.allDay)
            o.put("startMinutes", e.startMinutes)
            o.put("endMinutes", e.endMinutes ?: -1)
            o.put("colorIndex", e.colorIndex)
            o.put("reminderOffsetMinutes", e.reminderOffsetMinutes ?: -1)
            o.put("repeatType", e.repeatType)
            o.put("soundUri", e.soundUri ?: "")
            o.put("vibrate", e.vibrate)
            o.put("isBirthday", e.isBirthday)
            arr.put(o)
        }
        root.put("events", arr)
        return root.toString(2)
    }

    data class ImportResult(val added: Int, val skipped: Int)

    suspend fun importFromJson(context: Context, uri: Uri, existing: List<EventEntity>): ImportResult {
        val dao = ir.hooshmandcalendar.app.data.db.AppDatabase.get(context).eventDao()
        val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: return ImportResult(0, 0)
        val root = JSONObject(json)
        val arr = root.optJSONArray("events") ?: return ImportResult(0, 0)

        // کلید ساده برای جلوگیری از تکرار
        val existingKeys = existing.map { keyOf(it.title, it.dayEpochDay, it.startMinutes) }.toMutableSet()
        var added = 0
        var skipped = 0
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val titleRaw = o.optString("title", "")
            if (titleRaw.isBlank()) continue
            val title = titleRaw
            val epochDay = o.optLong("dayEpochDay", 0L)
            val startMin = o.optInt("startMinutes", 540)
            val key = keyOf(title, epochDay, startMin)
            if (key in existingKeys) {
                skipped++
                continue
            }
            val endMin = o.optInt("endMinutes", -1)
            val rem = o.optInt("reminderOffsetMinutes", -1)
            val jd = JalaliDate.fromEpochDay(epochDay)
            val e = EventEntity(
                title = title,
                description = o.optString("description", ""),
                jYear = o.optInt("jYear", jd.year),
                jMonth = o.optInt("jMonth", jd.month),
                jDay = o.optInt("jDay", jd.day),
                dayEpochDay = epochDay,
                allDay = o.optBoolean("allDay", false),
                startMinutes = startMin,
                endMinutes = if (endMin >= 0) endMin else null,
                colorIndex = o.optInt("colorIndex", 0),
                reminderOffsetMinutes = if (rem >= 0) rem else null,
                repeatType = o.optInt("repeatType", RepeatType.NONE),
                soundUri = o.optString("soundUri", "").ifBlank { null },
                vibrate = o.optBoolean("vibrate", true),
                isBirthday = o.optBoolean("isBirthday", false)
            )
            dao.insert(e)
            existingKeys.add(key)
            added++
        }
        return ImportResult(added, skipped)
    }

    private fun keyOf(title: String, epochDay: Long, startMin: Int) = "$title|$epochDay|$startMin"
}
