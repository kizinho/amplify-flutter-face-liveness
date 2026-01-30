package br.com.webeleven.rekognitionFaceLiveness.rekognition_face_liveness

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import android.util.Log
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AlreadyConfiguredException

/** FaceLivenessDetectorPlugin */
class FaceLivenessDetectorPlugin: FlutterPlugin {
  private val TAG = "FaceLivenessPlugin"

  /// The event channel that will handle communication between Flutter and native Android
  private lateinit var eventChannel : EventChannel

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    val handler = EventStreamHandler()
    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "face_liveness_event")
    eventChannel.setStreamHandler(handler)

    flutterPluginBinding
      .platformViewRegistry
      .registerViewFactory("face_liveness_view", FaceLivenessViewFactory(handler))

    // Only add plugins and configure once (main engine). In background engine (e.g. FCM)
    // Amplify is already configured -> catch AlreadyConfiguredException and skip.
    try {
      Amplify.addPlugin(AWSCognitoAuthPlugin())
      Amplify.configure(flutterPluginBinding.applicationContext)
      Log.i(TAG, "FaceLivenessPlugin initialized with custom credentials provider")
    } catch (e: AlreadyConfiguredException) {
      Log.i(TAG, "Amplify already configured; skipping in secondary engine")
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    // No cleanup needed
  }
}

// Add backward compatibility with the original plugin name
/** RekognitionFaceLivenessPlugin */
class RekognitionFaceLivenessPlugin: FlutterPlugin {
  private val TAG = "FaceLivenessPlugin"
  private val delegate = FaceLivenessDetectorPlugin()

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    delegate.onAttachedToEngine(flutterPluginBinding)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    delegate.onDetachedFromEngine(binding)
  }
}

class EventStreamHandler: EventChannel.StreamHandler {
  private var eventSink: EventChannel.EventSink? = null

  fun onComplete() {
    eventSink?.success("complete")
  }

  fun onError(code: String) {
    eventSink?.success(code)
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    eventSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventSink = null
  }
}
