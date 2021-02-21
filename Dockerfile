FROM adoptopenjdk:11-jdk-openj9-bionic

RUN apt-get update && apt-get install -y gnupg2 unzip

# Chrome
RUN curl -O https://dl-ssl.google.com/linux/linux_signing_key.pub \
    && apt-key add ./linux_signing_key.pub \
    && /bin/bash -c 'echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list' \
    && apt-get update \
    && apt-get install -y google-chrome-stable xvfb

# ChormeDriver
RUN CHROME_VERSION=$(google-chrome-stable --version | sed -e 's/Google Chrome //' -e 's/\..*//') \
    && CHROMEDRIVER_VERSION=$(curl -sS chromedriver.storage.googleapis.com/LATEST_RELEASE_$CHROME_VERSION) \
    && mkdir -p /opt/chromedriver-$CHROMEDRIVER_VERSION \
    && curl -sS -o /tmp/chromedriver_linux64.zip http://chromedriver.storage.googleapis.com/$CHROMEDRIVER_VERSION/chromedriver_linux64.zip \
    && unzip -qq /tmp/chromedriver_linux64.zip -d /opt/chromedriver-$CHROMEDRIVER_VERSION \
    && rm /tmp/chromedriver_linux64.zip \
    && chmod +x /opt/chromedriver-$CHROMEDRIVER_VERSION/chromedriver \
    && ln -fs /opt/chromedriver-$CHROMEDRIVER_VERSION/chromedriver /usr/local/bin/chromedriver

CMD rm -f /tmp/.X99-lock && \
    Xvfb :99 -screen 0 1024x768x16 & \
    while [ ! -f /tmp/.X99-lock ]; do sleep 1; done && \
    google-chrome --no-sandbox --window-size=1920,1080 --disable-gpu --disable-dev-shm-usage & \
    cd /root/work && chmod +x ./gradlew && ./gradlew run
