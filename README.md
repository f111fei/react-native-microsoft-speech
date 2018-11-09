[![npm][npm]][npm-url]
[![deps][deps]][deps-url]

<h1 align="center">React Native Microsoft Speech</h1>

<p align="center">A speech-to-text library for <a href="https://facebook.github.io/react-native/">React Native.</a></p>

```sh
npm i react-native-microsoft-speech --save
```

## Table of contents
  * [Linking](#linking)
    * [Manually Link Android](#manually-link-android)
    * [Manually Link iOS](#manually-link-ios)
  * [Usage](#usage)
    * [Example](#example)
  * [API](#api)
  * [Events](#events)
  * [Permissions](#permissions)
    * [Android](#android)
    * [iOS](#ios)
  * [Contibutors](#contibutors)

<h2 align="center">Linking</h2>

<p align="center">Manually or automatically link the NativeModule</p>

```sh
react-native link react-native-microsoft-speech
```

### Example

```javascript
import Speech from 'react-native-microsoft-speech';
import React, {Component} from 'react';

class SpeechTest extends Component {
  constructor(props) {
    Speech.onSpeechStart = this.onSpeechStartHandler.bind(this);
    Speech.onSpeechEnd = this.onSpeechEndHandler.bind(this);
    Speech.onSpeechResults = this.onSpeechResultsHandler.bind(this);
  }
  onStartButtonPress(e){
    Speech.start('en-US');
  }
  ...
}
```


<h2 align="center">API</h2>

<p align="center">Static access to the Speech API.</p>

**All methods _now_ return a `new Promise` for `async/await` compatibility.**

Method Name                 | Description                                                                         | Platform
--------------------------- | ----------------------------------------------------------------------------------- | --------
Speech.isAvailable()         | Checks whether a speech recognition service is available on the system.             | Android, iOS
Speech.start(locale)         | Starts listening for speech for a specific locale. Returns null if no error occurs. | Android, iOS
Speech.stop()                | Stops listening for speech. Returns null if no error occurs.                        | Android, iOS
Speech.cancel()              | Cancels the speech recognition. Returns null if no error occurs.                    | Android, iOS
Speech.destroy()             | Destroys the current SpeechRecognizer instance. Returns null if no error occurs.    | Android, iOS
Speech.removeAllListeners()  | Cleans/nullifies overridden `Speech` static methods.                                 | Android, iOS
Speech.isRecognizing()       | Return if the SpeechRecognizer is recognizing.                                      | Android, iOS

<h2 align="center">Events</h2>

<p align="center">Callbacks that are invoked when a native event emitted.</p>

Event Name                          | Description                                            | Event                                           | Platform
----------------------------------- | ------------------------------------------------------ | ----------------------------------------------- | --------
Speech.onSpeechStart(event)          | Invoked when `.start()` is called without error.       | `{ error: false }`                              | Android, iOS
Speech.onSpeechRecognized(event)     | Invoked when speech is recognized.                     | `{ error: false }`                              | Android, iOS
Speech.onSpeechEnd(event)            | Invoked when SpeechRecognizer stops recognition.       | `{ error: false }`                              | Android, iOS
Speech.onSpeechError(event)          | Invoked when an error occurs.                          | `{ error: Description of error as string }`     | Android, iOS
Speech.onSpeechResults(event)        | Invoked when SpeechRecognizer is finished recognizing. | `{ value: [..., 'Speech recognized'] }`         | Android, iOS
Speech.onSpeechPartialResults(event) | Invoked when any results are computed.                 | `{ value: [..., 'Partial speech recognized'] }` | Android, iOS
Speech.onSpeechVolumeChanged(event)  | Invoked when pitch that is recognized changed.         | `{ value: pitch in dB }`                        | Android

<h2 align="center">Permissions</h2>

<p align="center">Arguably the most important part.</p>

### Android
While the included `VoiceTest` app works without explicit permissions checks and requests, it may be necessary to add a permission request for `RECORD_AUDIO` for some configurations.
Since Android M (6.0), [user need to grant permission at runtime (and not during app installation)](https://developer.android.com/training/permissions/requesting.html).
By default, calling the `startSpeech` method will invoke `RECORD AUDIO` permission popup to the user. This can be disabled by passing `REQUEST_PERMISSIONS_AUTO: true` in the options argument.

### iOS
Need to include permissions for `NSMicrophoneUsageDescription` and `NSSpeechRecognitionUsageDescription` inside Info.plist for iOS. See the included `VoiceTest` for how to handle these cases.

```xml
<dict>
  ...
  <key>NSMicrophoneUsageDescription</key>
  <string>Description of why you require the use of the microphone</string>
  <key>NSSpeechRecognitionUsageDescription</key>
  <string>Description of why you require the use of the speech recognition</string>
  ...
</dict>
```

Please see the documentation provided by ReactNative for this: [PermissionsAndroid](http://facebook.github.io/react-native/releases/0.38/docs/permissionsandroid.html)