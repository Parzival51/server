package example.com.product

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


@Serializable
data class Product(
    val name: String,
    val price: Double,
    val category: String,
    val baseCategory: String, // Yeni alan
    val imageUrl: String,
    val averageRating: Double = 0.0,
    val averageTasteFlavor: Double = 0.0, // Tat ve Lezzet ortalaması (Gıda ve İçecekler)
    val averagePricePerformance: Double = 0.0, // Fiyat Performans ortalaması (Birden fazla kategori)
    val averageNutritionalValue: Double = 0.0, // Besin Değeri ortalaması (Gıda ve İçecekler)
    val averagePortionSize: Double = 0.0, // Doyuruculuk ortalaması (Gıda ve İçecekler)
    val averageEffectiveness: Double = 0.0, // Etkililik ortalaması (Kişisel Bakım ve Kozmetik, Sağlık ve Spor)
    val averageScent: Double = 0.0, // Koku ortalaması (Kişisel Bakım ve Kozmetik)
    val averageSkinCompatibility: Double = 0.0, // Cilt Uyumu ortalaması (Kişisel Bakım ve Kozmetik)
    val averageDurability: Double = 0.0, // Dayanıklılık ortalaması (Birden fazla kategori)
    val averageDesign: Double = 0.0, // Tasarım ortalaması (Birden fazla kategori)
    val averageFunctionality: Double = 0.0, // Fonksiyonellik ortalaması (Ev ve Yaşam)
    val averagePerformance: Double = 0.0, // Performans ortalaması (Elektronik ve Teknoloji)
    val averageQuality: Double = 0.0, // Kalite ortalaması (Giyim ve Moda)
    val averageComfort: Double = 0.0, // Konfor ortalaması (Giyim ve Moda)
    val averageEaseOfUse: Double = 0.0, // Kullanım Kolaylığı ortalaması (Sağlık ve Spor)
    val id: String = ObjectId().toString()
)
