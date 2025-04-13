; Script generated by the Inno Setup Script Wizard.
#define MyAppName "MusicPlayer"
#define MyAppVersion "1.0.0"
#define MyAppPublisher "Muhammad Fahad Alam"
#define MyAppExeName "MusicPlayer.exe" ; Confirm this matches the actual .exe name

[Setup]
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
OutputBaseFilename=MusicPlayerSetup
Compression=lzma
SolidCompression=yes
; Use .ico file, not .xml (if you have one)
SetupIconFile=C:\Users\USER\Downloads\Compose-Desktop-App\src\main\resources\images\icon.ico
PrivilegesRequired=admin

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Update this path to match your actual distributable folder
Source: "C:\Users\USER\Downloads\Compose-Desktop-App\build\compose\binaries\main\app\MusicPlayer\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
; Adjust path if .exe is not in bin/
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
; Adjust path if .exe is not in bin/
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent