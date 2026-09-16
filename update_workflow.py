content = """name: Build APK

on:
  push:
    branches: [ main ]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Restore Debug Keystore
        env:
          DEBUG_KEYSTORE_BASE64: ${{ secrets.DEBUG_KEYSTORE_BASE64 }}
        run: |
          if [ -z "$DEBUG_KEYSTORE_BASE64" ]; then
            echo "Error: DEBUG_KEYSTORE_BASE64 secret is missing."
            echo "You must add the DEBUG_KEYSTORE_BASE64 secret to your GitHub repository."
            echo "1. Go to your AI Studio project, and run 'base64 -w 0 debug.keystore > debug.keystore.base64'"
            echo "2. Copy the contents of the generated debug.keystore.base64 file."
            echo "3. In GitHub, go to Settings -> Secrets and variables -> Actions -> New repository secret"
            echo "4. Name it DEBUG_KEYSTORE_BASE64 and paste the contents."
            exit 1
          fi
          echo "$DEBUG_KEYSTORE_BASE64" | base64 -d > debug.keystore

      - name: Restore Firebase Config
        run: cp app/google-services-public.json app/google-services.json

      - name: Set Release Version
        run: |
          CALC_VERSION_CODE=$(( ${{ github.run_number }} + 3 ))
          CALC_VERSION_NAME="${CALC_VERSION_CODE}.0"
          echo "TAG_NAME=v${CALC_VERSION_NAME}" >> $GITHUB_ENV
          echo "VERSION_NAME=${CALC_VERSION_NAME}" >> $GITHUB_ENV

      - name: Build Debug APK
        run: chmod +x gradlew && ./gradlew assembleDebug

      - name: Upload APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: motor-winding-data-apk
          path: app/build/outputs/apk/debug/app-debug.apk

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ env.TAG_NAME }}
          name: App Version ${{ env.VERSION_NAME }}
          files: app/build/outputs/apk/debug/app-debug.apk
          generate_release_notes: true
"""

with open(".github/workflows/main.yml", "w") as f:
    f.write(content)
print("Workflow updated.")
