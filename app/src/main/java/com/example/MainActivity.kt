package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PacketEntity
import com.example.ui.dialogs.AppDetailDialog
import com.example.ui.dialogs.IpDetailDialog
import com.example.ui.dialogs.PacketDetailDialog
import com.example.ui.dialogs.SslCertDialog
import com.example.ui.dialogs.TargetAppSelectorDialog
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.AnalyzeScreen
import com.example.ui.screens.CaptureScreen
import com.example.ui.screens.ConnectionsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PacketAnalyzerScreen
import com.example.ui.screens.PcapLibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.PacketCaptureTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.util.SummaryReportUtils
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    setContent {
      PacketCaptureTheme {
        val authState by viewModel.authState.collectAsStateWithLifecycle()
        val userSession by viewModel.userSession.collectAsStateWithLifecycle()
        val pendingEmail by viewModel.pendingEmail.collectAsStateWithLifecycle()
        val pendingDisplayName by viewModel.pendingDisplayName.collectAsStateWithLifecycle()

        val isCapturing by viewModel.isCapturing.collectAsStateWithLifecycle()
        val stats by viewModel.liveStats.collectAsStateWithLifecycle()
        val protocols by viewModel.protocolDistribution.collectAsStateWithLifecycle()
        val topApps by viewModel.appTrafficSummaries.collectAsStateWithLifecycle()
        val alarms by viewModel.recentAlarms.collectAsStateWithLifecycle()
        val packets by viewModel.filteredPackets.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        val selectedProtocol by viewModel.selectedProtocolFilter.collectAsStateWithLifecycle()
        val pcapFiles by viewModel.allPcapFiles.collectAsStateWithLifecycle(initialValue = emptyList())
        val selectedPacket by viewModel.selectedPacket.collectAsStateWithLifecycle()
        val selectedPcapForHexView by viewModel.selectedPcapForHexView.collectAsStateWithLifecycle()
        val showSslCertDialog by viewModel.showSslCertDialog.collectAsStateWithLifecycle()
        val showTargetAppSelector by viewModel.showTargetAppSelector.collectAsStateWithLifecycle()
        val targetApps by viewModel.targetApps.collectAsStateWithLifecycle()
        val notificationSettings by viewModel.notificationSettings.collectAsStateWithLifecycle(initialValue = com.example.data.model.NotificationSettingEntity())

        // Packet Analysis Stats Flows
        val highestConsumer by viewModel.highestTrafficConsumer.collectAsStateWithLifecycle()
        val detailedApps by viewModel.detailedApplications.collectAsStateWithLifecycle()
        val detailedIps by viewModel.detailedIpAddresses.collectAsStateWithLifecycle()
        val timelineScope by viewModel.selectedTimelineScope.collectAsStateWithLifecycle()
        val timelinePoints by viewModel.timelineDataPoints.collectAsStateWithLifecycle()
        val selectedAppDetails by viewModel.selectedAppDetails.collectAsStateWithLifecycle()
        val selectedIpDetails by viewModel.selectedIpDetails.collectAsStateWithLifecycle()

        // Advanced Packet Capture & Diagnostics Flows
        val activeInterface by viewModel.activeInterface.collectAsStateWithLifecycle()
        val promiscuousMode by viewModel.promiscuousMode.collectAsStateWithLifecycle()
        val captureFilterExpression by viewModel.captureFilterExpression.collectAsStateWithLifecycle()
        val isCaptureFilterValid by viewModel.isCaptureFilterValid.collectAsStateWithLifecycle()
        val fileFormat by viewModel.fileFormat.collectAsStateWithLifecycle()
        val ringBufferSizeMb by viewModel.ringBufferSizeMb.collectAsStateWithLifecycle()
        val snapLength by viewModel.snapLength.collectAsStateWithLifecycle()
        val isPaused by viewModel.isPaused.collectAsStateWithLifecycle()

        val conversations by viewModel.conversations.collectAsStateWithLifecycle()
        val endpoints by viewModel.endpoints.collectAsStateWithLifecycle()
        val packetLengthBuckets by viewModel.packetLengthBuckets.collectAsStateWithLifecycle()
        val ioGraphPoints by viewModel.ioGraphPoints.collectAsStateWithLifecycle()
        val ioGraphInterval by viewModel.ioGraphInterval.collectAsStateWithLifecycle()

        val trafficAlerts by viewModel.trafficAlerts.collectAsStateWithLifecycle()
        val selectedAlertSeverity by viewModel.selectedAlertSeverity.collectAsStateWithLifecycle()
        val displayFilterPresets = viewModel.displayFilterPresets

        val pingResults by viewModel.pingResults.collectAsStateWithLifecycle()
        val isPingRunning by viewModel.isPingRunning.collectAsStateWithLifecycle()
        val tracerouteResults by viewModel.tracerouteResults.collectAsStateWithLifecycle()
        val isTracerouteRunning by viewModel.isTracerouteRunning.collectAsStateWithLifecycle()
        val dnsResults by viewModel.dnsResults.collectAsStateWithLifecycle()
        val isDnsRunning by viewModel.isDnsRunning.collectAsStateWithLifecycle()
        val portScanResults by viewModel.portScanResults.collectAsStateWithLifecycle()
        val isPortScanRunning by viewModel.isPortScanRunning.collectAsStateWithLifecycle()
        val bandwidthResult by viewModel.bandwidthResult.collectAsStateWithLifecycle()
        val packetGenLog by viewModel.packetGenLog.collectAsStateWithLifecycle()
        val isPacketGenRunning by viewModel.isPacketGenRunning.collectAsStateWithLifecycle()

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        var selectedNavTab by remember { mutableIntStateOf(0) }

        if (userSession == null || userSession?.isAuthenticated == false) {
          LoginScreen(
            authState = authState,
            pendingEmail = pendingEmail,
            pendingDisplayName = pendingDisplayName,
            totpSecret = viewModel.getTotpSecret(),
            onStartGoogleAuth = { email, displayName ->
              viewModel.startGoogleAuth(email, displayName)
            },
            onVerifyTotp = { code ->
              viewModel.verifyTotpCode(code)
            },
            onResetToLogin = {
              viewModel.resetToLoginScreen()
            },
            onClearError = {
              viewModel.clearAuthError()
            }
          )
        } else {
          Scaffold(
            topBar = {
              TopAppBar(
                title = {
                  Text(
                    text = when (selectedNavTab) {
                      0 -> "Packet Capture Pro"
                      1 -> "Live Capture Hub"
                      2 -> "Deep Packet Analyzer"
                      3 -> "Traffic Statistics & I/O"
                      4 -> "Network Diagnostics"
                      5 -> "Security Warning Center"
                      6 -> "PCAP File Vault"
                      else -> "Capture Settings"
                    },
                    fontWeight = FontWeight.Bold
                  )
                },
                actions = {
                  if (selectedNavTab == 0 || selectedNavTab == 1 || selectedNavTab == 2) {
                    IconButton(
                      onClick = {
                        val report = viewModel.generateTextSummaryReport()
                        SummaryReportUtils.shareTextReport(
                          context = context,
                          reportContent = report,
                          title = "Packet Capture Pro - Network Summary Report"
                        )
                        scope.launch {
                          snackbarHostState.showSnackbar("Sharing network summary report...")
                        }
                      },
                      modifier = Modifier.testTag("dashboard_share_button")
                    ) {
                      Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Summary Report",
                        tint = MaterialTheme.colorScheme.primary
                      )
                    }
                    IconButton(
                      onClick = { viewModel.toggleCapture() },
                      modifier = Modifier.testTag("toggle_capture_top_bar")
                    ) {
                      Icon(
                        imageVector = if (isCapturing) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isCapturing) "Stop Capture" else "Start Capture",
                        tint = if (isCapturing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                      )
                    }
                    IconButton(
                      onClick = {
                        viewModel.clearAllCapturedData()
                        scope.launch { snackbarHostState.showSnackbar("Live packet buffers cleared") }
                      },
                      modifier = Modifier.testTag("clear_packets_top_bar")
                    ) {
                      Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Packets")
                    }
                  }
                  IconButton(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.testTag("sign_out_button")
                  ) {
                    Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = MaterialTheme.colorScheme.surface,
                  titleContentColor = MaterialTheme.colorScheme.onSurface
                )
              )
            },
            bottomBar = {
              ScrollableTabRow(
                selectedTabIndex = selectedNavTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp
              ) {
                val navItems = listOf(
                  "Dashboard" to Icons.Default.Dashboard,
                  "Capture" to Icons.Default.Radio,
                  "Analyze" to Icons.Default.FormatListNumbered,
                  "Statistics" to Icons.Default.BarChart,
                  "Tools" to Icons.Default.Build,
                  "Alerts" to Icons.Default.NotificationsActive,
                  "Vault" to Icons.Default.FolderZip,
                  "Settings" to Icons.Default.Settings
                )
                navItems.forEachIndexed { index, (title, icon) ->
                  Tab(
                    selected = selectedNavTab == index,
                    onClick = { selectedNavTab = index },
                    modifier = Modifier.testTag("nav_tab_$index"),
                    icon = { Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp)) },
                    text = { Text(title, fontSize = 11.sp, fontWeight = if (selectedNavTab == index) FontWeight.Bold else FontWeight.Normal) }
                  )
                }
              }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
          ) { paddingValues ->
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
            ) {
              when (selectedNavTab) {
                0 -> DashboardScreen(
                  isCapturing = isCapturing,
                  stats = stats,
                  protocols = protocols,
                  topApps = topApps,
                  alarms = alarms,
                  highestConsumer = highestConsumer,
                  detailedApps = detailedApps,
                  detailedIps = detailedIps,
                  timelineScope = timelineScope,
                  timelinePoints = timelinePoints,
                  onToggleCapture = { viewModel.toggleCapture() },
                  onScopeChanged = { viewModel.setTimelineScope(it) },
                  onInspectApp = { viewModel.inspectApp(it) },
                  onInspectIp = { viewModel.inspectIp(it) }
                )
                1 -> CaptureScreen(
                  isCapturing = isCapturing,
                  isPaused = isPaused,
                  stats = stats,
                  activeInterface = activeInterface,
                  promiscuousMode = promiscuousMode,
                  captureFilter = captureFilterExpression,
                  isCaptureFilterValid = isCaptureFilterValid,
                  fileFormat = fileFormat,
                  ringBufferSizeMb = ringBufferSizeMb,
                  snapLength = snapLength,
                  recentPackets = packets,
                  onToggleCapture = { viewModel.toggleCapture() },
                  onPauseResume = { if (isPaused) viewModel.resumeCapture() else viewModel.pauseCapture() },
                  onClearPackets = { viewModel.clearAllCapturedData() },
                  onExportPcap = {
                    viewModel.exportPcap("Capture Snapshot") { saved ->
                      scope.launch { snackbarHostState.showSnackbar("Exported to ${saved.fileName}") }
                    }
                  },
                  onSelectInterface = { viewModel.setCaptureInterface(it) },
                  onTogglePromiscuous = { viewModel.setPromiscuousMode(it) },
                  onChangeFilter = { viewModel.setCaptureFilter(it) },
                  onSelectFileFormat = { viewModel.setFileFormat(it) },
                  onSelectRingBuffer = { viewModel.setRingBufferSize(it) },
                  onSelectSnapLength = { viewModel.setSnapLength(it) },
                  onPacketClick = { viewModel.selectPacket(it) }
                )
                2 -> AnalyzeScreen(
                  packets = packets,
                  searchQuery = searchQuery,
                  selectedProtocol = selectedProtocol,
                  detailedApps = detailedApps,
                  detailedIps = detailedIps,
                  conversations = conversations,
                  endpoints = endpoints,
                  protocols = protocols,
                  displayFilters = displayFilterPresets,
                  onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                  onProtocolSelect = { viewModel.selectProtocolFilter(it) },
                  onPacketClick = { viewModel.selectPacket(it) },
                  onInspectApp = { viewModel.inspectApp(it) },
                  onInspectIp = { viewModel.inspectIp(it) },
                  onFilterSelected = { viewModel.setCaptureFilter(it) }
                )
                3 -> StatisticsScreen(
                  stats = stats,
                  protocols = protocols,
                  lengthBuckets = packetLengthBuckets,
                  ioPoints = ioGraphPoints,
                  selectedInterval = ioGraphInterval,
                  onSelectInterval = { viewModel.setIoGraphInterval(it) }
                )
                4 -> ToolsScreen(
                  pingResults = pingResults,
                  isPingRunning = isPingRunning,
                  tracerouteResults = tracerouteResults,
                  isTracerouteRunning = isTracerouteRunning,
                  dnsResults = dnsResults,
                  isDnsRunning = isDnsRunning,
                  portScanResults = portScanResults,
                  isPortScanRunning = isPortScanRunning,
                  bandwidthResult = bandwidthResult,
                  packetGenLog = packetGenLog,
                  isPacketGenRunning = isPacketGenRunning,
                  onRunPing = { host, count -> viewModel.runPing(host, count) },
                  onRunTraceroute = { host -> viewModel.runTraceroute(host) },
                  onRunDns = { domain -> viewModel.runDnsLookup(domain) },
                  onRunPortScan = { host, start, end -> viewModel.runPortScan(host, start, end) },
                  onRunBandwidthTest = { viewModel.runBandwidthTest() },
                  onRunPacketGen = { target, port, proto, count, payload -> viewModel.runPacketGenerator(target, port, proto, count, payload) }
                )
                5 -> AlertsScreen(
                  alerts = trafficAlerts,
                  selectedSeverity = selectedAlertSeverity,
                  onSelectSeverity = { viewModel.setSelectedAlertSeverity(it) },
                  onAlertClick = { alert ->
                    val app = detailedApps.find { it.appName.equals(alert.entityName, ignoreCase = true) }
                    if (app != null) viewModel.inspectApp(app)
                  }
                )
                6 -> PcapLibraryScreen(
                  pcapFiles = pcapFiles,
                  onExportCurrentCapture = { notes ->
                    viewModel.exportPcap(notes) { savedPcap ->
                      scope.launch { snackbarHostState.showSnackbar("Exported to ${savedPcap.fileName}") }
                    }
                  },
                  onDeletePcap = { id ->
                    viewModel.deletePcapFile(id)
                    scope.launch { snackbarHostState.showSnackbar("PCAP file deleted") }
                  },
                  onSelectPcapForHexView = { pcap ->
                    viewModel.selectPcapForHexView(pcap)
                  }
                )
                7 -> SettingsScreen(
                  userSession = userSession,
                  onSignOut = { viewModel.signOut() },
                  notificationSettings = notificationSettings,
                  onSaveNotificationSettings = { viewModel.saveNotificationSettings(it) },
                  onOpenTargetAppSelector = { viewModel.setShowTargetAppSelector(true) },
                  onOpenSslCertDialog = { viewModel.setShowSslCertDialog(true) }
                )
              }

              // Dialog Modals
              selectedPacket?.let { packet ->
                PacketDetailDialog(
                  packet = packet,
                  onDismiss = { viewModel.selectPacket(null) }
                )
              }

              selectedAppDetails?.let { appDetails ->
                AppDetailDialog(
                  app = appDetails,
                  onDismiss = { viewModel.inspectApp(null) },
                  onSelectIp = { ip ->
                    viewModel.inspectIpByAddress(ip)
                  },
                  onSaveRegulation = { updatedPolicy ->
                    viewModel.saveAppRegulation(updatedPolicy)
                    scope.launch { snackbarHostState.showSnackbar("Quota & Policy saved for ${updatedPolicy.appName}") }
                  }
                )
              }

              selectedIpDetails?.let { ipDetails ->
                IpDetailDialog(
                  ipInfo = ipDetails,
                  onDismiss = { viewModel.inspectIp(null) },
                  onSelectApp = { appName ->
                    viewModel.inspectAppByName(appName)
                  }
                )
              }

              selectedPcapForHexView?.let { pcap ->
                val samplePacket = PacketEntity(
                  sessionId = "pcap_file_${pcap.id}",
                  timeFormatted = pcap.dateFormatted,
                  appName = pcap.fileName,
                  appPackage = "com.example.pcap",
                  sourceIp = "10.0.0.1",
                  sourcePort = 443,
                  destIp = "10.0.0.2",
                  destPort = 53210,
                  host = "pcap.dump.analyzer",
                  protocol = "PCAP",
                  length = pcap.fileSizeBytes.toInt(),
                  info = "PCAP Raw File Dump: ${pcap.packetCount} frames",
                  payloadHex = "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F",
                  payloadAscii = "PCAP_RAW_TRACE_DATA_PACKET_HEADER"
                )
                PacketDetailDialog(
                  packet = samplePacket,
                  onDismiss = { viewModel.selectPcapForHexView(null) }
                )
              }

              if (showTargetAppSelector) {
                TargetAppSelectorDialog(
                  apps = targetApps,
                  onToggleApp = { viewModel.toggleTargetAppSelected(it) },
                  onDismiss = { viewModel.setShowTargetAppSelector(false) }
                )
              }

              if (showSslCertDialog) {
                SslCertDialog(
                  onDismiss = { viewModel.setShowSslCertDialog(false) },
                  onInstallCert = {
                    scope.launch { snackbarHostState.showSnackbar("Root CA Certificate exported to downloads") }
                  }
                )
              }
            }
          }
        }
      }
    }
  }
}
