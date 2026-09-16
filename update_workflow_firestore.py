import re

with open('.github/workflows/main.yml', 'r') as f:
    content = f.read()

# Update Set Release Version to also export VERSION_CODE
old_set_version = """      - name: Set Release Version
        run: |
          CALC_VERSION_CODE=$(( ${{ github.run_number }} + 3 ))
          CALC_VERSION_NAME="${CALC_VERSION_CODE}.0"
          echo "TAG_NAME=v${CALC_VERSION_NAME}" >> $GITHUB_ENV
          echo "VERSION_NAME=${CALC_VERSION_NAME}" >> $GITHUB_ENV"""

new_set_version = """      - name: Set Release Version
        run: |
          CALC_VERSION_CODE=$(( ${{ github.run_number }} + 3 ))
          CALC_VERSION_NAME="${CALC_VERSION_CODE}.0"
          echo "TAG_NAME=v${CALC_VERSION_NAME}" >> $GITHUB_ENV
          echo "VERSION_NAME=${CALC_VERSION_NAME}" >> $GITHUB_ENV
          echo "VERSION_CODE=${CALC_VERSION_CODE}" >> $GITHUB_ENV"""

content = content.replace(old_set_version, new_set_version)

# Add Firestore update step at the end
firestore_step = """
      - name: Update Firestore App Config
        env:
          FIREBASE_SERVICE_ACCOUNT: ${{ secrets.FIREBASE_SERVICE_ACCOUNT }}
        run: |
          if [ -z "$FIREBASE_SERVICE_ACCOUNT" ]; then
            echo "Error: FIREBASE_SERVICE_ACCOUNT secret is missing."
            exit 1
          fi
          npm install firebase-admin
          node -e "
          const admin = require('firebase-admin');
          const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
          admin.initializeApp({
            credential: admin.credential.cert(serviceAccount)
          });
          const db = admin.firestore();
          const updateUrl = \`https://github.com/\${process.env.GITHUB_REPOSITORY}/releases/download/\${process.env.TAG_NAME}/app-debug.apk\`;
          db.doc('app_config/update').set({
            latestVersionCode: parseInt(process.env.VERSION_CODE, 10),
            latestVersionName: process.env.VERSION_NAME,
            minimumSupportedVersionCode: parseInt(process.env.VERSION_CODE, 10),
            forceUpdate: true,
            updateMessage: 'A new version of Motor Winding Data is available. Please update the app to continue.',
            updateUrl: updateUrl
          }).then(() => {
            console.log('Successfully updated Firestore app_config/update');
            process.exit(0);
          }).catch(error => {
            console.error('Error updating Firestore:', error);
            process.exit(1);
          });
          "
"""

content += firestore_step

with open('.github/workflows/main.yml', 'w') as f:
    f.write(content)
print("Workflow updated with Firestore step.")
