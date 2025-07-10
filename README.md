# 🏦 bankingApp

Bu proje, temel bir banka uygulaması olarak geliştirilmiştir. Kullanıcıların cüzdan (wallet) bakiyelerini tutar ve para transferi gibi işlemleri destekler. Uygulama PostgreSQL veritabanı kullanarak veriyi kalıcı hale getirir.

## 🧰 Kullanılan Teknolojiler

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Transaction Management
- PostgreSQL
- Lombok
- Maven

## 📦 Katmanlar

- `User` → Sistemdeki kullanıcıları temsil eder.
- `Account` → Her kullanıcının hesabını temsil eder.
- `TransactionService` → Transfer işlemlerini yönetir.

## 🧮 Temel Özellikler

- Kullanıcı oluşturma
- Hesap oluşturma
- Para gönderme
- Transactional yapı ile veri tutarlılığı sağlanır


## 🔁 Transactional Para Transferi

Para gönderme işlemi sırasında:
- Gönderen kullanıcının bakiyesi kontrol edilir.
- Bakiye yeterliyse düşülür, alıcıya eklenir.
- İşlem transactional yapıda çalışır, böylece ya tamamen başarılı olur ya da rollback edilir.

```java
@Transactional
public void transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
    // Bakiye kontrolü, güncellemeler vs.
}
```

## 🧪 Test Senaryoları
- Yetersiz bakiye → Hata alınmalı, rollback edilmeli
- Geçersiz kullanıcı ID'si → Hata alınmalı
- Başarılı transfer → İki hesap güncellenmeli


## 🚀 Geliştirme Notları
- Transaction yönetimi için @Transactional kullanıldı.
- Optimistic Lock ve Pessimistic Lock kullanıldı
- Exception handling ile hata durumları kontrol altına alındı.
- Gelişmiş senaryolar için ileride: audit log, işlem geçmişi, fraud kontrolü eklenebilir.

