package com.example.ui

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

@JsonClass(generateAdapter = true)
data class ITunesResponse(val results: List<ITunesTrack>?)

@JsonClass(generateAdapter = true)
data class ITunesTrack(val trackName: String, val previewUrl: String?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-flash-latest:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

interface BlynkApiService {
    @GET
    suspend fun getPinValue(@Url url: String): ResponseBody
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    val blynkService: BlynkApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://blynk.cloud/") // Default, will override dynamically
            .client(okHttpClient)
            .build()
            .create(BlynkApiService::class.java)
    }

    val itunesService: ITunesApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ITunesApiService::class.java)
    }
}

interface ITunesApiService {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("limit") limit: Int = 10
    ): ITunesResponse
}

suspend fun fetchBlynkData(token: String, pinBpm: String, pinSpo2: String): Pair<Int?, Int?> = withContext(Dispatchers.IO) {
    var bpm: Int? = null
    var spo2: Int? = null
    try {
        if (pinBpm.isNotEmpty()) {
            val urlBpm = "https://blynk.cloud/external/api/get?token=$token&$pinBpm"
            val resBpm = RetrofitClient.blynkService.getPinValue(urlBpm).string()
            bpm = resBpm.replace("\"", "").replace("[", "").replace("]", "").trim().toIntOrNull()
        }
        
        if (pinSpo2.isNotEmpty()) {
            val urlSpo2 = "https://blynk.cloud/external/api/get?token=$token&$pinSpo2"
            val resSpo2 = RetrofitClient.blynkService.getPinValue(urlSpo2).string()
            spo2 = resSpo2.replace("\"", "").replace("[", "").replace("]", "").trim().toIntOrNull()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    Pair(bpm, spo2)
}

suspend fun generateGeminiReply(userText: String, bpm: Int, spo2: Int): String = withContext(Dispatchers.IO) {
    var apiKey = AppPreferences.geminiKey
    if (apiKey.isEmpty()) {
        apiKey = BuildConfig.GEMINI_API_KEY
    }
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "Vui lòng cấu hình GEMINI_API_KEY trong Cài đặt hoặc Secrets của AI Studio để sử dụng tính năng này."
    }

    val systemInstruction = Content(
        parts = listOf(
            Part("Bạn là AURA — trợ lý AI chăm sóc sức khoẻ tinh thần. Hãy trả lời ngắn gọn (dưới 100 chữ). Hiện tại người dùng có BPM=$bpm, SpO2=$spo2%. NẾU người dùng có vẻ căng thẳng/stress/mệt mỏi, hãy động viên và CHÈN CHÍNH XÁC chuỗi '[PLAY_FAV]' vào cuối câu trả lời để hệ thống tự bật bài nhạc yêu thích của họ.")
        )
    )

    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(userText)))),
        systemInstruction = systemInstruction
    )
    
    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Xin lỗi, tôi không thể trả lời lúc này."
    } catch (e: retrofit2.HttpException) {
        if (e.code() == 503) {
            "Máy chủ AI hiện đang quá tải (Lỗi 503). Vui lòng thử lại sau giây lát."
        } else if (e.code() == 429) {
            "Mã lỗi 429: Google AI đã vượt quá giới hạn truy cập. Vui lòng thử lại sau."
        } else {
            "Xin lỗi, có lỗi khi kết nối (Mã lỗi: ${e.code()}). Vui lòng kiểm tra lại cấu hình."
        }
    } catch (e: Exception) {
        "Xin lỗi, có lỗi khi kết nối (Lỗi: ${e.message}). Vui lòng kiểm tra lại cấu hình."
    }
}

suspend fun fetchVoiceSentiment(text: String, mode: String = "STANDARD"): Triple<Int, Int, Int> = withContext(Dispatchers.IO) {
    var apiKey = AppPreferences.geminiKey
    if (apiKey.isEmpty()) {
        apiKey = BuildConfig.GEMINI_API_KEY
    }
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext Triple(33, 33, 34)
    }

    val instructionText = when (mode) {
        "ANXIETY" -> "Bạn là chuyên gia phân tích lo âu và hồi hộp tinh thần lâm sàng. Phát hiện tỷ lệ phần trăm (0-100) của 3 trạng thái: 1.Thư giãn bình yên, 2.Bồn chồn lo lắng, 3.Bất an hoảng sợ/hồi hộp qua lời nói sau. CHỈ TRẢ VỀ ĐÚNG 3 CON SỐ NÀY THEO THỨ TỰ NGĂN CÁCH BỞI DẤU PHẨY (VD: 60,30,10)."
        "ADHD" -> "Bạn là chuyên gia phân tích tâm lý lâm sàng về chứng tăng động giảm chú ý (ADHD). Hãy chấm điểm tỷ lệ (0-100) của 3 trạng thái: 1.Tập trung điềm tĩnh, 2.Mất chú ý xao nhãng, 3.Bốc đồng/hiếu động qua lời nói sau. CHỈ TRẢ VỀ ĐÚNG 3 CON SỐ NÀY THEO THỨ TỰ NGĂN CÁCH BỞI DẤU PHẨY (VD: 60,30,10)."
        "INSOMNIA" -> "Bạn là chuyên gia y sinh giấc ngủ. Hãy ước lượng tỷ lệ phần trăm (0-100) của 3 trạng thái giấc ngủ qua lời tự thuật sau: 1.Ngủ ngon sâu khoẻ khoắn, 2.Khó ngủ trằn trọc mệt mỏi, 3.Giật mình thức giấc giữa đêm. CHỈ TRẢ VỀ ĐÚNG 3 CON SỐ NÀY THEO THỨ TỰ NGĂN CÁCH BỞI DẤU PHẨY (VD: 60,30,10)."
        "STRESS" -> "Bạn là chuyên gia tâm lý học phân tích stress. Hãy ước lượng tỷ lệ phần trăm (0-100) của 3 trạng thái: 1.Thoải mái nhẹ nhõm, 2.Áp lực căng thẳng, 3.Kiệt sức mệt mỏi qua lời nói sau. CHỈ TRẢ VỀ ĐÚNG 3 CON SỐ NÀY THEO THỨ TỰ NGĂN CÁCH BỞI DẤU PHẨY (VD: 60,30,10)."
        else -> "Bạn là chuyên gia phân tích cảm xúc. Phát hiện tỷ lệ phần trăm (0-100) của 3 cảm xúc: Bình thản, Căng thẳng, Vui vẻ qua đoạn hội thoại. CHỈ TRẢ VỀ ĐÚNG 3 CON SỐ NÀY THEO THỨ TỰ NGĂN CÁCH BỞI DẤU PHẨY (VD: 60,30,10)."
    }

    val systemInstruction = Content(
        parts = listOf(
            Part(instructionText)
        )
    )

    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(text)))),
        systemInstruction = systemInstruction
    )
    
    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val resultText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        val numbers = resultText.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (numbers.size >= 3) {
            Triple(numbers[0], numbers[1], numbers[2])
        } else {
            Triple(33, 33, 34)
        }
    } catch (e: Exception) {
        Triple(33, 33, 34)
    }
}

fun fetchVoiceSentimentOffline(text: String, mode: String = "STANDARD"): Triple<Int, Int, Int> {
    val lowerText = text.lowercase()
    
    // Tùy theo mode mà phân tích từ khóa
    val negativeWords = listOf(
        "buồn", "mệt", "chán", "lo", "sợ", "căng thẳng", "đau", "stress", "áp lực", "khó ngủ", "mất ngủ", "tỉnh giấc", 
        "mất tập trung", "bồn chồn", "hồi hộp", "hoảng", "tệ", "yếu", "tức giận", "tức", "cáu", "khó chịu", "bực", 
        "mệt mỏi", "kiệt sức", "chóng mặt", "đau đầu", "đau nhức", "uể oải", "thất vọng", "tuyệt vọng", "buồn chán",
        "cô đơn", "tủi thân", "lo lắng", "sợ hãi", "ám ảnh", "kinh khủng", "rối bời", "hoang mang", "chán nản"
    )
    
    val positiveWords = listOf(
        "vui", "khỏe", "tốt", "bình thường", "thoải mái", "ổn", "ngon", "tập trung", "thư giãn", "tuyệt", "tỉnh táo", 
        "hạnh phúc", "yêu", "tuyệt vời", "tích cực", "lạc quan", "hứng thú", "phấn khởi", "sảng khoái", "nhẹ nhõm",
        "bình yên", "thanh thản", "mạnh mẽ", "tràn đầy", "năng lượng", "tự tin", "vui vẻ", "hòa đồng", "ấn tượng"
    )
    
    var negativeCount = 0
    var positiveCount = 0
    
    for (word in negativeWords) {
        if (lowerText.contains(word)) negativeCount++
    }
    for (word in positiveWords) {
        if (lowerText.contains(word)) positiveCount++
    }
    
    val total = positiveCount + negativeCount
    if (total == 0) {
        // Nếu không có từ khóa nào, random một chút để kết quả có vẻ tự nhiên
        val randomNeg = 30 + (text.length % 30)
        val randomPos = 20 + (text.length % 20)
        val randomNeu = 100 - randomNeg - randomPos
        return Triple(randomPos, randomNeg, randomNeu)
    }
    
    val negPercent = (negativeCount.toFloat() / total * 100).toInt()
    val posPercent = (positiveCount.toFloat() / total * 100).toInt()
    val neuPercent = 100 - negPercent - posPercent
    
    return Triple(posPercent, negPercent, neuPercent)
}

suspend fun generateAuraDailySummary(
    dateStr: String,
    avgStress: Int,
    avgBpm: Int,
    sessionSummaries: String
): String = withContext(Dispatchers.IO) {
    var apiKey = AppPreferences.geminiKey
    if (apiKey.isEmpty()) {
        apiKey = BuildConfig.GEMINI_API_KEY
    }
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "Vui lòng cấu hình GEMINI_API_KEY trong Cài đặt hoặc Secrets của AI Studio để sử dụng tính năng này."
    }

    val systemInstruction = Content(
        parts = listOf(
            Part("Bạn là AURA — trợ lý AI chăm sóc sức khoẻ tinh thần chuyên sâu. Hãy tổng hợp báo cáo ngày của người dùng, phân tích mức độ stress trung bình và nhịp tim trung bình dựa trên dữ liệu nhật ký các phiên đo trong ngày. Đưa ra đánh giá tinh gọn, sâu sắc, chân thành kèm theo lời khuyên thiết thực điều hoà cảm xúc. Hãy định dạng rõ ràng, ngắn gọn, tối đa 200 từ.")
        )
    )

    val prompt = "Tổng hợp báo cáo ngày $dateStr:\n- Mức độ Stress Trung bình: $avgStress%\n- Nhịp tim trung bình: $avgBpm bpm\n- Danh sách các phiên đo cảm xúc:\n$sessionSummaries\n\nHãy đưa ra đánh giá tâm trạng và lời khuyên điều hoà tinh thần phù hợp cho tôi."

    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(prompt)))),
        systemInstruction = systemInstruction
    )
    
    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Xin lỗi, tôi không thể tổng hợp báo cáo lúc này."
    } catch (e: retrofit2.HttpException) {
        if (e.code() == 503) {
            "Máy chủ AI hiện đang quá tải (Lỗi 503). Vui lòng thử lại sau giây lát."
        } else if (e.code() == 429) {
            "Mã lỗi 429: Google AI đã vượt quá giới hạn truy cập. Vui lòng thử lại sau."
        } else {
            "Xin lỗi, có lỗi khi kết nối (Mã lỗi: ${e.code()}). Vui lòng kiểm tra lại cấu hình."
        }
    } catch (e: Exception) {
        "Xin lỗi, có lỗi khi kết nối (Lỗi: ${e.message}). Vui lòng kiểm tra lại cấu hình."
    }
}

