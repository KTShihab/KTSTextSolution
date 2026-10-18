# KTSTextSolution Project Rules & Workflow

## Build & Deployment Rule

Whenever a new version or change is built for this app:
1. **Build the Debug APK**:
   Set `JAVA_HOME` to Android Studio's bundled JBR and assemble the APK:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
   .\gradlew.bat assembleDebug
   ```

2. **Save to PC Downloads Folder**:
   Copy the generated APK to the user's system Downloads directory:
   ```powershell
   Copy-Item -Path "app\build\outputs\apk\debug\app-debug.apk" -Destination "$env:USERPROFILE\Downloads\KTSTextSolution.apk" -Force
   ```

3. **Save to Phone's Download Folder**:
   Push the APK directly to the connected device's Download directory:
   ```powershell
   & "C:\Users\Shiha\AppData\Local\Android\Sdk\platform-tools\adb.exe" push "app\build\outputs\apk\debug\app-debug.apk" "/sdcard/Download/KTSTextSolution.apk"
   ```

4. **Install on the Connected Phone**:
   Directly install/update the APK on the connected phone:
   ```powershell
   & "C:\Users\Shiha\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
   ```
