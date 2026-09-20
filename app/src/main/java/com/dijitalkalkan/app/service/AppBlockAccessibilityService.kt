package com.dijitalkalkan.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dijitalkalkan.app.data.PrefsManager
import com.dijitalkalkan.app.data.UsageStatsHelper
import com.dijitalkalkan.app.ui.screens.BlockActivity
import com.dijitalkalkan.app.ui.screens.ProtectedActionScreen

/**
 * Ön planda hangi uygulamanın açık olduğunu izleyen Erişilebilirlik Servisi.
 * İki görevi var:
 *  1) Günlük süre limiti dolan uygulamaları tespit edip BlockActivity göstermek.
 *  2) Çocuğun Ayarlar'da bu uygulamayı kaldırma / izinlerini kapatma ekranına
 *     girdiğini tespit edip ebeveyn şifresi isteyen ProtectedActionScreen'i
 *     göstermek.
 *
 * ÖNEMLİ GERÇEK KISITLAMA: Bu servisi kullanıcı (ebeveyn, çocuğun cihazında)
 * Ayarlar > Erişilebilirlik ekranından ELLE açmalıdır; kod ile otomatik
 * etkinleştirilemez ve kullanıcı bu izni istediği an kapatabilir.
 */
class AppBlockAccessibilityService : AccessibilityService() {

    private lateinit var prefs: PrefsManager
    private val handler = Handler(Looper.getMainLooper())
    private var currentPackage: String? = null
    private var lastGuardTriggerMillis = 0L

    private val settingsRelatedPackages = setOf(
        "com.android.settings",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller"
    )

    private val periodicCheck = object : Runnable {
        override fun run() {
            currentPackage?.let { checkAndBlockIfNeeded(it) }
            handler.postDelayed(this, 30_000L)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PrefsManager(applicationContext)
        handler.post(periodicCheck)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return // kendi uygulamamızı yok say

        if (packageName in settingsRelatedPackages) {
            checkSettingsGuard()
            return
        }

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        currentPackage = packageName
        checkAndBlockIfNeeded(packageName)
    }

    private fun checkAndBlockIfNeeded(packageName: String) {
        val limit = prefs.getEffectiveLimit(packageName)
        if (limit <= 0) return // bu uygulama için limit tanımlanmamış: sınırsız

        val usedMinutes = UsageStatsHelper.getTodayUsage(applicationContext)
            .firstOrNull { it.packageName == packageName }?.minutesToday ?: 0L

        if (usedMinutes >= limit) {
            val intent = Intent(applicationContext, BlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(BlockActivity.EXTRA_PACKAGE_NAME, packageName)
            }
            startActivity(intent)
        }
    }

    /** Ayarlar ekranında bizim uygulama adımız (veya "Erişilebilirlik"/"Cihaz yöneticisi") geçiyorsa koruma ekranını göster. */
    private fun checkSettingsGuard() {
        val now = System.currentTimeMillis()
        if (now - lastGuardTriggerMillis < 3000L) return // aynı ekranda tekrar tekrar tetiklenmesin

        val root = rootInActiveWindow ?: return
        val texts = mutableListOf<String>()
        collectText(root, texts, depth = 0)

        val appNameHit = texts.any { it.contains("Dijital Koruyucu", ignoreCase = true) }
        if (!appNameHit) return

        lastGuardTriggerMillis = now
        val intent = Intent(applicationContext, ProtectedActionScreen::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun collectText(node: AccessibilityNodeInfo?, out: MutableList<String>, depth: Int) {
        if (node == null || depth > 6 || out.size > 200) return
        node.text?.let { out.add(it.toString()) }
        node.contentDescription?.let { out.add(it.toString()) }
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), out, depth + 1)
        }
    }

    override fun onInterrupt() { /* gerekli değil */ }
}
