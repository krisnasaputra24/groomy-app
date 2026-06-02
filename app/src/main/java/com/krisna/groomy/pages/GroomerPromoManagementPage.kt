package com.krisna.groomy.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.krisna.groomy.api.RetrofitClient
import com.krisna.groomy.model.PromoRequest
import com.krisna.groomy.model.PromoResponse
import com.krisna.groomy.model.ServiceResponse
import com.krisna.groomy.utils.PrefManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroomerPromoManagementPage(navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    val scope = rememberCoroutineScope()
    
    val promos = remember { mutableStateListOf<PromoResponse>() }
    val services = remember { mutableStateListOf<ServiceResponse>() }
    var isLoading by remember { mutableStateOf(true) }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPromo by remember { mutableStateOf<PromoResponse?>(null) }

    fun fetchData() {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    isLoading = true
                    // Fetch Promos
                    val promoRes = RetrofitClient.instance.getAllPromos("Bearer $token")
                    if (promoRes.isSuccessful) {
                        promos.clear()
                        promoRes.body()?.let { promos.addAll(it) }
                    }
                    
                    // Fetch Services for dropdown
                    val profileRes = RetrofitClient.instance.getProfile("Bearer $token")
                    val groomerId = profileRes.body()?.groomers?.firstOrNull()?.id
                    if (groomerId != null) {
                        val serviceRes = RetrofitClient.instance.getAllServices(groomerId)
                        if (serviceRes.isSuccessful) {
                            services.clear()
                            serviceRes.body()?.let { services.addAll(it) }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    fun deletePromoItem(id: Int) {
        val token = prefManager.getToken()
        if (token != null) {
            scope.launch {
                try {
                    val res = RetrofitClient.instance.deletePromo("Bearer $token", id)
                    if (res.isSuccessful) {
                        Toast.makeText(context, "Promo dihapus", Toast.LENGTH_SHORT).show()
                        fetchData()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Gagal menghapus promo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) { fetchData() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Promo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF257DEF),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Promo")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (promos.isEmpty()) {
                Text("Belum ada promo aktif", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(promos) { promo ->
                        PromoItemCard(
                            promo = promo,
                            onEdit = { editingPromo = promo },
                            onDelete = { deletePromoItem(promo.id) }
                        )
                    }
                }
            }
        }

        if (showAddDialog || editingPromo != null) {
            PromoDialog(
                promo = editingPromo,
                services = services,
                onDismiss = { 
                    showAddDialog = false
                    editingPromo = null 
                },
                onSave = { request ->
                    val token = prefManager.getToken()
                    if (token != null) {
                        scope.launch {
                            try {
                                val res = if (editingPromo != null) {
                                    RetrofitClient.instance.updatePromo("Bearer $token", editingPromo!!.id, request)
                                } else {
                                    RetrofitClient.instance.createPromo("Bearer $token", request)
                                }
                                
                                if (res.isSuccessful) {
                                    Toast.makeText(context, "Berhasil disimpan", Toast.LENGTH_SHORT).show()
                                    fetchData()
                                    showAddDialog = false
                                    editingPromo = null
                                } else {
                                    Toast.makeText(context, "Gagal: ${res.code()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PromoItemCard(promo: PromoResponse, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF257DEF).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Percent, contentDescription = null, tint = Color(0xFF257DEF))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(promo.code, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(promo.service?.name ?: "Layanan", fontSize = 14.sp, color = Color.Gray)
                Text("Diskon ${promo.discount}%", fontSize = 14.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                Text("Exp: ${promo.expiryDate.take(10)}", fontSize = 12.sp, color = Color.LightGray)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF3B82F6))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoDialog(
    promo: PromoResponse?,
    services: List<ServiceResponse>,
    onDismiss: () -> Unit,
    onSave: (PromoRequest) -> Unit
) {
    var code by remember { mutableStateOf(promo?.code ?: "") }
    var description by remember { mutableStateOf(promo?.description ?: "") }
    var discount by remember { mutableStateOf(promo?.discount?.toString() ?: "") }
    var expiryDate by remember { mutableStateOf(promo?.expiryDate?.take(10) ?: "2026-06-30") }
    var selectedServiceId by remember { mutableStateOf(promo?.serviceId ?: services.firstOrNull()?.id ?: 0) }
    
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (promo != null) "Edit Promo" else "Tambah Promo Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Kode Promo") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Deskripsi") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text("Diskon (%)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("Tanggal Exp (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = services.find { it.id == selectedServiceId }?.name ?: "Pilih Layanan",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Layanan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        services.forEach { service ->
                            DropdownMenuItem(
                                text = { Text(service.name) },
                                onClick = {
                                    selectedServiceId = service.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val discountInt = discount.toIntOrNull() ?: 0
                    onSave(PromoRequest(code, description, discountInt, "${expiryDate}T00:00:00.000Z", selectedServiceId))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF257DEF))
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
