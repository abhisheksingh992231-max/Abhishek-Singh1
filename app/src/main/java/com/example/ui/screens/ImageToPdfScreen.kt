package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PdfViewModel

@Composable
fun ImageToPdfScreen(
    viewModel: PdfViewModel
) {
    val images = remember { mutableStateListOf<Bitmap>() }
    var customOutputName by remember { mutableStateOf("") }

    // Helper to generate sample photo bitmaps for scanner demo
    fun addSamplePhoto(type: String) {
        val width = 600
        val height = 800
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        when (type) {
            "receipt" -> {
                canvas.drawColor(Color.rgb(250, 250, 245))
                val paint = Paint().apply { color = Color.BLACK; textSize = 28f; isFakeBoldText = true }
                canvas.drawText("EXPRESS MARKET RECEIPT", 60f, 100f, paint)
                paint.textSize = 20f
                paint.isFakeBoldText = false
                canvas.drawText("Item 1: Organic Milk .......... $4.99", 60f, 180f, paint)
                canvas.drawText("Item 2: Fresh Bread ........... $3.49", 60f, 220f, paint)
                canvas.drawText("Item 3: Apples (1kg) .......... $5.99", 60f, 260f, paint)
                paint.isFakeBoldText = true
                canvas.drawText("TOTAL PAID: $14.47", 60f, 340f, paint)
            }
            "id_card" -> {
                canvas.drawColor(Color.rgb(240, 244, 255))
                val paint = Paint().apply { color = Color.rgb(26, 35, 126); textSize = 32f; isFakeBoldText = true }
                canvas.drawText("NATIONAL IDENTITY CARD", 50f, 100f, paint)
                paint.color = Color.DKGRAY
                paint.textSize = 22f
                paint.isFakeBoldText = false
                canvas.drawText("Name: Johnathan Doe", 50f, 180f, paint)
                canvas.drawText("ID No: 984-204-1029", 50f, 220f, paint)
                canvas.drawText("DOB: 15/08/1992", 50f, 260f, paint)
            }
            else -> {
                canvas.drawColor(Color.rgb(224, 242, 241))
                val paint = Paint().apply { color = Color.rgb(0, 121, 107); textSize = 36f; isFakeBoldText = true }
                canvas.drawText("SCANNED PHOTO PAGE", 60f, 120f, paint)
            }
        }
        images.add(bmp)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Image to PDF Converter",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Convert camera photos, receipts, or document scans into a multi-page PDF.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Add Sample Photos Row
        Text(
            text = "Add Photos / Scans",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { addSamplePhoto("receipt") },
                modifier = Modifier.weight(1f).testTag("add_receipt_photo_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Scan Receipt", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = { addSamplePhoto("id_card") },
                modifier = Modifier.weight(1f).testTag("add_id_card_photo_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Scan ID Card", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Images List
        Text(
            text = "Selected Pages (${images.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (images.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = "Photos",
                        tint = androidx.compose.ui.graphics.Color(0xFF8E24AA),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No images added yet",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Tap the quick scan buttons above to add photo pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().height(220.dp)
            ) {
                itemsIndexed(images) { index, bmp ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .height(210.dp),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Page ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Top Page Badge
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = "Page ${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }

                            // Remove Button
                            IconButton(
                                onClick = { images.removeAt(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Remove",
                                    tint = androidx.compose.ui.graphics.Color.Red
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = customOutputName,
                onValueChange = { customOutputName = it },
                placeholder = { Text("Scanned_Photos_Doc.pdf") },
                modifier = Modifier.fillMaxWidth().testTag("image_to_pdf_output_name_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF8E24AA),
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.createPdfFromDrawnImages(images, customOutputName)
                    images.clear()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("convert_images_to_pdf_btn"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF8E24AA))
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate PDF (${images.size} Pages)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
