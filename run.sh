Xvfb :99 -screen 0 1920x1080x16 &
export DISPLAY=:99.0
cd /root/work && chmod +x ./gradlew && ./gradlew run
