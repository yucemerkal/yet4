package com.dijitalkalkan.app.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Cihaz Yöneticisi alıcısı. Bu izin etkinleştirildiğinde, kullanıcı
 * (çocuk) uygulamayı normal "Kaldır" akışıyla silemez; önce Ayarlar >
 * Cihaz Yöneticisi Uygulamaları'ndan bu izni elle kapatması gerekir.
 *
 * ÖNEMLİ GERÇEK KISITLAMA: Bu, %100 silinemezlik garantisi VERMEZ — sadece
 * ek bir engel katmanıdır. Tam garantili silinmezlik (Device Owner / MDM)
 * yalnızca cihaz sıfırdan kurulurken kurumsal kayıt ile mümkündür, normal
 * kullanıcı kurulumunda bu şekilde etkinleştirilemez.
 */
class AppDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "DijitalKalkan koruması etkinleştirildi", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "DijitalKalkan koruması kapatıldı", Toast.LENGTH_SHORT).show()
    }
}
