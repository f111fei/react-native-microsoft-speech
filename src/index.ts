'use strict';
import {
  NativeModules,
  NativeEventEmitter,
  Platform,
} from 'react-native';

const { MSSpeech } = NativeModules;

// NativeEventEmitter is only availabe on React Native platforms, so this conditional is used to avoid import conflicts in the browser/server
const voiceEmitter = Platform.OS !== "web" ? new NativeEventEmitter(MSSpeech) : null;

export class RCTMSSpeech {
  private _loaded: boolean;
  private _listeners: any;
  private _events: { 'onSpeechStart': any; 'onSpeechRecognized': any; 'onSpeechEnd': any; 'onSpeechError': any; 'onSpeechResult': any; 'onSpeechPartialResult': any; };
  onSpeechStart: any;
  onSpeechRecognized: any;
  onSpeechEnd: any;
  onSpeechError: any;
  onSpeechResult: any;
  onSpeechPartialResult: any;

  constructor() {
    this._loaded = false;
    this._listeners = null;
    this._events = {
      'onSpeechStart': this._onSpeechStart.bind(this),
      'onSpeechRecognized': this._onSpeechRecognized.bind(this),
      'onSpeechEnd': this._onSpeechEnd.bind(this),
      'onSpeechError': this._onSpeechError.bind(this),
      'onSpeechResult': this._onSpeechResult.bind(this),
      'onSpeechPartialResult': this._onSpeechPartialResult.bind(this),
    };
  }
  removeAllListeners() {
    MSSpeech.onSpeechStart = null;
    MSSpeech.onSpeechRecognized = null;
    MSSpeech.onSpeechEnd = null;
    MSSpeech.onSpeechError = null;
    MSSpeech.onSpeechResult = null;
    MSSpeech.onSpeechPartialResult = null;
  }
  destroy() {
    if (!this._loaded && !this._listeners) {
      return Promise.resolve();
    }
    return new Promise((resolve, reject) => {
      MSSpeech.destroySpeech((error) => {
        if (error) {
          reject(new Error(error));
        } else {
          if (this._listeners) {
            this._listeners.map((listener, index) => listener.remove());
            this._listeners = null;
          }
          resolve();
        }
      });
    });
  }
  setKeyAndRegion(key, region) {
    MSSpeech.setKeyAndRegion(key, region);
  }
  start(locale, options = {}) {
    if (!this._loaded && !this._listeners && voiceEmitter !== null) {
      this._listeners = Object.keys(this._events)
        .map((key, index) => voiceEmitter.addListener(key, this._events[key]));
    }

    return new Promise((resolve, reject) => {
      const callback = (error) => {
        if (error) {
          reject(new Error(error));
        } else {
          resolve();
        }
      };
      if (Platform.OS === 'android') {
        MSSpeech.startSpeech(locale, Object.assign({
          EXTRA_LANGUAGE_MODEL: "LANGUAGE_MODEL_FREE_FORM",
          EXTRA_MAX_RESULTS: 5,
          EXTRA_PARTIAL_RESULTS: true,
          REQUEST_PERMISSIONS_AUTO: true,
        }, options), callback);
      } else {
        MSSpeech.startSpeech(callback);
      }
    });
  }
  stop() {
    if (!this._loaded && !this._listeners) {
      return Promise.resolve();
    }
    return new Promise((resolve, reject) => {
      MSSpeech.stopSpeech((error) => {
        if (error) {
          reject(new Error(error));
        } else {
          resolve();
        }
      });
    });
  }
  cancel() {
    if (!this._loaded && !this._listeners) {
      return Promise.resolve();
    }
    return new Promise((resolve, reject) => {
      MSSpeech.cancelSpeech((error) => {
        if (error) {
          reject(new Error(error));
        } else {
          resolve();
        }
      });
    });
  }
  isAvailable() {
    return true;
  }
  isRecognizing() {
    return new Promise((resolve, reject) => {
      MSSpeech.isRecognizing(isRecognizing => resolve(isRecognizing));
    });
  }
  private _onSpeechStart(e) {
    if (this.onSpeechStart) {
      this.onSpeechStart(e);
    }
  }
  private _onSpeechRecognized(e) {
    if (this.onSpeechRecognized) {
      this.onSpeechRecognized(e);
    }
  }
  private _onSpeechEnd(e) {
    if (this.onSpeechEnd) {
      this.onSpeechEnd(e);
    }
  }
  private _onSpeechError(e) {
    if (this.onSpeechError) {
      this.onSpeechError(e);
    }
  }
  private _onSpeechResult(e) {
    if (this.onSpeechResult) {
      this.onSpeechResult(e);
    }
  }
  private _onSpeechPartialResult(e) {
    if (this.onSpeechPartialResult) {
      this.onSpeechPartialResult(e);
    }
  }
}

const speech = new RCTMSSpeech();
export default speech;
