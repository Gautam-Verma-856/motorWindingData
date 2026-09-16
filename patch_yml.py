with open(".github/workflows/main.yml", "r") as f:
    content = f.read()

target = """      - name: Restore Firebase Config
        run: cp app/google-services-public.json app/google-services.json
      - name: Build Debug APK"""

replacement = """      - name: Restore Firebase Config
        run: cp app/google-services-public.json app/google-services.json
      - name: Calculate Version
        id: version
        run: |
          # Use github.run_number to ensure monotonically increasing integer.
          # Offset by 3 so the next build is >= 4.
          CALC_VERSION_CODE=$(( ${{ github.run_number }} + 3 ))
          CALC_VERSION_NAME="${CALC_VERSION_CODE}.0"
          echo "VERSION_CODE=$CALC_VERSION_CODE" >> $GITHUB_ENV
          echo "VERSION_NAME=$CALC_VERSION_NAME" >> $GITHUB_ENV
          echo "Calculated versionCode: $CALC_VERSION_CODE"
          echo "Calculated versionName: $CALC_VERSION_NAME"
      - name: Build Debug APK"""
      
if target in content:
    content = content.replace(target, replacement)
else:
    print("Could not find insertion point!")

with open(".github/workflows/main.yml", "w") as f:
    f.write(content)

print("Patched main.yml")
