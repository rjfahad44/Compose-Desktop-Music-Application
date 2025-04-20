@echo off
echo Building Music audio_player Installer...
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" music-audio_player-setup.iss
if %ERRORLEVEL%==0 (
    echo Installer created at: Output\MyMusicSetup.exe
) else (
    echo Error: Failed to create installer
    exit /b %ERRORLEVEL%
)