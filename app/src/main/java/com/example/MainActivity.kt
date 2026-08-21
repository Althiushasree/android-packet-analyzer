package com.example

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import com.example.data.vpn.VpnPermissionManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PacketEntity
import com.example.ui.dialogs.AppDetailDialog
import com.example.ui.dialogs.DeviceDeepAnalysisDialog
import com.example.ui.dialogs.IpDetailDialog
import com.example.ui.dialogs.PacketDetailDialog
import com.example.ui.dialogs.SslCertDialog
import com.example.ui.dialogs.TargetAppSelectorDialog
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Hub
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
import com.example.ui.screens.GeminiChatScreen
import com.example.ui.screens.HistoryDatabaseScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NetworkIntelligenceScreen
import com.example.ui.screens.NetworkMonitoringScreen
import com.example.ui.screens.PacketAnalyzerScreen
import com.example.ui.screens.PcapLibraryScreen
import com.example.ui.screens.ServerConnectionScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.PacketCaptureTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.util.SummaryReportUtils
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.AutoAwesome

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

        // Protocol, IP, Connection, and Global Filter State Flows
        val globalTimeRange by viewModel.selectedGlobalTimeRange.collectAsStateWithLifecycle()
        val activeCrossFilter by viewModel.activeCrossFilter.collectAsStateWithLifecycle()
        val enhancedProtocols by viewModel.enhancedProtocolAnalysis.collectAsStateWithLifecycle()
        val protocolHierarchy by viewModel.protocolHierarchyRoot.collectAsStateWithLifecycle()
        val enhancedConnections by viewModel.enhancedSocketConnections.collectAsStateWithLifecycle()

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

        // Network Intelligence State Flows
        val realNetworkInfo by viewModel.realNetworkInfo.collectAsStateWithLifecycle()
        val availableInterfaces by viewModel.availableInterfaces.collectAsStateWithLifecycle()
        val networkChangeBanner by viewModel.networkChangeBanner.collectAsStateWithLifecycle()
        val observedDevices by viewModel.observedDevices.collectAsStateWithLifecycle()
        val selectedDeviceForDeepAnalysis by viewModel.selectedDeviceForDeepAnalysis.collectAsStateWithLifecycle()
        val communicationFlows by viewModel.communicationFlows.collectAsStateWithLifecycle()
        val applicationServices by viewModel.applicationServices.collectAsStateWithLifecycle()
        val dnsLogs by viewModel.dnsLogs.collectAsStateWithLifecycle()
        val liveTrafficStats by viewModel.liveTrafficStats.collectAsStateWithLifecycle()
        val networkHealth by viewModel.networkHealth.collectAsStateWithLifecycle()
        val securityAlerts by viewModel.securityAlerts.collectAsStateWithLifecycle()
        val isIntelMonitoringActive by viewModel.isIntelMonitoringActive.collectAsStateWithLifecycle()
        val isDiscoveryScanning by viewModel.isDiscoveryScanning.collectAsStateWithLifecycle()
        val aiAnalystInsight by viewModel.aiAnalystInsight.collectAsStateWithLifecycle()
        val mlModelHealth by viewModel.mlModelHealth.collectAsStateWithLifecycle()
        val mlRecentInferences by viewModel.mlRecentInferences.collectAsStateWithLifecycle()

        val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
        val isChatGenerating by viewModel.isChatGenerating.collectAsStateWithLifecycle()
        val selectedChatModel by viewModel.selectedChatModel.collectAsStateWithLifecycle()

        // Authorized Network Monitoring Scoped State Flows
        val monNetworkScopes by viewModel.monNetworkScopes.collectAsStateWithLifecycle()
        val monSelectedScope by viewModel.monSelectedScope.collectAsStateWithLifecycle()
        val monScopedDevices by viewModel.monScopedDevices.collectAsStateWithLifecycle()
        val monScopedFlows by viewModel.monScopedFlows.collectAsStateWithLifecycle()
        val monScopedServices by viewModel.monScopedServices.collectAsStateWithLifecycle()
        val monScopedAlerts by viewModel.monScopedAlerts.collectAsStateWithLifecycle()

        // Database & Persistence State Flows (36 - 57)
        val historicalSessions by viewModel.historicalSessions.collectAsStateWithLifecycle()
        val historicalDevices by viewModel.historicalDevices.collectAsStateWithLifecycle()
        val retentionSettings by viewModel.retentionSettings.collectAsStateWithLifecycle()
        val isDbConnected by viewModel.isDbConnected.collectAsStateWithLifecycle()
        val dbTotalRecordsCount by viewModel.dbTotalRecordsCount.collectAsStateWithLifecycle()
        val databaseSizeBytes by viewModel.databaseSizeBytes.collectAsStateWithLifecycle()
        val dbLastWriteTimestamp by viewModel.dbLastWriteTimestamp.collectAsStateWithLifecycle()
        val dbActiveSessionId by viewModel.dbActiveSessionId.collectAsStateWithLifecycle()
        val selectedHistoricalSessionDetails by viewModel.selectedHistoricalSessionDetails.collectAsStateWithLifecycle()

        // Server Connection & Sync State Flows
        val serverConfig by viewModel.serverConfig.collectAsStateWithLifecycle()
        val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
        val serverHealth by viewModel.serverHealth.collectAsStateWithLifecycle()
        val lastPingLatencyMs by viewModel.lastPingLatencyMs.collectAsStateWithLifecycle()
        val lastConnectionTimestamp by viewModel.lastConnectionTimestamp.collectAsStateWithLifecycle()
        val serverLastErrorMessage by viewModel.serverLastErrorMessage.collectAsStateWithLifecycle()

        val syncState by viewModel.syncState.collectAsStateWithLifecycle()
        val syncedRecordsCount by viewModel.syncedRecordsCount.collectAsStateWithLifecycle()
        val pendingRecordsCount by viewModel.pendingRecordsCount.collectAsStateWithLifecycle()
        val failedRecordsCount by viewModel.failedRecordsCount.collectAsStateWithLifecycle()
        val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsStateWithLifecycle()
        val lastSyncMessage by viewModel.lastSyncMessage.collectAsStateWithLifecycle()
        val syncLogs by viewModel.syncLogs.collectAsStateWithLifecycle()

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        var selectedNavTab by remember { mutableIntStateOf(0) }

        val vpnPermissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
          if (result.resultCode == Activity.RESULT_OK) {
            viewModel.toggleCapture()
          }
        }

        val handleToggleCapture = {
          if (isCapturing) {
            viewModel.toggleCapture()
          } else {
            VpnPermissionManager.requestVpnPermissionIfNeeded(
              context = context,
              launcher = vpnPermissionLauncher,
              onPermissionGranted = {
                viewModel.toggleCapture()
              }
            )
          }
        }

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
              var timeFilterMenuExpanded by remember { mutableStateOf(false) }

              TopAppBar(
                title = {
                  Text(
                    text = when (selectedNavTab) {
                      0 -> "PACKETIVEX"
                      1 -> "Monitoring"
                      2 -> "Cyber AI"
                      3 -> "Intelligence"
                      4 -> "Connections"
                      5 -> "History"
                      6 -> "Server Sync"
                      7 -> "Live Capture"
                      8 -> "Analyzer"
                      9 -> "Statistics"
                      10 -> "Diagnostics"
                      11 -> "Alerts"
                      12 -> "PCAP Vault"
                      else -> "Settings"
                    },
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.titleMedium
                  )
                },
                actions = {
                  // Global Time Filter Chip
                  Box {
                    androidx.compose.material3.FilterChip(
                      selected = true,
                      onClick = { timeFilterMenuExpanded = true },
                      label = {
                        Text(
                          text = globalTimeRange.shortLabel,
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold
                        )
                      },
                      shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                      modifier = Modifier.padding(end = 4.dp)
                    )
                    androidx.compose.material3.DropdownMenu(
                      expanded = timeFilterMenuExpanded,
                      onDismissRequest = { timeFilterMenuExpanded = false }
                    ) {
                      com.example.data.model.GlobalTimeRange.values().forEach { range ->
                        androidx.compose.material3.DropdownMenuItem(
                          text = { Text(range.label, fontSize = 12.sp) },
                          onClick = {
                            viewModel.setGlobalTimeRange(range)
                            timeFilterMenuExpanded = false
                          }
                        )
                      }
                    }
                  }

                  if (selectedNavTab in 0..5) {
                    IconButton(
                      onClick = {
                        val report = viewModel.generateTextSummaryReport()
                        SummaryReportUtils.shareTextReport(
                          context = context,
                          reportContent = report,
                          title = "PACKETIVEX - Network Summary Report"
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
                      onClick = { handleToggleCapture() },
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
                edgePadding = 12.dp
              ) {
                val navItems = listOf(
                  "Dashboard" to Icons.Default.Dashboard,
                  "Monitoring" to Icons.Default.Router,
                  "Cyber AI" to Icons.Default.AutoAwesome,
                  "Intelligence" to Icons.Default.Hub,
                  "Sockets" to Icons.Default.CompareArrows,
                  "History" to Icons.Default.Storage,
                  "Server Sync" to Icons.Default.CloudSync,
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
                    text = {
                      Text(
                        text = title,
                        fontSize = 11.5.sp,
                        fontWeight = if (selectedNavTab == index) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                      )
                    }
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
                  onToggleCapture = { handleToggleCapture() },
                  onScopeChanged = { viewModel.setTimelineScope(it) },
                  onInspectApp = { viewModel.inspectApp(it) },
                  onInspectIp = { viewModel.inspectIp(it) }
                )
                1 -> NetworkMonitoringScreen(
                  networkScopes = monNetworkScopes,
                  selectedScope = monSelectedScope,
                  devices = monScopedDevices,
                  flows = monScopedFlows,
                  services = monScopedServices,
                  alerts = monScopedAlerts,
                  networkInfo = realNetworkInfo,
                  historicalSessions = historicalSessions,
                  onSelectNetworkScope = { viewModel.selectMonNetworkScope(it) },
                  onExportReport = { format, target, timeRange ->
                    val result = viewModel.exportMonReport(format, target, timeRange)
                    SummaryReportUtils.shareTextReport(
                      context = context,
                      reportContent = result,
                      title = "Authorized Network Monitoring Report ($format)"
                    )
                    result
                  },
                  onRefreshDiscovery = {
                    viewModel.selectMonNetworkScope(monSelectedScope.id)
                    viewModel.triggerSubnetDiscovery()
                  }
                )
                2 -> GeminiChatScreen(
                  messages = chatMessages,
                  isGenerating = isChatGenerating,
                  selectedModel = selectedChatModel,
                  onModelSelected = { viewModel.setChatModel(it) },
                  onSendMessage = { text, attachTelemetry -> viewModel.sendChatMessage(text, attachTelemetry) },
                  onClearChat = { viewModel.clearChatHistory() },
                  liveTelemetrySummary = "Interface: ${realNetworkInfo.interfaceName}, Local IP: ${realNetworkInfo.localIpv4}, Devices: ${observedDevices.size}, Packets: ${stats.totalPacketsCaptured}"
                )
                3 -> NetworkIntelligenceScreen(
                  networkInfo = realNetworkInfo,
                  availableInterfaces = availableInterfaces,
                  networkChangeBanner = networkChangeBanner,
                  observedDevices = observedDevices,
                  communicationFlows = communicationFlows,
                  applicationServices = applicationServices,
                  dnsLogs = dnsLogs,
                  liveTrafficStats = liveTrafficStats,
                  networkHealth = networkHealth,
                  securityAlerts = securityAlerts,
                  aiAnalystInsight = aiAnalystInsight,
                  mlModelHealth = mlModelHealth,
                  mlInferences = mlRecentInferences,
                  isMonitoringActive = isIntelMonitoringActive,
                  isDiscoveryScanning = isDiscoveryScanning,
                  onDismissNetworkChangeBanner = { viewModel.dismissNetworkChangeBanner() },
                  onToggleMonitoring = { viewModel.toggleIntelMonitoring() },
                  onTriggerDiscovery = { viewModel.triggerSubnetDiscovery() },
                  onClearData = { viewModel.clearIntelligenceData() },
                  onSelectInterface = { viewModel.selectInterface(it) },
                  onSelectDeviceForDeepAnalysis = { viewModel.selectDeviceForDeepAnalysis(it) },
                  onGenerateAiAnalysis = { viewModel.generateAiAnalystReport() },
                  onRetrainMlModel = { viewModel.trainMlModel() },
                  onUpdateContaminationThreshold = { viewModel.updateMlContaminationThreshold(it) },
                  onTestMlFlow = { r, b, e, j, p -> viewModel.runManualMlInferenceTest(r, b, e, j, p) },
                  onExportReport = { format ->
                    val reportContent = buildString {
                      appendLine("=== NT04 NETWORK INTELLIGENCE REPORT ===")
                      appendLine("Timestamp: ${System.currentTimeMillis()}")
                      appendLine("SSID: ${realNetworkInfo.ssid}")
                      appendLine("Interface: ${realNetworkInfo.interfaceName} (${realNetworkInfo.interfaceType})")
                      appendLine("Local IPv4: ${realNetworkInfo.localIpv4}")
                      appendLine("Subnet Mask: ${realNetworkInfo.subnetMask}")
                      appendLine("Gateway: ${realNetworkInfo.defaultGateway}")
                      appendLine("DNS: ${realNetworkInfo.dnsServers.joinToString(", ")}")
                      appendLine("Observed Devices Count: ${observedDevices.size}")
                    }
                    SummaryReportUtils.shareTextReport(
                      context = context,
                      reportContent = reportContent,
                      title = "NT04 Network Intelligence Report ($format)"
                    )
                    scope.launch { snackbarHostState.showSnackbar("Network Intelligence Report shared") }
                  }
                )
                4 -> ConnectionsScreen(
                  packets = packets,
                  enhancedConnections = enhancedConnections,
                  onFilterPacketsByConnection = { remoteIp, appName ->
                    viewModel.navigateToPacketsWithFilter(appName = appName, ipAddress = remoteIp)
                    selectedNavTab = 7
                  }
                )
                5 -> HistoryDatabaseScreen(
                  sessions = historicalSessions,
                  devices = historicalDevices,
                  isDbConnected = isDbConnected,
                  totalRecordsCount = dbTotalRecordsCount,
                  databaseSizeBytes = databaseSizeBytes,
                  activeSessionId = dbActiveSessionId,
                  lastWriteTimestamp = dbLastWriteTimestamp,
                  selectedHistoricalSession = selectedHistoricalSessionDetails,
                  retentionSettings = retentionSettings,
                  onSelectSession = { viewModel.selectHistoricalSession(it) },
                  onClearSelectedSession = { viewModel.clearSelectedHistoricalSession() },
                  onUpdateRetention = { viewModel.updateRetentionSettings(it) },
                  onEnforceRetentionNow = { viewModel.enforceRetentionNow() },
                  onRefreshMetrics = { viewModel.refreshDatabaseMetrics() }
                )
                6 -> ServerConnectionScreen(
                  config = serverConfig,
                  connectionStatus = connectionStatus,
                  serverHealth = serverHealth,
                  lastPingLatencyMs = lastPingLatencyMs,
                  lastConnectionTimestamp = lastConnectionTimestamp,
                  lastErrorMessage = serverLastErrorMessage,
                  syncState = syncState,
                  syncedRecordsCount = syncedRecordsCount,
                  pendingRecordsCount = pendingRecordsCount,
                  failedRecordsCount = failedRecordsCount,
                  lastSyncTimestamp = lastSyncTimestamp,
                  lastSyncMessage = lastSyncMessage,
                  syncLogs = syncLogs,
                  onUpdateConfig = { viewModel.updateServerConfig(it) },
                  onTestConnection = { viewModel.testServerConnection() },
                  onDisconnect = { viewModel.disconnectServer() },
                  onSyncNow = { viewModel.syncDatabaseNow() }
                )
                7 -> CaptureScreen(
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
                  activeCrossFilter = activeCrossFilter,
                  onClearCrossFilter = { viewModel.clearCrossFilter() },
                  onToggleCapture = { handleToggleCapture() },
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
                8 -> AnalyzeScreen(
                  packets = packets,
                  searchQuery = searchQuery,
                  selectedProtocol = selectedProtocol,
                  detailedApps = detailedApps,
                  detailedIps = detailedIps,
                  conversations = conversations,
                  endpoints = endpoints,
                  protocols = protocols,
                  enhancedProtocols = enhancedProtocols,
                  protocolHierarchy = protocolHierarchy,
                  displayFilters = displayFilterPresets,
                  onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                  onProtocolSelect = { viewModel.selectProtocolFilter(it) },
                  onPacketClick = { viewModel.selectPacket(it) },
                  onInspectApp = { viewModel.inspectApp(it) },
                  onInspectIp = { viewModel.inspectIp(it) },
                  onFilterSelected = { viewModel.setCaptureFilter(it) },
                  onNavigateToPackets = { app, ip, proto ->
                    viewModel.navigateToPacketsWithFilter(app, ip, proto)
                    selectedNavTab = 7
                  }
                )
                9 -> StatisticsScreen(
                  stats = stats,
                  protocols = protocols,
                  lengthBuckets = packetLengthBuckets,
                  ioPoints = ioGraphPoints,
                  selectedInterval = ioGraphInterval,
                  onSelectInterval = { viewModel.setIoGraphInterval(it) }
                )
                10 -> ToolsScreen(
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
                11 -> AlertsScreen(
                  alerts = trafficAlerts,
                  selectedSeverity = selectedAlertSeverity,
                  onSelectSeverity = { viewModel.setSelectedAlertSeverity(it) },
                  onAlertClick = { alert ->
                    val app = detailedApps.find { it.appName.equals(alert.entityName, ignoreCase = true) }
                    if (app != null) viewModel.inspectApp(app)
                  },
                  onInvestigateTarget = { ip, app, proto ->
                    viewModel.navigateToPacketsWithFilter(app, ip, proto)
                    selectedNavTab = 7
                  }
                )
                12 -> PcapLibraryScreen(
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
                13 -> SettingsScreen(
                  userSession = userSession,
                  onSignOut = { viewModel.signOut() },
                  notificationSettings = notificationSettings,
                  onSaveNotificationSettings = { viewModel.saveNotificationSettings(it) },
                  onOpenTargetAppSelector = { viewModel.setShowTargetAppSelector(true) },
                  onOpenSslCertDialog = { viewModel.setShowSslCertDialog(true) },
                  onRequestVpnPermission = {
                    val wasGranted = VpnPermissionManager.requestVpnPermissionIfNeeded(
                      context = context,
                      launcher = vpnPermissionLauncher,
                      onPermissionGranted = {
                        scope.launch {
                          snackbarHostState.showSnackbar("VPN permission is already granted!")
                        }
                      }
                    )
                    if (!wasGranted) {
                      scope.launch {
                        snackbarHostState.showSnackbar("Opening system VPN permission request...")
                      }
                    }
                  }
                )
              }

              // Dialog Modals
              selectedDeviceForDeepAnalysis?.let { device ->
                DeviceDeepAnalysisDialog(
                  device = device,
                  onDismiss = { viewModel.selectDeviceForDeepAnalysis(null) }
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
