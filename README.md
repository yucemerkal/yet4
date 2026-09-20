# DijitalKalkan — Tek Uygulama, İki Giriş (Ebeveyn / Çocuk)

Bu sürüm, tamamen tek bir cihazda çalışan gerçek bir ebeveyn kontrol sistemidir.
Hiçbir sunucu, Firebase veya internet bağlantısı gerekmez.

## Nasıl çalışır?

- **Ana ekran:** "Ebeveyn Girişi" ve "Çocuk Girişi" olmak üzere iki buton.
- **Ebeveyn Girişi:** İlk seferde bir **P-code** (4-6 haneli şifre) belirlersin.
  Sonraki girişlerde bu kod istenir. İçeride:
  - Cihazdaki tüm uygulamalar listelenir, her biri için günlük dakika limiti
    belirleyebilirsin (0 = sınırsız).
  - **S-code** üretebilirsin: istediğin sayıda, istediğin dakika değerinde
    tek kullanımlık kodlar üretilir. Bu kodları çocuğa telefonla/mesajla
    söyleyerek ona geçici ek süre verebilirsin.
  - Gerekli 3 izni (Kullanım Erişimi, Erişilebilirlik Servisi, Cihaz
    Yöneticisi) buradan açabilirsin.
- **Çocuk Girişi:** Bugünkü kullanım süresini görür, S-code girip ek süre
  kazanabilir.
- **Otomatik engelleme:** Bir uygulamanın günlük limiti dolduğunda, o
  uygulamayı her açtığında tam ekran bir "süre doldu" ekranı çıkar
  (Erişilebilirlik Servisi ile gerçek zamanlı tespit edilir).
- **Silinme koruması:** Cihaz Yöneticisi izni açıkken, uygulamayı normal
  yoldan kaldırmak zorlaşır (önce bu izni Ayarlar'dan kapatmak gerekir).

## Gerçekçi sınırlamalar (dürüstçe belirtiyorum)

- Erişilebilirlik Servisi ve Cihaz Yöneticisi izinleri **elle** açılmalıdır,
  koddan otomatik açılamaz — Android bunu güvenlik gereği izin vermiyor.
- Cihaz Yöneticisi, %100 "silinemez" garantisi vermez; sadece ek bir engel
  katmanıdır. Tam garantili koruma (Device Owner / MDM) yalnızca cihaz
  fabrika ayarlarına dönüp özel şekilde kurulursa mümkündür.
- Bu sürüm ebeveyn ve çocuğun **aynı fiziksel cihazı** kullandığı (ebeveyn
  ara sıra P-code ile girip ayar yaptığı) senaryo için tasarlanmıştır. Farklı
  iki cihaz arası gerçek zamanlı uzaktan kontrol için ayrı bir mimari
  (Firebase gibi bir sunucu) gerekir.

## GitHub'a nasıl yüklenir?

Bu sefer çok fazla dosya değişti/eklendi/silindi. En kolay yol:

1. GitHub'daki mevcut reponu aç.
2. İçindeki TÜM dosyaları seç ve sil (veya en temizi: yeni bir repo oluştur).
3. Bu zip'in içeriğini (DijitalKalkan klasörünün İÇİNDEKİ her şeyi, klasörün
   kendisini değil) sürükleyip bırak, commit et.
4. Actions sekmesinde yeşil ✅ olunca APK'yı indir, telefona kur.
5. Kurulumdan sonra "Ebeveyn Girişi"ne gir, P-code belirle, 3 izni sırayla aç
   (Kullanım Erişimi → Erişilebilirlik Servisi → Cihaz Yöneticisi),
   sonra uygulama limitlerini ayarla.
