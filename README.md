# afet-yardim-be

Bu repo afet-yardım projesinin backend spring boot uygulamasından oluşuyor. Uygulamanın amacı şu anda whatsapp,
instagram, google drive vb. yollardan organize edilmeye çalışılan ankara, istanbul, izmir vb. illerimizdeki deprem
yardım
merkezlerini tek bir yerde toplamak ve insanların kendilerine en yakın merkezleri görmelerini sağlamak.

Bu merkezlerde bazen malzeme, insan fazlası vb. olabilirken başka merkezlerde eksik olabiliyor. Bir websitesi üstünden
bu merkezlerin şu anki durumlarını da insanlara sunmayı umuyoruz ki yardım etmek isteyen insanlar doğru merkezlere
yönlenebilsin.

https://www.afetyardimalanlari.org/

# Uygulamayı çalıştırmak

Uygulama java, maven ve spring boot stack'iyle yazılıyor. Veri tabanı şifresini repoya ekleyemediğim için geliştirme
yaparken lokal bir postgresql instance'ı kaldırıp application.properties dosyasındaki spring.datasource propertylerinin
lokal instance ile değiştirilmesi gerekiyor.



