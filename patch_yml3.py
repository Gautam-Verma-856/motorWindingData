with open(".github/workflows/main.yml", "r") as f:
    content = f.read()

target = """      - name: Restore Firebase Config
        run: cp app/google-services-public.json app/google-services.json
      - name: Calculate Version
        id: version
        run: |
          CALC_VERSION_CODE=$(( ${{ github.run_number }} + 3 ))
          CALC_VERSION_NAME="${CALC_VERSION_CODE}.0"
          echo "VERSION_CODE=$CALC_VERSION_CODE" >> $GITHUB_ENV
          echo "VERSION_NAME=$CALC_VERSION_NAME" >> $GITHUB_ENV
          echo "Calculated versionCode: $CALC_VERSION_CODE"
          echo "Calculated versionName: $CALC_VERSION_NAME"
      - name: Build Debug APK
        run: chmod +x gradlew && ./gradlew assembleDebug"""

replacement = """      - name: Restore Firebase Config
        run: cp app/google-services-public.json app/google-services.json
      - name: Build Debug APK
        run: chmod +x gradlew && ./gradlew assembleDebug"""

content = content.replace(target, replacement)
with open(".github/workflows/main.yml", "w") as f:
    f.write(content)
