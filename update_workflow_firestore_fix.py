import re

with open('.github/workflows/main.yml', 'r') as f:
    content = f.read()

old_firestore_step = """
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
          const updateUrl = \\`https://github.com/\\${process.env.GITHUB_REPOSITORY}/releases/download/\\${process.env.TAG_NAME}/app-debug.apk\\`;
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

new_firestore_step = """
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
          const { initializeApp, cert } = require('firebase-admin/app');
          const { getFirestore } = require('firebase-admin/firestore');
          const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
          initializeApp({
            credential: cert(serviceAccount)
          });
          const db = getFirestore();
          const updateUrl = \\`https://github.com/\\${process.env.GITHUB_REPOSITORY}/releases/download/\\${process.env.TAG_NAME}/app-debug.apk\\`;
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

if old_firestore_step.strip() in content:
    content = content.replace(old_firestore_step.strip(), new_firestore_step.strip())
else:
    print("Could not find exact string, trying a regex approach.")
    # More robust replacement
    start_str = "      - name: Update Firestore App Config"
    start_idx = content.find(start_str)
    if start_idx != -1:
        content = content[:start_idx] + new_firestore_step.strip() + "\n"

with open('.github/workflows/main.yml', 'w') as f:
    f.write(content)
print("Workflow updated with fix.")
