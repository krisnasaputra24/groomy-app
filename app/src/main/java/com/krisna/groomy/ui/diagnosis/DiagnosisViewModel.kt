package com.krisna.groomy.ui.diagnosis

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krisna.groomy.data.ml.KeluhanClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiagnosisViewModel(
    private val classifier: KeluhanClassifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosisUiState())
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

    fun onTextInputChange(input: String) {
        _uiState.update { it.copy(textInput = input) }
    }

    fun analyzeComplaint() {
        val input = _uiState.value.textInput
        if (input.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isResultVisible = false) }
            
            val result = classifier.classify(input)
            Log.d("DiagnosisViewModel", "Classifier Result: Label=${result.label}, Confidence=${result.confidence}, Raw=${result.rawScore}")
            
            if (result.label.startsWith("Error")) {
                _uiState.update { 
                    it.copy(
                        prediction = "Kesalahan Sistem",
                        confidence = 0f,
                        recommendation = result.label,
                        isLoading = false,
                        isResultVisible = true
                    )
                }
                return@launch
            }

            val (friendlyLabel, recommendation) = getMapping(result.label)
            
            _uiState.update {
                it.copy(
                    prediction = friendlyLabel,
                    confidence = result.confidence * 100,
                    recommendation = recommendation,
                    isLoading = false,
                    isResultVisible = true
                )
            }
        }
    }

    private fun getMapping(label: String): Pair<String, String> {
        return when (label) {
            "anti_kutu" -> "Masalah Kutu" to "Gunakan sampo anti-kutu dan sisir serit. Segera konsultasikan jika kulit mulai luka atau merah parah."
            "anti_tungau" -> "Infeksi Tungau (Mites)" to "Bersihkan telinga dan gunakan obat tetes khusus tungau. Cuci tempat tidur hewan dengan air panas."
            "batuk" -> "Batuk / Masalah Pernapasan" to "Jauhkan dari debu dan asap. Jika batuk disertai lemas, segera bawa ke dokter hewan."
            "bau_mulut" -> "Masalah Gigi & Mulut" to "Lakukan pembersihan karang gigi (scaling) dan rutin sikat gigi dengan pasta gigi khusus hewan."
            "bulu_kusut" -> "Bulu Menggumpal (Matting)" to "Gunakan conditioner dan sisir perlahan. Jika terlalu parah, sebaiknya dilakukan cukur medis."
            "bulu_rontok" -> "Kerontokan Bulu" to "Berikan asupan Omega-3 dan vitamin bulu. Cek apakah ada jamur atau alergi makanan."
            "demam" -> "Suhu Tubuh Tinggi" to "Kompres lipatan kaki dengan air biasa. Segera ke dokter jika suhu melebihi 39.5 derajat Celcius."
            "diare" -> "Gangguan Pencernaan (Diare)" to "Berikan banyak minum dan makanan lembut (wet food). Puasakan dari snack berminyak sementara waktu."
            "grooming_biasa" -> "Perlu Grooming Rutin" to "Hewan Anda terlihat sehat, namun tetap butuh mandi rutin untuk menjaga kebersihan kulit dan bulu."
            "grooming_premium" -> "Butuh Perawatan Ekstra" to "Sangat disarankan mengambil paket Premium untuk relaksasi dan pembersihan menyeluruh."
            "infeksi_telinga" -> "Infeksi Telinga" to "Jangan bersihkan terlalu dalam. Gunakan cairan pembersih telinga medis dan amati adanya bau menyengat."
            "jamur_kulit" -> "Infeksi Jamur (Ringworm)" to "Gunakan sampo antijamur ketoconazole. Isolasi dari hewan lain karena jamur bisa menular."
            "konsultasi_dokter" -> "Butuh Tindakan Medis" to "Gejala menunjukkan perlunya pemeriksaan fisik langsung oleh dokter hewan profesional."
            "kulit_kering" -> "Kulit Kering / Bersisik" to "Hindari mandi terlalu sering. Gunakan sampo berbahan oatmeal atau aloe vera."
            "kulit_merah" -> "Iritasi / Alergi Kulit" to "Cek adanya kutu atau perubahan makanan baru. Gunakan spray penenang kulit khusus hewan."
            "luka_ringan" -> "Luka Tergores" to "Bersihkan dengan antiseptik non-alkohol. Pastikan hewan tidak menjilati luka tersebut."
            "mandi_hewan" -> "Jadwal Mandi" to "Sudah waktunya hewan Anda dimandikan agar terhindar dari kuman dan bau tidak sedap."
            "mata_belekan" -> "Masalah Mata" to "Bersihkan kotoran mata dengan kapas air hangat. Jika mata merah, mungkin terjadi peradangan."
            "muntah" -> "Muntah" to "Amati frekuensi muntah. Berikan air minum sedikit demi sedikit. Jika muntah >3 kali sehari, segera ke dokter."
            "perawatan_anak_hewan" -> "Perawatan Puppy/Kitten" to "Gunakan produk khusus bayi hewan. Pastikan suhu air mandi hangat kuku agar tidak kedinginan."
            "perawatan_gigi" -> "Kebersihan Gigi" to "Gunakan sikat gigi jari. Berikan snack 'dental chew' untuk membantu mengurangi plak."
            "perawatan_kulit" -> "Maintenance Kulit" to "Lakukan pemijatan saat mandi untuk melancarkan sirkulasi darah di bawah kulit."
            "perawatan_lansia" -> "Hewan Senior" to "Hewan tua butuh penanganan lebih lembut. Gunakan alas anti-slip saat mandi untuk menghindari cidera."
            "perawatan_mata" -> "Pembersihan Area Mata" to "Potong bulu di sekitar mata agar tidak menusuk bola mata dan menyebabkan iritasi."
            "perawatan_telinga" -> "Pembersihan Telinga" to "Cukup bersihkan bagian daun telinga saja. Gunakan kapas bersih untuk setiap telinga."
            "potong_kuku" -> "Pemotongan Kuku" to "Potong sedikit saja agar tidak mengenai pembuluh darah (quick). Gunakan alat potong khusus."
            "rekomendasi_produk" -> "Saran Produk" to "Gunakan produk perawatan organik untuk hasil terbaik dan minim risiko alergi."
            "sterilisasi" -> "Sterilisasi (Kebiri)" to "Tindakan ini baik untuk kesehatan jangka panjang dan mengontrol populasi serta perilaku agresif."
            "tidak_mau_makan" -> "Nafsu Makan Menurun" to "Coba hangatkan makanan agar aromanya lebih kuat. Cek adanya sariawan atau masalah gigi."
            "vaksinasi" -> "Jadwal Vaksin" to "Lengkapi vaksin tahunan agar hewan kebal dari virus mematikan seperti Parvo atau Distemper."
            else -> "Masalah Tidak Terindentifikasi" to "Kami menyarankan Anda melakukan pemeriksaan umum ke Groomer atau Dokter Hewan terdekat."
        }
    }
}
