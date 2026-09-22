package com.bahri.lovelypos.ui.screen.summary

import android.R
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bahri.lovelypos.ui.components.common.LovelyEmptyState
import com.bahri.lovelypos.ui.screen.summary.components.CustomDateRangeRow
import com.bahri.lovelypos.ui.screen.summary.components.DatePickerDialogBase
import com.bahri.lovelypos.ui.screen.summary.components.DateRangeSelector
import com.bahri.lovelypos.ui.screen.summary.components.SummaryContent
import com.bahri.lovelypos.ui.theme.LovelyPOSTheme
import com.bahri.lovelypos.ui.viewmodel.SummaryViewModel
import com.bahri.lovelypos.util.PdfFileResult
import com.bahri.lovelypos.util.PdfReportGenerator
import com.bahri.lovelypos.util.UiState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel = koinViewModel()
) {
    val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()
    val dateRangeMode by viewModel.dateRangeMode.collectAsStateWithLifecycle()
    val customStart by viewModel.customStartDate.collectAsStateWithLifecycle()
    val customEnd by viewModel.customEndDate.collectAsStateWithLifecycle()

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var activePdfResult by remember { mutableStateOf<PdfFileResult?>(null) }
    var showExportOptionsSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentReport = (summaryState as? UiState.Success)?.data

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destinationUri ->
        val pdfResult = activePdfResult
        if (destinationUri != null && pdfResult != null) {
            val success = PdfReportGenerator.savePdfToDestination(context, pdfResult.file, destinationUri)
            if (success) {
                Toast.makeText(context, "Laporan tersimpan: ${pdfResult.fileName}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Gagal menyimpan laporan", Toast.LENGTH_SHORT).show()
            }
        }
        showExportOptionsSheet = false
    }

    LovelyPOSTheme { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DateRangeSelector(
                selectedRange = dateRangeMode,
                onRangeSelected = { viewModel.setDateRangeMode(it) }
            )

            if (dateRangeMode == "Custom") {
                CustomDateRangeRow(
                    startDate = customStart,
                    endDate = customEnd,
                    onStartClick = { showStartPicker = true },
                    onEndClick = { showEndPicker = true }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = summaryState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SummaryContent"
                ) { state ->
                    when (state) {
                        is UiState.Loading -> Box(
                            Modifier.fillMaxSize(),
                            Alignment.Center
                        ) { CircularProgressIndicator() }

                        is UiState.Success -> {
                            val report = state.data
                            if (report == null || report.transactionCount == 0) {
                                LovelyEmptyState(
                                    icon = Icons.Default.Info,
                                    message = "Tidak ada data untuk periode ini"
                                )
                            } else {
                                SummaryContent(report = report)
                            }
                        }

                        is UiState.Error -> Box(
                            Modifier.fillMaxSize(),
                            Alignment.Center
                        ) { Text(state.message, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }

            // Pinned Bottom Action for Export PDF
            if (currentReport != null && currentReport.transactionCount > 0) {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = {
                            val pdfResult = PdfReportGenerator.generateSummaryPdfResult(context, currentReport, dateRangeMode)
                            if (pdfResult != null) {
                                activePdfResult = pdfResult
                                showExportOptionsSheet = true
                            } else {
                                Toast.makeText(context, "Gagal membuat PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Export Laporan (PDF)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showExportOptionsSheet && activePdfResult != null) {
        val sheetState = rememberModalBottomSheetState()
        val pdfResult = activePdfResult!!

        ModalBottomSheet(
            onDismissRequest = { showExportOptionsSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Export Laporan PDF",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pdfResult.fileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Option 1: Save directly to Device Storage (Downloads/Documents)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            createDocumentLauncher.launch(pdfResult.fileName)
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu_save),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Simpan ke Perangkat (Downloads)",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Simpan langsung dengan nama ${pdfResult.fileName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // Option 2: Share / Print via Android System Chooser
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showExportOptionsSheet = false
                            PdfReportGenerator.sharePdf(context, pdfResult.uri, pdfResult.fileName)
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Bagikan / Cetak (Share)",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Kirim via WhatsApp, Email, Drive, atau Printer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showStartPicker) DatePickerDialogBase({
        showStartPicker = false
    }) { viewModel.setCustomDateRange(it, customEnd); showStartPicker = false }

    if (showEndPicker) DatePickerDialogBase({
        showEndPicker = false
    }) { viewModel.setCustomDateRange(customStart, it); showEndPicker = false }
}
