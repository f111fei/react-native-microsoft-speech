package com.reactnative.ms;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.PermissionListener;
import com.microsoft.cognitiveservices.speech.CancellationDetails;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

public class SpeechModule extends ReactContextBaseJavaModule {

    final ReactApplicationContext reactContext;
    private SpeechConfig speechConfig = null;
    private SpeechRecognizer recognizer = null;
    private boolean isRecognizing = false;
    private MicrophoneStream microphoneStream;
    private String logTag = "ms-speech";

    private MicrophoneStream createMicrophoneStream() {
        if (microphoneStream != null) {
            microphoneStream.close();
            microphoneStream = null;
        }

        microphoneStream = new MicrophoneStream();
        return microphoneStream;
    }

    public SpeechModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    private void startListening(String locale, ReadableMap opts) throws Exception {
        if (recognizer != null) {
            recognizer.close();
            recognizer = null;
        }
        if (speechConfig == null) {
            throw new Exception("Please setKeyAndRegion first");
        }
        speechConfig.setSpeechRecognitionLanguage(locale);

        final AudioConfig audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
        final SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioInput);
        this.recognizer = recognizer;

        recognizer.canceled.addEventListener((o, speechRecognitionCanceledEventArgs) -> {
            this.onEndOfSpeech();
        });

        recognizer.sessionStarted.addEventListener((o, sessionEventArgs) -> {
            this.onReadyForSpeech();
        });

        recognizer.sessionStopped.addEventListener((o, sessionEventArgs) -> {
            this.onEndOfSpeech();
        });

        recognizer.speechStartDetected.addEventListener((o, recognitionEventArgs) -> {
            this.onBeginningOfSpeech();
        });

        recognizer.speechEndDetected.addEventListener((o, recognitionEventArgs) -> {
            this.onEndOfSpeech();
        });

        recognizer.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
            final String s = speechRecognitionResultEventArgs.getResult().getText();
            Log.i(logTag, "Intermediate result received: " + s);
            this.onPartialResult(s);
        });

        final Future<SpeechRecognitionResult> task = recognizer.recognizeOnceAsync();
        setOnTaskCompletedListener(task, result -> {
            String s = result.getText();
            if (result.getReason() != ResultReason.RecognizedSpeech) {
                String errorDetails = (result.getReason() == ResultReason.Canceled) ? CancellationDetails.fromResult(result).getErrorDetails() : "";
                s = "Recognition failed with " + result.getReason() + System.lineSeparator() + errorDetails;
                this.onError(s);
            } else {
                Log.i(logTag, "Recognizer returned: " + s);
                this.onResult(s);
                this.onSpeechRecognized();
            }
            if (recognizer != null) {
                recognizer.close();
            }
            this.onEndOfSpeech();
        });
    }

    @Override
    public String getName() {
        return "RCTMSSpeech";
    }

    @ReactMethod
    public void setKeyAndRegion(String key, String region) {
        speechConfig = SpeechConfig.fromSubscription(key, region);
    }

    @ReactMethod
    public void startSpeech(String locale, final ReadableMap opts, final Callback callback) {
        if (!isPermissionGranted() && opts.getBoolean("REQUEST_PERMISSIONS_AUTO")) {
            String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};
            if (this.getCurrentActivity() != null) {
                ((ReactActivity) this.getCurrentActivity()).requestPermissions(PERMISSIONS, 1, new PermissionListener() {
                    public boolean onRequestPermissionsResult(final int requestCode,
                                                              @NonNull final String[] permissions,
                                                              @NonNull final int[] grantResults) {
                        boolean permissionsGranted = true;
                        for (int i = 0; i < permissions.length; i++) {
                            final boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                            permissionsGranted = permissionsGranted && granted;
                        }

                        return permissionsGranted;
                    }
                });
            }
            return;
        }

        Handler mainHandler = new Handler(this.reactContext.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    startListening(locale, opts);
                    isRecognizing = true;
                    callback.invoke(false);
                } catch (Exception e) {
                    callback.invoke(e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void stopSpeech(final Callback callback) {
        Handler mainHandler = new Handler(this.reactContext.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (recognizer != null) {
                        recognizer.close();
                    }
                    isRecognizing = false;
                    callback.invoke(false);
                } catch(Exception e) {
                    callback.invoke(e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void cancelSpeech(final Callback callback) {
        Handler mainHandler = new Handler(this.reactContext.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (recognizer != null) {
                        recognizer.close();
                    }
                    isRecognizing = false;
                    callback.invoke(false);
                } catch(Exception e) {
                    callback.invoke(e.getMessage());
                }
            }
        });
    }

    @ReactMethod
    public void destroySpeech(final Callback callback) {
        Handler mainHandler = new Handler(this.reactContext.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (recognizer != null) {
                        recognizer.close();
                    }
                    recognizer = null;
                    isRecognizing = false;
                    callback.invoke(false);
                } catch(Exception e) {
                    callback.invoke(e.getMessage());
                }
            }
        });
    }

    private boolean isPermissionGranted() {
        String permission = Manifest.permission.RECORD_AUDIO;
        int res = getReactApplicationContext().checkCallingOrSelfPermission(permission);
        return res == PackageManager.PERMISSION_GRANTED;
    }

    @ReactMethod
    public void isRecognizing(Callback callback) {
        callback.invoke(isRecognizing);
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        this.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    public void onBeginningOfSpeech() {
        WritableMap event = Arguments.createMap();
        event.putBoolean("error", false);
        sendEvent("onSpeechStart", event);
        Log.d("ASR", "onBeginningOfSpeech()");
    }

    public void onSpeechRecognized() {
        WritableMap event = Arguments.createMap();
        event.putBoolean("error", false);
        sendEvent("onSpeechRecognized", event);
        Log.d("ASR", "onSpeechRecognized()");
    }

    public void onEndOfSpeech() {
        WritableMap event = Arguments.createMap();
        event.putBoolean("error", false);
        sendEvent("onSpeechEnd", event);
        Log.d("ASR", "onEndOfSpeech()");
        isRecognizing = false;
    }

    public void onError(String errorMessage) {
        WritableMap event = Arguments.createMap();
        event.putString("error", errorMessage);
        sendEvent("onSpeechError", event);
        Log.d("ASR", "onError() - " + errorMessage);
    }

    public void onPartialResult(String result) {
        WritableMap event = Arguments.createMap();
        event.putString("value", result);
        sendEvent("onSpeechPartialResult", event);
        Log.d("ASR", "onPartialResult()");
    }

    public void onReadyForSpeech() {
        WritableMap event = Arguments.createMap();
        event.putBoolean("error", false);
        sendEvent("onSpeechStart", event);
        Log.d("ASR", "onReadyForSpeech()");
    }

    public void onResult(String result) {
        WritableMap event = Arguments.createMap();
        event.putString("value", result);
        sendEvent("onSpeechResult", event);
        Log.d("ASR", "onResult()");
    }

    private <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {
        s_executorService.submit(() -> {
            T result = task.get();
            listener.onCompleted(result);
            return null;
        });
    }

    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }

    private static ExecutorService s_executorService;
    static {
        s_executorService = Executors.newCachedThreadPool();
    }
}
