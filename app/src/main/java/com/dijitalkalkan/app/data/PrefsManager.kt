package com.dijitalkalkan.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class SCode(
    val code: String,
    val minutes: Int,
    val used: Boolean
)

/**
 * Tüm uygulama ayarlarını (P-code, uygulama limitleri, C-kod'lar, bonus süre)
 * cihaz üzerinde yerel olarak (SharedPreferences) saklayan sınıf.
 *
 * Bu tasarımda hiçbir sunucu / internet bağlantısı gerekmez: hem Ebeveyn hem
 * Çocuk arayüzü aynı cihazda, aynı yerel veriyi okur/yazar.
 */
class PrefsManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("dijitalkalkan_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LIMITS = "app_limits_json"        // { "pkg": dakikaLimiti }
        private const val KEY_PCODE_HASH = "p_code_hash"
        private const val KEY_SCODES = "s_codes_json"           // [ {code,minutes,used} ]
        private const val KEY_EXTRA_MINUTES = "extra_minutes_today"
        private const val KEY_EXTRA_DATE = "extra_minutes_date"

        private fun todayString(): String =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        private fun sha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    // ---------- Uygulama limitleri ----------

    fun getLimits(): Map<String, Int> {
        val raw = prefs.getString(KEY_LIMITS, null) ?: return emptyMap()
        val json = JSONObject(raw)
        val map = mutableMapOf<String, Int>()
        json.keys().forEach { key -> map[key] = json.getInt(key) }
        return map
    }

    fun setLimit(packageName: String, minutes: Int) {
        val current = getLimits().toMutableMap()
        if (minutes <= 0) current.remove(packageName) else current[packageName] = minutes
        val json = JSONObject()
        current.forEach { (k, v) -> json.put(k, v) }
        prefs.edit().putString(KEY_LIMITS, json.toString()).apply()
    }

    // ---------- P-code (ebeveyn şifresi) ----------

    fun isPCodeSet(): Boolean = prefs.contains(KEY_PCODE_HASH)

    fun setPCode(code: String) {
        prefs.edit().putString(KEY_PCODE_HASH, sha256(code)).apply()
    }

    fun verifyPCode(code: String): Boolean {
        val stored = prefs.getString(KEY_PCODE_HASH, null) ?: return false
        return stored == sha256(code)
    }

    // ---------- C-kod (ek süre kodları) ----------

    fun getSCodes(): List<SCode> {
        val raw = prefs.getString(KEY_SCODES, null) ?: return emptyList()
        val arr = JSONArray(raw)
        val list = mutableListOf<SCode>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(SCode(o.getString("code"), o.getInt("minutes"), o.getBoolean("used")))
        }
        return list
    }

    private fun saveSCodes(codes: List<SCode>) {
        val arr = JSONArray()
        codes.forEach { c ->
            val o = JSONObject()
            o.put("code", c.code)
            o.put("minutes", c.minutes)
            o.put("used", c.used)
            arr.put(o)
        }
        prefs.edit().putString(KEY_SCODES, arr.toString()).apply()
    }

    /** [count] adet yeni C-kod üretir, her biri [minutes] dakika değerinde. Üretilen kodları döner. */
    fun generateSCodes(count: Int, minutes: Int): List<String> {
        val existing = getSCodes().toMutableList()
        val newCodes = mutableListOf<String>()
        repeat(count) {
            var code: String
            do {
                code = (100000 + Random.nextInt(900000)).toString()
            } while (existing.any { it.code == code })
            existing.add(SCode(code, minutes, used = false))
            newCodes.add(code)
        }
        saveSCodes(existing)
        return newCodes
    }

    /** Kod geçerliyse ekstra süreye ekler, kodu kullanılmış işaretler ve true döner. */
    fun redeemSCode(code: String): Boolean {
        val codes = getSCodes().toMutableList()
        val index = codes.indexOfFirst { it.code == code.trim() && !it.used }
        if (index == -1) return false
        val found = codes[index]
        codes[index] = found.copy(used = true)
        saveSCodes(codes)
        addExtraMinutes(found.minutes)
        return true
    }

    fun deleteAllUsedSCodes() {
        val remaining = getSCodes().filter { !it.used }
        saveSCodes(remaining)
    }

    // ---------- Bonus süre (C-kod ile kazanılan, tüm sınırlı uygulamalara uygulanır) ----------

    private fun addExtraMinutes(minutes: Int) {
        rolloverExtraIfNewDay()
        val current = prefs.getInt(KEY_EXTRA_MINUTES, 0)
        prefs.edit()
            .putInt(KEY_EXTRA_MINUTES, current + minutes)
            .putString(KEY_EXTRA_DATE, todayString())
            .apply()
    }

    fun getExtraMinutesToday(): Int {
        rolloverExtraIfNewDay()
        return prefs.getInt(KEY_EXTRA_MINUTES, 0)
    }

    private fun rolloverExtraIfNewDay() {
        val storedDate = prefs.getString(KEY_EXTRA_DATE, null)
        if (storedDate != todayString()) {
            prefs.edit()
                .putInt(KEY_EXTRA_MINUTES, 0)
                .putString(KEY_EXTRA_DATE, todayString())
                .apply()
        }
    }

    /** Bir uygulamanın bugünkü efektif limiti: temel limit + bugünkü bonus süre. 0 = sınırsız. */
    fun getEffectiveLimit(packageName: String): Int {
        val base = getLimits()[packageName] ?: return 0
        return base + getExtraMinutesToday()
    }
}
