Speech Trigger
==============

Android Speech Recognizer service based on
https://github.com/cmusphinx/pocketsphinx-android-demo

Demonstrates the basic architecture of a multi-lingual speech trigger service, i.e. a speech service that waits as long as a given phrase is heard, but then returns the phrase immediately to the caller (e.g. Kõnele). Comes with on-device sphinx acoustic models and a tiny lexicon, for English and Estonian. Can be installed (e.g. with "adb install") on Android Things and Android Wear. Makes most sense in combination with Kõnele and Android Things (see https://github.com/Kaljurand/K6nele/tree/master/docs/android_things).

Current recognition results:

- English works unreliably
- Estonian does not work at all
- Wear does not work at all

Models from:

- English: https://github.com/cmusphinx/pocketsphinx-android-demo
- Estonian: https://github.com/alumae/et-pocketsphinx-tutorial
